package com.github.kevskitv.webrtckmp.externals

internal external interface RTCDTMFSender {
    val toneBuffer: String

    fun insertDTMF(
        tones: String,
        duration: Long,
        interToneGap: Long,
    )
}
