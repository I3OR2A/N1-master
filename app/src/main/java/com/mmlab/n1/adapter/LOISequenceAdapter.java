package com.mmlab.n1.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mmlab.n1.POIActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.model.DEHUser;
import com.mmlab.n1.model.LOISequenceModel;
import com.mmlab.n1.network.ProxyService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;


public class LOISequenceAdapter extends RecyclerView.Adapter<LOISequenceAdapter.LOISequenceViewHolder> {


    private final Realm realm;
    private Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<LOISequenceModel> mModels;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;
    private String mType;
    private String mIdentifier;

    public LOISequenceAdapter(Context context, ArrayList<LOISequenceModel> models, String type, String identifier) {
        mInflater = LayoutInflater.from(context);
        mModels = models;
        mContext = context;
        mType = type;
        mIdentifier = identifier;
        realm = Realm.getInstance(context);
    }

    @Override
    public LOISequenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new LOISequenceViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(LOISequenceViewHolder holder,final int position) {
        final LOISequenceModel model = mModels.get(position);
        mDrawableBuilder = TextDrawable.builder()
                .round();

        TextDrawable drawable;

        if(mType.equals("LOI-Sequence"))
            drawable = mDrawableBuilder.build(String.valueOf(position+1), mColorGenerator.getColor(position));
        else
            drawable = mDrawableBuilder.build(String.valueOf(model.getPOIName().charAt(0)), mColorGenerator.getColor(position));

        holder.imageView.setImageDrawable(drawable);

        holder.poiDistance.setVisibility(View.GONE);

        holder.mIcon1.setVisibility(View.GONE);
        holder.mIcon2.setVisibility(View.GONE);
        holder.mIcon3.setVisibility(View.GONE);
        holder.mIcon4.setVisibility(View.GONE);

        if (model.getMediaFormat() == 1) {
            holder.mIcon1.setVisibility(View.VISIBLE);
            holder.mIcon1.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_image)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }
        else if (model.getMediaFormat() == 2) {
            holder.mIcon2.setVisibility(View.VISIBLE);
            holder.mIcon2.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_volume_up)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }
        else if (model.getMediaFormat() == 4) {
            holder.mIcon3.setVisibility(View.VISIBLE);
            holder.mIcon3.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_videocam)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }
        else if (model.getMediaFormat() == 8) {
            holder.mIcon4.setVisibility(View.VISIBLE);
            holder.mIcon4.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_record_voice_over)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }
        else if(model.getMediaFormat() == 9){
            holder.mIcon1.setVisibility(View.VISIBLE);
            holder.mIcon1.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_image)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));

            holder.mIcon4.setVisibility(View.VISIBLE);
            holder.mIcon4.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_record_voice_over)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }
        else if(model.getMediaFormat() == 10){
            holder.mIcon2.setVisibility(View.VISIBLE);
            holder.mIcon2.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_volume_up)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));

            holder.mIcon4.setVisibility(View.VISIBLE);
            holder.mIcon4.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_record_voice_over)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }
        else if(model.getMediaFormat() == 12){
            holder.mIcon3.setVisibility(View.VISIBLE);
            holder.mIcon3.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_videocam)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));

            holder.mIcon4.setVisibility(View.VISIBLE);
            holder.mIcon4.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_record_voice_over)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }
        else{
            holder.mIcon4.setVisibility(View.VISIBLE);
            holder.mIcon4.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_place)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }


        holder.mName.setText(model.getPOIName());
        if(mIdentifier.equals("docent")&&model.getOpen().equals("0")){
            holder.mName.setTextColor(mContext.getResources().getColor(R.color.md_red_400));
        }
        holder.mInfo.setText(model.getPOIDescription());

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDialog materialDialog;
                String alertMessage;
                RealmResults<DEHUser> userResult = realm.where(DEHUser.class)
                        .findAll();
                String userId=null;
                for (DEHUser user : userResult) {
                    userId = user.getId();
                }
                boolean open = false;
                Log.d("test", model.getContributor()+"");
                if(userId!=null){
                    if(userId.equals(model.getContributor())){
                        open = true;
                    }
                }
                if(mIdentifier.equals("docent") && model.getOpen().equals("0") && !open){

                        materialDialog = new MaterialDialog.Builder(mContext)
                                .title("此景點為私人景點")
                                .positiveText(R.string.confirm)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        // TODO
                                    }
                                }).build();
                        if (model.getIdentifier()=="docent") {
                            JSONObject obj = model.getmContributorDetail();
                            alertMessage = "詳細內容請聯絡導覽員: ";
                            try {
                                alertMessage +=  obj.getString("name");
                                alertMessage += "\n電話: " + obj.getString("telphone");
                                alertMessage += "\n手機: " + obj.getString("cellphone");
                                alertMessage += "\nEmail: " + obj.getString("email");
                                alertMessage += "\n地址: " + obj.getString("user_address");
                                alertMessage += "\n社群帳號: " + obj.getString("social_id");
                                alertMessage += "\n導覽解說地區: " + obj.getString("docent_area");
                                alertMessage += "\n導覽解說使用語言: " + obj.getString("docent_language");
                                alertMessage += "\n收費標準: " + obj.getString("charge");
                                alertMessage += "\n自我介紹: " + obj.getString("introduction");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            alertMessage = "詳細內容請聯絡製作者: " + model.getContributor();
                        }
                        materialDialog.setMessage(alertMessage);
                        materialDialog.show();


                } else {
                    Intent intent = new Intent(view.getContext(), POIActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("POI-Content", model);
                    bundle.putString("type", "POI-Id");
                    intent.putExtras(bundle);
                    view.getContext().startActivity(intent);
                }

            }
        });

    }


    @Override
    public int getItemCount() {
        return (null != mModels ? mModels.size() : 0);
    }


    public void animateTo(ArrayList<LOISequenceModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(ArrayList<LOISequenceModel> newModels) {
        for (int i = mModels.size() - 1; i >= 0; i--) {
            final LOISequenceModel model = mModels.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<LOISequenceModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final LOISequenceModel model = newModels.get(i);
            if (!mModels.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<LOISequenceModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final LOISequenceModel model = newModels.get(toPosition);
            final int fromPosition = mModels.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public LOISequenceModel removeItem(int position) {
        final LOISequenceModel model = mModels.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, LOISequenceModel model) {
        mModels.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final LOISequenceModel model = mModels.remove(fromPosition);
        mModels.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }



    public static class LOISequenceViewHolder extends RecyclerView.ViewHolder {

        private TextView mName;
        private LinearLayout listItem;
        private ImageView imageView;
        private TextView mInfo;
        private TextView poiDistance;
        private ImageView mIcon1;
        private ImageView mIcon2;
        private ImageView mIcon3;
        private ImageView mIcon4;

        public LOISequenceViewHolder(final View itemView) {
            super(itemView);
            listItem = (LinearLayout) itemView.findViewById(R.id.list_item);
            mName = (TextView) itemView.findViewById(R.id.titleTextView);
            mInfo = (TextView) itemView.findViewById(R.id.infoTextView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            mIcon1 = (ImageView) itemView.findViewById(R.id.captionIcon1);
            mIcon2 = (ImageView) itemView.findViewById(R.id.captionIcon2);
            mIcon3 = (ImageView) itemView.findViewById(R.id.captionIcon3);
            mIcon4 = (ImageView) itemView.findViewById(R.id.captionIcon4);
            poiDistance = (TextView) itemView.findViewById(R.id.captionTextView);
        }

    }

}
