package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.app.SearchManager;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class VideosActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView imageViewUser, refresh;
    private TextView textViewUsername, textViewFetchError, textViewNoVideo;
    private String path;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private VideosAdapter adapter;
    private List<Video> videoList;
    private ProgressDialog progressDialog;
    private int total;
    private String limit;
    private Button showMoreVideosButton, showMoreSearchVideosButton;
    private StoreNextStart storeNextStart;
    private String userImgLink;
    private StoreSearchTerm storeSearchTerm;
    private RelativeLayout recycleViewContainer;
    private RelativeLayout progressBarContainer, fetchErrorContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videos);

        limit = "5";

        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        showMoreVideosButton = (Button) findViewById(R.id.showMoreVideosButton);
        showMoreSearchVideosButton = (Button) findViewById(R.id.showMoreSearchVideosButton);
        recycleViewContainer = (RelativeLayout) findViewById(R.id.recycler_view_container);
        fetchErrorContainer = (RelativeLayout) findViewById(R.id.fetch_error);
        refresh = (ImageView) findViewById(R.id.refresh);

        recycleViewContainer.setVisibility(View.GONE);
        fetchErrorContainer.setVisibility(View.GONE);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_videos);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        videoList = new ArrayList<>();
        adapter = new VideosAdapter(this, videoList);

        recyclerView.setAdapter(adapter);

        showFewVideos();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Videos");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        View header = navigationView.getHeaderView(0);
        textViewUsername = (TextView) header.findViewById(R.id.textViewUsername);
        imageViewUser = (ImageView) header.findViewById(R.id.imageViewUser);
        textViewUsername.setText(SharedPrefManager.getInstance(getApplicationContext()).getUsername());

        userImgLink = SharedPrefManager.getInstance(getApplicationContext()).getUserImageLink();
        path = Constants.URL_USER_IMG;

        navigation.getMenu().getItem(1).setChecked(true);

        showMoreVideosButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMoreVideos(storeNextStart.getStart());
                    }
                }
        );

        showMoreSearchVideosButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMoreSearchVideos(storeSearchTerm.getSearchTerm(), storeNextStart.getStart());
                    }
                }
        );

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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) search.getActionView();
        searchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" + getResources().getString(R.string.hint_search) + "</font>"));

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        AutoCompleteTextView searchTextView = (AutoCompleteTextView) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor); //This sets the cursor resource ID to 0 or @null which will make it visible on white background
        } catch (Exception e) {
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchTerm = searchView.getQuery().toString();
                if (searchTerm.length() < 3) {
                    Toast.makeText(getApplicationContext(), "Please enter at least 3 characters",
                            Toast.LENGTH_LONG).show();
                } else {
                    searchFewVideos(searchTerm);
                    storeSearchTerm = new StoreSearchTerm(searchTerm);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
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
                case R.id.navigation_memes:
                    startActivity(new Intent(getApplicationContext(), MemeActivity.class));
                    finish();
                    return true;
            }
            return false;
        }
    };

    public void showFewVideos() {

        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_VIDEOS + "?limit=" + limit,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        recycleViewContainer.setVisibility(View.VISIBLE);

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                if (!jsonObject.getBoolean("noData")) {
                                    storeNextStart = new StoreNextStart(jsonObject.getInt("next_start"));
                                    total = jsonObject.getInt("total");
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    Iterator<?> keys = data.keys();

                                    if (total < 6) {
                                        showMoreVideosButton.setVisibility(View.GONE);
                                    }

                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        if (data.get(key) instanceof JSONObject) {
                                            JSONObject row = new JSONObject(data.get(key).toString());

                                            Video video = new Video(row.getInt("id"),
                                                    row.getString("code"), row.getString("title"));
                                            videoList.add(video);

                                        }
                                    }
                                    Collections.sort(videoList, new Comparator<Video>() {
                                        @Override
                                        public int compare(Video lhs, Video rhs) {
                                            return Integer.toString(lhs.getVideoId()).compareTo(Integer.toString(rhs.getVideoId()));
                                        }
                                    });
                                    Collections.reverse(videoList);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    showMoreVideosButton.setVisibility(View.GONE);
                                }
                            } else {
                                recycleViewContainer.setVisibility(View.GONE);
                                textViewFetchError.setVisibility(View.VISIBLE);
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
                        recycleViewContainer.setVisibility(View.GONE);
                        fetchErrorContainer.setVisibility(View.VISIBLE);
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void showMoreVideos(int start) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_VIDEOS + "?limit=" + limit + "&start=" + start,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                storeNextStart.setStart(jsonObject.getInt("next_start"));

                                if (storeNextStart.getStart() >= jsonObject.getInt("total")) {
                                    showMoreVideosButton.setVisibility(View.GONE);
                                }

                                total = jsonObject.getInt("total");
                                JSONObject data = jsonObject.getJSONObject("data");
                                Iterator<?> keys = data.keys();

                                List<Video> videoListNew = new ArrayList<>();

                                if (total < 6) {
                                    showMoreVideosButton.setVisibility(View.GONE);
                                }

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    if (data.get(key) instanceof JSONObject) {
                                        JSONObject row = new JSONObject(data.get(key).toString());

                                        Video video = new Video(row.getInt("id"),
                                                row.getString("code"), row.getString("title"));
                                        videoListNew.add(video);

                                    }
                                }
                                Collections.sort(videoListNew, new Comparator<Video>() {
                                    @Override
                                    public int compare(Video lhs, Video rhs) {
                                        int n1 = lhs.getVideoId();
                                        int n2 = rhs.getVideoId();
                                        if (n1 >= n2) {
                                            return 1;
                                        }
                                        return -1;
                                    }
                                });
                                Collections.reverse(videoListNew);
                                videoList.addAll(videoListNew);
                                adapter.notifyDataSetChanged();

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
                        Toast toast = new Toast(getApplicationContext());
                        View view = getLayoutInflater().inflate(R.layout.network_error, null);
                        toast.setView(view);
                        int gravity = Gravity.BOTTOM;
                        toast.setGravity(gravity, 10, 10);
                        toast.show();
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void searchFewVideos(String search_term) {

        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SEARCH_VIDEOS + "?limit=" + limit + "&search_term=" + search_term,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        videoList.clear();
                        adapter.notifyDataSetChanged();

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                if (!jsonObject.getBoolean("noData")) {
                                    recycleViewContainer.setVisibility(View.VISIBLE);
                                    textViewNoVideo.setVisibility(View.GONE);
                                    textViewFetchError.setVisibility(View.GONE);
                                    showMoreVideosButton.setVisibility(View.GONE);
                                    showMoreSearchVideosButton.setVisibility(View.VISIBLE);
                                    storeNextStart = new StoreNextStart(jsonObject.getInt("next_start"));
                                    total = jsonObject.getInt("total");
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    Iterator<?> keys = data.keys();

                                    if (total < 6) {
                                        showMoreSearchVideosButton.setVisibility(View.GONE);
                                    }

                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        if (data.get(key) instanceof JSONObject) {
                                            JSONObject row = new JSONObject(data.get(key).toString());

                                            Video video = new Video(row.getInt("id"),
                                                    row.getString("code"), row.getString("title"));
                                            videoList.add(video);

                                        }
                                    }
                                    Collections.sort(videoList, new Comparator<Video>() {
                                        @Override
                                        public int compare(Video lhs, Video rhs) {
                                            int n1 = lhs.getVideoId();
                                            int n2 = rhs.getVideoId();
                                            if (n1 >= n2) {
                                                return 1;
                                            }
                                            return -1;
                                        }
                                    });
                                    Collections.reverse(videoList);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    recycleViewContainer.setVisibility(View.GONE);
                                    textViewNoVideo.setVisibility(View.VISIBLE);
                                }
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
                        Toast toast = new Toast(getApplicationContext());
                        View view = getLayoutInflater().inflate(R.layout.network_error, null);
                        toast.setView(view);
                        int gravity = Gravity.BOTTOM;
                        toast.setGravity(gravity, 10, 10);
                        toast.show();
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void showMoreSearchVideos(String search_term, int start) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SEARCH_VIDEOS + "?limit=" + limit + "&start=" + start + "&search_term=" + search_term,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                storeNextStart.setStart(jsonObject.getInt("next_start"));

                                if (storeNextStart.getStart() >= jsonObject.getInt("total")) {
                                    showMoreSearchVideosButton.setVisibility(View.GONE);
                                }

                                total = jsonObject.getInt("total");
                                JSONObject data = jsonObject.getJSONObject("data");
                                Iterator<?> keys = data.keys();

                                List<Video> videoListNew = new ArrayList<>();

                                if (total < 6) {
                                    showMoreSearchVideosButton.setVisibility(View.GONE);
                                }

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    if (data.get(key) instanceof JSONObject) {
                                        JSONObject row = new JSONObject(data.get(key).toString());

                                        Video video = new Video(row.getInt("id"),
                                                row.getString("code"), row.getString("title"));
                                        videoListNew.add(video);

                                    }
                                }
                                Collections.sort(videoListNew, new Comparator<Video>() {
                                    @Override
                                    public int compare(Video lhs, Video rhs) {
                                        int n1 = lhs.getVideoId();
                                        int n2 = rhs.getVideoId();
                                        if (n1 >= n2) {
                                            return 1;
                                        }
                                        return -1;
                                    }
                                });
                                Collections.reverse(videoListNew);
                                videoList.addAll(videoListNew);
                                adapter.notifyDataSetChanged();

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
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.network_error), Toast.LENGTH_LONG).show();
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

}
