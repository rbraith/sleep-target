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

package com.rbraithwaite.sleeptarget.ui.stats.chart_intervals;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleeptarget.ui.stats.StatsFormatting;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalDataSetGenerator;
import com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalsDataSet;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;

import static com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalsDataSet.Resolution.MONTH;
import static com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalsDataSet.Resolution.WEEK;
import static com.rbraithwaite.sleeptarget.ui.stats.chart_intervals.data_set.IntervalsDataSet.Resolution.YEAR;

public class IntervalsChartViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<IntervalsDataSet.Config> mIntervalsConfig;
    
    private LiveData<IntervalsDataSet> mIntervalsDataSet;
    
    private TimeUtils mTimeUtils;
    
    private IntervalDataSetGenerator mSleepIntervalsDataSetGenerator;
    private SleepSessionRepository mSleepSessionRepository;
    
    private Executor mExecutor;
    private IntervalsDataSet.Resolution mIntervalsResolution = WEEK;

//*********************************************************
// public constants
//*********************************************************

    public static final boolean DEFAULT_INTERVALS_INVERT = true;
    
    // offsetting to the sunday of last week, so that the range start time of day is 4pm but
    // it includes the start of the monday (ie the first 24hrs of the range goes
    // from sun 4pm - mon 4pm)
    public static final int DEFAULT_INTERVALS_OFFSET_HOURS = -8;

//*********************************************************
// public helpers
//*********************************************************

    public enum Step
    {
        FORWARD,
        BACKWARD
    }

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public IntervalsChartViewModel(
            SleepSessionRepository sleepSessionRepository,
            IntervalDataSetGenerator sleepIntervalsDataSetGenerator,
            Executor executor)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mSleepIntervalsDataSetGenerator = sleepIntervalsDataSetGenerator;
        mExecutor = executor;
        mTimeUtils = createTimeUtils();
    }



//*********************************************************
// api
//*********************************************************

    
    /**
     * Step the intervals range forward or backward by its current resolution.
     */
    public void stepIntervalsRange(Step direction)
    {
        int sign = direction == Step.FORWARD ? 1 : -1;
        
        IntervalsDataSet.Config config = getIntervalsDataSetConfig();
        IntervalsDataSet.Resolution resolution = getIntervalsResolution();
        GregorianCalendar cal;
        DateRange newRange = null;
        switch (resolution) {
        case WEEK:
            // weeks are always 7 days so this is fine
            newRange = config.dateRange.offsetDays(7 * sign);
            break;
        case MONTH:
            cal = new GregorianCalendar();
            // if the offset is greater than 0, the end will be some time in the next month,
            // so I need to use the start, then vice versa if the offset is less than 0
            if (config.offsetMillis >= 0) {
                cal.setTime(config.dateRange.getStart());
            } else {
                cal.setTime(config.dateRange.getEnd());
            }
            cal.setTime(config.dateRange.getEnd());
            cal.add(Calendar.MONTH, 1 * sign);
            newRange = DateRange.asMonthOf(cal.getTime(), config.offsetMillis);
            break;
        case YEAR:
            cal = new GregorianCalendar();
            if (config.offsetMillis >= 0) {
                cal.setTime(config.dateRange.getStart());
            } else {
                cal.setTime(config.dateRange.getEnd());
            }
            cal.add(Calendar.YEAR, 1 * sign);
            newRange = DateRange.asYearOf(cal.getTime(), config.offsetMillis);
            break;
        }
        
        config.dateRange = newRange;
        configureIntervalsDataSet(config);
    }
    
    /**
     * The LiveData returned from {@link #getIntervalsDataSet() getIntervalsDataSet()} is bound to
     * this configuration and is updated when this is reconfigured.
     */
    public void configureIntervalsDataSet(IntervalsDataSet.Config config)
    {
        getIntervalsConfigMutable().setValue(config);
    }
    
    public LiveData<IntervalsDataSet> getIntervalsDataSet()
    {
        if (mIntervalsDataSet == null) {
            initIntervalsDataSet();
        }
        return mIntervalsDataSet;
    }
    
    // SMELL [21-03-5 1:06AM] -- This should probably be private - its only being used in the tests
    //  and it doesn't make sense for StatsFragment to ever know about SleepIntervalsDataSet.Config
    
    public LiveData<String> getIntervalsValueText()
    {
        return Transformations.map(
                getIntervalsConfigMutable(),
                config -> {
                    IntervalsDataSet.Resolution intervalsResolution = getIntervalsResolution();
                    switch (intervalsResolution) {
                    case WEEK:
                        // add 1 day so it displays mon-sun, instead of sun-sun
                        GregorianCalendar cal = new GregorianCalendar();
                        cal.setTime(config.dateRange.getStart());
                        cal.add(Calendar.DAY_OF_WEEK, 1);
                        return StatsFormatting.formatIntervalsRange(
                                new DateRange(cal.getTime(), config.dateRange.getEnd()));
                    case MONTH:
                        // The range offset is negative, so the end date will have the correct
                        // month.
                        return StatsFormatting.formatIntervalsMonthOf(config.dateRange.getEnd());
                    case YEAR:
                        return StatsFormatting.formatIntervalsYearOf(config.dateRange.getEnd());
                    default:
                        return null;
                    }
                });
    }
    
    public void setTimeUtils(TimeUtils timeUtils)
    {
        mTimeUtils = timeUtils;
    }
    
    public DateRange getIntervalsDateRange()
    {
        return getIntervalsConfigMutable().getValue().dateRange;
    }
    
    public IntervalsDataSet.Resolution getIntervalsResolution()
    {
        return mIntervalsResolution;
    }
    
    public void setIntervalsResolution(IntervalsDataSet.Resolution intervalsResolution)
    {
        // only do anything if the resolution has changed
        if (intervalsResolution != mIntervalsResolution) {
            mIntervalsResolution = intervalsResolution;
            
            IntervalsDataSet.Config config = getIntervalsConfigMutable().getValue();
            
            Date date;
            boolean invert = DEFAULT_INTERVALS_INVERT;
            int offsetMillis;
            if (config == null) {
                date = mTimeUtils.getNow();
                offsetMillis = (int) mTimeUtils.hoursToMillis(DEFAULT_INTERVALS_OFFSET_HOURS);
            } else {
                // getEnd() is used here because I know the offset is negative so that the end date
                // has the right week. Making this assumption here is fine because the
                // view model is the single source for the intervals range.
                date = config.dateRange.getEnd();
                invert = config.invert;
                offsetMillis = config.offsetMillis;
            }
            
            switch (intervalsResolution) {
            case WEEK:
                configureIntervalsDataSet(new IntervalsDataSet.Config(
                        DateRange.asWeekOf(date, offsetMillis),
                        offsetMillis,
                        invert,
                        WEEK));
                break;
            case MONTH:
                configureIntervalsDataSet(new IntervalsDataSet.Config(
                        DateRange.asMonthOf(date, offsetMillis),
                        offsetMillis,
                        invert,
                        MONTH,
                        mTimeUtils.monthOf(date)));
                break;
            case YEAR:
                configureIntervalsDataSet(new IntervalsDataSet.Config(
                        DateRange.asYearOf(date, offsetMillis),
                        offsetMillis,
                        invert,
                        YEAR,
                        mTimeUtils.yearOf(date)));
                break;
            default:
                // TODO [21-02-28 9:08PM] -- raise an exception here.
                break;
            }
        }
    }
    
    // SMELL [21-03-5 1:06AM] -- This should probably be private - its only being used in the tests
    //  and it doesn't make sense for StatsFragment to ever know about SleepIntervalsDataSet.Config
    public IntervalsDataSet.Config getIntervalsDataSetConfig()
    {
        return getIntervalsConfigMutable().getValue();
    }
    
    // HACK [21-03-30 4:11PM] -- This is only meant to be used in tests - find a better way.
    public void setIntervalsDataSetConfig(IntervalsDataSet.Config intervalsDataSetConfig)
    {
        getIntervalsConfigMutable().setValue(intervalsDataSetConfig);
    }
    
    /**
     * This is needed because sometimes the intervals range will extend across multiple months or
     * multiple years, and something must help callers know how these ranges are expected to be
     * displayed - i.e. which year or month that particular resolution & range is related to. If the
     * resolution is Resolution.WEEK, -1 is returned. If the resolution is invalid somehow, -2 is
     * returned.
     */
    public int getIntervalsResolutionValue()
    {
        IntervalsDataSet.Config config;
        GregorianCalendar cal;
        switch (getIntervalsResolution()) {
        case WEEK:
            return -1;
        case MONTH:
            config = getIntervalsDataSetConfig();
            cal = new GregorianCalendar();
            cal.setTime(config.dateRange.getEnd());
            return cal.get(Calendar.MONTH);
        case YEAR:
            config = getIntervalsDataSetConfig();
            cal = new GregorianCalendar();
            cal.setTime(config.dateRange.getEnd());
            return cal.get(Calendar.YEAR);
        default:
            return -2;
        }
    }
    
    /**
     * Whether or not there is any interval data, in any range.
     */
    public LiveData<Boolean> hasAnyData()
    {
        return Transformations.map(
                mSleepSessionRepository.getTotalSleepSessionCount(),
                count -> count > 0);
    }

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }

