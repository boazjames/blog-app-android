package com.kiddnation254.kiddnation254;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
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

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextEmail, editTextPhone, editTextPassword, editTextConfirmPassword;
    private RelativeLayout progressBarContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (SharedPrefManager.getInstance(this).isLoggedIn()) {
            finish();
            startActivity(new Intent(this, HomeActivity.class));
            return;
        }

        progressBarContainer = (RelativeLayout) findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.GONE);

        editTextUsername = (EditText) findViewById(R.id.username);
        editTextEmail = (EditText) findViewById(R.id.email);
        editTextPhone = (EditText) findViewById(R.id.phone);
        editTextPassword = (EditText) findViewById(R.id.password);
        editTextConfirmPassword = (EditText) findViewById(R.id.confirmPassword);
        Button buttonRegister = (Button) findViewById(R.id.buttonRegister);

        TextView textViewGoToLogin = (TextView) findViewById(R.id.goToLogin);

        textViewGoToLogin.setOnClickListener(
                new TextView.OnClickListener() {
                    public void onClick(View v) {
                        goToLogin();
                    }
                }
        );

        buttonRegister.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String username = editTextUsername.getText().toString().trim();
                        final String email = editTextEmail.getText().toString().trim();
                        final String phone = editTextPhone.getText().toString().trim();
                        final String password = editTextPassword.getText().toString().trim();
                        final String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                        if (username.length() == 0 || email.length() == 0 || phone.length() == 0 ||
                                password.length() == 0 || confirmPassword.length() == 0) {
                            Toast toast = new Toast(getApplicationContext());
                            View view1 = getLayoutInflater().inflate(R.layout.empty_field, null);
                            toast.setView(view1);
                            toast.setDuration(Toast.LENGTH_LONG);
                            int gravity = Gravity.BOTTOM;

                            toast.setGravity(gravity, 10, 10);
                            toast.show();
                        } else if (password.length() < 8) {
                            Toast toast = new Toast(getApplicationContext());
                            View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                            TextView textView = view1.findViewById(R.id.message);
                            textView.setText(R.string.short_password);
                            toast.setView(view1);
                            toast.setDuration(Toast.LENGTH_LONG);
                            int gravity = Gravity.BOTTOM;

                            toast.setGravity(gravity, 10, 10);
                            toast.show();
                        } else if (!password.equals(confirmPassword)) {
                            Toast toast = new Toast(getApplicationContext());
                            View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                            TextView textView = view1.findViewById(R.id.message);
                            textView.setText(R.string.password_mismatch);
                            toast.setView(view1);
                            toast.setDuration(Toast.LENGTH_LONG);
                            int gravity = Gravity.BOTTOM;

                            toast.setGravity(gravity, 10, 10);
                            toast.show();
                        } else if (!Helpers.isValidEmail(email)) {
                            Toast toast = new Toast(getApplicationContext());
                            View view1 = getLayoutInflater().inflate(R.layout.warning, null);
                            TextView textView = view1.findViewById(R.id.message);
                            textView.setText(R.string.invalid_email);
                            toast.setView(view1);
                            toast.setDuration(Toast.LENGTH_LONG);
                            int gravity = Gravity.BOTTOM;

                            toast.setGravity(gravity, 10, 10);
                            toast.show();
                        } else {
                            registerUser();
                        }
                    }
                }
        );
    }

    public void registerUser() {
        final String username = editTextUsername.getText().toString().trim();
        final String email = editTextEmail.getText().toString().trim();
        final String phone = editTextPhone.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        final String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        progressBarContainer.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        StringRequest stringRequest = new StringRequest(
                Request.Method.POST,
                Constants.URL_REGISTER,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBarContainer.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {
                                Toast toast = new Toast(getApplicationContext());
                                View view = getLayoutInflater().inflate(R.layout.message, null);
                                TextView textView = view.findViewById(R.id.message);
                                textView.setText(jsonObject.getString("message"));
                                toast.setView(view);
                                int gravity = Gravity.BOTTOM;
                                toast.setGravity(gravity, 10, 10);
                                toast.show();

                                Intent intent = new Intent(getApplicationContext(), VerifyActivity.class);
                                intent.putExtra("email", jsonObject.getString("email"));
                                startActivity(intent);
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
//                            e.printStackTrace();
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
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("email", email);
                params.put("phone", phone);
                params.put("password", password);
                params.put("confirm_password", confirmPassword);
                return params;
            }
        };

        RequestHandler.getInstance(this).addToRequestQueue(stringRequest);

    }

    public void goToVerify() {
        startActivity(new Intent(this, VerifyActivity.class));
        finish();
    }

    public void goToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

}
