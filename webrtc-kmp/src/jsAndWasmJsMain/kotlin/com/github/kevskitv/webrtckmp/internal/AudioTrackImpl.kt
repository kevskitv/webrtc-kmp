package com.github.kevskitv.webrtckmp.internal

import com.github.kevskitv.webrtckmp.AudioTrack
import com.github.kevskitv.webrtckmp.MediaStreamTrackImpl
import com.github.kevskitv.webrtckmp.externals.PlatformMediaStreamTrack

internal class AudioTrackImpl(
    platform: PlatformMediaStreamTrack,
) : MediaStreamTrackImpl(platform),
    AudioTrack {
    override fun setVolume(volume: Double) {
        // not available in JS
    }
}
