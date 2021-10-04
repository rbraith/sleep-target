/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.rbraithwaite.sleeptarget.ui.sleep_tracker;

import android.content.Context;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.TransitionManager;

import com.rbraithwaite.sleeptarget.R;

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
        ConstraintSet set = loadConstraintSet(R.layout.tracker_interruptions_interrupted);
        set.setVisibility(R.id.tracker_interrupt_duration, View.VISIBLE);
        set.setVisibility(R.id.tracker_interrupt_reason, View.VISIBLE);
        transitionToKeyframeSet(mInterruptionsLayout, set);
    }
    
    public void transitionOutOfInterruptionTimer()
    {
        ConstraintSet set = loadConstraintSet(R.layout.tracker_interruptions);
        set.setVisibility(R.id.tracker_interrupt_duration, View.GONE);
        set.setVisibility(R.id.tracker_interrupt_reason, View.GONE);
        transitionToKeyframeSet(mInterruptionsLayout, set);
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void transitionLayoutToKeyframe(ConstraintLayout layout, int keyframeId)
    {
        // TODO [21-07-4 1:56AM] -- This is ok for now, but later on switch over to MotionLayout for
        //  more animation control (eg speed, colour, visibility, etc)
        //  https://developer.android.com/training/constraint-layout/motionlayout
        transitionToKeyframeSet(layout, loadConstraintSet(keyframeId));
    }
    
    private ConstraintSet loadConstraintSet(int keyframeId)
    {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.load(mContext, keyframeId);
        return constraintSet;
    }
    
    private void transitionToKeyframeSet(ConstraintLayout layout, ConstraintSet keyframe)
    {
        TransitionManager.beginDelayedTransition(layout);
        keyframe.applyTo(layout);
    }
}
