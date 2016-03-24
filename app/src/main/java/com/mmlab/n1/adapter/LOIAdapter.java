package com.mmlab.n1.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mmlab.n1.LOIActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.model.LOIModel;

import java.util.ArrayList;


public class LOIAdapter extends RecyclerView.Adapter<LOIAdapter.LOIViewHolder> {

	private Context mContext;
	private final LayoutInflater mInflater;
	private final ArrayList<LOIModel> mModels;
	private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
	private TextDrawable.IBuilder mDrawableBuilder;


	public LOIAdapter(Context context, ArrayList<LOIModel> models) {
		mInflater = LayoutInflater.from(context);
		mModels = new ArrayList<>(models);
		mContext = context;
	}

	@Override
	public LOIViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

		return new LOIViewHolder(LayoutInflater.from(parent.getContext()).inflate(
				R.layout.list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(LOIViewHolder holder, int position) {
		final LOIModel model = mModels.get(position);
		mDrawableBuilder = TextDrawable.builder()
				.round();
//		TextDrawable drawable = mDrawableBuilder.build(String.valueOf(model.getLOIName().charAt(0)), mColorGenerator.getColor(position+1));
		String identifier = "";
		TextDrawable drawable = null;
		if(model.getIdentifier().equals("expert")){
			identifier = mContext.getString(R.string.expert);
			drawable = mDrawableBuilder.build(String.valueOf(identifier.charAt(0)), mContext.getResources().getColor(R.color.md_indigo_500));

		}
		else if(model.getIdentifier().equals("user")){
			identifier = mContext.getString(R.string.user);
			drawable = mDrawableBuilder.build(String.valueOf(identifier.charAt(0)), mContext.getResources().getColor(R.color.md_orange_500));

		}
		else {
			identifier = mContext.getString(R.string.narrator);
			drawable = mDrawableBuilder.build(String.valueOf(identifier.charAt(0)), mContext.getResources().getColor(R.color.md_purple_500));
		}

		holder.imageView.setImageDrawable(drawable);
		holder.mIcon.setImageDrawable(new IconicsDrawable(mContext)
				.icon(FontAwesome.Icon.faw_clock_o)
				.color(mContext.getResources().getColor(R.color.md_blue_grey_500))
				.sizeDp(18));
		holder.mName.setText(model.getLOIName());
		holder.mDuration.setText(model.getLOIDuration());
		holder.mInfo.setText(model.getLOIInfo());

		holder.listItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				Intent intent = new Intent(view.getContext(), LOIActivity.class);
				Bundle bundle = new Bundle();
				bundle.putParcelable("LOI-Content", model);
				intent.putExtras(bundle);
				view.getContext().startActivity(intent);
			}
		});

	}


	@Override
	public int getItemCount() {
		return mModels.size();
	}


	public void animateTo(ArrayList<LOIModel> models) {
		applyAndAnimateRemovals(models);
		applyAndAnimateAdditions(models);
		applyAndAnimateMovedItems(models);
	}

	private void applyAndAnimateRemovals(ArrayList<LOIModel> newModels) {
		for (int i = mModels.size() - 1; i >= 0; i--) {
			final LOIModel model = mModels.get(i);
			if (!newModels.contains(model)) {
				removeItem(i);
			}
		}
	}

	private void applyAndAnimateAdditions(ArrayList<LOIModel> newModels) {
		for (int i = 0, count = newModels.size(); i < count; i++) {
			final LOIModel model = newModels.get(i);
			if (!mModels.contains(model)) {
				addItem(i, model);
			}
		}
	}

	private void applyAndAnimateMovedItems(ArrayList<LOIModel> newModels) {
		for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
			final LOIModel model = newModels.get(toPosition);
			final int fromPosition = mModels.indexOf(model);
			if (fromPosition >= 0 && fromPosition != toPosition) {
				moveItem(fromPosition, toPosition);
			}
		}
	}

	public LOIModel removeItem(int position) {
		final LOIModel model = mModels.remove(position);
		notifyItemRemoved(position);
		return model;
	}

	public void addItem(int position, LOIModel model) {
		mModels.add(position, model);
		notifyItemInserted(position);
	}

	public void moveItem(int fromPosition, int toPosition) {
		final LOIModel model = mModels.remove(fromPosition);
		mModels.add(toPosition, model);
		notifyItemMoved(fromPosition, toPosition);
	}



	public static class LOIViewHolder extends RecyclerView.ViewHolder {

		private final ImageView mIcon;
		private TextView mDuration;
		private TextView mName;
		private LinearLayout listItem;
		private ImageView imageView;
		private TextView mInfo;

		public LOIViewHolder(final View itemView) {
			super(itemView);
			listItem = (LinearLayout) itemView.findViewById(R.id.list_item);
			mName = (TextView) itemView.findViewById(R.id.titleTextView);
			mInfo = (TextView) itemView.findViewById(R.id.infoTextView);
			mDuration = (TextView) itemView.findViewById(R.id.captionTextView);
			imageView = (ImageView) itemView.findViewById(R.id.imageView);
			mIcon = (ImageView) itemView.findViewById(R.id.captionIcon);
		}

	}

}
