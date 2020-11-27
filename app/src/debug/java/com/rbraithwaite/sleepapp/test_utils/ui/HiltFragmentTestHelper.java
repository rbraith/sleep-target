package com.rbraithwaite.sleepapp.test_utils.ui;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;

import com.rbraithwaite.sleepapp.TestUtils;

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
public class HiltFragmentTestHelper<FragmentType extends Fragment>
{
//*********************************************************
// private properties
//*********************************************************

    private ActivityScenario<HiltFragmentActivity> mScenario;
    
//*********************************************************
// public helpers
//*********************************************************

    public interface SyncedFragmentAction<F extends Fragment>
    {
        public void perform(F fragment);
    }
    
//*********************************************************
// constructors
//*********************************************************

    private HiltFragmentTestHelper(final Class<FragmentType> fragmentClass, final Bundle args)
    {
        mScenario = ActivityScenario.launch(HiltFragmentActivity.class);
        TestUtils.performSyncedActivityAction(
                mScenario, new TestUtils.SyncedActivityAction<HiltFragmentActivity>()
                {
                    @Override
                    public void perform(HiltFragmentActivity activity)
                    {
                        setupActivityWithFragment(activity, fragmentClass, args);
                    }
                });
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
    
    public ActivityScenario<HiltFragmentActivity> getScenario()
    {
        return mScenario;
    }
    
    public void performSyncedFragmentAction(final SyncedFragmentAction<FragmentType> syncedFragmentAction)
    {
        TestUtils.performSyncedActivityAction(
                mScenario,
                new TestUtils.SyncedActivityAction<HiltFragmentActivity>()
                {
                    @Override
                    public void perform(HiltFragmentActivity activity)
                    {
                        FragmentType fragment = (FragmentType) activity.getFragment();
                        syncedFragmentAction.perform(fragment);
                    }
                }
        );
    }
    
//*********************************************************
// private methods
//*********************************************************

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


