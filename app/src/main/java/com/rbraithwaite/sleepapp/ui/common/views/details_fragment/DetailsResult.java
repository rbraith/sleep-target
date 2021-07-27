package com.rbraithwaite.sleepapp.ui.common.views.details_fragment;

import androidx.lifecycle.ViewModel;

public class DetailsResult<ResultType>
        extends ViewModel
{
//*********************************************************
// private properties
//*********************************************************

    private Result<ResultType> mResult;
    
//*********************************************************
// public helpers
//*********************************************************

    public enum Action
    {
        ADDED,
        UPDATED,
        DELETED
    }
    
    public static class Result<T>
    {
        public Action action;
        public T data;
        
        public Result(T data, Action action)
        {
            this.action = action;
            this.data = data;
        }
    }
    
//*********************************************************
// api
//*********************************************************

    public void setResult(Result<ResultType> result)
    {
        mResult = result;
    }
    
    public Result<ResultType> consumeResult()
    {
        Result<ResultType> temp = mResult;
        mResult = null;
        return temp;
    }
}
