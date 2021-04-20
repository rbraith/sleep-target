package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunction;
import com.rbraithwaite.sleepapp.data.database.junctions.sleep_session_tags.SleepSessionTagJunctionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
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
import static org.hamcrest.Matchers.is;

public class SleepSessionTagJunctionDaoTests
{
    private SleepAppDatabase database;
    private SleepSessionTagJunctionDao sleepSessionTagJunctionDao;
    private TagDao tagDao;
    private SleepSessionDao sleepSessionDao;

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

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
    
//*********************************************************
// api
//*********************************************************

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
    
        int tag1Id = (int)tagDao.addTag(tag1);
        int tag2Id = (int)tagDao.addTag(tag2);
    
        SleepSessionEntity sleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        int sessionId = (int)sleepSessionDao.addSleepSessionWithTags(sleepSession, Arrays.asList(tag1Id, tag2Id));
    
        List<SleepSessionTagJunction> junctions = sleepSessionTagJunctionDao.getAll();
        assertThat(junctions.size(), is(2));
    
        // SUT
        sleepSessionDao.deleteSleepSession(sessionId);
    
        // verify
        junctions = sleepSessionTagJunctionDao.getAll();
        assertThat(junctions.isEmpty(), is(true));
    }
}
