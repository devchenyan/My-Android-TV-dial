package cn.caratel.voip;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;

import cn.caratech.voip.R;
import cn.caratel.voip.cells.PanelFragment;
import cn.caratel.voip.cells.QrcodeFragment;

/**
 * Created by chenyan on 6/29/16.
 */
public class ContactAddActivity extends Activity {

    private FragmentManager fragmentManager;

    private PanelFragment panelFragment;
    private QrcodeFragment qrcodeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_contact_add);

        fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();

        if (panelFragment == null) {
            panelFragment = new PanelFragment();
            fragmentTransaction.add(R.id.panel_container, panelFragment);
        }else {
            fragmentTransaction.show(panelFragment);
        }

        if (qrcodeFragment == null) {
            qrcodeFragment = new QrcodeFragment();
            fragmentTransaction.add(R.id.qrcode_container, qrcodeFragment);
        } else {
            fragmentTransaction.show(qrcodeFragment);
        }

        fragmentTransaction.commit();
    }
}
