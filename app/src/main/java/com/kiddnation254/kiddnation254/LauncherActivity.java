package com.kiddnation254.kiddnation254;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.novoda.merlin.Merlin;
import com.novoda.merlin.registerable.connection.Connectable;
import com.novoda.merlin.registerable.disconnection.Disconnectable;

public class LauncherActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    Merlin merlin = new Merlin.Builder().withConnectableCallbacks().build(this);
    Merlin merlinDisconnect = new Merlin.Builder().withDisconnectableCallbacks().build(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        Helpers helpers = new Helpers(this);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        final Snackbar snackbar = Snackbar.make(coordinatorLayout, "No Internet Connection",
                Snackbar.LENGTH_INDEFINITE);

        // set action button color
        snackbar.setActionTextColor(getResources().getColor(R.color.deep_aqua));
        snackbar.setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }
        });

        // get snackbar view
        View snackbarView = snackbar.getView();

        Drawable drawable = getResources().getDrawable(R.drawable.baseline_signal_cellular_connected_no_internet_4_bar_white_24dp);

        // change snackbar text color
        int snackbarTextId = android.support.design.R.id.snackbar_text;
        int snackbarActionTextId = android.support.design.R.id.snackbar_action;
        TextView textView = (TextView) snackbarView.findViewById(snackbarTextId);
        textView.setCompoundDrawables(drawable, null, null, null);
        TextView textViewAction = (TextView) snackbarView.findViewById(snackbarActionTextId);
        textView.setTextColor(Color.WHITE);
        textViewAction.setTextColor(Color.CYAN);

        // change snackbar background
        snackbarView.setBackgroundColor(getResources().getColor(R.color.deep_aqua));

        if (SharedPrefManager.getInstance(this).isFirstTimeLaunch()) {
            finish();
            startActivity(new Intent(this, WelcomeActivity.class));
        } else {
            if (helpers.isConnectedToInternet()) {
                    finish();
                    startActivity(new Intent(this, LoginActivity.class));
            } else {
                snackbar.show();

            }
        }

        merlin.registerConnectable(new Connectable() {
            @Override
            public void onConnect() {
                if (SharedPrefManager.getInstance(getApplicationContext()).isFirstTimeLaunch()) {
                    startActivity(new Intent(getApplicationContext(), WelcomeActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }
        });

        merlinDisconnect.registerDisconnectable(new Disconnectable() {
            @Override
            public void onDisconnect() {
                snackbar.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        merlin.bind();
    }

    @Override
    protected void onPause() {
        merlin.unbind();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }


}
