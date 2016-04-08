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

        String identifier, status = "";
        TextDrawable drawable = null;
        if(mStatus!=null) {
            if (mStatus.equals("MyPOI")) {
                if (model.getOpen().equals("1")) {
                    status = mContext.getResources().getString(R.string.open);
                    drawable = mDrawableBuilder.build(String.valueOf(status.charAt(0)), mContext.getResources().getColor(R.color.md_cyan_700));
                } else {
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

        holder.mIcon1.setVisibility(View.GONE);
        holder.mIcon2.setVisibility(View.GONE);
        holder.mIcon3.setVisibility(View.GONE);
        holder.mIcon4.setVisibility(View.GONE);

        if(!model.getMedia().isEmpty()) {
            for (String media_format : model.getMedia()) {
                switch (media_format) {
                    case "photo":
                        holder.mIcon1.setVisibility(View.VISIBLE);
                        holder.mIcon1.setImageDrawable(new IconicsDrawable(mContext)
                                .icon(GoogleMaterial.Icon.gmd_image)
                                .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                                .sizeDp(18));
                        break;
                    case "audio":
                        holder.mIcon2.setVisibility(View.VISIBLE);
                        holder.mIcon2.setImageDrawable(new IconicsDrawable(mContext)
                                .icon(GoogleMaterial.Icon.gmd_volume_up)
                                .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                                .sizeDp(18));
                        break;
                    case "movie":
                        holder.mIcon3.setVisibility(View.VISIBLE);
                        holder.mIcon3.setImageDrawable(new IconicsDrawable(mContext)
                                .icon(GoogleMaterial.Icon.gmd_videocam)
                                .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                                .sizeDp(18));
                        break;
                    case "audio tour":
                        holder.mIcon4.setVisibility(View.VISIBLE);
                        holder.mIcon4.setImageDrawable(new IconicsDrawable(mContext)
                                .icon(GoogleMaterial.Icon.gmd_record_voice_over)
                                .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                                .sizeDp(18));
                        break;
                }
            }

        }
        else {
            holder.mIcon4.setVisibility(View.VISIBLE);
            holder.mIcon4.setImageDrawable(new IconicsDrawable(mContext)
                    .icon(GoogleMaterial.Icon.gmd_place)
                    .color(mContext.getResources().getColor(R.color.md_blue_grey_500))
                    .sizeDp(18));
        }

        holder.poiTitle.setText(model.getPOIName());
        holder.poiInfo.setText(model.getPOIInfo());
        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (proxyService != null) {
                    Log.d("Contributor", model.getContributor());

                        proxyService.sendSinglePOI(model);
                        Intent intent = new Intent(view.getContext(), POIActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("POI-Content", model);
                        bundle.putString("type", "Normal");
                        intent.putExtras(bundle);
                        view.getContext().startActivity(intent);

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

        private TextView poiTitle;
        private LinearLayout listItem;
        private ImageView imageView;
        private TextView poiDistance;
        private TextView poiInfo;
        private ImageView mIcon1;
        private ImageView mIcon2;
        private ImageView mIcon3;
        private ImageView mIcon4;

        public POIViewHolder(final View itemView) {
            super(itemView);
            listItem = (LinearLayout) itemView.findViewById(R.id.list_item);
            poiTitle = (TextView) itemView.findViewById(R.id.titleTextView);
            poiInfo = (TextView) itemView.findViewById(R.id.infoTextView);
            poiDistance = (TextView) itemView.findViewById(R.id.captionTextView);
            poiDistance.setVisibility(View.GONE);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
            mIcon1 = (ImageView) itemView.findViewById(R.id.captionIcon1);
            mIcon2 = (ImageView) itemView.findViewById(R.id.captionIcon2);
            mIcon3 = (ImageView) itemView.findViewById(R.id.captionIcon3);
            mIcon4 = (ImageView) itemView.findViewById(R.id.captionIcon4);

        }

    }

}
