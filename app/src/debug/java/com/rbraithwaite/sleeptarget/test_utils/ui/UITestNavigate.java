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

package com.rbraithwaite.sleeptarget.test_utils.ui;

import com.rbraithwaite.sleeptarget.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;



/**
 * Use this class for when the *means* of the navigation is not important.
 */
public class UITestNavigate
{
//*********************************************************
// constructors
//*********************************************************

    private UITestNavigate() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void fromHome_toGoals()
    {
        onView(withId(R.id.nav_sleepgoals)).perform(click());
    }
    
    public static void fromHome_toAddSession()
    {
        fromHome_toSessionArchive();
        fromSessionArchive_toAddSession();
    }
    
    public static void fromHome_toSessionArchive()
    {
        onView(withId(R.id.nav_session_archive)).perform(click());
    }
    
    public static void fromSessionArchive_toAddSession()
    {
        onView(withId(R.id.session_archive_fab)).perform(click());
    }
    
    public static void up()
    {
        onView(withContentDescription(R.string.nav_app_bar_navigate_up_description)).perform(click());
    }
}
