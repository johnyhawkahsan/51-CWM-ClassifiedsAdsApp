package com.ahsan.a51_cwm_classifiedsadsapp;

//=========================================This is our MainActivity===============================================//

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.ahsan.a51_cwm_classifiedsadsapp.util.SectionsPagerAdapter;


public class SearchActivity extends AppCompatActivity {

    private static final String TAG = "SearchActivity";
    private static final int REQUEST_CODE = 222;

    //Widgets
    private TabLayout mTabLayout;
    public ViewPager mViewPager;

    //vars
    public SectionsPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mViewPager = (ViewPager) findViewById(R.id.viewpager_container);

        //setupViewPager(); //Now moved to verifyPermissions()
        verifyPermissions();
    }

    private void setupViewPager(){
        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //Add all fragments to our SectionsPagerAdapter
        mPagerAdapter.addFragment(new SearchFragment());        //Position 0
        mPagerAdapter.addFragment(new WatchListFragment());     //Position 1
        mPagerAdapter.addFragment(new PostFragment());          //Position 2
        mPagerAdapter.addFragment(new AccountFragment());       //Position 3

        mViewPager.setAdapter(mPagerAdapter); //set adapter for ViewPager in activity_search
        mTabLayout.setupWithViewPager(mViewPager); //We are setting up tabs according to ViewPager

        //Set tabs text according to their corresponding position
        mTabLayout.getTabAt(0).setText(getString(R.string.fragment_search));
        mTabLayout.getTabAt(1).setText(getString(R.string.fragment_watch_list));
        mTabLayout.getTabAt(2).setText(getString(R.string.fragment_post));
        mTabLayout.getTabAt(3).setText(getString(R.string.fragment_account));

    }

    //For API 21 and above, we incorporated Permission in Manifest, but for MARSHMALLOW, We need to ask for permissions explicitly
    private void verifyPermissions(){
        Log.d(TAG, "verifyPermissions()");

        //Permissions are stored in String array
        String[] permissions = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
        };

        //if permissions are granted
        if ( ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[0]) == PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[1]) == PackageManager.PERMISSION_GRANTED
          && ContextCompat.checkSelfPermission(this.getApplicationContext(), permissions[2]) == PackageManager.PERMISSION_GRANTED)

        {
            //If those 3 permissions are granted, setup view pager
            Log.d(TAG, "verifyPermissions: if those 3 permissions are granted, run setup view pager" );
            setupViewPager();

        }

        //else if permissions are not granted, ask for those 3 permissions.
        else{
            Log.d(TAG, "verifyPermissions: else ask for 3 permissions" );
            ActivityCompat.requestPermissions(
                    SearchActivity.this,
                    permissions,
                    REQUEST_CODE);
        }
    }

    //Do task based on REQUEST_CODE- Here we have only one task to execute, so we can omit if else statement based on requestCode and directly call verifyPermissions()
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Here request code doesn't matter because we've already asked for permission above but to confirm, I log requestCode here
        Log.d(TAG, "onRequestPermissionsResult: requestCode: " + requestCode);

        verifyPermissions();
    }
}
