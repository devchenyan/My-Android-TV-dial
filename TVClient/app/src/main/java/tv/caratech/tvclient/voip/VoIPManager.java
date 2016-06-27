package tv.caratech.tvclient.voip;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.caratech.SIP.SDPOptimization;
import cn.caratech.SIP.SIPEvent;
import cn.caratech.SIP.SIPSDK;
import tv.caratech.tvclient.AppRTCAudioManager;
import tv.caratech.tvclient.PeerConnectionClient;
import tv.caratech.tvclient.PercentFrameLayout;
import tv.caratech.tvclient.R;
import tv.caratech.tvclient.util.FloatingWindow;

/**
 * Created by wurenhai on 2016/6/15.
 */
public class VoIPManager implements SIPEvent, PeerConnectionClient.PeerConnectionEvents {

    private static final String TAG = "SIPClient";

    enum VoIPStatus {
        IDLE,
        RING,
        BUSY,
    }

    enum SipStatus {
        NotStarted,
        Connecting,
        Connected,
    }

    private Context context;
    private boolean isShutdown = false;

    private View voipRingView;
    private View voipCallView;

    private FloatingWindow voipRingWindow;
    private FloatingWindow voipCallWindow;

    private View.OnClickListener onClickAcceptListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            voipRingWindow.hide();
            startCall();
        }
    };

    private View.OnClickListener onClickRejectListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            voipRingWindow.hide();
            reject(sipCallId);
            voipStatus = VoIPStatus.IDLE;
        }
    };

    private View.OnClickListener onClickHangupListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            hangup(sipCallId);
            Toast.makeText(context, "onClickHangup", Toast.LENGTH_SHORT).show();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    disconnect();
                }
            });
        }
    };

    //Rendering
    private EglBase rootEglBase;
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

    //UI thread
    private Handler handler = new Handler();
    //sip client thread
    private ExecutorService sipExecutor = Executors.newSingleThreadExecutor();

    private SIPSDK sipSdk = new SIPSDK();
    private SipStatus sipStatus = SipStatus.NotStarted;
    private VoIPStatus voipStatus = VoIPStatus.IDLE;

    //呼叫中对方的SIP user
    private String sipCalledUser;
    //自己是主叫还是被叫?
    private boolean isCaller = false;
    //当前通话的SIP callId
    private int sipCallId = -1;
    //自己作为被叫时对方发过来的offer sdp
    private String offerSdp;

    private PeerConnectionClient pcClient;
    private SessionDescription localSdp;
    private ArrayList<IceCandidate> localCandidates = new ArrayList<IceCandidate>();
    private AppRTCAudioManager audioManager = null;
    private boolean iceConnected = false;

    private MediaPlayer ringtone;

    public void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public VoIPManager(Context context) {
        this.context = context;

        loadView();

        initRingtone();
    }

    private void initRingtone() {
        Uri uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE);
        if (uri == null) {
            //TODO: set default ring file uri
            return;
        }
        ringtone = MediaPlayer.create(context, RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_RINGTONE));
        ringtone.setLooping(true);
        try {
            ringtone.prepare();
        } catch (IllegalStateException e) {
            Log.e(TAG, "initRingtone exception: " + e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (IOException e) {
            Log.e(TAG, "initRingtone exception: " + e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void startRing() {
        if (ringtone == null) {
            return;
        }
        try {
            ringtone.start();
        } catch (IllegalStateException e) {
            Log.e(TAG, "startRing exception: " + e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void stopRing() {
        if (ringtone == null) {
            return;
        }
        try {
            ringtone.stop();
        } catch (IllegalStateException e) {
            Log.e(TAG, "stopRing exception: " + e.getMessage());
            Log.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void releaseRingtone() {
        if (ringtone != null) {
            ringtone.release();
            ringtone = null;
        }
    }

    public void setVoipStatus(VoIPStatus status) {
        voipStatus = status;
        if (status == VoIPStatus.RING) {
            //startRing();
        } else {
            //stopRing();
        }
    }

    private void loadView() {
        voipRingView = LayoutInflater.from(context).inflate(R.layout.voip_ring, null);
        voipCallView = LayoutInflater.from(context).inflate(R.layout.voip_call, null);

        ImageButton btn_accept_call = (ImageButton)voipRingView.findViewById(R.id.voip_accept_call);
        btn_accept_call.setOnClickListener(onClickAcceptListener);
        ImageButton btn_reject_call = (ImageButton)voipRingView.findViewById(R.id.voip_reject_call);
        btn_reject_call.setOnClickListener(onClickRejectListener);
        ImageButton btn_hangup_call = (ImageButton)voipCallView.findViewById(R.id.voip_hangup_call);
        btn_hangup_call.setOnClickListener(onClickHangupListener);

        localRender = (SurfaceViewRenderer)voipCallView.findViewById(R.id.local_video_view);
        remoteRender = (SurfaceViewRenderer)voipCallView.findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout)voipCallView.findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout)voipCallView.findViewById(R.id.remote_video_layout);

        rootEglBase = EglBase.create();
        localRender.init(rootEglBase.getEglBaseContext(), null);
        remoteRender.init(rootEglBase.getEglBaseContext(), null);
        localRender.setZOrderMediaOverlay(true);

        voipRingWindow = new FloatingWindow(context, voipRingView);
        voipCallWindow = new FloatingWindow(context, voipCallView)
                .setTouchMoveView(voipCallView, true);

        //voipRingWindow.show();
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.scale);
        btn_accept_call.startAnimation(animation);

        updateVideoView();
    }

    private void updateVideoView() {
        remoteRenderLayout.setPosition(0, 0, 100, 100);
        remoteRender.setScalingType(scalingType);
        remoteRender.setMirror(false);

        if (iceConnected) {
            localRenderLayout.setPosition(72, 72, 25, 25);
            localRender.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FIT);
        } else {
            localRenderLayout.setPosition(0, 0, 100, 100);
            localRender.setScalingType(scalingType);
        }
        localRender.setMirror(true);

        localRenderLayout.requestLayout();
        remoteRenderLayout.requestLayout();
    }

    /*
     * 开始呼叫对方
     */
    private void startCall() {
        audioManager = AppRTCAudioManager.create(context, new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onAudioManagerChangedState");
            }
        });
        Log.d(TAG, "Initializing the audio manager...");
        audioManager.init();
        pcClient = PeerConnectionClient.getInstance();
        pcClient.setEventListener(this);
        pcClient.createPeerConnectionFactory(context);

        pcClient.createPeerConnection(rootEglBase.getEglBaseContext(), localRender, remoteRender);

        //TODO: show floating call window
        voipCallWindow.show();

        if (isCaller) {
            pcClient.createOffer();
        } else {
            if (offerSdp != null && sipCallId > 0) {
                SDPOptimization sdpOptimization = new SDPOptimization();
                offerSdp = sdpOptimization.disableRTCPMuxAndBundle(offerSdp);

                Log.i(TAG, "Set remote offer sdp");
                pcClient.setRemoteDescription(SessionDescription.Type.OFFER, offerSdp);
                pcClient.createAnswer();
            } else {
                disconnectWithError("Unexpected arguments.");
            }
        }

        pcClient.startVideoSource();
    }

    private void disconnect() {
        if (voipStatus == VoIPStatus.IDLE) {
            return;
        }
        setVoipStatus(VoIPStatus.IDLE);

        if (pcClient != null) {
            pcClient.close();
            pcClient = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }

        //TODO: hide floating call window
        voipCallWindow.hide();
    }

    private void disconnectWithError(final String message) {
        Log.e(TAG, message);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }

    public VoIPManager init(final SipConfigure configure) {
        sipExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to init sip client");
                sipSdk.init();
                sipSdk.setSIPEventHandler(VoIPManager.this);
                sipSdk.setUser(configure.user, configure.pass, configure.domain, configure.port);
            }
        });
        return this;
    }

    public void start() {
        sipExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to register to sip server");
                sipStatus = SipStatus.Connecting;
                sipSdk.register();
            }
        });
    }

    /*
     * 自己发起一个呼叫
     */
    public void launchCall(final String calledUser) {
        if (calledUser == null || "".equals(calledUser)) {
            Log.e(TAG, "Try to call an invalid user: " + calledUser);
            return;
        }
        if (voipStatus != VoIPStatus.IDLE) {
            Log.e(TAG, "Currently no idle: " + voipStatus);
            return;
        }
        setVoipStatus(VoIPStatus.RING);
        sipCalledUser = calledUser;
        isCaller = true;

        startCall();
    }

    public void shutdown() {
        isShutdown = true;
        sipExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "graceful shutdowm sip client");
                sipSdk.unregister();
                sipSdk.done();
                sipStatus = SipStatus.NotStarted;
            }
        });
        releaseRingtone();
        voipCallWindow.hide();
        voipRingWindow.hide();
    }

    private void call(final String targetSdp) {
        final String callee = sipCalledUser;
        sipExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to call " + callee);
                sipCallId = sipSdk.call(callee, targetSdp);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (sipCallId > 0) {
                            Log.i(TAG, "sipSdk.call alloc new callId: " + sipCallId);
                        } else {
                            disconnectWithError("Invoke sipSdk.call failed: " + sipCallId);
                        }
                    }
                });
            }
        });
    }

    private void answer(final String targetSdp) {
        final int callId = sipCallId;
        sipExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to answer: " + callId);
                int result = sipSdk.answer(callId, targetSdp);
                if (result != 0) {
                    disconnectWithError("Invoke sipSdk.answer failed: " + result);
                }
            }
        });
    }

    private void reject(final int callId) {
        sipExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to reject: " + callId);
                sipSdk.reject(callId);
            }
        });
    }

    private void hangup(final int callId) {
        sipExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Try to hangup: " + callId);
                sipSdk.hangup(callId);
            }
        });
    }

    /*
     * SIPEvent implements
     */
    @Override
    public void onRegisterSuccess() {
        Log.i(TAG, "onRegisterSuccess");
        sipStatus = SipStatus.Connected;
    }

    @Override
    public void onRegisterFailure(int code, String reason) {
        Log.i(TAG, "onRegisterFailure: " + code + ", " + reason);
        if (!isShutdown) {
            start();
        }
    }

    @Override
    public void onInviteIncoming(final int callId, final String caller, final String sdp) {
        Log.i(TAG, "onInviteIncoming: " + callId + ", " + caller);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (voipStatus != VoIPStatus.IDLE) {
                    Log.e(TAG, "Currently no idle: " + voipStatus);
                    reject(callId);
                    return;
                }
                setVoipStatus(VoIPStatus.RING);

                sipCallId = callId;
                sipCalledUser = caller;
                offerSdp = sdp;

                //TODO: show ring floating window
                voipRingWindow.show();
            }
        });
    }

    @Override
    public void onInviteTrying(int callId) {
        Log.i(TAG, "onInviteTrying: " + callId);
    }

    @Override
    public void onInviteRinging(int callId) {
        Log.i(TAG, "onInviteRinging: " + callId);
    }

    @Override
    public void onInviteClosed(int callId) {
        Log.i(TAG, "onInviteClosed: " + callId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }

    @Override
    public void onInviteAnswered(final int callId, final String sdp) {
        Log.i(TAG, "onInviteAnswered: " + callId);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (voipStatus == VoIPStatus.IDLE || pcClient == null) {
                    return;
                }
                SDPOptimization sdpOptimization = new SDPOptimization();
                String answerSdp = sdpOptimization.disableRTCPMuxAndBundle(sdp);

                Log.i(TAG, "Set remote answer sdp");
                pcClient.setRemoteDescription(SessionDescription.Type.ANSWER, answerSdp);
            }
        });
    }

    @Override
    public void onInviteConnected(final int callId) {
        Log.i(TAG, "onInviteConnected: " + callId);
        setVoipStatus(VoIPStatus.BUSY);
    }

    @Override
    public void onInviteFailure(int callId, int code, String reason) {
        disconnectWithError("onInviteFailure: " + callId + ", " + code + ", " + reason);
    }

    /*
     * PeerConnectionClient.PeerConnectionEvents implements
     */

    @Override
    public void onLocalDescription(final SessionDescription sdp) {
        localSdp = sdp;
    }

    @Override
    public void onIceCandidate(final IceCandidate candidate) {
        Log.i(TAG, "onIceCandidate: " + candidate.sdpMid + " " + candidate.sdp);
        localCandidates.add(candidate);
    }

    @Override
    public void onIceCandidatesRemoved(final IceCandidate[] candidates) {
        for (IceCandidate candidate : candidates) {
            localCandidates.remove(candidate);
        }
    }

    @Override
    public void onIceGatheringComplete() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (localSdp == null) {
                    disconnectWithError("localSdp is not gathered yet.");
                    return;
                }

                SDPOptimization sdpOptimization = new SDPOptimization();
                int result = sdpOptimization.optimization(localSdp.description, localCandidates);
                if (result != 0) {
                    Log.e(TAG, "SDP optimization failed: " + result + ", candidates: " + localCandidates.size());
                    disconnectWithError("SDP optimization failed: " + result);
                    return;
                }

                String targetSdp = sdpOptimization.getOptimizedSdp();
                if (isCaller) {
                    call(targetSdp);
                } else {
                    answer(targetSdp);
                }
            }
        });
    }

    @Override
    public void onIceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onIceConnected");
                iceConnected = true;
                updateVideoView();
            }
        });
    }

    @Override
    public void onIceDisconnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onIceDisconnected");
                iceConnected = false;
            }
        });
    }

    @Override
    public void onPeerConnectionClosed() {
        Log.i(TAG, "onPeerConnectionClosed");
    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {

    }

    @Override
    public void onPeerConnectionError(String description) {
        disconnectWithError(description);
    }

}
