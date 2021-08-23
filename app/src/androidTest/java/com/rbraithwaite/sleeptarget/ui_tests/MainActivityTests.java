/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui_tests;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class MainActivityTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void setBottomNavVisibilityTest()
    {
        ActivityScenario<MainActivity> mainActivityScenario =
                ActivityScenario.launch(MainActivity.class);

        // bottom nav starts visible
        onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));

        // set the bottom nav to not be visible
        mainActivityScenario.onActivity(activity -> activity.setBottomNavVisibility(false));

        // assert that the bottom nav is not visible
        onView(withId(R.id.main_bottomnav)).check(matches(not(isDisplayed())));

        // set bottom nav back to visible
        mainActivityScenario.onActivity(activity -> activity.setBottomNavVisibility(true));

        // assert that bottom nav has gone back to visible
        onView(withId(R.id.main_bottomnav)).check(matches(isDisplayed()));
    }
}
