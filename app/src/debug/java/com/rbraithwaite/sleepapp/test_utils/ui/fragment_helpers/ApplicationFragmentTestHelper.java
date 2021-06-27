package com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.test.core.app.ActivityScenario;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.MainActivity;



/**
 * Used for fragments existing in the full application.
 */
public class ApplicationFragmentTestHelper<FragmentType extends Fragment>
        implements FragmentTestHelper<FragmentType>
{
//*********************************************************
// private properties
//*********************************************************

    private ScenarioCallbacks mScenarioCallbacks;
    
//*********************************************************
// public helpers
//*********************************************************

    public interface ScenarioCallbacks
    {
        ActivityScenario<MainActivity> getScenario();
        /**
         * This is used when the app is restarted. This should create a new ActivityScenario
         * instance, and after this has been called {@link #getScenario()} should return the new
         * scenario instance.
         */
        void recreateScenario();
    }

//*********************************************************
// constructors
//*********************************************************

    public ApplicationFragmentTestHelper(ScenarioCallbacks scenarioCallbacks)
    {
        mScenarioCallbacks = scenarioCallbacks;
    }

//*********************************************************
// overrides
//*********************************************************

    @Override
    public ActivityScenario<? extends AppCompatActivity> getScenario()
    {
        return mScenarioCallbacks.getScenario();
    }
    
    @Override
    public void performSyncedFragmentAction(FragmentTestHelper.SyncedFragmentAction<FragmentType> syncedFragmentAction)
    {
        // When in the full application, the navigation component is in charge of the fragments and
        // you need to interact with it in order to access the displayed fragment.
        TestUtils.performSyncedActivityAction(
                mScenarioCallbacks.getScenario(), activity -> {
                    // TODO [21-05-11 2:33AM] -- there should be an instanceof check here on the
                    //  fragment type.
                    // TODO [21-05-11 3:04AM] -- Is getPrimaryNavigationFragment() supposed to be
                    //  returning the NavHostFragment?
                    //      https://issuetracker.google.com/issues/119800853
                    //  It seems like your supposed to be able to use it access the current
                    //  fragment?
                    FragmentType fragment =
                            (FragmentType) activity
                                    .getSupportFragmentManager()
                                    .getPrimaryNavigationFragment()
                                    .getChildFragmentManager()
                                    .getFragments()
                                    .get(0);
                    syncedFragmentAction.performOn(fragment);
                });
    }
    
    @Override
    public void restartFragment()
    {
        mScenarioCallbacks.getScenario().recreate();
    }
    
    @Override
    public void restartApp()
    {
        mScenarioCallbacks.getScenario().close();
        mScenarioCallbacks.recreateScenario();
    }
    
    @Override
    public void rotateScreen(int desiredOrientation)
    {
        TestUtils.rotateActivitySynced(mScenarioCallbacks.getScenario(), desiredOrientation);
    }
}
