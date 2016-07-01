package cn.caratel.voip.demo;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import cn.caratech.voip.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by wurenhai on 2016/6/28.
 */
@RunWith(AndroidJUnit4.class)
@Ignore
public class DemoActivityTest {

    @Rule
    public ActivityTestRule<DemoActivity> demoActivityRule = new ActivityTestRule(DemoActivity.class);

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void showHelloWorldWhenOpen() {
        onView(withText("hello world")).check(matches(isDisplayed()));
        onView(withId(R.id.hello)).check(matches(withText("hello world")));
    }
}