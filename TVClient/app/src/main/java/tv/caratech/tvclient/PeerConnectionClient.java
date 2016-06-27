package tv.caratech.tvclient;

import android.content.Context;
import android.util.Log;

import org.webrtc.AudioTrack;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.DataChannel;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.Logging;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.VideoCapturerAndroid;
import org.webrtc.VideoRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;
import org.webrtc.voiceengine.WebRtcAudioManager;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wurenhai on 2016/6/2.
 */
public class PeerConnectionClient {

    private static final String TAG = "PeerConnectionClient";

    private static final PeerConnectionClient instance = new PeerConnectionClient();

    private static final String FIELD_TRIAL_AUTOMATIC_RESIZE = "WebRTC-MediaCodecVideoEncoder-AutomaticResize/Enabled/";

    private static final String AUDIO_CODEC_ISAC = "ISAC";
    private static final String VIDEO_CODEC_VP8 = "VP8";
    private static final String VIDEO_CODEC_PARAM_START_BITRATE = "x-google-start-bitrate";
    private static final String AUDIO_CODEC_PARAM_BITRATE = "maxaveragebitrate";

    private static final int MIN_FRAME_RATE = 15;
    private static final int MAX_FRAME_RATE = 30;

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private PeerConnectionEvents events;

    private PeerConnectionFactory.Options options;
    private PeerConnectionFactory factory;
    private MediaConstraints pcConstraints;
    private MediaConstraints videoConstraints;
    private MediaConstraints audioConstraints;
    private MediaConstraints sdpMediaConstraints;
    private MediaStream mediaStream;
    private VideoCapturerAndroid videoCapturer;
    private VideoSource videoSource;
    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private VideoTrack remoteVideoTrack;

    private VideoRenderer.Callbacks localRender;
    private VideoRenderer.Callbacks remoteRender;

    private PeerConnection pc;
    private SessionDescription localSdp; // either offer or answer SDP

    private final PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
        @Override
        public void onSignalingChange(PeerConnection.SignalingState newState) {
            Log.d(TAG, "SignalingState: " + newState);
        }

