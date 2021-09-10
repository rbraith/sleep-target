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
package com.rbraithwaite.sleeptarget.test_utils.ui.fragment_helpers;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

// https://dagger.dev/hilt/testing
// "Warning:Hilt does not currently support FragmentScenario because there is no way to specify an
// activity class, and Hilt requires a Hilt fragment to be contained in a Hilt activity. One
// workaround
// for this is to launch a Hilt activity and then attach your fragment."
//
// https://stackoverflow.com/a/64620273
// https://github.com/android/architecture-samples/blob/f2fd9ce969a431b20218f3ace38bbb95fd4d1151
// /app/src/androidTest/java/com/example/android/architecture/blueprints/todoapp/HiltExt.kt
// All this does is create a bare-bones hilt activity containing the provided fragment.
// You unfortunately lose the lifecycle-transition capabilities of FragmentScenario. I tried looking
// at the FragmentScenario source, but its a final class so I don't think there's anything I can do.
// This is mainly for testing fragment UI in isolation I guess.



/**
 * This is for testing Hilt fragments in isolation, since currently FragmentScenario doesn't play
 * nice with them.
 */
public class HiltFragmentTestHelper<FragmentType extends Fragment>
        implements FragmentTestHelper<FragmentType>
{
//*********************************************************
// private properties
//*********************************************************

    private ActivityScenario<HiltFragmentActivity> mScenario;
    // These are stored for reference when the app is restarted.
    private Class<FragmentType> mFragmentClass;
    private Bundle mFragmentArgs;

//*********************************************************
// constructors
//*********************************************************

    private HiltFragmentTestHelper(final Class<FragmentType> fragmentClass, final Bundle args)
    {
        mFragmentClass = fragmentClass;
        mFragmentArgs = args;
        initScenario();
    }

//*********************************************************
// overrides
//*********************************************************

    public ActivityScenario<HiltFragmentActivity> getScenario()
    {
        return mScenario;
    }
    
    @Override
    public void performSyncedFragmentAction(FragmentTestHelper.SyncedFragmentAction<FragmentType> syncedFragmentAction)
    {
        TestUtils.performSyncedActivityAction(
                mScenario,
                activity -> {
                    FragmentType fragment = (FragmentType) activity.getFragment();
                    syncedFragmentAction.performOn(fragment);
                }
        );
    }
    
    public void restartFragment()
    {
        // this is what FragmentScenario does so I'm doing it too lol
        mScenario.recreate();
    }
    
    /**
     * Restart the application and start up this fragment with the same args it was created with.
     */
    @Override
    public void restartApp()
    {
        mScenario.close();
        // TODO [21-06-24 2:41AM] -- Should the app be allowed to be restarted with new args?
        //  This would involve updating mArgs.
        initScenario();
    }
    
    @Override
    public void rotateScreenTo(int desiredOrientation)
    {
        TestUtils.rotateActivitySynced(mScenario, desiredOrientation);
    }

//*********************************************************
// api
//*********************************************************

    public static <FragmentType extends Fragment> HiltFragmentTestHelper<FragmentType> launchFragment(
            final Class<FragmentType> fragmentClass)
    {
        return launchFragmentWithArgs(fragmentClass, null);
    }
    
    public static <FragmentType extends Fragment> HiltFragmentTestHelper<FragmentType> launchFragmentWithArgs(
            final Class<FragmentType> fragmentClass,
            final Bundle args)
    {
        return new HiltFragmentTestHelper<>(fragmentClass, args);
    }


//*********************************************************
// private methods
//*********************************************************

    
    /**
     * Create the ActivityScenario then create the fragment in the activity.
     */
    private void initScenario()
    {
        mScenario = ActivityScenario.launch(HiltFragmentActivity.class);
        TestUtils.performSyncedActivityAction(
                mScenario, activity -> setupActivityWithFragment(activity, mFragmentClass,
                                                                 mFragmentArgs));
    }
    
    private void setupActivityWithFragment(
            HiltFragmentActivity activity,
            final Class<FragmentType> fragmentClass,
            final Bundle fragmentArgs)
    {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                fragmentClass.getClassLoader(),
                fragmentClass.getName()
        );
        
        if (fragmentArgs != null) {
            fragment.setArguments(fragmentArgs);
        }
        
        fragmentManager.beginTransaction()
                .add(android.R.id.content, fragment, HiltFragmentActivity.FRAGMENT_TAG)
                .commitNow();
    }
}


