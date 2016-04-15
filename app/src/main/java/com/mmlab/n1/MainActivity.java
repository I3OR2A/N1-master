package com.mmlab.n1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.http.HttpResponseCache;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookSdk;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.google.android.gms.location.LocationListener;
import com.melnykov.fab.FloatingActionButton;
import com.mmlab.n1.constant.IDENTITY;
import com.mmlab.n1.constant.MSN;
import com.mmlab.n1.helper.ExternalStorage;
import com.mmlab.n1.model.DEHUser;
import com.mmlab.n1.model.Friend;
import com.mmlab.n1.model.POIModel;
import com.mmlab.n1.network.CacheService;
import com.mmlab.n1.network.NetworkManager;
import com.mmlab.n1.network.NetworkManagerN2;
import com.mmlab.n1.service.Filter;
import com.mmlab.n1.service.FirstUsage;
import com.mmlab.n1.widget.ExitDialog;
import com.mmlab.n1.fragment.GroupFragment;
import com.mmlab.n1.fragment.SiteFragment;
import com.mmlab.n1.model.Package;
import com.mmlab.n1.model.User;
import com.mmlab.n1.network.MemberService;
import com.mmlab.n1.network.NetWorkUtils;
import com.mmlab.n1.network.ProxyService;
import com.mmlab.n1.helper.Preset;
import com.mmlab.n1.widget.FilterDialog;
import com.mmlab.n1.widget.HotspotDialog;
import com.mmlab.n1.widget.IdentityDialog;
import com.mmlab.n1.widget.NavigationDrawer;
import com.mmlab.n1.widget.NetworkDialog;
import com.rey.material.widget.Spinner;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class MainActivity extends AppCompatActivity implements FABProgressListener {
    private static final int DEFAULT_DISTANCE = 10000;
    private static final int DEFAULT_NUMBER = 50;
    private static final String TAG = "MainActivity";
    private static final CharSequence[] choose_identity = {"導覽員", "成員"};
    public static final int PAGE_SITE = 1;
    private Toolbar toolbar = null;
    private TabLayout tabLayout = null;
    private static final int SIGN_IN = 1;

    /**
     * V2 Start
     */
//    private View layout_disconnect = null;
//    private Button button_reconnect = null;
    /**
     * V2 End
     **/
    private ViewPager viewPager = null;
    private int[] tabIcons = {
            R.drawable.ic_group,
            R.drawable.ic_site
    };
    private int[] toolbarLayout = {
            R.layout.toolbar_group,
            R.layout.toolbar_site
    };

    private ProxyService mServer = null;
    private MemberService mClient = null;

    private ServerReceiver serverReceiver = null;
    private ClientReceiver clientReceiver = null;

    private android.app.FragmentManager fragmentManager = null;
    private MyApplication globalVariable;
    private NavigationDrawer navigationDrawer;
    private CoordinatorLayout coordinatorLayout;
    private Toolbar actionBar;
    private FABProgressCircle fabProgressCircle;
    private FloatingActionButton fab;
    private int selectTab;
    private int number;
    private Double distance;
    private String mType;
    private String api;
    private boolean taskRunning;
    private String hint;
    private Menu mMenu;
    private String identifier;
    private String media;
    private String category;
    private Fragment page;
    private String url;
    private EditText accountInput;
    private TextView passwordInput;
    private Realm realm;
    private RealmResults<DEHUser> userResult;
    private String userId;
    private IdentityDialog identityDialog;


    public void clearFileDirectory(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                clearFileDirectory(child);

        fileOrDirectory.delete();

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate()...");
        FacebookSdk.sdkInitialize(getApplicationContext());
        setContentView(R.layout.activity_main);

        globalVariable = (MyApplication) getApplicationContext();


        Intent intent = new Intent(this, IntroActivity.class);
        startActivity(intent);

        realm = Realm.getInstance(this);


        try {
            File mediaCacheDir = new File(this.getCacheDir(), "media-cache");
            long mediaCacheSize = 150 * 1024 * 1024; // 150 MiB
            HttpResponseCache.install(mediaCacheDir, mediaCacheSize);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Preset.clearPreferences(getApplicationContext());
        clearFileDirectory(new File(ExternalStorage.BASE_ROOT + File.separator + ExternalStorage.TARGET_DIRECTORY));


        fragmentManager = getFragmentManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setTitle("DEH Narrator");


        navigationDrawer = new NavigationDrawer(this, toolbar);

        identityDialog = new IdentityDialog();

        RealmResults<User> userResult = realm.where(User.class).findAll();
        if (!userResult.isEmpty()) {
            identityDialog.show(getFragmentManager(), "identityDialog");
        }


        init();

        fab.setVisibility(View.GONE);
        fabProgressCircle.setVisibility(View.GONE);


        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        actionBar = (Toolbar) findViewById(R.id.action_bar);
        ImageButton imageButton_network = (ImageButton) actionBar.findViewById(R.id.imageButton_network);
        imageButton_network.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                NetworkDialog dialog = new NetworkDialog();
                dialog.show(getFragmentManager(), "networkDialog");
            }
        });

        ImageButton imageButton_hotspot = (ImageButton) findViewById(R.id.imageButton_hotspot);
        imageButton_hotspot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HotspotDialog dialog = new HotspotDialog();
                dialog.show(getFragmentManager(), "hotspotDialog");
            }
        });

        ImageButton imageButton_identity = (ImageButton) actionBar.findViewById(R.id.imageButton_identity);
        imageButton_identity.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                identityDialog.show(getFragmentManager(), "identityDialog");
            }
        });


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Drawable icon = getResources().getDrawable(tabIcons[tab.getPosition()]);
                icon.setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP);
                tabLayout.getTabAt(tab.getPosition()).setIcon(icon);

                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition() == 0) {
                    fab.setVisibility(View.GONE);
                    fabProgressCircle.setVisibility(View.GONE);
                    actionBar.setVisibility(View.VISIBLE);
                } else {
                    actionBar.setVisibility(View.GONE);
                    if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.MEMBER) {
                        fab.setVisibility(View.GONE);
                        fabProgressCircle.setVisibility(View.GONE);
                    } else {
                        fab.setVisibility(View.VISIBLE);
                        fabProgressCircle.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                Drawable icon = getResources().getDrawable(tabIcons[tab.getPosition()]);
                icon.setColorFilter(getResources().getColor(R.color.colorPrimaryDark), PorterDuff.Mode.SRC_ATOP);
                tabLayout.getTabAt(tab.getPosition()).setIcon(icon);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        setupTabIcons();


        MSN.identity = Preset.loadPreferences(getApplicationContext());
        startService();


    }

    public void init() {

        globalVariable.createDrawer(navigationDrawer);

        fabProgressCircle = (FABProgressCircle) findViewById(R.id.fabProgressCircle);
        fabProgressCircle.attachListener(this);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (globalVariable.checkInternet()) {

                    userResult = realm.where(DEHUser.class)
                            .findAll();

                    for (DEHUser user : userResult) {
                        userId = user.getId();
                    }

                    MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                            .title(R.string.search_settings)
                            .customView(R.layout.dialog_search, true)
                            .positiveText(R.string.search)
                            .negativeText(R.string.cancel)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                    if (number != 0 && distance != 0) {
                                        Double lat = globalVariable.getLatitude();
                                        Double lng = globalVariable.getLongitude();
                                        if (selectTab == 0) {
                                            mType = "POI";
                                            api = getResources().getString(R.string.api_nearbyPOIs);
                                        } else if (selectTab == 1) {
                                            mType = "LOI";
                                            api = getResources().getString(R.string.api_nearbyLOIs);
                                        } else if (selectTab == 2) {
                                            mType = "AOI";
                                            api = getResources().getString(R.string.api_nearbyAOIs);
                                        } else if (selectTab == 3) {
                                            mType = "MyPOI";
                                            api = getResources().getString(R.string.api_userPOIs);
                                        } else if (selectTab == 4) {
                                            mType = "MyLOI";
                                            api = getResources().getString(R.string.api_userLOIs);
                                        } else if (selectTab == 5) {
                                            mType = "MyAOI";
                                            api = getResources().getString(R.string.api_userAOIs);
                                        }

                                        globalVariable.setStatus(mType);

                                        String language = Locale.getDefault().getDisplayLanguage();
                                        if (language.equals("English"))
                                            language = "en";
                                        else if (language.equals("中文"))
                                            language = "zh-tw";
                                        else if (language.equals("日本語"))
                                            language = "ja";

                                        Log.d("lang", language);

                                        url = api + "lat=" + lat + "&lng=" + lng + "&dist=" + distance + "&num=" +
                                                number + "&did=" + globalVariable.getDeviceID() +
                                                "&appver=mini200&ulat=22.9942&ulng=120.1659&clang=" + language;

                                        if (selectTab > 2) {
                                            url = api + "id=" + userId + "&lat=" + lat + "&lng=" + lng + "&dist=" + distance + "&num=" +
                                                    number + "&did=" + globalVariable.getDeviceID() +
                                                    "&appver=mini200&ulat=22.9942&ulng=120.1659";
                                        }

                                        Log.d("url", url);
                                        Log.d("type", mType);

                                        if (!taskRunning) {
                                            mServer.Search(mType, url);

                                            fabProgressCircle.show();
                                            taskRunning = true;
                                        }

                                    }

                                    selectTab = 0;
                                    identifier = "all";
                                    media = "all";
                                    category = "all";
                                }
                            })
                            .build();

                    TabLayout tabLayout = (TabLayout) dialog.getCustomView().findViewById(R.id.tab);
                    tabLayout.addTab(tabLayout.newTab().setText(R.string.poi));
                    tabLayout.addTab(tabLayout.newTab().setText(R.string.loi));
                    tabLayout.addTab(tabLayout.newTab().setText(R.string.aoi));

                    if (!userResult.isEmpty()) {
                        tabLayout.addTab(tabLayout.newTab().setText(R.string.mypoi));
                        tabLayout.addTab(tabLayout.newTab().setText(R.string.myloi));
                        tabLayout.addTab(tabLayout.newTab().setText(R.string.myaoi));
                    }

                    final ViewPager viewPager = (ViewPager) dialog.getCustomView().findViewById(R.id.viewpager);

                    viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
                    tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                        @Override
                        public void onTabSelected(TabLayout.Tab tab) {

                            selectTab = tab.getPosition();
                        }

                        @Override
                        public void onTabUnselected(TabLayout.Tab tab) {

                        }

                        @Override
                        public void onTabReselected(TabLayout.Tab tab) {

                        }
                    });

                    SeekBar seekbarDistance = (SeekBar) dialog.getCustomView().findViewById(R.id.seekBar_distance);
                    seekbarDistance.setProgress(DEFAULT_DISTANCE);
                    distance = (double) DEFAULT_DISTANCE;
                    final TextView textViewDistance = (TextView) dialog.getCustomView().findViewById(R.id.text_distance);
                    textViewDistance.setText(new DecimalFormat("#0.0").format(DEFAULT_DISTANCE / 1000) + " " + getString(R.string.kilometer));
                    seekbarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            distance = (double) progress;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            textViewDistance.setText(new DecimalFormat("#0.0").format(distance / 1000) + " " + getString(R.string.kilometer));
                        }
                    });

                    SeekBar seekbarNumber = (SeekBar) dialog.getCustomView().findViewById(R.id.seekBar_number);
                    seekbarNumber.setProgress(DEFAULT_NUMBER);
                    number = DEFAULT_NUMBER;
                    final TextView textViewNumber = (TextView) dialog.getCustomView().findViewById(R.id.text_number);
                    textViewNumber.setText(seekbarNumber.getProgress() + " " + getResources().getString(R.string.unit));
                    seekbarNumber.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            number = progress;
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {

                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            textViewNumber.setText(number + " " + getResources().getString(R.string.unit));
                        }
                    });

                    dialog.show();


                } else {
                    globalVariable.noticeInternet(MainActivity.this, fab);
                }
            }
        });
    }

    public void exitDEH() {
        finish();
    }


    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()...");
        if (MSN.identity == IDENTITY.PROXY) {
            try {
                unregisterReceiver(serverReceiver);
            } catch (Exception e) {
            }
            try {
                unbindService(serverConnection);
            } catch (Exception e) {

            }
        } else {
            try {
                unregisterReceiver(clientReceiver);
            } catch (Exception e) {

            }
            try {
                unbindService(clientConnection);
            } catch (Exception e) {

            }
        }

        unregisterReceiver(mBroadcastReceiver);
    }

    public void onRegisteProxyReceiver() {
        serverReceiver = new ServerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ProxyService.GETLOGIN_ACTION);
        intentFilter.addAction(ProxyService.GETPOI_ACTION);
        intentFilter.addAction(ProxyService.GETLOI_ACTION);
        intentFilter.addAction(ProxyService.GETAOI_ACTION);
        intentFilter.addAction(ProxyService.FILE_COMPLETE__ACTION);
        registerReceiver(serverReceiver, intentFilter);
    }

    public void onRegisteMemberReceiver() {
        clientReceiver = new ClientReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MemberService.CONNECT_ACTION);
        intentFilter.addAction(MemberService.VIDEO_START_ACTION);
        intentFilter.addAction(MemberService.CONNECT_TO_PROXY);
        intentFilter.addAction(MemberService.DISCONNECT_FROM_PROXY);
        registerReceiver(clientReceiver, intentFilter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()...");
        Log.d(TAG, "refresh_drawer : " + getIntent().getBooleanExtra("refresh_drawer", false));

        if (getIntent().getBooleanExtra("refresh_drawer", false) == true) {
            globalVariable.createDrawer(navigationDrawer);

        }
    }

    /**
     * V2 End
     */


    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()...");

        Log.d(TAG, "refresh_drawer : " + getIntent().getBooleanExtra("refresh_drawer", false));

        MSN.identity = Preset.loadPreferences(getApplicationContext());

        /** V2 Start **/
        if (MSN.identity == IDENTITY.PROXY) {
            try {
//                layout_disconnect.setVisibility(View.GONE);
                stopMemberService();
            } catch (Exception e) {
            }
            startProxyService();
        } else {
            try {
//                layout_disconnect.setVisibility(View.GONE);
                stopProxyService();
            } catch (Exception e) {
            }
            startMemberService();
            if (viewPager.getCurrentItem() == PAGE_SITE) {
                Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
                if (mClient != null)
                    ((SiteFragment) page).updateMemberSites(mClient);
            }
        }

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mBroadcastReceiver, intentFilter);

        /** V2 End **/
    }

    protected void onDestroy() {
        super.onDestroy();
        stopProxyService();
        stopMemberService();
        HttpResponseCache cache = HttpResponseCache.getInstalled();
        if (cache != null) {
            cache.flush();
        }
    }

    public void onBackPressed() {
        ExitDialog dialog = new ExitDialog();
        dialog.show(getFragmentManager(), "exitDialog");
    }


    private void setupTabIcons() {
        Drawable icon = getResources().getDrawable(tabIcons[tabLayout.getSelectedTabPosition()]);
        icon.setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_ATOP);
        tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).setIcon(icon);

        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new GroupFragment(), "GROUP");
        adapter.addFragment(new SiteFragment(), "SITE");
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(2);
    }

    public void startService() {
        if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
            stopMemberService();
            startProxyService();
            startService(new Intent(MainActivity.this, CacheService.class));
        } else if (Preset.loadPreferences(getApplication()) == IDENTITY.MEMBER) {
            stopProxyService();
            startMemberService();
        } else {
            stopMemberService();
            stopProxyService();
        }
    }

    public void startProxyService() {
        // NetWorkUtils.setAPEnabledMethod(getApplicationContext(), true);
//        onDisConnectMessage(false);

        try {
            unregisterReceiver(serverReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onRegisteProxyReceiver();

        Intent intent = new Intent(MainActivity.this, ProxyService.class);
        startService(intent);
        Intent intent1 = new Intent(MainActivity.this, ProxyService.class);
        bindService(intent1, serverConnection, BIND_AUTO_CREATE);

    }

    public void startMemberService() {
        NetWorkUtils.setWiFiEnabled(getApplicationContext(), true);

        try {
            unregisterReceiver(clientReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onRegisteMemberReceiver();

        Intent intent = new Intent(MainActivity.this, MemberService.class);
        startService(intent);
        Intent intent1 = new Intent(MainActivity.this, MemberService.class);
        bindService(intent1, clientConnection, BIND_AUTO_CREATE);

    }

    public void stopProxyService() {
        try {
            if (mServer != null) mServer.stopProxyService();
            Intent intent = new Intent(MainActivity.this, ProxyService.class);
            stopService(intent);
            stopService(new Intent(MainActivity.this, CacheService.class));
            unbindService(serverConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(serverReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopMemberService() {
        try {
            Intent intent = new Intent(MainActivity.this, MemberService.class);
            stopService(intent);
            unbindService(clientConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            unregisterReceiver(clientReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ServiceConnection serverConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
//            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mServer = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
//            Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            ProxyService.ProxyBinder mLocalBinder = (ProxyService.ProxyBinder) service;
            mServer = mLocalBinder.getProxyInstance();
            Log.d("service", "Service is connected");
            if (globalVariable.checkInternet()) {
                mServer.retrieveIp();
            }
        }
    };

    ServiceConnection clientConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
//            Toast.makeText(MainActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mClient = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
//            Toast.makeText(MainActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            MemberService.MemberBinder mLocalBinder = (MemberService.MemberBinder) service;
            mClient = mLocalBinder.getMemberInstance();
        }
    };

    public void searchSite(String type, String url) {
        mServer.Search(type, url);
    }

    public void onIdentityChanged() {
        if (viewPager.getCurrentItem() == 0) {
            Log.d(TAG, "onIdentityChanged()...");
            Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
            ((GroupFragment) page).onIdentityChanged();
        }


        if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
            if (Preset.loadModePreference(getApplicationContext()) == IDENTITY.MODE_INDIVIDIUAL) {
                mServer.hangService();
            } else {
                mServer.restartService();
            }
        }
    }

    @Override
    public void onFABProgressAnimationEnd() {
        if (hint != null)
            Snackbar.make(fab, hint, Snackbar.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                fab.show(false);
            }
        }, 3000);
    }

    public class ServerReceiver extends BroadcastReceiver {

        public ServerReceiver() {

        }

        public void onReceive(Context context, Intent intent) {
            if (ProxyService.GETLOGIN_ACTION.equals(intent.getAction())) {
                if (mServer.getLogInStatus()) {
                    hint = getResources().getString(R.string.login_success);
                } else {
                    hint = getResources().getString(R.string.login_failure);
                }
                Snackbar.make(coordinatorLayout, hint, Snackbar.LENGTH_SHORT).show();
            }
            if (viewPager.getCurrentItem() == PAGE_SITE) {
                taskRunning = false;
                fabProgressCircle.beginFinalAnimation();
                page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
                if (ProxyService.GETPOI_ACTION.equals(intent.getAction())) {

                    Filter filter = new Filter(getApplicationContext());
                    final ArrayList<POIModel> filteredModelList, filteredModelList1, filteredModelList2;

                    if (identifier != null && media != null && category != null) {
                        if (identifier.equals("all") && media.equals("all") && category.equals("all")) {
                            ((SiteFragment) page).updateSites(mServer, 0, mType);
                            hint = getString(R.string.find) + " " + mServer.getPOIList().size() + " " + getString(R.string.sites);

                        } else if (!identifier.equals("all") && media.equals("all") && category.equals("all")) {
                            filteredModelList = filter.identifierFilter(mServer.getPOIList(), identifier.toLowerCase());
                            ((SiteFragment) page).updatePOI(filteredModelList);

                            if (filteredModelList.size() == 0)
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                            else
                                hint = getString(R.string.find) + " " + filteredModelList.size() + " " + getString(R.string.sites);

                        } else if (identifier.equals("all") && !media.equals("all") && category.equals("all")) {
                            filteredModelList = filter.mediaFilter(mServer.getPOIList(), media.toLowerCase());
                            ((SiteFragment) page).updatePOI(filteredModelList);

                            if (filteredModelList.size() == 0)
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                            else
                                hint = getString(R.string.find) + " " + filteredModelList.size() + " " + getString(R.string.sites);

                        } else if (identifier.equals("all") && media.equals("all") && !category.equals("all")) {
                            filteredModelList = filter.categoryFilter(mServer.getPOIList(), category.toLowerCase());
                            ((SiteFragment) page).updatePOI(filteredModelList);

                            if (filteredModelList.size() == 0)
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                            else
                                hint = getString(R.string.find) + " " + filteredModelList.size() + " " + getString(R.string.sites);

                        } else if (!identifier.equals("all") && !media.equals("all") && category.equals("all")) {
                            filteredModelList = filter.identifierFilter(mServer.getPOIList(), identifier.toLowerCase());
                            filteredModelList1 = filter.mediaFilter(filteredModelList, media.toLowerCase());
                            ((SiteFragment) page).updatePOI(filteredModelList1);

                            if (filteredModelList1.size() == 0)
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                            else
                                hint = getString(R.string.find) + " " + filteredModelList1.size() + " " + getString(R.string.sites);

                        } else if (!identifier.equals("all") && media.equals("all") && !category.equals("all")) {
                            filteredModelList = filter.identifierFilter(mServer.getPOIList(), identifier.toLowerCase());
                            filteredModelList1 = filter.categoryFilter(filteredModelList, category.toLowerCase());
                            ((SiteFragment) page).updatePOI(filteredModelList1);

                            if (filteredModelList1.size() == 0)
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                            else
                                hint = getString(R.string.find) + " " + filteredModelList1.size() + " " + getString(R.string.sites);

                        } else if (identifier.equals("all") && !media.equals("all") && !category.equals("all")) {
                            filteredModelList = filter.mediaFilter(mServer.getPOIList(), media.toLowerCase());
                            filteredModelList1 = filter.categoryFilter(filteredModelList, category.toLowerCase());
                            ((SiteFragment) page).updatePOI(filteredModelList1);

                            if (filteredModelList1.size() == 0)
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                            else
                                hint = getString(R.string.find) + " " + filteredModelList1.size() + " " + getString(R.string.sites);

                        } else {
                            filteredModelList = filter.identifierFilter(mServer.getPOIList(), identifier.toLowerCase());
                            filteredModelList1 = filter.mediaFilter(filteredModelList, media.toLowerCase());
                            filteredModelList2 = filter.categoryFilter(filteredModelList1, category.toLowerCase());
                            ((SiteFragment) page).updatePOI(filteredModelList2);

                            if (filteredModelList2.size() == 0)
                                Toast.makeText(MainActivity.this, getResources().getString(R.string.not_match), Toast.LENGTH_SHORT).show();
                            else
                                hint = getString(R.string.find) + " " + filteredModelList2.size() + " " + getString(R.string.sites);
                        }
                    }

                    ProxyService.type = 0;
                    mMenu.findItem(R.id.filter).setVisible(true);

                } else if (ProxyService.GETLOI_ACTION.equals(intent.getAction())) {
                    ((SiteFragment) page).updateSites(mServer, 1, mType);
                    ProxyService.type = 1;
                    mMenu.findItem(R.id.filter).setVisible(false);
                    hint = getString(R.string.find) + " " + mServer.getLOIList().size() + " " + getString(R.string.lines);
                } else if (ProxyService.GETAOI_ACTION.equals(intent.getAction())) {
                    ((SiteFragment) page).updateSites(mServer, 2, mType);
                    ProxyService.type = 2;
                    mMenu.findItem(R.id.filter).setVisible(false);
                    hint = getString(R.string.find) + " " + mServer.getAOIList().size() + " " + getString(R.string.areas);
                }
            }
//            if (ProxyService.GETPOI_ACTION.equals(intent.getAction())) {
//                if (viewPager.getCurrentItem() == PAGE_SITE) {
//                    Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
//                    ((SiteFragment) page).updateSites(mServer.getPOIList(), mType);
//                }
//            }
            if (ProxyService.MEMBER_ACTION.equals(intent.getAction())) {

            }
        }
    }

    public class ClientReceiver extends BroadcastReceiver {

        public ClientReceiver() {

        }

        public void onReceive(Context context, Intent intent) {
            if (MemberService.CONNECT_ACTION.equals(intent.getAction())) {
                if (viewPager.getCurrentItem() == PAGE_SITE) {
                    Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
                    ((SiteFragment) page).updateMemberSites(mClient);
                }
                if (intent.getIntExtra("show", Package.SHOW_NONE) == Package.SHOW_AUTO) {
                    Log.d(TAG, "show auto");
                    Intent intent1 = new Intent(MainActivity.this, POIActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("POI-Content", mClient.getCurPOI());
                    bundle.putString("type", "Normal");
                    intent1.putExtras(bundle);
                    startActivity(intent1);
                }
            } else if (MemberService.VIDEO_START_ACTION.equals(intent.getAction())) {
//                Intent intentVideo = new Intent(GroupActivity.this, VideoDemoActivity.class);
//                Log.d(TAG, "Broadcast : " + intent.getLongExtra("mediaLength", -1));
//                intentVideo.putExtra("mediaLength", intent.getLongExtra("mediaLength", -1));
//                intentVideo.putExtra("remoteUri", intent.getStringExtra("remoteUri"));
//                intentVideo.putExtra("data", intent.getIntExtra("data", 0));
//                startActivity(intentVideo);
            } else if (MemberService.DISCONNECT_FROM_PROXY.equals(intent.getAction())) {

                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, "中斷連線", Snackbar.LENGTH_LONG)
                        .setAction("重新嘗試", new View.OnClickListener() {
                            public void onClick(View view) {
                                mClient.stopClient();
                                mClient.startClient();
                            }
                        });
                snackbar.show();
            } else if (MemberService.CONNECT_TO_PROXY.equals(intent.getAction())) {
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "已連線", Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        public CharSequence getPageTitle(int position) {
            // return null to display only the icon
            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        menu.findItem(R.id.filter).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }
        if (id == R.id.deh_sign_in) {

            userResult = realm.where(DEHUser.class)
                    .findAll();

            for (DEHUser user : userResult) {
                userId = user.getId();
            }

            if (!userResult.isEmpty()) {


                new MaterialDialog.Builder(this)
                        .title(R.string.deh_logout)
                        .content(userId + " , " + getString(R.string.deh_logout_detail))
                        .positiveText(R.string.confirm)
                        .negativeText(R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                realm.beginTransaction();
                                realm.clear(DEHUser.class);
                                realm.commitTransaction();
                            }
                        })
                        .show();
            } else {

                MaterialDialog dialog = new MaterialDialog.Builder(this)
                        .title(R.string.deh_sign_in)
                        .customView(R.layout.dialog_deh_sign_in, true)
                        .positiveText(R.string.confirm)
                        .negativeText(android.R.string.cancel)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                                String account = accountInput.getText().toString();
                                String password = passwordInput.getText().toString();
                                if (!account.matches("") && !password.matches("")) {
                                    mType = "DEH log in";
                                    url = getResources().getString(R.string.api_dehLogIn) + "userid=" + account + "&pwd=" + password;
                                    mServer.logIn(mType, url, account, password);
                                } else {
                                    Toast.makeText(MainActivity.this, "帳號密碼不可為空", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .build();

                accountInput = (EditText) dialog.getCustomView().findViewById(R.id.account);
                passwordInput = (EditText) dialog.getCustomView().findViewById(R.id.password);


                // Toggling the show password CheckBox will mask or unmask the password input EditText
                CheckBox checkbox = (CheckBox) dialog.getCustomView().findViewById(R.id.showPassword);
                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        passwordInput.setInputType(!isChecked ? InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_CLASS_TEXT);
                        passwordInput.setTransformationMethod(!isChecked ? PasswordTransformationMethod.getInstance() : null);
                    }
                });


                dialog.show();
            }


        }
        if (id == R.id.filter) {

            FilterDialog dialog = new FilterDialog();
            dialog.show(getFragmentManager(), "filterDialog");

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void startFilter(String identifier, String media, String category) {
        this.identifier = identifier;
        this.media = media;
        this.category = category;
        hint = null;
        Log.d("push", identifier + media + category);
        mServer.Search(mType, url);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SIGN_IN:
                if (data != null) {
                    if (data.getExtras().getString("login_success").equals("Login Success"))
                        identityDialog.show(getFragmentManager(), "identityDialog");
                }
        }
    }


    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkManager networkManager = new NetworkManager(getApplicationContext());
            Log.d(TAG, "connect server upload");
            if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY && networkManager.isConnected()) {
                Log.d(TAG, "connect server upload inner");
                Realm realm = Realm.getInstance(MainActivity.this);
                RealmResults<User> userResult = realm.where(User.class)
                        .findAll();

                ArrayList<String> friendList = new ArrayList<>();
                for (User user : userResult) {
                    MSN.FB_ID = user.getId();
                    MSN.FB_NAME = user.getName();
                    for (Friend friend : user.getFriends()) {
                        if (friend.isValid()) {
                            friendList.add(friend.getId());
                            Log.d("test", friend.toString());
                        }
                    }
                }

                MSN.FB_FL = friendList.toString();

                NetworkManagerN2 networkManagerN2 = new NetworkManagerN2(getApplicationContext());
                MSN.WIFI_ACCESS = Preset.loadWiFiPreferences(getApplicationContext());
                switch (MSN.WIFI_ACCESS) {
                    case 0:
                        new FirstUsage(MainActivity.this, networkManagerN2.getWifiApConfiguration()).execute(MSN.FB_ID, MSN.FB_NAME, MSN.FB_FL, "public");
                        break;
                    case 1:
                        new FirstUsage(MainActivity.this, networkManagerN2.getWifiApConfiguration()).execute(MSN.FB_ID, MSN.FB_NAME, MSN.FB_FL, "friend");
                        break;
                    case 2:
                        new FirstUsage(MainActivity.this, networkManagerN2.getWifiApConfiguration()).execute(MSN.FB_ID, MSN.FB_NAME, MSN.FB_FL, "group");
                        break;
                }
            }

            if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY && Preset.loadModePreference(getApplicationContext()) == IDENTITY.MODE_INDIVIDIUAL) {
                if (viewPager.getCurrentItem() == 0) {
                    Log.d(TAG, "onIdentityChanged()...");
                    Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.viewpager + ":" + viewPager.getCurrentItem());
                    ((GroupFragment) page).onIdentityChanged();
                }
            }
        }
    };
}