        @Override
        public void onIceConnectionChange(final PeerConnection.IceConnectionState newState) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "IceConnectionState: " + newState);
                    if (newState == PeerConnection.IceConnectionState.CONNECTED) {
                        events.onIceConnected();
                    } else if (newState == PeerConnection.IceConnectionState.DISCONNECTED) {
                        events.onIceDisconnected();
                    } else if (newState == PeerConnection.IceConnectionState.FAILED) {
                        //reportError("ICE connection failed.");
                        Log.e(TAG, "ICE connection failed.");
                    }
                }
            });
        }

        @Override
        public void onIceConnectionReceivingChange(boolean receiving) {
            Log.d(TAG, "IceConnectionReceiving changed to " + receiving);
        }

        @Override
        public void onIceGatheringChange(PeerConnection.IceGatheringState newState) {
            Log.d(TAG, "IceGatheringState: " + newState);
            if (newState == PeerConnection.IceGatheringState.COMPLETE) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        events.onIceGatheringComplete();
                    }
                });
            }
        }

        @Override
        public void onIceCandidate(final IceCandidate candidate) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    events.onIceCandidate(candidate);
                }
            });
        }

        @Override
        public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    events.onIceCandidatesRemoved(candidates);
                }
            });
        }

        @Override
        public void onAddStream(final MediaStream stream) {
            Log.i(TAG, "onAddStream: MedisStream income");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (pc == null || isError) {
                        return;
                    }
                    if (stream.videoTracks.size() > 1 || stream.audioTracks.size() > 1) {
                        reportError("Weird-looking stream: " + stream);
                        return;
                    }
                    if (stream.videoTracks.size() == 1) {
                        remoteVideoTrack = stream.videoTracks.get(0);
                        remoteVideoTrack.setEnabled(renderVideo);
                        remoteVideoTrack.addRenderer(new VideoRenderer(remoteRender));

                        Log.i(TAG, "Remote video stream added.");
                    }
                }
            });
        }

        @Override
        public void onRemoveStream(final MediaStream stream) {
            Log.i(TAG, "onAddStream: MedisStream gone");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    remoteVideoTrack = null;
                }
            });
        }

        @Override
        public void onDataChannel(final DataChannel dc) {
            reportError("AppRTC doesn't use data channels, but got: " + dc.label() + " anyway!");
        }

        @Override
        public void onRenegotiationNeeded() {
            // No need to do anything; AppRTC follows a pre-agreed-upon
            // signaling/negotiation protocol.
        }
    };
    private final SdpObserver sdpObserver = new SdpObserver() {
        @Override
        public void onCreateSuccess(SessionDescription origSdp) {
            if (localSdp != null) {
                reportError("Multiple SDP create.");
                return;
            }

            String sdpDescription = origSdp.description;
            if (preferIsac) {
                sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
            }
            if (videoCallEnabled) {
                sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_VP8, false);
            }

            final SessionDescription sdp = new SessionDescription(origSdp.type, sdpDescription);
            localSdp = sdp;

            Log.d(TAG, "Local SDP :" + localSdp.description);

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (pc != null && !isError) {
                        Log.i(TAG, "Set local SDP from " + sdp.type);
                        pc.setLocalDescription(sdpObserver, sdp);
                    }
                }
            });
        }

        @Override
        public void onSetSuccess() {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (pc == null || isError) {
                        return;
                    }

                    if (isInitiator) {
                        // For offering peer connection we first create offer and set
                        // local SDP, then after receiving answer set remote SDP.
                        if (pc.getRemoteDescription() == null) {
                            // We've just set our local SDP so time to send it.
                            Log.i(TAG, "Local SDP set successfully");
                            events.onLocalDescription(localSdp);
                        } else {
                            // We've just set remote description, so drain remote
                            // and send local ICE candidates.
                            Log.i(TAG, "Remote SDP set successfully");
                        }
                    } else {
                        // For answering peer connection we set remote SDP and then
                        // create answer and set local SDP.
                        if (pc.getLocalDescription() != null) {
                            // We've just set our local SDP so time to send it, drain
                            // remote and send local ICE candidates.
                            Log.i(TAG, "Local SDP set successfully");
                            events.onLocalDescription(localSdp);
                        } else {
                            // We've just set remote SDP - do nothing for now -
                            // answer will be created soon.
                            Log.i(TAG, "Remote SDP set successfully");
                        }
                    }
                }
            });
        }

        @Override
        public void onCreateFailure(String error) {
            reportError("onCreateFailure: " + error);
        }

        @Override
        public void onSetFailure(String error) {
            reportError("onSetFailure: " + error);
        }
    };

    private int numberOfCameras;

    private boolean videoCallEnabled = true;

    private boolean isInitiator = true;
    private boolean isError = false;
    private boolean loopback = false;
    private boolean renderVideo = true;
    private boolean enableAudio = true;
    private boolean useOpenSLES = true;
    private boolean preferIsac = false;
    public boolean captureToTexture = true;

    private List<PeerConnection.IceServer> iceServers;

    public static PeerConnectionClient getInstance(){
        return instance;
    }

    private  PeerConnectionClient() {
        iceServers = new LinkedList<>();
    }

    public void setEventListener(PeerConnectionEvents listener) {
        events = listener;
    }

    public void createPeerConnectionFactory(final Context context) {
        // Reset variables to initial states.
        factory = null;
        pc = null;
        isError = false;
        localSdp = null;    // either offer or answer SDP
        mediaStream = null;
        videoCapturer = null;
        videoCallEnabled = true;
        renderVideo = true;
        localVideoTrack = null;
        remoteVideoTrack = null;
        enableAudio = true;
        localAudioTrack = null;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                createPeerConnectionFactoryInternal(context);
            }
        });
    }

    private void createPeerConnectionFactoryInternal(Context context) {
        // Initialize field trials.
        PeerConnectionFactory.initializeFieldTrials(FIELD_TRIAL_AUTOMATIC_RESIZE);

        WebRtcAudioManager.setBlacklistDeviceForOpenSLESUsage(useOpenSLES);

        if (!PeerConnectionFactory.initializeAndroidGlobals(context, true, true, true)) {
            events.onPeerConnectionError("Failed to initializeAndroidGlobals");
            return;
        }

        // Set default WebRTC tracing and INFO libjingle logging.
        // NOTE: this _must_ happen while |factory| is alive!
        Logging.enableTracing(
                "logcat:",
                EnumSet.of(Logging.TraceLevel.TRACE_APICALL),
                Logging.Severity.LS_WARNING);

        if (loopback) {
            options = new PeerConnectionFactory.Options();
            options.networkIgnoreMask = 0;
        }

        factory = new PeerConnectionFactory(options);
    }

    public void createPeerConnection(final EglBase.Context renderEGLContext,
                                     final VideoRenderer.Callbacks localRender,
                                     final VideoRenderer.Callbacks remoteRender){

        this.localRender = localRender;
        this.remoteRender = remoteRender;

        executor.execute(new Runnable() {
            @Override
            public void run() {
                createMediaConstraintsInternal();
                createPeerConnectionInternal(renderEGLContext);
            }
        });

    }

    private void createMediaConstraintsInternal() {
        pcConstraints = new MediaConstraints();
        pcConstraints.optional.add(new MediaConstraints.KeyValuePair("DtlsSrtpKeyAgreement", "true"));

        numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
        if (numberOfCameras == 0) {
            Log.w(TAG, "No camera on device. Switch to audio only call.");
            videoCallEnabled = false;
        }

        videoConstraints = new MediaConstraints();
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("minFrameRate", Integer.toString(MIN_FRAME_RATE)));
        videoConstraints.mandatory.add(new MediaConstraints.KeyValuePair("maxFrameRate", Integer.toString(MAX_FRAME_RATE)));

        audioConstraints = new MediaConstraints();
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googEchoCancellation", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googAutoGainControl", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googHighpassFilter", "true"));
        audioConstraints.mandatory.add(new MediaConstraints.KeyValuePair("googNoiseSuppression" , "true"));

        sdpMediaConstraints = new MediaConstraints();
        sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        if (videoCallEnabled || loopback) {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
        } else {
            sdpMediaConstraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"));
        }
    }

    private void createPeerConnectionInternal(EglBase.Context renderEGLContext){
        if (factory == null || isError) {
            Log.e(TAG, "Peerconnection factory is not created");
            return;
        }
        Log.d(TAG, "Create peer connection.");
        Log.i(TAG, "PCConstraints: " + pcConstraints.toString());
        if (videoConstraints != null) {
            Log.i(TAG, "VideoConstraints: " + videoConstraints.toString());
        }
        Log.i(TAG, "AudioConstraints: " + audioConstraints.toString());
        Log.i(TAG, "SdpMediaConstraints: " + sdpMediaConstraints.toString());

        if (videoCallEnabled) {
            Log.i(TAG, "EGLContext: " + renderEGLContext);
            factory.setVideoHwAccelerationOptions(renderEGLContext, renderEGLContext);
        }

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        // TCP candidates are only useful when connecting to a server that supports ICE-TCP.
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.DISABLED;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.BALANCED;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE;
        rtcConfig.continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_ONCE;
        // Use ECDSA encryption.
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;

        pc = factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
        isInitiator = false;

        mediaStream = factory.createLocalMediaStream("ARDAMS");
        if (videoCallEnabled) {
            String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(0);
            String frontCameraDeviceName = CameraEnumerationAndroid.getNameOfFrontFacingDevice();
            if (numberOfCameras > 1 && frontCameraDeviceName != null) {
                cameraDeviceName = frontCameraDeviceName;
            }
            Log.i(TAG, "Opening camera: " + cameraDeviceName);

            videoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null, captureToTexture ? renderEGLContext : null);
            if (videoCapturer == null) {
                reportError("Failed to open camera");
                return;
            }

            mediaStream.addTrack(createVideoTrack(videoCapturer));
        }
        mediaStream.addTrack(createAudioTrack());

        pc.addStream(mediaStream);

        Log.i(TAG, "Peer connection created.");
    }

    private VideoTrack createVideoTrack(VideoCapturerAndroid capturer) {
        videoSource = factory.createVideoSource(capturer, videoConstraints);
        localVideoTrack = factory.createVideoTrack("ARDAMSv0", videoSource);
        localVideoTrack.setEnabled(renderVideo);
        localVideoTrack.addRenderer(new VideoRenderer(localRender));
        return localVideoTrack;
    }

    private AudioTrack createAudioTrack() {
        localAudioTrack = factory.createAudioTrack("ARDAMSa0", factory.createAudioSource(audioConstraints));
        localAudioTrack.setEnabled(enableAudio);
        return localAudioTrack;
    }

    public void startVideoSource(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoCallEnabled && videoSource != null) {
                    Log.d(TAG, "Restart video source.");
                    videoSource.restart();
                }
            }
        });
    }

    public void stopVideoSource() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (videoCallEnabled && videoSource != null) {
                    Log.d(TAG, "Stop video source.");
                    videoSource.stop();
                }
            }
        });
    }

    public void close(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                closeInternal();
            }
        });
    }

    private void closeInternal(){
        Log.d(TAG, "Closing peer connection.");
        if (pc != null) {
            //pc.close();
            pc.dispose();
            pc = null;
        }
        Log.d(TAG, "Closing video source.");
        if (videoSource != null) {
            videoSource.dispose();
            videoSource = null;
        }
        Log.d(TAG, "Closing peer connection factory.");
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
        Log.d(TAG, "Closing peer connection done.");
        events.onPeerConnectionClosed();

//        PeerConnectionFactory.stopInternalTracingCapture();
//        PeerConnectionFactory.shutdownInternalTracer();
    }

    public void createAnswer(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc != null) {
                    Log.i(TAG, "PC Create ANSWER");
                    isInitiator = false;
                    pc.createAnswer(sdpObserver, sdpMediaConstraints);
                }
            }
        });
    }

    public void createOffer(){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc != null && !isError) {
                    Log.i(TAG, "PC Create OFFER");
                    isInitiator = true;
                    pc.createOffer(sdpObserver, sdpMediaConstraints);
                }
            }
        });
    }

    public void setRemoteDescription(final SessionDescription.Type type, final String sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (pc == null || isError) {
                    return;
                }

                String sdpDescription = sdp;
                if (preferIsac) {
                    sdpDescription = preferCodec(sdpDescription, AUDIO_CODEC_ISAC, true);
                }
                if (videoCallEnabled) {
                    sdpDescription = preferCodec(sdpDescription, VIDEO_CODEC_VP8, false);
                }

                Log.i(TAG, "Set remote SDP from type " + type);

                SessionDescription sdpRemote = new SessionDescription(type, sdpDescription);
                pc.setRemoteDescription(sdpObserver, sdpRemote);
            }
        });
    }

    private void reportError(final String errMsg){
        Log.e(TAG, "Peerconnection error: " + errMsg);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                if (!isError) {
                    events.onPeerConnectionError(errMsg);
                    isError = true;
                }
            }
        });
    }

    private static String setStartBitrate(String codec, boolean isVideoCodec,
                                          String sdpDescription, int bitrateKbps) {
        String[] lines = sdpDescription.split("\r\n");
        int rtpmapLineIndex = -1;
        boolean sdpFormatUpdated = false;
        String codecRtpMap = null;
        // Search for codec rtpmap in format
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                rtpmapLineIndex = i;
                break;
            }
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec + " codec");
            return sdpDescription;
        }
        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap
                + " at " + lines[rtpmapLineIndex]);

        // Check if a=fmtp string already exist in remote SDP for this codec and
        // update it with new bitrate parameter.
        regex = "^a=fmtp:" + codecRtpMap + " \\w+=\\d+.*[\r]?$";
        codecPattern = Pattern.compile(regex);
        for (int i = 0; i < lines.length; i++) {
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                Log.d(TAG, "Found " +  codec + " " + lines[i]);
                if (isVideoCodec) {
                    lines[i] += "; " + VIDEO_CODEC_PARAM_START_BITRATE
                            + "=" + bitrateKbps;
                } else {
                    lines[i] += "; " + AUDIO_CODEC_PARAM_BITRATE
                            + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Update remote SDP line: " + lines[i]);
                sdpFormatUpdated = true;
                break;
            }
        }

        StringBuilder newSdpDescription = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            newSdpDescription.append(lines[i]).append("\r\n");
            // Append new a=fmtp line if no such line exist for a codec.
            if (!sdpFormatUpdated && i == rtpmapLineIndex) {
                String bitrateSet;
                if (isVideoCodec) {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + VIDEO_CODEC_PARAM_START_BITRATE + "=" + bitrateKbps;
                } else {
                    bitrateSet = "a=fmtp:" + codecRtpMap + " "
                            + AUDIO_CODEC_PARAM_BITRATE + "=" + (bitrateKbps * 1000);
                }
                Log.d(TAG, "Add remote SDP line: " + bitrateSet);
                newSdpDescription.append(bitrateSet).append("\r\n");
            }

        }
        return newSdpDescription.toString();
    }

    private static String preferCodec(String sdpDescription, String codec, boolean isAudio) {
        String[] lines = sdpDescription.split("\r\n");
        int mLineIndex = -1;
        String codecRtpMap = null;
        // a=rtpmap:<payload type> <encoding name>/<clock rate> [/<encoding parameters>]
        String regex = "^a=rtpmap:(\\d+) " + codec + "(/\\d+)+[\r]?$";
        Pattern codecPattern = Pattern.compile(regex);
        String mediaDescription = "m=video ";
        if (isAudio) {
            mediaDescription = "m=audio ";
        }
        for (int i = 0; (i < lines.length)
                && (mLineIndex == -1 || codecRtpMap == null); i++) {
            if (lines[i].startsWith(mediaDescription)) {
                mLineIndex = i;
                continue;
            }
            Matcher codecMatcher = codecPattern.matcher(lines[i]);
            if (codecMatcher.matches()) {
                codecRtpMap = codecMatcher.group(1);
                continue;
            }
        }
        if (mLineIndex == -1) {
            Log.w(TAG, "No " + mediaDescription + " line, so can't prefer " + codec);
            return sdpDescription;
        }
        if (codecRtpMap == null) {
            Log.w(TAG, "No rtpmap for " + codec);
            return sdpDescription;
        }
        Log.d(TAG, "Found " +  codec + " rtpmap " + codecRtpMap + ", prefer at "
                + lines[mLineIndex]);
        String[] origMLineParts = lines[mLineIndex].split(" ");
        if (origMLineParts.length > 3) {
            StringBuilder newMLine = new StringBuilder();
            int origPartIndex = 0;
            // Format is: m=<media> <port> <proto> <fmt> ...
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(origMLineParts[origPartIndex++]).append(" ");
            newMLine.append(codecRtpMap);
            for (; origPartIndex < origMLineParts.length; origPartIndex++) {
                if (!origMLineParts[origPartIndex].equals(codecRtpMap)) {
                    newMLine.append(" ").append(origMLineParts[origPartIndex]);
                }
            }
            lines[mLineIndex] = newMLine.toString();
            Log.d(TAG, "Change media description: " + lines[mLineIndex]);
        } else {
            Log.e(TAG, "2: " + lines[mLineIndex]);
        }
        StringBuilder newSdpDescription = new StringBuilder();
        for (String line : lines) {
            newSdpDescription.append(line).append("\r\n");
        }
        return newSdpDescription.toString();
    }

    public interface PeerConnectionEvents {

        /**
         * Callback fired once local SDP is created and set.
         */
        void onLocalDescription(final SessionDescription sdp);

        /**
         * Callback fired once local Ice candidate is generated.
         */
        void onIceCandidate(final IceCandidate candidate);

        /**
         * Callback fired once local ICE candidates are removed.
         */
        void onIceCandidatesRemoved(final IceCandidate[] candidates);

        /**
         * Callback fired once ICE candidates gathering complete.
         */
        void onIceGatheringComplete();

        /**
         * Callback fired once connection is established (IceConnectionState is
         * CONNECTED).
         */
        void onIceConnected();

        /**
         * Callback fired once connection is closed (IceConnectionState is
         * DISCONNECTED).
         */
        void onIceDisconnected();

        /**
         * Callback fired once peer connection is closed.
         */
        void onPeerConnectionClosed();

        /**
         * Callback fired once peer connection statistics is ready.
         */
        void onPeerConnectionStatsReady(final StatsReport[] reports);

        /**
         * Callback fired once peer connection error happened.
         */
        void onPeerConnectionError(final String description);

    }

}
