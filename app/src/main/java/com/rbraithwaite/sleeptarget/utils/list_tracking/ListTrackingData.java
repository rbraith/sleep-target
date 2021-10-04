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

package com.rbraithwaite.sleeptarget.utils.list_tracking;

import java.util.List;
import java.util.Objects;

public class ListTrackingData<T>
{
//*********************************************************
// private constants
//*********************************************************

    private final long mChangeId;

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
        
        @Override
        public int hashCode()
        {
            int result = elem != null ? elem.hashCode() : 0;
            result = 31 * result + index;
            result = 31 * result + changeType.hashCode();
            return result;
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }
            
            ListChange<?> that = (ListChange<?>) o;
            
            if (index != that.index) { return false; }
            if (changeType != that.changeType) { return false; }
            // elem comparison is potentially the heaviest, so do that last
            return Objects.equals(elem, that.elem);
        }
    }

//*********************************************************
// constructors
//*********************************************************

    public ListTrackingData(
            long changeId,
            List<T> list,
            ListChange<T> lastChange)
    {
        // changeId is used to help optimize comparisons - see equals(), see changeId generation
        // in ListTrackingLiveData. It's unlikely but possible that 2 instances of ListTrackingData
        // share the same id due to overflow.
        mChangeId = changeId;
        this.list = list;
        this.lastChange = lastChange;
    }
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    public int hashCode()
    {
        int result = list.hashCode();
        result = 31 * result + (lastChange != null ? lastChange.hashCode() : 0);
        result = 31 * result + (int) (mChangeId ^ (mChangeId >>> 32));
        return result;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        
        ListTrackingData<?> that = (ListTrackingData<?>) o;
        
        // changeId is compared first, to speed up comparison
        if (mChangeId != that.mChangeId) { return false; }
        if (Objects.equals(lastChange, that.lastChange)) { return false; }
        // list comparison is likely the heaviest, so compare the lists last
        return list.equals(that.list);
    }
}
