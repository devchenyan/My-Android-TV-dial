package cn.caratel.voip.data.repository;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.source.ContactsDataSource;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class ContactsRepository extends BaseRepository<Contact, ContactsDataSource> {

    private static ContactsRepository instance = null;

    public static ContactsRepository getInstance(ContactsDataSource remoteDataSource, ContactsDataSource localDataSource) {
        if (instance == null) {
            instance = new ContactsRepository(remoteDataSource, localDataSource);
        }
        return instance;
    }

    public static void releaseInstance() {
        instance = null;
    }

    private ContactsRepository(ContactsDataSource remoteDataSource, ContactsDataSource localDataSource) {
        super(remoteDataSource, localDataSource);
    }

}
