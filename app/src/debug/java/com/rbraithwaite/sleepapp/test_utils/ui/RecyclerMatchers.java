package com.rbraithwaite.sleepapp.test_utils.ui;

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
