package com.rbraithwaite.sleepapp.ui.interruption_details;

import androidx.lifecycle.MutableLiveData;

import com.rbraithwaite.sleepapp.core.models.Interruption;
import com.rbraithwaite.sleepapp.ui.common.views.details_fragment.DetailsFragmentViewModel;

public class InterruptionDetailsFragmentViewModel
        extends DetailsFragmentViewModel<InterruptionWrapper>
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<Interruption> mInterruption = new MutableLiveData<>();
    private boolean mInitialized = false;
    
//*********************************************************
// overrides
//*********************************************************

    // TEST NEEDED [21-07-27 12:22AM] -- .
    @Override
    public InterruptionWrapper getResult()
    {
        return new InterruptionWrapper(mInterruption.getValue());
    }
    
    // TEST NEEDED [21-07-27 12:22AM] -- .
    // REFACTOR [21-07-23 5:44PM] -- the logic in these overrides duplicates
    //  SessionDetailsFragmentViewModel - should I move this logic to DetailsFragmentViewModel
    //  somehow?
    @Override
    public void initData(InterruptionWrapper data)
    {
        if (!mInitialized) {
            mInterruption.setValue(data.getData());
            mInitialized = true;
        }
    }
    
    // TEST NEEDED [21-07-27 12:22AM] -- .
    @Override
    public void clearData()
    {
        mInterruption.setValue(null);
        mInitialized = false;
    }
}
