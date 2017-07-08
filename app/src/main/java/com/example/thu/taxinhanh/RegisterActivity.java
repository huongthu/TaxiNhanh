package com.example.thu.taxinhanh;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.thu.utils.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    startActivity(intent);
                    RegisterActivity.this.finish();
                }
            }
        };



        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((TextView) findViewById(R.id.etEmail)).getText().toString();
                String password = ((TextView) findViewById(R.id.etPassword)).getText().toString();
                String password2 = ((TextView) findViewById(R.id.etPassword2)).getText().toString();
                String fullName = ((TextView) findViewById(R.id.etFullname)).getText().toString();
                String phoneNumber = ((TextView) findViewById(R.id.etPhoneNumber)).getText().toString();

                if (!password.equals(password2)) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, getResources().getText(R.string.password_not_match),Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                registerWithFirebase(email, password, fullName, phoneNumber);
            }
        });
    }

    private void registerWithFirebase(final String email, String password, final String name, final String phoneNumber) {
        if (Utils.isNullOrEmpty(email, password, name, phoneNumber)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(RegisterActivity.this, getResources().getText(R.string.plz_provide_full_register_info),Toast.LENGTH_LONG).show();
                }
            });

            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    StringRequest postRequest = new StringRequest(Request.Method.POST, getResources().getString(R.string.register_url),
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    try {
                                        final JSONObject obj = new JSONObject(response);
                                        final String mesage = obj.getString("message");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RegisterActivity.this, mesage,Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    } catch (JSONException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(RegisterActivity.this, getResources().getText(R.string.register_failed),Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    mAuth.addAuthStateListener(mAuthListener);
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    error.printStackTrace();
                                }
                            }
                    ) {
                        @Override
                        protected Map<String, String> getParams()
                        {
                            Map<String, String>  params = new HashMap<>();
                            // the POST parameters:
                            params.put("registerType", "self");
                            params.put("userType", "customer");

                            params.put("_uid", user.getUid());
                            params.put("email", email);
                            params.put("phone", phoneNumber);
                            params.put("fullName", name);

                            params.put("Content-Type", "application/json");
                            return params;
                        }
                    };
                    RequestQueue mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
                    mRequestQueue.add(postRequest);
                    mRequestQueue.start();

//                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
//                                    .setDisplayName(name)
//                                    .build();
//
//                            user.updateProfile(profileUpdates)
//                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                        @Override
//                                        public void onComplete(@NonNull Task<Void> task) {
//                                            if (task.isSuccessful()) {
//                                                FirebaseDatabase database = FirebaseDatabase.getInstance();
//                                                DatabaseReference myRef = database.getReference
//                                                        (getResources().getString(R.string.db_child_phone_number) + "/" + user.getUid());
//                                                myRef.setValue(phoneNumber);
//
//                                                runOnUiThread(new Runnable() {
//                                                    @Override
//                                                    public void run() {
//                                                        Toast.makeText(RegisterActivity.this, getResources().getText(R.string.register_successful),Toast.LENGTH_LONG).show();
//                                                    }
//                                                });
//                                            }
//                                        }
//                                    });
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(RegisterActivity.this, getResources().getText(R.string.register_failed),Toast.LENGTH_LONG).show();
                        }
                    });
                }
                }
            });
    }

    private class SendPostToServer extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return Utils.getContentFromUrl(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            ((LinearLayout)findViewById(R.id.llLoading)).setVisibility(View.GONE);
            //https://stackoverflow.com/questions/17939760/how-to-solve-android-os-networkonmainthreadexception-in-json
            if (android.os.Build.VERSION.SDK_INT > 9) {
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                        .permitAll().build();

                StrictMode.setThreadPolicy(policy);
            }
        }

        @Override
        protected void onPreExecute() {
            ((LinearLayout)findViewById(R.id.llLoading)).setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) { }
    }
}
