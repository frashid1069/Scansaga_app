package com.example.scansaga;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

import com.example.scansaga.Views.ShowAllEvents;

/**
 * Espresso tests for {@link ShowAllEvents} activity to verify event list display
 * and deletion functionality.
 */
@RunWith(AndroidJUnit4.class)
public class ShowAllEventsTest {

    @Rule
    public ActivityScenarioRule<ShowAllEvents> activityScenarioRule =
            new ActivityScenarioRule<>(ShowAllEvents.class);

    /**
     * Verifies that the ListView displaying events is indeed visible to the user.
     */
    @Test
    public void testListViewDisplaysEvents() {
        // Check if the ListView is displayed
        Espresso.onView(withId(R.id.listView)).check(matches(isDisplayed()));
    }

    /**
     * Tests the delete functionality by attempting to click a delete button
     * associated with an event. This test assumes the presence of a delete button
     * within the ListView and may need to be adjusted for actual implementation details.
     */
    @Test
    public void testDeleteButtonDeletesEvent() {
        // Click the delete button for the first event in the list
        // Note: This action assumes that there is a delete button with R.id.button_delete visible
        // in the list. Depending on the actual layout, this might require targeting the button within a specific item.
        Espresso.onView(withId(R.id.button_delete)).perform(ViewActions.click());


    }
    @Test
    public void clickSignupButton_ShowsAlertDialog() {

        Espresso.onData(anything()).inAdapterView(withId(R.id.listView)).atPosition(0).perform(click());

        // Now click the signup button
        Espresso.onView(withId(R.id.signup_button)).perform(click());

        // This checks for the presence of the OK button as a simple verification
        Espresso.onView(withText("OK")).check(matches(isDisplayed()));
    }
}
