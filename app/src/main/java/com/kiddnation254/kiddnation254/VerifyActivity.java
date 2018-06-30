package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.jkb.vcedittext.VerificationCodeEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class VerifyActivity extends AppCompatActivity {
    private VerificationCodeEditText verificationCodeEditText;
    private ProgressDialog progressDialog;
    private String email;
    private RelativeLayout progressBarContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            return;
        }
        email = extras.getString("email");

        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.GONE);

        verificationCodeEditText = (VerificationCodeEditText) findViewById(R.id.editTextCode);
        Button buttonVerify = (Button) findViewById(R.id.buttonVerify);

        progressDialog = new ProgressDialog(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Activate Account");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        buttonVerify.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String code = verificationCodeEditText.getText().toString().trim();
                        if(code.length() > 4) {
                            verifyCode(code, email);
                        } else {
                            Toast toast = new Toast(getApplicationContext());
                            View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                            TextView textView = view1.findViewById(R.id.message);
                            textView.setText(R.string.code_short);
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

    private void verifyCode(final String code, final String email){
//        progressDialog.setMessage("verifying");
//        progressDialog.show();
        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_VERIFY,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if(!jsonObject.getBoolean("error")){
                                SharedPrefManager.getInstance(getApplicationContext())
                                        .userLogin(
                                                jsonObject.getInt("id"),
                                                jsonObject.getString("username"),
                                                jsonObject.getString("email"),
                                                jsonObject.getString("phone"),
                                                jsonObject.getString("image_link")
                                        );
                                Toast toast = new Toast(getApplicationContext());
                                View view1 = getLayoutInflater().inflate(R.layout.message, null);
                                TextView textView = view1.findViewById(R.id.message);
                                textView.setText(R.string.activate_sucess);
                                toast.setView(view1);
                                int gravity = Gravity.BOTTOM;
                                toast.setGravity(gravity, 10, 10);
                                toast.show();
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                finish();
                            }else {
                                Toast toast = new Toast(getApplicationContext());
                                View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                                TextView textView = view1.findViewById(R.id.message);
                                textView.setText(jsonObject.getString("message"));
                                toast.setView(view1);
                                int gravity = Gravity.BOTTOM;
                                toast.setGravity(gravity, 10, 10);
                                toast.show();                            }
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "An error ocurred.\n Please try again.", Toast.LENGTH_LONG).show();
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
        ){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("code", code);
                params.put("email", email);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);
    }

}
