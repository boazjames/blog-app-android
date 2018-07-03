package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SinglePostActivity extends AppCompatActivity {

    private String path;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private CommentsAdapter adapter;
    private List<Comment> commentList;
    private ProgressDialog progressDialog;
    private ImageView userImg, postImg, refresh;
    private TextView author, postedAt, postBody, commentHeading, postTitle, textViewFetchError;
    private int postId;
    private int totalComments;
    private Button commentsAllButton, commentButton;
    private EditText editTextComment;
    private String limit;
    private StoreNextStart storeNextStart;
    private RelativeLayout relativeLayoutContainer;
    private RelativeLayout progressBarContainer, fetchError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_post);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        postId = extras.getInt("id");
        totalComments = 0;
        limit = "4";

        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.GONE);

        postImg = (ImageView) findViewById(R.id.videoImage);
        author = (TextView) findViewById(R.id.author);
        postedAt = (TextView) findViewById(R.id.postedAt);
        postBody = (TextView) findViewById(R.id.postBody);
        commentHeading = (TextView) findViewById(R.id.commentHeading);
        commentsAllButton = (Button) findViewById(R.id.commentsAllButton);
        editTextComment = (EditText) findViewById(R.id.editTextComment);
        commentButton = (Button) findViewById(R.id.commentButton);
        postTitle = (TextView) findViewById(R.id.postTitle);
        fetchError = (RelativeLayout) findViewById(R.id.fetch_error);
        relativeLayoutContainer = (RelativeLayout) findViewById(R.id.relativeLayoutContainer);
        refresh = (ImageView) findViewById(R.id.refresh);

        fetchError.setVisibility(View.GONE);
        relativeLayoutContainer.setVisibility(View.GONE);


        progressDialog = new ProgressDialog(this);


        recyclerView = (RecyclerView) findViewById(R.id.recycler_view_comments);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        commentList = new ArrayList<>();
        adapter = new CommentsAdapter(this, commentList);

        recyclerView.setAdapter(adapter);
        showPost();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        path = "http://192.168.43.167/kidd_nation/user_images/";

        commentButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String comment = editTextComment.getText().toString();
                        String user_id = Integer.toString(SharedPrefManager.getInstance(getApplicationContext()).getUserId());
                        addComment(Integer.toString(postId), comment, user_id);
                    }
                }
        );

        refresh.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = getIntent();
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        overridePendingTransition( 0, 0);
                        startActivity(intent);
                        overridePendingTransition( 0, 0);
                    }
                }
        );

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

    public void showPost() {
        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager
        .LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_POST + "?id=" + postId,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        try {
                            final JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {

                                Post post = new Post(jsonObject.getInt("id"), jsonObject.getString("title"), jsonObject.getString("body"),
                                        Constants.URL_POST_IMG + jsonObject.getString("post_image"),
                                        jsonObject.getString("author"), showDate(jsonObject.getString("time")));

                                author.setText(post.getAuthor());
                                postedAt.setText(post.getTime());
//                                postBody.setText(trimString(post.getBody()));
                                if (Build.VERSION.SDK_INT >= 26) {
                                    postBody.setText(Html.fromHtml(trimString(post.getBody()), Html.FROM_HTML_MODE_COMPACT));
                                } else {
                                    postBody.setText(Html.fromHtml(trimString(post.getBody())));
                                }
                                postTitle.setText(post.getTitle());
                                getSupportActionBar().setTitle(post.getTitle());

                                Picasso.with(getApplicationContext())
                                        .load(post.getThumbnail())
                                        .placeholder(R.drawable.blog_placeholder)
                                        .error(R.drawable.blog_placeholder)
                                        .fit()
                                        .into(postImg);

                                relativeLayoutContainer.setVisibility(View.VISIBLE);


                                showFewComments(Integer.toString(post.getPostId()), limit);

                                commentsAllButton.setOnClickListener(
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                try {
                                                    showMoreComments(jsonObject.getString("id"),
                                                            storeNextStart.getStart(),
                                                            limit);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        }
                                );

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

    public void showMoreComments(String postId, int start, String limit) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_COMMENTS + "?postId=" + postId + "&start=" + start + "&limit=" + limit,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                storeNextStart.setStart(jsonObject.getInt("next_start"));

                                if (storeNextStart.getStart() >= jsonObject.getInt("total")) {
                                    commentsAllButton.setVisibility(View.GONE);
                                }

                                totalComments = jsonObject.getInt("total");
                                commentHeading.setText("(" + totalComments + ")Comments");
                                JSONObject data = jsonObject.getJSONObject("data");
                                Iterator<?> keys = data.keys();

                                List<Comment> commentListNew = new ArrayList<>();

                                while (keys.hasNext()) {
                                    String key = (String) keys.next();
                                    if (data.get(key) instanceof JSONObject) {
                                        JSONObject row = new JSONObject(data.get(key).toString());

                                        Comment comment = new Comment(row.getString("user_uid"),
                                                row.getString("comment"), timeAgo(row.getString("time")), row.getString("user_image"));
                                        commentListNew.add(comment);

                                    }
                                }
                                Collections.reverse(commentListNew);
                                commentList.addAll(commentListNew);
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

    public void showFewComments(String postId, String limit) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.GET,
                Constants.URL_SHOW_COMMENTS + "?postId=" + postId + "&limit=" + limit,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                if (!jsonObject.getBoolean("noData")) {
                                    storeNextStart = new StoreNextStart(jsonObject.getInt("next_start"));
                                    totalComments = jsonObject.getInt("total");
                                    String commentCount = "(" + totalComments + ")Comments";
                                    commentHeading.setText(commentCount);

                                    if (totalComments < 5) {
                                        commentsAllButton.setVisibility(View.GONE);
                                    }

                                    JSONObject data = jsonObject.getJSONObject("data");
                                    Iterator<?> keys = data.keys();

                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        if (data.get(key) instanceof JSONObject) {
                                            JSONObject row = new JSONObject(data.get(key).toString());

                                            Comment comment = new Comment(row.getString("user_uid"),
                                                    row.getString("comment"), timeAgo(row.getString("time")), row.getString("user_image"));
                                            commentList.add(comment);

                                        }
                                    }
                                    Collections.reverse(commentList);
                                    adapter.notifyDataSetChanged();

                                } else {
                                    commentsAllButton.setVisibility(View.GONE);
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

    public void addComment(final String postId, final String comment, final String user_id) {
        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_ADD_COMMENT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                commentList.clear();
                                showFewComments(postId, limit);
                                editTextComment.setText("");
                                editTextComment.clearFocus();

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
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("post_id", postId);
                params.put("comment", comment);
                params.put("user_id", user_id);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }

    private String timeAgo(String dateString) {
        String format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long timeInMilliseconds = date.getTime();
        return TimeAgo.using(timeInMilliseconds);
    }

    private String trimString(String value) {
        String[] regx = {"<p>", "</p>", "<b>", "</b>", "<h4>", "</h4>"};
        for (int i = 0; i < regx.length; i++) {
            String regX = regx[i];
            char[] ca = regX.toCharArray();
            for (char c : ca) {
                value = value.replace("" + c, "");
            }
        }
        return value;
    }

}
