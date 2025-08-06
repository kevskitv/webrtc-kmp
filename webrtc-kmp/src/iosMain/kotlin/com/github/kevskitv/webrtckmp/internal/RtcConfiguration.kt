@file:OptIn(ExperimentalForeignApi::class)

package com.github.kevskitv.webrtckmp.internal

import WebRTC.RTCBundlePolicy
import WebRTC.RTCConfiguration
import WebRTC.RTCContinualGatheringPolicy
import WebRTC.RTCIceTransportPolicy
import WebRTC.RTCRtcpMuxPolicy
import WebRTC.RTCSdpSemantics
import com.github.kevskitv.webrtckmp.BundlePolicy
import com.github.kevskitv.webrtckmp.ContinualGatheringPolicy
import com.github.kevskitv.webrtckmp.IceServer
import com.github.kevskitv.webrtckmp.IceTransportPolicy
import com.github.kevskitv.webrtckmp.RtcConfiguration
import com.github.kevskitv.webrtckmp.RtcpMuxPolicy
import kotlinx.cinterop.ExperimentalForeignApi

internal fun RtcConfiguration.toPlatform(): RTCConfiguration {
    return RTCConfiguration().also {
        it.bundlePolicy = bundlePolicy.toPlatform()
        it.certificate = certificates?.firstOrNull()?.native
        it.iceCandidatePoolSize = iceCandidatePoolSize
        it.iceServers = iceServers.map(IceServer::toPlatform)
        it.rtcpMuxPolicy = rtcpMuxPolicy.toPlatform()
        it.iceTransportPolicy = iceTransportPolicy.toPlatform()
        it.sdpSemantics = RTCSdpSemantics.RTCSdpSemanticsUnifiedPlan
        it.continualGatheringPolicy = continualGatheringPolicy.toPlatform()
    }
}

private fun RtcpMuxPolicy.toPlatform(): RTCRtcpMuxPolicy {
    return when (this) {
        RtcpMuxPolicy.Negotiate -> RTCRtcpMuxPolicy.RTCRtcpMuxPolicyNegotiate
        RtcpMuxPolicy.Require -> RTCRtcpMuxPolicy.RTCRtcpMuxPolicyRequire
    }
}

private fun BundlePolicy.toPlatform(): RTCBundlePolicy {
    return when (this) {
        BundlePolicy.Balanced -> RTCBundlePolicy.RTCBundlePolicyBalanced
        BundlePolicy.MaxBundle -> RTCBundlePolicy.RTCBundlePolicyMaxBundle
        BundlePolicy.MaxCompat -> RTCBundlePolicy.RTCBundlePolicyMaxCompat
    }
}

private fun IceTransportPolicy.toPlatform(): RTCIceTransportPolicy {
    return when (this) {
        IceTransportPolicy.None -> RTCIceTransportPolicy.RTCIceTransportPolicyNone
        IceTransportPolicy.Relay -> RTCIceTransportPolicy.RTCIceTransportPolicyRelay
        IceTransportPolicy.NoHost -> RTCIceTransportPolicy.RTCIceTransportPolicyNoHost
        IceTransportPolicy.All -> RTCIceTransportPolicy.RTCIceTransportPolicyAll
    }
}

private fun ContinualGatheringPolicy.toPlatform(): RTCContinualGatheringPolicy {
    return when (this) {
        ContinualGatheringPolicy.GatherOnce -> RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherOnce
        ContinualGatheringPolicy.GatherContinually -> RTCContinualGatheringPolicy.RTCContinualGatheringPolicyGatherContinually
    }
}
