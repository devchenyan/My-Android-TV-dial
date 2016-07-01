package cn.caratel.voip.data.repository;

import android.content.Context;

import com.google.common.collect.Lists;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.source.ContactsDataSource;
import cn.caratel.voip.data.source.DataSource;
import cn.caratel.voip.data.source.remote.FakeContactsDataSource;
import retrofit2.http.GET;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by wurenhai on 2016/6/29.
 */
public class ContactsRepositoryTest {

    //FakeContactsDataSource remoteDateSource;

    //FakeContactsDataSource localDataSource;

    @Mock
    ContactsDataSource remoteDateSource;

    @Mock
    ContactsDataSource localDataSource;

    @Mock
    DataSource.LoadDataCallback<Contact> loadDataCallback;
    @Mock
    DataSource.GetItemCallback<Contact> getItemCallback;

    @Captor
    ArgumentCaptor<DataSource.LoadDataCallback<Contact>> dataCallbackCaptor;

    @Captor
    ArgumentCaptor<DataSource.GetItemCallback<Contact>> itemCallbackCaptor;

    private ContactsRepository contactsRepository;

    private static final String VOIP_NUMBER = "500123456";
    private static final String VOIP_NUMBER2 = "5001236789";
    private static final String PHONE_NUMBER = "18900731234";
    private static final String PHONE_NUMBER2 = "18900735678";

    private static List<Contact> CONTACTS = Lists.newArrayList(
            new Contact(VOIP_NUMBER, PHONE_NUMBER, "", ""),
            new Contact(VOIP_NUMBER2, PHONE_NUMBER2, "", "")
    );


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        contactsRepository = ContactsRepository.getInstance(remoteDateSource, localDataSource);
    }

    @After
    public void tearDown() throws Exception {
        //必须要释放, 不然下个用例仍然会使用上次的实例导致测试验证不通过
        ContactsRepository.releaseInstance();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(contactsRepository);
    }

    @Test
    public void all_repositoryCachesAfterFirstApiCall() {
        // Given a setup Captor to capture callbacks
        // When two calls are issued to the repository
        twoLoadCallsToRepository(loadDataCallback);

        // Then only requested once from Service API
        verify(remoteDateSource).all(any(DataSource.LoadDataCallback.class));
    }

    @Test
    public void all_requestsAllFromLocalDataSource() {
        // When requested from the repository
        contactsRepository.all(loadDataCallback);

        // Then loaded from the local data source
        verify(localDataSource).all(any(DataSource.LoadDataCallback.class));
    }

    @Test
    public void all_withDirty_retrievedFromRemote(){
        // When calling all in the repository with dirty cache
        contactsRepository.refresh();
        contactsRepository.all(loadDataCallback);

        // And the remote data source has data available
        setDataAvailable(remoteDateSource, CONTACTS);

        // Verify the data from the remote data source are returned, not the local
        verify(localDataSource, never()).all(loadDataCallback);
        verify(loadDataCallback).onDataLoaded(CONTACTS);
    }

    @Test
    public void all_withLocalUnavailable_retrievedFromRemote(){
        contactsRepository.all(loadDataCallback);

        setDataNotAvailable(localDataSource);

        setDataAvailable(remoteDateSource, CONTACTS);

        verify(loadDataCallback).onDataLoaded(CONTACTS);
    }

    @Test
    public void all_withBothUnavailable_firesOnDataUnavailable(){
        contactsRepository.all(loadDataCallback);

        setDataNotAvailable(localDataSource);

        setDataNotAvailable(remoteDateSource);

        verify(loadDataCallback).onDataNotAvailable();
    }

    @Test
    public void all_refreshesLocalDataSource() {
        contactsRepository.refresh();

        contactsRepository.all(loadDataCallback);

        setDataAvailable(remoteDateSource, CONTACTS);

        verify(localDataSource, times(CONTACTS.size())).save(any(Contact.class));
    }

    @Test
    public void find_requestsSingleFromLocalDataSource() {
        // When requested from the repository
        contactsRepository.find(VOIP_NUMBER, getItemCallback);

        // Then loaded from the database
        verify(localDataSource).find(eq(VOIP_NUMBER), any(DataSource.GetItemCallback.class));
    }

    @Test
    public void save_saveToServerApi() {
        Contact contact = new Contact("hello", "world", "", "");

        contactsRepository.save(contact);

        verify(remoteDateSource).save(contact);
        verify(localDataSource).save(contact);
        assertThat(contactsRepository.cached.size(), is(1));
    }

    @Test
    public void deleteAll_deleteAllToServiceApiUpdatesCache(){
        Contact contact = new Contact(VOIP_NUMBER, PHONE_NUMBER, "", "");
        contactsRepository.save(contact);
        Contact contact2 = new Contact(VOIP_NUMBER2, PHONE_NUMBER2, "", "");
        contactsRepository.save(contact2);

        contactsRepository.deleteAll();

        verify(remoteDateSource).deleteAll();
        verify(localDataSource).deleteAll();

        assertThat(contactsRepository.cached.size(), is(0));
    }

    @Test
    public void delete_deleteToServiceApiRemoveFromCache() {
        Contact contact = new Contact(VOIP_NUMBER, PHONE_NUMBER, "", "");
        contactsRepository.save(contact);
        assertThat(contactsRepository.cached.containsKey(contact.idValue()), is(true));

        contactsRepository.delete(contact.idValue());

        verify(remoteDateSource).delete(contact.idValue());
        verify(localDataSource).delete(contact.idValue());

        assertThat(contactsRepository.cached.containsKey(contact.idValue()), is(false));
    }

    private void twoLoadCallsToRepository(DataSource.LoadDataCallback<Contact> callback) {
        // When tasks are requested from repository
        contactsRepository.all(callback); // First call to API

        // Use the Mockito Captor to capture the callback
        verify(localDataSource).all(dataCallbackCaptor.capture());

        // Local data source doesn't have data yet
        dataCallbackCaptor.getValue().onDataNotAvailable();

        // Verify the remote data source is queried
        verify(remoteDateSource).all(dataCallbackCaptor.capture());

        // Trigger callback so tasks are cached
        dataCallbackCaptor.getValue().onDataLoaded(CONTACTS);

        contactsRepository.all(callback); // Second call to API
    }

    private void setDataAvailable(ContactsDataSource dataSource, List<Contact> contacts){
        verify(dataSource).all(dataCallbackCaptor.capture());
        dataCallbackCaptor.getValue().onDataLoaded(contacts);
    }

    private void setDataNotAvailable(ContactsDataSource dataSource) {
        verify(dataSource).all(dataCallbackCaptor.capture());
        dataCallbackCaptor.getValue().onDataNotAvailable();
    }

}