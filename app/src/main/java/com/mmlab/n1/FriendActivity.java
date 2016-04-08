package com.mmlab.n1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.mmlab.n1.adapter.FriendAdapter;
import com.mmlab.n1.model.Friend;
import com.mmlab.n1.model.User;
import com.mmlab.n1.widget.NavigationDrawer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

public class FriendActivity extends AppCompatActivity {

	private MyApplication globalVariable;
	private Realm realm;
	private User user;
	private RecyclerView mRecyclerView;
	private FriendAdapter mAdapter;
	private ArrayList<Friend> friendList = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FacebookSdk.sdkInitialize(getApplicationContext());
		setContentView(R.layout.activity_friend);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		assert getSupportActionBar() != null;
		getSupportActionBar().setTitle(R.string.drawer_item_friends);

		globalVariable = (MyApplication) getApplicationContext();
		NavigationDrawer navigationDrawer = new NavigationDrawer(this, toolbar);
		globalVariable.createDrawer(navigationDrawer);
		realm = Realm.getInstance(this);

		setFriendList();

		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(FriendActivity.this));
		mAdapter = new FriendAdapter(FriendActivity.this, friendList);
		mRecyclerView.setAdapter(mAdapter);

	}

	public void setFriendList(){
		friendList = new ArrayList<>();
		RealmResults<User> userResult = realm.where(User.class)
				.findAll();

		for (User user : userResult) {
			this.user = user;
			for (Friend friend : user.getFriends()) {
				if (friend.isValid())
					friendList.add(friend);
				Log.d("test", friend.toString());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_friend, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if (id == android.R.id.home) {
			finish();
			return true;
		}
		if(id == R.id.update_friends){

			Bundle parameters = new Bundle();
			parameters.putString("fields", "friends{name,picture}");
			if(globalVariable.checkInternet()) {
				new GraphRequest(
						AccessToken.getCurrentAccessToken(),
						"me",
						parameters,
						HttpMethod.GET,
						new GraphRequest.Callback() {
							public void onCompleted(GraphResponse response) {

								JSONObject object = response.getJSONObject();
								RealmList<Friend> friends = new RealmList<Friend>();
								realm.beginTransaction();
								user.getFriends().clear();
								realm.commitTransaction();
								if (object.has("friends")) {

									JSONArray jsonArray = object.optJSONObject("friends").optJSONArray("data");
									String friendName, friendId, friendPhoto;
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

								setFriendList();
								mAdapter.updateItems(friendList);

							}
						}
				).executeAsync();
			}
			else {
				globalVariable.noticeInternet(this, mRecyclerView);
			}

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		//handle the back press :D close the drawer first and if the drawer is closed close the activity
		if (globalVariable.getDrawer() != null && globalVariable.getDrawer().isDrawerOpen()) {
			globalVariable.getDrawer().closeDrawer();
		}
		finish();

	}

}
