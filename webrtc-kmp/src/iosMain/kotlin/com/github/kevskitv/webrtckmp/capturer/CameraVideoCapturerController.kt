@file:OptIn(ExperimentalForeignApi::class)

package com.github.kevskitv.webrtckmp.capturer

import WebRTC.RTCVideoCapturerDelegateProtocol
import com.github.kevskitv.webrtckmp.MediaTrackConstraints
import kotlinx.cinterop.ExperimentalForeignApi

internal expect class CameraVideoCapturerController(
    constraints: MediaTrackConstraints,
    videoCapturerDelegate: RTCVideoCapturerDelegateProtocol,
) : VideoCapturerController {

    override fun startCapture()
    override fun stopCapture()
    fun switchCamera()
    fun switchCamera(deviceId: String)
}
