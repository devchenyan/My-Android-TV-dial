package cn.caratel.voip.cells;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import cn.caratech.voip.R;

/**
 * Created by chenyan on 6/30/16.
 */
public class QrcodeFragment extends Fragment {
    private ImageView qrImg;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_qrcode, container, false);

        qrImg = (ImageView) v.findViewById(R.id.qrcode_img);

        return v;
    }

//    TODO: 二维码的设定
}
