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
package com.rbraithwaite.sleeptarget.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mikepenz.aboutlibraries.LibsBuilder;
import com.rbraithwaite.sleeptarget.BuildConfig;
import com.rbraithwaite.sleeptarget.R;
import com.rbraithwaite.sleeptarget.databinding.AboutFragmentBinding;
import com.rbraithwaite.sleeptarget.ui.BaseFragment;

public class AboutFragment
        extends BaseFragment<AboutViewModel>
{
//*********************************************************
// private properties
//*********************************************************

    private AboutFragmentBinding mBinding;
    
//*********************************************************
// overrides
//*********************************************************

    @Override
    protected Properties<AboutViewModel> initProperties()
    {
        return new Properties<>(false, AboutViewModel.class);
    }
    
    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState)
    {
        mBinding = AboutFragmentBinding.inflate(inflater, container, false);
        return mBinding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        mBinding.aboutHeader.aboutVersionNum.setText(getVersionText());
        mBinding.aboutHeader.aboutDescription.setMovementMethod(LinkMovementMethod.getInstance());
        
        mBinding.aboutLicenses.aboutLicensesViewBtn.setOnClickListener(v -> onDisplayLicenses());
        
        mBinding.aboutPrivacy.aboutPrivacyViewBtn.setOnClickListener(v -> onClickPrivacyPolicyButton());
        
        mBinding.aboutCreditsContent.aboutCreditsVisual.setMovementMethod(LinkMovementMethod.getInstance());
    }

//*********************************************************
// private methods
//*********************************************************

    private void onDisplayLicenses()
    {
        LibsBuilder libsBuilder = new LibsBuilder()
                .withLicenseShown(true);
        
        getNavController().navigate(
                AboutFragmentDirections.actionNavAboutToAboutLibraries(libsBuilder));
    }
    
    private void onClickPrivacyPolicyButton()
    {
        String policyUrl = "https://gist.github.com/rbraith/b0785564704b7a891fd490c3c2aa18b9";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(policyUrl)));
    }
    
    // REFACTOR [21-08-24 5:53PM] -- This should go somewhere else - view model? utility class?
    private String getVersionText()
    {
        return getString(R.string.about_version_num, BuildConfig.VERSION_NAME);
    }
}
