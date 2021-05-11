package com.rbraithwaite.sleepapp.test_utils.data.database.daos;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class QueryBuilder
{
//*********************************************************
// private properties
//*********************************************************

    private SQLiteDatabase mDatabase;
    
    private String mTableName;
    
    private String[] mColumns;
    private String mSelection;
    private String[] mSelectionArgs;
    private String mGroupBy;
    private String mHaving;
    private String mOrderBy;
    
//*********************************************************
// public helpers
//*********************************************************

    public interface OnCursor
    {
        void onCursor(Cursor cursor);
    }
    
//*********************************************************
// constructors
//*********************************************************

    public QueryBuilder(SQLiteDatabase database, String tableName)
    {
        mDatabase = database;
        mTableName = tableName;
    }
    
//*********************************************************
// api
//*********************************************************

    public QueryBuilder setColumns(String[] columns)
    {
        mColumns = columns;
        return this;
    }
    
    public QueryBuilder setSelection(String selection)
    {
        mSelection = selection;
        return this;
    }
    
    public QueryBuilder setSelectionArgs(String[] selectionArgs)
    {
        mSelectionArgs = selectionArgs;
        return this;
    }
    
    public QueryBuilder setGroupBy(String groupBy)
    {
        mGroupBy = groupBy;
        return this;
    }
    
    public QueryBuilder setHaving(String having)
    {
        mHaving = having;
        return this;
    }
    
    public QueryBuilder setOrderBy(String orderBy)
    {
        mOrderBy = orderBy;
        return this;
    }
    
    public void executeQuery(OnCursor onCursor)
    {
        Cursor cursor = mDatabase.query(
                mTableName,
                mColumns,
                mSelection,
                mSelectionArgs,
                mGroupBy,
                mHaving,
                mOrderBy);
        
        onCursor.onCursor(cursor);
        
        cursor.close();
    }
}
