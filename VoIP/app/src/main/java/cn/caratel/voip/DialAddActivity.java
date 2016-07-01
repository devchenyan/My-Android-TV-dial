package cn.caratel.voip;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

import cn.caratech.voip.R;

/**
 * Created by chenyan on 6/29/16.
 */
public class DialAddActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_dial_add);
    }

}
