package cn.caratel.voip.data.source.remote.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import cn.caratel.voip.data.model.Contact;
import retrofit2.Call;
import retrofit2.Response;

import static org.junit.Assert.*;

/**
 * Created by wurenhai on 2016/6/28.
 */
public class ContactServiceTest {

    private static final String baseUrl = "http://127.0.0.1:8080";

    private ContactService contactService;

    @Before
    public void setUp() {
        contactService = ServiceProvider.getInstance(baseUrl).provideContactService();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testPreConditions() {
        assertNotNull(contactService);
    }

    @Test
    public void getAllFromServer() throws IOException {
        Call<List<Contact>> call = contactService.all();
        Response<List<Contact>> response = call.execute();
        assertTrue(response.isSuccessful());
        assertTrue(response.body().size() == 0);
    }

}