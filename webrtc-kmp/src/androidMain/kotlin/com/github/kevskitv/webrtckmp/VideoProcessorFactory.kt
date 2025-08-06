package com.github.kevskitv.webrtckmp

import org.webrtc.VideoProcessor

public fun interface VideoProcessorFactory {
    public fun createVideoProcessor(): VideoProcessor
}
