package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SendCodeActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private ProgressDialog progressDialog;
    private RelativeLayout progressBarContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_code);

        editTextEmail = (EditText) findViewById(R.id.email);
        Button buttonSendCode = (Button) findViewById(R.id.buttonSendCode);

        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset Password");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonSendCode.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String email = editTextEmail.getText().toString().trim();
                        if(email.length() != 0) {
                            if(Helpers.isValidEmail(email)) {
                                sendCode(email);
                            } else {
                                Toast toast = new Toast(getApplicationContext());
                                View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                                TextView textView = view1.findViewById(R.id.message);
                                textView.setText(R.string.invalid_email);
                                toast.setView(view1);
                                int gravity = Gravity.BOTTOM;
                                toast.setGravity(gravity, 10, 10);
                                toast.show();
                            }
                        } else {
                            Toast toast = new Toast(getApplicationContext());
                            View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                            TextView textView = view1.findViewById(R.id.message);
                            textView.setText(R.string.empty_email_field);
                            toast.setView(view1);
                            int gravity = Gravity.BOTTOM;
                            toast.setGravity(gravity, 10, 10);
                            toast.show();
                        }
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

    private void sendCode(final String email) {
//        progressDialog.setMessage("Sending Password Reset Code");
//        progressDialog.show();
        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_SEND_CODE,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                Toast toast = new Toast(getApplicationContext());
                                View view1 = getLayoutInflater().inflate(R.layout.message, null);
                                TextView textView = view1.findViewById(R.id.message);
                                textView.setText(jsonObject.getString("message"));
                                toast.setView(view1);
                                int gravity = Gravity.BOTTOM;
                                toast.setGravity(gravity, 10, 10);
                                toast.show();
                                Intent intent = new Intent(getApplicationContext(), VerifyResetCodeActivity.class);
                                intent.putExtra("email", jsonObject.getString("email"));
                                startActivity(intent);
                            } else {
                                Toast toast = new Toast(getApplicationContext());
                                View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                                TextView textView = view1.findViewById(R.id.message);
                                textView.setText(jsonObject.getString("message"));
                                toast.setView(view1);
                                int gravity = Gravity.BOTTOM;
                                toast.setGravity(gravity, 10, 10);
                                toast.show();                            }
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
                        View view1 = getLayoutInflater().inflate(R.layout.network_error, null);
                        toast.setView(view1);
                        int gravity = Gravity.BOTTOM;
                        toast.setGravity(gravity, 10, 10);
                        toast.show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("email", email);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }
}
