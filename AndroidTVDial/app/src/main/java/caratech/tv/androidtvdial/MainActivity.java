package caratech.tv.androidtvdial;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by echo on 6/27/16.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
