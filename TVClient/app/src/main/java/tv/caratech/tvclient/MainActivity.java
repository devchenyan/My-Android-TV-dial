package tv.caratech.tvclient;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cn.caratech.SIP.SIPEvent;
import cn.caratech.SIP.SIPSDK;

public class MainActivity extends AppCompatActivity implements SIPClient.SIPClientEvent {

    private static final String TAG = "MainActivity";

    private static final int CALLER_CODE = 1;
    private static final int CALLEE_CODE = 2;

    private static final String[] allSipUsers = new String[]{
            "101",
            "102",
            "103",
            "104",
            "105",
            "106",
            "107",
            "108",
            "109",
            "110",
            "111",
            "112",
            "113",
            "114",
            "115",
            "116",
            "117",
            "118",
            "119",
            "120",
            "121",
            "122",
            "123",
            "124",
            "125",
    };

    //自己的SIP配置参数
    private String sipUser = "115";
    private String sipPass = "115";
    private String sipDomain = "10.0.0.213";
    private int sipPort = 5060;

    //对方SIP帐号
    private String sipCalledUser = "116";
    private String sipIncomeUser;
    private String offerSdp;
    private int incomeCallId;

    //是否正在通话中
    private boolean isBusy = false;
    private boolean isRing = false;

    //SIPClient与SIPSDK都为单实例
    private SIPClient sipClient = SIPClient.getInstance();
    private SIPSDK sipSdk;

    private View groupedButtons;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isBusy = false;
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadRandomUser();

        groupedButtons = findViewById(R.id.grouped_buttons);

        sipSdk = sipClient.getSipSdk();

        sipClient.configure(this, sipUser, sipPass, sipDomain, sipPort);
        sipClient.init();
    }

    @Override
    protected void onDestroy() {
        sipClient.release();
        super.onDestroy();
    }

    public void onClickCall(View view){

        if (isBusy || isRing) {
            Toast.makeText(this, "Busy now", Toast.LENGTH_SHORT).show();
            return;
        }

        EditText editText = (EditText)findViewById(R.id.txt_sipcalleduser);
        sipCalledUser = editText.getText().toString();
        if (sipCalledUser.isEmpty()) {
            editText.requestFocus();
            return;
        }

        Intent intent = new Intent(MainActivity.this, CallingActivity.class);
        intent.putExtra("sipCalled", sipCalledUser);
        intent.putExtra("isCaller", true);
        startActivityForResult(intent, CALLER_CODE);

        isBusy = true;
    }

    public void onClickAccept(View view){
        hideButtons();
        Intent intent = new Intent(MainActivity.this, CallingActivity.class);
        intent.putExtra("sipCallId", incomeCallId);
        intent.putExtra("sipCalled", sipIncomeUser);
        intent.putExtra("isCaller", false);
        intent.putExtra("offerSdp", offerSdp);
        startActivityForResult(intent, CALLEE_CODE);
        isBusy = true;
        isRing = false;
    }

    public void onClickReject(View view) {
        hideButtons();
        Toast.makeText(this, "onClickReject", Toast.LENGTH_SHORT).show();
        sipClient.reject(incomeCallId);
        isRing = false;
    }

    private void loadRandomUser() {
        Random random = new Random();
        int index = random.nextInt(allSipUsers.length);
        sipUser = allSipUsers[index];
        sipPass = sipUser;

        TextView lblMySipUser = (TextView)findViewById(R.id.lbl_mysipuser);
        lblMySipUser.setText(sipUser);
    }

    private void hideButtons() {
        groupedButtons.setVisibility(View.GONE);
    }

    private void showButtons() {
        groupedButtons.setVisibility(View.VISIBLE);
    }

    private void toastOnUiThread(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRegisterSuccess() {
        toastOnUiThread("onRegisterSuccess");
    }

    @Override
    public void onRegisterFailure(int code, String reason) {
        toastOnUiThread("onRegisterFailure: " + code + ", " + reason);
    }

    @Override
    public void onCallIncoming(final int callId, final String caller, final String sdp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (isRing || isBusy) {
                    Toast.makeText(MainActivity.this, "Busy now", Toast.LENGTH_SHORT).show();
                    sipClient.reject(callId);
                    return;
                }
                isRing = true;

                incomeCallId = callId;
                sipIncomeUser = caller;
                offerSdp = sdp;

                showButtons();
            }
        });
    }
}
