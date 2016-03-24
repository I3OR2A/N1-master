package com.mmlab.n1;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.rahatarmanahmed.cpv.CircularProgressView;
import com.melnykov.fab.FloatingActionButton;
import com.mmlab.n1.constant.IDENTITY;
import com.mmlab.n1.constant.PLAYBACK;
import com.mmlab.n1.model.LOISequenceModel;
import com.mmlab.n1.model.MyFavorite;
import com.mmlab.n1.model.POIModel;
import com.mmlab.n1.network.MemberService;
import com.mmlab.n1.network.ProxyService;
import com.mmlab.n1.helper.Preset;
import com.mmlab.n1.save_data.SavePOI;
import com.mmlab.n1.service.HttpAsyncTask;
import com.mmlab.n1.service.TaskCompleted;
import com.mmlab.n1.widget.AudioPlayer;
import com.mmlab.n1.model.Package;

import io.realm.Realm;
import io.realm.RealmResults;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class POIActivity extends AppCompatActivity implements TaskCompleted {

    private String keyword;
    private String type;
    private POIModel item;
    private LOISequenceModel loiSequence;
    private ImageView mPhoto;
    private TextView mSubject;
    private TextView mType1;
    private TextView mKeyword;
    private TextView mAddress;
    private FloatingActionButton mAlbum;
    private TextView mInfo;
    private FloatingActionButton mAudio;
    private FloatingActionButton mMovie;
    private FABProgressCircle fabProgressCircle;
    private AudioPlayer audioPlayer;
    private android.support.design.widget.FloatingActionButton mLike;
    private Realm realm;

    private MemberService mClient = null;
    private ClientReceiver clientReceiver = null;

    private ProxyService mServer = null;
    private ServerReceiver serverReceiver = null;
    private CollapsingToolbarLayout collapsingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);

        realm = Realm.getInstance(getApplicationContext());

        mPhoto = (ImageView) findViewById(R.id.imageView_poi);
        mSubject = (TextView) findViewById(R.id.subjectTextView);
        mType1 = (TextView) findViewById(R.id.type1TextView);
        mKeyword = (TextView) findViewById(R.id.keywordTextView);
        mAddress = (TextView) findViewById(R.id.addressTextView);
        mInfo = (TextView) findViewById(R.id.infoTextView);

        mLike = (android.support.design.widget.FloatingActionButton) findViewById(R.id.like);
        mAlbum = (FloatingActionButton) findViewById(R.id.album);
        fabProgressCircle = (FABProgressCircle) findViewById(R.id.fabProgressCircle);
        mAudio = (FloatingActionButton) findViewById(R.id.audio);
        mMovie = (FloatingActionButton) findViewById(R.id.movie);

        Bundle bundle = getIntent().getExtras();
        type = bundle.getString("type");


        if (type != null) {
            if (type.equals("POI-Id")) {
                loiSequence = getIntent().getParcelableExtra("POI-Content");

                String api = getResources().getString(R.string.api_poi_id);
                String url = null;

                if (loiSequence != null) {
                    collapsingToolbar.setTitle(loiSequence.getPOIName());
                    url = api + "id=" + loiSequence.getPOIId();
                } else {
                    collapsingToolbar.setTitle(bundle.getString("title"));
                    url = api + "id=" + bundle.getString("id");
                }

                Log.d("url", url);
                new HttpAsyncTask(this, type).execute(url);

            } else {
                item = getIntent().getParcelableExtra("POI-Content");
                collapsingToolbar.setTitle(item.getPOIName());
                loadData();
            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_poi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onTaskComplete(String response, String type) {
        Log.d("response", response);
        SavePOI savePOI = new SavePOI();
        savePOI.parsePoiListJSONObject(response);
        item = savePOI.getPOIList().get(0);
        ProxyService proxyService = new ProxyService();
        proxyService.sendSinglePOI(item);
        loadData();
    }


    private void loadData() {
        collapsingToolbar.setTitle(item.getPOIName());

        mSubject.setText(item.getPOISubject());
        mType1.setText(item.getPOIType1());

        for (String i : item.getPOIKeywords()) {
            keyword = i;
        }

        mKeyword.setText(keyword);

        mAddress.setText(item.getPOIAddress());
        mInfo.setText(item.getPOIInfo());

        final RealmResults<MyFavorite> result = realm.where(MyFavorite.class)
                .equalTo("id", item.getPOIId())
                .findAll();

        if (!result.isEmpty()) {
            mLike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_pink_300)));
            mLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));
        }

        mLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (result.isEmpty()) {
                    realm.beginTransaction();
                    final MyFavorite myFavorite = realm.createObject(MyFavorite.class);

                    myFavorite.setId(item.getPOIId());
                    myFavorite.setTitle(item.getPOIName());
                    if (!item.getPOIPics().isEmpty())
                        myFavorite.setPic(item.getPOIPics().get(0));

                    realm.commitTransaction();

                    mLike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.md_pink_300)));
                    mLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_white_24dp));

                    Snackbar.make(mInfo, getString(R.string.add_to_favorite), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null)
                            .show();

                } else {
                    realm.beginTransaction();
                    MyFavorite myFavorite = result.get(0);
                    myFavorite.removeFromRealm();
                    realm.commitTransaction();

                    mLike.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.white)));
                    mLike.setImageDrawable(getResources().getDrawable(R.drawable.ic_favorite_outline_pink_200_24dp));

                    Snackbar.make(mInfo, getString(R.string.cancel_add_to_favorite), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null)
                            .show();
                }


            }
        });

        if (item.getPOIPics().size() != 0) {
            Glide.with(this).load(item.getPOIPics().get(0)).into(mPhoto);
            if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
                mAlbum.setVisibility(View.VISIBLE);
                mAlbum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
                            mServer.sendPhoto();
                        }

                        Intent intent = new Intent(POIActivity.this, AlbumActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Pics", item.getPOIPics());
                        intent.putExtras(bundle);
                        startActivityForResult(intent, UPDATE_PHOTO);
                    }
                });
            }
        } else {
            Glide.clear(mPhoto);
            mAlbum.setVisibility(View.GONE);
        }

        final CircularProgressView progressView = (CircularProgressView) findViewById(R.id.loading_progress);
        final FrameLayout mFrame = (FrameLayout) findViewById(R.id.frame);
        if (item.getPOIAudios().size() != 0 && Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
            mAudio.setVisibility(View.VISIBLE);
            mAudio.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//					if(audioPlayer==null){
//						audioPlayer = new AudioPlayer(POIActivity.this, mAudio, progressView, fabProgressCircle, item.getPOIAudios().get(0));
//					}
//					else{
//						audioPlayer.playAudio();
//					}
                    Intent intent = new Intent(POIActivity.this, VideoDemoActivity.class);
                    PLAYBACK.remoteUri = item.getPOIAudios().get(0).replace("moe2//", "");
                    startActivityForResult(intent, UPDATE);
                }
            });

