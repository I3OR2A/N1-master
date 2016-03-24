package com.mmlab.n1.save_data;

import android.util.Log;


import com.mmlab.n1.model.LOIModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by waynewei on 2015/11/2.
 */
public class SaveLOI {
	private ArrayList<LOIModel> loiList;

	public SaveLOI(String response) {
		try {
			//JSON is the JSON code above
			JSONObject jsonResponse = new JSONObject(response);
			loiList = new ArrayList<>();

			JSONArray results = jsonResponse.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject object = results.getJSONObject(i);
				String loiID = object.getString("routeid");
				String loiName = object.getString("routetitle");
				Double time = object.getDouble("duration") / 60;
				String loiDuration = new DecimalFormat("#0.0").format(time) + " 小時";
				String loiInfo = object.getString("routedescription");
				String con = null;
				if (object.has("username")){
					con = object.getString("username");
				}
				String identifier = object.getString("identifier");

				loiList.add(new LOIModel(loiID, loiName, loiDuration, loiInfo, con, identifier));
				Log.d("Route", loiID + " " + loiName + " " + loiDuration + " " + loiInfo);

			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<LOIModel> getLOIList() {
		return loiList;
	}
}
