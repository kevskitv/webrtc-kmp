package com.github.kevskitv.webrtckmp

public class TrackEvent internal constructor(
    public val receiver: RtpReceiver,
    public val streams: List<MediaStream>,
    public val track: MediaStreamTrack?,
    public val transceiver: RtpTransceiver,
)
