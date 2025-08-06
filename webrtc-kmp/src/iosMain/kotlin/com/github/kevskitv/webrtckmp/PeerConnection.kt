@file:OptIn(ExperimentalForeignApi::class)

package com.github.kevskitv.webrtckmp

import WebRTC.RTCAudioTrack
import WebRTC.RTCDataChannel
import WebRTC.RTCDataChannelConfiguration
import WebRTC.RTCIceCandidate
import WebRTC.RTCIceConnectionState
import WebRTC.RTCIceGatheringState
import WebRTC.RTCMediaConstraints
import WebRTC.RTCMediaStream
import WebRTC.RTCPeerConnection
import WebRTC.RTCPeerConnectionDelegateProtocol
import WebRTC.RTCPeerConnectionState
import WebRTC.RTCRtpReceiver
import WebRTC.RTCRtpSender
import WebRTC.RTCRtpTransceiver
import WebRTC.RTCSessionDescription
import WebRTC.RTCSignalingState
import WebRTC.RTCVideoTrack
import WebRTC.dataChannelForLabel
import WebRTC.kRTCMediaStreamTrackKindAudio
import WebRTC.kRTCMediaStreamTrackKindVideo
import WebRTC.statisticsWithCompletionHandler
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.ConnectionStateChange
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.IceConnectionStateChange
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.IceGatheringStateChange
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.NegotiationNeeded
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.NewDataChannel
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.NewIceCandidate
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.RemoveTrack
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.RemovedIceCandidates
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.SignalingStateChange
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.StandardizedIceConnectionChange
import com.github.kevskitv.webrtckmp.PeerConnectionEvent.Track
import com.github.kevskitv.webrtckmp.internal.toPlatform
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCSignatureOverride
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual class PeerConnection actual constructor(
    rtcConfiguration: RtcConfiguration,
) : NSObject(),
    RTCPeerConnectionDelegateProtocol {
    public val ios: RTCPeerConnection =
        checkNotNull(
            WebRtc.peerConnectionFactory.peerConnectionWithConfiguration(
                configuration = rtcConfiguration.toPlatform(),
                constraints = RTCMediaConstraints(),
                delegate = this,
            ),
        ) { "Failed to create peer connection" }

    public actual val localDescription: SessionDescription? get() = ios.localDescription?.asCommon()

    public actual val remoteDescription: SessionDescription? get() =
        ios.remoteDescription
            ?.asCommon()

    public actual val signalingState: SignalingState
        get() = rtcSignalingStateAsCommon(ios.signalingState())

    public actual val iceConnectionState: IceConnectionState
        get() = rtcIceConnectionStateAsCommon(ios.iceConnectionState())

    public actual val connectionState: PeerConnectionState
        get() = rtcPeerConnectionStateAsCommon(ios.connectionState())

    public actual val iceGatheringState: IceGatheringState
        get() = rtcIceGatheringStateAsCommon(ios.iceGatheringState())

    @Suppress("ktlint:standard:backing-property-naming")
    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> =
        _peerConnectionEvent
            .asSharedFlow()

    private val coroutineScope = MainScope()
    private val localTracks = mutableMapOf<String, MediaStreamTrackImpl>()
    private val remoteTracks = mutableMapOf<String, MediaStreamTrackImpl>()

    public actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxPacketLifeTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean,
    ): DataChannel? {
        val config =
            RTCDataChannelConfiguration().also {
                it.channelId = id
                it.isOrdered = ordered
                it.maxRetransmitTimeMs = maxPacketLifeTimeMs.toLong()
                it.maxRetransmits = maxRetransmits
                it.protocol = protocol
                it.isNegotiated = negotiated
            }
        return ios.dataChannelForLabel(label, config)?.let { DataChannel(it) }
    }

    public actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription =
            ios.awaitResult {
                offerForConstraints(constraints, it)
            }
        return sessionDescription.asCommon()
    }

    public actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription {
        val constraints = options.toRTCMediaConstraints()
        val sessionDescription: RTCSessionDescription =
            ios.awaitResult {
                answerForConstraints(constraints, it)
            }
        return sessionDescription.asCommon()
    }

    private fun OfferAnswerOptions.toRTCMediaConstraints(): RTCMediaConstraints {
        val mandatory =
            mutableMapOf<Any?, String?>().apply {
                iceRestart?.let { this += "IceRestart" to "$it" }
                offerToReceiveAudio?.let { this += "OfferToReceiveAudio" to "$it" }
                offerToReceiveVideo?.let { this += "OfferToReceiveVideo" to "$it" }
                voiceActivityDetection?.let { this += "VoiceActivityDetection" to "$it" }
            }
        return RTCMediaConstraints(mandatory, null)
    }

    public actual suspend fun setLocalDescription(description: SessionDescription) {
        ios.await { setLocalDescription(description.asIos(), it) }
    }

    public actual suspend fun setRemoteDescription(description: SessionDescription) {
        ios.await { setRemoteDescription(description.asIos(), it) }
    }

    public actual fun setConfiguration(configuration: RtcConfiguration): Boolean =
        ios.setConfiguration(configuration.toPlatform())

    public actual suspend fun addIceCandidate(candidate: IceCandidate): Boolean {
        ios.addIceCandidate(candidate.native)
        return true
    }

    public actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        ios.removeIceCandidates(candidates.map { it.native })
        return true
    }

    public actual fun getSenders(): List<RtpSender> =
        ios.senders.map {
            val iosSender = it as RTCRtpSender
            RtpSender(iosSender, localTracks[iosSender.track?.trackId])
        }

    public actual fun getReceivers(): List<RtpReceiver> =
        ios.receivers.map {
            val iosReceiver = it as RTCRtpReceiver
            RtpReceiver(iosReceiver, remoteTracks[iosReceiver.track?.trackId])
        }

    public actual fun getTransceivers(): List<RtpTransceiver> =
        ios.transceivers.map {
            val iosTransceiver = it as RTCRtpTransceiver
            val senderTrack = localTracks[iosTransceiver.sender.track?.trackId]
            val receiverTrack = remoteTracks[iosTransceiver.receiver.track?.trackId]
            RtpTransceiver(iosTransceiver, senderTrack, receiverTrack)
        }

    public actual fun addTrack(
        track: MediaStreamTrack,
        vararg streams: MediaStream,
    ): RtpSender {
        require(track is MediaStreamTrackImpl)

        val streamIds = streams.map { it.id }
        val iosSender = checkNotNull(ios.addTrack(track.ios, streamIds)) { "Failed to add track" }
        localTracks[track.id] = track
        return RtpSender(iosSender, track)
    }

    public actual fun removeTrack(sender: RtpSender): Boolean {
        localTracks.remove(sender.track?.id)
        return ios.removeTrack(sender.ios)
    }

    public actual suspend fun getStats(): RtcStatsReport? =
        suspendCoroutine { cont ->
            ios.statisticsWithCompletionHandler { report ->
                report?.let { cont.resume(RtcStatsReport(it)) }
            }
        }

    public actual fun close() {
        remoteTracks.values.forEach(MediaStreamTrack::stop)
        remoteTracks.clear()
        ios.close()
        coroutineScope.launch {
            _peerConnectionEvent.emit(SignalingStateChange(SignalingState.Closed))
            coroutineScope.cancel()
        }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeSignalingState: RTCSignalingState,
    ) {
        val event = SignalingStateChange(rtcSignalingStateAsCommon(didChangeSignalingState))
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddStream: RTCMediaStream,
    ) {
        // this deprecated API should not longer be used
        // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onaddstream
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveStream: RTCMediaStream,
    ) {
        // The removestream event has been removed from the WebRTC specification in favor of
        // the existing removetrack event on the remote MediaStream and the corresponding
        // MediaStream.onremovetrack event handler property of the remote MediaStream.
        // The RTCPeerConnection API is now track-based, so having zero tracks in the remote
        // stream is equivalent to the remote stream being removed and the old removestream event.
        // https://developer.mozilla.org/en-US/docs/Web/API/RTCPeerConnection/onremovestream
    }

    override fun peerConnectionShouldNegotiate(peerConnection: RTCPeerConnection) {
        coroutineScope.launch { _peerConnectionEvent.emit(NegotiationNeeded) }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceConnectionState: RTCIceConnectionState,
    ) {
        val event =
            IceConnectionStateChange(rtcIceConnectionStateAsCommon(didChangeIceConnectionState))
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeIceGatheringState: RTCIceGatheringState,
    ) {
        val event =
            IceGatheringStateChange(rtcIceGatheringStateAsCommon(didChangeIceGatheringState))
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didGenerateIceCandidate: RTCIceCandidate,
    ) {
        val event = NewIceCandidate(IceCandidate(didGenerateIceCandidate))
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveIceCandidates: List<*>,
    ) {
        val candidates = didRemoveIceCandidates.map { IceCandidate(it as RTCIceCandidate) }
        val event = RemovedIceCandidates(candidates)
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didOpenDataChannel: RTCDataChannel,
    ) {
        val event = NewDataChannel(DataChannel(didOpenDataChannel))
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    @ObjCSignatureOverride
    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeStandardizedIceConnectionState: RTCIceConnectionState,
    ) {
        val event =
            StandardizedIceConnectionChange(
                rtcIceConnectionStateAsCommon(didChangeStandardizedIceConnectionState),
            )
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didChangeConnectionState: RTCPeerConnectionState,
    ) {
        val event = ConnectionStateChange(rtcPeerConnectionStateAsCommon(didChangeConnectionState))
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didAddReceiver: RTCRtpReceiver,
        streams: List<*>,
    ) {
        val transceiver =
            ios.transceivers
                .map { it as RTCRtpTransceiver }
                .find { it.receiver.receiverId == didAddReceiver.receiverId }
                ?: return

        val senderTrack = localTracks[transceiver.sender.track?.trackId]

        val receiverTrack =
            didAddReceiver.track()?.let {
                remoteTracks.getOrPut(it.trackId) {
                    when (val kind = it.kind()) {
                        kRTCMediaStreamTrackKindAudio -> RemoteAudioTrack(it as RTCAudioTrack)
                        kRTCMediaStreamTrackKindVideo -> RemoteVideoTrack(it as RTCVideoTrack)
                        else -> error("Unsupported track kind: $kind")
                    }
                }
            }

        val iosStreams = streams.map { it as RTCMediaStream }

        val commonStreams =
            iosStreams.map { iosStream ->
                MediaStream(iosStream).also { stream ->
                    iosStream.audioTracks.forEach {
                        stream.addTrack(RemoteAudioTrack(it as RTCAudioTrack))
                    }
                    iosStream.videoTracks.forEach {
                        stream.addTrack(RemoteVideoTrack(it as RTCVideoTrack))
                    }
                }
            }

        val trackEvent =
            TrackEvent(
                receiver = RtpReceiver(didAddReceiver, receiverTrack),
                streams = commonStreams,
                track = receiverTrack,
                transceiver = RtpTransceiver(transceiver, senderTrack, receiverTrack),
            )

        val event = Track(trackEvent)
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
    }

    override fun peerConnection(
        peerConnection: RTCPeerConnection,
        didRemoveReceiver: RTCRtpReceiver,
    ) {
        val track = remoteTracks.remove(didRemoveReceiver.track?.trackId)
        val event = RemoveTrack(RtpReceiver(didRemoveReceiver, track))
        coroutineScope.launch { _peerConnectionEvent.emit(event) }
        track?.stop()
    }
}
