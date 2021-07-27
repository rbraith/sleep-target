package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.dialog.DialogTestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.ApplicationFragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionDetailsFragment;
import com.rbraithwaite.sleepapp.ui.interruption_details.InterruptionDetailsFragmentViewModel;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

// REFACTOR [21-07-27 1:56AM] -- a lot of this duplicates SessionDetailsTestDriver.
public class InterruptionDetailsTestDriver
        extends BaseFragmentTestDriver<InterruptionDetailsFragment,
        InterruptionDetailsTestDriver.Assertions>
{
//*********************************************************
// private properties
//*********************************************************

    private OnNegativeActionListener mOnNegativeActionListener;
    
//*********************************************************
// public helpers
//*********************************************************

    public interface OnNegativeActionListener
    {
        void onNegativeAction();
    }
    
    public static class Assertions
            extends BaseFragmentTestDriver.BaseAssertions<InterruptionDetailsTestDriver,
            InterruptionDetailsFragmentViewModel>
    {
        public Assertions(InterruptionDetailsTestDriver owningDriver)
        {
            super(owningDriver);
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    private InterruptionDetailsTestDriver() {}
    
//*********************************************************
// api
//*********************************************************

    public static InterruptionDetailsTestDriver inApplication(ApplicationFragmentTestHelper<InterruptionDetailsFragment> helper)
    {
        InterruptionDetailsTestDriver driver = new InterruptionDetailsTestDriver();
        driver.init(helper, new Assertions(driver));
        return driver;
    }
    
    public void setOnNegativeActionListener(OnNegativeActionListener onNegativeActionListener)
    {
        mOnNegativeActionListener = onNegativeActionListener;
    }
    
    /**
     * Don't call this unless you're in an {@link ApplicationTestDriver}. This assumes that this
     * InterruptionDetailsFragment was accessed in an edit-mode from the session details.
     */
    public void deleteInterruption()
    {
        if (mOnNegativeActionListener != null) {
            mOnNegativeActionListener.onNegativeAction();
        }
        pressNegativeButton();
        DialogTestUtils.pressPositiveButton();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void pressNegativeButton()
    {
        onView(withId(R.id.action_negative)).perform(click());
    }
}
