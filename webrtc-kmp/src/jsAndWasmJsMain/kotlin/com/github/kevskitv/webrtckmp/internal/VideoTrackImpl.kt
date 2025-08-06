package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.MediaStreamTrackImpl
import com.github.kevskitv.webrtckmp.VideoTrack
import com.github.kevskitv.webrtckmp.externals.PlatformMediaStreamTrack

internal class VideoTrackImpl(
    override val js: PlatformMediaStreamTrack,
) : MediaStreamTrackImpl(js),
    VideoTrack {
    override suspend fun switchCamera(deviceId: String?) {
        Console.warn("switchCamera is not supported in browser environment.")
    }
}
