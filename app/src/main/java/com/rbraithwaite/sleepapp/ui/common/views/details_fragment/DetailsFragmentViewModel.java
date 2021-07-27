package com.rbraithwaite.sleepapp.ui.common.views.details_fragment;

import androidx.lifecycle.ViewModel;

public abstract class DetailsFragmentViewModel<DataType>
        extends ViewModel
{
//*********************************************************
// abstract
//*********************************************************

    public abstract DataType getResult();
    
    /**
     * Set the data only if the view model is clear (ie no data has been set yet, or clearData was
     * called).
     */
    public abstract void initData(DataType data);
    
    public abstract void clearData();
}
