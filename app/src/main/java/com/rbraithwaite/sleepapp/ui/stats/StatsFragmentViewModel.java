package com.rbraithwaite.sleepapp.ui.stats;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;
import com.rbraithwaite.sleepapp.ui.stats.data.IntervalsDataSetGenerator;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.XYMultipleSeriesDataset;

import java.util.List;
import java.util.concurrent.Executor;

public class StatsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private MutableLiveData<IntervalsDataSetGenerator.Config> mIntervalsConfig =
            new MutableLiveData<>();
    
    private LiveData<XYMultipleSeriesDataset> mIntervalsDataSet;

//*********************************************************
// public constants
//*********************************************************

    public static final int DEFAULT_INTERVALS_OFFSET_HOURS = 16;

//*********************************************************
// package properties
//*********************************************************

    IntervalsDataSetGenerator mIntervalsDataSetGenerator;
    
    SleepSessionRepository mSleepSessionRepository;
    
    Executor mExecutor;

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public StatsFragmentViewModel(
            SleepSessionRepository sleepSessionRepository,
            IntervalsDataSetGenerator intervalsDataSetGenerator,
            Executor executor)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mIntervalsDataSetGenerator = intervalsDataSetGenerator;
        mExecutor = executor;
    }

//*********************************************************
// api
//*********************************************************

    public DateRange getDefaultIntervalsDateRange()
    {
        return DateRange.asWeekOf(
                TimeUtils.getNow(),
                (int) TimeUtils.hoursToMillis(DEFAULT_INTERVALS_OFFSET_HOURS));
    }
    
    /**
     * The LiveData returned from {@link StatsFragmentViewModel#getIntervalsDataSet()
     * getIntervalsDataSet()} is bound to this configuration and is updated when this is
     * reconfigured.
     */
    public void configureIntervalsDataSet(IntervalsDataSetGenerator.Config config)
    {
        mIntervalsConfig.setValue(config);
    }
    
    public LiveData<XYMultipleSeriesDataset> getIntervalsDataSet()
    {
        if (mIntervalsDataSet == null) {
            initIntervalsDataSet();
        }
        return mIntervalsDataSet;
    }
    
    public LiveData<String> getIntervalsValueText()
    {
        return Transformations.map(
                mIntervalsConfig,
                new Function<IntervalsDataSetGenerator.Config, String>()
                {
                    @Override
                    public String apply(IntervalsDataSetGenerator.Config config)
                    {
                        return StatsFormatting.formatIntervalsRange(config.dateRange);
                    }
                });
    }

//*********************************************************
// private methods
//*********************************************************

    private void initIntervalsDataSet()
    {
        // the intervals data set is bound to the configuration, so that it gets updated when the
        // intervals are reconfigured
        mIntervalsDataSet = Transformations.switchMap(
                mIntervalsConfig,
                new Function<IntervalsDataSetGenerator.Config, LiveData<XYMultipleSeriesDataset>>()
                {
                    @Override
                    public LiveData<XYMultipleSeriesDataset> apply(final IntervalsDataSetGenerator.Config config)
                    {
                        // returning a switch map transformation here since there are 2 needed
                        // layers of asynchronicity (the repo query and then the computation of the
                        // data set) and Transformations conveniently handles that first layer.
                        return Transformations.switchMap(
                                mSleepSessionRepository.getSleepSessionsInRange(
                                        config.dateRange.getStart(),
                                        config.dateRange.getEnd()),
                                new Function<List<SleepSessionModel>,
                                        LiveData<XYMultipleSeriesDataset>>()
                                {
                                    @Override
                                    public LiveData<XYMultipleSeriesDataset> apply(final List<SleepSessionModel> sleepSessions)
                                    {
                                        final MutableLiveData<XYMultipleSeriesDataset> liveData =
                                                new MutableLiveData<>();
                                        // computing the data set from the sleep sessions is a
                                        // potentially big job and needs to be asynchronous.
                                        mExecutor.execute(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                liveData.postValue(mIntervalsDataSetGenerator.generateFromConfig(
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
