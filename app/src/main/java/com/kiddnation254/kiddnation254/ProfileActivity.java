package com.kiddnation254.kiddnation254;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity {

    private TextView profileUsername, profileEmail, profilePhone;
    private ImageView profilePhoto;
    private String path, userImgLink, userEmail, userPhone, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        profilePhoto = (ImageView) findViewById(R.id.profilePhoto);
        profileUsername = (TextView) findViewById(R.id.profileUsername);
        profileEmail = (TextView) findViewById(R.id.profileEmail);
        profilePhone = (TextView) findViewById(R.id.profilePhone);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(R.string.title_Profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        userImgLink = SharedPrefManager.getInstance(getApplicationContext()).getUserImageLink();
        userEmail = SharedPrefManager.getInstance(getApplicationContext()).getUserEmail();
        userPhone = SharedPrefManager.getInstance(getApplicationContext()).getUserPhone();
        username = SharedPrefManager.getInstance(getApplicationContext()).getUsername();
        path = Constants.URL_USER_IMG;

        profileUsername.setText(username);
        profileEmail.setText(userEmail);
        profilePhone.setText(userPhone);

        Picasso.with(this)
                .load(path + userImgLink)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(profilePhoto);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here

                onBackPressed();

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
