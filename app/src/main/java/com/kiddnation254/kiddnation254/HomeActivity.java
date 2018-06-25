package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ImageView imageViewUser, refresh;
    private TextView textViewUsername, textViewNoPost, textViewFetchError;
    private String path, postImgPath;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private PostsAdapter adapter;
    private List<Post> postList;
    private ProgressDialog progressDialog;
    private int total;
    private String limit;
    private Button showMorePostsButton, showMoreSearchPostsButton;
    private StoreNextStart storeNextStart;
    private String userImgLink;
    private StoreSearchTerm storeSearchTerm;
    private RelativeLayout recycleViewContainer;
    private RelativeLayout progressBarContainer, fetch_error;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        limit = "5";

        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);
        showMorePostsButton = (Button) findViewById(R.id.showMorePostsButton);
        showMoreSearchPostsButton = (Button) findViewById(R.id.showMoreSearchPostsButton);
        recycleViewContainer = (RelativeLayout) findViewById(R.id.recycler_view_container);
        fetch_error = (RelativeLayout) findViewById(R.id.fetch_error);
        refresh = (ImageView) findViewById(R.id.refresh);

        recycleViewContainer.setVisibility(View.GONE);
        fetch_error.setVisibility(View.GONE);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        postList = new ArrayList<>();
        adapter = new PostsAdapter(this, postList);

        recyclerView.setAdapter(adapter);

        showFewPosts();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Posts");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.bottomNavigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.getMenu().getItem(0).setChecked(true);

        View header = navigationView.getHeaderView(0);
        textViewUsername = (TextView) header.findViewById(R.id.textViewUsername);
        imageViewUser = (ImageView) header.findViewById(R.id.imageViewUser);
        textViewUsername.setText(SharedPrefManager.getInstance(getApplicationContext()).getUsername());

        userImgLink = SharedPrefManager.getInstance(getApplicationContext()).getUserImageLink();
        path = Constants.URL_USER_IMG;


        showMorePostsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMorePosts(storeNextStart.getStart());
                    }
                }
        );

        showMoreSearchPostsButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        searchMorePosts(storeSearchTerm.getSearchTerm(), storeNextStart.getStart());
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
        searchView.setQueryHint(Html.fromHtml("<font color = #ffffff>" +
                getResources().getString(R.string.hint_search) + "</font>"));

        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        AutoCompleteTextView searchTextView = (AutoCompleteTextView)
                searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        try {
            Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
            mCursorDrawableRes.setAccessible(true);
            mCursorDrawableRes.set(searchTextView, R.drawable.cursor);
        } catch (Exception e) {
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String searchTerm = searchView.getQuery().toString();
                if (searchTerm.length() < 3) {
                    Toast.makeText(getApplicationContext(),
                            "Please enter at least 3 characters", Toast.LENGTH_LONG).show();
                } else {
                    searchFewPosts(searchTerm);
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

    public void showFewPosts() {
        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_POSTS + "?limit=" + limit,
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
                                        showMorePostsButton.setVisibility(View.GONE);
                                    }

                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        if (data.get(key) instanceof JSONObject) {
                                            JSONObject row = new JSONObject(data.get(key).toString());

                                            Post post = new Post(row.getInt("id"), row.getString("title"),
                                                    limitStringLength(row.getString("body"), 200),
                                                    Constants.URL_POST_IMG +
                                                            row.getString("post_image"),
                                                    row.getString("author"), showDate(row.getString("time")));
                                            postList.add(post);

                                        }
                                    }
                                    Collections.sort(postList, new Comparator<Post>() {
                                        @Override
                                        public int compare(Post lhs, Post rhs) {
                                            return Integer.toString(lhs.getPostId())
                                                    .compareTo(Integer.toString(rhs.getPostId()));
                                        }
                                    });
                                    Collections.reverse(postList);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    showMorePostsButton.setVisibility(View.GONE);
                                }
                            } else {
                                recycleViewContainer.setVisibility(View.GONE);
                                textViewFetchError.setVisibility(View.VISIBLE);
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
//                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                        recycleViewContainer.setVisibility(View.GONE);
                        fetch_error.setVisibility(View.VISIBLE);
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void showMorePosts(int start) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_POSTS + "?limit=" + limit + "&start=" + start,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                storeNextStart.setStart(jsonObject.getInt("next_start"));

                                if (storeNextStart.getStart() >= jsonObject.getInt("total")) {
                                    showMorePostsButton.setVisibility(View.GONE);
                                }

                                total = jsonObject.getInt("total");
                                JSONObject data = jsonObject.getJSONObject("data");
                                Iterator<?> keys = data.keys();

                                List<Post> postListNew = new ArrayList<>();

                                if (total < 6) {
                                    showMorePostsButton.setVisibility(View.GONE);
                                }

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    if (data.get(key) instanceof JSONObject) {
                                        JSONObject row = new JSONObject(data.get(key).toString());

                                        Post post = new Post(row.getInt("id"), row.getString("title"),
                                                limitStringLength(row.getString("body"), 200),
                                                Constants.URL_POST_IMG +
                                                        row.getString("post_image"),
                                                row.getString("author"), showDate(row.getString("time")));
                                        postListNew.add(post);

                                    }
                                }
                                Collections.sort(postListNew, new Comparator<Post>() {
                                    @Override
                                    public int compare(Post lhs, Post rhs) {
                                        int n1 = lhs.getPostId();
                                        int n2 = rhs.getPostId();
                                        if (n1 >= n2) {
                                            return 1;
                                        }
                                        return -1;
                                    }
                                });
                                Collections.reverse(postListNew);
                                postList.addAll(postListNew);
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

    private String showDate(String dateString) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        android.text.format.DateFormat dateFormat = new android.text.format.DateFormat();
        return dateFormat.format("MMM dd, yyyy", date).toString();

    }

    public void searchFewPosts(String search_term) {

        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SEARCH_POSTS + "?limit=" + limit + "&search_term=" + search_term,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        postList.clear();
                        adapter.notifyDataSetChanged();

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                if (!jsonObject.getBoolean("noData")) {
                                    recycleViewContainer.setVisibility(View.VISIBLE);
                                    textViewNoPost.setVisibility(View.GONE);
                                    textViewFetchError.setVisibility(View.GONE);
                                    showMorePostsButton.setVisibility(View.GONE);
                                    showMoreSearchPostsButton.setVisibility(View.VISIBLE);
                                    storeNextStart = new StoreNextStart(jsonObject.getInt("next_start"));
                                    total = jsonObject.getInt("total");
                                    JSONObject data = jsonObject.getJSONObject("data");
                                    Iterator<?> keys = data.keys();

                                    if (total < 6) {
                                        showMoreSearchPostsButton.setVisibility(View.GONE);
                                    }

                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        if (data.get(key) instanceof JSONObject) {
                                            JSONObject row = new JSONObject(data.get(key).toString());

                                            Post post = new Post(row.getInt("id"), row.getString("title"),
                                                    limitStringLength(row.getString("body"), 200),
                                                    Constants.URL_POST_IMG +
                                                            row.getString("post_image"),
                                                    row.getString("author"), showDate(row.getString("time")));
                                            postList.add(post);

                                        }
                                    }
                                    Collections.sort(postList, new Comparator<Post>() {
                                        @Override
                                        public int compare(Post lhs, Post rhs) {
                                            int n1 = lhs.getPostId();
                                            int n2 = rhs.getPostId();
                                            if (n1 >= n2) {
                                                return 1;
                                            }
                                            return -1;
                                        }
                                    });
                                    Collections.reverse(postList);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    Toast toast = new Toast(getApplicationContext());
                                    View view = getLayoutInflater().inflate(R.layout.warning, null);
                                    TextView textView = view.findViewById(R.id.message);
                                    textView.setText(R.string.no_post);
                                    toast.setView(view);
                                    int gravity = Gravity.BOTTOM;
                                    toast.setGravity(gravity, 10, 10);
                                    toast.show();
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
                        /*Toast toast = Toast.makeText(getApplicationContext(), getString(R.string.network_error),
                                Toast.LENGTH_LONG);
                        // set message color
                        TextView textView = (TextView) toast.getView().findViewById(android.R.id.message);
                        textView.setTextColor(Color.WHITE);

                        // set background color
                        toast.getView().setBackgroundColor(getResources().getColor(R.color.deep_aqua));
                        toast.show();*/

                        Toast toast = new Toast(getApplicationContext());

                        View view = getLayoutInflater().inflate(R.layout.network_error, null);
                        toast.setView(view);
                        toast.setDuration(Toast.LENGTH_LONG);
                        int gravity = Gravity.BOTTOM;
                        toast.setGravity(gravity, 10, 10);
                        toast.show();
                    }
                }
        );

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void searchMorePosts(String search_term, int start) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SEARCH_POSTS + "?limit=" + limit + "&start=" + start + "&search_term=" + search_term,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                storeNextStart.setStart(jsonObject.getInt("next_start"));

                                if (storeNextStart.getStart() >= jsonObject.getInt("total")) {
                                    showMoreSearchPostsButton.setVisibility(View.GONE);
                                }

                                total = jsonObject.getInt("total");
                                JSONObject data = jsonObject.getJSONObject("data");
                                Iterator<?> keys = data.keys();

                                List<Post> postListNew = new ArrayList<>();

                                if (total < 6) {
                                    showMoreSearchPostsButton.setVisibility(View.GONE);
                                }

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    if (data.get(key) instanceof JSONObject) {
                                        JSONObject row = new JSONObject(data.get(key).toString());

                                        Post post = new Post(row.getInt("id"), row.getString("title"),
                                                limitStringLength(row.getString("body"), 200),
                                                Constants.URL_POST_IMG +
                                                        row.getString("post_image"),
                                                row.getString("author"), showDate(row.getString("time")));
                                        postListNew.add(post);

                                    }
                                }
                                Collections.sort(postListNew, new Comparator<Post>() {
                                    @Override
                                    public int compare(Post lhs, Post rhs) {
                                        int n1 = lhs.getPostId();
                                        int n2 = rhs.getPostId();
                                        if (n1 >= n2) {
                                            return 1;
                                        }
                                        return -1;
                                    }
                                });
                                Collections.reverse(postListNew);
                                postList.addAll(postListNew);
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

    private String limitStringLength(String value, int length) {
        String[] regx = {"<p>", "</p>", "<b>", "</b>", "<h4>", "</h4>"};
        for (int i = 0; i < regx.length; i++) {
            String regX = regx[i];
            char[] ca = regX.toCharArray();
            for (char c : ca) {
                value = value.replace("" + c, "");
            }
        }

        StringBuilder buf = new StringBuilder(value);
        if (buf.length() > length) {
            buf.setLength(length);
            buf.append("...");
        }

        return buf.toString();
    }

}
