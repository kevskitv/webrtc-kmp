package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.SessionDescription
import com.github.kevskitv.webrtckmp.toCanonicalString
import kotlin.js.json

internal fun SessionDescription.toPlatform() = json(
    "type" to type.toCanonicalString(),
    "sdp" to sdp
)
