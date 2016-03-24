package com.mmlab.n1.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class ChickenView extends ImageView {
    public ChickenView(Context context) {
        super(context);
    }

    public ChickenView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChickenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        return false;
    }

    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
