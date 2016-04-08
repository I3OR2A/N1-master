package com.mmlab.n1;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.github.paolorotolo.appintro.AppIntro;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mmlab.n1.model.User;
import com.mmlab.n1.widget.NavigationDrawer;

import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by waynewei on 2015/9/7.
 */
public class MyApplication extends Application {

	private Realm realm;
	private String ip, language, deviceID;
	private double latitude, longitude;
	private Drawer drawer;
	private AccountHeader header;
	private String status;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getLanguage() {
		return language;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setStatus(String status){
		this.status = status;
	}

	public String getStatus(){
		return status;
	}

	public void createDrawer(NavigationDrawer navigationDrawer){

		RealmResults<User> userResult = realm.where(User.class)
				.findAll();

		String userName = null, userEmail = null, userPhoto = null, userCover=null;

		if (userResult.isEmpty()) {
			navigationDrawer.createDefaultDrawer();
		}
		else {
			for (User user : userResult) {
				userName = user.getName();
				userEmail = user.getEmail();
				userPhoto = user.getPhoto();
				userCover = user.getCover();
			}
			navigationDrawer.createUserDrawer(userName, userEmail, userPhoto, userCover);
		}

		language = Locale.getDefault().getDisplayLanguage();
		if (language.equals("中文")) language = "zh-tw";
		else language = "en";

		deviceID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

	}

	public void setDrawer(Drawer drawer) {
		this.drawer = drawer;
	}

	public Drawer getDrawer(){
		return drawer;
	}

	public void setHeader(AccountHeader header){
		this.header = header;
	}

	public AccountHeader getHeader(){
		return header;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		realm = Realm.getInstance(this);

		DrawerImageLoader.init(new AbstractDrawerImageLoader() {
			@Override
			public void set(ImageView imageView, Uri uri, Drawable placeholder) {
				Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
			}

			@Override
			public void cancel(ImageView imageView) {
				Glide.clear(imageView);
			}

			@Override
			public Drawable placeholder(Context ctx) {
				return null;
			}
		});


	}

	public boolean checkInternet() {
		ConnectivityManager connMgr = (ConnectivityManager)
				getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	public void noticeInternet(final Context context, View view) {
		Snackbar.make(view, context.getString(R.string.no_internet_title), Snackbar.LENGTH_LONG)
				.setAction("SETTING", new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						new MaterialDialog.Builder(context)
								.title(R.string.no_internet_title)
								.content(R.string.internet_detail)
								.positiveText(R.string.confirm)
								.negativeText(R.string.cancel)
								.callback(new MaterialDialog.ButtonCallback() {
									@Override
									public void onPositive(MaterialDialog dialog) {
										context.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
									}
								})
								.show();
					}
				})
				.show();
	}

	public void getCurrentLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		LocationListener locationListener = new LocationListener() {


			@Override
			public void onLocationChanged(Location location) {
				if (location != null) {
					latitude = location.getLatitude();
					longitude = location.getLongitude();
					Log.i("location:", String.valueOf(longitude));
					Log.i("location:", String.valueOf(latitude));
				} else {
					Log.e("Location", "Cannot get location!");
				}
			}

			@Override
			public void onProviderDisabled(String provider) {

				Toast.makeText(getApplicationContext(), getString(R.string.no_gps_title), Toast.LENGTH_LONG).show();
			}

			@Override
			public void onProviderEnabled(String provider) {

			}

			@Override
			public void onStatusChanged(String provider,
										int status, Bundle extras) {

			}
		};

		if (Build.VERSION.SDK_INT >= 23 &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			return;
		}

		long minTime = 5 * 1000; // Minimum time interval for update in seconds, i.e. 5 seconds.
		long minDistance = 10;  // Minimum distance change for update in meters, i.e. 10 meters.

		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener);


		Location location = locationManager.getLastKnownLocation(
				LocationManager.GPS_PROVIDER);
		if (location == null) {
			location = locationManager.getLastKnownLocation(
					LocationManager.NETWORK_PROVIDER);
		}
		if (location != null) {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			Log.i("location:", String.valueOf(longitude));
			Log.i("location:", String.valueOf(latitude));
		} else {
			Log.e("Location", "Cannot get location!");
		}
	}


}
