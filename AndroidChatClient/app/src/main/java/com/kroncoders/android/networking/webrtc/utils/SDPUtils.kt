package com.kroncoders.android.networking.webrtc.utils

import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend inline fun createValue(
    crossinline call: (SdpObserver) -> Unit
): Result<SessionDescription> = suspendCoroutine {

    val observer = object : SdpObserver {

        /**
         * Handling of create values.
         */
        override fun onCreateSuccess(sdp: SessionDescription?) {
            if (sdp != null) {
                it.resume(Result.success(sdp))
            } else {
                it.resume(Result.failure(RuntimeException("SessionDescription is null!")))
            }
        }

        override fun onCreateFailure(error: String?) = it.resume(Result.failure(RuntimeException(error)))

        /**
         * We ignore set results.
         */
        override fun onSetSuccess() = Unit
        override fun onSetFailure(error: String?) = Unit
    }

    call(observer)
}

suspend inline fun setValue(
    crossinline call: (SdpObserver) -> Unit
): Result<Unit> = suspendCoroutine {
    val observer = object : SdpObserver {
        /**
         * We ignore create results.
         */
        override fun onCreateSuccess(sdp: SessionDescription?) = Unit
        override fun onCreateFailure(error: String?) = Unit


        override fun onSetSuccess() = it.resume(Result.success(Unit))
        override fun onSetFailure(error: String?) = it.resume(Result.failure(RuntimeException(error)))
    }
    call(observer)
}