package com.github.kevskitv.webrtckmp

public interface AudioTrack : MediaStreamTrack {
    public fun setVolume(volume: Double)
}

@Deprecated("Use AudioTrack instead", ReplaceWith("AudioTrack"))
public typealias AudioStreamTrack = AudioTrack
