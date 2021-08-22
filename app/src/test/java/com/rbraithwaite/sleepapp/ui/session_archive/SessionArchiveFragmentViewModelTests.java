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

package com.rbraithwaite.sleepapp.ui.session_archive;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.core.models.Tag;
import com.rbraithwaite.sleepapp.core.repositories.SleepSessionRepository;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;
import com.rbraithwaite.sleepapp.ui.session_details.data.SleepSessionWrapper;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;

import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SessionArchiveFragmentViewModelTests
{
//*********************************************************
// package properties
//*********************************************************

    SleepSessionRepository mockSleepSessionRepository;
    SessionArchiveFragmentViewModel viewModel;

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        mockSleepSessionRepository = mock(SleepSessionRepository.class);
        viewModel = new SessionArchiveFragmentViewModel(mockSleepSessionRepository);
    }
    
    @After
    public void teardown()
    {
        viewModel = null;
        mockSleepSessionRepository = null;
    }
    
    @Test
    public void getInitialAddSessionData_returnsZeroId()
    {
        LiveData<SleepSessionWrapper> sleepSession = viewModel.getInitialAddSessionData();
        TestUtils.activateLocalLiveData(sleepSession);
        SleepSession sleepSessionModel = sleepSession.getValue().getModel();
        assertThat(sleepSessionModel.getId(), is(0));
    }
    
    @Test
    public void getSleepSessionEntity_positiveInput()
    {
        int testId = 5;
        SleepSession expected = TestUtils.ArbitraryData.getSleepSession();
        expected.setId(testId);
        when(mockSleepSessionRepository.getSleepSession(testId)).thenReturn(
                new MutableLiveData<SleepSession>(expected));
        
        // SUT
        LiveData<SleepSessionWrapper> sleepSession = viewModel.getSleepSession(testId);
        
        TestUtils.activateLocalLiveData(sleepSession);
        assertThat(sleepSession.getValue().getModel(), is(expected));
    }
    
    @Test
    public void addSessionFromResult_addsSessionOnValidInput()
    {
        SleepSession sleepSession = TestUtils.ArbitraryData.getSleepSession();
        viewModel.addSleepSession(new SleepSessionWrapper(sleepSession));
        
        ArgumentCaptor<SleepSessionRepository.NewSleepSessionData> arg =
                ArgumentCaptor.forClass(SleepSessionRepository.NewSleepSessionData.class);
        verify(mockSleepSessionRepository, times(1)).addSleepSession(arg.capture());
        
        SleepSessionRepository.NewSleepSessionData newSleepSession = arg.getValue();
        
        // REFACTOR [21-05-10 3:26PM] -- consider converting to the same type then using equalTo().
        assertThat_NewSleepSession_isEqualTo_SleepSession(newSleepSession, sleepSession);
    }
    
    @Test
    public void updateSessionFromResult_updatesSessionOnValidInput()
    {
        // for this test, it doesn't matter that the id for this data is 0
        SleepSession expected = TestUtils.ArbitraryData.getSleepSession();
        
        viewModel.updateSleepSession(new SleepSessionWrapper(expected));
        
        verify(mockSleepSessionRepository, times(1)).updateSleepSession(expected);
    }
    
    @Test
    public void deleteSession_deletesSession()
    {
        SleepSession toDelete = TestUtils.ArbitraryData.getSleepSession();
        int sessionId = 5;
        toDelete.setId(sessionId);
        viewModel.deleteSession(new SleepSessionWrapper(toDelete));
        verify(mockSleepSessionRepository).deleteSleepSession(sessionId);
    }
    
    @Test
    public void deleteSession_returnsDeletedSessionId()
    {
        SleepSession toDelete = TestUtils.ArbitraryData.getSleepSession();
        int sessionId = 5;
        toDelete.setId(sessionId);
        int deletedId = viewModel.deleteSession(new SleepSessionWrapper(toDelete));
        assertThat(deletedId, is(sessionId));
    }

//*********************************************************
// private methods
//*********************************************************

    private void assertThat_NewSleepSession_isEqualTo_SleepSession(
            SleepSessionRepository.NewSleepSessionData newSleepSession,
            SleepSession sleepSession)
    {
        assertThat(newSleepSession.start, is(equalTo(sleepSession.getStart())));
        assertThat(newSleepSession.end, is(equalTo(sleepSession.getEnd())));
        assertThat(newSleepSession.durationMillis, is(equalTo(sleepSession.getDurationMillis())));
        assertThat(newSleepSession.additionalComments,
                   is(equalTo(sleepSession.getAdditionalComments())));
        assertThat(newSleepSession.mood, is(equalTo(sleepSession.getMood())));
        assertThat(newSleepSession.tagIds,
                   is(equalTo(sleepSession.getTags()
                                      .stream()
                                      .map(Tag::getTagId)
                                      .collect(Collectors.toList()))));
        assertThat(newSleepSession.rating, is(equalTo(sleepSession.getRating())));
    }
}
