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

package com.rbraithwaite.sleeptarget.ui.common.views.details_fragment;

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
