@file:OptIn(ExperimentalForeignApi::class)

package com.github.kevskitv.webrtckmp.internal

import WebRTC.RTCIceServer
import WebRTC.RTCTlsCertPolicy
import com.github.kevskitv.webrtckmp.IceServer
import com.github.kevskitv.webrtckmp.TlsCertPolicy
import kotlinx.cinterop.ExperimentalForeignApi

internal fun IceServer.toPlatform(): RTCIceServer {
    return RTCIceServer(
        uRLStrings = urls,
        username = username,
        credential = password,
        tlsCertPolicy = tlsCertPolicy.toPlatform(),
        hostname = hostname,
        tlsAlpnProtocols = tlsAlpnProtocols,
        tlsEllipticCurves = tlsEllipticCurves
    )
}

private fun TlsCertPolicy.toPlatform(): RTCTlsCertPolicy {
    return when (this) {
        TlsCertPolicy.TlsCertPolicySecure -> RTCTlsCertPolicy.RTCTlsCertPolicySecure

        TlsCertPolicy.TlsCertPolicyInsecureNoCheck -> {
            RTCTlsCertPolicy.RTCTlsCertPolicyInsecureNoCheck
        }
    }
}
