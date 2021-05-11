package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import android.content.Context;
import android.view.View;

import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;

public interface MoodViewFactory
{
//*********************************************************
// abstract
//*********************************************************

    View createView(MoodUiData mood, Context context, float scale);
}
