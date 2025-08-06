package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.BundlePolicy
import com.github.kevskitv.webrtckmp.IceTransportPolicy
import com.github.kevskitv.webrtckmp.RtcpMuxPolicy

internal fun BundlePolicy.toStringValue(): String = when (this) {
    BundlePolicy.Balanced -> "balanced"
    BundlePolicy.MaxBundle -> "max-bundle"
    BundlePolicy.MaxCompat -> "max-compat"
}

internal fun IceTransportPolicy.toStringValue(): String = when (this) {
    IceTransportPolicy.All -> "all"
    IceTransportPolicy.Relay -> "relay"
    else -> "all"
}

internal fun RtcpMuxPolicy.toStringValue(): String = when (this) {
    RtcpMuxPolicy.Negotiate -> "negotiate"
    RtcpMuxPolicy.Require -> "require"
}
