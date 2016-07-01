package cn.caratel.voip.data.source.local;

import android.content.Context;
import android.support.annotation.NonNull;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.source.ContactsDataSource;
import cn.caratel.voip.data.source.local.entity.ContactEntity;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class ContactsLocalDataSource extends LocalDataSource<Contact, ContactEntity> implements ContactsDataSource {

    private static ContactsLocalDataSource instance = null;

    public static ContactsLocalDataSource getInstance(Context context) {
        if (instance == null) {
            instance = new ContactsLocalDataSource(context);
        }
        return instance;
    }

    public ContactsLocalDataSource(@NonNull Context context) {
        super(context, new ContactEntity());
    }

}
