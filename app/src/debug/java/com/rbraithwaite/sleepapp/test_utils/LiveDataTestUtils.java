package com.rbraithwaite.sleepapp.test_utils;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleepapp.utils.interfaces.ProviderOf;

public class LiveDataTestUtils
{
//*********************************************************
// constructors
//*********************************************************

    private LiveDataTestUtils() {/* No instantiation */}
    
    
//*********************************************************
// api
//*********************************************************

    public static <T> LiveData<T> activateLocally(ProviderOf<LiveData<T>> provider)
    {
        LiveData<T> ld = provider.provide();
        // REFACTOR [21-07-31 3:04AM] -- move activateLocalLiveData and all other LiveData-related
        //  test utils into here.
        TestUtils.activateLocalLiveData(ld);
        return ld;
    }
}
