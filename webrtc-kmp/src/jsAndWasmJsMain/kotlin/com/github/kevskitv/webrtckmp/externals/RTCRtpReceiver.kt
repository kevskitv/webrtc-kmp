package com.github.kevskitv.webrtckmp.externals

internal external interface RTCRtpReceiver {
    val track: PlatformMediaStreamTrack

    fun getParameters(): RTCRtpParameters
}
