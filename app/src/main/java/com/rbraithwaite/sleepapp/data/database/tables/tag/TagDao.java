package com.rbraithwaite.sleepapp.data.database.tables.tag;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public abstract class TagDao
{
//*********************************************************
// abstract
//*********************************************************

    @Query("SELECT * FROM " + TagContract.TABLE_NAME)
    public abstract LiveData<List<TagEntity>> getAllTags();
    
    @Insert
    public abstract long addTag(TagEntity tag);
    
    @Delete
    public abstract void deleteTag(TagEntity tag);
    
    @Update
    public abstract void updateTag(TagEntity tag);
    
    @Query("SELECT * FROM " + TagContract.TABLE_NAME + " WHERE " + TagContract.Columns.ID +
           " IN(:ids)")
    public abstract LiveData<List<TagEntity>> getTagsWithIds(List<Integer> ids);
}
