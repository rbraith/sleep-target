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

package com.rbraithwaite.sleeptarget.data_tests;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;

import com.rbraithwaite.sleeptarget.data.database.AppDatabase;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagDao;
import com.rbraithwaite.sleeptarget.data.database.tables.tag.TagEntity;
import com.rbraithwaite.sleeptarget.test_utils.TestUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class TagDaoTests
{
//*********************************************************
// private properties
//*********************************************************

    private AppDatabase database;
    private TagDao tagDao;

//*********************************************************
// public properties
//*********************************************************

    @Rule
    // protection against potentially infinitely blocked threads
    public Timeout timeout = new Timeout(10, TimeUnit.SECONDS);

//*********************************************************
// api
//*********************************************************

    @Before
    public void setup()
    {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        tagDao = database.getTagDao();
    }
    
    @After
    public void teardown()
    {
        tagDao = null;
        database.close();
    }
    
    @Test
    public void getTagsWithIds_getsCorrectTags()
    {
        String[] texts = {
                "wazzup",
                "wazzuuuuuuuppppp",
                "howdy"
        };
        
        List<Long> ids = new ArrayList<>();
        for (String text : texts) {
            ids.add(tagDao.addTag(new TagEntity(text)));
        }
        
        // SUT
        Integer[] expectedIds = {1, 2};
        LiveData<List<TagEntity>> tags = tagDao.getTagsWithIds(Arrays.asList(expectedIds));

        TestUtils.activateInstrumentationLiveData(tags);
        assertThat(tags.getValue().size(), is(expectedIds.length));
        for (int i = 0; i < tags.getValue().size(); i++) {
            TagEntity tag = tags.getValue().get(i);
            int expectedId = expectedIds[i];
            String expectedText = texts[i];
            
            assertThat(tag.id, is(expectedId));
            assertThat(tag.text, is(equalTo(expectedText)));
        }
    }
}
