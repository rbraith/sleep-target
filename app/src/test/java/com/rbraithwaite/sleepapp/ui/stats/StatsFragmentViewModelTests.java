package com.rbraithwaite.sleepapp.ui.stats;

import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.stats.data.DateRange;

import org.achartengine.model.RangeCategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.Executor;

import static com.rbraithwaite.sleepapp.utils.TimeUtils.hoursToMillis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class StatsFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    StatsFragmentViewModel viewModel;
    SleepSessionRepository mockSleepSessionRepository;
    Executor executor;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        executor = new TestUtils.SynchronizedExecutor();
        viewModel = new StatsFragmentViewModel(mockSleepSessionRepository, executor);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        executor = null;
        mockSleepSessionRepository = null;
    }
    
    @Test
    public void getIntervalDataSetFromDateRange_returnsCorrectDataSet()
    {
        // setup test data
        GregorianCalendar start1 =
                new GregorianCalendar(2021, 2, 19, 23, 0); // 1 hr over into 02/20
        long duration1 = hoursToMillis(2);
        GregorianCalendar start2 = new GregorianCalendar(2021, 2, 21, 1, 0); // 3 hrs in 02/21
        long duration2 = hoursToMillis(3);
        GregorianCalendar start3 = new GregorianCalendar(2021, 2, 21, 5, 0); // 1 hr in 02/21
        long duration3 = hoursToMillis(1);
        
        SleepSessionModel sleepSession1 = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession1.setStart(start1.getTime());
        sleepSession1.setDuration(duration1);
        
        SleepSessionModel sleepSession2 = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession2.setStart(start2.getTime());
        sleepSession2.setDuration(duration2);
        
        SleepSessionModel sleepSession3 = TestUtils.ArbitraryData.getSleepSessionModel();
        sleepSession3.setStart(start3.getTime());
        sleepSession3.setDuration(duration3);
        
        when(mockSleepSessionRepository.getSleepSessionsInRange(any(Date.class), any(Date.class)))
                .thenReturn(new MutableLiveData<>(Arrays.asList(
                        sleepSession1,
                        sleepSession2,
                        sleepSession3)));
        
        // SUT
        LiveData<XYMultipleSeriesDataset> dataSet = viewModel.getIntervalDataSetFromDateRange(
                new DateRange(
                        new GregorianCalendar(2021, 2, 20).getTime(),
                        new GregorianCalendar(2021, 2, 22).getTime()),
                false);
        TestUtils.activateLocalLiveData(dataSet);
        shadowOf(Looper.getMainLooper()).idle(); // allows the asynchronous interval data set
        // computation to run
        
        // verifying expected point data
        //
        //           02/20                  02/21
        // series 1: [(1, 0), (1, <1hr>),   (2, <1hr>),  (2, <4hrs>)]
        // series 2: [(1,0),  (1,0),        (2, <5hrs>), (2, <6hr>)]
        
        XYSeries series1 = dataSet.getValue().getSeriesAt(0);
        // 02/20
        assertThat((int) series1.getX(0), is(equalTo(1)));
        assertThat(series1.getY(0), is(equalTo(0.0)));
        assertThat((int) series1.getX(1), is(equalTo(1)));
        assertThat(series1.getY(1), is(equalTo((double) hoursToMillis(1))));
        // 02/21
        assertThat((int) series1.getX(2), is(equalTo(2)));
        assertThat(series1.getY(2), is(equalTo((double) hoursToMillis(1))));
        assertThat((int) series1.getX(3), is(equalTo(2)));
        assertThat(series1.getY(3), is(equalTo((double) hoursToMillis(4))));
        
        XYSeries series2 = dataSet.getValue().getSeriesAt(1);
        // 02/20
        // (this is intended to test the zero-height 'filler' ranges used to make sure
        // further data entries are entered on the correct day)
        assertThat((int) series2.getX(0), is(equalTo(1)));
        assertThat(series2.getY(0), is(equalTo(0.0)));
        assertThat((int) series2.getX(1), is(equalTo(1)));
        assertThat(series2.getY(1), is(equalTo(0.0)));
        // 02/21
        assertThat((int) series2.getX(2), is(equalTo(2)));
        assertThat(series2.getY(2), is(equalTo((double) hoursToMillis(5))));
        assertThat((int) series2.getX(3), is(equalTo(2)));
        assertThat(series2.getY(3), is(equalTo((double) hoursToMillis(6))));
    }
    
    
    // clarifying AChartEngine behaviour for myself
    @Test
    public void RangeCategorySeries_toXYSeries_createsPairedXPoints()
    {
        RangeCategorySeries testSeries = new RangeCategorySeries("test");
        // data point #1
        testSeries.add(1, 4.5); // this becomes [0](1, 1.0), [1](1, 4.5)
        // data point #2
        testSeries.add(3, 5.4); // this becomes [2](2, 3.0), [3](2, 5.4)
        
        XYSeries testXYSeries = testSeries.toXYSeries();
        
        assertThat(testXYSeries.getItemCount(), is(equalTo(4)));
        
        // range data point #1
        assertThat((int) testXYSeries.getX(0), is(equalTo(1)));
        assertThat(testXYSeries.getY(0), is(equalTo(1.0)));
        assertThat((int) testXYSeries.getX(1), is(equalTo(1)));
        assertThat(testXYSeries.getY(1), is(equalTo(4.5)));
        
        // range data point #2
        assertThat((int) testXYSeries.getX(2), is(equalTo(2)));
        assertThat(testXYSeries.getY(2), is(equalTo(3.0)));
        assertThat((int) testXYSeries.getX(3), is(equalTo(2)));
        assertThat(testXYSeries.getY(3), is(equalTo(5.4)));
    }
}
