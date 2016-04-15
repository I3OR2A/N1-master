package com.mmlab.n1.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.mmlab.n1.MainActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.adapter.WifiRecordAdapter;
import com.mmlab.n1.constant.IDENTITY;
import com.mmlab.n1.constant.MSN;
import com.mmlab.n1.decoration.DividerItemDecoration;
import com.mmlab.n1.model.WifiRecord;
import com.mmlab.n1.network.MemberService;
import com.mmlab.n1.network.NetWorkUtils;
import com.mmlab.n1.network.NetworkManagerN2;
import com.mmlab.n1.helper.Preset;
import com.mmlab.n1.network.ProxyService;
import com.mmlab.n1.service.FindPassword;
import com.mmlab.n1.widget.ConnectionDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


public class GroupFragment extends Fragment {

    public static final String TAG = "GroupFragment";

    private SwipeRefreshLayout swipeRefreshLayout = null;
    private List<WifiRecord> mRecords = new ArrayList<>();
    private LinkedHashMap<String, WifiRecord> mRecords_hashMap = new LinkedHashMap<>();
    private WifiRecordAdapter mAdapter = null;
    private BroadcastReceiver wifiReceiver;
    IntentFilter intentFilter = new IntentFilter();
    private NetworkManagerN2 networkManagerN2 = null;
    private MaterialDialog materialDialog = null;
    private View layout_group = null;

    public GroupFragment() {
        // Required empty public constructor
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Log.d(TAG, "onCreate()...");


    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChange()...");
    }

