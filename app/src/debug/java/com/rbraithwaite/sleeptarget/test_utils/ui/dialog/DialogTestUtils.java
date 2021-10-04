/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.test_utils.ui.dialog;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class DialogTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private DialogTestUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static void pressPositiveButton()
    {
        // button1 is dialog positive btn
        onView(withId(android.R.id.button1)).inRoot(isDialog()).perform(click());
    }
    
    public static void pressNegativeButton()
    {
        onView(withId(android.R.id.button2)).inRoot(isDialog()).perform(click());
    }
}
