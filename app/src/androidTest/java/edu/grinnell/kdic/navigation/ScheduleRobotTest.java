package edu.grinnell.kdic.navigation;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import edu.grinnell.kdic.Constants;
import edu.grinnell.kdic.MainActivity;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ScheduleRobotTest {

    @Rule public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void checkItemsNoMock() {
        ScheduleRobot scheduleRobot = new ScheduleRobot();
        scheduleRobot
                .checkHeaders()
                .clickDay(Constants.DAYS_OF_WEEK[0]);
    }
}