package com.github.kevskitv.webrtckmp

public expect class RtpReceiver {
    public val id: String
    public val track: MediaStreamTrack?
    public val parameters: RtpParameters
}
