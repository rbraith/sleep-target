package com.rbraithwaite.sleepapp.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.TestUtils;
import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.dao.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.views.SleepSessionData;
import com.rbraithwaite.sleepapp.data.database.views.dao.SleepSessionDataDao;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SleepAppRepositoryTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepAppDataPrefs mockPrefs;
    SleepAppDatabase mockDatabase;
    
    Executor synchronousExecutor = new Executor()
    {
        @Override
        public void execute(Runnable command)
        {
            command.run();
        }
    };
    
    SleepAppRepository repository;
    Context context;
    
//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockPrefs = mock(SleepAppDataPrefs.class);
        mockDatabase = mock(SleepAppDatabase.class);
        repository = new SleepAppRepository(mockPrefs, mockDatabase, synchronousExecutor);
        
        context = ApplicationProvider.getApplicationContext();
    }
    
    @After
    public void teardown()
    {
        mockPrefs = null;
        mockDatabase = null;
        repository = null;
        context = null;
    }
    
    
    @Test
    public void getCurrentSession_nullWhenNoSession()
    {
        LiveData<Date> mockLiveData = new MutableLiveData<>(null);
        when(mockPrefs.getCurrentSession(any(Context.class))).thenReturn(mockLiveData);
        
        assertThat(repository.getCurrentSession(context).getValue(), is(nullValue()));
    }
    
    @Test
    public void getCurrentSession_reflectsSetSession()
    {
        final GregorianCalendar calendar = new GregorianCalendar();
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                calendar.setTime(invocation.getArgumentAt(1, Date.class));
                return null;
            }
        }).when(mockPrefs).setCurrentSession(any(Context.class), any(Date.class));
        doAnswer(new Answer<LiveData<Date>>()
        {
            @Override
            public LiveData<Date> answer(InvocationOnMock invocation) throws Throwable
            {
                return new MutableLiveData<Date>(calendar.getTime());
            }
        }).when(mockPrefs).getCurrentSession(any(Context.class));
        
        calendar.set(2020, 6, 6, 13, 15);
        Date testDate1 = calendar.getTime();
        
        calendar.set(2012, 3, 4, 15, 6);
        Date testDate2 = calendar.getTime();
        
        repository.setCurrentSession(context, testDate1);
        assertThat(repository.getCurrentSession(context).getValue(), is(equalTo(testDate1)));
        
        repository.setCurrentSession(context, testDate2);
        assertThat(repository.getCurrentSession(context).getValue(), is(equalTo(testDate2)));
    }
    
    @Test
    public void addSleepSession_addsSleepSession()
    {
        SleepSessionDao mockSleepSessionDao = mock(SleepSessionDao.class);
        when(mockDatabase.getSleepSessionDao()).thenReturn(mockSleepSessionDao);
        
        SleepSessionEntity testSleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        
        repository.addSleepSession(testSleepSession);
        
        verify(mockSleepSessionDao, times(1)).addSleepSession(testSleepSession);
    }
    
    @Test
    public void getAllSleepSessionIds_returnEmptyListWhenNoIds()
    {
        SleepSessionDataDao mockSleepSessionDataDao = mock(SleepSessionDataDao.class);
        when(mockSleepSessionDataDao.getAllSleepSessionDataIds()).thenReturn(
                new MutableLiveData<List<Integer>>(new ArrayList<Integer>()));
        
        when(mockDatabase.getSleepSessionDataDao()).thenReturn(mockSleepSessionDataDao);
        
        List<Integer> ids = repository.getAllSleepSessionDataIds().getValue();
        assertThat(ids.size(), is(0));
    }
    
    
    // todo this test just duplicates getAllSleepSessionIds_returnEmptyListWhenNoIds
    @Test
    public void getAllSleepSessionIds_returnsAllIds()
    {
        List<Integer> testIdData = Arrays.asList(1, 2, 3, 4);
        
        SleepSessionDataDao mockSleepSessionDataDao = mock(SleepSessionDataDao.class);
        when(mockSleepSessionDataDao.getAllSleepSessionDataIds())
                .thenReturn(new MutableLiveData<>(testIdData));
        
        when(mockDatabase.getSleepSessionDataDao()).thenReturn(mockSleepSessionDataDao);
        
        List<Integer> ids = repository.getAllSleepSessionDataIds().getValue();
        assertThat(ids, is(equalTo(testIdData)));
    }
    
    @Test
    public void getSleepSessionData_positiveInput()
    {
        int positiveId = 1;
        
        SleepSessionData expectedData = TestUtils.ArbitraryData.getSleepSessionData();
        SleepSessionDataDao mockSleepSessionDataDao = mock(SleepSessionDataDao.class);
        when(mockSleepSessionDataDao.getSleepSessionData(positiveId))
                .thenReturn(new MutableLiveData<>(expectedData));
        
        when(mockDatabase.getSleepSessionDataDao()).thenReturn(mockSleepSessionDataDao);
        
        LiveData<SleepSessionData> liveData = repository.getSleepSessionData(positiveId);
        
        assertThat(liveData.getValue(), is(equalTo(expectedData)));
    }
    // todo
    //  null input
    //  negative input
}
