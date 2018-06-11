package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class ResetPasswordActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private EditText editTextPassword;
    private EditText editTextConfirm_password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        final String email = extras.getString("email");
        Button buttonReset = (Button) findViewById(R.id.buttonReset);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextConfirm_password = (EditText) findViewById(R.id.confirmPassword);

        progressDialog = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset Password");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonReset.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String password = editTextPassword.getText().toString().trim();
                        String confirm_password = editTextConfirm_password.getText().toString().trim();
                        if (password.length() < 8) {
                            Toast.makeText(getApplicationContext(),
                                    "Password length must be at least 8 characters",
                                    Toast.LENGTH_LONG).show();

                        } else if (!password.equals(confirm_password)) {
                            Toast.makeText(getApplicationContext(), "Password does not match",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            resetPassword(password, email);
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

    private void resetPassword(final String password, final String email) {
        progressDialog.setMessage("Resetting password");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_RESET_PASSWORD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.hide();

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"),
                                        Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getApplicationContext(), jsonObject.getString("message"), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.hide();
                        Toast.makeText(getApplicationContext(),
                                "Network error please try again.", Toast.LENGTH_LONG).show();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("password", password);
                params.put("email", email);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }
}
