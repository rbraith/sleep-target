package com.rbraithwaite.sleepapp.ui.utils;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class RecyclerUtils
{
//*********************************************************
// constructors
//*********************************************************

    private RecyclerUtils() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static View inflateLayout(int layoutId, ViewGroup parent)
    {
        return LayoutInflater
                .from(parent.getContext())
                .inflate(layoutId, parent, false);
    }
}
