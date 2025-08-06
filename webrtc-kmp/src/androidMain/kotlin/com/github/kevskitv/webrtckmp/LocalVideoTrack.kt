package com.github.kevskitv.webrtckmp

import com.github.kevskitv.webrtckmp.capturer.CameraVideoCapturerController
import com.github.kevskitv.webrtckmp.capturer.VideoCapturerController
import com.github.kevskitv.webrtckmp.capturer.VideoCapturerErrorListener
import org.webrtc.VideoTrack as AndroidVideoTrack

internal class LocalVideoTrack(
    android: AndroidVideoTrack,
    private val videoCapturerController: VideoCapturerController,
) : RenderedVideoTrack(android),
    VideoTrack {
    override val settings: MediaTrackSettings get() = videoCapturerController.settings

    init {
        videoCapturerController.videoCapturerErrorListener = VideoCapturerErrorListener { stop() }
        videoCapturerController.startCapture()
    }

    override suspend fun switchCamera(deviceId: String?) {
        (videoCapturerController as? CameraVideoCapturerController)?.let { controller ->
            deviceId?.let { controller.switchCamera(it) } ?: controller.switchCamera()
        }
    }

    override fun onSetEnabled(enabled: Boolean) {
        if (enabled) {
            videoCapturerController.startCapture()
        } else {
            videoCapturerController.stopCapture()
        }
    }

    override fun onStop() {
        videoCapturerController.dispose()
    }
}
