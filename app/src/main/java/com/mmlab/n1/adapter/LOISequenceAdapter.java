package com.mmlab.n1.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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
import com.mmlab.n1.POIActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.model.LOISequenceModel;
import com.mmlab.n1.network.ProxyService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class LOISequenceAdapter extends RecyclerView.Adapter<LOISequenceAdapter.LOISequenceViewHolder> {

    private Context mContext;
    private final LayoutInflater mInflater;
    private final ArrayList<LOISequenceModel> mModels;
    private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
    private TextDrawable.IBuilder mDrawableBuilder;


    public LOISequenceAdapter(Context context, ArrayList<LOISequenceModel> models) {
        mInflater = LayoutInflater.from(context);
        mModels = models;
        mContext = context;
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
        TextDrawable drawable = mDrawableBuilder.build(String.valueOf(position+1), mColorGenerator.getColor(position));

        holder.imageView.setImageDrawable(drawable);
        holder.mName.setText(model.getPOIName());
        holder.mInfo.setText(model.getPOIDescription());

        holder.listItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MaterialDialog materialDialog;
                String alertMessage;
                if (model.getOpen().equals("0")) {
                    materialDialog = new MaterialDialog.Builder(mContext)
                            .title("此景點為私人景點")
                            .positiveText(R.string.confirm)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    // TODO
                                }
                            }).build();
                    if (model.getIdentifier().equals("docent")) {
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

        public LOISequenceViewHolder(final View itemView) {
            super(itemView);
            listItem = (LinearLayout) itemView.findViewById(R.id.list_item);
            mName = (TextView) itemView.findViewById(R.id.titleTextView);
            mInfo = (TextView) itemView.findViewById(R.id.infoTextView);
            imageView = (ImageView) itemView.findViewById(R.id.imageView);
        }

    }

}
