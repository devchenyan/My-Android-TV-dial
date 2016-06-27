package tv.caratech.tvclient;

import cn.caratech.SIP.SIPSDK;

/**
 * Created by wurenhai on 2016/6/2.
 */
public class SIPCall {

    private SIPSDK sipSdk;
    private int callId;
    private SIPCallEvent sipCallEvent;

    public SIPCall(int callId) {
        this.sipSdk = SIPClient.getInstance().getSipSdk();
        this.callId = callId;
    }

    public SIPCall setEventListener(SIPCallEvent listener) {
        sipCallEvent = listener;
        return this;
    }

    public SIPCallEvent getEventListener() {
        return sipCallEvent;
    }

    public int getCallId() {
        return callId;
    }

    public interface SIPCallEvent {

        void onInviteFailure(int callId, int code, final String reason);

        void onAnswered(int callId, final String sdp);

        void onCallConnected(int callId);

        void onCallClosed(int callId);

    }

}
