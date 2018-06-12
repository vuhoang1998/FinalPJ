package com.example.hoang.final_project.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.hoang.final_project.R;
import com.example.hoang.final_project.events.OnClickProfile;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.facebook.login.widget.ProfilePictureView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "Main Activity";
    CallbackManager callbackManager = CallbackManager.Factory.create();
    @BindView(R.id.tv_fb_name)
    TextView tvFbName;
    @BindView(R.id.tv_fb_mail)
    TextView tvFbMail;
    @BindView(R.id.iv_profile)
    ProfilePictureView ivProfile;
    @BindView(R.id.login_button)
    LoginButton loginButton;
    Profile profile;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile", "email", "user_friends"));

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        callbackManager = CallbackManager.Factory.create();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("user");

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.d(TAG, "LoginActivity: " + response.toString());
                                        profile = Profile.getCurrentProfile();
                                        id = profile.getId();

                                        ivProfile.setVisibility(View.VISIBLE);
                                        ivProfile.setProfileId(profile.getId());

                                        //Application code
                                        try {
                                            String email = object.getString("email");
                                            String name = profile.getName();
                                            Log.d(TAG, "onCompleted: " + email);
                                            Log.d(TAG, "onCompleted: " + name);
                                            tvFbMail.setVisibility(View.VISIBLE);
                                            tvFbName.setVisibility(View.VISIBLE);
                                            tvFbName.setText(name);
                                            tvFbMail.setText(email);
                                        } catch (JSONException e) {
                                            Log.d(TAG, "onCompleted: "+ "abcdef");
                                            e.printStackTrace();
                                        }

                                        if (databaseReference != null) {
                                            databaseReference.orderByChild("id").equalTo(id).addListenerForSingleValueEvent(
                                                    new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            Log.d(TAG, "onDataChange: " + id);
                                                            Log.d(TAG, "onDataChange: " + dataSnapshot);
                                                            int size = 0;
                                                            for (DataSnapshot bookSnapshot : dataSnapshot.getChildren()) {
                                                                Log.d(TAG, "onDataChange: " + bookSnapshot.getKey());
                                                                size++;
                                                                if (size == 1) break;
                                                            }

                                                            if (size == 0){
                                                                EventBus.getDefault()
                                                                        .postSticky(new OnClickProfile(profile));
                                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                                startActivity(intent);
                                                            }

                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    }
                                            );
                                        }
                                        else {
                                            Log.d(TAG, "onSuccess: "+ "chay vao di");
                                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                            startActivity(intent);
                                        }

                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {

                        Log.v("LoginActivity", "cancel");
                        LoginManager.getInstance().logOut();

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.v("LoginActivity", error.getMessage());
                    }
                });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.login_button)
    public void onViewClicked() {

    }

}
