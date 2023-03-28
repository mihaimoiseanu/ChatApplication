package com.kroncoders.android.networking.webrtc.audio

sealed interface AudioDevice {

    /** The friendly name of the device.*/
    val name: String

    /** An [AudioDevice] representing a Bluetooth Headset.*/
    data class BluetoothHeadset internal constructor(override val name: String = "Bluetooth") : AudioDevice

    /** An [AudioDevice] representing a Wired Headset.*/
    data class WiredHeadset internal constructor(override val name: String = "Wired Headset") : AudioDevice

    /** An [AudioDevice] representing the Earpiece.*/
    data class Earpiece internal constructor(override val name: String = "Earpiece") : AudioDevice

    /** An [AudioDevice] representing the Speakerphone.*/
    data class Speakerphone internal constructor(override val name: String = "Speakerphone") : AudioDevice
}