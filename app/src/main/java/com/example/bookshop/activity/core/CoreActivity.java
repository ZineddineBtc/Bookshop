package com.example.bookshop.activity.core;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.bookshop.R;
import com.example.bookshop.StaticClass;
import com.example.bookshop.activity.core.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

public class CoreActivity extends AppCompatActivity {

    BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_core);
        navView = findViewById(R.id.nav_view);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_search, R.id.navigation_profile)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
    }
    @Override
    public void onBackPressed() {
        if(navView.getSelectedItemId()==R.id.navigation_profile){
            if(ProfileFragment.citiesListShown){
                ProfileFragment.citiesLL.setVisibility(View.GONE);
                ProfileFragment.shadeLL.setVisibility(View.GONE);
                ProfileFragment.citiesListShown = false;
            }
        }else{
            moveTaskToBack(true);
        }

    }

}
