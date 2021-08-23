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

package com.rbraithwaite.sleeptarget.ui.common.views;

import com.rbraithwaite.sleeptarget.ui.common.CommonFormatting;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public class CommonFormattingTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void formatTimeOfDay_formatsCorrectly()
    {
        Object[][] data = {
                // hour, minute, expected
                {13, 24, "1:24 PM"},
                {11, 2, "11:02 AM"}
        };
        
        for (Object[] dataSet : data) {
            int hour = (int) dataSet[0];
            int minute = (int) dataSet[1];
            String expected = (String) dataSet[2];
            
            assertThat(CommonFormatting.formatTimeOfDay(hour, minute), is(equalTo(expected)));
        }
    }
    
    @Test
    public void formatDate_formatsCorrectly()
    {
        Object[][] data = {
                // year, month, dayOfMonth, expected
                {2021, 7, 3, "Aug 3 2021"},
        };
        
        for (Object[] dataSet : data) {
            int year = (int) dataSet[0];
            int month = (int) dataSet[1];
            int dayOfMonth = (int) dataSet[2];
            String expected = (String) dataSet[3];
            
            assertThat(CommonFormatting.formatDate(year, month, dayOfMonth), is(equalTo(expected)));
        }
    }
}
