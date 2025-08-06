package com.github.kevskitv.webrtckmp.externals

import com.github.kevskitv.webrtckmp.MediaTrackConstraints
import com.github.kevskitv.webrtckmp.internal.toMediaTrackConstraints
import org.w3c.dom.mediacapture.MediaStreamTrack

internal actual fun PlatformMediaStreamTrack.getConstraints(): MediaTrackConstraints {
    return (this as MediaStreamTrack).getConstraints().toMediaTrackConstraints()
}
