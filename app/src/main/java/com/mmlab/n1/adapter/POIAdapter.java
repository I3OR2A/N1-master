package com.mmlab.n1.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.bumptech.glide.Glide;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mmlab.n1.MainActivity;
import com.mmlab.n1.POIActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.model.POIModel;
import com.mmlab.n1.network.MemberService;
import com.mmlab.n1.network.ProxyService;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;


public class POIAdapter extends RecyclerView.Adapter<POIAdapter.POIViewHolder> {

    private String mStatus;
    private ProxyService proxyService;
    private MemberService memberService;
    private Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<POIModel> mModels;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;


    public POIAdapter(Context context, ProxyService service, String status) {
        mInflater = LayoutInflater.from(context);
        proxyService = service;
        mModels = service.getPOIList();
        mContext = context;
        mStatus = status;
    }

    public POIAdapter(Context context, MemberService service) {
        mInflater = LayoutInflater.from(context);
        memberService = service;
        mModels = service.getPOIList();
        mContext = context;
    }

    @Override
    public POIViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        return new POIViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(POIViewHolder holder, final int position) {
        final POIModel model = mModels.get(position);
        mDrawableBuilder = TextDrawable.builder()
                .round();
//        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(model.getIdentifier().charAt(0)), mColorGenerator.getColor(model.getPOIName().charAt(0)));
//
//        if (model.getPOIPics().size() != 0) {
//            Glide.with(mContext).load(model.getPOIPics().get(0)).bitmapTransform(new CropCircleTransformation(mContext)).into(holder.imageView);
//        } else {
//            Glide.clear(holder.imageView);
//            holder.imageView.setImageDrawable(drawable);
//        }

        String identifier, status = "";
        TextDrawable drawable = null;
        if(mStatus.equals("MyPOI")){
            if(model.getOpen().equals("1")) {
                status = mContext.getResources().getString(R.string.open);
                drawable = mDrawableBuilder.build(String.valueOf(status.charAt(0)), mContext.getResources().getColor(R.color.md_cyan_700));
            }
            else {
                status = mContext.getResources().getString(R.string.close);
                drawable = mDrawableBuilder.build(String.valueOf(status.charAt(0)), mContext.getResources().getColor(R.color.md_orange_400));
            }
        }
        else {

            if (model.getIdentifier().equals("expert")) {
                identifier = mContext.getString(R.string.expert);
                drawable = mDrawableBuilder.build(String.valueOf(identifier.charAt(0)), mContext.getResources().getColor(R.color.md_indigo_500));

            } else if (model.getIdentifier().equals("user")) {
                identifier = mContext.getString(R.string.user);
                drawable = mDrawableBuilder.build(String.valueOf(identifier.charAt(0)), mContext.getResources().getColor(R.color.md_orange_500));

            } else {
                identifier = mContext.getString(R.string.narrator);
                drawable = mDrawableBuilder.build(String.valueOf(identifier.charAt(0)), mContext.getResources().getColor(R.color.md_purple_500));
            }
        }

        holder.imageView.setImageDrawable(drawable);

        if (model.getPOIPics().size() != 0) {
            holder.mIcon.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_image)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        } else if(model.getPOIAudios().size() != 0){
            holder.mIcon.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_volume_up)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        } else if(model.getPOIPMovies().size() != 0){
            holder.mIcon.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_videocam)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }else{
            holder.mIcon.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_place)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }

        holder.poiTitle.setText(model.getPOIName());
        holder.poiInfo.setText(model.getPOIInfo());
        holder.poiDistance.setText(model.getPOIDistance() + " " + mContext.getResources().getString(R.string.kilometer));

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (proxyService != null) {
                    if (model.getOpen().equals("0")) {
                        new MaterialDialog.Builder(mContext).title("此景點為私人景點")
                                .content("此景點為私人景點，請聯絡導覽員:" + model.getContributor())
                                .positiveText(mContext.getResources().getString(R.string.confirm))
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    }
                                }).build().show();
                    } else {
                        proxyService.sendSinglePOI(model);
                        Intent intent = new Intent(view.getContext(), POIActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("POI-Content", model);
                        bundle.putString("type", "Normal");
                        intent.putExtras(bundle);
                        view.getContext().startActivity(intent);
                    }
                }
            }
        });

    }


    @Override
    public int getItemCount() {
        return mModels.size();
    }



    public void animateTo(ArrayList<POIModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(ArrayList<POIModel> newModels) {
        for (int i = mModels.size() - 1; i >= 0; i--) {
            final POIModel model = mModels.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(ArrayList<POIModel> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final POIModel model = newModels.get(i);
            if (!mModels.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(ArrayList<POIModel> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final POIModel model = newModels.get(toPosition);
            final int fromPosition = mModels.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public POIModel removeItem(int position) {
        final POIModel model = mModels.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, POIModel model) {
        mModels.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final POIModel model = mModels.remove(fromPosition);
        mModels.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }


    public static class POIViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private TextView poiTitle;
        private LinearLayout listItem;
        private ImageView imageView;
        private TextView poiDistance;
        private TextView poiInfo;

        public POIViewHolder(final View itemView) {
            super(itemView);
            listItem = (LinearLayout) itemView.findViewById(R.id.list_item);
            poiTitle = (TextView) itemView.findViewById(R.id.titleTextView);
            poiInfo = (TextView) itemView.findViewById(R.id.infoTextView);
            poiDistance = (TextView) itemView.findViewById(R.id.captionTextView);

            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            mIcon = (ImageView) itemView.findViewById(R.id.captionIcon);
        }

    }

}
