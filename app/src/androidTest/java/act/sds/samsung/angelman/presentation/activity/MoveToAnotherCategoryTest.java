package act.sds.samsung.angelman.presentation.activity;


import android.support.annotation.NonNull;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.view.View;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import act.sds.samsung.angelman.R;

import static act.sds.samsung.angelman.presentation.activity.TestUtil.childAtPosition;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class MoveToAnotherCategoryTest {

    @Rule
    public ActivityTestRule<CategoryMenuActivity> mActivityTestRule = new ActivityTestRule<>(CategoryMenuActivity.class);

    @Test
    public void moveToAnotherCategoryTest() {

        onView(secondCategoryItemMatcher())
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.category_item_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("놀이")));
        onView(withId(R.id.back_button))
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.logo_angeltalk))
                .check(matches(isDisplayed()));
        onView(fourthCategoryItemMatcher())
                .check(matches(isDisplayed()))
                .perform(click());

        onView(withId(R.id.category_item_title))
                .check(matches(isDisplayed()))
                .check(matches(withText("가고 싶은 곳")));
    }

    @NonNull
    private Matcher<View> fourthCategoryItemMatcher() {
        return allOf(
                withId(R.id.category_item_card),
                childAtPosition(withId(R.id.category_list),3)
        );
    }

    @NonNull
    private Matcher<View> secondCategoryItemMatcher() {
        return allOf(
                withId(R.id.category_item_card),
                childAtPosition(withId(R.id.category_list), 1)
        );
    }
}
