package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.RtcConfiguration
import com.github.kevskitv.webrtckmp.externals.RTCPeerConnectionConfiguration

internal expect fun RtcConfiguration.toPlatform(): RTCPeerConnectionConfiguration
