package cn.caratel.voip.data.source.local;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import cn.caratel.voip.data.model.Contact;
import cn.caratel.voip.data.model.Option;
import cn.caratel.voip.data.source.DataSource;

import static org.junit.Assert.*;

/**
 * Created by wurenhai on 2016/6/30.
 */
@RunWith(AndroidJUnit4.class)
public class OptionsLoaclDataSourceTest {

    private static final String KEY = "my.voip_number";
    private static final String KEY2 = "my.client_id";
    private static final String VALUE = "500123456";
    private static final String VALUE2 = "sn123456789";

    private OptionsLoaclDataSource localDataSource;

    @Before
    public void setUp() throws Exception {
        localDataSource = OptionsLoaclDataSource.getInstance(InstrumentationRegistry.getTargetContext());
    }

    @After
    public void tearDown() throws Exception {
        localDataSource.deleteAll();
    }

    @Test
    public void testPreConditions() {
        assertNotNull(localDataSource);
    }

    @Test
    public void all_firstSaveThenQuery() {
        final Option option = new Option(KEY, VALUE);
        localDataSource.save(option);
        final Option option2 = new Option(KEY2, VALUE2);
        localDataSource.save(option2);

        localDataSource.all(new DataSource.LoadDataCallback<Option>() {
            @Override
            public void onDataLoaded(List<Option> data) {
                assertNotNull(data);
                assertTrue(data.size() >= 2);

                boolean item1Found = false;
                boolean item2Found = false;
                for (Option item: data) {
                    if (item.equals(option)) {
                        item1Found = true;
                    }
                    if (item.equals(option2)) {
                        item2Found = true;
                    }
                }
                assertTrue(item1Found);
                assertTrue(item2Found);
            }

            @Override
            public void onDataNotAvailable() {
                fail("callback error");
            }
        });
    }

    @Test
    public void all_noData() {
        localDataSource.all(new DataSource.LoadDataCallback<Option>() {
            @Override
            public void onDataLoaded(List<Option> data) {
                fail("callback error");
            }

            @Override
            public void onDataNotAvailable() {
                assertTrue(true);
            }
        });
    }

    @Test
    public void find_dataNotExist() {
        localDataSource.find(KEY, new DataSource.GetItemCallback<Option>() {
            @Override
            public void onItemLoaded(Option item) {
                fail("callback error");
            }

            @Override
            public void onItemNotAvailable() {
                assertTrue(true);
            }
        });
    }

    @Test
    public void find_firstSaveThenQuery() {
        final Option option = new Option(KEY, VALUE);
        localDataSource.save(option);
        final Option option2 = new Option(KEY2, VALUE2);
        localDataSource.save(option2);

        localDataSource.find(KEY, new DataSource.GetItemCallback<Option>() {
            @Override
            public void onItemLoaded(Option item) {
                assertEquals(option, item);
            }

            @Override
            public void onItemNotAvailable() {
                fail("callback error");
            }
        });

        localDataSource.find(KEY2, new DataSource.GetItemCallback<Option>() {
            @Override
            public void onItemLoaded(Option item) {
                assertEquals(option2, item);
            }

            @Override
            public void onItemNotAvailable() {
                fail("callback error");
            }
        });
    }

    @Test
    public void delete_firstSaveThenDeleteThenQuery() {
        final Option option = new Option(KEY, VALUE);
        localDataSource.save(option);
        final Option option2 = new Option(KEY2, VALUE2);
        localDataSource.save(option2);

        localDataSource.delete(option.idValue());

        localDataSource.find(KEY, new DataSource.GetItemCallback<Option>() {
            @Override
            public void onItemLoaded(Option item) {
                fail("callback error");
            }

            @Override
            public void onItemNotAvailable() {
                assertTrue(true);
            }
        });
    }

    @Test
    public void delete_deleteNotExisted() {
        localDataSource.delete(KEY);
        localDataSource.find(KEY, new DataSource.GetItemCallback<Option>() {
            @Override
            public void onItemLoaded(Option item) {
                fail("callback error");
            }

            @Override
            public void onItemNotAvailable() {
                assertTrue(true);
            }
        });
    }

    @Test
    public void deleteAll_firstSaveThenDeleteThenQuery() {
        final Option option = new Option(KEY, VALUE);
        localDataSource.save(option);
        final Option option2 = new Option(KEY2, VALUE2);
        localDataSource.save(option2);

        localDataSource.deleteAll();

        localDataSource.all(new DataSource.LoadDataCallback<Option>() {
            @Override
            public void onDataLoaded(List<Option> data) {
                fail("callback error");
            }

            @Override
            public void onDataNotAvailable() {
                assertTrue(true);
            }
        });
    }
}