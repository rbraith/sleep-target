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

package com.rbraithwaite.sleeptarget.core.repositories;

import androidx.lifecycle.LiveData;

import com.rbraithwaite.sleeptarget.core.models.Tag;
import com.rbraithwaite.sleeptarget.utils.list_tracking.ListTrackingData;

import java.util.List;

public interface TagRepository
{
//*********************************************************
// abstract
//*********************************************************

    LiveData<ListTrackingData<Tag>> getAllTags();
    void addTag(Tag newTag);
    void deleteTag(Tag tag);
    void updateTag(Tag tag);
    LiveData<List<Tag>> getTagsWithIds(List<Integer> tagIds);
}
