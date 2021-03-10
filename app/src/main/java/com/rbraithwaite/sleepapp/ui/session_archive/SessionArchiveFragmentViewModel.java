package com.rbraithwaite.sleepapp.ui.session_archive;

import androidx.arch.core.util.Function;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.rbraithwaite.sleepapp.data.current_goals.CurrentGoalsRepository;
import com.rbraithwaite.sleepapp.data.current_goals.SleepDurationGoalModel;
import com.rbraithwaite.sleepapp.data.current_goals.WakeTimeGoalModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionModel;
import com.rbraithwaite.sleepapp.data.sleep_session.SleepSessionRepository;
import com.rbraithwaite.sleepapp.ui.UIDependenciesModule;
import com.rbraithwaite.sleepapp.ui.format.DateTimeFormatter;
import com.rbraithwaite.sleepapp.ui.format.DurationFormatter;
import com.rbraithwaite.sleepapp.ui.session_archive.data.SessionArchiveListItem;
import com.rbraithwaite.sleepapp.ui.session_data.data.SleepSessionWrapper;
import com.rbraithwaite.sleepapp.utils.LiveDataUtils;
import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.Date;
import java.util.List;

public class SessionArchiveFragmentViewModel
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private SleepSessionRepository mSleepSessionRepository;
    private CurrentGoalsRepository mCurrentGoalsRepository;
    private DateTimeFormatter mDateTimeFormatter;
    private TimeUtils mTimeUtils;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "SessionArchiveFragViewMod";

//*********************************************************
// constructors
//*********************************************************

    @ViewModelInject
    public SessionArchiveFragmentViewModel(
            SleepSessionRepository sleepSessionRepository,
            CurrentGoalsRepository currentGoalsRepository,
            @UIDependenciesModule.SessionArchiveDateTimeFormatter DateTimeFormatter dateTimeFormatter)
    {
        mCurrentGoalsRepository = currentGoalsRepository;
        mSleepSessionRepository = sleepSessionRepository;
        mDateTimeFormatter = dateTimeFormatter;
        mTimeUtils = createTimeUtils();
    }

//*********************************************************
// api
//*********************************************************

    public void addSleepSession(SleepSessionWrapper sleepSession)
    {
        mSleepSessionRepository.addSleepSession(sleepSession.getModel());
    }
    
    public void updateSleepSession(SleepSessionWrapper sleepSession)
    {
        mSleepSessionRepository.updateSleepSession(sleepSession.getModel());
    }
    
    public int deleteSession(SleepSessionWrapper sessionToDelete)
    {
        int id = sessionToDelete.getModel().getId();
        mSleepSessionRepository.deleteSleepSession(id);
        return id;
    }
    
    public LiveData<SessionArchiveListItem> getListItemData(int id)
    {
        // convert from db form to ui form
        return Transformations.map(
                mSleepSessionRepository.getSleepSession(id),
                new Function<SleepSessionModel, SessionArchiveListItem>()
                {
                    @Override
                    public SessionArchiveListItem apply(SleepSessionModel input)
                    {
                        return convertSleepSessionToListItem(input);
                    }
                });
    }
    
    public LiveData<List<Integer>> getAllSleepSessionIds()
    {
        return mSleepSessionRepository.getAllSleepSessionIds();
    }
    
    public LiveData<SleepSessionWrapper> getInitialAddSessionData()
    {
        // REFACTOR [21-01-5 9:14PM] -- consider making this lazy init & storing the value in a
        //  field (avoid re-instantiation of the mapped LiveData?)
        // LiveData retval & mapping are needed because the wake-time & sleep duration goals are
        // returned from the repository asynchronously.
        return LiveDataUtils.merge(
                mCurrentGoalsRepository.getWakeTimeGoal(),
                mCurrentGoalsRepository.getSleepDurationGoal(),
                new LiveDataUtils.Merger<WakeTimeGoalModel, SleepDurationGoalModel,
                        SleepSessionWrapper>()
                {
                    @Override
                    public SleepSessionWrapper applyMerge(
                            WakeTimeGoalModel wakeTimeGoal,
                            SleepDurationGoalModel sleepDurationGoal)
                    {
                        Date wakeTimeGoalDate = null;
                        if (wakeTimeGoal != null && wakeTimeGoal.isSet()) {
                            wakeTimeGoalDate = wakeTimeGoal.asDate();
                        }
                        
                        return new SleepSessionWrapper(
                                new SleepSessionModel(
                                        mTimeUtils.getNow(),
                                        0,
                                        // REFACTOR [21-03-8 10:59PM] -- change this to take the
                                        //  wake-time goal model instead.
                                        wakeTimeGoalDate,
                                        sleepDurationGoal));
                    }
                });
    }
    
    public LiveData<SleepSessionWrapper> getSleepSession(int id)
    {
        return Transformations.map(
                mSleepSessionRepository.getSleepSession(id),
                new Function<SleepSessionModel, SleepSessionWrapper>()
                {
                    @Override
                    public SleepSessionWrapper apply(SleepSessionModel input)
                    {
                        return new SleepSessionWrapper(input);
                    }
                });
    }

//*********************************************************
// protected api
//*********************************************************

    // REFACTOR [21-03-5 12:52AM] -- consider ctor injecting this instead? the main reason I'm using
    //  a factory method here is so that I didn't need to update the test class.
    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }


//*********************************************************
// private methods
//*********************************************************

    // REFACTOR [20-11-15 3:54PM] -- consider extracting this method?
    private SessionArchiveListItem convertSleepSessionToListItem(SleepSessionModel sleepSession)
    {
        if (sleepSession == null) {
            return null;
        }
        
        SleepDurationGoalModel sleepDurationGoal = sleepSession.getSleepDurationGoal();
        return SessionArchiveListItem.create(
                mDateTimeFormatter.formatFullDate(sleepSession.getStart()),
                mDateTimeFormatter.formatFullDate(sleepSession.getEnd()),
                // REFACTOR [21-01-13 2:06AM] -- inject this.
                new DurationFormatter().formatDurationMillis(sleepSession.getDuration()),
                (sleepSession.getWakeTimeGoal() != null),
                (sleepDurationGoal != null && sleepDurationGoal.isSet()));
    }
}
