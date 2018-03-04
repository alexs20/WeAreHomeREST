package com.wolandsoft.wahrest.activity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.ActionBar;
import android.support.v7.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.wolandsoft.wahrest.activity.fragment.SettingsFragment;
import com.wolandsoft.wahrest.activity.fragment.status.StatusFragment;
import com.wolandsoft.wahrest.common.LogEx;
import com.wolandsoft.wahrest.R;
import com.wolandsoft.wahrest.service.CoreMonitorService;
import com.wolandsoft.wahrest.service.ServiceManager;

import java.util.List;

public class MainActivity extends AppCompatActivity implements
        FragmentManager.OnBackStackChangedListener,
        ActivityCompat.OnRequestPermissionsResultCallback{

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.fragment_settings, true);

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = new StatusFragment();
            transaction.replace(R.id.content_fragment, fragment, StatusFragment.class.getName());
            transaction.commit();
            ServiceManager.manageService(this, CoreMonitorService.class, true);
        }

        //Listen for changes in the back stack
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        //drawer
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView drawerView = (NavigationView) findViewById(R.id.nvView);
        //drawer items selection listener
        drawerView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return false;
                    }
                });
        //drawer button customization
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.label_open_drawer, R.string.label_close_drawer) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null && actionBar.isShowing())
                        actionBar.setTitle(R.string.app_name);
                }
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null && actionBar.isShowing())
                    actionBar.setTitle(R.string.label_options);
            }
        };
        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        TextView txtFooter = (TextView) findViewById(R.id.txtFooter);
        try {
            PackageInfo appInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            txtFooter.setText(String.format(getString(R.string.label_version),
                    appInfo.versionName, String.valueOf(appInfo.versionCode)));
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        controlDrawerAvailability();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }

    private void selectDrawerItem(MenuItem menuItem) {
        mDrawerLayout.closeDrawers();

        switch (menuItem.getItemId()) {
            case R.id.navSettings: {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                Fragment fragment = new SettingsFragment();
                transaction.replace(R.id.content_fragment, fragment, SettingsFragment.class.getName());
                transaction.addToBackStack(SettingsFragment.class.getName());
                transaction.commit();
                break;
            }
        }

    }


    @Override
    public void onStart() {
        super.onStart();
/*        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                0);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Nearby.getMessagesClient(this, new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build()).publish(mMessage);
            Nearby.getMessagesClient(this, new MessagesOptions.Builder()
                    .setPermissions(NearbyPermissions.BLE)
                    .build()).subscribe(mMessageListener);
        }*/
    }

    @Override
    public void onStop() {
  //      Nearby.getMessagesClient(this).unpublish(mMessage);
  //      Nearby.getMessagesClient(this).unsubscribe(mMessageListener);

        super.onStop();
    }

    @Override
    public void onBackStackChanged() {
        if (LogEx.IS_DEBUG) {
            LogEx.d("onBackStackChanged()");
            List<Fragment> fragments = getSupportFragmentManager().getFragments();
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    LogEx.d("Fragment: ", fragment.getClass().getName(), "; Tag: ", fragment.getTag());
                }
            }
        }
        controlDrawerAvailability();
    }

    private void controlDrawerAvailability() {
        boolean isEmptyStack = getSupportFragmentManager().getBackStackEntryCount() == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(isEmptyStack);
        mDrawerLayout.setDrawerLockMode(isEmptyStack ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        getSupportFragmentManager().popBackStack();
        return true;
    }
}
