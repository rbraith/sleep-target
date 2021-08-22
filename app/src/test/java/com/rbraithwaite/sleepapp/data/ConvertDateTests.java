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

package com.rbraithwaite.sleepapp.data;

import com.rbraithwaite.sleepapp.data.database.convert.ConvertDate;

import org.junit.Test;

import java.util.Date;
import java.util.GregorianCalendar;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

public class ConvertDateTests
{
//*********************************************************
// api
//*********************************************************

    @Test
    public void twoWayConversionTest()
    {
        Date testDate = new GregorianCalendar(2019, 8, 7).getTime();
        
        Date resultDate = ConvertDate.fromMillis(
                ConvertDate.toMillis(testDate));
        
        assertThat(resultDate, is(equalTo(testDate)));
    }
    
    @Test
    public void nullInputTest()
    {
        Date result = ConvertDate.fromMillis(ConvertDate.toMillis(null));
        assertThat(result, is(nullValue()));
    }
}
