package com.mmlab.n1;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mmlab.n1.adapter.MyFavoriteAdapter;
import com.mmlab.n1.model.MyFavorite;
import com.mmlab.n1.service.Filter;
import com.mmlab.n1.widget.NavigationDrawer;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;

public class MyFavoriteActivity extends AppCompatActivity {

	private Realm realm;
	private MyFavoriteAdapter mAdapter;
	private RecyclerView mRecyclerView;
	private ArrayList<MyFavorite> mModels;
	private MyApplication globalVariable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_favorite);


		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		assert getSupportActionBar() != null;
		getSupportActionBar().setTitle(R.string.drawer_item_favorite);

		globalVariable = (MyApplication) getApplicationContext();

		NavigationDrawer navigationDrawer = new NavigationDrawer(this, toolbar);

		realm = Realm.getInstance(this);

		globalVariable.createDrawer(navigationDrawer);

		RealmResults<MyFavorite> myFavorites = realm.where(MyFavorite.class)
				.findAll();

		Log.d("My favorite", myFavorites.toString());

		mModels = new ArrayList<>();

		for (MyFavorite favorite: myFavorites) {
			if (favorite.isValid())
				mModels.add(favorite);
		}

		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(MyFavoriteActivity.this));
		mAdapter = new MyFavoriteAdapter(MyFavoriteActivity.this, mModels);
		mRecyclerView.setAdapter(mAdapter);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		//add the values which need to be saved from the drawer to the bundle
		outState = globalVariable.getDrawer().saveInstanceState(outState);
		outState = globalVariable.getHeader().saveInstanceState(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_my_favorite, menu);

		if(mModels.size()==0){
			menu.findItem(R.id.action_search).setVisible(false);
			menu.findItem(R.id.clear).setVisible(false);
		}

		final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
		searchView.setQueryHint(getResources().getString(R.string.search));
		searchView.setOnSearchClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//hide  action item
				menu.findItem(R.id.clear).setVisible(false);
			}
		});
		searchView.setOnCloseListener(new SearchView.OnCloseListener() {
			@Override
			public boolean onClose() {
				menu.findItem(R.id.clear).setVisible(true);
				return false;
			}
		});
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			@Override
			public boolean onQueryTextSubmit(String query) {

				return false;
			}

			@Override
			public boolean onQueryTextChange(String query) {

				Filter filter = new Filter(getApplicationContext());
				final ArrayList<MyFavorite> filteredModelList = filter.favoriteFilter(mModels, query.toLowerCase());
				Log.d("Check Size", String.valueOf(filteredModelList.size()));
				mAdapter.animateTo(filteredModelList);
				mRecyclerView.scrollToPosition(0);
				mRecyclerView.setAdapter(mAdapter);

				return true;
			}
		});

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
		if (id == R.id.clear) {

				new MaterialDialog.Builder(MyFavoriteActivity.this)
						.title(R.string.clear)
						.content(R.string.clear_detail)
						.positiveText(R.string.confirm)
						.negativeText(R.string.cancel)
						.callback(new MaterialDialog.ButtonCallback() {
							@Override
							public void onPositive(MaterialDialog dialog) {
								realm.beginTransaction();
								realm.clear(MyFavorite.class);
								realm.commitTransaction();
								mModels.clear();
								mAdapter = new MyFavoriteAdapter(MyFavoriteActivity.this, mModels);
								mRecyclerView.setAdapter(mAdapter);
								Snackbar.make(mRecyclerView, getResources().getString(R.string.clear_all), Snackbar.LENGTH_SHORT)
										.show();
							}
						})
						.show();

			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume(){
		super.onResume();

		RealmResults<MyFavorite> myFavorites = realm.where(MyFavorite.class)
				.findAll();

		Log.d("My favorite", myFavorites.toString());

		mModels = new ArrayList<>();

		for (MyFavorite favorite: myFavorites) {
			if (favorite.isValid())
				mModels.add(favorite);
		}
		mAdapter = new MyFavoriteAdapter(MyFavoriteActivity.this, mModels);
		mRecyclerView.setAdapter(mAdapter);

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
