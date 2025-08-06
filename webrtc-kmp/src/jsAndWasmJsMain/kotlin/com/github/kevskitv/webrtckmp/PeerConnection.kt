package com.github.kevskitv.webrtckmp

import com.github.kevskitv.webrtckmp.externals.RTCPeerConnection
import com.github.kevskitv.webrtckmp.externals.addIceCandidate
import com.github.kevskitv.webrtckmp.externals.createAnswer
import com.github.kevskitv.webrtckmp.externals.createDataChannel
import com.github.kevskitv.webrtckmp.externals.createOffer
import com.github.kevskitv.webrtckmp.externals.getReceivers
import com.github.kevskitv.webrtckmp.externals.getSenders
import com.github.kevskitv.webrtckmp.externals.getStats
import com.github.kevskitv.webrtckmp.externals.getTransceivers
import com.github.kevskitv.webrtckmp.externals.setLocalDescription
import com.github.kevskitv.webrtckmp.externals.setRemoteDescription
import com.github.kevskitv.webrtckmp.externals.streams
import com.github.kevskitv.webrtckmp.externals.toSessionDescription
import com.github.kevskitv.webrtckmp.internal.AudioTrackImpl
import com.github.kevskitv.webrtckmp.internal.Console
import com.github.kevskitv.webrtckmp.internal.VideoTrackImpl
import com.github.kevskitv.webrtckmp.internal.toPlatform
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

