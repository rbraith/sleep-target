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

package com.rbraithwaite.sleepapp.ui.utils;

import android.content.Context;
import android.content.res.TypedArray;

import com.rbraithwaite.sleepapp.R;

public class AppColors
{
//*********************************************************
// private constants
//*********************************************************

    private static final int[] attrArray = new int[] {
            R.attr.colorPrimary,
            R.attr.colorOnPrimary,
            R.attr.colorPrimaryDark,
            R.attr.colorSecondary,
            R.attr.colorOnSecondary,
            R.attr.appColorTriadic2,
            R.attr.appColorTriadic3,
            R.attr.colorPrimarySurface,
            R.attr.colorOnPrimarySurface,
            R.attr.appColorOnPrimarySurface2,
            R.attr.appColorSecondarySurface,
            R.attr.appColorOnSecondarySurface,
            R.attr.appColorBackground,
            R.attr.colorBackgroundFloating,
            R.attr.appColorOnBackgroundFloating,
            R.attr.appColorOnBackgroundFloating2,
            R.attr.appColorSurfaceFloating,
            R.attr.appDialogTitleIconColor,
            R.attr.appColorComplementary,
            R.attr.appColorInterruption,
            R.attr.appColorInterruptionDark
    };

//*********************************************************
// public properties
//*********************************************************

    public int colorPrimary;
    public int colorOnPrimary;
    public int colorPrimaryDark;
    public int colorSecondary;
    public int colorOnSecondary;
    public int appColorTriadic2;
    public int appColorTriadic3;
    public int colorPrimarySurface;
    public int colorOnPrimarySurface;
    public int appColorOnPrimarySurface2;
    public int appColorSecondarySurface;
    public int appColorOnSecondarySurface;
    public int appColorBackground;
    public int colorBackgroundFloating;
    public int appColorOnBackgroundFloating;
    public int appColorOnBackgroundFloating2;
    public int appColorSurfaceFloating;
    public int appDialogTitleIconColor;
    public int appColorComplementary;
    public int appColorInterruption;
    public int appColorInterruptionDark;

//*********************************************************
// public constants
//*********************************************************

    public static final int NOT_FOUND = -1;

//*********************************************************
// constructors
//*********************************************************

    private AppColors() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static AppColors from(Context context)
    {
        AppColors appColors = new AppColors();
        // REFACTOR [21-07-12 10:02PM] -- this should all be in a try/finally block.
        TypedArray ta = context.obtainStyledAttributes(AppColors.attrArray);
        appColors.colorPrimary = ta.getColor(0, AppColors.NOT_FOUND);
        appColors.colorOnPrimary = ta.getColor(1, AppColors.NOT_FOUND);
        appColors.colorPrimaryDark = ta.getColor(2, AppColors.NOT_FOUND);
        appColors.colorSecondary = ta.getColor(3, AppColors.NOT_FOUND);
        appColors.colorOnSecondary = ta.getColor(4, AppColors.NOT_FOUND);
        appColors.appColorTriadic2 = ta.getColor(5, AppColors.NOT_FOUND);
        appColors.appColorTriadic3 = ta.getColor(6, AppColors.NOT_FOUND);
        appColors.colorPrimarySurface = ta.getColor(7, AppColors.NOT_FOUND);
        appColors.colorOnPrimarySurface = ta.getColor(8, AppColors.NOT_FOUND);
        appColors.appColorOnPrimarySurface2 = ta.getColor(9, AppColors.NOT_FOUND);
        appColors.appColorSecondarySurface = ta.getColor(10, AppColors.NOT_FOUND);
        appColors.appColorOnSecondarySurface = ta.getColor(11, AppColors.NOT_FOUND);
        appColors.appColorBackground = ta.getColor(12, AppColors.NOT_FOUND);
        appColors.colorBackgroundFloating = ta.getColor(13, AppColors.NOT_FOUND);
        appColors.appColorOnBackgroundFloating = ta.getColor(14, AppColors.NOT_FOUND);
        appColors.appColorOnBackgroundFloating2 = ta.getColor(15, AppColors.NOT_FOUND);
        appColors.appColorSurfaceFloating = ta.getColor(16, AppColors.NOT_FOUND);
        appColors.appDialogTitleIconColor = ta.getColor(17, AppColors.NOT_FOUND);
        appColors.appColorComplementary = ta.getColor(18, AppColors.NOT_FOUND);
        appColors.appColorInterruption = ta.getColor(19, AppColors.NOT_FOUND);
        appColors.appColorInterruptionDark = ta.getColor(20, AppColors.NOT_FOUND);
        ta.recycle();
        return appColors;
    }
}
