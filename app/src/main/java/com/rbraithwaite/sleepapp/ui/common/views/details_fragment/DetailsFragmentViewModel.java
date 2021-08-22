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

package com.rbraithwaite.sleepapp.ui.common.views.details_fragment;

import androidx.lifecycle.ViewModel;

public abstract class DetailsFragmentViewModel<DataType>
        extends ViewModel
{
//*********************************************************
// abstract
//*********************************************************

    public abstract DataType getResult();
    
    /**
     * Set the data only if the view model is clear (ie no data has been set yet, or clearData was
     * called).
     */
    public abstract void initData(DataType data);
    
    public abstract void clearData();

//*********************************************************
// api
//*********************************************************

    
    /**
     * Simple convenience method which clears then re-initializes the data.
     */
    public void setData(DataType data)
    {
        clearData();
        initData(data);
    }
}
