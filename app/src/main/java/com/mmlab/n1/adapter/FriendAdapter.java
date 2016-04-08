package com.mmlab.n1.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mmlab.n1.R;
import com.mmlab.n1.model.Friend;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by waynewei on 2015/10/25.
 */
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

	private ArrayList<Friend> friends;
	private final LayoutInflater mInflater;
	private final Context mContext;

	public FriendAdapter(Context context, ArrayList<Friend> friends) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		this.friends = new ArrayList<>(friends);
	}


	@Override
	public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = mInflater.inflate(R.layout.item_friend, parent, false);
		return new FriendViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final FriendViewHolder holder, int position) {
		final Friend friend = friends.get(position);

		Glide.with(mContext).load(friend.getPhotoUrl()).bitmapTransform(new CropCircleTransformation(mContext)).into(holder.mPhoto);

		holder.mName.setText(friend.getName());
		holder.listItem.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}

	@Override
	public int getItemCount() {
		return friends.size();
	}


	public void updateItems(ArrayList<Friend> friends) {
		this.friends = new ArrayList<>();
		this.friends.clear();
		this.friends.addAll(friends);
		notifyDataSetChanged();

	}

	static class FriendViewHolder extends RecyclerView.ViewHolder {

		private final TextView mName;
		private final ImageView mPhoto;
		private LinearLayout listItem;

		public FriendViewHolder(View itemView) {
			super(itemView);
			listItem = (LinearLayout) itemView.findViewById(R.id.list_item);
			mName = (TextView) itemView.findViewById(R.id.nameTextView);
			mPhoto = (ImageView) itemView.findViewById(R.id.imageView);
		}

	}
}
