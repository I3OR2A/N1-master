package com.mmlab.n1.service;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;


import com.mmlab.n1.R;
import com.mmlab.n1.model.LOIModel;
import com.mmlab.n1.model.MyFavorite;
import com.mmlab.n1.model.POIModel;

import java.util.ArrayList;

/**
 * Created by waynewei on 2015/11/23.
 */
public class Filter {

	private Context mContext;

	public Filter(Context context){
		mContext = context;
	}

	public ArrayList<POIModel> poiFilter(ArrayList<POIModel> models, String query) {

		final ArrayList<POIModel> filteredModelList = new ArrayList<>();
		for (POIModel model : models) {
			final String text = model.getPOIName().toLowerCase();
			if (text.contains(query.toLowerCase())) {
				filteredModelList.add(model);
			}
		}
		if (filteredModelList.size() == 0) {
			Toast.makeText(mContext, mContext.getString(R.string.empty1) + query + mContext.getString(R.string.empty2), Toast.LENGTH_SHORT).show();
		}
		return filteredModelList;
	}


	public ArrayList<POIModel> identifierFilter(ArrayList<POIModel> models, String query) {

		final ArrayList<POIModel> filteredModelList = new ArrayList<>();
		for (POIModel model : models) {
			final String text = model.getIdentifier().toLowerCase();
			if (text.equals(query.toLowerCase())) {
				filteredModelList.add(model);
			}
		}
		return filteredModelList;
	}

	public ArrayList<POIModel> mediaFilter(ArrayList<POIModel> models, String query) {

		final ArrayList<POIModel> filteredModelList = new ArrayList<>();
		for (POIModel model : models) {
			if (!model.getMedia().isEmpty()){
				for(String media_format : model.getMedia()) {
					final String text = media_format.toLowerCase();
					if (text.equals(query.toLowerCase())) {
						filteredModelList.add(model);
						break;
					}
				}
			}
		}
		return filteredModelList;
	}

	public ArrayList<POIModel> categoryFilter(ArrayList<POIModel> models, String query) {

		final ArrayList<POIModel> filteredModelList = new ArrayList<>();
		for (POIModel model : models) {
			final String text = model.getPOIType1().toLowerCase();
			if (text.equals(query.toLowerCase())) {
				filteredModelList.add(model);
			}
		}
		return filteredModelList;
	}

	public ArrayList<MyFavorite> favoriteFilter(ArrayList<MyFavorite> models, String query) {

		final ArrayList<MyFavorite> filteredModelList = new ArrayList<>();
		for (MyFavorite model : models) {
			final String text = model.getTitle().toLowerCase();
			if (text.contains(query.toLowerCase())) {
				filteredModelList.add(model);
			}
		}
		if (filteredModelList.size() == 0) {
			Toast.makeText(mContext, mContext.getString(R.string.empty1) + query + mContext.getString(R.string.empty2), Toast.LENGTH_SHORT).show();
		}
		return filteredModelList;
	}
}
