package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.text.TextWatcher;

// REFACTOR [21-07-5 3:24AM] -- move this somewhere more general.


/**
 * A TextWatcher which only cares about the text *after* it has changed.
 */
public abstract class AfterTextChangedWatcher
        implements TextWatcher
{
//*********************************************************
// overrides
//*********************************************************

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
        // do nothing
    }
    
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
        // do nothing
    }
}
