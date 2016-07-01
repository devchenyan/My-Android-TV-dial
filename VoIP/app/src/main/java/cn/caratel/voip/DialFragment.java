package cn.caratel.voip;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import cn.caratech.voip.R;

/**
 * Created by chenyan on 6/28/16.
 */
public class DialFragment extends Fragment{

    private ImageButton addBtn;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_dial, container, false);

        addBtn = (ImageButton) v.findViewById(R.id.dial_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DialAddActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }
}
