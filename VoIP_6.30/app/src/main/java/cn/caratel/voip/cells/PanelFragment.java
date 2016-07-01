package cn.caratel.voip.cells;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.caratech.voip.R;

/**
 * Created by chenyan on 6/30/16.
 */
public class PanelFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_panel, container, false);
        return v;
    }

    //TODO: 按钮的监听\按钮点击的处理逻辑
}
