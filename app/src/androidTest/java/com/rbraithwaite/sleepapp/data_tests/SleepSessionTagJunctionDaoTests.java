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

package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunctionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithTags;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagDao;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class SleepSessionTagJunctionDaoTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDatabase database;
    private SleepSessionTagJunctionDao sleepSessionTagJunctionDao;
    private TagDao tagDao;
    private SleepSessionDao sleepSessionDao;

//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, SleepAppDatabase.class).build();
        sleepSessionTagJunctionDao = database.getSleepSessionTagsDao();
        tagDao = database.getTagDao();
        sleepSessionDao = database.getSleepSessionDao();
    }
    
    @After
    public void teardown()
    {
        database.close();
    }
    
    
    // REFACTOR [21-04-22 8:39PM] -- Should this be in SleepSessionDaoTests?
    @Test
    public void getSleepSessionWithTags_reflects_updateSleepSessionWithTags()
    {
        TagEntity tag1 = new TagEntity();
        tag1.text = "tag1";
        TagEntity tag2 = new TagEntity();
        tag2.text = "tag2";
        
        int tag1Id = (int) tagDao.addTag(tag1);
        int tag2Id = (int) tagDao.addTag(tag2);
        tag1.id = tag1Id;
        tag2.id = tag2Id;
        
        SleepSessionEntity entity = TestUtils.ArbitraryData.getSleepSessionEntity();
        int sessionId =
                (int) sleepSessionDao.addSleepSessionWithTags(entity, Arrays.asList(tag1Id));
        entity.id = sessionId;
        
        LiveData<SleepSessionWithTags> sleepSession =
                sleepSessionDao.getSleepSessionWithTags(sessionId);
        TestUtils.InstrumentationLiveDataSynchronizer<SleepSessionWithTags> synchronizer =
                new TestUtils.InstrumentationLiveDataSynchronizer<>(sleepSession);
        
        String updatedComment = "updated";
        entity.additionalComments = updatedComment;
        // SUT
        sleepSessionDao.updateSleepSessionWithTags(entity, Arrays.asList(tag2Id));
        
        // verify
        synchronizer.sync();
        assertThat(sleepSession.getValue().sleepSession, is(equalTo(entity)));
        assertThat(sleepSession.getValue().tags.get(0), is(equalTo(tag2)));
    }
    
    @Test
    public void sleepSessionTagJunctionsRemovedWhenTagDeleted()
    {
        TagEntity tag1 = new TagEntity();
        tag1.text = "tag1";
        TagEntity tag2 = new TagEntity();
        tag2.text = "tag2";
        
        int tag1Id = (int) tagDao.addTag(tag1);
        int tag2Id = (int) tagDao.addTag(tag2);
        tag1.id = tag1Id;
        tag2.id = tag2Id;
        
        SleepSessionEntity sleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        sleepSessionDao.addSleepSessionWithTags(sleepSession, Arrays.asList(tag1Id, tag2Id));
        
        List<SleepSessionTagJunction> junctions = sleepSessionTagJunctionDao.getAll();
        assertThat(junctions.size(), is(2));
        
        // SUT
        tagDao.deleteTag(tag1);
        
        // verify
        junctions = sleepSessionTagJunctionDao.getAll();
        assertThat(junctions.size(), is(1));
    }
    
    @Test
    public void sleepSessionTagJunctionsRemovedWhenSleepSessionDeleted()
    {
        // REFACTOR [21-04-20 3:27PM] -- duplicates sleepSessionTagJunctionsRemovedWhenTagDeleted
        //  setup.
        TagEntity tag1 = new TagEntity();
        tag1.text = "tag1";
        TagEntity tag2 = new TagEntity();
        tag2.text = "tag2";
        
        int tag1Id = (int) tagDao.addTag(tag1);
        int tag2Id = (int) tagDao.addTag(tag2);
        
        SleepSessionEntity sleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        int sessionId = (int) sleepSessionDao.addSleepSessionWithTags(sleepSession,
                                                                      Arrays.asList(tag1Id,
                                                                                    tag2Id));
        
        List<SleepSessionTagJunction> junctions = sleepSessionTagJunctionDao.getAll();
        assertThat(junctions.size(), is(2));
        
        // SUT
        sleepSessionDao.deleteSleepSession(sessionId);
        
        // verify
        junctions = sleepSessionTagJunctionDao.getAll();
        assertThat(junctions.isEmpty(), is(true));
    }
}
