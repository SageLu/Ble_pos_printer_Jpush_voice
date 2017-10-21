package com.sagelu.ble_pos_printer_jpush_voice.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sagelu.ble_pos_printer_jpush_voice.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by lenovo on 2016/12/21.
 */
public class BleDevicesRecyclerViewAdapter extends RecyclerView.Adapter<BleDevicesRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private List<Map<String, Object>> datas = new ArrayList<>();

    public BleDevicesRecyclerViewAdapter(Context context) {
        this.context = context;

    }

    public void setDatas(List datas) {
        if (datas!= null) {
            this.datas = datas;
        }
        notifyDataSetChanged();
//        notifyItemRangeChanged(0, datas.getData().size());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_printername, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Map<String, Object> map = datas.get(position);
        String printername = (String) map.get("PRINTERNAME");
        holder.tv_name_bledevice.setText(printername);


    }

    @Override
    public int getItemCount() {
        return datas.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_name_bledevice;
        private TextView tv_choose;

        public ViewHolder(View itemView) {
            super(itemView);
            tv_name_bledevice = (TextView) itemView.findViewById(R.id.tv_name_bledevice);
            tv_choose = (TextView) itemView.findViewById(R.id.tv_choose);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onItemClickListener != null) {
                        onItemClickListener.OnItemClick(view,getLayoutPosition(),datas.get(getLayoutPosition()));
                    }
                }
            });

        }
    }

    public interface OnItemClickListener {

        public void OnItemClick(View view, int position, Map<String, Object> data);
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
