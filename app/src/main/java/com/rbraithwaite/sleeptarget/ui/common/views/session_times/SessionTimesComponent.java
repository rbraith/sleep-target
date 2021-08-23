/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.ui.common.views.session_times;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.ui.common.dialog.AlertDialogFragment;
import com.rbraithwaite.sleeptarget.ui.common.views.datetime.DateTimeController;

// REFACTOR [21-07-29 5:23PM] -- Should this be a fragment?
public class SessionTimesComponent
        extends ConstraintLayout
{
//*********************************************************
// private properties
//*********************************************************

    private Fragment mParentFragment;
    private View mStartView;
    private View mEndView;
    private TextView mDurationText;
    private SessionTimesViewModel mViewModel;
    // these members are to retain the refs - nothing is done with them
    private DateTimeController mStartDateTimeController;
    private DateTimeController mEndDateTimeController;

//*********************************************************
// private constants
//*********************************************************

    private static final String DIALOG_ERROR = "DialogError_SessionTimes";
    
//*********************************************************
// constructors
//*********************************************************

    public SessionTimesComponent(@NonNull Context context)
    {
        super(context);
        initComponent(context);
    }
    
    public SessionTimesComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        initComponent(context);
    }
    
    public SessionTimesComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        initComponent(context);
    }
    
    public SessionTimesComponent(
            @NonNull Context context,
            @Nullable AttributeSet attrs,
            int defStyleAttr,
            int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        initComponent(context);
    }

//*********************************************************
// api
//*********************************************************

    
    /**
     * This MUST be called, or else things will break.
     */
    public void init(Fragment parentFragment, SessionTimesViewModel viewModel)
    {
        mParentFragment = parentFragment;
        mViewModel = viewModel;
        initStartController();
        initEndController();
        initDurationText();
    }
    
//*********************************************************
// private methods
//*********************************************************

    private void initDurationText()
    {
        mViewModel.getDurationText().observe(
                mParentFragment.getViewLifecycleOwner(),
                mDurationText::setText);
    }
    
    private void initStartController()
    {
        mStartDateTimeController = new DateTimeController(
                getString(R.string.common_session_times_start_title),
                mViewModel.getStartCalendar(),
                mStartView,
                createDateTimeFormatter(),
                mParentFragment.getViewLifecycleOwner(),
                mParentFragment.getChildFragmentManager());
        
        mStartDateTimeController.setCallbacks(new DateTimeController.Callbacks()
        {
            @Override
            public boolean beforeSetDate(int year, int month, int dayOfMonth)
            {
                try {
                    mViewModel.setStartDate(year, month, dayOfMonth);
                    return true;
                } catch (SessionTimesViewModel.InvalidDateTimeException e) {
                    displayErrorDialog(R.string.error_session_edit_start_datetime);
                    return false;
                } catch (SessionTimesViewModel.FutureDateTimeException e) {
                    // TODO [21-07-2 2:22AM] -- preferably the user wouldn't even
                    //  have the option to set future times (this could be tricky
                    //  though, due to how the Date & Time of Day are set separately).
                    displayErrorDialog(R.string.session_future_time_error);
                    return false;
                }
            }
            
            @Override
            public boolean beforeSetTimeOfDay(int hourOfDay, int minute)
            {
                // REFACTOR [21-07-1 9:13PM] -- Is there any way I can DRY these
                //  repeated try/catch blocks?.
                try {
                    mViewModel.setStartTimeOfDay(hourOfDay, minute);
                    return true;
                } catch (SessionTimesViewModel.InvalidDateTimeException e) {
                    displayErrorDialog(R.string.error_session_edit_start_datetime);
                    return false;
                } catch (SessionTimesViewModel.FutureDateTimeException e) {
                    displayErrorDialog(R.string.session_future_time_error);
                    return false;
                }
            }
        });
    }
    
    private void initEndController()
    {
        mEndDateTimeController = new DateTimeController(
                getString(R.string.common_session_times_end_title),
                mViewModel.getEndCalendar(),
                mEndView,
                createDateTimeFormatter(),
                mParentFragment.getViewLifecycleOwner(),
                mParentFragment.getChildFragmentManager());
        
        mEndDateTimeController.setCallbacks(new DateTimeController.Callbacks()
        {
            @Override
            public boolean beforeSetDate(int year, int month, int dayOfMonth)
            {
                try {
                    mViewModel.setEndDate(year, month, dayOfMonth);
                    return true;
                } catch (SessionTimesViewModel.InvalidDateTimeException e) {
                    displayErrorDialog(R.string.error_session_edit_end_datetime);
                    return false;
                } catch (SessionTimesViewModel.FutureDateTimeException e) {
                    displayErrorDialog(R.string.session_future_time_error);
                    return false;
                }
            }
            
            @Override
            public boolean beforeSetTimeOfDay(int hourOfDay, int minute)
            {
                try {
                    mViewModel.setEndTimeOfDay(hourOfDay, minute);
                    return true;
                } catch (SessionTimesViewModel.InvalidDateTimeException e) {
                    displayErrorDialog(R.string.error_session_edit_end_datetime);
                    return false;
                } catch (SessionTimesViewModel.FutureDateTimeException e) {
                    displayErrorDialog(R.string.session_future_time_error);
                    return false;
                }
            }
        });
    }
    
    // TODO [21-06-16 10:33PM] does it make sense to parameterize the formatting of
    //   the datetime views like this?
    private DateTimeController.Formatter createDateTimeFormatter()
    {
        return new DateTimeController.Formatter()
        {
            @Override
            public String formatTimeOfDay(int hourOfDay, int minute)
            {
                return SessionTimesFormatting.formatTimeOfDay(hourOfDay, minute);
            }
            
            @Override
            public String formatDate(int year, int month, int dayOfMonth)
            {
                return SessionTimesFormatting.formatDate(year, month, dayOfMonth);
            }
        };
    }
    
    // REFACTOR [21-07-30 11:55PM] -- Do I want a reusable component to be displaying its own
    //  error dialog? or should this be handled by the client in a callback?
    // REFACTOR [21-06-16 10:34PM] this should be extracted somewhere as a common utility.
    private void displayErrorDialog(int messageId)
    {
        AlertDialogFragment dialog = AlertDialogFragment.createInstance(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(mParentFragment.requireContext());
            builder.setMessage(messageId)
                    .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        });
        dialog.show(mParentFragment.getChildFragmentManager(), DIALOG_ERROR);
    }
    
    private String getString(int stringId)
    {
        return getContext().getString(stringId);
    }
    
    private void initComponent(Context context)
    {
        inflate(context, R.layout.common_session_times, this);
        
        mStartView = findViewById(R.id.common_session_times_start);
        mEndView = findViewById(R.id.common_session_times_end);
        mDurationText = findViewById(R.id.common_session_times_duration);
    }
}
