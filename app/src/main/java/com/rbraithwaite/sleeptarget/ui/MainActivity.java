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

package com.rbraithwaite.sleeptarget.ui;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rbraithwaite.sleeptarget.R;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class MainActivity
        extends AppCompatActivity
{
//*********************************************************
// private properties
//*********************************************************

    private NavController mNavController;
    
    private AppBarConfiguration mAppBarConfiguration;
    private BottomNavigationView mBottomNavigationView;

//*********************************************************
// private constants
//*********************************************************

    private static final String TAG = "MainActivity";

//*********************************************************
// overrides
//*********************************************************

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main_activity);
        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        
        mBottomNavigationView = findViewById(R.id.main_bottomnav);
        
        initializeNavigation();
    }
    
    @Override
    public boolean onSupportNavigateUp()
    {
        return NavigationUI.navigateUp(mNavController, mAppBarConfiguration)
               || super.onSupportNavigateUp();
    }
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev)
    {
        // Global behaviour for edit texts, clearing their focus when the user touches outside their
        // bounds. (especially useful for multi-line edit texts)
        // Stolen from https://stackoverflow.com/a/28939113
        // TODO [21-03-29 1:46AM] -- I might not want this to be global.
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect editTextRect = new Rect();
                v.getGlobalVisibleRect(editTextRect);
                
                boolean isTouchEventInsideEditText =
                        editTextRect.contains((int) ev.getRawX(), (int) ev.getRawY());
                if (!isTouchEventInsideEditText) {
                    v.clearFocus();
                    InputMethodManager imm =
                            (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

//*********************************************************
// api
//*********************************************************

    public void setBottomNavVisibility(boolean visibility)
    {
        mBottomNavigationView.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }


//*********************************************************
// private methods
//*********************************************************

    private void initializeNavigation()
    {
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.main_navhost);
        mNavController = navHostFragment.getNavController();
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_sleeptracker).build();
        NavigationUI.setupActionBarWithNavController(this, mNavController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(mBottomNavigationView, mNavController);
    }
}
