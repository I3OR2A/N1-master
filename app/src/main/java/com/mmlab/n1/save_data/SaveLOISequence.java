package com.mmlab.n1.save_data;

import android.util.Log;


import com.mmlab.n1.model.LOISequenceModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by waynewei on 2015/11/2.
 */
public class SaveLOISequence {

    private ArrayList<LOISequenceModel> loiSequenceList;

    public SaveLOISequence(String response) {
        try {
            //JSON is the JSON code above
            JSONObject jsonResponse = new JSONObject(response);
            loiSequenceList = new ArrayList<>();
            loiSequenceList.clear();
            JSONArray results = jsonResponse.getJSONArray("POIsequence");
            for (int i = 0; i < results.length(); i++) {
                JSONObject object = results.getJSONObject(i);
                String poiId = object.getString("POIid");
                String poiTitle = object.getString("POItitle");
                String poiDescription = object.getString("description");
                String open = object.getString("open");
                String contributor = object.getString("rights");
                String identifier = object.getString("identifier");
                loiSequenceList.add(new LOISequenceModel(poiId, poiTitle, poiDescription, open, contributor, identifier));
                Log.d("LOISequence", poiId + " " + poiTitle);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<LOISequenceModel> getLOISequenceList() {
        return loiSequenceList;
    }
}