    public void updateReceiver() {
        wifiReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                final WifiInfo wifiInfo = networkManagerN2.getConnectionInfo();
                boolean isChecked = false;
                String SSID = "";
                if (wifiInfo != null && wifiInfo.getSSID() != null)
                    SSID = WifiRecord.normalizedSSID(wifiInfo.getSSID());
                if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction()) || WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())) {
                    mRecords_hashMap = networkManagerN2.getWifiRecord();
                    if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
                        mRecords_hashMap.get(SSID).state = WifiRecord.FINISHED;
                    }
                    if (wifiInfo != null && wifiInfo.getSSID() != null) {
                        Log.d(TAG, "wifiInfo SSID : " + SSID);
                    }
                    if (swipeRefreshLayout.isRefreshing())
                        swipeRefreshLayout.setRefreshing(false);

                    ArrayList<String> tmpList = new ArrayList<>();
                    for (String key : mRecords_hashMap.keySet()) {
                        tmpList.add(key);
                    }
                    MemberService memberService = new MemberService();
                    if (!memberService.isConnected()) {
                        FindPassword findPassword = new FindPassword(getActivity(), mRecords_hashMap, tmpList);
                        final String finalSSID = SSID;
                        findPassword.setOnFinishedListener(new FindPassword.OnFinishedListener() {
                            public void onFinished(final HashMap<String, WifiRecord> hashmap) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mRecords_hashMap.clear();
                                        mRecords.clear();
                                        mRecords_hashMap.putAll(hashmap);
                                        setConnectedFirst(wifiInfo, finalSSID);
                                    }
                                });
                            }
                        });
                        findPassword.execute(MSN.FB_ID);
                    }
                } else if (WifiManager.SUPPLICANT_STATE_CHANGED_ACTION.equals(intent.getAction()) | ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                    mRecords_hashMap = networkManagerN2.getWifiRecord();
                    SupplicantState supplicantState = intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
                    if (supplicantState != null)
                        switch (supplicantState) {
                            case AUTHENTICATING:
                                Log.d(TAG, "AUTHENTICATING...");
                                if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
                                    mRecords_hashMap.get(SSID).state = WifiRecord.AUTHENTICATING;
                                }
                                break;
                            case COMPLETED:
                                Log.d(TAG, "COMPLETED...");
                                if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
                                    mRecords_hashMap.get(SSID).state = WifiRecord.COMPLETED;
                                }
                                break;
                            case DISCONNECTED:
                                Log.d(TAG, "DISCONNECTED...");
                                if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
                                    mRecords_hashMap.get(SSID).state = WifiRecord.DISCONNECTED;
                                }
                                break;
                            case FOUR_WAY_HANDSHAKE:
                                Log.d(TAG, "FOUR_WAY_HANDSHAKE...");
                                if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
                                    mRecords_hashMap.get(SSID).state = WifiRecord.AUTHENTICATING;
                                }
                                break;
                            case GROUP_HANDSHAKE:
                                Log.d(TAG, "GROUP_HANDSHAKE...");
                                if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
                                    mRecords_hashMap.get(SSID).state = WifiRecord.AUTHENTICATING;
                                }
                                break;
                            default:
                        }
                    int supl_error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR, -1);
                    if (supl_error == WifiManager.ERROR_AUTHENTICATING) {
                        Log.i("ERROR_AUTHENTICATING", "ERROR_AUTHENTICATING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                    }

                    // 檢查現在是否連接上WiFi
                    ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo info = connectivityManager.getActiveNetworkInfo();
                    if (info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
                        Log.d(TAG, "WiFi");
                        if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
                            mRecords_hashMap.get(SSID).state = WifiRecord.FINISHED;
                            ((MainActivity) getActivity()).startService();
                        }

                        ArrayList<String> tmpList = new ArrayList<>();
                        for (String key : mRecords_hashMap.keySet()) {
                            tmpList.add(key);
                        }
                        FindPassword findPassword = new FindPassword(getActivity(), mRecords_hashMap, tmpList);
                        findPassword.setOnFinishedListener(new FindPassword.OnFinishedListener() {
                            public void onFinished(final HashMap<String, WifiRecord> hashmap) {
                                getActivity().runOnUiThread(new Runnable() {
                                    public void run() {
                                        mRecords_hashMap.clear();
                                        mRecords.clear();
                                        mRecords_hashMap.putAll(hashmap);
                                        mRecords.addAll(new ArrayList<WifiRecord>(mRecords_hashMap.values()));
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        });
                        findPassword.execute(MSN.FB_ID);
                    }
                }
                setConnectedFirst(wifiInfo, SSID);

            }
        };
        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        intentFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
    }

    public void setConnectedFirst(WifiInfo wifiInfo, String SSID) {
        // 將當前連線的熱點置於頂端
        if (wifiInfo != null && wifiInfo.getSSID() != null && mRecords_hashMap.containsKey(SSID)) {
            WifiRecord tmpWifiRecord = mRecords_hashMap.get(SSID);
            mRecords_hashMap.remove(SSID);
            mRecords.clear();
            mRecords.add(tmpWifiRecord);
            mRecords.addAll(new ArrayList<>(mRecords_hashMap.values()));
            mRecords_hashMap.put(SSID, tmpWifiRecord);
            mAdapter.notifyDataSetChanged();
        } else {
            mRecords.clear();
            mRecords.addAll(new ArrayList<>(mRecords_hashMap.values()));
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint()..." + isVisibleToUser);
        if (isVisibleToUser && Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.MEMBER) {
            updateReceiver();
            getActivity().registerReceiver(wifiReceiver, intentFilter);
        } else {
            if (layout_group != null)
                onIdentityChanged();
        }
    }

    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()...");
    }

    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()...");
        if (wifiReceiver != null)
            try {
                getActivity().unregisterReceiver(wifiReceiver);
            } catch (Exception ignored) {
            }
    }

    public void onIdentityChanged() {
        Log.d(TAG, "onIdentityChanged()...");
        if (Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.PROXY && Preset.loadModePreference(getActivity().getApplicationContext()) == IDENTITY.MODE_GUIDE) {
            swipeRefreshLayout.setEnabled(false);
            try {
                getActivity().unregisterReceiver(wifiReceiver);
            } catch (Exception ignored) {
            }
            mRecords.clear();
            mRecords_hashMap.clear();
            mRecords_hashMap.put(networkManagerN2.getWifiApConfiguration().SSID, new WifiRecord(networkManagerN2.getWifiApConfiguration()));
            mRecords.addAll(new ArrayList<WifiRecord>(mRecords_hashMap.values()));
            mAdapter.notifyDataSetChanged();
        } else if ((Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.MEMBER) || (Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.PROXY && Preset.loadModePreference(getActivity().getApplicationContext()) == IDENTITY.MODE_INDIVIDIUAL && NetWorkUtils.isWiFiEnabled(getActivity().getApplicationContext()))) {
            try {
                getActivity().unregisterReceiver(wifiReceiver);
            } catch (Exception ignored) {
            }
            updateReceiver();
            getActivity().registerReceiver(wifiReceiver, intentFilter);
            swipeRefreshLayout.setEnabled(true);
            networkManagerN2.startScan();
        } else {
            try {
                getActivity().unregisterReceiver(wifiReceiver);
            } catch (Exception ignored) {
            }
            mRecords.clear();
            mRecords_hashMap.clear();
            mAdapter.notifyDataSetChanged();
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()...");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated()...");

        layout_group = view;
        networkManagerN2 = new NetworkManagerN2(getActivity().getApplicationContext());

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            public void onRefresh() {
                networkManagerN2.startScan();
                // swipeRefreshLayout.setRefreshing(false);
            }
        });
        mAdapter = new WifiRecordAdapter(getActivity().getApplicationContext(), mRecords);
        RecyclerView mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(linearLayoutManager.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });
        mAdapter.setOnItemClickLitener(new WifiRecordAdapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                if (materialDialog != null) materialDialog.dismiss();

                if (!mRecords.get(position).isHost) {
                    materialDialog = ConnectionDialog.createDialog(networkManagerN2, getActivity(), mRecords.get(position));
                } else {

                }
                // alertDialog.show();
            }

            public void onItemLongClick(View view, int position) {

            }
        });
        mRecyclerView.setAdapter(mAdapter);

        if (Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.MEMBER) {
            networkManagerN2.startScan();
        } else {
            swipeRefreshLayout.setEnabled(false);
        }

        mRecords.clear();
        mRecords_hashMap.clear();
        mRecords_hashMap.put(networkManagerN2.getWifiApConfiguration().SSID, new WifiRecord(networkManagerN2.getWifiApConfiguration()));
        mRecords.addAll(new ArrayList<WifiRecord>(mRecords_hashMap.values()));
        mAdapter.notifyDataSetChanged();
        Log.d(TAG, "HOST SSID : " + networkManagerN2.getWifiApConfiguration().SSID);

//        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
//
//        ImageButton imageButton_network = (ImageButton) toolbar.findViewById(R.id.imageButton_network);
//        imageButton_network.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                NetworkDialog dialog = new NetworkDialog();
//                dialog.show(getActivity().getFragmentManager(), "networkDialog");
//            }
//        });
//
//        ImageButton imageButton_identity = (ImageButton) toolbar.findViewById(R.id.imageButton_identity);
//        imageButton_identity.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                IdentityDialog dialog = new IdentityDialog();
//                dialog.show(getActivity().getFragmentManager(), "identityDialog");
//            }
//        });
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);

    }

    public void onDetach() {
        super.onDetach();
    }
}
