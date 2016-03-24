package com.mmlab.n1.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class LOIModel implements Parcelable {

    private String mLOIId;
    private String mLOIName;
    private String mLOIDuration;
    private String mLOIInfo;
    private String mContributor;
    private String mIdentifier;
    private JSONObject mContributorDetail;

    public LOIModel(String loiId, String loiName, String loiInfo, String con, String identifier) {
        mLOIId = loiId;
        mLOIName = loiName;
        mLOIInfo = loiInfo;
        mContributor = con;
        mIdentifier = identifier;
    }

    public LOIModel(String loiId, String loiName, String loiDuration, String loiInfo, String con, String identifier) {
        mLOIId = loiId;
        mLOIName = loiName;
        mLOIDuration = loiDuration;
        mLOIInfo = loiInfo;
        mContributor = con;
        mIdentifier = identifier;
    }

    protected LOIModel(Parcel in) {
        mLOIId = in.readString();
        mLOIName = in.readString();
        mLOIDuration = in.readString();
        mLOIInfo = in.readString();
        mContributor = in.readString();
        mIdentifier = in.readString();
        try {
            mContributorDetail = new JSONObject(in.readString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final Creator<LOIModel> CREATOR = new Creator<LOIModel>() {
        @Override
        public LOIModel createFromParcel(Parcel in) {
            return new LOIModel(in);
        }

        @Override
        public LOIModel[] newArray(int size) {
            return new LOIModel[size];
        }
    };

    public String getLOIId() {
        return mLOIId;
    }

    public String getLOIName(){
        return mLOIName;
    }

    public String getLOIDuration(){
        return mLOIDuration;
    }

    public String getContributor() { return mContributor; }

    public String getIdentifier() { return mIdentifier; }

    public String getLOIInfo(){
        return mLOIInfo;
    }

    public void setContributorDetail(JSONObject contributorDetail) { mContributorDetail = contributorDetail; }

    public JSONObject getContributorDetail() {
        return mContributorDetail;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mLOIId);
        dest.writeString(mLOIName);
        dest.writeString(mLOIDuration);
        dest.writeString(mLOIInfo);
        dest.writeString(mContributor);
        dest.writeString(mIdentifier);
        try {
            dest.writeString(mContributorDetail.toString());
        } catch (Exception ex) {
            dest.writeString("");
        }
    }
}
