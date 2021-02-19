package com.rbraithwaite.sleepapp.data.current_session;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.data.SleepAppDataPrefs;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Date;
import java.util.GregorianCalendar;

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
public class CurrentSessionRepositoryTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDataPrefs mockPrefs;
    private CurrentSessionRepository repository;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockPrefs = mock(SleepAppDataPrefs.class);
        repository = new CurrentSessionRepository(mockPrefs);
    }
    
    @After
    public void teardown()
    {
        repository = null;
        mockPrefs = null;
    }
    
    @Test
    public void setCurrentSession_setsCurrentSession()
    {
        Date testStartTime = TestUtils.ArbitraryData.getDate();
        repository.setCurrentSession(testStartTime);
        verify(mockPrefs, times(1)).setCurrentSession(testStartTime);
    }
    
    @Test
    public void clearCurrentSession_clearsCurrentSession()
    {
        repository.setCurrentSession(TestUtils.ArbitraryData.getDate());
        repository.clearCurrentSession();
        verify(mockPrefs, times(1)).setCurrentSession(null);
    }
    
    @Test
    public void getCurrentSession_callsPrefs()
    {
        Date expected = TestUtils.ArbitraryData.getDate();
        when(mockPrefs.getCurrentSession()).thenReturn(new MutableLiveData<>(expected));
        
        LiveData<CurrentSessionModel> currentSession = repository.getCurrentSession();
        TestUtils.activateLocalLiveData(currentSession);
        
        assertThat(currentSession.getValue().getStart(), is(equalTo(expected)));
    }
    
    @Test
    public void getCurrentSession_reflectsSetSession()
    {
        // setup
        final MutableLiveData<Date> mockCurrentSession = new MutableLiveData<>();
        doAnswer(new Answer<Void>()
        {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable
            {
                mockCurrentSession.setValue(invocation.getArgumentAt(0, Date.class));
                return null;
            }
        }).when(mockPrefs).setCurrentSession(any(Date.class));
        when(mockPrefs.getCurrentSession()).thenReturn(mockCurrentSession);
        
        final GregorianCalendar calendar = new GregorianCalendar();
        calendar.set(2020, 6, 6, 13, 15);
        Date testDate1 = calendar.getTime();
        
        calendar.set(2012, 3, 4, 15, 6);
        Date testDate2 = calendar.getTime();
        
        // run the test
        LiveData<CurrentSessionModel> currentSession = repository.getCurrentSession();
        TestUtils.activateLocalLiveData(currentSession);
        
        repository.setCurrentSession(testDate1);
        assertThat(currentSession.getValue().getStart(), is(equalTo(testDate1)));
        
        repository.setCurrentSession(testDate2);
        assertThat(currentSession.getValue().getStart(), is(equalTo(testDate2)));
    }
}
