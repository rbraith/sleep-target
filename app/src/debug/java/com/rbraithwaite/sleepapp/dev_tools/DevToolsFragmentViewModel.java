package com.rbraithwaite.sleepapp.dev_tools;

import android.os.Handler;
import android.os.Looper;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.database.SleepAppDatabase;
import com.rbraithwaite.sleepapp.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

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
    
    // TODO [21-06-14 2:15AM] -- I wonder if the unicode formatting problem is due to these characters: ’.
    private static final String[] mAdditionalCommentsPool = {
            "What the fuck did you just fucking say about me, you little bitch?",
            "I’ll have you know I graduated top of my class in the Navy Seals, and I’ve been " +
            "involved in numerous secret raids on Al-Quaeda, and I have over 300 confirmed kills.",
            "I am trained in gorilla warfare and I’m the top sniper in the entire US armed forces.",
            "You are nothing to me but just another target.",
            "I will wipe you the fuck out with precision the likes of which has never been seen " +
            "before on this Earth, mark my fucking words.",
            "You think you can get away with saying that shit to me over the Internet? Think " +
            "again, fucker.",
            "As we speak I am contacting my secret network of spies across the USA and your IP is" +
            " being traced right now so you better prepare for the storm, maggot.",
            " The storm that wipes out the pathetic little thing you call your life.",
            "You’re fucking dead, kid. I can be anywhere, anytime, and I can kill you in over " +
            "seven hundred ways, and that’s just with my bare hands.",
            "Not only am I extensively trained in unarmed combat, but I have access to the entire" +
            " arsenal of the United States Marine Corps and I will use it to its full extent to " +
            "wipe your miserable ass off the face of the continent, you little shit.",
            "If only you could have known what unholy retribution your little “clever” comment " +
            "was about to bring down upon you, maybe you would have held your fucking tongue.",
            "But you couldn’t, you didn’t, and now you’re paying the price, you goddamn idiot. I " +
            "will shit fury all over you and you will drown in it. You’re fucking dead, kiddo. ",
    };

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
                () -> {
                    mDatabase.clearAllTables();
                    mBaseDay.setTimeInMillis(defaultBaseDay.getTimeInMillis());
                },
                listener);
    }
    
    public void addArbitrarySleepSessions(final int sessionAmount, final AsyncTaskListener listener)
    {
        runAsyncTask(
                () -> {
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
                },
                listener);
    }
    
    public void maybeInitHistoricalGoalData()
    {
        runAsyncTask(() -> {
            // For now, only having 1 historical value. This is hopefully enough to have the
            // random generated sleep sessions succeed at least sometimes. Its important that
            // the goals be set before the first generated sleep session, otherwise this data is
            // arbitrary.
            Date goalDate = new GregorianCalendar(2019, 10, 11).getTime();
            int wakeTimeGoal = 8; // 8 am
            int sleepDurationGoal = 8; // 8 hrs
            
            mDatabase.getSleepDurationGoalDao().updateSleepDurationGoal(
                    new SleepDurationGoalEntity(
                            goalDate,
                            sleepDurationGoal * 60 /* 8 hours in minutes */));
            
            mDatabase.getWakeTimeGoalDao().updateWakeTimeGoal(
                    new WakeTimeGoalEntity(
                            goalDate,
                            wakeTimeGoal * 60 * 60 * 1000 /* 8am waketime goal */));
            
            // REFACTOR [21-06-1 1:11AM] -- consider moving this to a separate tool.
            // add some sleep sessions that hit the goals
            // -----------------------------------------------------
            // today will hit both goals
            GregorianCalendar today = new GregorianCalendar();
            // set to 12am... uhh there's probably a better way to do this lol
            today = new GregorianCalendar(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH));
            
            // yesterday will only hit waketime goal
            GregorianCalendar yesterday = new GregorianCalendar();
            yesterday.setTime(today.getTime());
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            
            // 2 days ago will only hit duration goal
            GregorianCalendar two_days_ago = new GregorianCalendar();
            two_days_ago.setTime(yesterday.getTime());
            two_days_ago.add(Calendar.DAY_OF_MONTH, -1);
            
            TimeUtils timeUtils = new TimeUtils();
            int durationGoalMillis = (int) timeUtils.hoursToMillis(sleepDurationGoal);
            
            SleepSessionEntity bothGoalsSuccess = new SleepSessionEntity(
                    today.getTime(),
                    timeUtils.addDurationToDate(today.getTime(), durationGoalMillis));
            
            SleepSessionEntity wakeTimeGoalSuccess = new SleepSessionEntity(
                    // move the start forward so the duration goal is missed
                    timeUtils.addDurationToDate(yesterday.getTime(),
                                                (int) timeUtils.hoursToMillis(2)),
                    timeUtils.addDurationToDate(yesterday.getTime(), durationGoalMillis));
            
            // set this back so that the wake time goal is missed
            two_days_ago.add(Calendar.HOUR, -2);
            SleepSessionEntity durationGoalSuccess = new SleepSessionEntity(
                    two_days_ago.getTime(),
                    timeUtils.addDurationToDate(two_days_ago.getTime(), durationGoalMillis));
            
            mDatabase.getSleepSessionDao().addSleepSession(bothGoalsSuccess);
            mDatabase.getSleepSessionDao().addSleepSession(wakeTimeGoalSuccess);
            mDatabase.getSleepSessionDao().addSleepSession(durationGoalSuccess);
        });
    }

//*********************************************************
// private methods
//*********************************************************
    
    private void runAsyncTask(final Runnable task, final AsyncTaskListener listener)
    {
        mExecutor.execute(() -> {
            task.run();
            if (listener != null) {
                Handler UIThreadHandler = new Handler(Looper.getMainLooper());
                UIThreadHandler.post(listener::onComplete);
            }
        });
    }
    
    private void runAsyncTask(Runnable task)
    {
        runAsyncTask(task, null);
    }
    
    private SleepSessionEntity generateRandomSleepSessionEntity(
            GregorianCalendar baseDay,
            Random rand)
    {
        SleepSessionEntity entity = new SleepSessionEntity();
        // 8pm -> 4am
        entity.startTime = randomStartTime(baseDay, 20, 28, rand);
        entity.duration = randomDurationHours(5, 10, rand) * 60 * 1000;
        entity.endTime = new TimeUtils().addDurationToDate(entity.startTime, (int) entity.duration);
        entity.moodIndex = randomMoodIndex(rand);
        entity.additionalComments = randomComments(rand);
        entity.rating = randomRating(rand);
        return entity;
    }
    
    private int randomMoodIndex(Random rand)
    {
        return rand.nextInt(MoodView.getMoodCount());
    }
    
    private String randomComments(Random rand)
    {
        return mAdditionalCommentsPool[rand.nextInt(mAdditionalCommentsPool.length)];
    }
    
    private float randomRating(Random rand)
    {
        return (float) Math.floor(rand.nextFloat() * 5f);
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
