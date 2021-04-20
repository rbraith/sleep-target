package com.rbraithwaite.sleepapp.ui;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class UiUtils
{
//*********************************************************
// constructors
//*********************************************************

    private UiUtils() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static void closeSoftKeyboard(View v)
    {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
}
