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

package com.rbraithwaite.sleeptarget.test_utils;

import com.rbraithwaite.sleeptarget.utils.TimeUtils;

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
        TimeUtils timeUtils = createTimeUtils();
        // HACK [20-11-20 1:01AM] -- there is definitely a better way to track the timeout.
        long timeoutMillis = unit.toMillis(timeout);
        long elapsedTimeMillis = 0;
        long startTime = timeUtils.getNow().getTime();
        while (!mIsDone && (elapsedTimeMillis < timeoutMillis)) {
            if (mIsCancelled) {
                throw new CancellationException("cancelled");
            }
            elapsedTimeMillis = timeUtils.getNow().getTime() - startTime;
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

//*********************************************************
// protected api
//*********************************************************

    protected TimeUtils createTimeUtils()
    {
        return new TimeUtils();
    }
}
