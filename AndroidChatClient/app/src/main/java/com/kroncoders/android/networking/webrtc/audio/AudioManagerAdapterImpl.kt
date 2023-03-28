package com.kroncoders.android.networking.webrtc.audio

import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import io.getstream.log.taggedLogger

internal class AudioManagerAdapterImpl(
    private val context: Context,
    private val audioManager: AudioManager,
    private val audioFocusRequest: AudioFocusRequestWrapper = AudioFocusRequestWrapper(),
    private val audioFocusChangeListener: AudioManager.OnAudioFocusChangeListener
) : AudioManagerAdapter {

    private val logger by taggedLogger("Call:AudioManager")

    private var savedAudioMode = 0
    private var savedIsMicrophoneMuted = false
    private var savedSpeakerphoneEnabled = false
    private var audioRequest: AudioFocusRequest? = null

    init {
        logger.i { "<init> audioFocusChangeListener: $audioFocusChangeListener" }
    }

    override fun hasEarpiece(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)
    }

    override fun hasSpeakerphone(): Boolean {
        return if (context.packageManager.hasSystemFeature(PackageManager.FEATURE_AUDIO_OUTPUT)) {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            for (device in devices) {
                if (device.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER) {
                    return true
                }
            }
            false
        } else {
            true
        }
    }

    override fun setAudioFocus() {
        audioRequest = audioFocusRequest.buildRequest(audioFocusChangeListener)
        audioRequest?.let {
            val result = audioManager.requestAudioFocus(it)
            logger.i { "[setAudioFocus] #new; completed: ${result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED}" }
        }
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    override fun enableBluetoothSco(enable: Boolean) {
        logger.i { "[enableBluetoothSco] enable: $enable" }
        audioManager.run { if (enable) startBluetoothSco() else stopBluetoothSco() }
    }

    override fun enableSpeakerphone(enable: Boolean) {
        logger.i { "[enableSpeakerphone] enable: $enable" }
        audioManager.isSpeakerphoneOn = enable
    }

    override fun mute(mute: Boolean) {
        logger.i { "[mute] mute: $mute" }
        audioManager.isMicrophoneMute = mute
    }

    override fun cacheAudioState() {
        logger.i { "[cacheAudioState] no args" }
        savedAudioMode = audioManager.mode
        savedIsMicrophoneMuted = audioManager.isMicrophoneMute
        savedSpeakerphoneEnabled = audioManager.isSpeakerphoneOn
    }

    override fun restoreAudioState() {
        logger.i { "[cacheAudioState] no args" }
        audioManager.mode = savedAudioMode
        mute(savedIsMicrophoneMuted)
        enableSpeakerphone(savedSpeakerphoneEnabled)
        audioRequest?.let {
            logger.d { "[cacheAudioState] abandonAudioFocusRequest: $it" }
            audioManager.abandonAudioFocusRequest(it)
        }
    }
}