package com.mmlab.n1;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


import com.mmlab.n1.adapter.FriendAdapter;
import com.mmlab.n1.model.Friend;
import com.mmlab.n1.model.User;
import com.mmlab.n1.widget.NavigationDrawer;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class FriendActivity extends AppCompatActivity {

	private MyApplication globalVariable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friend);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		assert getSupportActionBar() != null;
		getSupportActionBar().setTitle(R.string.drawer_item_friends);

		globalVariable = (MyApplication) getApplicationContext();
		NavigationDrawer navigationDrawer = new NavigationDrawer(this, toolbar);
		globalVariable.createDrawer(navigationDrawer);


		Realm realm = Realm.getInstance(this);
		RealmResults<User> userResult = realm.where(User.class)
				.findAll();

		ArrayList<Friend> mModels = new ArrayList<>();

		for (User user : userResult) {
			for (Friend friend : user.getFriends()) {
				if (friend.isValid())
					mModels.add(friend);
				Log.d("test", friend.toString());
			}
		}

		RecyclerView mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(FriendActivity.this));
		FriendAdapter mAdapter = new FriendAdapter(FriendActivity.this, mModels);
		mRecyclerView.setAdapter(mAdapter);

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
