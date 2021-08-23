/*
 * Copyright (c) 2020-2021 Richard Braithwaite (rbraithwaite.dev@gmail.com)
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

package com.rbraithwaite.sleeptarget.test_utils.ui;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class RecyclerMatchers
{
//*********************************************************
// public helpers
//*********************************************************

    public static class ListItemMatcherFactory
    {
        private int mItemPosition;
        
        public ListItemMatcherFactory(int itemPosition)
        {
            mItemPosition = itemPosition;
        }
        
        public Matcher<View> matching(Matcher<View> matcher)
        {
            return new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
            {
                @Override
                protected boolean matchesSafely(RecyclerView recycler)
                {
                    RecyclerView.ViewHolder viewHolder =
                            recycler.findViewHolderForAdapterPosition(mItemPosition);
                    return viewHolder != null && matcher.matches(viewHolder.itemView);
                }
                
                @Override
                public void describeTo(Description description)
                {
                    description.appendText(
                            "with item at position " + mItemPosition + " matching: ");
                    matcher.describeTo(description);
                }
            };
        }
    }
    
//*********************************************************
// constructors
//*********************************************************

    private RecyclerMatchers() {/* No instantiation */}
    
//*********************************************************
// api
//*********************************************************

    public static ListItemMatcherFactory withItemAt(int position)
    {
        return new ListItemMatcherFactory(position);
    }
    
    /**
     * Match a RecyclerView's items against the provided list.
     */
    public static Matcher<View> withItems(Matcher<View>... itemMatchers)
    {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
        {
            @Override
            protected boolean matchesSafely(RecyclerView recycler)
            {
                if (recycler.getAdapter().getItemCount() != itemMatchers.length) { return false; }
                for (int i = 0; i < itemMatchers.length; i++) {
                    if (!(withItemAt(i).matching(itemMatchers[i]).matches(recycler))) {
                        return false;
                    }
                }
                return true;
            }
            
            @Override
            public void describeTo(Description description)
            {
                description.appendText("with recycler items matching: ");
                for (Matcher<View> m : itemMatchers) {
                    m.describeTo(description);
                }
            }
        };
    }
}
