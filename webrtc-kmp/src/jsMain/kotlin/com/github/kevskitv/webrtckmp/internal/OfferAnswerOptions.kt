package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.OfferAnswerOptions
import kotlin.js.Json
import kotlin.js.json

internal fun OfferAnswerOptions.toPlatform(): Json {
    return json().apply {
        iceRestart?.also { add(json("iceRestart" to it)) }
        offerToReceiveAudio?.also { add(json("offerToReceiveAudio" to it)) }
        offerToReceiveVideo?.also { add(json("offerToReceiveVideo" to it)) }
        voiceActivityDetection?.also { add(json("voiceActivityDetection" to it)) }
    }
}
