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

package com.rbraithwaite.sleeptarget.utils.list_tracking;

import android.os.Looper;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
public class ListTrackingLiveDataTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void add_updatesWithCorrectData()
    {
        ListTrackingLiveData<Integer> listTracking = new ListTrackingLiveData<>(new ArrayList<>());
        TestUtils.activateLocalLiveData(listTracking);
        
        // SUT
        listTracking.add(1);
        shadowOf(Looper.getMainLooper()).idle(); // since postValue is used
        
        ListTrackingData<Integer> data = listTracking.getValue();
        assertThat(data.list.size(), is(1));
        assertThat(data.list.get(0), is(equalTo(1)));
        assertThat(data.lastChange.changeType, is(equalTo(ListTrackingData.ChangeType.ADDED)));
        assertThat(data.lastChange.elem, is(equalTo(1)));
        assertThat(data.lastChange.index, is(equalTo(0)));
    }
    
    @Test
    public void delete_updatesWithCorrectData()
    {
        List<String> testList = new ArrayList<>();
        testList.add("test");
        ListTrackingLiveData<String> listTracking = new ListTrackingLiveData<>(testList);
        TestUtils.activateLocalLiveData(listTracking);
        
        // SUT
        listTracking.delete("test");
        shadowOf(Looper.getMainLooper()).idle(); // since postValue is used
        
        ListTrackingData<String> data = listTracking.getValue();
        assertThat(data.list.isEmpty(), is(true));
        assertThat(data.lastChange.changeType, is(equalTo(ListTrackingData.ChangeType.DELETED)));
        assertThat(data.lastChange.elem, is(equalTo("test")));
        assertThat(data.lastChange.index, is(equalTo(0)));
    }
    
    @Test
    public void set_updatesWithCorrectData()
    {
        List<String> testList = new ArrayList<>();
        testList.add("test");
        ListTrackingLiveData<String> listTracking = new ListTrackingLiveData<>(testList);
        TestUtils.activateLocalLiveData(listTracking);

        // SUT
        String expected = "updated";
        listTracking.set(0, expected);
        shadowOf(Looper.getMainLooper()).idle(); // since postValue is used

        ListTrackingData<String> data = listTracking.getValue();
        assertThat(data.list.size(), is(1));
        assertThat(data.list.get(0), is(equalTo(expected)));
        assertThat(data.lastChange.changeType, is(equalTo(ListTrackingData.ChangeType.MODIFIED)));
        assertThat(data.lastChange.elem, is(equalTo(expected)));
        assertThat(data.lastChange.index, is(equalTo(0)));
    }
}
