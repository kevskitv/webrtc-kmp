package com.github.kevskitv.webrtckmp.externals

import com.github.kevskitv.webrtckmp.SessionDescription
import com.github.kevskitv.webrtckmp.SessionDescriptionType

internal external interface RTCSessionDescription {
    val type: String
    val sdp: String
}

internal fun RTCSessionDescription.toSessionDescription(): SessionDescription {
    val type =
        SessionDescriptionType.valueOf(
            this.type.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
        )

    return SessionDescription(type, sdp)
}
