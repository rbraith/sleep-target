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

package com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import dagger.hilt.android.AndroidEntryPoint;

// HACK [20-11-22 9:29PM] -- I don't like this being public and in a separate module,
//  but it seems like Hilt won't build without it being public?
// this is outside of HiltFragmentTestHelper, instead of being something like
// a static nested class, since there is a bug right now in Hilt where it doesn't
// like generic classes
// see:
// https://github.com/google/dagger/issues/2140
// https://github.com/google/dagger/issues/2042
// https://stackoverflow.com/questions/62909138/dagger-hilt-abstract-class-with-types
@AndroidEntryPoint
public class HiltFragmentActivity
        extends AppCompatActivity
{
    public static final String FRAGMENT_TAG = "hilt fragment";
    
    public Fragment getFragment()
    {
        return getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }
}
