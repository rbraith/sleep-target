package com.rbraithwaite.sleepapp.dev_tools;

import android.os.Handler;
import android.os.Looper;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.concurrent.Executor;

public class DevToolsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    // No repo layer here cause these are just dumb dev tools.
    // Otherwise I would need to implement 'addSleepSessions' in
    // SleepSessionRepository and SleepSessionDao, which isn't ideal
    private SleepAppDatabase mDatabase;
    private Executor mExecutor;
    private GregorianCalendar defaultBaseDay = new GregorianCalendar(2021, 1, 14);
    private GregorianCalendar mBaseDay;

//*********************************************************
// private constants
//*********************************************************

    private static final long RANDOM_SEED = 123456L;

//*********************************************************
// public helpers
//*********************************************************

    public interface AsyncTaskListener
    {
        void onComplete();
    }


//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public DevToolsFragmentViewModel(
            SleepAppDatabase database,
            Executor executor)
    {
        mDatabase = database;
        mExecutor = executor;
        
        mBaseDay = new GregorianCalendar();
        mBaseDay.setTimeInMillis(defaultBaseDay.getTimeInMillis());
    }

//*********************************************************
// api
//*********************************************************

    public void clearData(final AsyncTaskListener listener)
    {
        runAsyncTask(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        mDatabase.clearAllTables();
                        mBaseDay.setTimeInMillis(defaultBaseDay.getTimeInMillis());
                    }
                },
                listener);
    }
    
    public void addArbitrarySleepSessions(final int sessionAmount, final AsyncTaskListener listener)
    {
        runAsyncTask(
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        Random rand = new Random();
                        rand.setSeed(RANDOM_SEED); // keep the random data deterministic
                        for (int i = 0; i < sessionAmount; i++) {
                            mDatabase.getSleepSessionDao()
                                    .addSleepSession(generateRandomSleepSessionEntity(mBaseDay,
                                                                                      rand));
                            // one session per day
                            // having the base day as a stored property ensures subsequent
                            // additions of
                            // sleep sessions are added to the right days
                            mBaseDay.add(Calendar.DAY_OF_MONTH, 1);
                        }
                    }
                },
                listener);
    }

//*********************************************************
// private methods
//*********************************************************

    private void runAsyncTask(final Runnable task, final AsyncTaskListener listener)
    {
        mExecutor.execute(new Runnable()
        {
            @Override
            public void run()
            {
                task.run();
                Handler UIThreadHandler = new Handler(Looper.getMainLooper());
                UIThreadHandler.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        listener.onComplete();
                    }
                });
            }
        });
    }
    
    private SleepSessionEntity generateRandomSleepSessionEntity(
            GregorianCalendar baseDay,
            Random rand)
    {
        SleepSessionEntity entity = new SleepSessionEntity();
        // 8pm -> 4am
        entity.startTime = randomStartTime(baseDay, 20, 28, rand);
        entity.duration = randomDurationHours(5, 10, rand) * 60 * 1000;
        return entity;
    }
    
    private Date randomStartTime(
            GregorianCalendar baseDay,
            int minStartHour,
            int maxStartHour,
            Random rand)
    {
        GregorianCalendar result = new GregorianCalendar();
        result.setTimeInMillis(baseDay.getTimeInMillis());
        int randStartTime = randomDurationMillis(
                hourToMillis(minStartHour),
                hourToMillis(maxStartHour),
                rand);
        result.add(Calendar.MILLISECOND, randStartTime);
        return result.getTime();
    }
    
    private Date randomWakeTimeGoal(
            GregorianCalendar baseDay,
            int minStartHour,
            int maxStartHour,
            Random rand)
    {
        GregorianCalendar wakeTimeGoalBaseDay = new GregorianCalendar();
        wakeTimeGoalBaseDay.setTimeInMillis(baseDay.getTimeInMillis());
        // wake time goals occur the next day
        wakeTimeGoalBaseDay.add(Calendar.DAY_OF_MONTH, 1);
        
        return randomStartTime(wakeTimeGoalBaseDay, minStartHour, maxStartHour, rand);
    }
    
    /**
     * assumes min < max
     *
     * @return minutes
     */
    private Integer randomDurationHours(int minHours, int maxHours, Random rand)
    {
        int randMillis = randomDurationMillis(
                hourToMillis(minHours),
                hourToMillis(maxHours),
                rand);
        
        return (randMillis / 1000) / 60;
    }
    
    private Integer randomDurationMillis(int minMillis, int maxMillis, Random rand)
    {
        int diff = maxMillis - minMillis;
        return minMillis + rand.nextInt(diff);
    }
    
    private Integer hourToMillis(int hour)
    {
        return hour * 60 * 60 * 1000;
    }
}
