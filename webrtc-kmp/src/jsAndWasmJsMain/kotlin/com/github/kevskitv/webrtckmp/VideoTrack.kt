package com.github.kevskitv.webrtckmp

import com.github.kevskitv.webrtckmp.externals.PlatformMediaStreamTrack

public actual interface VideoTrack : MediaStreamTrack {
    public val js: PlatformMediaStreamTrack

    public actual suspend fun switchCamera(deviceId: String?)
}
