package cn.caratech.SIP;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.webrtc.IceCandidate;

/**
 * Created by hfx on 2016/6/1.
 */
public class SDPOptimization {

    public static final int ERR_AUDIO_CONNECTION_INFO = -1000;
    public static final int ERR_VIDEO_CONNECTION_INFO = -1001;
    public static final int ERR_AUDIO_ICE_EMPTY = -1002;

    private static final String mRegCandidateRule = "^a=candidate:([0-9]+)\\s([0-9]+)\\s(udp|UDP)\\s([0-9]+)+\\s([0-9a-zA-Z\\.]+)\\s([0-9]+)\\styp\\s.*";
    private static final String mRegIp4Rule = "[0-9]+\\.[0-9]\\.[0-9]+\\.[0-9]";
    private static final String mRegCINRule = "c=IN";
    private static final String mRegOriginRule = "o=";
    private static final String mRegMidumAudioRule = "m=audio ";
    private static final String mRegMidumAudioRepRule = "m=audio\\s[0-9]+";
    private static final String mRegMidumVideoRule = "m=video ";
    private static final String mRegMidumVideoRepRule = "m=video\\s[0-9]+";
    private static final String mRegMidumRtcpRule = "a=rtcp:";
    private static final String mRegMidumRtcpRepRule = "a=rtcp:[0-9]+";
    private static final String mRegCandidateStartRule = "a=";
    private static final String mRegBUNDLERule = "a=group:BUNDLE";
    private static final String mRegBUNDLERepVal = "a=xgroup:BUNDLE";
    private static final String mRegRTCPMuxRule = "a=rtcp-mux";
    private static final String mRegRTCPMuxRepVal = "a=invalidrtcpmux";
    private ICEInfo mAudioIceInfo = null;
    private ICEInfo mVideoIceInfo = null;

    private ArrayList<String> mAudioIceCandidateSdps = new ArrayList<String>();
    private ArrayList<String> mVideoIceCandidateSdps = new ArrayList<String>();

    private String mTargetSDP = null;

    private String mAudioMid = null;
    private String mVideoMid = null;

    public SDPOptimization() {

        mAudioIceInfo = new ICEInfo();
        mVideoIceInfo = new ICEInfo();

        mAudioIceCandidateSdps = new ArrayList<String>();
        mVideoIceCandidateSdps = new ArrayList<String>();
    }

    // NOTE:
    // Return value
    //   0   - for okay
    //   < 0 - for some error
    public int optimization(String sdp, ArrayList<IceCandidate> iceCandidates) {
        int ret = 0;

        obtainMediaMid(sdp);

        generateIceSdps(iceCandidates); // MUST before generate connection info

        ret = generateConnectionInfo();

        if (ret != 0) // Connection info create failure
            return ret;

        return optimizationInternal(sdp);
    }

    // NOTE:
    //   MAYBE return a null object
    //   MUST judge value whether null
    public String getOptimizedSdp() {
        return mTargetSDP;
    }

    private void obtainMediaMid(String sdp) {
        List<String> sdpLines = new ArrayList<String>(Arrays.asList(sdp
                .split("\r\n")));

        Pattern p = Pattern.compile("a=mid:(.+)");

        final int globalSec = 1;
        final int audioSec = 2;
        final int videoSec = 3;

        int sec = globalSec;

        for (int i = 0; i < sdpLines.size(); i++) {
            if (sdpLines.get(i).contains(mRegMidumAudioRule)) {
                sec = audioSec;
            }
            else if (sdpLines.get(i).contains(mRegMidumVideoRule)) {
                sec = videoSec;
            }

            Matcher m = p.matcher(sdpLines.get(i));
            if (m.find()) {
                if (sec == audioSec) {
                    mAudioMid = m.group(1);
                }
                else if(sec == videoSec){
                    mVideoMid = m.group(1);
                }
                else
                {
                    // a=mid: --> placed error
                }
            }

            if(mAudioMid != null && mVideoMid != null)
            {
                break;
            }
        }
    }

