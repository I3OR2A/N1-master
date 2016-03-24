package com.mmlab.n1.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.mmlab.n1.widget.ExitDialog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by waynewei on 2015/8/31.
 */
public class LOISequenceModel implements Parcelable{


    private String mPOIId;
    private String mPOIName;
    private String mPOIDescription;
    private String mContributor;
    private String mOpen;
    private String mIdentifier;
    private JSONObject mContributorDetail;

    public LOISequenceModel(String poiId, String poiName, String poiDescription, String open, String contributor, String identifier) {
        mPOIId = poiId;
        mPOIName = poiName;
        mPOIDescription = poiDescription;
        mOpen = open;
        mContributor = contributor;
        mIdentifier = identifier;
    }

    protected LOISequenceModel(Parcel in) {
        mPOIId = in.readString();
        mPOIName = in.readString();
        mPOIDescription = in.readString();
    }

    public static final Creator<LOISequenceModel> CREATOR = new Creator<LOISequenceModel>() {
        @Override
        public LOISequenceModel createFromParcel(Parcel in) {
            return new LOISequenceModel(in);
        }

        @Override
        public LOISequenceModel[] newArray(int size) {
            return new LOISequenceModel[size];
        }
    };

    public String getPOIId() {
        return mPOIId;
    }

    public String getPOIName(){
        return mPOIName;
    }

    public String getPOIDescription(){
        return mPOIDescription;
    }

    public String getIdentifier() { return mIdentifier; }

    public JSONObject getmContributorDetail() { return mContributorDetail; }

    public void setContributorDetail(JSONObject contributorDetail) { mContributorDetail = contributorDetail; }

    public void setContributor(String contributor) { mContributor = contributor; }

    public String getContributor() { return mContributor; }

    public void setOpen(String open) { mOpen = open; }

    public String getOpen() { return mOpen; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mPOIId);
        dest.writeString(mPOIName);
        dest.writeString(mPOIDescription);
    }
}
