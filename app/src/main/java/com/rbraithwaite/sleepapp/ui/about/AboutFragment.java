package com.rbraithwaite.sleepapp.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.ui.BaseFragment;

public class AboutFragment
        extends BaseFragment<AboutViewModel>
{
//*********************************************************
// overrides
//*********************************************************

    @Override
    protected Properties<AboutViewModel> initProperties()
    {
        return new Properties<>(false, AboutViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.about_fragment, container, false);
    }
}