    private int optimizationInternal(String sdp) {
        List<String> sdpLines = new ArrayList<String>(Arrays.asList(sdp
                .split("\r\n")));

        if (!mAudioIceCandidateSdps.isEmpty()) {
            int index = 0;
            for (; index < sdpLines.size(); index++) {
                if (sdpLines.get(index).contains(mRegOriginRule)) {
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(
                                    mRegIp4Rule,
                                    mAudioIceInfo.connectionAddr()));
                }

                if (sdpLines.get(index).contains(mRegMidumAudioRule)) {
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(mRegMidumAudioRepRule,
                                    mRegMidumAudioRule + mAudioIceInfo.rtpPort()));
                    sdpLines.addAll(index + 3, mAudioIceCandidateSdps);
                    break;
                }
            }

            for (; index < sdpLines.size(); index++) {
                if (sdpLines.get(index).contains(mRegMidumVideoRule))
                    break;

                if (sdpLines.get(index).contains(mRegCINRule)) {
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(
                                    mRegIp4Rule,
                                    mAudioIceInfo.connectionAddr()));
                }

                if (sdpLines.get(index).contains(mRegMidumRtcpRule)) {
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(mRegMidumRtcpRepRule,
                                    mRegMidumRtcpRule + mAudioIceInfo.rtcpPort()));
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(
                                    mRegIp4Rule,
                                    mAudioIceInfo.connectionAddr()));
                }
            }
        }

        if (!mVideoIceCandidateSdps.isEmpty()) {
            int index = 0;
            for (; index < sdpLines.size(); index++) {
                if (sdpLines.get(index).contains(mRegMidumVideoRule)) {
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(mRegMidumVideoRepRule,
                                    mRegMidumVideoRule + mVideoIceInfo.rtpPort()));
                    sdpLines.addAll(index + 3, mVideoIceCandidateSdps);
                    break;
                }
            }

            for (; index < sdpLines.size(); index++) {
                if (sdpLines.get(index).contains(mRegMidumAudioRule))
                    break;

                if (sdpLines.get(index).contains(mRegCINRule)) {
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(
                                    mRegIp4Rule,
                                    mVideoIceInfo.connectionAddr()));
                }

                if (sdpLines.get(index).contains(mRegMidumRtcpRule)) {
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(mRegMidumRtcpRepRule,
                                    mRegMidumRtcpRule + mVideoIceInfo.rtcpPort()));
                    sdpLines.set(
                            index,
                            sdpLines.get(index).replaceAll(
                                    mRegIp4Rule,
                                    mVideoIceInfo.connectionAddr()));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sdpLines.size(); i++) {
            sb.append(sdpLines.get(i));
            sb.append("\r\n");
        }

        mTargetSDP = new String(sb.toString());
        mTargetSDP = disableRTCPMuxAndBundle(mTargetSDP);

        return 0;
    }

    // when receive a offer obtains BUNDLE:GROUP && rtcp-mux
    //      will only one onIceCandidate callback
    // S: disable Bundle and rtcp-mux
    public static String disableRTCPMuxAndBundle(String sdp)
    {
        sdp = sdp.replace(mRegBUNDLERule, mRegBUNDLERepVal);
        sdp = sdp.replaceAll(mRegRTCPMuxRule, mRegRTCPMuxRepVal);

        return sdp;
    }

    private void generateIceSdps(ArrayList<IceCandidate> iceCandidates) {
        for (int i = 0; i < iceCandidates.size(); i++) {
            int endIndex = iceCandidates.get(i).sdp.indexOf(" generation");
            if (endIndex < 0)
                endIndex = iceCandidates.get(i).sdp.length();

            if (mAudioMid != null && !mAudioMid.isEmpty() &&
                    iceCandidates.get(i).sdpMid.contains(mAudioMid)) // audio ice
            // candidates
            {
                mAudioIceCandidateSdps.add(mRegCandidateStartRule
                        + iceCandidates.get(i).sdp.substring(0, endIndex));
            }
            else if (mVideoMid != null && !mVideoMid.isEmpty() &&
                    iceCandidates.get(i).sdpMid.contains(mVideoMid))// video ice candidates
            {
                mVideoIceCandidateSdps.add(mRegCandidateStartRule
                        + iceCandidates.get(i).sdp.substring(0, endIndex));
            }
            else
                continue;
        }
    }

    private int generateConnectionInfo() {
        Pattern p = Pattern.compile(mRegCandidateRule);

        if (mAudioIceCandidateSdps.isEmpty())
            return ERR_AUDIO_ICE_EMPTY;

        for (int i = 0; i < mAudioIceCandidateSdps.size(); i++) {
            Matcher m = p.matcher(mAudioIceCandidateSdps.get(i));
            if (m.find()) {
                if (!mAudioIceInfo.isInitialized()) {
                    mAudioIceInfo.init(m.group(1), m.group(2), m.group(4),
                            m.group(5), m.group(6));
                } else {
                    mAudioIceInfo.setPort(m.group(1), m.group(2), m.group(6));
                }
            }

            if (mAudioIceInfo.isComplete())
                break;
        }

        if (!mAudioIceInfo.isComplete())
            return ERR_AUDIO_CONNECTION_INFO;

        if (mVideoIceCandidateSdps.isEmpty())
            return 0;

        for (int i = 0; i < mVideoIceCandidateSdps.size(); i++) {
            Matcher m = p.matcher(mVideoIceCandidateSdps.get(i));

            if (m.find()) {
                if (!mVideoIceInfo.isInitialized()) {
                    // make sure audio & vidoe candidate using the same
                    // foundation
                    if (mAudioIceInfo.foundation().equals(m.group(1)))
                        mVideoIceInfo.init(m.group(1), m.group(2), m.group(4),
                                m.group(5), m.group(6));
                } else {
                    mVideoIceInfo.setPort(m.group(1), m.group(2), m.group(6));
                }
            }

            if (mVideoIceInfo.isComplete())
                break;
        }

        if (!mVideoIceInfo.isComplete())
            return ERR_VIDEO_CONNECTION_INFO;

        return 0;
    }


    public class ICEInfo {
        private String mFoundation = "";
        private String mConnectionAddr = "";
        private String mConnectionRTPPort = "";
        private String mConnectionRTCPPort = "";

        private boolean mInitialized = false;

        public final static String COMPONENT_ID_RTP = "1";
        public final static String COMPONENT_ID_RTCP = "2";

        public ICEInfo() {
        }

        public boolean isInitialized() {
            return mInitialized;
        }

        public String foundation() {
            return mFoundation;
        }

        public String rtpPort() {
            return mConnectionRTPPort;
        }

        public String rtcpPort() {
            return mConnectionRTCPPort;
        }

        public String connectionAddr() {
            return mConnectionAddr;
        }

        public boolean isComplete() {
            return (!mFoundation.isEmpty() // && !mConnectionRTCPPort.isEmpty()
                    && !mConnectionAddr.isEmpty()
                    && !mConnectionRTPPort.isEmpty() && mInitialized);
        }

        public boolean init(String foundation, String componentId,
                            String priority, String addr, String port) {
            if (mInitialized)
                return false;

            mFoundation = foundation;
            mConnectionAddr = addr;

            if (componentId.equals(COMPONENT_ID_RTP)) {
                mConnectionRTPPort = port;
            } else {
                mConnectionRTCPPort = port;
            }

            mInitialized = true;

            return true;
        }

        public boolean setPort(String foundation, String componentId,
                               String port) {
            if (!mInitialized || !foundation.equals(mFoundation))
                return false;

            if (componentId.equals(COMPONENT_ID_RTP)
                    && mConnectionRTPPort.isEmpty()) {
                mConnectionRTPPort = port;
            } else if (componentId.equals(COMPONENT_ID_RTCP)
                    && mConnectionRTCPPort.isEmpty()) {
                mConnectionRTCPPort = port;
            } else
                return false;

            return true;
        }
    }
}
