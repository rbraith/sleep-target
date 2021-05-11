package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.rbraithwaite.sleepapp.ui.common.data.MoodUiData;



/**
 * Displays moods as emoji TextViews.
 */
public class EmojiMoodViewFactory
        implements MoodViewFactory
{
//*********************************************************
// overrides
//*********************************************************

    // TODO [21-04-3 1:56AM] -- think about how to test this.
    @Override
    public View createView(MoodUiData mood, Context context, float scale)
    {
        TextView moodView = new TextView(context);
        // https://stackoverflow.com/a/26894146
        moodView.setText(new String(Character.toChars(convertMoodToEmojiUnicode(mood))));
        moodView.setTextSize(scale);
        return moodView;
    }

//*********************************************************
// private methods
//*********************************************************

    private int convertMoodToEmojiUnicode(MoodUiData mood)
    {
        switch (mood.type) {
        case MOOD_1:
            return 0x1F600;
        case MOOD_2:
            return 0x1F603;
        case MOOD_3:
            return 0x1F604;
        case MOOD_4:
            return 0x1F601;
        case MOOD_5:
            return 0x1F606;
        case MOOD_6:
            return 0x1F605;
        case MOOD_7:
            return 0x1F923;
        case MOOD_8:
            return 0x1F602;
        case MOOD_9:
            return 0x1F642;
        case MOOD_10:
            return 0x1F643;
        case MOOD_11:
            return 0x1F609;
        case MOOD_12:
            return 0x1F60A;
        default:
            throw new IllegalArgumentException("Invalid Mood Type.");
        }
    }
}
