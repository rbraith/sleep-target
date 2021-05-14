package com.rbraithwaite.sleepapp.test_utils.ui.drivers;

import android.view.View;
import android.widget.RatingBar;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;

import com.rbraithwaite.sleepapp.R;
import com.rbraithwaite.sleepapp.test_utils.ui.fragment_helpers.FragmentTestHelper;
import com.rbraithwaite.sleepapp.ui.session_archive.SessionArchiveFragment;

import junit.framework.AssertionFailedError;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;

public class SessionArchiveTestDriver
{
//*********************************************************
// private constants
//*********************************************************

    private final FragmentTestHelper<SessionArchiveFragment> mHelper;
    private final OnOpenSessionDetailsListener mOnOpenSessionDetailsListener;
    
//*********************************************************
// public helpers
//*********************************************************

    public interface OnOpenSessionDetailsListener
    {
        void onOpenSessionDetails();
    }
    
//*********************************************************
// constructors
//*********************************************************

    public SessionArchiveTestDriver(
            FragmentTestHelper<SessionArchiveFragment> helper,
            OnOpenSessionDetailsListener onOpenSessionDetailsListener)
    {
        mOnOpenSessionDetailsListener = onOpenSessionDetailsListener;
        mHelper = helper;
    }
    
    public SessionArchiveTestDriver(FragmentTestHelper<SessionArchiveFragment> helper)
    {
        this(helper, null);
    }
    
//*********************************************************
// api
//*********************************************************
    
    private ViewInteraction onRecyclerView()
    {
        return onView(withId(R.id.session_archive_list));
    }

    public void openSessionDetailsFor(int listItemIndex)
    {
        if (mOnOpenSessionDetailsListener != null) {
            mOnOpenSessionDetailsListener.onOpenSessionDetails();
        }
        onRecyclerView()
                .perform(RecyclerViewActions.actionOnItemAtPosition(listItemIndex, click()));
    }
    
    public ListItemAssertions assertThatListItemAtIndex(int listItemIndex)
    {
        return new ListItemAssertions(listItemIndex, this);
    }
    
    public static class ListItemAssertions
    {
        private int mListItemIndex;
        private SessionArchiveTestDriver mOwner;
    
        private ListItemAssertions(int listItemIndex, SessionArchiveTestDriver owner)
        {
            mListItemIndex = listItemIndex;
            mOwner = owner;
        }
        
        public void hasRating(float rating)
        {
            mOwner.onRecyclerView().check(listItemAt(mListItemIndex).matches(hasDescendant(
                    allOf(withId(R.id.session_archive_list_item_rating),
                          withRating(rating)))));
        }
        
        // REFACTOR [21-05-14 5:23PM] -- extract this as a generic RatingBar matcher.
        private Matcher<View> withRating(float rating)
        {
            return new BoundedMatcher<View, RatingBar>(RatingBar.class)
            {
                @Override
                protected boolean matchesSafely(RatingBar item)
                {
                    return item.getRating() == rating;
                }
    
                @Override
                public void describeTo(Description description)
                {
                    description.appendText("rating matches: " + rating);
                }
            };
        }
        
        private ListItemMatchAssertion listItemAt(int index)
        {
            return new ListItemMatchAssertion(index);
        }
        
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
                return ViewAssertions.matches(new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
                    @Override
                    protected boolean matchesSafely(RecyclerView item)
                    {
                        RecyclerView.ViewHolder viewHolder = item.findViewHolderForAdapterPosition(mListItemIndex);
                        if (viewHolder == null) {
                            // There is no item with matching index (possibly because it isn't visible)
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
}
