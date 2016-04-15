package com.mmlab.n1.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.mmlab.n1.R;
import com.mmlab.n1.model.WifiRecord;
import com.mmlab.n1.network.NetworkManagerN2;
import com.mmlab.n1.view.ChickenView;

import java.util.List;

/**
 * Created by mmlab on 2015/9/16.
 */
public class WifiRecordAdapter extends RecyclerView.Adapter<WifiRecordAdapter.MyViewHolder> {

    private static final String TAG = "WifiRecordAdapter";
    private Context mContext = null;

    private List<WifiRecord> mRecords = null;

    public WifiRecordAdapter(Context context, List<WifiRecord> records) {
        this.mRecords = records;
        this.mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wifirecord, parent, false));
    }

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private OnItemClickLitener mOnItemClickLitener;

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        switch (NetworkManagerN2.calculateSignalStength(mRecords.get(position).level)) {
            case 1:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_1));
                break;
            case 2:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_2));
                break;
            case 3:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_3));
                break;
            case 4:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_4));
                break;
            default:
                holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_signal_wifi_0));
        }

        if (mRecords.get(position).isHost) {
            holder.wifirecord_menu.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_remove_red_eye_blue_grey_500_24dp));
        }

        holder.wifirecord_title.setText(mRecords.get(position).SSID);
        holder.wifirecord_content.setText(mRecords.get(position).getStatus());
//        if(mRecords.get(position).getStatus().contains("已連線")){
//            holder.wifirecord_title.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDark));
//            holder.wifirecord_content.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
//        }
        holder.wifirecord_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(mContext, "menu", Toast.LENGTH_SHORT).show();
            }
        });

        // 如果設置了回調，則設置點擊事件
        if (mOnItemClickLitener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemClick(holder.itemView, pos);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int pos = holder.getLayoutPosition();
                    mOnItemClickLitener.onItemLongClick(holder.itemView, pos);
                    return false;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView wifirecord_title;
        TextView wifirecord_content;
        ChickenView wifirecord_menu;

        public MyViewHolder(View view) {
            super(view);
            wifirecord_title = (TextView) view.findViewById(R.id.wifirecord_title);
            wifirecord_content = (TextView) view.findViewById(R.id.wifirecord_content);
            wifirecord_menu = (ChickenView) view.findViewById(R.id.wifirecord_menu);
            wifirecord_menu.setClickable(false);
            wifirecord_menu.setFocusable(false);
            wifirecord_menu.setFocusableInTouchMode(false);
            // Log.d(TAG, "menu : " + wifirecord_menu.getParent().toString());
        }
    }
}
