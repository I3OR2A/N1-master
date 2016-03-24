package com.mmlab.n1.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.mmlab.n1.R;
import com.mmlab.n1.view.TouchImageView;

import java.util.ArrayList;

/**
 * Created by waynewei on 2015/10/30.
 */
public class ImageAdapter extends PagerAdapter {

	private Context mContext;
	;
	private ArrayList<String> _imagePaths;
	private LayoutInflater inflater;

	// constructor
	public ImageAdapter(Context context, ArrayList<String> imagePaths) {
		mContext = context;
		this._imagePaths = imagePaths;
	}

	@Override
	public int getCount() {
		return this._imagePaths.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == ((View) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {

		inflater = LayoutInflater.from(mContext);
		View viewLayout = inflater.inflate(R.layout.fullscreen_image, container,
				false);

		TouchImageView imgDisplay = (TouchImageView) viewLayout.findViewById(R.id.imgDisplay);

		Glide.with(mContext).load(_imagePaths.get(position)).fitCenter().into(imgDisplay);
		Log.d("current", String.valueOf(position));

		container.addView(viewLayout);

		return viewLayout;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);

	}
}
