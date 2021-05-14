package com.rbraithwaite.sleepapp.data.convert;

import com.rbraithwaite.sleepapp.core.models.SleepSession;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.SleepSessionEntity;
import com.rbraithwaite.sleepapp.data.database.tables.sleep_session.data.SleepSessionWithTags;
import com.rbraithwaite.sleepapp.data.database.tables.tag.TagEntity;
import com.rbraithwaite.sleepapp.test_utils.TestUtils;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertSleepSessionTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void fromEntityWithTags_returnsNullOnNullInput()
    {
        assertThat(ConvertSleepSession.fromEntityWithTags(null), is(nullValue()));
    }
    
    @Test
    public void fromEntityWithTags_positiveInput()
    {
        // setup
        SleepSessionWithTags entityWithTags = new SleepSessionWithTags();
        entityWithTags.sleepSession = TestUtils.ArbitraryData.getSleepSessionEntity();
        entityWithTags.tags = Arrays.asList(
                new TagEntity(1, "tag 1"),
                new TagEntity(2, "tag 2"));
        
        // SUT
        SleepSession sleepSession = ConvertSleepSession.fromEntityWithTags(entityWithTags);
        
        // verify
        assertThat(sleepSession.getId(), is(entityWithTags.sleepSession.id));
        assertThat(sleepSession.getStart(), is(equalTo(entityWithTags.sleepSession.startTime)));
        assertThat(sleepSession.getAdditionalComments(),
                   is(equalTo(entityWithTags.sleepSession.additionalComments)));
        assertThat(sleepSession.getDurationMillis(),
                   is(equalTo(entityWithTags.sleepSession.duration)));
        
        for (int i = 0; i < sleepSession.getTags().size(); i++) {
            assertThat(sleepSession.getTags().get(i).getTagId(), is(entityWithTags.tags.get(i).id));
            assertThat(sleepSession.getTags().get(i).getText(),
                       is(entityWithTags.tags.get(i).text));
        }
    }
    
    @Test
    public void convertEntityToModel_returnsNullOnNullInput()
    {
        assertThat(ConvertSleepSession.fromEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertEntityToModel_positiveInput()
    {
        SleepSessionEntity entity = TestUtils.ArbitraryData.getSleepSessionEntity();
        SleepSession model = ConvertSleepSession.fromEntity(entity);
        
        assertThat(model.getId(), is(equalTo(entity.id)));
        assertThat(model.getStart(), is(equalTo(entity.startTime)));
        assertThat(model.getDurationMillis(), is(equalTo(entity.duration)));
        assertThat(model.getAdditionalComments(), is(equalTo(entity.additionalComments)));
        assertThat(model.getRating(), is(equalTo(entity.rating)));
    }
    
    @Test
    public void convertModelToEntity_returnsNullOnNullInput()
    {
        assertThat(ConvertSleepSession.toEntity(null), is(nullValue()));
    }
    
    @Test
    public void convertModelToEntity_positiveInput()
    {
        SleepSession model = TestUtils.ArbitraryData.getSleepSession();
        SleepSessionEntity entity = ConvertSleepSession.toEntity(model);
        
        assertThat(model.getId(), is(equalTo(entity.id)));
        assertThat(model.getStart(), is(equalTo(entity.startTime)));
        assertThat(model.getDurationMillis(), is(equalTo(entity.duration)));
        assertThat(model.getAdditionalComments(), is(equalTo(entity.additionalComments)));
    }
}
