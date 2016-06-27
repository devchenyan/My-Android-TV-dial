package tv.caratech.tvclient.entry;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import tv.caratech.tvclient.CallingActivity;
import tv.caratech.tvclient.R;
import tv.caratech.tvclient.service.VoIPService;

public class EntryActivity extends AppCompatActivity {

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

    private void loadRandomUser() {
        Random random = new Random();
        int index = random.nextInt(allSipUsers.length);
        sipUser = allSipUsers[index];
        sipPass = sipUser;

        TextView lblMySipUser = (TextView)findViewById(R.id.lbl_mysipuser);
        lblMySipUser.setText(sipUser);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        loadRandomUser();

        Intent intent = new Intent(getApplicationContext(), VoIPService.class);
        intent.putExtra(VoIPService.SIP_CMD_ARG, VoIPService.SIP_CMD_START);
        intent.putExtra(VoIPService.SIP_USER_ARG, sipUser);
        intent.putExtra(VoIPService.SIP_PASS_ARG, sipPass);
        intent.putExtra(VoIPService.SIP_DOMAIN_ARG, sipDomain);
        intent.putExtra(VoIPService.SIP_PORT_ARG, sipPort);
        startService(intent);
    }

    @Override
    protected void onDestroy() {
        Intent intent = new Intent(getApplicationContext(), VoIPService.class);
        intent.putExtra(VoIPService.SIP_CMD_ARG, VoIPService.SIP_CMD_SHUTDOWN);
        startService(intent);

        super.onDestroy();
    }

    public void onClickCall(View view){
        EditText editText = (EditText)findViewById(R.id.txt_sipcalleduser);
        String calledUser = editText.getText().toString();
        if (calledUser.isEmpty()) {
            editText.requestFocus();
            return;
        }
        Toast.makeText(this, "onClickCall: " + calledUser, Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), VoIPService.class);
        intent.putExtra(VoIPService.SIP_CMD_ARG, VoIPService.SIP_CMD_CALL);
        intent.putExtra(VoIPService.SIP_CALLED_USR_ARG, calledUser);
        startService(intent);
    }
}
