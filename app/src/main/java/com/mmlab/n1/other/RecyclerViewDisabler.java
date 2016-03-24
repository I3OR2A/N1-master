package com.mmlab.n1.other;

import android.support.v7.widget.RecyclerView;
import android.view.MotionEvent;

/**
 * Created by waynewei on 2015/11/6.
 */
public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

	@Override
	public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
		return true;
	}

	@Override
	public void onTouchEvent(RecyclerView rv, MotionEvent e) {

	}

	@Override
	public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

	}
}