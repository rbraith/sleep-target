package com.rbraithwaite.sleepapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.test.core.app.ActivityScenario;

import dagger.hilt.android.AndroidEntryPoint;

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
@AndroidEntryPoint
public class HiltFragmentScenarioWorkaround
        extends AppCompatActivity
{
//*********************************************************
// private constants
//*********************************************************

    private static final String FRAGMENT_TAG = "hilt fragment";
    
//*********************************************************
// api
//*********************************************************

    public static <FragmentType extends Fragment> ActivityScenario<HiltFragmentScenarioWorkaround> launchFragmentInHiltContainer(
            final Class<FragmentType> fragmentClass)
    {
        ActivityScenario<HiltFragmentScenarioWorkaround> scenario =
                ActivityScenario.launch(HiltFragmentScenarioWorkaround.class);
        
        scenario.onActivity(new ActivityScenario.ActivityAction<HiltFragmentScenarioWorkaround>()
        {
            @Override
            public void perform(HiltFragmentScenarioWorkaround activity)
            {
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                Fragment fragment = fragmentManager.getFragmentFactory().instantiate(
                        fragmentClass.getClassLoader(),
                        fragmentClass.getName()
                );
                
                fragmentManager.beginTransaction()
                        .add(android.R.id.content, fragment, FRAGMENT_TAG)
                        .commitNow();
            }
        });
        
        return scenario;
    }
    
    public Fragment getFragment()
    {
        return getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }
}
