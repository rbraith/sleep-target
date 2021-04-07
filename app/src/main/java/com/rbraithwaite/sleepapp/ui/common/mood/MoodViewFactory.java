package com.rbraithwaite.sleepapp.ui.common.mood;

import android.content.Context;
import android.view.View;

public interface MoodViewFactory
{
//*********************************************************
// abstract
//*********************************************************

    View createView(MoodUiData mood, Context context, float scale);
}
