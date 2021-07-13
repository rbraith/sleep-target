package com.rbraithwaite.sleepapp.ui.sleep_tracker;

import android.content.Context;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import com.rbraithwaite.sleepapp.R;

public class SleepTrackerAnimations
{
//*********************************************************
// private properties
//*********************************************************

    private Context mContext;
    private ConstraintLayout mSleepTrackingLayout;
    private ConstraintLayout mInterruptionsLayout;

//*********************************************************
// constructors
//*********************************************************

    public SleepTrackerAnimations(Context context, View fragmentRoot)
    {
        mContext = context;
        mSleepTrackingLayout = fragmentRoot.findViewById(R.id.tracker_sleep_tracking_layout);
        mInterruptionsLayout = fragmentRoot.findViewById(R.id.tracker_interruptions_layout);
    }

//*********************************************************
// api
//*********************************************************

    public void transitionIntoTrackingSession()
    {
        transitionLayoutToKeyframe(mSleepTrackingLayout,
                                   R.layout.tracker_content_tracking_in_session);
    }
    
    public void transitionOutOfTrackingSession()
    {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.load(mContext, R.layout.tracker_content_tracking);
        // Currently no animation here, as this transition gets hidden beneath the
        // post-sleep dialog
//        TransitionManager.beginDelayedTransition(mSleepTrackingLayout);
        constraintSet.applyTo(mSleepTrackingLayout);
    }
    
    public void transitionIntoInterruptionTimer()
    {
        transitionLayoutToKeyframe(mInterruptionsLayout,
                                   R.layout.tracker_interruptions_interrupted);
    }
    
    public void transitionOutOfInterruptionTimer()
    {
        transitionLayoutToKeyframe(mInterruptionsLayout, R.layout.tracker_interruptions);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void transitionLayoutToKeyframe(ConstraintLayout layout, int keyframeId)
    {
        // TODO [21-07-4 1:56AM] -- This is ok for now, but later on switch over to MotionLayout for
        //  more animation control (eg speed, colour, visibility, etc)
        //  https://developer.android.com/training/constraint-layout/motionlayout
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.load(mContext, keyframeId);
        TransitionManager.beginDelayedTransition(layout);
        constraintSet.applyTo(layout);
    }
}
