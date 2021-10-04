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
package com.rbraithwaite.sleeptarget.ui.utils;

import android.content.Context;
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerUtils
{
//*********************************************************
// public helpers
//*********************************************************

    public static class VerticalMargin
            extends RecyclerView.ItemDecoration
    {
        private int marginPx;
        
        public VerticalMargin(int marginDp, Context context)
        {
            marginPx = UiUtils.convertDpToPx(marginDp, context);
        }
        
        @Override
        public void getItemOffsets(
                @NonNull Rect outRect,
                @NonNull View view,
                @NonNull RecyclerView parent,
                @NonNull RecyclerView.State state)
        {
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = marginPx;
            }
            outRect.bottom = marginPx;
        }
    }

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
