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

package com.rbraithwaite.sleepapp.ui.common.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment
        extends DialogFragment
        implements DatePickerDialog.OnDateSetListener
{
//*********************************************************
// private properties
//*********************************************************

    private OnDateSetListener mListener;
    private DatePicker mDatePicker;

//*********************************************************
// private constants
//*********************************************************

    private static final String ARG_YEAR = "year";
    private static final String ARG_MONTH = "month";
    private static final String ARG_DAY = "day";

//*********************************************************
// public helpers
//*********************************************************

    public interface OnDateSetListener
    {
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth);
    }

//*********************************************************
// overrides
//*********************************************************

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        
        return new DatePickerDialog(
                requireActivity(),
                this,
                args.getInt(ARG_YEAR),
                args.getInt(ARG_MONTH),
                args.getInt(ARG_DAY));
    }
    
    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
    {
        if (mListener != null) {
            mListener.onDateSet(view, year, month, dayOfMonth);
        }
    }

//*********************************************************
// api
//*********************************************************

    public static Bundle createArguments(int year, int month, int dayOfMonth)
    {
        Bundle args = new Bundle();
        args.putInt(ARG_YEAR, year);
        args.putInt(ARG_MONTH, month);
        args.putInt(ARG_DAY, dayOfMonth);
        return args;
    }
    
    public void setOnDateSetListener(DatePickerFragment.OnDateSetListener listener)
    {
        mListener = listener;
    }
    
    public DatePicker getDatePicker()
    {
        return mDatePicker;
    }
}
