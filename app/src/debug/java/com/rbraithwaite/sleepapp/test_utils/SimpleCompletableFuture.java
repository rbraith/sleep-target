package com.rbraithwaite.sleepapp.test_utils;

import com.rbraithwaite.sleepapp.utils.TimeUtils;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// It was a mistake to implement this interface lol, this is way overkill for what I needed.
// I should have just made my own super basic one with get() and complete(), but oh well the
// work is done.


/**
 * This is because apparently CompletableFuture is >= API 24 :( https://developer.android
 * .com/reference/java/util/concurrent/CompletableFuture
 */
public class SimpleCompletableFuture<T>
        implements Future<T>
{
//*********************************************************
// private properties
//*********************************************************

    private volatile boolean mIsCancelled = false;
    private volatile boolean mIsDone = false;
    private volatile boolean mGetWasCalled = false;
    private volatile T mValue;

//*********************************************************
// overrides
//*********************************************************

    @Override
    public boolean cancel(boolean mayInterruptIfRunning)
    {
        // idk if i implemented this right lol
        
        // "This attempt will fail if the task has already completed, has already been cancelled,
        // or could not be cancelled for some other reason."
        if (mIsDone || mIsCancelled) {
            return false;
        }
        // "If the task has already started, then the mayInterruptIfRunning parameter determines
        // whether the thread executing this task should be interrupted in an attempt to stop the
        // task."
        // TODO [20-11-20 1:20AM] -- idk what to do with this :/ - what task, what thread? those
        //  things don't make sense for a completable future.
        
        // "After this method returns, subsequent calls to isDone() will always return true"
        mIsDone = true;
        // " Subsequent calls to isCancelled() will always return true if this method returned true"
        mIsCancelled = true;
        return true;
    }
    
    @Override
    public boolean isCancelled()
    {
        return mIsCancelled;
    }
    
    @Override
    public boolean isDone()
    {
        return mIsDone;
    }
    
    @Override
    public T get() throws ExecutionException, InterruptedException
    {
        mGetWasCalled = true;
        while (!mIsDone) {
            if (mIsCancelled) {
                throw new CancellationException("cancelled");
            }
        }
        return mValue;
    }
    
    @Override
    public T get(long timeout, TimeUnit unit) throws
            ExecutionException,
            InterruptedException,
            TimeoutException
    {
        mGetWasCalled = true;
        // HACK [20-11-20 1:01AM] -- there is definitely a better way to track the timeout.
        long timeoutMillis = unit.toMillis(timeout);
        long elapsedTimeMillis = 0;
        long startTime = TimeUtils.getNow().getTime();
        while (!mIsDone && (elapsedTimeMillis < timeoutMillis)) {
            if (mIsCancelled) {
                throw new CancellationException("cancelled");
            }
            elapsedTimeMillis = TimeUtils.getNow().getTime() - startTime;
        }
        if (mIsDone) {
            return mValue;
        } else {
            mIsDone = true;
            throw new TimeoutException("timed out");
        }
    }

//*********************************************************
// api
//*********************************************************

    public synchronized void complete(T value)
    {
        // TODO [20-11-20 5:44PM] -- wrap this in 'if (!mIsDone) {...}' ??
        //  maybe have it return a bool - false if already was done?
        mValue = value;
        mIsDone = true;
    }
}
