package com.rbraithwaite.sleepapp.ui.stats;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;
import com.rbraithwaite.sleepapp.ui.stats.data.SleepIntervalsDataSet;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

public class StatsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<SleepIntervalsDataSet.Config> mIntervalsConfig;
    
    private LiveData<SleepIntervalsDataSet> mIntervalsDataSet;
    
    private TimeUtils mTimeUtils;
    
    private SleepIntervalsDataSet.Generator mSleepIntervalsDataSetGenerator;
    private SleepSessionRepository mSleepSessionRepository;
    
    private Executor mExecutor;
    private Resolution mIntervalsResolution = Resolution.WEEK;

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

    // REFACTOR [21-02-26 10:31PM] -- consider moving this to something like StatsUtils.Resolution,
    //  since StatsFragmentViewModel.Resolution is kind of awkward.
    public enum Resolution
    {
        WEEK,
        MONTH,
        YEAR,
        // TODO [21-02-26 10:25PM] -- future feature: custom chart resolutions.
//        CUSTOM
    }
    
    public enum Step
    {
        FORWARD,
        BACKWARD
    }

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public StatsFragmentViewModel(
            SleepSessionRepository sleepSessionRepository,
            SleepIntervalsDataSet.Generator sleepIntervalsDataSetGenerator,
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
        
        SleepIntervalsDataSet.Config oldConfig = getIntervalsDataSetConfig();
        StatsFragmentViewModel.Resolution resolution = getIntervalsResolution();
        GregorianCalendar cal;
        DateRange newRange = null;
        switch (resolution) {
        case WEEK:
            // weeks are always 7 days so this is fine
            newRange = oldConfig.dateRange.offsetDays(7 * sign);
            break;
        case MONTH:
            cal = new GregorianCalendar();
            // if the offset is greater than 0, the end will be some time in the next month,
            // so I need to use the start, then vice versa if the offset is less than 0
            if (oldConfig.offsetMillis >= 0) {
                cal.setTime(oldConfig.dateRange.getStart());
            } else {
                cal.setTime(oldConfig.dateRange.getEnd());
            }
            cal.setTime(oldConfig.dateRange.getEnd());
            cal.add(Calendar.MONTH, 1 * sign);
            newRange = DateRange.asMonthOf(cal.getTime(), oldConfig.offsetMillis);
            break;
        case YEAR:
            cal = new GregorianCalendar();
            if (oldConfig.offsetMillis >= 0) {
                cal.setTime(oldConfig.dateRange.getStart());
            } else {
                cal.setTime(oldConfig.dateRange.getEnd());
            }
            cal.add(Calendar.YEAR, 1 * sign);
            newRange = DateRange.asYearOf(cal.getTime(), oldConfig.offsetMillis);
            break;
        }
        
        configureIntervalsDataSet(new SleepIntervalsDataSet.Config(
                newRange,
                oldConfig.offsetMillis,
                oldConfig.invert));
    }
    
    /**
     * The LiveData returned from {@link StatsFragmentViewModel#getIntervalsDataSet()
     * getIntervalsDataSet()} is bound to this configuration and is updated when this is
     * reconfigured.
     */
    public void configureIntervalsDataSet(SleepIntervalsDataSet.Config config)
    {
        getIntervalsConfigMutable().setValue(config);
    }
    
    public LiveData<SleepIntervalsDataSet> getIntervalsDataSet()
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
                new Function<SleepIntervalsDataSet.Config, String>()
                {
                    @Override
                    public String apply(SleepIntervalsDataSet.Config config)
                    {
                        Resolution intervalsResolution = getIntervalsResolution();
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
    
    public Resolution getIntervalsResolution()
    {
        return mIntervalsResolution;
    }
    
    public void setIntervalsResolution(Resolution intervalsResolution)
    {
        // only do anything if the resolution has changed
        if (intervalsResolution != mIntervalsResolution) {
            mIntervalsResolution = intervalsResolution;
            
            SleepIntervalsDataSet.Config config = getIntervalsConfigMutable().getValue();
            
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
                configureIntervalsDataSet(new SleepIntervalsDataSet.Config(
                        DateRange.asWeekOf(date, offsetMillis),
                        offsetMillis,
                        invert));
                break;
            case MONTH:
                configureIntervalsDataSet(new SleepIntervalsDataSet.Config(
                        DateRange.asMonthOf(date, offsetMillis),
                        offsetMillis,
                        invert));
                break;
            case YEAR:
                configureIntervalsDataSet(new SleepIntervalsDataSet.Config(
                        DateRange.asYearOf(date, offsetMillis),
                        offsetMillis,
                        invert));
                break;
            default:
                // TODO [21-02-28 9:08PM] -- raise an exception here.
                break;
            }
        }
    }
    
    // SMELL [21-03-5 1:06AM] -- This should probably be private - its only being used in the tests
    //  and it doesn't make sense for StatsFragment to ever know about SleepIntervalsDataSet.Config
    public SleepIntervalsDataSet.Config getIntervalsDataSetConfig()
    {
        return getIntervalsConfigMutable().getValue();
    }
    
    // HACK [21-03-30 4:11PM] -- This is only meant to be used in tests - find a better way.
    public void setIntervalsDataSetConfig(SleepIntervalsDataSet.Config intervalsDataSetConfig)
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
        SleepIntervalsDataSet.Config config;
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

    private MutableLiveData<SleepIntervalsDataSet.Config> getIntervalsConfigMutable()
    {
        if (mIntervalsConfig == null) {
            mIntervalsConfig = new MutableLiveData<>(getDefaultIntervalsDataSetConfig());
        }
        return mIntervalsConfig;
    }
    
    // SMELL [21-03-2 5:23PM] -- I don't think this is a great solution - think harder about a
    //  better one.
    
    private SleepIntervalsDataSet.Config getDefaultIntervalsDataSetConfig()
    {
        int offsetMillis = (int) mTimeUtils.hoursToMillis(DEFAULT_INTERVALS_OFFSET_HOURS);
        return new SleepIntervalsDataSet.Config(
                DateRange.asWeekOf(mTimeUtils.getNow(), offsetMillis),
                offsetMillis,
                DEFAULT_INTERVALS_INVERT);
    }
    
    private void initIntervalsDataSet()
    {
        // the intervals data set is bound to the configuration, so that it gets updated when the
        // intervals are reconfigured
        mIntervalsDataSet = Transformations.switchMap(
                getIntervalsConfigMutable(),
                new Function<SleepIntervalsDataSet.Config, LiveData<SleepIntervalsDataSet>>()
                {
                    @Override
                    public LiveData<SleepIntervalsDataSet> apply(final SleepIntervalsDataSet.Config config)
                    {
                        // returning a switch map transformation here since there are 2 needed
                        // layers of asynchronicity (the repo query and then the computation of the
                        // data set) and Transformations conveniently handles that first layer.
                        return Transformations.switchMap(
                                mSleepSessionRepository.getSleepSessionsInRange(
                                        config.dateRange.getStart(),
                                        config.dateRange.getEnd()),
                                new Function<List<SleepSession>,
                                        LiveData<SleepIntervalsDataSet>>()
                                {
                                    @Override
                                    public LiveData<SleepIntervalsDataSet> apply(final List<SleepSession> sleepSessions)
                                    {
                                        final MutableLiveData<SleepIntervalsDataSet> liveData =
                                                new MutableLiveData<>();
                                        // computing the data set from the sleep sessions is a
                                        // potentially big job and needs to be asynchronous.
                                        mExecutor.execute(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                liveData.postValue(mSleepIntervalsDataSetGenerator.generateFromConfig(
                                                        sleepSessions,
                                                        config));
                                            }
                                        });
                                        return liveData;
                                    }
                                });
                    }
                });
    }
}
