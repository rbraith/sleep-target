package com.rbraithwaite.sleepapp.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.rbraithwaite.sleepapp.R;

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
