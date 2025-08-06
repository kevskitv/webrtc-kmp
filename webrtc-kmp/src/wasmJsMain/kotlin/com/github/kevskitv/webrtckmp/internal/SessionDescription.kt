package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.SessionDescription
import com.github.kevskitv.webrtckmp.externals.WasmRTCSessionDescription
import com.github.kevskitv.webrtckmp.toCanonicalString

internal fun SessionDescription.toWasmJs(): WasmRTCSessionDescription =
    createRTCSessionDescription(type.toCanonicalString(), sdp)

@Suppress("UNUSED_PARAMETER")
private fun createRTCSessionDescription(type: String, sdp: String): WasmRTCSessionDescription =
    js("({type: type, sdp: sdp})")