//*********************************************************
// private methods
//*********************************************************

    private MutableLiveData<IntervalsDataSet.Config> getIntervalsConfigMutable()
    {
        if (mIntervalsConfig == null) {
            mIntervalsConfig = new MutableLiveData<>(getDefaultIntervalsDataSetConfig());
        }
        return mIntervalsConfig;
    }
    
    // SMELL [21-03-2 5:23PM] -- I don't think this is a great solution - think harder about a
    //  better one.
    
    private IntervalsDataSet.Config getDefaultIntervalsDataSetConfig()
    {
        int offsetMillis = (int) mTimeUtils.hoursToMillis(DEFAULT_INTERVALS_OFFSET_HOURS);
        return new IntervalsDataSet.Config(
                DateRange.asWeekOf(mTimeUtils.getNow(), offsetMillis),
                offsetMillis,
                DEFAULT_INTERVALS_INVERT,
                WEEK);
    }
    
    private void initIntervalsDataSet()
    {
        // the intervals data set is bound to the configuration, so that it gets updated when the
        // intervals are reconfigured
        mIntervalsDataSet = Transformations.switchMap(
                getIntervalsConfigMutable(),
                config -> {
                    // returning a switch map transformation here since there are 2 needed
                    // layers of asynchronicity (the repo query and then the computation of the
                    // data set) and Transformations conveniently handles that first layer.
                    return Transformations.switchMap(
                            mSleepSessionRepository.getSleepSessionsInRange(
                                    config.dateRange.getStart(),
                                    config.dateRange.getEnd()),
                            sleepSessions -> {
                                final MutableLiveData<IntervalsDataSet> liveData =
                                        new MutableLiveData<>();
                                // computing the data set from the sleep sessions is a
                                // potentially big job and needs to be asynchronous.
                                mExecutor.execute(() -> liveData.postValue(
                                        mSleepIntervalsDataSetGenerator.generateFromConfig(
                                                sleepSessions,
                                                config)));
                                return liveData;
                            });
                });
    }
}
