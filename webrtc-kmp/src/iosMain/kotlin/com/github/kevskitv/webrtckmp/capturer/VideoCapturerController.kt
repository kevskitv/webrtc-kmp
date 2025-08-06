package com.github.kevskitv.webrtckmp.capturer

import com.github.kevskitv.webrtckmp.MediaTrackSettings

internal abstract class VideoCapturerController {
    var settings: MediaTrackSettings = MediaTrackSettings()
        protected set

    abstract fun startCapture()
    abstract fun stopCapture()
}
