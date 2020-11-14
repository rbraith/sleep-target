package com.rbraithwaite.sleepapp.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppRepository;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.ui.sleep_tracker.SleepTrackerFragmentViewModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SleepTrackerFragmentViewModelTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepTrackerFragmentViewModel viewModel;
    private SleepAppRepository mockRepository;
    private Context context;
    
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
        viewModel.inSleepSession(context).observeForever(new Observer<Boolean>()
        {
            @Override
            public void onChanged(Boolean aBoolean)
            {
                // do nothing
                // this is just to activate the inSleepSession mediatorlivedata
                // https://stackoverflow.com/a/52407292
            }
        });
        
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
        viewModel.inSleepSession(context).observeForever(new Observer<Boolean>()
        {
            @Override
            public void onChanged(Boolean aBoolean)
            {
                // update inSleepSession livedata
            }
        });
        
        viewModel.endSleepSession(context);
        verify(mockRepository, times(0))
                .addSleepSession(any(SleepSessionEntity.class));
    }
    
    @Test
    public void endSleepSession_recordsNewSession() throws InterruptedException
    {
        Date testStartTime = TestUtils.ArbitraryData.getDate();
        LiveData<Date> mockLiveData = new MutableLiveData<Date>(testStartTime);
        when(mockRepository.getCurrentSession(context)).thenReturn(mockLiveData);
        viewModel.inSleepSession(context).observeForever(new Observer<Boolean>()
        {
            @Override
            public void onChanged(Boolean aBoolean)
            {
                // do nothing, activate inSleepSession
            }
        });
        
        viewModel.startSleepSession(context);
        viewModel.endSleepSession(context);
        
        ArgumentCaptor<SleepSessionEntity> addSleepSessionArg =
                ArgumentCaptor.forClass(SleepSessionEntity.class);
        verify(mockRepository, times(1)).addSleepSession(addSleepSessionArg.capture());
        
        SleepSessionEntity addSleepSession = addSleepSessionArg.getValue();
        assertThat(addSleepSession.startTime, is(equalTo(testStartTime)));
    }
}
