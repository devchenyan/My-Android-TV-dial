package tv.caratech.tvclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.RendererCommon;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;
import org.webrtc.SurfaceViewRenderer;

import java.util.ArrayList;

import cn.caratech.SIP.SDPOptimization;

/**
 * Created by wurenhai on 2016/5/31.
 */
public class CallingActivity extends AppCompatActivity
        implements PeerConnectionClient.PeerConnectionEvents, SIPCall.SIPCallEvent, SIPClient.SIPCallObserver {

    private static final String TAG = "CallingActivity";

    private int sipCallId;
    //对方sip号码,从Intent中获取
    private String sipCalled;
    //是否主叫,如果是主叫,需要发起Offer
    private boolean isCaller;
    //对方通过SIP发过来的sdp消息内容
    private String offerSdp;

    private boolean isActivityFinished = false;
    private boolean iceConnected = false;

    private EglBase rootEglBase;
    private SurfaceViewRenderer localRender;
    private SurfaceViewRenderer remoteRender;
    private PercentFrameLayout localRenderLayout;
    private PercentFrameLayout remoteRenderLayout;
    private RendererCommon.ScalingType scalingType = RendererCommon.ScalingType.SCALE_ASPECT_FILL;

    //PeerConnectionClient与SIPClient都为单实例
    private SIPClient sipClient = SIPClient.getInstance();
    private PeerConnectionClient pcClient;

    private SessionDescription localSdp;
    private ArrayList<IceCandidate> localCandidates = new ArrayList<IceCandidate>();

    private AppRTCAudioManager audioManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);

        Intent intent = getIntent();
        sipCallId = intent.getIntExtra("sipCallId", -1);
        sipCalled = intent.getStringExtra("sipCalled");
        isCaller = intent.getBooleanExtra("isCaller", true);
        offerSdp = intent.getStringExtra("offerSdp");

        localRender = (SurfaceViewRenderer)findViewById(R.id.local_video_view);
        remoteRender = (SurfaceViewRenderer)findViewById(R.id.remote_video_view);
        localRenderLayout = (PercentFrameLayout)findViewById(R.id.local_video_layout);
        remoteRenderLayout = (PercentFrameLayout)findViewById(R.id.remote_video_layout);

        rootEglBase = EglBase.create();
        localRender.init(rootEglBase.getEglBaseContext(), null);
        remoteRender.init(rootEglBase.getEglBaseContext(), null);
        localRender.setZOrderMediaOverlay(true);

        updateVideoView();

        audioManager = AppRTCAudioManager.create(this, new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "onAudioManagerChangedState");
            }
        });
        Log.d(TAG, "Initializing the audio manager...");
        audioManager.init();

        pcClient = PeerConnectionClient.getInstance();
        pcClient.setEventListener(this);
        pcClient.createPeerConnectionFactory(getApplicationContext());

        pcClient.createPeerConnection(rootEglBase.getEglBaseContext(), localRender, remoteRender);


        if (isCaller) {
            pcClient.createOffer();
        } else {
            if (offerSdp != null && sipCallId > 0) {
                sipClient.addCall(new SIPCall(sipCallId).setEventListener(CallingActivity.this));

                SDPOptimization sdpOptimization = new SDPOptimization();
                offerSdp = sdpOptimization.disableRTCPMuxAndBundle(offerSdp);

                Log.i(TAG, "Set remote offer sdp");
                pcClient.setRemoteDescription(SessionDescription.Type.OFFER, offerSdp);
                pcClient.createAnswer();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pcClient.startVideoSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (pcClient != null) {
            pcClient.stopVideoSource();
        }
    }

    @Override
    protected void onDestroy() {
        disconnect();
        if (sipCallId > 0) {
            sipClient.removeCall(sipCallId);
        }
        rootEglBase.release();
        super.onDestroy();
    }

    public void onClickHangup(View view){
        sipClient.hangup(sipCallId);
        Toast.makeText(this, "onClickHangup", Toast.LENGTH_SHORT).show();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
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

    private void call(final String targetSdp) {
        sipClient.call(this, sipCalled, targetSdp);
    }

    @Override
    public void notifySdkCall(final int callId) {
        sipCallId = callId;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (sipCallId > 0) {
                    Log.i(TAG, "sipSdk.call alloc new callId: " + sipCallId);
                    sipClient.addCall(new SIPCall(sipCallId).setEventListener(CallingActivity.this));
                } else {
                    disconnectWithError("Invoke sipSdk.call failed: " + sipCallId);
                }
            }
        });
    }

    private void answer(final String targetSdp) {
        sipClient.answer(this, sipCallId, targetSdp);
    }

    @Override
    public void notifySdkAnswer(final int result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (result != 0) {
                    disconnectWithError("Invoke sipSdk.answer failed: " + result);
                }
            }
        });
    }

    private void disconnect() {
        if (isActivityFinished) {
            return;
        }
        isActivityFinished = true;

        if (pcClient != null) {
            pcClient.close();
            pcClient = null;
        }
        if (localRender != null) {
            localRender.release();
            localRender = null;
        }
        if (remoteRender != null) {
            remoteRender.release();
            remoteRender = null;
        }
        if (audioManager != null) {
            audioManager.close();
            audioManager = null;
        }
        setResult(0);
        finish();
    }

    private void disconnectWithError(final String message) {
        Log.e(TAG, message);
        if (isActivityFinished) {
            return;
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }

    /*
     ----- Implementations of PeerConnectionClient.PeerConnectionEvents -----
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
        //ICE穿透信息收集完成后,追加到localsdp然后发送给对方
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (localSdp == null) {
                    Log.e(TAG, "localSdp is not gathered yet.");
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
                //disconnect();
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

    /*
     ----- Implementations of SIPCall.SIPCallEvent -----
     */
    @Override
    public void onInviteFailure(final int callId, final int code, final String reason) {
        disconnectWithError("onInviteFailure: " + callId + ", " + code + ", " + reason);
    }

    @Override
    public void onAnswered(int callId, final String sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pcClient == null) {
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
    public void onCallConnected(int callId) {
        Log.i(TAG, "onCallConnected");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(CallingActivity.this, "onCallConnected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCallClosed(int callId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                disconnect();
            }
        });
    }
}
