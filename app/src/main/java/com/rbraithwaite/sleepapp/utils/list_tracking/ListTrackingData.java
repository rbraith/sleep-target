package com.rbraithwaite.sleepapp.utils.list_tracking;

import java.util.List;

public class ListTrackingData<T>
{
//*********************************************************
// public constants
//*********************************************************

    public final List<T> list;
    public final ListChange<T> lastChange;

//*********************************************************
// public helpers
//*********************************************************

    public enum ChangeType
    {
        ADDED,
        DELETED,
        MODIFIED
    }
    
    public static class ListChange<T>
    {
        public final T elem;
        public final int index;
        public final ChangeType changeType;
        
        public ListChange(
                T elem,
                int index,
                ChangeType changeType)
        {
            this.elem = elem;
            this.index = index;
            this.changeType = changeType;
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public ListTrackingData(
            List<T> list,
            ListChange<T> lastChange)
    {
        this.list = list;
        this.lastChange = lastChange;
    }
}
