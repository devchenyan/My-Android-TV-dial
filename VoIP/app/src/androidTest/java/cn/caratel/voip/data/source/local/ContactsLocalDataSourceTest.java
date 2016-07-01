package cn.caratel.voip.data.source.local;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.source.DataSource;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by wurenhai on 2016/6/28.
 */
@RunWith(AndroidJUnit4.class)
public class ContactsLocalDataSourceTest {

    private static final String VOIP_NUMBER = "500123456";
    private static final String VOIP_NUMBER2 = "5001236789";
    private static final String CLIENT_ID = "18900731234";
    private static final String CLIENT_ID2 = "18900735678";

    private ContactsLocalDataSource localDataSource;

    @Before
    public void setUp() {
        localDataSource = ContactsLocalDataSource.getInstance(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void tearDown() {
        localDataSource.deleteAll();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(localDataSource);
    }

    @Test
    public void saveContact_retrievesContact() {
        final Contact contact = new Contact(VOIP_NUMBER, CLIENT_ID, "");

        localDataSource.save(contact);

        localDataSource.find(contact.getVoipNumber(), new DataSource.GetItemCallback<Contact>() {
            @Override
            public void onItemLoaded(Contact item) {
                assertThat(contact, is(item));
            }

            @Override
            public void onItemNotAvailable() {
                fail("callback error");
            }
        });
    }

    @Test
    public void deleteAllContacts_emptyListOfRetrievesContacts() {
        Contact contact = new Contact(VOIP_NUMBER, CLIENT_ID, "");
        localDataSource.save(contact);

        DataSource.LoadDataCallback<Contact> callback = mock(DataSource.LoadDataCallback.class);

        localDataSource.deleteAll();

        localDataSource.all(callback);

        verify(callback).onDataNotAvailable();
        verify(callback, never()).onDataLoaded(anyList());
    }

    @Test
    public void getContacts_retrieveSavedContacts() {
        final Contact contact1 = new Contact(VOIP_NUMBER, CLIENT_ID, "");
        localDataSource.save(contact1);
        final Contact contact2 = new Contact(VOIP_NUMBER2, CLIENT_ID, "");
        localDataSource.save(contact2);

        localDataSource.all(new DataSource.LoadDataCallback<Contact>() {
            @Override
            public void onDataLoaded(List<Contact> data) {
                assertNotNull(data);
                assertTrue(data.size() >= 2);

                boolean contact1IdFound = false;
                boolean contact2IdFound = false;
                for (Contact item: data) {
                    if (item.getVoipNumber().equals(contact1.getVoipNumber())) {
                        contact1IdFound = true;
                    }
                    if (item.getVoipNumber().equals(contact2.getVoipNumber())) {
                        contact2IdFound = true;
                    }
                }
                assertTrue(contact1IdFound);
                assertTrue(contact2IdFound);
            }

            @Override
            public void onDataNotAvailable() {
                fail("callback error");
            }
        });
    }

    @Test
    public void updateContact_retrieveUpdatedContact() {
        final Contact contact = new Contact(VOIP_NUMBER, CLIENT_ID, "");
        localDataSource.save(contact);

        contact.setNickname("hello world");
        localDataSource.update(contact);

        localDataSource.find(VOIP_NUMBER, new DataSource.GetItemCallback<Contact>() {
            @Override
            public void onItemLoaded(Contact item) {
                assertTrue(contact.equals(item));
            }

            @Override
            public void onItemNotAvailable() {
                fail("callback error");
            }
        });
    }

}