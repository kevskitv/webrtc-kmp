package com.github.kevskitv.webrtckmp

import com.github.kevskitv.webrtckmp.externals.PlatformMediaStream
import com.github.kevskitv.webrtckmp.externals.getTracks
import com.github.kevskitv.webrtckmp.internal.AudioTrackImpl
import com.github.kevskitv.webrtckmp.internal.VideoTrackImpl

public actual class MediaStream internal constructor(
    public val js: PlatformMediaStream,
) {
    public actual constructor() : this(PlatformMediaStream())

    public actual val id: String get() = js.id
    public actual val tracks: List<MediaStreamTrack> get() =
        js.getTracks().map {
            when (it.kind) {
                "audio" -> AudioTrackImpl(it)
                "video" -> VideoTrackImpl(it)
                else -> throw IllegalArgumentException("Unknown track kind: ${it.kind}")
            }
        }

    public actual fun addTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        js.addTrack(track.platform)
    }

    public actual fun getTrackById(id: String): MediaStreamTrack? =
        js.getTrackById(id)?.let {
            MediaStreamTrackImpl(it)
        }

    public actual fun removeTrack(track: MediaStreamTrack) {
        require(track is MediaStreamTrackImpl)
        js.removeTrack(track.platform)
    }

    public actual fun release() {
        tracks.forEach(MediaStreamTrack::stop)
    }
}