//			mAudio.setOnLongClickListener(new View.OnLongClickListener() {
//				@Override
//				public boolean onLongClick(View v) {
//					if(audioPlayer!=null){
//						audioPlayer.stopAudio();
//					}
//					return true;
//				}
//			});

        } else {
            mFrame.setVisibility(View.GONE);
        }

        if (item.getPOIPMovies().size() != 0 && Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
            mMovie.setVisibility(View.VISIBLE);
            mMovie.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(POIActivity.this, VideoDemoActivity.class);
                    PLAYBACK.remoteUri = item.getPOIPMovies().get(0).replace("moe2//", "");
                    startActivityForResult(intent, UPDATE);
                }
            });
        } else {
            mMovie.setVisibility(View.GONE);
        }
    }

    ServiceConnection serverConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
//			Toast.makeText(POIActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mServer = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
//			Toast.makeText(POIActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            ProxyService.ProxyBinder mLocalBinder = (ProxyService.ProxyBinder) service;
            mServer = mLocalBinder.getProxyInstance();
        }
    };

    ServiceConnection clientConnection = new ServiceConnection() {

        public void onServiceDisconnected(ComponentName name) {
//			Toast.makeText(POIActivity.this, "Service is disconnected", Toast.LENGTH_SHORT).show();
            mClient = null;
        }

        public void onServiceConnected(ComponentName name, IBinder service) {
//			Toast.makeText(POIActivity.this, "Service is connected", Toast.LENGTH_SHORT).show();
            MemberService.MemberBinder mLocalBinder = (MemberService.MemberBinder) service;
            mClient = mLocalBinder.getMemberInstance();
        }
    };

    public class ServerReceiver extends BroadcastReceiver {

        public ServerReceiver() {

        }

        public void onReceive(Context context, Intent intent) {
            if (ProxyService.GETPOI_ACTION.equals(intent.getAction())) {

            } else if (ProxyService.MEMBER_ACTION.equals(intent.getAction())) {

            } else if (ProxyService.FILE_COMPLETE__ACTION.equals(intent.getAction())) {
//				updateImagesList();
//				Toast.makeText(getApplicationContext(), "Proxy" + intent.getStringExtra("file"), Toast.LENGTH_SHORT).show();
            }

        }
    }

    public class ClientReceiver extends BroadcastReceiver {

        public ClientReceiver() {

        }

        public void onReceive(Context context, Intent intent) {
            if (MemberService.CONNECT_ACTION.equals(intent.getAction())) {
                if (intent.getIntExtra("show", Package.SHOW_NONE) == Package.SHOW_AUTO) {
                    item = mClient.getCurPOI();

                    loadData();
                }
            } else if (MemberService.FILE_COMPLETE__ACTION.equals(intent.getAction())) {
//				updateImagesList();
                Toast.makeText(getApplicationContext(), "Member" + intent.getStringExtra("file"), Toast.LENGTH_SHORT).show();
            } else if (MemberService.PHOTO_START_ACTION.equals(intent.getAction())) {
                Intent intent1 = new Intent(getApplicationContext(), AlbumActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Pics", item.getPOIPics());
                intent1.putExtras(bundle);
                startActivityForResult(intent1, PHOTO);
            }
        }
    }

    /**
     * 處理來自Service的broadcast訊息
     * 交由pHandler處理費時任務和mHandler更新UI介面
     * 避免阻塞主線程
     */
    public BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MemberService.VIDEO_START_ACTION)) {
                Intent intentVideo = new Intent(POIActivity.this, VideoDemoActivity.class);
//				Log.d(TAG, "Broadcast : " + intent.getLongExtra("mediaLength", -1));
                intentVideo.putExtra("mediaLength", intent.getLongExtra("mediaLength", -1));
                intentVideo.putExtra("remoteUri", intent.getStringExtra("remoteUri"));
                intentVideo.putExtra("data", intent.getIntExtra("data", 0));
                startActivityForResult(intentVideo, UPDATE);
            }
        }
    };

    public static final int UPDATE = 0;
    public static final int VIDEO = 1;
    public static final int PHOTO = 2;
    public static final int UPDATE_PHOTO = 3;

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("OOOOOOOOOOO", "requsetCode : " + requestCode);
        switch (resultCode) {
            case UPDATE_PHOTO:
                Log.d("HHHHHHH", requestCode + "");
                break;
            case UPDATE:
                if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.MEMBER)
                    Log.d("UUUUUUUUPDATE", mClient.getCurPOI().getPOIName());
                try {
                    if (data.getIntExtra("show", Package.SHOW_NONE) == Package.SHOW_AUTO) {
                        item = mClient.getCurPOI();
                        loadData();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                break;
            case VIDEO:
                Intent intent = new Intent(POIActivity.this, VideoDemoActivity.class);
                PLAYBACK.remoteUri = item.getPOIPMovies().get(0).replace("moe2//", "");
                startActivityForResult(intent, VIDEO);
                break;
            case PHOTO:
                intent = new Intent(getApplicationContext(), AlbumActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("Pics", item.getPOIPics());
                intent.putExtras(bundle);
                startActivityForResult(intent, PHOTO);
                break;
            default:
        }
    }

    protected void onStart() {
        super.onStart();
        Preset.loadPreferences(getApplicationContext());

        if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.MEMBER) {
            Intent intent = new Intent(POIActivity.this, MemberService.class);
            startService(intent);
            Intent intent1 = new Intent(POIActivity.this, MemberService.class);
            bindService(intent1, clientConnection, BIND_AUTO_CREATE);

            clientReceiver = new ClientReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MemberService.CONNECT_ACTION);
            intentFilter.addAction(MemberService.FILE_COMPLETE__ACTION);
            intentFilter.addAction(MemberService.PHOTO_START_ACTION);
            registerReceiver(clientReceiver, intentFilter);
            // register
            intentFilter = new IntentFilter();
            intentFilter.addAction(MemberService.VIDEO_START_ACTION);
            registerReceiver(serviceReceiver, intentFilter);
        } else if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
            Intent intent = new Intent(POIActivity.this, ProxyService.class);
            startService(intent);
            Intent intent1 = new Intent(POIActivity.this, ProxyService.class);
            bindService(intent1, serverConnection, BIND_AUTO_CREATE);

            serverReceiver = new ServerReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ProxyService.GETPOI_ACTION);
            intentFilter.addAction(ProxyService.FILE_COMPLETE__ACTION);
            registerReceiver(serverReceiver, intentFilter);
        }


    }

    @Override
    protected void onStop() {
        super.onStop();
        if (audioPlayer != null) {
            audioPlayer.restAudio();
        }
        if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.MEMBER) {
            unbindService(clientConnection);
            unregisterReceiver(clientReceiver);
            // unregister
            unregisterReceiver(serviceReceiver);
        } else if (Preset.loadPreferences(getApplicationContext()) == IDENTITY.PROXY) {
            unbindService(serverConnection);
            unregisterReceiver(serverReceiver);
        }
    }
}

