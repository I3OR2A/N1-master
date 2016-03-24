package com.mmlab.n1.save_data;

import android.util.Log;

import com.mmlab.n1.model.POIModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by waynewei on 2015/11/2.
 */
public class SavePOI {
	private ArrayList<POIModel> poiList;

//	public SavePOI(String response) {
//		try {
//			//JSON is the JSON code above
//			JSONObject jsonResponse = new JSONObject(response);
//			poiList = new ArrayList<>();
//			poiList.clear();
//			JSONArray results = jsonResponse.getJSONArray("results");
//			for (int i = 0; i < results.length(); i++) {
//				JSONObject object = results.getJSONObject(i);
//				String poiID = object.getString("POI_id");
//				String poiName = object.getString("POI_title");
//				String poiInfo = object.getString("POI_description");
//				Double distance = object.getDouble("distance")/1000;
//				String poiDistance = new DecimalFormat("#0.0").format(distance) + " 公里";
//				String poiAddress = object.getString("POI_address");
//				String poiSubject = object.getString("subject");
//				String poiType1 = object.getString("type1");
//
//				ArrayList<String> poiKeywords = new ArrayList<>();
//
//				String keyword1 = object.getString("keyword1");
//				if (!keyword1.isEmpty()) poiKeywords.add(keyword1);
//				String keyword2 = object.getString("keyword2");
//				if (!keyword2.isEmpty()) poiKeywords.add(keyword2);
//				String keyword3 = object.getString("keyword3");
//				if (!keyword3.isEmpty()) poiKeywords.add(keyword3);
//				String keyword4 = object.getString("keyword4");
//				if (!keyword4.isEmpty()) poiKeywords.add(keyword4);
//				String keyword5 = object.getString("keyword5");
//				if (!keyword5.isEmpty()) poiKeywords.add(keyword5);
//
//				String poiLatitude = object.getString("latitude");
//				String poiLongitude = object.getString("longitude");
//
//				String poiSource = object.getString("POI_source");
//
//				JSONArray pic = object.getJSONObject("PICs").getJSONArray("pic");
//				pics = new ArrayList<>();
//				audios = new ArrayList<>();
//				movies = new ArrayList<>();
//
//				for (int j = 0; j < pic.length(); j++) {
//					JSONObject obj = pic.getJSONObject(j);
//					poiPicUrl = obj.getString("url");
//					if (poiPicUrl.matches("^(.*\\.((jpg)$))?[^.]*$")) {
////						Log.d("pic", poiPicUrl);
//						pics.add(poiPicUrl);
//					} else if (poiPicUrl.matches("^(.*\\.((aac)$))?[^.]*$")) {
//						Log.d("aac", poiPicUrl);
//						audios.add(poiPicUrl);
//					} else if (poiPicUrl.matches("^(.*\\.((mp4)$))?[^.]*$")) {
//						Log.d("mp4", poiPicUrl);
//						movies.add(poiPicUrl);
//					}
//
//				}
//
//				poiList.add(new POIModel(poiID, poiName, poiDistance, poiSubject, poiType1, poiAddress, poiInfo, poiLatitude, poiLongitude, poiSource, poiKeywords, pics, audios, movies));
//				Log.d("POI", poiName + " " + pics + " " + audios + " " + poiSource);
//
//			}
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//	}

	public POIModel parsePoiJSONObject(String result){
		POIModel poiModel = new POIModel();
		try {
				JSONObject object = new JSONObject(result);
				poiModel.setJsonObject(object);
				poiModel.setPOIId(object.getString("POI_id"));
				poiModel.setPOIName(object.getString("POI_title"));
				poiModel.setPOIInfo(object.getString("POI_description"));
				Double distance = object.getDouble("distance")/1000;
				poiModel.setPOIDistance(new DecimalFormat("#0.0").format(distance));
				poiModel.setPOIAddress(object.getString("POI_address"));
				poiModel.setPOISubject(object.getString("subject"));
				poiModel.setPOIType1(object.getString("type1"));

				if(object.getString("identifier").equals("docent"))
					poiModel.setIdentifier("narrator");
				else
					poiModel.setIdentifier(object.getString("identifier"));

				poiModel.setContributor(object.getString("contributor"));
				poiModel.setOpen(object.getString("open"));
				ArrayList<String> poiKeywords = new ArrayList<>();

				String keyword1 = object.getString("keyword1");
				if (!keyword1.isEmpty()) poiKeywords.add(keyword1);
				String keyword2 = object.getString("keyword2");
				if (!keyword2.isEmpty()) poiKeywords.add(keyword2);
				String keyword3 = object.getString("keyword3");
				if (!keyword3.isEmpty()) poiKeywords.add(keyword3);
				String keyword4 = object.getString("keyword4");
				if (!keyword4.isEmpty()) poiKeywords.add(keyword4);
				String keyword5 = object.getString("keyword5");
				if (!keyword5.isEmpty()) poiKeywords.add(keyword5);

				poiModel.setPOIKeywords(poiKeywords);

				poiModel.setPOILat(object.getString("latitude"));
				poiModel.setPOILong(object.getString("longitude"));

				poiModel.setPOISource(object.getString("POI_source"));

				JSONArray pic = object.getJSONObject("PICs").getJSONArray("pic");

				ArrayList<String> urls = new ArrayList<>();
				ArrayList<String> pics = new ArrayList<>();
				ArrayList<String> audios = new ArrayList<>();
				ArrayList<String> movies = new ArrayList<>();

				for (int j = 0; j < pic.length(); j++) {
					JSONObject obj = pic.getJSONObject(j);
					String poiPicUrl = obj.getString("url");
					urls.add(poiPicUrl);
					if (poiPicUrl.matches("^(.*\\.((jpg)$))?[^.]*$")) {
						pics.add(poiPicUrl);
						poiModel.setMedia("photo");
					} else if (poiPicUrl.matches("^(.*\\.((aac)$))?[^.]*$")) {
						audios.add(poiPicUrl);
						poiModel.setMedia("audio");
					} else if (poiPicUrl.matches("^(.*\\.((mp4)$))?[^.]*$")) {
						movies.add(poiPicUrl);
						poiModel.setMedia("movie");
					}
				}
				poiModel.setUrl(urls);
				poiModel.setPOIPics(pics);
				poiModel.setPOIAudios(audios);
				poiModel.setPOIMovies(movies);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return poiModel;
	}

	public void parsePoiListJSONObject(String response) {
		try {
			//JSON is the JSON code above
			JSONObject jsonResponse = new JSONObject(response);
			poiList = new ArrayList<>();
			poiList.clear();
			JSONArray results = jsonResponse.getJSONArray("results");
			for (int i = 0; i < results.length(); i++) {
				JSONObject poiObject = results.getJSONObject(i);
				poiList.add(parsePoiJSONObject(poiObject.toString()));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<POIModel> getPOIList() {
		return poiList;
	}
}
