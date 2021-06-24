package com.rbraithwaite.sleepapp.ui.common.views.mood_selector.TEMP;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import com.rbraithwaite.sleepapp.R;

import java.util.Arrays;
import java.util.List;

public class MoodView
        extends androidx.appcompat.widget.AppCompatImageView
{
//*********************************************************
// private properties
//*********************************************************

    private int mMoodColor;

//*********************************************************
// private constants
//*********************************************************

    private static final List<Integer> MOOD_DRAWABLES = Arrays.asList(
            R.drawable.mood_00face00smileclosed00_111_smile,
            R.drawable.mood_00face00smileclosed01_114_smile,
            R.drawable.mood_00face00smileclosed02_116_smile,
            R.drawable.mood_00face00smileclosed03_051_grinning,
            R.drawable.mood_00face00smileclosed04_016_relax,
            R.drawable.mood_00face00smileclosed05_154_wink,
            R.drawable.mood_00face00smileclosed06_024_cool,
            R.drawable.mood_00face00smileclosed07_121_cool,
            R.drawable.mood_00face00smileclosed08_015_teeth,
            R.drawable.mood_00face00smileclosed09_068_amused,
            R.drawable.mood_00face00smileclosed10_103_tongue,
            R.drawable.mood_00face00smileclosed11_104_tongue,
            R.drawable.mood_00face00smileclosed12_119_angel,
            R.drawable.mood_00face01smileopen00_056_grinning,
            R.drawable.mood_00face01smileopen01_053_happy,
            R.drawable.mood_00face01smileopen02_057_grinning,
            R.drawable.mood_00face01smileopen03_060_grinning,
            R.drawable.mood_00face01smileopen04_064_grinning,
            R.drawable.mood_00face01smileopen05_137_laughing,
            R.drawable.mood_00face01smileopen06_096_laughing,
            R.drawable.mood_00face01smileopen07_095_laughing,
            R.drawable.mood_00face01smileopen08_067_sweat,
            R.drawable.mood_00face01smileopen09_065_wink,
            R.drawable.mood_00face01smileopen10_036_greed,
            R.drawable.mood_00face01smileopen11_131_famous,
            R.drawable.mood_00face01smileopen12_160_zany,
            R.drawable.mood_00face02love00_071_in_love,
            R.drawable.mood_00face02love01_013_kiss,
            R.drawable.mood_00face02love02_073_kiss,
            R.drawable.mood_00face02love03_074_kiss,
            R.drawable.mood_00face02love04_120_smile,
            R.drawable.mood_00face02love05_072_hug,
            R.drawable.mood_00face03tongue00_127_mocking,
            R.drawable.mood_00face03tongue00_128_mocking,
            R.drawable.mood_00face03tongue00_143_tongue,
            R.drawable.mood_00face03tongue00_146_tongue,
            R.drawable.mood_00face03tongue00_147_tongue,
            R.drawable.mood_00face03tongue00_153_wink,
            R.drawable.mood_00face03tongue00_158_yummy,
            R.drawable.mood_00face04neutral00_017_shut,
            R.drawable.mood_00face04neutral00_022_confused,
            R.drawable.mood_00face04neutral00_049_smart,
            R.drawable.mood_00face04neutral00_079_liar,
            R.drawable.mood_00face04neutral00_086_neutral,
            R.drawable.mood_00face04neutral00_094_rolling_eyes,
            R.drawable.mood_00face04neutral00_109_sleeping,
            R.drawable.mood_00face04neutral00_126_sleep,
            R.drawable.mood_00face04neutral00_149_upside_down,
            R.drawable.mood_00face04neutral00_155_mute,
            R.drawable.mood_00face04neutral00_161_zany,
            R.drawable.mood_00face04neutral00_162_zipper,
            R.drawable.mood_00face05surprise00_004_amazed,
            R.drawable.mood_00face05surprise00_020_confused,
            R.drawable.mood_00face05surprise00_032_dead,
            R.drawable.mood_00face05surprise00_034_dead,
            R.drawable.mood_00face05surprise00_037_exploding,
            R.drawable.mood_00face05surprise00_039_flushed,
            R.drawable.mood_00face05surprise00_105_shock,
            R.drawable.mood_00face05surprise00_133_surprised,
            R.drawable.mood_00face06sick00_082_sick,
            R.drawable.mood_00face06sick00_083_mouth_full,
            R.drawable.mood_00face06sick00_122_cold,
            R.drawable.mood_00face06sick00_150_puke,
            R.drawable.mood_00face07sad00_042_sad,
            R.drawable.mood_00face07sad01_100_sad,
            R.drawable.mood_00face07sad02_102_sad,
            R.drawable.mood_00face07sad03_099_sad,
            R.drawable.mood_00face07sad04_136_crying,
            R.drawable.mood_00face07sad05_045_sad,
            R.drawable.mood_00face07sad06_043_sad,
            R.drawable.mood_00face07sad07_152_sad,
            R.drawable.mood_00face07sad08_029_crying,
            R.drawable.mood_00face07sad09_091_disappointment,
            R.drawable.mood_00face07sad10_018_cold,
            R.drawable.mood_00face08angry00_005_anger,
            R.drawable.mood_00face08angry00_006_anger,
            R.drawable.mood_00face08angry00_008_angry,
            R.drawable.mood_00face08angry00_092_angry,
            R.drawable.mood_00face08angry00_148_upset,
            R.drawable.mood_00face09other00_087_ninja,
            R.drawable.mood_00face09other00_090_pirate,
            R.drawable.mood_00face09other00_140_thief,
            R.drawable.mood_01other_001_alien,
            R.drawable.mood_01other_046_ghost,
            R.drawable.mood_01other_088_poo,
            R.drawable.mood_01other_093_robot,
            R.drawable.mood_01other_106_skull,
            R.drawable.mood_01other_117_ghost);
    private static final int DEFAULT_MOOD_INDEX = 0;
    
    // TODO [21-06-13 11:31PM] --
    //  I should have a ctor that takes the mood index.
    
//*********************************************************
// constructors
//*********************************************************

    public MoodView(@NonNull Context context)
    {
        super(context);
        setMood(DEFAULT_MOOD_INDEX);
    }
    
    public MoodView(
            Context context,
            @Nullable AttributeSet attrs)
    {
        super(context, attrs);
        TypedArray ta = context.obtainStyledAttributes(R.styleable.MoodView);
        int moodIndex;
        try {
            moodIndex = ta.getInt(R.styleable.MoodView_mood, DEFAULT_MOOD_INDEX);
        } finally {
            ta.recycle();
        }
        setMood(moodIndex);
    }
    
//*********************************************************
// api
//*********************************************************

    public static int getMoodCount()
    {
        return MOOD_DRAWABLES.size();
    }
    
    public void setMood(int moodIndex)
    {
        int drawableId;
        try {
            drawableId = MOOD_DRAWABLES.get(moodIndex);
        } catch (IndexOutOfBoundsException e) {
            // TODO [21-06-9 7:21PM] -- maybe let the client handle an invalid index instead?
            drawableId = DEFAULT_MOOD_INDEX;
        }
        setImageDrawable(AppCompatResources.getDrawable(getContext(), drawableId));
    }
    
    public int getMoodColor()
    {
        return mMoodColor;
    }
    
    public void setMoodColor(int color)
    {
        setColorFilter(color);
        mMoodColor = color;
    }
}
