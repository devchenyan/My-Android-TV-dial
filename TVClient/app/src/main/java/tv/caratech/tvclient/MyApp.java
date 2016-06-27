package tv.caratech.tvclient;

import android.app.Application;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;

import tv.caratech.tvclient.service.VoIPService;
import tv.caratech.tvclient.util.FloatingWindow;

/**
 * Created by wurenhai on 2016/6/15.
 */
public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

/*
        View view1 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.voip_calling, null);
        new FloatingWindow(getApplicationContext(), view1)
                .setTouchMoveView(view1, true)
                .show();

        View view2 = LayoutInflater.from(getApplicationContext()).inflate(R.layout.voip_calling, null);
        new FloatingWindow(getApplicationContext(), view2)
                .setTouchMoveView(view2, true)
                .show();
*/

/*        Intent intent = new Intent(getApplicationContext(), VoIPService.class);
        intent.putExtra(VoIPService.SIP_USER_ARG, "101");
        intent.putExtra(VoIPService.SIP_PASS_ARG, "101");
        intent.putExtra(VoIPService.SIP_DOMAIN_ARG, "10.0.0.213");
        intent.putExtra(VoIPService.SIP_PORT_ARG, 5060);
        startService(intent);*/
    }

}
