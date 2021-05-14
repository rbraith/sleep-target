package com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers;

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
