package cn.caratel.voip.data.source.remote;

import android.support.annotation.NonNull;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.source.DataSource;
import cn.caratel.voip.data.source.remote.service.ContactService;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class ContactsRemoteDataSource implements DataSource<Contact> {

    private ContactService contactService;

    @Override
    public void all(@NonNull LoadDataCallback<Contact> callback) {

    }

    @Override
    public void find(@NonNull String id, @NonNull GetItemCallback<Contact> callback) {

    }

    @Override
    public void save(@NonNull Contact model) {

    }

    @Override
    public void insert(@NonNull Contact model) {

    }

    @Override
    public void update(@NonNull Contact model) {

    }

    @Override
    public void delete(@NonNull String id) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public void refresh() {

    }
}
