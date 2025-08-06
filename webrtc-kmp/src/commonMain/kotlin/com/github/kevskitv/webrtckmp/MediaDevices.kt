package com.github.kevskitv.webrtckmp

internal const val DEFAULT_VIDEO_WIDTH = 1280
internal const val DEFAULT_VIDEO_HEIGHT = 720
internal const val DEFAULT_FRAME_RATE = 30

public interface MediaDevices {
    public suspend fun getUserMedia(
        streamConstraints: MediaStreamConstraintsBuilder.() -> Unit = {},
    ): MediaStream

    public suspend fun getUserMedia(
        audio: Boolean = false,
        video: Boolean = false,
    ): MediaStream =
        getUserMedia {
            if (audio) audio()
            if (video) video()
        }

    public suspend fun getDisplayMedia(): MediaStream

    public suspend fun supportsDisplayMedia(): Boolean

    public suspend fun enumerateDevices(): List<MediaDeviceInfo>

    public companion object : MediaDevices by mediaDevices
}

internal expect val mediaDevices: MediaDevices

internal fun MediaTrackConstraints.toMandatoryMap(): Map<Any?, *> =
    mutableMapOf<Any?, String>().apply {
        echoCancellation?.exact?.let { this += "googEchoCancellation" to "$it" }
        autoGainControl?.exact?.let { this += "googAutoGainControl" to "$it" }
        noiseSuppression?.exact?.let { this += "googNoiseSuppression" to "$it" }
        highpassFilter?.exact?.let { this += "googHighpassFilter" to "$it" }
        typingNoiseDetection?.exact?.let { this += "googTypingNoiseDetection" to "$it" }
    }

internal fun MediaTrackConstraints.toOptionalMap(): Map<Any?, *> =
    mutableMapOf<Any?, String>().apply {
        echoCancellation?.ideal?.let { this += "googEchoCancellation" to "$it" }
        autoGainControl?.ideal?.let { this += "googAutoGainControl" to "$it" }
        noiseSuppression?.ideal?.let { this += "googNoiseSuppression" to "$it" }
        highpassFilter?.ideal?.let { this += "googHighpassFilter" to "$it" }
        typingNoiseDetection?.ideal?.let { this += "googTypingNoiseDetection" to "$it" }
    }
