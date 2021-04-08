package com.rbraithwaite.sleepapp.ui.common.mood_selector;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

public class MoodSelectorViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<MoodUiData> mMood;
    private List<MoodUiData> allMoods;

//*********************************************************
// public constants
//*********************************************************

    public static final int NO_MOOD = -1;

//*********************************************************
// constructors
//*********************************************************

    public MoodSelectorViewModel(MoodUiData mood)
    {
        mMood = new MutableLiveData<>(mood);
    }
    
    public MoodSelectorViewModel()
    {
        this(null);
    }

//*********************************************************
// api
//*********************************************************

    public Boolean isMoodSet()
    {
        return mMood.getValue() != null;
    }
    
    public void clearMood()
    {
        mMood.setValue(null);
    }
    
    public List<MoodUiData> getAllMoods()
    {
        if (allMoods == null) {
            allMoods = new ArrayList<>();
            for (MoodUiData.Type t : MoodUiData.Type.values()) {
                allMoods.add(new MoodUiData(t));
            }
        }
        return allMoods;
    }
    
    /**
     * @return The index of the currently set mood. If no mood is set, returns {@link
     * MoodSelectorViewModel#NO_MOOD}
     */
    public int getMoodIndex()
    {
        MoodUiData currentMood = getMood().getValue();
        if (currentMood == null) {
            return NO_MOOD;
        }
        
        List<MoodUiData> allMoods = getAllMoods();
        for (int i = 0; i < allMoods.size(); i++) {
            MoodUiData mood = allMoods.get(i);
            if (mood.equals(currentMood)) {
                return i;
            }
        }
        
        return NO_MOOD;
    }
    
    public LiveData<MoodUiData> getMood()
    {
        return mMood;
    }
    
    public void setMood(MoodUiData mood)
    {
        mMood.setValue(mood);
    }
}
