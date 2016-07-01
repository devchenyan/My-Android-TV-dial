package cn.caratel.voip;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.caratech.voip.R;
import cn.caratel.voip.cells.ContactsAdapter;
import cn.caratel.voip.cells.model.ContactModel;

/**
 * Created by chenyan on 6/28/16.
 */

public class ContactsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ContactsAdapter mContactsAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_contacts, container, false);

        recyclerView = (RecyclerView) v.findViewById(R.id.contacts_recycler_view);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());

        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
//        linearLayoutManager.setOrientation(OrientationHelper.HORIZONTAL);

        recyclerView.setLayoutManager(linearLayoutManager);


        updateUI();

        return v;
    }

    private void updateUI() {
        List<ContactModel> contactList = new ArrayList<>();
        ContactModel fcm = new ContactModel();
        fcm.setName("添加联系人");
        contactList.add(fcm);

        for (int i = 1; i <= 20; i++) {
            ContactModel cm = new ContactModel();
            cm.setName("name #" + i);
            contactList.add(cm);
        }

        mContactsAdapter = new ContactsAdapter(getActivity(), contactList);
        mContactsAdapter.setOnItemClickListener(new ContactsAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent;
                if (position == 0) {
                    intent = new Intent(getActivity(), ContactAddActivity.class);
                } else {
                    // TODO: 带有 联系人的信息 跳转
                    intent = new Intent(getActivity(), ContactInfoActivity.class);
                }
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(mContactsAdapter);
    }
}