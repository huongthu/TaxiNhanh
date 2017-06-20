package com.example.thu.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.method.KeyListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.thu.taxinhanh.LoginActivity;
import com.example.thu.taxinhanh.MainActivity;
import com.example.thu.taxinhanh.R;
import com.example.thu.taxinhanh.RegisterActivity;
import com.example.thu.utils.Enums;
import com.github.nkzawa.emitter.Emitter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by thu on 6/19/2017.
 */

public class ProfileFragment extends Fragment {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.activity_profile, null);
        final EditText etFullName = (EditText) root.findViewById(R.id.etFullName);
        final EditText etPhone = (EditText) root.findViewById(R.id.etPhoneNumber);
        final EditText etEmail = (EditText) root.findViewById(R.id.etEmail);

        Button btnLogout = (Button) root.findViewById(R.id.btnLogOut);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != FirebaseAuth.getInstance().getCurrentUser()) {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        etEmail.setKeyListener(null);
        final KeyListener phoneListener = etPhone.getKeyListener();
        etPhone.setKeyListener(null);

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            return root;
        }

        etFullName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        event.getAction() == KeyEvent.ACTION_DOWN &&
                                event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    if (!event.isShiftPressed()) {
                        final EditText etFullName = (EditText) getActivity().findViewById(R.id.etFullName);
                        final EditText etPhone = (EditText) getActivity().findViewById(R.id.etPhoneNumber);
                        updateUserInformation(etFullName.getText().toString(), etPhone.getText().toString());

                        return true; // consume.
                    }
                }
                return false;
            }
        });
        etPhone.setOnEditorActionListener(updateUserInformationListener);

        etPhone.setOnFocusChangeListener(finishEditting);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    etFullName.setText(mAuth.getCurrentUser().getDisplayName());
                    etEmail.setText(mAuth.getCurrentUser().getEmail());

                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mDatabase.child(getResources().getString(R.string.db_child_phone_number)).child(user.getUid())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    String phoneNumber = (String)dataSnapshot.getValue();
                                    etPhone.setText(phoneNumber);
                                    etPhone.setTypeface(null, Typeface.NORMAL);
                                    etPhone.setKeyListener(phoneListener);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                }
            }
        };
        mAuth.addAuthStateListener(mAuthListener);

        return root;
    }

    private TextView.OnEditorActionListener updateUserInformationListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    event.getAction() == KeyEvent.ACTION_DOWN &&
                            event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                if (!event.isShiftPressed()) {
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput (InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_HIDDEN);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                    final EditText etFullName = (EditText) getActivity().findViewById(R.id.etFullName);
                    final EditText etPhone = (EditText) getActivity().findViewById(R.id.etPhoneNumber);
                    updateUserInformation(etFullName.getText().toString(), etPhone.getText().toString());

                    return true; // consume.
                }
            }
            return false;
        }
    };

    private View.OnFocusChangeListener finishEditting = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput (InputMethodManager.SHOW_FORCED, InputMethodManager.RESULT_HIDDEN);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    };

    private void updateUserInformation(String fullName, final String phoneNumber) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(fullName)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference
                                    (getResources().getString(R.string.db_child_phone_number) + "/" + user.getUid());
                            myRef.setValue(phoneNumber);

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), getResources().getText(R.string.update_successful),Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });
    }
}
