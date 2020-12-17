package com.rbraithwaite.sleepapp.viewmodels;

import android.content.Context;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.ui.Constants;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragmentViewModel;
import com.rbraithwaite.sleepapp.utils.TickingLiveData;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowLooper;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

//import androidx.arch.core.executor.testing.InstantTaskExecutorRule;

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentViewModelTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepTrackerFragmentViewModel viewModel;
    private SleepAppRepository mockRepository;
    private Context context;
    
    // TODO [20-11-18 8:23PM] -- try replacing uses of livedata synchronizers (TestUtils) with
    //  InstantTaskExecutorRule
    //  requires this dep in gradle: testImplementation "android.arch.core:core-testing:1.1.1"
    //  ---
    //  see:
    //  https://proandroiddev.com/how-to-unit-test-livedata-and-lifecycle-components-8a0af41c90d9
    //  https://developer.android.com/reference/androidx/arch/core/executor/testing
    //  /InstantTaskExecutorRule
    //  https://stackoverflow.com/a/57843898
//    @Rule
//    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockRepository = mock(SleepAppRepository.class);
        viewModel = new SleepTrackerFragmentViewModel(mockRepository);
        context = ApplicationProvider.getApplicationContext();
    }
    
    @After
    public void teardown()
    {
        mockRepository = null;
        viewModel = null;
        context = null;
    }
    
    @Test
    public void getSessionStartTime_returnsTheStartTimeWhenThereIsASession()
    {
        Date testDate = TestUtils.ArbitraryData.getDate();
        when(mockRepository.getCurrentSession(any(Context.class))).thenReturn(new MutableLiveData<Date>(
                testDate));
        
        String expected = new SimpleDateFormat(Constants.STANDARD_FORMAT_FULL_DATE,
                                               Constants.STANDARD_LOCALE).format(testDate);
        
        String testStartTime = viewModel.getSessionStartTime(context);
        
        assertThat(testStartTime, is(equalTo(expected)));
    }
    
    @Test
    public void getSessionStartTime_returnsNullWhenNoSession()
    {
        when(mockRepository.getCurrentSession(any(Context.class))).thenReturn(new MutableLiveData<Date>(
                null));
        
        String testStartTime = viewModel.getSessionStartTime(context);
        
        assertThat(testStartTime, is(nullValue()));
    }
    
    @Test
    public void getCurrentSessionDuration_isZeroWhenNoSession()
    {
        String expected = new DurationFormatter().formatDurationMillis(0);
        
        when(mockRepository.getCurrentSession(context)).thenReturn(new MutableLiveData<Date>(null));
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDuration(context);
        TestUtils.activateLocalLiveData(currentSessionDuration);
        
        assertThat(currentSessionDuration.getValue(), is(equalTo(expected)));
    }
    
    // SMELL [20-11-18 9:03PM] -- tests shouldn't be this complex.
    @Test
    public void getCurrentSessionDuration_updatesWhenInSession()
    {
        final MutableLiveData<Date> mockCurrentSessionStart = new MutableLiveData<>(null);
        
        when(mockRepository.getCurrentSession(context)).thenReturn(mockCurrentSessionStart);
        
        LiveData<String> currentSessionDuration = viewModel.getCurrentSleepSessionDuration(context);
        TestUtils.LocalLiveDataSynchronizer<String> currentSessionDurationSynchronizer =
                new TestUtils.LocalLiveDataSynchronizer<>(currentSessionDuration);
        
        // start sleep session
        mockCurrentSessionStart.setValue(TestUtils.ArbitraryData.getDate());
        
        // REFACTOR [20-11-18 9:04PM] -- call this TestUtils.getShadowLooper(threadName).
        // It was necessary to manually manipulate the TickingLiveData's looper like this,
        // as it was not looping otherwise - it was not running the posted tick runnable, and
        // as a result mockCurrentSessionStart was never updated, meaning the sync() call below
        // would block forever.
        // ---
        // more information:
        // http://robolectric.org/blog/2019/06/04/paused-looper/
        // https://github.com/robolectric/robolectric/blob
        // /e197c5b9ed83dfd0d2ea6a74cf189f7b39463adc/robolectric/src/test/java/org/robolectric
        // /shadows/ShadowPausedLooperTest.java#L95
        // https://github.com/robolectric/robolectric/issues/1993
        // https://stackoverflow.com/a/39122515
        Collection<Looper> loopers = ShadowLooper.getAllLoopers();
        ShadowLooper shadowLooper = null;
        for (Looper looper : loopers) {
            if (looper.getThread().getName().equals(TickingLiveData.THREAD_NAME)) {
                shadowLooper = Shadow.extract(looper);
                break;
            }
        }
        assertThat(shadowLooper, is(notNullValue()));
        // no need to tick constantly, only need first runnable to update the value (first task)
        shadowLooper.runOneTask();
        shadowOf(Looper.getMainLooper()).idle(); // idk why this makes this test work but it does
        
        // assert current session duration reflects state of being in a session
        currentSessionDurationSynchronizer.sync();
        assertThat(currentSessionDuration.getValue(),
                   is(not(equalTo(new DurationFormatter().formatDurationMillis(0)))));
    }
    
    @Test
    public void inSleepSession_matchesSessionState()
    {
        final MutableLiveData<Date> mockStartTime = new MutableLiveData<Date>(null);
        when(mockRepository.getCurrentSession(any(Context.class))).thenReturn(mockStartTime);
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                mockStartTime.setValue(invocation.getArgumentAt(1, Date.class));
                return null;
            }
        }).when(mockRepository).setCurrentSession(any(Context.class), any(Date.class));
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                mockStartTime.setValue(null);
                return null;
            }
        }).when(mockRepository).clearCurrentSession(any(Context.class));
        
        TestUtils.activateLocalLiveData(viewModel.inSleepSession(context));
        assertThat(viewModel.inSleepSession(context).getValue(), is(false));
        
        viewModel.startSleepSession(context);
        assertThat(viewModel.inSleepSession(context).getValue(), is(true));
        
        viewModel.endSleepSession(context);
        assertThat(viewModel.inSleepSession(context).getValue(), is(false));
    }
    
    @Test
    public void startSleepSession_startsSession()
    {
        viewModel.startSleepSession(context);
        verify(mockRepository, times(1))
                .setCurrentSession(any(Context.class), any(Date.class));
    }
    
    @Test
    public void endSleepSession_doesNothingIfNoSession()
    {
        when(mockRepository.getCurrentSession(any(Context.class)))
                .thenReturn(new MutableLiveData<Date>(null));
        
        // SMELL [20-11-15 12:45AM] -- Right now, endSleepSession() fails if inSleepSession()
        //  is not being observed (ie is not activated). Consider other solutions besides
        //  adding an internal observer.
        TestUtils.activateLocalLiveData(viewModel.inSleepSession((context)));
        
        viewModel.endSleepSession(context);
        verify(mockRepository, times(0))
                .addSleepSessionData(any(SleepSessionData.class));
    }
    
    @Test
    public void endSleepSession_recordsNewSession() throws InterruptedException
    {
        Date testStartTime = TestUtils.ArbitraryData.getDate();
        LiveData<Date> mockLiveData = new MutableLiveData<Date>(testStartTime);
        when(mockRepository.getCurrentSession(context)).thenReturn(mockLiveData);
        
        TestUtils.activateLocalLiveData(viewModel.inSleepSession(context));
        
        viewModel.startSleepSession(context);
        viewModel.endSleepSession(context);
        
        ArgumentCaptor<SleepSessionData> addSleepSessionArg =
                ArgumentCaptor.forClass(SleepSessionData.class);
        verify(mockRepository, times(1)).addSleepSessionData(addSleepSessionArg.capture());
        
        SleepSessionData addSleepSession = addSleepSessionArg.getValue();
        assertThat(addSleepSession.startTime, is(equalTo(testStartTime)));
    }
}
