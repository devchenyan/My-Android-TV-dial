package cn.caratel.voip.data.source.remote;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.source.ContactsDataSource;

/**
 * Created by wurenhai on 2016/6/29.
 */
public class FakeContactsDataSource implements ContactsDataSource {

    Map<String, Contact> contacts = new LinkedHashMap<>();

    @Override
    public void all(@NonNull LoadDataCallback<Contact> callback) {
        if (contacts.isEmpty()) {
            callback.onDataNotAvailable();
        } else {
            callback.onDataLoaded(new ArrayList<>(contacts.values()));
        }
    }

    @Override
    public void find(@NonNull String id, @NonNull GetItemCallback<Contact> callback) {
        if (contacts.containsKey(id)) {
            callback.onItemLoaded(contacts.get(id));
        } else {
            callback.onItemNotAvailable();
        }
    }

    @Override
    public void save(@NonNull Contact model) {
        contacts.put(model.idValue(), model);
    }

    @Override
    public void insert(@NonNull Contact model) {
        contacts.put(model.idValue(), model);
    }

    @Override
    public void update(@NonNull Contact model) {
        contacts.put(model.idValue(), model);
    }

    @Override
    public void delete(@NonNull String id) {
        contacts.remove(id);
    }

    @Override
    public void deleteAll() {
        contacts.clear();
    }

    @Override
    public void refresh() {

    }

}
