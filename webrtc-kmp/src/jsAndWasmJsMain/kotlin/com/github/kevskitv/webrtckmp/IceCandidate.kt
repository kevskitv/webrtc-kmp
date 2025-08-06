package com.github.kevskitv.webrtckmp

import com.github.kevskitv.webrtckmp.externals.RTCIceCandidate
import com.github.kevskitv.webrtckmp.internal.jsonStringify

public actual class IceCandidate internal constructor(
    internal val js: RTCIceCandidate,
) {
    public actual constructor(sdpMid: String, sdpMLineIndex: Int, candidate: String) : this(
        RTCIceCandidate(candidate, sdpMid, sdpMLineIndex),
    )

    public actual val sdpMid: String = js.sdpMid
    public actual val sdpMLineIndex: Int = js.sdpMLineIndex
    public actual val candidate: String = js.candidate

    actual override fun toString(): String = js.jsonStringify()
}
