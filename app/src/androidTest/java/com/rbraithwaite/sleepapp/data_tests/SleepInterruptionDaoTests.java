package com.rbraithwaite.sleepapp.data_tests;

import android.content.Context;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionDao;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionDao;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aListOf;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSleepInterruptionEntity;
import static com.rbraithwaite.sleepapp.test_utils.test_data.TestData.aSleepSessionEntity;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class SleepInterruptionDaoTests
{
//*********************************************************
// private properties
//*********************************************************

    private SleepAppDatabase database;
    private SleepInterruptionDao interruptionDao;
    private SleepSessionDao sleepSessionDao;
    
//*********************************************************
// private constants
//*********************************************************

    private final List<Integer> NO_TAGS = new ArrayList<>();

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
        interruptionDao = database.getSleepInterruptionDao();
        sleepSessionDao = database.getSleepSessionDao();
    }
    
    @After
    public void teardown()
    {
        database.close();
    }
    
    @Test
    public void deleteMany_deletesCorrectValues()
    {
        sleepSessionDao.addSleepSessionWithExtras(
                aSleepSessionEntity().build(),
                NO_TAGS,
                aListOf(
                        aSleepInterruptionEntity(),
                        aSleepInterruptionEntity(),
                        aSleepInterruptionEntity()));
        
        List<SleepInterruptionEntity> entities = interruptionDao.getAll();
        assertThat(entities.size(), is(3));
        
        interruptionDao.deleteMany(Arrays.asList(1, 2));
        
        entities = interruptionDao.getAll();
        assertThat(entities.size(), is(1));
        assertThat(entities.get(0).id, is(3));
    }
    
    @Test
    public void interruptionsRemovedWhenSleepSessionDeleted()
    {
        SleepInterruptionEntity interruption1 = new SleepInterruptionEntity(
                TestUtils.ArbitraryData.getDate(),
                12345,
                "reason 1");
        
        SleepInterruptionEntity interruption2 = new SleepInterruptionEntity(
                TestUtils.ArbitraryData.getDate(),
                54321,
                "reason 2");
        
        long id = sleepSessionDao.addSleepSessionWithExtras(
                TestUtils.ArbitraryData.getSleepSessionEntity(),
                NO_TAGS,
                Arrays.asList(interruption1, interruption2));
        
        assertThat(interruptionDao.getAll().size(), is(2));
        
        // SUT
        sleepSessionDao.deleteSleepSession((int) id);
        
        assertThat(interruptionDao.getAll().isEmpty(), is(true));
    }
}
