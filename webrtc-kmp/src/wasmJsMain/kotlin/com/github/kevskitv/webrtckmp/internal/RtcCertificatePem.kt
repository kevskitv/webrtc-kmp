package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.RtcCertificatePem
import com.github.kevskitv.webrtckmp.externals.RTCCertificate

internal external interface JsRTCCertificate : RTCCertificate, JsAny

@Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
internal fun RtcCertificatePem.toWasmJs(): JsRTCCertificate = js as JsRTCCertificate
