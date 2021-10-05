/*
 * Copyright (c) 2020-2021 Rich Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.dev_tools;

import android.os.Handler;
import android.os.Looper;

import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleeptarget.data.database.AppDatabase;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_sleepduration.SleepDurationGoalEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.goal_waketime.WakeTimeGoalEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_interruptions.SleepInterruptionEntity;
import com.rbraithwaite.sleeptarget.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleeptarget.test_utils.test_data.builders.DateBuilder;
import com.rbraithwaite.sleeptarget.ui.common.views.mood_selector.TEMP.MoodView;
import com.rbraithwaite.sleeptarget.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.aDate;
import static com.rbraithwaite.sleeptarget.test_utils.test_data.TestData.valueOf;

public class DevToolsFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************
    
    // REFACTOR [21-10-4 12:52AM] -- I should rethink this haha.
    // No repo layer here cause these are just dumb dev tools.
    // Otherwise I would need to implement 'addSleepSessions' in
    // SleepSessionRepository and SleepSessionDao, which isn't ideal
    private AppDatabase mDatabase;
    private Executor mExecutor;
    private GregorianCalendar defaultBaseDay;
    private GregorianCalendar mBaseDay;

//*********************************************************
// private constants
//*********************************************************
    
    private static final long RANDOM_SEED = 123456L;
    
    // TODO [21-06-14 2:15AM] -- I wonder if the unicode formatting problem is due to these characters: ’.
    private static final String[] mTextPool = {
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
            AppDatabase database,
            Executor executor)
    {
        mDatabase = database;
        mExecutor = executor;
        
        defaultBaseDay = new GregorianCalendar();
        new TimeUtils().setCalendarTimeOfDay(defaultBaseDay, 0);
        
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
                        SleepSessionEntity entity = generateRandomSleepSessionEntity(mBaseDay, rand);
                        List<SleepInterruptionEntity> interruptions = generateRandomInterruptionsForSleepSession(entity, rand);
                        List<Integer> NO_TAGS = new ArrayList<>();
                        
                        mDatabase.getSleepSessionDao()
                                .addSleepSessionWithExtras(
                                        entity,
                                        NO_TAGS,
                                        interruptions);
                        
                        // one session per day
                        // having the base day as a stored property ensures subsequent
                        // additions of
                        // sleep sessions are added to the right days
                        mBaseDay.add(Calendar.DAY_OF_MONTH, -1);
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
            int sleepDurationGoalHours = 8; // 8 hrs
            
            mDatabase.getSleepDurationGoalDao().updateSleepDurationGoal(
                    new SleepDurationGoalEntity(
                            goalDate,
                            sleepDurationGoalHours * 60 /* 8 hours in minutes */));
            
            mDatabase.getWakeTimeGoalDao().updateWakeTimeGoal(
                    new WakeTimeGoalEntity(
                            goalDate,
                            wakeTimeGoal * 60 * 60 * 1000 /* 8am waketime goal */));
            
            // REFACTOR [21-06-1 1:11AM] -- consider moving this to a separate tool.
            // Add some sleep sessions that hit the goals.
            // -----------------------------------------------------
            GregorianCalendar today = new GregorianCalendar();
            // set to 12am... uhh there's probably a better way to do this lol
            today = new GregorianCalendar(
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH));
            
            GregorianCalendar yesterday = new GregorianCalendar();
            yesterday.setTime(today.getTime());
            yesterday.add(Calendar.DAY_OF_MONTH, -1);
            
            GregorianCalendar twoDaysAgo = new GregorianCalendar();
            twoDaysAgo.setTime(yesterday.getTime());
            twoDaysAgo.add(Calendar.DAY_OF_MONTH, -1);
            
            TimeUtils timeUtils = new TimeUtils();
            int durationGoalMillis = (int) TimeUtils.hoursToMillis(sleepDurationGoalHours);
    
            // Since the goal success for a day depends on the sleep through that day's night
            // into the next day, the succeeded days appear one day back of the defined days above
            // (since they are initialized to 12am of the next day).
            
            // yesterday will hit both goals (12am - 8am)
            SleepSessionEntity bothGoalsSuccess = new SleepSessionEntity(
                    today.getTime(),
                    timeUtils.addDurationToDate(today.getTime(), durationGoalMillis));
            // 2 days ago will only hit the wake time (2am - 8am)
            SleepSessionEntity wakeTimeGoalSuccess = new SleepSessionEntity(
                    // move the start forward so the duration goal is missed
                    timeUtils.addDurationToDate(yesterday.getTime(),
                                                (int) TimeUtils.hoursToMillis(2)),
                    timeUtils.addDurationToDate(yesterday.getTime(), durationGoalMillis));
            
            // 3 days ago will only hit the duration (10pm - 6am)
            // set this back so that the wake time goal is missed
            twoDaysAgo.add(Calendar.HOUR, -2);
            SleepSessionEntity durationGoalSuccess = new SleepSessionEntity(
                    twoDaysAgo.getTime(),
                    timeUtils.addDurationToDate(twoDaysAgo.getTime(), durationGoalMillis));
            
            mDatabase.getSleepSessionDao().addSleepSession(bothGoalsSuccess);
            mDatabase.getSleepSessionDao().addSleepSession(wakeTimeGoalSuccess);
            mDatabase.getSleepSessionDao().addSleepSession(durationGoalSuccess);
        });
    }
    
    public void crashTheApp()
    {
        throw new RuntimeException("forcing a crash");
    }
    
    public void addChartTargetTestData()
    {
        runAsyncTask(() -> {
            // wake time goals
            // ----------------------------------------------------------
            List<WakeTimeGoalEntity> wakeTimeGoalEntities = new ArrayList<>();
    
            int sixPm = 18 * 60 * 60 * 1000;
            int fiveAm = 5 * 60 * 60 * 1000;
            int eightAm = 8 * 60 * 60 * 1000;
            
            DateBuilder date = aDate().withValue(2021, 9, 19, 12, 34);
            
            wakeTimeGoalEntities.add(new WakeTimeGoalEntity(valueOf(date), sixPm));
            date.addDays(1);
            wakeTimeGoalEntities.add(new WakeTimeGoalEntity(valueOf(date), eightAm));
            date.addDays(1);
            wakeTimeGoalEntities.add(new WakeTimeGoalEntity(valueOf(date), sixPm));
            date.addDays(2);
            wakeTimeGoalEntities.add(new WakeTimeGoalEntity(valueOf(date), WakeTimeGoalEntity.NO_GOAL));
            
            date.withValue(2021, 9, 26, 12, 34);
            wakeTimeGoalEntities.add(new WakeTimeGoalEntity(valueOf(date), eightAm));
    
            for (WakeTimeGoalEntity entity : wakeTimeGoalEntities) {
                mDatabase.getWakeTimeGoalDao().updateWakeTimeGoal(entity);
            }
    
            // sleep duration goals
            // ----------------------------------------------------------
            List<SleepDurationGoalEntity> sleepDurationGoalEntities = new ArrayList<>();
    
            date = aDate().now().subtractDays(8);
    
            int fiveHours = 5 * 60;
            int eightHours = 8 * 60;
    
            sleepDurationGoalEntities.add(new SleepDurationGoalEntity(valueOf(date), SleepDurationGoalEntity.NO_GOAL));
            date.addDays(2);
            
            sleepDurationGoalEntities.add(new SleepDurationGoalEntity(valueOf(date), eightHours));
            date.addDays(3);
    
            sleepDurationGoalEntities.add(new SleepDurationGoalEntity(valueOf(date), fiveHours));
            date.addDays(1);
    
            sleepDurationGoalEntities.add(new SleepDurationGoalEntity(valueOf(date), SleepDurationGoalEntity.NO_GOAL));
            
            for (SleepDurationGoalEntity entity : sleepDurationGoalEntities) {
                mDatabase.getSleepDurationGoalDao().updateSleepDurationGoal(entity);
            }
        });
    }
    
    public void clearDataThenAddPromoData(AsyncTaskListener listener)
    {
        clearData(() -> runAsyncTask(this::addPromoData, listener));
    }
    
    public void clearDataThenAddPromoCalData()
    {
        clearData(() -> runAsyncTask(this::addPromoCalData));
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
    
    // REFACTOR [21-10-4 7:43PM] -- This is similar to maybeInitHistoricalGoalData().
    private void addPromoCalData()
    {
        Date goalDate = new GregorianCalendar(2020, 10, 11).getTime();
        int wakeTimeGoal = 8; // 8 am
        int sleepDurationGoalHours = 8; // 8 hrs
        mDatabase.getSleepDurationGoalDao().updateSleepDurationGoal(
                new SleepDurationGoalEntity(
                        goalDate,
                        sleepDurationGoalHours * 60 /* 8 hours in minutes */));
        mDatabase.getWakeTimeGoalDao().updateWakeTimeGoal(
                new WakeTimeGoalEntity(
                        goalDate,
                        wakeTimeGoal * 60 * 60 * 1000 /* 8am waketime goal */));
        
        Random rand = new Random();
        final long SEED = 654321;
        rand.setSeed(SEED);
        
        final float CHANCE_WAKETIME_SUCCESS = 0.9f;
        final float CHANCE_DURATION_SUCCESS = 0.9f;
        
        final int SESSION_COUNT = 100;
        DateBuilder date = aDate().now().atMidnight();
        for (int i = 0; i < SESSION_COUNT; i ++) {
            boolean shouldWakeTimeFail = rand.nextFloat() > CHANCE_WAKETIME_SUCCESS;
            boolean shouldDurationFail = rand.nextFloat() > CHANCE_DURATION_SUCCESS;
            
            Date entityStart = valueOf(date);
            Date entityEnd = valueOf(date.copy().addHours(8));
//            if (shouldWakeTimeFail) {
//                entityStart = valueOf(date.copy().addHours(1));
//                entityEnd = valueOf(date.copy().addHours(9));
//            }
//            if (shouldDurationFail) {
//                entityStart = valueOf(date.copy().addHours(3));
//            }
            
            SleepSessionEntity entity = new SleepSessionEntity(entityStart, entityEnd);
            mDatabase.getSleepSessionDao().addSleepSession(entity);
            
            date.subtractDays(1);
        }
    }
    
    private void addPromoData()
    {
        Random rand = new Random();
        long PROMO_SEED = 654321;
        rand.setSeed(PROMO_SEED); // keep the random data deterministic
        
         // historical sleep session data
        // --------------------------------------------------
        // REFACTOR [21-10-4 1:21AM] -- This duplicates a lot of addArbitrarySleepSessions().
        int SESSION_AMOUNT = 100;
        for (int i = 0; i < SESSION_AMOUNT; i++) {
            SleepSessionEntity entity = generateRandomSleepSessionEntity(
                    mBaseDay,
                    rand,
                    22f, // 10pm
                    25.5f, // 1:30am
                    7f,
                    9.6f);
            entity.rating = randomRating(
                    rand,
                    4.5f,
                    3.5f,
                    0.33f);
            List<SleepInterruptionEntity> interruptions = generateRandomInterruptionsForSleepSession(
                    entity,
                    rand,
                    10,
                    0.1f);
            List<Integer> NO_TAGS = new ArrayList<>();
        
            mDatabase.getSleepSessionDao()
                    .addSleepSessionWithExtras(
                            entity,
                            NO_TAGS,
                            interruptions);
            
            mBaseDay.add(Calendar.DAY_OF_MONTH, -1);
        }
        
        // historical target data
        // --------------------------------------------------
        // REFACTOR [21-10-4 5:57PM] -- This sort of duplicates addChartTargetTestData()
        // wake time
        List<WakeTimeGoalEntity> wakeTimeGoalEntities = new ArrayList<>();
        
        int wakeTimeTarget1 = 5 * 60 * 60 * 1000;
        int wakeTimeTarget2 = 7 * 60 * 60 * 1000;
    
        DateBuilder date = aDate().withValue(2021, 9, 14, 12, 34);
        wakeTimeGoalEntities.add(new WakeTimeGoalEntity(valueOf(date), wakeTimeTarget1));
    
        date.withValue(2021, 9, 17, 12, 34);
        wakeTimeGoalEntities.add(new WakeTimeGoalEntity(valueOf(date), wakeTimeTarget2));
    
        for (WakeTimeGoalEntity entity : wakeTimeGoalEntities) {
            mDatabase.getWakeTimeGoalDao().updateWakeTimeGoal(entity);
        }
        
        // duration
        List<SleepDurationGoalEntity> sleepDurationGoalEntities = new ArrayList<>();
    
        int fiveHours = 5 * 60;
        int eightHours = 8 * 60 + 30;
    
        date = aDate().withValue(2021, 8, 28, 12, 34);
        sleepDurationGoalEntities.add(new SleepDurationGoalEntity(valueOf(date), eightHours));
    
//        date.withValue(2021, 9, 26, 12, 34);
//        sleepDurationGoalEntities.add(new SleepDurationGoalEntity(valueOf(date), eightHours));
    
        for (SleepDurationGoalEntity entity : sleepDurationGoalEntities) {
            mDatabase.getSleepDurationGoalDao().updateSleepDurationGoal(entity);
        }
    }
    
    private List<SleepInterruptionEntity> generateRandomInterruptionsForSleepSession(
            SleepSessionEntity sleepSession, Random rand)
    {
        return generateRandomInterruptionsForSleepSession(sleepSession, rand, 5, 0.5f);
    }
    
    /**
     * Generates SleepInterruptionEntity instances within the start/end bounds of the sleep session.
     * Basic algo: The sleep session duration is split into equal time blocks based on splitCount,
     * then each time block has interruptionChance chance of containing an interruption.
     *
     * @param sleepSession The sleep session used to bound the interruptions
     * @param rand Used for interruptionChance
     * @param splitCount The number of splits in the sleep session time block
     * @param interruptionChance The chance for a single split to contain an interruption.
     * @return The interruptions.
     */
    private List<SleepInterruptionEntity> generateRandomInterruptionsForSleepSession(
            SleepSessionEntity sleepSession,
            Random rand,
            int splitCount,
            float interruptionChance)
    {
        List<SleepInterruptionEntity> result = new ArrayList<>();
    
        int durationBlockWidth = (int) (sleepSession.duration / splitCount);
    
        for (int i = 0; i < splitCount; i++) {
            float chance = rand.nextFloat();
            if (chance > interruptionChance) {
                continue;
            }
            SleepInterruptionEntity interruptionEntity = new SleepInterruptionEntity();
            interruptionEntity.sessionId = sleepSession.id;
            interruptionEntity.startTime = new TimeUtils().addDurationToDate(sleepSession.startTime, i * durationBlockWidth);
            interruptionEntity.durationMillis = durationBlockWidth;
            interruptionEntity.reason = randomText(rand);
        
            result.add(interruptionEntity);
        }
    
        return result;
    }
    
    private SleepSessionEntity generateRandomSleepSessionEntity(
            GregorianCalendar baseDay,
            Random rand)
    {
        return generateRandomSleepSessionEntity(
                baseDay,
                rand,
                20, // 8pm
                28,  // 4 am
                5,
                10);
    }
    
    private SleepSessionEntity generateRandomSleepSessionEntity(
            GregorianCalendar baseDay,
            Random rand,
            float minStartHour, float maxStartHour, float minDurationHours, float maxDurationHours)
    {
        SleepSessionEntity entity = new SleepSessionEntity();
        entity.startTime = randomStartTime(baseDay, minStartHour, maxStartHour, rand);
        entity.duration = randomDurationFromHours(minDurationHours, maxDurationHours, rand) * 60 * 1000;
        entity.endTime = new TimeUtils().addDurationToDate(entity.startTime, (int) entity.duration);
        entity.moodIndex = randomMoodIndex(rand);
        entity.additionalComments = randomText(rand);
        entity.rating = randomRating(rand);
        return entity;
    }
    
    private int randomMoodIndex(Random rand)
    {
        return rand.nextInt(MoodView.getMoodCount());
    }
    
    private String randomText(Random rand)
    {
        return mTextPool[rand.nextInt(mTextPool.length)];
    }
    
    private float randomRating(Random rand)
    {
        return (float) Math.floor(rand.nextFloat() * 5f);
    }
    
    private float randomRating(
            Random rand,
            float minRating,
            float maxRating,
            float noRatingChance)
    {
        // TODO [21-10-4 2:52PM] -- pre-cond for max > min.
        
        float ratingChance = rand.nextFloat();
        if (ratingChance < noRatingChance) {
            return 0f;
        }
    
        minRating = Math.max(minRating, 0.5f); // not 0, since that's controlled by noRatingChance
        maxRating = Math.min(maxRating, 5f);
        
        float diff = maxRating - minRating;
        float ratio = rand.nextFloat();
        float rawRating = minRating + (diff * ratio);
        
        // clamp the raw rating to 0.5 increments
        double ceil = Math.ceil(rawRating);
        double floor = Math.floor(rawRating);
        double midPoint = floor + 0.5;
        double diffToCeil = ceil - rawRating;
        double diffToFloor = rawRating - floor;
        double diffToMidPoint = Math.abs(rawRating - midPoint);
        
        return diffToCeil < diffToFloor ?
                (diffToCeil < diffToMidPoint ? (float) ceil : (float) midPoint) :
                (diffToFloor < diffToMidPoint ? (float) floor : (float) midPoint);
    }
    
    private Date randomStartTime(
            GregorianCalendar baseDay,
            float minStartHour,
            float maxStartHour,
            Random rand)
    {
        GregorianCalendar result = new GregorianCalendar();
        result.setTimeInMillis(baseDay.getTimeInMillis());
        int randStartTime = randomDurationMillis(
                hoursToMillis(minStartHour),
                hoursToMillis(maxStartHour),
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
    private Integer randomDurationFromHours(float minHours, float maxHours, Random rand)
    {
        int randMillis = randomDurationMillis(
                hoursToMillis(minHours),
                hoursToMillis(maxHours),
                rand);
        
        return (randMillis / 1000) / 60;
    }
    
    private Integer randomDurationMillis(int minMillis, int maxMillis, Random rand)
    {
        int diff = maxMillis - minMillis;
        return minMillis + rand.nextInt(diff);
    }
    
    private Integer hoursToMillis(float hours)
    {
        return ((int) (hours * 60 * 60)) * 1000;
    }
}
