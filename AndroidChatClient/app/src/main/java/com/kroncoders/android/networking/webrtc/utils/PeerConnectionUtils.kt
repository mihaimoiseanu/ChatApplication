package com.kroncoders.android.networking.webrtc.utils

import org.webrtc.AddIceObserver
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun PeerConnection.addRtcIceCandidate(iceCandidate: IceCandidate): Result<Unit> {
    return suspendCoroutine { continuation ->
        addIceCandidate(
            iceCandidate,
            object : AddIceObserver {
                override fun onAddSuccess() {
                    continuation.resume(Result.success(Unit))
                }

                override fun onAddFailure(error: String?) {
                    continuation.resume(Result.failure(RuntimeException(error)))
                }

            }
        )
    }
}