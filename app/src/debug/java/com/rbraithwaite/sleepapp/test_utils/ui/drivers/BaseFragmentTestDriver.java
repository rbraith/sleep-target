package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.BaseFragment;
import com.rbraithwaite.sleepapp.utils.CommonUtils;

import org.hamcrest.Matcher;

public class BaseFragmentTestDriver<FragmentType extends BaseFragment<?>,
        AssertionsType extends BaseFragmentTestDriver.BaseAssertions<?, ?>>
{
//*********************************************************
// private properties
//*********************************************************

    private FragmentTestHelper<FragmentType> mHelper;
    private AssertionsType mAssertions;

//*********************************************************
// public helpers
//*********************************************************

    public static class BaseAssertions<FragmentTestDriverType extends BaseFragmentTestDriver<?,
            ?>, ViewModelType extends ViewModel>
    {
        private FragmentTestDriverType mOwningDriver;
        private ViewModelType mViewModel;
        
        public BaseAssertions(FragmentTestDriverType owningDriver)
        {
            mOwningDriver = owningDriver;
        }
        
        protected FragmentTestDriverType getOwningDriver()
        {
            return mOwningDriver;
        }
        
        protected ViewModelType getViewModel()
        {
            mViewModel = CommonUtils.lazyInit(mViewModel, () -> {
                TestUtils.DoubleRef<ViewModelType> viewModel = new TestUtils.DoubleRef<>(null);
                mOwningDriver.getHelper()
                        .performSyncedFragmentAction(fragment -> viewModel.ref =
                                (ViewModelType) fragment.getViewModel());
                return viewModel.ref;
            });
            return mViewModel;
        }
    }
    
    // HACK [21-06-25 12:35AM] -- This is to avoid an instantiation error where BaseAssertions
    //  depends on a BaseFragmentTestDriver but BaseFragmentTestDriver depends on a BaseAssertions.

//*********************************************************
// api
//*********************************************************

    public FragmentTestHelper<FragmentType> getHelper()
    {
        return mHelper;
    }
    
    public AssertionsType assertThat()
    {
        return mAssertions;
    }
    
    public void restartFragment()
    {
        mHelper.restartFragment();
    }
    
    public void restartApp()
    {
        mHelper.restartApp();
    }
    
    /**
     * Rotate to either landscape or portrait.
     *
     * @param desiredOrientation Uses ActivityInfo.SCREEN_ORIENTATION_...
     */
    public void rotateScreen(int desiredOrientation)
    {
        mHelper.rotateScreen(desiredOrientation);
    }
    
    // IDEA [21-06-25 12:11AM] -- restartFragmentWithArgs(args).

//*********************************************************
// protected api
//*********************************************************

    // HACK [21-06-25 12:50AM] -- This is because I wanted to use assertThat publicly, but the
    //  compiler gets confused when I try to use hamcrest's assertThat internally (and I don't want
    //  to need to fully qualify it every time).
    protected static <T> void hamcrestAssertThat(T actual, Matcher<? super T> matcher)
    {
        org.hamcrest.MatcherAssert.assertThat(actual, matcher);
    }
    
    /**
     * Classes deriving from BaseFragmentTestDriver are *required* to call this in their
     * constructor.
     */
    protected void init(FragmentTestHelper<FragmentType> helper, AssertionsType assertions)
    {
        mHelper = helper;
        mAssertions = assertions;
    }
}
