package com.mmlab.n1.service;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.github.rahatarmanahmed.cpv.CircularProgressViewListener;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by waynewei on 2015/11/8.
 */
public class DownloadMusic extends AsyncTask<String, Integer, String> {

	private final Context mContext;
	private final DownloadCompleted mCallback;
	private CircularProgressView progressView;
	private String fileName;

	public DownloadMusic(Context context, String fileName, CircularProgressView progressView, DownloadCompleted callback) {
		this.mContext = context;
		this.fileName = fileName;
		this.mCallback = callback;
		this.progressView = progressView;
	}

	// Show Progress bar before downloading Music
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressView.setProgress(0f);
	}

	// Download Music File from Internet
	@Override
	protected String doInBackground(String... f_url) {
		int count;
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;
		try {
			URL url = new URL(f_url[0]);
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty("Accept-Encoding", "identity");
			connection.connect();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return "Server returned HTTP " + connection.getResponseCode()
						+ " " + connection.getResponseMessage();
			}

			// Get Music file length
			int lenghtOfFile = connection.getContentLength();

			Log.d("size", String.valueOf(lenghtOfFile));
			// input stream to read file - with 8k buffer
			input = connection.getInputStream();
			// Output stream to write file in SD card
			output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + fileName);
			byte data[] = new byte[4096];
			long total = 0;
			while ((count = input.read(data)) != -1) {
				if (isCancelled()) {
					input.close();
					output.flush();
					output.close();
					Log.d("Close", "cancel");
					break;
				}
				total += count;
				// Publish the progress which triggers onProgressUpdate method
				if (lenghtOfFile > 0)
					publishProgress((int) ((total * 100) / lenghtOfFile));
				// Write data to file
				output.write(data, 0, count);
			}

		} catch (Exception e) {
			return e.toString();
		} finally {
			try {
				if (output != null)
					output.close();
				if (input != null)
					input.close();
			} catch (IOException ignored) {
			}

			if (connection != null)
				connection.disconnect();
		}
		return null;
	}

	// While Downloading Music File
	@Override
	protected void onProgressUpdate(Integer... progress) {
		Log.d("Downloding...", String.valueOf(progress[0])+" "+progressView.getMaxProgress());
		progressView.setProgress((float) progress[0]);
	}

	// Once Music File is downloaded
	@Override
	protected void onPostExecute(String file_url) {
		// Dismiss the dialog after the Music file was downloaded
		progressView.addListener(new CircularProgressViewListener() {
			@Override
			public void onProgressUpdate(float currentProgress) {

			}

			@Override
			public void onProgressUpdateEnd(float currentProgress) {
				progressView.setVisibility(View.INVISIBLE);
				mCallback.musicDownloaded();
			}

			@Override
			public void onAnimationReset() {

			}

			@Override
			public void onModeChanged(boolean isIndeterminate) {

			}
		});
	}

}
