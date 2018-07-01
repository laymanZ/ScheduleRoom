package com.example.z.scheduleroom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.z.scheduleroom.R;
import com.example.z.scheduleroom.ScreenUtils;
import com.example.z.scheduleroom.bean.CourseBean;

import java.util.List;


public class ScheduleTopAdapter extends RecyclerView.Adapter<ScheduleTopAdapter.TopViewHolder> {

    private List<CourseBean> mData;
    private Context mContext;
    private int normalCellWidth;

    public ScheduleTopAdapter(List<CourseBean> data, Context context) {
        this.mContext = context;
        setData(data);
    }

    @Override
    public TopViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.top_header, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = normalCellWidth;
        view.setLayoutParams(layoutParams);
        return new TopViewHolder(view);
    }


    @Override
    public void onBindViewHolder(TopViewHolder holder, final int position) {

        holder.tvRoomName.setText(mData.get(position).getCourseName());
        holder.tvCapacity.setText(mData.get(position).getCourseId());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(mContext,"这是:"+mData.get(position).getCourseName(),Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    class TopViewHolder extends RecyclerView.ViewHolder {

        public TextView tvRoomName, tvCapacity;

        public TopViewHolder(View itemView) {
            super(itemView);
            tvRoomName = itemView.findViewById(R.id.tv_room_name);
            tvCapacity = itemView.findViewById(R.id.tv_room_capacity);
        }
    }

    public void setData(List<CourseBean> data) {
        this.mData = data;
        if (data != null) {
            int size = data.size();
            int leftCellWidth = ScreenUtils.dp2px(mContext, ScreenUtils.LEFT_CELL_WIDTH);
            switch (size) {
                case 1:
                    normalCellWidth = ScreenUtils.getScreenWidth(mContext) - leftCellWidth;
                    break;
                case 2:
                    normalCellWidth = (ScreenUtils.getScreenWidth(mContext) - leftCellWidth) / 2;
                    break;
                case 3:
                    normalCellWidth = (ScreenUtils.getScreenWidth(mContext) - leftCellWidth) / 3;
                    break;
                default:
                    normalCellWidth = (ScreenUtils.getScreenWidth(mContext) - leftCellWidth - ScreenUtils.dp2px(mContext, ScreenUtils.RIGHT_SHOW_LENGTH)) / 3;
                    break;
            }
        }
    }
}
