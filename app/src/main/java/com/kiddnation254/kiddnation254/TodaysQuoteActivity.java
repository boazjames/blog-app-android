package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

public class TodaysQuoteActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView imageViewUser, refresh;
    private TextView textViewUsername, textViewFetchError;
    private String path;
    private TextView quoteBody, quoteAuothor;
    private ProgressDialog progressDialog;
    private String userImgLink;
    private RelativeLayout relativeLayoutContainer;
    private RelativeLayout progressBarContainer, fetchError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todays_quote);

        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        quoteBody = (TextView) findViewById(R.id.quoteBody);
        quoteAuothor = (TextView) findViewById(R.id.quoteAuthor);
        fetchError = (RelativeLayout) findViewById(R.id.fetch_error);
        relativeLayoutContainer = (RelativeLayout) findViewById(R.id.relativeLayoutContainer);
        refresh = (ImageView) findViewById(R.id.refresh);

        fetchError.setVisibility(View.GONE);
        relativeLayoutContainer.setVisibility(View.GONE);

        showQuote();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("Today's Quote");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(2).setChecked(true);

        View header = navigationView.getHeaderView(0);
        textViewUsername = (TextView) header.findViewById(R.id.textViewUsername);
        imageViewUser = (ImageView) header.findViewById(R.id.imageViewUser);
        textViewUsername.setText(SharedPrefManager.getInstance(getApplicationContext()).getUsername());

        userImgLink = SharedPrefManager.getInstance(getApplicationContext()).getUserImageLink();
        path = Constants.URL_USER_IMG;

        refresh.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                        overridePendingTransition( 0, 0);
                        startActivity(getIntent());
                        overridePendingTransition( 0, 0);
                    }
                }
        );

    }

    @Override
    protected void onStart() {
        super.onStart();

        Picasso.with(this)
                .load(path + userImgLink)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(imageViewUser);

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
                    R.style.MyDialogTheme);
            alertDialogBuilder.setTitle("Exit App?");
            alertDialogBuilder
                    .setMessage("Do you want to quit!")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    moveTaskToBack(true);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);
                                }
                            })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_change_profile_picture) {
            startActivity(new Intent(getApplicationContext(), ChangeProfilePhotoActivity.class));
        } else if (id == R.id.nav_view_profile) {
            startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,
                    R.style.MyDialogTheme);
            alertDialogBuilder.setTitle("Logout?");
            alertDialogBuilder
                    .setMessage("Do you want to logout!")
                    .setCancelable(false)
                    .setPositiveButton("Yes",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SharedPrefManager.getInstance(getApplicationContext()).logout();
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                    finish();
                                }
                            })

                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(getApplicationContext(), AboutActivity.class));
        } else if (id == R.id.nav_exit) {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_posts:
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    finish();
                    return true;
                case R.id.navigation_videos:
                    startActivity(new Intent(getApplicationContext(), VideosActivity.class));
                    finish();
                    return true;
                case R.id.navigation_quotes:
                    startActivity(new Intent(getApplicationContext(), TodaysQuoteActivity.class));
                    finish();
                    return true;
            }
            return false;
        }
    };

    public void showQuote() {
        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_TODAYS_QUOTE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


                        try {
                            final JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {

                                Quote quote = new Quote(jsonObject.getInt("id"), jsonObject.getString("body"),
                                        "-" + jsonObject.getString("author"));

                                quoteBody.setText(quote.getQuoteBody());
                                quoteAuothor.setText(quote.getQuoteAuthor());
                                relativeLayoutContainer.setVisibility(View.VISIBLE);

                            } else {
                                Toast toast = new Toast(getApplicationContext());
                                View view = getLayoutInflater().inflate(R.layout.warning, null);
                                TextView textView = view.findViewById(R.id.message);
                                textView.setText(jsonObject.getString("message"));
                                toast.setView(view);
                                int gravity = Gravity.BOTTOM;
                                toast.setGravity(gravity, 10, 10);
                                toast.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        fetchError.setVisibility(View.VISIBLE);
                        relativeLayoutContainer.setVisibility(View.GONE);
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }
}
