package com.mmlab.n1.widget;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mmlab.n1.MainActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.constant.IDENTITY;
import com.mmlab.n1.constant.MSN;
import com.mmlab.n1.helper.Preset;
import com.mmlab.n1.model.Friend;
import com.mmlab.n1.model.User;
import com.mmlab.n1.network.NetWorkUtils;
import com.mmlab.n1.network.NetworkManagerN2;
import com.mmlab.n1.service.FirstUsage;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


public class IdentityDialog extends DialogFragment {

    public static final String TAG = "IdentityDialog";

    public void setOnChangedListener(OnChangedListener onChangedListener) {
        this.onChangedListener = onChangedListener;
    }

    public OnChangedListener onChangedListener = null;

    public IdentityDialog() {
        // Required empty public constructor
    }

    public interface OnChangedListener {
        void onChanged();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    public void onDetach() {
        super.onDetach();
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Log.d(TAG, "identity : " + Preset.loadPreferences(getActivity()));
//        return new MaterialDialog.Builder(getActivity())
//                .title(R.string.title_dialog_identity)
//                .items(R.array.item_identity)
//                .itemsCallbackSingleChoice(Preset.loadPreferences(getActivity()), new MaterialDialog.ListCallbackSingleChoice() {
//                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
//                        Toast.makeText(getActivity(), which + ": " + text, Toast.LENGTH_SHORT).show();
//                        if (which == 0) {
//                            Preset.savePreferences(getActivity(), IDENTITY.PROXY);
//                            MSN.identity = IDENTITY.PROXY;
//                            ((MainActivity) getActivity()).startService();
//                        } else {
//                            Preset.savePreferences(getActivity(), IDENTITY.MEMBER);
//                            MSN.identity = IDENTITY.MEMBER;
//                            ((MainActivity) getActivity()).startService();
//                        }
//                        return true;
//                    }
//                })
//                .positiveText(R.string.confirm)
//                .contentLineSpacing(1.6f)
//                .build();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_identity, null);
        final Spinner spinner = (Spinner) view.findViewById(R.id.spinnner);

        final CheckBox checkBox_proxy = (CheckBox) view.findViewById(R.id.checkBox_proxy);
        final CheckBox checkBox_member = (CheckBox) view.findViewById(R.id.checkBox_member);
        final CheckBox checkBox_personal = (CheckBox) view.findViewById(R.id.checkBox_personal);
        checkBox_proxy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBox_member.setChecked(false);
                    checkBox_member.setClickable(true);
                    checkBox_personal.setChecked(false);
                    checkBox_personal.setClickable(true);

                    checkBox_proxy.setClickable(false);
                    spinner.setVisibility(View.VISIBLE);
                }
            }
        });
        checkBox_member.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBox_proxy.setChecked(false);
                    checkBox_proxy.setClickable(true);
                    checkBox_personal.setChecked(false);
                    checkBox_personal.setClickable(true);

                    checkBox_member.setClickable(false);
                    spinner.setVisibility(View.GONE);
                }
            }
        });
        checkBox_personal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    checkBox_proxy.setChecked(false);
                    checkBox_proxy.setClickable(true);
                    checkBox_member.setChecked(false);
                    checkBox_member.setClickable(true);

                    checkBox_personal.setClickable(false);
                    spinner.setVisibility(View.GONE);
                }
            }
        });

        if (Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.PROXY && Preset.loadModePreference(getActivity().getApplicationContext()) == IDENTITY.MODE_GUIDE) {
            checkBox_proxy.setChecked(true);
            checkBox_proxy.setClickable(false);
            spinner.setVisibility(View.VISIBLE);
        } else if (Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.MEMBER) {
            spinner.setVisibility(View.GONE);
            checkBox_member.setChecked(true);
            checkBox_member.setClickable(false);
        } else {
            spinner.setVisibility(View.GONE);
            checkBox_personal.setChecked(true);
            checkBox_personal.setClickable(false);
        }

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.narrator_setting));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(Preset.loadSubPreferences(getActivity().getApplicationContext()));
        spinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView adapterView, View view, int position, long id) {

            }

            public void onNothingSelected(AdapterView arg0) {
            }
        });

        return new MaterialDialog.Builder(getActivity())
                .title(R.string.title_dialog_identity)
                .customView(view, true)
                .widgetColor(getResources().getColor(R.color.colorPrimary))
                .positiveText(R.string.confirm)
                .negativeText(R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        int prev = Preset.loadPreferences(getActivity().getApplicationContext());
                        int prev_mode = Preset.loadModePreference(getActivity().getApplicationContext());
                        MSN.mode = IDENTITY.MODE_GUIDE;
                        Preset.saveModePreference(getActivity().getApplicationContext(), IDENTITY.MODE_GUIDE);
                        if (checkBox_proxy.isChecked()) {
                            MSN.identity = IDENTITY.PROXY;
                            Preset.savePreferences(getActivity().getApplicationContext(), IDENTITY.PROXY);
                            NetWorkUtils.setAPEnabledMethod(getActivity().getApplicationContext(), true);
                            NetWorkUtils.setMobileDataEnabledMethod1(getActivity().getApplicationContext(), true);
                        } else if (checkBox_member.isChecked()) {
                            MSN.identity = IDENTITY.MEMBER;
                            Preset.savePreferences(getActivity().getApplicationContext(), IDENTITY.MEMBER);
                            NetWorkUtils.setWiFiEnabled(getActivity().getApplicationContext(), true);
                            NetWorkUtils.setMobileDataEnabledMethod1(getActivity().getApplicationContext(), false);
                        } else {
                            MSN.identity = IDENTITY.PROXY;
                            Preset.savePreferences(getActivity().getApplicationContext(), IDENTITY.PROXY);
                            MSN.mode = IDENTITY.MODE_INDIVIDIUAL;
                            Preset.saveModePreference(getActivity().getApplicationContext(), IDENTITY.MODE_INDIVIDIUAL);
                        }
                        Preset.saveSubPreferences(getActivity().getApplicationContext(), spinner.getSelectedItemPosition());

                        if (prev != Preset.loadPreferences(getActivity().getApplicationContext()) || prev_mode != Preset.loadModePreference(getActivity().getApplicationContext())) {
                            ((MainActivity) getActivity()).startService();
                            ((MainActivity) getActivity()).onIdentityChanged();
                        }

                        if (onChangedListener != null) {
                            onChangedListener.onChanged();
                        }

                        if (Preset.loadPreferences(getActivity().getApplicationContext()) == IDENTITY.PROXY) {
                            Realm realm = Realm.getInstance(getActivity());
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

                            NetworkManagerN2 networkManagerN2 = new NetworkManagerN2(getActivity().getApplicationContext());
                            switch (spinner.getSelectedItemPosition()) {
                                case 0:
                                    new FirstUsage(getActivity(), networkManagerN2.getWifiApConfiguration()).execute(MSN.FB_ID, MSN.FB_NAME, MSN.FB_FL, "public");
                                    break;
                                case 1:
                                    new FirstUsage(getActivity(), networkManagerN2.getWifiApConfiguration()).execute(MSN.FB_ID, MSN.FB_NAME, MSN.FB_FL, "friend");
                                    break;
                                case 2:
                                    new FirstUsage(getActivity(), networkManagerN2.getWifiApConfiguration()).execute(MSN.FB_ID, MSN.FB_NAME, MSN.FB_FL, "group");
                                    break;
                            }
                        }
                    }
                })
                .contentLineSpacing(1.6f)
                .build();
    }
}
