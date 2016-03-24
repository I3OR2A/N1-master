package com.mmlab.n1;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mmlab.n1.adapter.LOIAdapter;
import com.mmlab.n1.adapter.LOISequenceAdapter;
import com.mmlab.n1.model.LOIModel;
import com.mmlab.n1.model.LOISequenceModel;
import com.mmlab.n1.save_data.SaveLOISequence;
import com.mmlab.n1.service.Encryption;
import com.mmlab.n1.service.HttpAsyncTask;
import com.mmlab.n1.service.TaskCompleted;
import com.ms.square.android.expandabletextview.ExpandableTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class LOIActivity extends AppCompatActivity implements TaskCompleted {

	private RecyclerView mRecyclerView;
	private LOISequenceAdapter mAdapter;
	private ArrayList<LOISequenceModel> loiSequenceList = new ArrayList<>();
	private MyApplication globalVariable;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loi);

		final LOIModel item = getIntent().getParcelableExtra("LOI-Content");
		globalVariable = (MyApplication) getApplicationContext();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		assert getSupportActionBar() != null;
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(item.getLOIName());

		ExpandableTextView mDescription = (ExpandableTextView) findViewById(R.id.expand_text_view);
		mDescription.setText(item.getLOIInfo());
		TextView docentName = (TextView) findViewById(R.id.docent_name);
		if (item.getIdentifier().equals("docent")) {
			try {
				docentName.setText(item.getContributorDetail().getString("name"));
                docentName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MaterialDialog materialDialog;
                        String alertMessage = "";
                        JSONObject obj = item.getContributorDetail();
                        materialDialog = new MaterialDialog.Builder(view.getContext())
                                .title("導覽員資訊")
                                .positiveText(R.string.confirm)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        // TODO
                                    }
                                }).build();
                        try {
                            alertMessage += "姓名:" + obj.getString("name");
                            alertMessage += "\n電話: " + obj.getString("telphone");
                            alertMessage += "\n手機: " + obj.getString("cellphone");
                            alertMessage += "\nEmail: " + obj.getString("email");
                            alertMessage += "\n地址: " + obj.getString("user_address");
                            alertMessage += "\n社群帳號: " + obj.getString("social_id");
                            alertMessage += "\n導覽解說地區: " + obj.getString("docent_area");
                            alertMessage += "\n導覽解說使用語言: " + obj.getString("docent_language");
                            alertMessage += "\n收費標準: " + obj.getString("charge");
                            alertMessage += "\n自我介紹: " + obj.getString("introduction");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        materialDialog.setMessage(alertMessage);
                        materialDialog.show();
                    }
                });
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		String type = "LOI-Sequence";
		String api = getResources().getString(R.string.api_loi_seq);
		String url = api + "id=" + item.getLOIId() + "&did=" + globalVariable.getDeviceID()
				+ "&appver=mini200&ulat=22.9942&ulng=120.1659&clang=" + globalVariable.getLanguage();

		Log.d("url", url);

		if(globalVariable.checkInternet()){
			new HttpAsyncTask(LOIActivity.this, "IP").execute(getResources().getString(R.string.api_clientIP));
			new HttpAsyncTask(LOIActivity.this, type).execute(url);
		}

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setAdapter(new LOISequenceAdapter(this, loiSequenceList));

//		mAdapter = new DataAdapter(loiSequenceList, mRecyclerView);
//
//		// set the adapter object to the Recyclerview
//		mRecyclerView.setAdapter(mAdapter);
//		//	 mAdapter.notifyDataSetChanged();



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
	public void onTaskComplete(String response, String type) {
		if (type.equals("IP")) {
			globalVariable.setIp(response.replaceAll("\n", ""));
			Log.d("ip", globalVariable.getIp());

		} else if(globalVariable.getIp()!=null) {
			if (type.equals("LOI-Sequence")) {
				String result = Encryption.decode(this, response, globalVariable.getIp());

				SaveLOISequence saveLOISequence = new SaveLOISequence(result);
				loiSequenceList = saveLOISequence.getLOISequenceList();
				for (int i = 0; i < loiSequenceList.size(); ++i) {
					if (loiSequenceList.get(i).getIdentifier().equals("docent")) {
						new HttpAsyncTask(LOIActivity.this, "contributor-detail:" + Integer.toString(i))
								.execute(getResources().getString(R.string.api_docent_info) + loiSequenceList.get(i).getContributor());
					}
				}
				mAdapter = new LOISequenceAdapter(this, loiSequenceList);
				mRecyclerView.setHasFixedSize(true);
				mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
				mRecyclerView.setAdapter(mAdapter);

				String hint = getString(R.string.find) + loiSequenceList.size() + getString(R.string.sites);
				Snackbar.make(mRecyclerView, hint, Snackbar.LENGTH_SHORT)
						.setAction("Action", null)
						.show();
			} else if (type.contains("contributor-detail")) {
				String result = Encryption.decode(this, response, globalVariable.getIp());
				try {
					JSONObject obj = (JSONObject) (new JSONObject(result)).getJSONArray("results").getJSONObject(0);
					int index = Integer.valueOf(type.substring("contributor-detail:".length()));

					loiSequenceList.get(index).setContributorDetail(obj);
				} catch (JSONException ex) {
					ex.printStackTrace();
				}
			}
		}
	}
}
