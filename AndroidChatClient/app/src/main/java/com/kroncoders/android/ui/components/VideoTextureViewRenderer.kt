package com.kroncoders.android.ui.components

import android.content.Context
import android.content.res.Resources
import android.graphics.SurfaceTexture
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import org.webrtc.*
import org.webrtc.RendererCommon.RendererEvents
import java.util.concurrent.CountDownLatch

/**
 * Custom [TextureView] used to render local/incoming videos on the screen.
 */
open class VideoTextureViewRenderer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextureView(context, attrs), VideoSink, SurfaceTextureListener {

    /**
     * Cached resource name
     */
    private val resourceName: String = getResourceName()

    /**
     * Renderer used to render the video
     */
    private val eglRenderer: EglRenderer = EglRenderer(resourceName)

    /**
     * Callback used for reporting render events
     */
    private var rendererEvents: RendererEvents? = null

    /**
     * Handler to access the UI thread
     */
    private val uiThreadHandler = Handler(Looper.getMainLooper())

    /**
     * Whether the first frame has been rendered or not
     */
    private var isFirstFrameRendered = false

    /**
     * The rotated [VideoFrame] width
     */
    private var rotatedFrameWidth = 0

    /**
     * The rotated [VideoFrame] width
     */
    private var rotatedFrameHeight = 0

    /**
     * The rotated [VideoFrame] rotation
     */
    private var frameRotation = 0

    /**
     * Called when a new frame is received. Sends the frame to be rendered.
     *
     * @param frame The [VideoFrame] received from WebRTC connection to draw on the screen
     */
    override fun onFrame(frame: VideoFrame) {
        eglRenderer.onFrame(frame)
        updateFrameData(frame)
    }

    private fun updateFrameData(frame: VideoFrame) {
        if (isFirstFrameRendered) {
            rendererEvents?.onFirstFrameRendered()
            isFirstFrameRendered = true
        }

        if (frame.rotatedWidth != rotatedFrameWidth ||
            frame.rotatedHeight != rotatedFrameHeight ||
            frame.rotation != frameRotation
        ) {
            rotatedFrameWidth = frame.rotatedWidth
            rotatedFrameHeight = frame.rotatedHeight
            frameRotation = frame.rotation

            uiThreadHandler.post {
                rendererEvents?.onFrameResolutionChanged(rotatedFrameWidth, rotatedFrameHeight, rotatedFrameWidth)
            }
        }
    }

    /**
     * After the view is laid out we need to set the correct layout aspect ratio to the renderer so that the image
     * is scaled correctly.
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        eglRenderer.setLayoutAspectRatio((right - left) / bottom.toFloat() - top)
    }

    /**
     * Initialise the renderer. Should be called from the main thread.
     *
     * @param sharedContext [EglBase.Context]
     * @param rendererEvents Sets the render event listener.
     */
    fun init(
        sharedContext: EglBase.Context,
        rendererEvents: RendererEvents
    ) {
        ThreadUtils.checkIsOnMainThread()
        this.rendererEvents = rendererEvents
        eglRenderer.init(sharedContext, EglBase.CONFIG_PLAIN, GlRectDrawer())
    }

    /**
     * [SurfaceTextureListener] callback that lets us know when a surface texture is ready and we can draw on it.
     */
    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        eglRenderer.createEglSurface(surfaceTexture)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
    }

    /**
     * [SurfaceTextureListener] callback that lets us know when a surface texture is destroyed we need to stop drawing
     * on it.
     */
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        val completionLatch = CountDownLatch(1)
        eglRenderer.releaseEglSurface { completionLatch.countDown() }
        ThreadUtils.awaitUninterruptibly(completionLatch)
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    override fun onDetachedFromWindow() {
        eglRenderer.release()
        super.onDetachedFromWindow()
    }

    private fun getResourceName(): String {
        return try {
            resources.getResourceEntryName(id) + ": "
        } catch (e: Resources.NotFoundException) {
            ""
        }
    }
}