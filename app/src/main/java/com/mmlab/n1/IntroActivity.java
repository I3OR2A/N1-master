package com.mmlab.n1;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.github.paolorotolo.appintro.AppIntro2;
import com.mmlab.n1.fragment.SampleSlide;

public class IntroActivity extends AppIntro2 {


	@Override
	public void init(@Nullable Bundle savedInstanceState) {
		addSlide(SampleSlide.newInstance(R.layout.intro_1));
		addSlide(SampleSlide.newInstance(R.layout.intro_2));
		addSlide(SampleSlide.newInstance(R.layout.intro_3));
	}

	private void loadMainActivity(){
		finish();
	}

	@Override
	public void onDonePressed() {
		loadMainActivity();
	}

	@Override
	public void onNextPressed() {

	}

	@Override
	public void onSlideChanged() {

	}
}
