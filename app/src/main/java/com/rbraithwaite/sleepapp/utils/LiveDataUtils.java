package com.rbraithwaite.sleepapp.utils;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

public class LiveDataUtils
{
//*********************************************************
// public helpers
//*********************************************************

    
    /**
     * Used with {@link LiveDataUtils#merge(LiveData, LiveData, Merger)} to apply the merging of the
     * 2 provided values into a new type.
     */
    public interface Merger<A, B, C>
    {
        C applyMerge(A a, B b);
    }

//*********************************************************
// constructors
//*********************************************************

    private LiveDataUtils() {/* No instantiation */}

//*********************************************************
// api
//*********************************************************

    public static <T> void refresh(MutableLiveData<T> liveData)
    {
        liveData.setValue(liveData.getValue());
    }
    
    public static <T> MutableLiveData<T> lazyInitMutable(MutableLiveData<T> mutable, T initialValue)
    {
        return mutable == null ? new MutableLiveData<>(initialValue) : mutable;
    }
    
    /**
     * Lazy init with no initial value for the MutableLiveData.
     */
    public static <T> MutableLiveData<T> lazyInitMutable(MutableLiveData<T> mutable)
    {
        return mutable == null ? new MutableLiveData<T>() : mutable;
    }
    
    /**
     * Merge the values of 2 LiveData instances into a new LiveData type. Same behavioural rules
     * apply as {@link Transformations#switchMap(LiveData, Function)} (value isn't computed until
     * observed, etc)
     */
    public static <A, B, C> LiveData<C> merge(
            LiveData<A> a,
            final LiveData<B> b,
            final Merger<A, B, C> merger)
    {
        // idea from https://stackoverflow.com/a/57819928
        return Transformations.switchMap(
                a,
                new Function<A, LiveData<C>>()
                {
                    @Override
                    public LiveData<C> apply(final A inputA)
                    {
                        return Transformations.map(
                                b,
                                new Function<B, C>()
                                {
                                    @Override
                                    public C apply(B inputB)
                                    {
                                        return merger.applyMerge(inputA, inputB);
                                    }
                                });
                    }
                });
    }
    
    // IDEA [21-02-7 1:32AM] -- mergeMany(LiveData<?>...)
    //  impl: cascade w/ merge() - the result type for all but the last merge is a List<Object> or
    //  something - so the intermediate merges happen like this: List<Object> + SomeType =
    //  List<Object>,
    //  its like a LiveData value collector.
    //  The last merge is an interface where the client takes the final List<Object> and merges it
    //  down into some return type. It wouldn't be ideal to require the client to cast back their
    //  supplied types, but it might be necessary?
}
