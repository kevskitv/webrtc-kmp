@file:OptIn(ExperimentalForeignApi::class)

package com.github.kevskitv.webrtckmp

import WebRTC.RTCVideoTrack
import com.github.kevskitv.webrtckmp.capturer.CameraVideoCapturerController
import com.github.kevskitv.webrtckmp.capturer.VideoCapturerController
import kotlinx.cinterop.ExperimentalForeignApi

internal class LocalVideoTrack(
    ios: RTCVideoTrack,
    private val videoCapturerController: VideoCapturerController,
) : RenderedVideoTrack(ios),
    VideoTrack {
    override val settings: MediaTrackSettings get() = videoCapturerController.settings

    init {
        videoCapturerController.startCapture()
    }

    override suspend fun switchCamera(deviceId: String?) {
        (videoCapturerController as? CameraVideoCapturerController)?.let { controller ->
            deviceId?.let { controller.switchCamera(deviceId) } ?: controller.switchCamera()
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
        videoCapturerController.stopCapture()
    }
}
