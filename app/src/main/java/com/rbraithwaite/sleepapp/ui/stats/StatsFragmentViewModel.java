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
import com.rbraithwaite.sleepapp.ui.stats.data.IntervalDataSet;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import org.achartengine.model.XYMultipleSeriesDataset;

import java.util.List;
import java.util.concurrent.Executor;

public class StatsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// public constants
//*********************************************************

    public static final int DEFAULT_CHART_OFFSET_HOURS = 16;

//*********************************************************
// package properties
//*********************************************************

    SleepSessionRepository mSleepSessionRepository;
    Executor mExecutor;
    
//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public StatsFragmentViewModel(
            SleepSessionRepository sleepSessionRepository,
            Executor executor)
    {
        mSleepSessionRepository = sleepSessionRepository;
        mExecutor = executor;
    }
    
//*********************************************************
// api
//*********************************************************

    public DateRange getDefaultIntervalsDateRange()
    {
        return DateRange.asWeekOf(
                TimeUtils.getNow(),
                (int) TimeUtils.hoursToMillis(DEFAULT_CHART_OFFSET_HOURS));
    }
    
    public LiveData<XYMultipleSeriesDataset> getIntervalDataSetFromDateRange(
            final DateRange dateRange,
            final boolean invert)
    {
        // returning a switch map transformation here since there are 2 needed layers of
        // asynchronicity (the repo query and then the computation of the data set) and
        // Transformations conveniently handles that first layer.
        return Transformations.switchMap(
                mSleepSessionRepository.getSleepSessionsInRange(
                        dateRange.getStart(),
                        dateRange.getEnd()),
                new Function<List<SleepSessionModel>, LiveData<XYMultipleSeriesDataset>>()
                {
                    @Override
                    public LiveData<XYMultipleSeriesDataset> apply(final List<SleepSessionModel> input)
                    {
                        final MutableLiveData<XYMultipleSeriesDataset> liveData =
                                new MutableLiveData<>();
                        // computing the data set from the sleep sessions is a potentially big job
                        // and needs to be asynchronous.
                        mExecutor.execute(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                IntervalDataSet intervalDataSet =
                                        IntervalDataSet.fromSleepSessionRange(
                                                input,
                                                dateRange,
                                                invert);
                                liveData.postValue(intervalDataSet.getDataSet());
                            }
                        });
                        return liveData;
                    }
                });
    }
}
