package com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;


/**
 * Provides utilities related to the currently displayed screen fragment.
 */
public interface FragmentTestHelper<FragmentType extends Fragment>
{
//*********************************************************
// public helpers
//*********************************************************

    interface SyncedFragmentAction<F extends Fragment>
    {
        void performOn(F fragment);
    }
    
//*********************************************************
// abstract
//*********************************************************

    ActivityScenario<? extends AppCompatActivity> getScenario();
    void performSyncedFragmentAction(FragmentTestHelper.SyncedFragmentAction<FragmentType> syncedFragmentAction);
    void restartFragment();
    void restartApp();
    /**
     * Rotate to either landscape or portrait.
     *
     * @param desiredOrientation Uses ActivityInfo.SCREEN_ORIENTATION_...
     */
    void rotateScreen(int desiredOrientation);
}
