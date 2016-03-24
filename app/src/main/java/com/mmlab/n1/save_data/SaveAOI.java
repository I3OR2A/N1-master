package com.mmlab.n1.save_data;

import android.util.Log;


import com.mmlab.n1.model.LOIModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by waynewei on 2015/11/2.
 */
public class SaveAOI {
	private ArrayList<LOIModel> aoiList;

	public SaveAOI(String response) {
		try {
			//JSON is the JSON code above
			JSONObject jsonResponse = new JSONObject(response);
			aoiList = new ArrayList<>();

			JSONArray results = jsonResponse.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject object = results.getJSONObject(i);
				String loiID = object.getString("id");
				String loiName = object.getString("title");
				String loiInfo = object.getString("description");
				String con = null;
				if (object.has("username")){
					con = object.getString("username");
				}
				String identifier = object.getString("identifier");

				aoiList.add(new LOIModel(loiID, loiName, loiInfo, con, identifier));
				Log.d("Route", loiID + " " + loiName + " " + loiInfo);

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<LOIModel> getAOIList() {
		return aoiList;
	}
}
