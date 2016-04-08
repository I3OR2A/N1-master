package com.mmlab.n1.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.jorgecastilloprz.FABProgressCircle;
import com.github.jorgecastilloprz.listeners.FABProgressListener;
import com.melnykov.fab.FloatingActionButton;
import com.melnykov.fab.ScrollDirectionListener;
import com.mmlab.n1.MainActivity;
import com.mmlab.n1.MyApplication;
import com.mmlab.n1.R;
import com.mmlab.n1.adapter.AOIAdapter;
import com.mmlab.n1.adapter.LOIAdapter;
import com.mmlab.n1.adapter.POIAdapter;
import com.mmlab.n1.constant.IDENTITY;
import com.mmlab.n1.decoration.DividerItemDecoration;
import com.mmlab.n1.model.POIModel;
import com.mmlab.n1.network.MemberService;
import com.mmlab.n1.network.ProxyService;
import com.mmlab.n1.other.RecyclerViewDisabler;
import com.mmlab.n1.helper.Preset;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.os.Handler;


public class SiteFragment extends Fragment {
    private static final int DEFAULT_DISTANCE = 10000;
    private static final int DEFAULT_NUMBER = 50;
    View layout_site;
    Toolbar toolbar;
    private RecyclerView mRecyclerView = null;
    private FloatingActionButton fab;
    private int number;
    private double distance;
    private int selectTab;
    private String api;
    private MyApplication globalVariable;
    private String mType;
    private boolean taskRunning;
    private RecyclerViewDisabler diabler;
    private FABProgressCircle fabProgressCircle;
    private POIAdapter poiAdapter;
    private String hint;
    private LOIAdapter loiAdapter;
    private AOIAdapter aoiAdapter;


    public SiteFragment() {
        // Required empty public constructor
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (layout_site != null && Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.MEMBER) {
            MemberService memberService = new MemberService();
            updateMemberSites(memberService);
        } else if (layout_site != null && Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.PROXY) {
            ProxyService proxyService = new ProxyService();
            updateSites(proxyService, ProxyService.type, ProxyService.status );
        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        layout_site = inflater.inflate(R.layout.fragment_site, container, false);
        // 創建MainAdapter實例
        // 取得RecyclerView實例
        mRecyclerView = (RecyclerView) layout_site.findViewById(R.id.recyclerView);
        // 設置布局管理器
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        // 設置Item增加移除動畫
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // 設置分割線
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        // 設置ClickListener
        // 根據每個Group的連現狀態會有不同的Dialog

        // 設置Adapter

        globalVariable = (MyApplication) getActivity().getApplicationContext();
        globalVariable.getCurrentLocation();
        return layout_site;
    }


    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateSites(ProxyService service, int type, String status) {

        switch (type) {
            case 0:
                poiAdapter = new POIAdapter(getActivity(), service, status);
                mRecyclerView.setAdapter(poiAdapter);
                break;
            case 1:
                loiAdapter = new LOIAdapter(getActivity(), service.getLOIList());
                mRecyclerView.setAdapter(loiAdapter);
                break;
            case 2:
                aoiAdapter = new AOIAdapter(getActivity(), service.getAOIList());
                mRecyclerView.setAdapter(aoiAdapter);
                break;
        }
    }

    public void updatePOI(ArrayList<POIModel> poiModels) {
        poiAdapter.animateTo(poiModels);
        mRecyclerView.scrollToPosition(0);
        mRecyclerView.setAdapter(poiAdapter);
    }

    public void resetList(ProxyService service, String status){
        poiAdapter = new POIAdapter(getActivity(), service, status);
    }

    public void updateMemberSites(MemberService mClient) {
        poiAdapter = new POIAdapter(getActivity(), mClient);
        mRecyclerView.setAdapter(poiAdapter);
    }
}