public actual class PeerConnection actual constructor(
    rtcConfiguration: RtcConfiguration,
) {
    public actual val localDescription: SessionDescription? get() =
        platform.localDescription ?.toSessionDescription()

    public actual val remoteDescription: SessionDescription? get() =
        platform.remoteDescription ?.toSessionDescription()

    public actual val signalingState: SignalingState get() =
        platform.signalingState.toSignalingState()

    public actual val iceConnectionState: IceConnectionState get() =
        platform.iceConnectionState.toIceConnectionState()

    public actual val connectionState: PeerConnectionState get() =
        platform.connectionState.toPeerConnectionState()

    public actual val iceGatheringState: IceGatheringState get() =
        platform.iceGatheringState
            .toIceGatheringState()

    private val platform: RTCPeerConnection

    private val _peerConnectionEvent =
        MutableSharedFlow<PeerConnectionEvent>(extraBufferCapacity = FLOW_BUFFER_CAPACITY)
    internal actual val peerConnectionEvent: Flow<PeerConnectionEvent> =
        _peerConnectionEvent
            .asSharedFlow()

    private val scope = MainScope()

    init {
        platform =
            RTCPeerConnection(rtcConfiguration).apply {
                onsignalingstatechange = {
                    scope.launch {
                        val event =
                            PeerConnectionEvent.SignalingStateChange(
                                this@PeerConnection.signalingState,
                            )
                        _peerConnectionEvent.emit(event)
                    }
                }
                oniceconnectionstatechange = {
                    scope.launch {
                        val event =
                            PeerConnectionEvent.IceConnectionStateChange(
                                this@PeerConnection.iceConnectionState,
                            )
                        _peerConnectionEvent.emit(event)
                    }
                }
                onconnectionstatechange = {
                    scope.launch {
                        val event =
                            PeerConnectionEvent.ConnectionStateChange(
                                this@PeerConnection.connectionState,
                            )
                        _peerConnectionEvent.emit(event)
                    }
                }
                onicegatheringstatechange = {
                    scope.launch {
                        val event =
                            PeerConnectionEvent.IceGatheringStateChange(
                                this@PeerConnection.iceGatheringState,
                            )
                        _peerConnectionEvent.tryEmit(event)
                    }
                }
                onicecandidate = { iceEvent ->
                    scope.launch {
                        val event =
                            iceEvent.candidate?.let {
                                PeerConnectionEvent.NewIceCandidate(
                                    IceCandidate(it),
                                )
                            }
                        event?.let { _peerConnectionEvent.emit(it) }
                    }
                }
                ondatachannel = { dataChannelEvent ->
                    scope.launch {
                        val event =
                            PeerConnectionEvent.NewDataChannel(
                                DataChannel(dataChannelEvent.channel),
                            )
                        _peerConnectionEvent.emit(event)
                    }
                }
                onnegotiationneeded = {
                    scope.launch {
                        _peerConnectionEvent.emit(PeerConnectionEvent.NegotiationNeeded)
                    }
                }
                ontrack = { rtcTrackEvent ->
                    scope.launch {
                        val trackEvent =
                            TrackEvent(
                                receiver = RtpReceiver(rtcTrackEvent.receiver),
                                streams = rtcTrackEvent.streams.map { MediaStream(it) },
                                track =
                                    rtcTrackEvent.track.let {
                                        when (it.kind) {
                                            "audio" -> AudioTrackImpl(it)
                                            "video" -> VideoTrackImpl(it)
                                            else -> throw IllegalArgumentException(
                                                "Unsupported track kind: ${it.kind}",
                                            )
                                        }
                                    },
                                transceiver = RtpTransceiver(rtcTrackEvent.transceiver),
                            )
                        _peerConnectionEvent.emit(PeerConnectionEvent.Track(trackEvent))
                    }
                }
            }
    }

    public actual fun createDataChannel(
        label: String,
        id: Int,
        ordered: Boolean,
        maxPacketLifeTimeMs: Int,
        maxRetransmits: Int,
        protocol: String,
        negotiated: Boolean,
    ): DataChannel? {
        return platform
            .createDataChannel(
                label,
                id,
                ordered,
                maxPacketLifeTimeMs,
                maxRetransmits,
                protocol,
                negotiated,
            )?.let { return DataChannel(it) }
    }

    public actual suspend fun createOffer(options: OfferAnswerOptions): SessionDescription =
        platform.createOffer(options).toSessionDescription()

    public actual suspend fun createAnswer(options: OfferAnswerOptions): SessionDescription =
        platform.createAnswer(options).toSessionDescription()

    public actual suspend fun setLocalDescription(description: SessionDescription) {
        platform.setLocalDescription(description)
    }

    public actual suspend fun setRemoteDescription(description: SessionDescription) {
        platform.setRemoteDescription(description)
    }

    public actual fun setConfiguration(configuration: RtcConfiguration): Boolean =
        runCatching { platform.setConfiguration(configuration.toPlatform()) }
            .onFailure { Console.error("Set RTCConfiguration failed: $it") }
            .map { true }
            .getOrDefault(false)

    public actual suspend fun addIceCandidate(candidate: IceCandidate): Boolean =
        runCatching { platform.addIceCandidate(candidate) }
            .onFailure { Console.error("Add ICE candidate failed: $it") }
            .map { true }
            .getOrDefault(false)

    public actual fun removeIceCandidates(candidates: List<IceCandidate>): Boolean {
        Console.warn("removeIceCandidates is not supported in JS")
        return true
    }

    public actual fun getSenders(): List<RtpSender> = platform.getSenders().map { RtpSender(it) }

    public actual fun getReceivers(): List<RtpReceiver> =
        platform.getReceivers().map {
            RtpReceiver(it)
        }

    public actual fun getTransceivers(): List<RtpTransceiver> =
        platform.getTransceivers().map {
            RtpTransceiver(it)
        }

    public actual fun addTrack(
        track: MediaStreamTrack,
        vararg streams: MediaStream,
    ): RtpSender {
        require(track is MediaStreamTrackImpl)
        val platformSender =
            platform.addTrack(
                track.platform,
                *Array(streams.size) { streams[it].js },
            )
        return RtpSender(platformSender)
    }

    public actual fun removeTrack(sender: RtpSender): Boolean =
        runCatching { platform.removeTrack(sender.js) }
            .onFailure { Console.error("Remove track failed: $it") }
            .map { true }
            .getOrDefault(false)

    public actual suspend fun getStats(): RtcStatsReport? =
        runCatching { platform.getStats() }
            .map { RtcStatsReport() }
            .onFailure { Console.error("Get stats failed: $it") }
            .getOrNull()

    public actual fun close() {
        platform.close()
        scope.launch {
            _peerConnectionEvent.emit(
                PeerConnectionEvent.SignalingStateChange(SignalingState.Closed),
            )
            scope.cancel()
        }
    }

    private fun String.toSignalingState(): SignalingState =
        when (this) {
            "stable" -> SignalingState.Stable
            "have-local-offer" -> SignalingState.HaveLocalOffer
            "have-remote-offer" -> SignalingState.HaveRemoteOffer
            "have-local-pranswer" -> SignalingState.HaveLocalPranswer
            "have-remote-pranswer" -> SignalingState.HaveRemotePranswer
            "closed" -> SignalingState.Closed
            else -> throw IllegalArgumentException("Illegal signaling state: $this")
        }

    private fun String.toIceConnectionState(): IceConnectionState =
        when (this) {
            "new" -> IceConnectionState.New
            "checking" -> IceConnectionState.Checking
            "connected" -> IceConnectionState.Connected
            "completed" -> IceConnectionState.Completed
            "failed" -> IceConnectionState.Failed
            "disconnected" -> IceConnectionState.Disconnected
            "closed" -> IceConnectionState.Closed
            else -> throw IllegalArgumentException("Illegal ICE connection state: $this")
        }

    private fun String.toPeerConnectionState(): PeerConnectionState =
        when (this) {
            "new" -> PeerConnectionState.New
            "connecting" -> PeerConnectionState.Connecting
            "connected" -> PeerConnectionState.Connected
            "disconnected" -> PeerConnectionState.Disconnected
            "failed" -> PeerConnectionState.Failed
            "closed" -> PeerConnectionState.Closed
            else -> throw IllegalArgumentException("Illegal connection state: $this")
        }

    private fun String.toIceGatheringState(): IceGatheringState =
        when (this) {
            "new" -> IceGatheringState.New
            "gathering" -> IceGatheringState.Gathering
            "complete" -> IceGatheringState.Complete
            else -> throw IllegalArgumentException("Illegal ICE gathering state: $this")
        }
}
