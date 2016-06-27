package tv.caratech.tvclient;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.caratech.SIP.SIPEvent;
import cn.caratech.SIP.SIPSDK;

/**
 * Created by wurenhai on 2016/6/2.
 */
public class SIPClient implements SIPEvent {

    private static final String TAG = "SIPClient";

    private final static SIPClient instance = new SIPClient();

    private final SIPSDK sipSdk = new SIPSDK();
    private String sipUser;
    private String sipPass;
    private String sipDomain;
    private int sipPort;

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    private SIPClientEvent sipClientEvents;

    private Map<Integer, SIPCall> sipCallMap = new HashMap<Integer, SIPCall>();

    public static SIPClient getInstance() {
        return instance;
    }

    private SIPClient() {
    }

    public SIPSDK getSipSdk() {
        return sipSdk;
    }

    public void configure(SIPClientEvent listener, String user, String pass, String domain, int port) {
        sipClientEvents = listener;
        sipUser = user;
        sipPass = pass;
        sipDomain = domain;
        sipPort = port;
    }

    public void init() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sipSdk.init();
                sipSdk.setSIPEventHandler(SIPClient.this);
                sipSdk.setUser(sipUser, sipPass, sipDomain, sipPort);
                sipSdk.register();
            }
        });
    }

    public void release() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sipSdk.unregister();
                sipSdk.done();
            }
        });
    }

    public SIPCall getCall(int callId) {
        return sipCallMap.get(callId);
    }

    public void addCall(SIPCall sipCall) {
        sipCallMap.put(sipCall.getCallId(), sipCall);
    }

    public SIPCall removeCall(int callId) {
        return sipCallMap.remove(callId);
    }

    public void call(final SIPCallObserver observer, final String callee, final String sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to call " + callee);
                int callId = sipSdk.call(callee, sdp);
                observer.notifySdkCall(callId);
            }
        });
    }

    public void answer(final SIPCallObserver observer, final int callId, final String sdp) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to answer " + callId);
                int result = sipSdk.answer(callId, sdp);
                observer.notifySdkAnswer(result);
            }
        });
    }

    public void reject(final int callId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to reject " + callId);
                sipSdk.reject(callId);
            }
        });
    }

    public void hangup(final int callId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to hangup " + callId);
                sipSdk.hangup(callId);
            }
        });
    }

    /*
     Implement SIPEvent
     */
    @Override
    public void onRegisterSuccess() {
        Log.d(TAG, "onRegisterSuccess");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sipClientEvents.onRegisterSuccess();
            }
        });
    }

    @Override
    public void onRegisterFailure(final int code, final String reason) {
        Log.i(TAG, "onRegisterFailure: " + code + ", " + reason);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sipClientEvents.onRegisterFailure(code, reason);
            }
        });
    }

    @Override
    public void onInviteIncoming(final int callId, final String caller, final String sdp) {
        Log.i(TAG, "onInviteIncoming: " + callId + ", " + caller);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sipClientEvents.onCallIncoming(callId, caller, sdp);
            }
        });
    }

    @Override
    public void onInviteTrying(final int callId) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onInviteTrying: " + callId);
            }
        });
    }

    @Override
    public void onInviteRinging(int callId) {
        Log.i(TAG, "onInviteRinging: " + callId);
    }

    @Override
    public void onInviteClosed(final int callId) {
        Log.i(TAG, "onInviteClosed: " + callId);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final SIPCall sipCall = getCall(callId);
                if (sipCall == null) {
                    return;
                }
                sipCall.getEventListener().onCallClosed(callId);
            }
        });
    }

    @Override
    public void onInviteAnswered(final int callId, final String sdp) {
        Log.i(TAG, "onInviteAnswered: " + callId);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final SIPCall sipCall = getCall(callId);
                if (sipCall == null) {
                    return;
                }
                sipCall.getEventListener().onAnswered(callId, sdp);
            }
        });
    }

    @Override
    public void onInviteConnected(final int callId) {
        Log.i(TAG, "onInviteConnected: " + callId);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final SIPCall sipCall = getCall(callId);
                if (sipCall == null) {
                    return;
                }
                sipCall.getEventListener().onCallConnected(callId);
            }
        });
    }

    @Override
    public void onInviteFailure(final int callId, final int code, final String reason) {
        Log.i(TAG, "onInviteFailure: " + callId + ", " + code + ", " + reason);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final SIPCall sipCall = getCall(callId);
                if (sipCall == null) {
                    return;
                }
                sipCall.getEventListener().onInviteFailure(callId, code, reason);
            }
        });
    }

    public interface SIPClientEvent {

        void onRegisterSuccess();

        void onRegisterFailure(final int code, final String reason);

        void onCallIncoming(int callId, final String caller, final String sdp);

    }

    public interface SIPCallObserver {

        void notifySdkCall(int callId);
        void notifySdkAnswer(int result);

    }

}
