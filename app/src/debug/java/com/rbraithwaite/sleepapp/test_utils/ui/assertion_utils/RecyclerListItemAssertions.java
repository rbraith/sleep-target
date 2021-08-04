package com.rbraithwaite.sleepapp.test_utils.ui.assertion_utils;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class RecyclerListItemAssertions
{
//*********************************************************
// private properties
//*********************************************************

    private int mListItemIndex;
    private int mRecyclerId;
    
//*********************************************************
// constructors
//*********************************************************

    public RecyclerListItemAssertions(int listItemIndex, int recyclerId)
    {
        mListItemIndex = listItemIndex;
        mRecyclerId = recyclerId;
    }
    
//*********************************************************
// protected api
//*********************************************************

    protected void checkThatThisListItemHasContentsMatching(Matcher<View> listItemContentsMatcher)
    {
        onView(withId(mRecyclerId))
                .check(listItemAt(mListItemIndex).matches(hasDescendant(listItemContentsMatcher)));
    }
    
//*********************************************************
// private methods
//*********************************************************

    private ListItemMatchAssertion listItemAt(int index)
    {
        return new ListItemMatchAssertion(index);
    }
    
//*********************************************************
// private helpers
//*********************************************************

    // https://stackoverflow.com/a/34795431
    private static class ListItemMatchAssertion
    {
        private int mListItemIndex;
        
        public ListItemMatchAssertion(int listItemIndex)
        {
            mListItemIndex = listItemIndex;
        }
        
        public ViewAssertion matches(Matcher<View> listItemMatcher)
        {
            return ViewAssertions.matches(new BoundedMatcher<View, RecyclerView>(RecyclerView.class)
            {
                @Override
                protected boolean matchesSafely(RecyclerView item)
                {
                    RecyclerView.ViewHolder viewHolder =
                            item.findViewHolderForAdapterPosition(mListItemIndex);
                    if (viewHolder == null) {
                        // There is no item with matching index (possibly because it isn't
                        // visible)
                        return false;
                    }
                    return listItemMatcher.matches(viewHolder.itemView);
                }
                
                @Override
                public void describeTo(Description description)
                {
                    description.appendText("has item at position " + mListItemIndex + ": ");
                    listItemMatcher.describeTo(description);
                }
            });
        }
    }
}
