package com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;

/**
 * Provides utilities related to the currently displayed screen fragment.
 */
public interface FragmentTestHelper<FragmentType extends Fragment>
{
    interface SyncedFragmentAction<F extends Fragment>
    {
        void performOn(F fragment);
    }
    
    ActivityScenario<? extends AppCompatActivity> getScenario();
    
    public void performSyncedFragmentAction(FragmentTestHelper.SyncedFragmentAction<FragmentType> syncedFragmentAction);
    
    public void restartFragment();
}
