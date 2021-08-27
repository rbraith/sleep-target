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

package com.rbraithwaite.sleeptarget.ui.stats.chart_durations;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.core.models.SleepSession;
import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.utils.CommonUtils;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DurationsChartViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSessionRepository mSleepSessionRepository;
    
    private LiveData<List<DataPoint>> mDataSet;
    private MutableLiveData<RangeData> mRangeData;
    private LiveData<String> mRangeText;
    private TimeUtils mTimeUtils;

//*********************************************************
// public constants
//*********************************************************

    public final int DEFAULT_RANGE_DISTANCE = 10;
    public final int DEFAULT_RANGE_OFFSET = 0;

//*********************************************************
// public helpers
//*********************************************************

    public static class DataPoint
    {
        public double sleepDurationHours;
        public float sleepRating;
        public String label;
        
        public DataPoint(double sleepDurationHours, float sleepRating, String label)
        {
            this.sleepDurationHours = sleepDurationHours;
            this.sleepRating = sleepRating;
            this.label = label;
        }
    }

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public DurationsChartViewModel(SleepSessionRepository sleepSessionRepository)
    {
        mSleepSessionRepository = sleepSessionRepository;
        // REFACTOR [21-05-17 2:59PM] -- this should be injected.
        mTimeUtils = new TimeUtils();
    }

//*********************************************************
// api
//*********************************************************

    public int getRangeDistance()
    {
        return Optional.ofNullable(getRangeData().getValue())
                .map(rangeData -> rangeData.distance)
                .orElse(0);
    }
    
    public void setRangeDistance(int rangeDistance)
    {
        // TODO [21-05-15 3:03PM] -- precond: distance > 0
        RangeData range = getRangeData().getValue();
        getRangeData().setValue(new RangeData(range.offset, rangeDistance));
    }
    
    public int getRangeOffset()
    {
        return Optional.ofNullable(getRangeData().getValue())
                .map(rangeData -> rangeData.offset)
                .orElse(0);
    }
    
    public LiveData<List<DataPoint>> getDataSet()
    {
        mDataSet = CommonUtils.lazyInit(mDataSet, () -> Transformations.switchMap(
                getRangeData(),
                rangeData -> toDataSet(mSleepSessionRepository.getLatestSleepSessionsFromOffset(
                        rangeData.offset,
                        rangeData.distance))));
        return mDataSet;
    }
    
    public void stepRangeBack()
    {
        RangeData range = getRangeData().getValue();
        List<DataPoint> dataset = getDataSet().getValue();
        // If the current dataset is less than the range distance, its because we've reached
        // the end of full data
        if (dataset != null && !(dataset.size() < range.distance)) {
            getRangeData().setValue(new RangeData(
                    // stepping back means increasing the offset (getting further from the most
                    // recent sleep session)
                    range.offset + range.distance,
                    range.distance));
        }
    }
    
    public void stepRangeForward()
    {
        RangeData range = getRangeData().getValue();
        // only do anything if the range has been stepped backward
        if (range.offset > 0) {
            getRangeData().setValue(new RangeData(
                    // stepping forward means decreasing the offset, down to 0 (which is the most
                    // recent session)
                    Math.max(0, range.offset - range.distance),
                    range.distance));
        }
    }
    
    public LiveData<String> getRangeText()
    {
        mRangeText = CommonUtils.lazyInit(mRangeText, () -> Transformations.map(
                getRangeData(),
                rangeData -> Integer.toString(rangeData.distance)
        ));
        return mRangeText;
    }

//*********************************************************
// private methods
//*********************************************************

    private MutableLiveData<RangeData> getRangeData()
    {
        mRangeData = CommonUtils.lazyInit(mRangeData, () -> new MutableLiveData<>(
                new RangeData(DEFAULT_RANGE_OFFSET, DEFAULT_RANGE_DISTANCE)));
        return mRangeData;
    }
    
    private LiveData<List<DataPoint>> toDataSet(LiveData<List<SleepSession>> sleepSessionsLive)
    {
        return Transformations.map(sleepSessionsLive, sleepSessions -> {
            // handling an edge case where the end of the full data aligned with the range (ie the
            // current dataset was never < the range distance). This might happen when stepping the
            // range back, and could cause the chart to display empty data. In this case we need
            // to reuse the old dataset (which would be the last set of data) and reset the
            // incorrectly-stepped-back range.
            if (sleepSessions == null || sleepSessions.isEmpty()) {
                RangeData rangeData = mRangeData.getValue();
                // REFACTOR [21-05-15 3:56PM] -- this is duplicating logic in stepRangeForward().
                // SMELL [21-05-15 4:00PM] -- This is kind of side-effect-y - is there anything
                //  I can do about this?
                // Step the range forward to counteract the implied back step that just occurred
                // (It's unnecessary to update the range live data here)
                rangeData.offset = Math.max(0, rangeData.offset - rangeData.distance);
                
                // return the old dataset
                return mDataSet.getValue();
            }
            
            return sleepSessions
                    .stream()
                    .map(sleepSession -> new DataPoint(
                            mTimeUtils.millisToHours(sleepSession.getDurationMillis()),
                            sleepSession.getRating(),
                            DurationsChartFormatting.formatDataLabel(sleepSession.getStart())))
                    .collect(Collectors.toList());
        });
    }

//*********************************************************
// private helpers
//*********************************************************

    private static class RangeData
    {
        public int offset;
        public int distance;
        
        public RangeData(int offset, int distance)
        {
            this.offset = offset;
            this.distance = distance;
        }
    }
}