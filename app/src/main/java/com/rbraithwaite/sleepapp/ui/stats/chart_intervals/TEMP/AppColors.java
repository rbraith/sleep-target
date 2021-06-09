package com.rbraithwaite.sleepapp.ui.stats.chart_intervals.TEMP;

import android.content.Context;
import android.content.res.TypedArray;

import com.rbraithwaite.sleepapp.R;

// REFACTOR [21-06-5 6:13PM] -- move this somewhere else.
public class AppColors
{
//*********************************************************
// private properties
//*********************************************************

    private static int[] attrArray = new int[] {
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
            R.attr.appColorComplementary
    };

//*********************************************************
// public properties
//*********************************************************

    public static int NOT_FOUND = -1;
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
        ta.recycle();
        return appColors;
    }
}
