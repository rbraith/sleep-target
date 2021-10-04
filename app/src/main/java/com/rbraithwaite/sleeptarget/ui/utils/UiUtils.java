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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class UiUtils
{
//*********************************************************
// public helpers
//*********************************************************

    
    /**
     * Values are in dp.
     */
    public static class SizeDp
    {
        public final int width;
        public final int height;
        
        public SizeDp(int width, int height)
        {
            this.width = width;
            this.height = height;
        }
        
        /**
         * A square size.
         */
        public SizeDp(int size)
        {
            this(size, size);
        }
    }
    
    /**
     * Values are in dp.
     */
    public static class MarginsDp
    {
        public final int left;
        public final int top;
        public final int right;
        public final int bottom;
        
        public MarginsDp(int left, int top, int right, int bottom)
        {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
        }
    }

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
    
    /**
     * Convert from density-independent pixels to actual pixels. see https://developer.android
     * .com/training/multiscreen/screendensities#dips-pels
     *
     * @param dp      the density-independent pixels to convert.
     * @param context Needed to get the device's dpi
     *
     * @return The actual screen pixel size.
     */
    public static int convertDpToPx(int dp, Context context)
    {
        return convertDpToPx(dp, getScaleFrom(context));
    }
    
    public static void initViewMarginLayoutParams(View view, SizeDp sizeDp)
    {
        initViewMarginLayoutParams(view, sizeDp, null);
    }
    
    /**
     * Gives the view a simple MarginLayoutParams. This is a convenience method so that dp can be
     * used instead of px. If marginsDp is null the margins are not set.
     */
    public static void initViewMarginLayoutParams(View view, SizeDp sizeDp, MarginsDp marginsDp)
    {
        float scale = getScaleFrom(view.getContext());
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(
                toPx(sizeDp.width, scale), toPx(sizeDp.height, scale));
        if (marginsDp != null) {
            params.setMargins(
                    convertDpToPx(marginsDp.left, scale),
                    convertDpToPx(marginsDp.top, scale),
                    convertDpToPx(marginsDp.right, scale),
                    convertDpToPx(marginsDp.bottom, scale));
        }
        view.setLayoutParams(params);
    }
    
    public static void setEditTextValue(EditText editText, String value)
    {
        editText.getText().clear();
        if (value != null && !value.isEmpty()) {
            editText.getText().append(value);
        }
    }
    
    public static void setViewPadding(View view, int leftDp, int rightDp, int topDp, int bottomDp)
    {
        Context context = view.getContext();
        view.setPadding(
                convertDpToPx(leftDp, context),
                convertDpToPx(topDp, context),
                convertDpToPx(rightDp, context),
                convertDpToPx(bottomDp, context));
    }
    
    public static int convertSpToPx(float sp, Context context)
    {
        // https://stackoverflow.com/a/29665208
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                                               sp,
                                               context.getResources().getDisplayMetrics());
    }

//*********************************************************
// private methods
//*********************************************************

    private static int convertDpToPx(int dp, float scale)
    {
        return (int) (dp * scale + 0.5f);
    }
    
    // SMELL [21-06-13 2:37AM] -- I don't like this interface lol - find a better way to do this
    //  (the problem was I couldn't create some BaseView class - maybe I could turn these view
    //  utilities into some kind of ViewHelper wrapping class idk).
    
    private static float getScaleFrom(Context context)
    {
        return context.getResources().getDisplayMetrics().density;
    }
    
    // REFACTOR [21-06-12 8:42PM] -- This needs a better name...
    
    /**
     * Convert dp to px while accounting for layout param values.
     */
    private static int toPx(int dp, float scale)
    {
        int size = dp;
        if (size != MATCH_PARENT && size != WRAP_CONTENT) {
            size = convertDpToPx(dp, scale);
        }
        return size;
    }
}
