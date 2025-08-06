package com.github.kevskitv.webrtckmp.externals

internal external interface RTCRtpParameters {
    val rtcp: RTCRtcpParameters
}

internal expect val RTCRtpParameters.codes: List<RTCRtpCodecParameters>
internal expect val RTCRtpParameters.headerExtensions: List<Any>
