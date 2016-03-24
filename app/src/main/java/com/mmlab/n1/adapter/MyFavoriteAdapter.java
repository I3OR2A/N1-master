package com.mmlab.n1.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bumptech.glide.Glide;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.daimajia.swipe.SimpleSwipeListener;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.mmlab.n1.MyApplication;
import com.mmlab.n1.POIActivity;
import com.mmlab.n1.R;
import com.mmlab.n1.model.MyFavorite;

import java.util.ArrayList;
import io.realm.Realm;
import io.realm.RealmResults;
import jp.wasabeef.glide.transformations.CropCircleTransformation;

/**
 * Created by waynewei on 2015/10/25.
 */
public class MyFavoriteAdapter extends RecyclerSwipeAdapter<MyFavoriteAdapter.MyFavoriteViewHolder> {

	private MyApplication globalVariable;
	private ArrayList<MyFavorite> myFavorites;
	private final LayoutInflater mInflater;
	private final Context mContext;
	private ColorGenerator mColorGenerator = ColorGenerator.MATERIAL;
	private TextDrawable.IBuilder mDrawableBuilder;
	private Realm realm;

	public MyFavoriteAdapter(Context context, ArrayList<MyFavorite> myFavorites) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		this.myFavorites = new ArrayList<>(myFavorites);
		globalVariable = (MyApplication) context.getApplicationContext();
		realm = Realm.getInstance(context);
	}

	@Override
	public MyFavoriteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = mInflater.inflate(R.layout.item_my_favorite, parent, false);
		return new MyFavoriteViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final MyFavoriteViewHolder holder, final int position) {
		final MyFavorite myFavorite = myFavorites.get(position);

		holder.mSwipe.setShowMode(SwipeLayout.ShowMode.PullOut);
		holder.mSwipe.addSwipeListener(new SimpleSwipeListener() {
			@Override
			public void onOpen(SwipeLayout layout) {
				YoYo.with(Techniques.Tada).duration(500).delay(100).playOn(layout.findViewById(R.id.trash));
			}
		});

		mDrawableBuilder = TextDrawable.builder().round();

		TextDrawable drawable = mDrawableBuilder.build(String.valueOf(myFavorite.getTitle().charAt(0)),
				mColorGenerator.getColor(myFavorite.getTitle().charAt(0)));
		if(myFavorite.getPic()!=null){
			Glide.with(mContext).load(myFavorite.getPic()).bitmapTransform(new CropCircleTransformation(mContext)).into(holder.mPhoto);
		}
		else{
			Glide.clear(holder.mPhoto);
			holder.mPhoto.setImageDrawable(drawable);
		}

		holder.mName.setText(myFavorite.getTitle());
		holder.mSwipe.getSurfaceView().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (globalVariable.checkInternet()) {
					Intent intent = new Intent(v.getContext(), POIActivity.class);
					Bundle bundle = new Bundle();
					bundle.putString("title", myFavorite.getTitle());
					bundle.putString("id", myFavorite.getId());
					bundle.putString("type", "POI-Id");
					intent.putExtras(bundle);
					v.getContext().startActivity(intent);
				} else {
					Snackbar.make(holder.mItem, mContext.getString(R.string.no_internet_title), Snackbar.LENGTH_LONG)
							.setAction("SETTING", new View.OnClickListener() {
								@Override
								public void onClick(View view) {
									new MaterialDialog.Builder(mContext)
											.title(R.string.no_internet_title)
											.content(R.string.internet_detail)
											.positiveText(R.string.confirm)
											.negativeText(R.string.cancel)
											.callback(new MaterialDialog.ButtonCallback() {
												@Override
												public void onPositive(MaterialDialog dialog) {
													mContext.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
												}
											})
											.show();
								}
							})
							.show();
				}
			}
		});

		holder.mDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				mItemManger.removeShownLayouts(holder.mSwipe);
				myFavorites.remove(position);
				notifyItemRemoved(position);
				notifyItemRangeChanged(position, myFavorites.size());
				mItemManger.closeAllItems();

				realm.beginTransaction();
				final RealmResults<MyFavorite> result = realm.where(MyFavorite.class)
						.equalTo("id", myFavorite.getId())
						.findAll();
				final MyFavorite favorite = result.get(0);
				favorite.removeFromRealm();
				realm.commitTransaction();

				Snackbar.make(holder.mItem, mContext.getString(R.string.cancel_add_to_favorite), Snackbar.LENGTH_LONG)
						.show();

			}
		});


		mItemManger.bindView(holder.itemView,position);
	}

	@Override
	public int getItemCount() {
		return myFavorites.size();
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe;
	}

	public void animateTo(ArrayList<MyFavorite> models) {
		applyAndAnimateRemovals(models);
		applyAndAnimateAdditions(models);
		applyAndAnimateMovedItems(models);
	}

	private void applyAndAnimateRemovals(ArrayList<MyFavorite> newModels) {
		for (int i = myFavorites.size() - 1; i >= 0; i--) {
			final MyFavorite model = myFavorites.get(i);
			if (!newModels.contains(model)) {
				removeItem(i);
			}
		}
	}

	private void applyAndAnimateAdditions(ArrayList<MyFavorite> newModels) {
		for (int i = 0, count = newModels.size(); i < count; i++) {
			final MyFavorite model = newModels.get(i);
			if (!myFavorites.contains(model)) {
				addItem(i, model);
			}
		}
	}

	private void applyAndAnimateMovedItems(ArrayList<MyFavorite> newModels) {
		for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
			final MyFavorite model = newModels.get(toPosition);
			final int fromPosition = myFavorites.indexOf(model);
			if (fromPosition >= 0 && fromPosition != toPosition) {
				moveItem(fromPosition, toPosition);
			}
		}
	}

	public MyFavorite removeItem(int position) {
		final MyFavorite model = myFavorites.remove(position);
		notifyItemRemoved(position);
		return model;
	}

	public void addItem(int position, MyFavorite model) {
		myFavorites.add(position, model);
		notifyItemInserted(position);
	}

	public void moveItem(int fromPosition, int toPosition) {
		final MyFavorite model = myFavorites.remove(fromPosition);
		myFavorites.add(toPosition, model);
		notifyItemMoved(fromPosition, toPosition);
	}


	static class MyFavoriteViewHolder extends RecyclerView.ViewHolder {

		private final TextView mName;
		private final ImageView mPhoto;
		private final LinearLayout mItem;
		private final SwipeLayout mSwipe;
		private final LinearLayout mDelete;

		public MyFavoriteViewHolder(View itemView) {
			super(itemView);
			mItem = (LinearLayout) itemView.findViewById(R.id.item);
			mSwipe = (SwipeLayout) itemView.findViewById(R.id.swipe);
			mDelete = (LinearLayout) itemView.findViewById(R.id.delete);
			mName = (TextView) itemView.findViewById(R.id.nameTextView);
			mPhoto = (ImageView) itemView.findViewById(R.id.imageView);
		}

	}
}
