package com.mmlab.n1.widget;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.melnykov.fab.FloatingActionButton;
import com.mmlab.n1.R;
import com.mmlab.n1.service.DownloadCompleted;
import com.mmlab.n1.service.DownloadMusic;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by waynewei on 2015/11/7.
 */
public class AudioPlayer implements FABProgressListener, MediaPlayer.OnErrorListener, MediaPlayer.OnPreparedListener,
		MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnCompletionListener, DownloadCompleted {

	private final Context mContext;
	private String url;
	private MediaPlayer mPlayer;
	private FABProgressCircle fabProgressCircle;
	private FloatingActionButton fab;
	private CircularProgressView progressView;
	private boolean wait;
	private boolean loaded;
	private Uri uri;
	private File file;
	private String fileName;
	private boolean downloadComplete;

	public AudioPlayer(Context context, FloatingActionButton fab, CircularProgressView progressView,
					   FABProgressCircle progressCircle, String url) {
		mContext = context;
		this.url = url;
		fileName = FilenameUtils.getBaseName(url) + "." + FilenameUtils.getExtension(url);
		this.fab = fab;
		this.progressView = progressView;
		fabProgressCircle = progressCircle;
		fabProgressCircle.attachListener(this);
		mPlayer = new MediaPlayer();
		setLoading();
	}

	public void loadMusic(){
		uri = Uri.parse(Environment.getExternalStorageDirectory().getPath() + "/" + fileName);
		if(mPlayer!=null){
			if(file.exists()){
				Log.d("file", "Found file in: " + String.valueOf(file));
					try {
						mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
						mPlayer.setDataSource(mContext, uri);
						mPlayer.prepareAsync();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			else{
				new DownloadMusic(mContext, fileName, progressView, this).execute(url);
				Log.d("file", "No file found");
				wait = true;
			}
		}
	}

	public void setLoading() {
		file = new File(Environment.getExternalStorageDirectory().getPath()+ "/" + fileName);
		if(mPlayer==null){
			mPlayer = new MediaPlayer();
		}
		loadMusic();
		mPlayer.setOnPreparedListener(this);
		mPlayer.setOnErrorListener(this);
		mPlayer.setOnSeekCompleteListener(this);
		mPlayer.setOnCompletionListener(this);
	}

	public void playAudio(){
		if (wait) {
			return;
		}
		if(loaded) {
			if (mPlayer.isPlaying()) {
				Log.d("test", "stop");
				fabProgressCircle.hide();
				mPlayer.pause();
				fab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_arrow_cyan_700_24dp));
				fab.setColorNormal(mContext.getResources().getColor(R.color.white));
			} else {
				Log.d("test", "play");
				fabProgressCircle.show();
				mPlayer.start();
				fab.setColorNormal(mContext.getResources().getColor(R.color.md_cyan_700));
				fab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pause_white_24dp));
			}
		}
		else {
			setLoading();
		}

	}

	public void restAudio(){
		if (mPlayer != null) {
			mPlayer.stop();
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
			loaded = false;
			wait = false;
			fab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_arrow_cyan_700_24dp));
			fab.setColorNormal(mContext.getResources().getColor(R.color.white));
			fabProgressCircle.hide();
		}
	}

	public void stopAudio(){
		if (loaded) {
			fab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_stop_cyan_700_24dp));
			fab.setColorNormal(mContext.getResources().getColor(R.color.white));
			fabProgressCircle.hide();
			mPlayer.seekTo(0);
			mPlayer.pause();
			mPlayer.reset();
			mPlayer.release();
			mPlayer = null;
			loaded = false;
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					fab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_arrow_cyan_700_24dp));
					fab.setColorNormal(mContext.getResources().getColor(R.color.white));
				}
			}, 1000);
		}
	}

	@Override
	public void onFABProgressAnimationEnd() {
		wait = false;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		restAudio();
		file.delete();
		Log.e("Error", what + " " + extra);
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mPlayer.start();
		fabProgressCircle.show();
		fab.setColorNormal(mContext.getResources().getColor(R.color.md_cyan_700));
		fab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_pause_white_24dp));
		loaded =true;
	}

	@Override
	public void onSeekComplete(MediaPlayer mp) {
		Log.d("finish", "finish");
		fabProgressCircle.beginFinalAnimation();
		wait = true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mPlayer.seekTo(0);
		fab.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_play_arrow_cyan_700_24dp));
		fab.setColorNormal(mContext.getResources().getColor(R.color.white));
	}

	@Override
	public void musicDownloaded() {
		loadMusic();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				wait = false;
			}
		}, 700);
	}

}
