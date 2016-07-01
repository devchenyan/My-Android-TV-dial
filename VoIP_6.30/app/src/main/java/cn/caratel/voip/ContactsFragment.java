package cn.caratel.voip;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.caratech.voip.R;
import cn.caratel.voip.cells.ContactCell;

/**
 * Created by chenyan on 6/28/16.
 */
public class ContactsFragment extends Fragment {

    private ContactCell addCell;
    private ContactCell infoCell;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        addCell = (ContactCell) v.findViewById(R.id.contact_cell_add);
        addCell.setOnContactCellClickListener(new ContactCell.OnContactCellClickListener() {
            @Override
            public void onImageClick() {
                Intent intent = new Intent(getActivity(), ContactAddActivity.class);
                startActivity(intent);
            }
        });

        infoCell = (ContactCell) v.findViewById(R.id.contact_cell_info);
        infoCell.setOnContactCellClickListener(new ContactCell.OnContactCellClickListener() {
            @Override
            public void onImageClick() {
                Intent intent = new Intent(getActivity(), ContactInfoActivity.class);
                startActivity(intent);
            }
        });

        return v;
    }
}
