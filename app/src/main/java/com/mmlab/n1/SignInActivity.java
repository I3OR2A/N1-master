package com.mmlab.n1;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.mmlab.n1.model.Friend;
import com.mmlab.n1.model.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmList;

public class SignInActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private ImageView image;
    private TextView info;
    private LoginButton loginButton;
    private String device_id;
    private String language;
    private String friendPhoto;
    private String friendName;
    private String friendId;
    private String userCover;
    private String userEmail;
    private Realm realm;
    private ProfileTracker mProfileTracker;
    private FacebookCallback<LoginResult> mFacebookCallback = new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            loginButton.setVisibility(View.GONE);

            realm = Realm.getInstance(getApplicationContext());
            realm.beginTransaction();
            final User user = realm.createObject(User.class);

            if (Profile.getCurrentProfile() == null) {
                mProfileTracker = new ProfileTracker() {
                    @Override
                    protected void onCurrentProfileChanged(Profile profile, Profile profile2) {
                        Log.v("facebook - profile", profile2.getFirstName());
                        mProfileTracker.stopTracking();
                    }
                };
                mProfileTracker.startTracking();
            } else {
                Profile profile = Profile.getCurrentProfile();
                Log.v("facebook - profile", profile.getFirstName());
                Log.d("Profile", String.valueOf(profile));
                final String userPhoto = profile.getProfilePictureUri(150, 150).toString();
                Log.d("Image", userPhoto);
                user.setPhoto(userPhoto);
            }

            Bundle parameters = new Bundle();
            parameters.putString("fields", "name,email,cover,friends{name,picture}");

            new GraphRequest(
                    AccessToken.getCurrentAccessToken(),
                    "me",
                    parameters,
                    HttpMethod.GET,
                    new GraphRequest.Callback() {
                        public void onCompleted(GraphResponse response) {
                            Log.d("response", response.toString());
                            JSONObject object = response.getJSONObject();

                            String userId = object.optString("id");
                            user.setId(userId);

                            String userName = object.optString("name");
                            user.setName(userName);

                            if (object.has("email")) {
                                userEmail = object.optString("email");
                                user.setEmail(userEmail);
                            }

                            if (object.has("cover")) {
                                userCover = object.optJSONObject("cover").optString("source");
                                user.setCover(userCover);
                            }

                            user.setDeviceId(device_id);
                            user.setLanguage(language);

                            realm.commitTransaction();

                            RealmList<Friend> friends = new RealmList<Friend>();

                            if (object.has("friends")) {
                                JSONArray jsonArray = object.optJSONObject("friends").optJSONArray("data");

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    realm.beginTransaction();
                                    final Friend friend = realm.createObject(Friend.class);
                                    try {
                                        friendName = jsonArray.getJSONObject(i).getString("name");
                                        Log.d("friend", friendName);
                                        friend.setName(friendName);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        friendId = jsonArray.getJSONObject(i).getString("id");
                                        Log.d("friend", friendId);
                                        friend.setId(friendId);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        friendPhoto = jsonArray.getJSONObject(i).getJSONObject("picture").optJSONObject("data").optString("url");
                                        Log.d("friend", friendPhoto);
                                        friend.setPhotoUrl(friendPhoto);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    user.getFriends().add(friend);
                                    realm.commitTransaction();
                                }

                            }


                            info.setText("Welcome " + userName);

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    intent.putExtra("refresh_drawer", true);
                                    startActivity(intent);
                                    finish();
                                }
                            }, 1500);

                        }
                    }
            ).executeAsync();

        }

        @Override
        public void onCancel() {
            Log.d("Login Status", "Login attempt canceled");
        }

        @Override
        public void onError(FacebookException e) {
            Log.d("Login Status", "Login attempt failed");
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_sign_in);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.drawer_item_sign_in);

        info = (TextView) findViewById(R.id.info);
        image = (ImageView) findViewById(R.id.image);
        setupLoginButton();

        device_id = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        language = Locale.getDefault().getDisplayLanguage();
        if (language.equals("中文"))
            language = "zh-tw";

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }

    private void setupLoginButton() {
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.facebook, 0, 0, 0);
        loginButton.setPadding(28, 28, 28, 28);
        loginButton.setTextSize(24);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email", "user_friends"));
        loginButton.registerCallback(mCallbackManager, mFacebookCallback);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
