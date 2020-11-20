package com.rbraithwaite.sleepapp.ui.session_edit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;

public class SessionEditFragment
        extends BaseFragment
{
//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionEditFragment";
    
//*********************************************************
// overrides
//*********************************************************

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.session_edit_fragment, container, false);
    }
    
    @Override
    protected boolean getBottomNavVisibility() { return false; }
}
