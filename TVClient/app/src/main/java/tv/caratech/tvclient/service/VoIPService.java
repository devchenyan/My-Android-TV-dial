package tv.caratech.tvclient.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import tv.caratech.tvclient.voip.SipConfigure;
import tv.caratech.tvclient.voip.VoIPManager;

public class VoIPService extends Service {

    private static final String TAG = "VoIPService";

    public final static String SIP_CMD_ARG = "sip_cmd";
    public final static String SIP_USER_ARG = "sip_user";
    public final static String SIP_PASS_ARG = "sip_pass";
    public final static String SIP_DOMAIN_ARG = "sip_domain";
    public final static String SIP_PORT_ARG = "sip_port";
    public final static String SIP_CALLED_USR_ARG = "sip_called_user";

    public final static String SIP_CMD_START = "start";
    public final static String SIP_CMD_CALL = "call";
    public final static String SIP_CMD_SHUTDOWN = "shutdown";

    private SipConfigure sipConfigure = new SipConfigure();

    private boolean isRunning = false;
    private VoIPManager voipManager;

    public VoIPService() {
    }

    private void logSipConfigure() {
        Log.i(TAG, SIP_USER_ARG + ": " + sipConfigure.user);
        Log.i(TAG, SIP_PASS_ARG + ": " + sipConfigure.pass);
        Log.i(TAG, SIP_DOMAIN_ARG + ": " + sipConfigure.domain);
        Log.i(TAG, SIP_PORT_ARG + ": " + sipConfigure.port);
    }

    private void startClient() {
        isRunning = true;

        voipManager.init(sipConfigure)
                .start();
    }

    private void onSipCmdStart() {
        if (isRunning) {
            Log.w(TAG, "service is running");
            return;
        }

        logSipConfigure();

        if (!sipConfigure.validate()) {
            Log.e(TAG, "invalid sip configurations");
        } else {
            startClient();
        }
    }

    private void onSipCmdCall(Intent intent) {
        if (!isRunning) {
            Log.e(TAG, "service is not running");
            return;
        }

        String sipUser = intent.getStringExtra(SIP_CALLED_USR_ARG);
        voipManager.launchCall(sipUser);
    }

    private void onSipCmdShutdown() {
        isRunning = false;
        voipManager.shutdown();
    }

    private void restart() {
        Intent intent = new Intent(getApplicationContext(), VoIPService.class);
        intent.putExtra(SIP_CMD_ARG, SIP_CMD_START);
        intent.putExtra(SIP_USER_ARG, sipConfigure.user);
        intent.putExtra(SIP_PASS_ARG, sipConfigure.pass);
        intent.putExtra(SIP_DOMAIN_ARG, sipConfigure.domain);
        intent.putExtra(SIP_PORT_ARG, sipConfigure.port);
        startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        voipManager = new VoIPManager(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_STICKY;
        }
        final String cmd = intent.getStringExtra(SIP_CMD_ARG);
        Log.i(TAG, "cmd: " + cmd);
        switch (cmd) {
            case SIP_CMD_START:
                sipConfigure.user = intent.getStringExtra(SIP_USER_ARG);
                sipConfigure.pass = intent.getStringExtra(SIP_PASS_ARG);
                sipConfigure.domain = intent.getStringExtra(SIP_DOMAIN_ARG);
                sipConfigure.port = intent.getIntExtra(SIP_PORT_ARG, SipConfigure.INVALID_PORT);
                onSipCmdStart();
                break;
            case SIP_CMD_CALL:
                onSipCmdCall(intent);
                break;
            case SIP_CMD_SHUTDOWN:
                onSipCmdShutdown();
                break;
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isRunning) {
            restart();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
