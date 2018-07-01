package com.example.z.scheduleroom.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.z.scheduleroom.R;
import com.example.z.scheduleroom.ScreenUtils;
import com.example.z.scheduleroom.api.OnMeetingSelectChange;
import com.example.z.scheduleroom.api.ScrollMotionEventListener;
import com.example.z.scheduleroom.bean.ScheduleStatus;
import com.example.z.scheduleroom.bean.ViewStatusBean;
import com.example.z.scheduleroom.view.ObserverScrollView;

import java.util.ArrayList;
import java.util.List;


public class ScheduleMajorAdapter extends RecyclerView.Adapter<ScheduleMajorAdapter.ScheduleHolder> {

    private List<String> mData;
    private List<List<ViewStatusBean>> mStatusBean;
    private Context mContext;
    private long mStartTime, mEndTime; //判断是否是点击的开始结束时间
    private int mPreRow = -1, mCurrentRow, mPrePosition = -1, mSelectColPosition = -1;
    private View mPreView;
    private ViewGroup mPreContainer;
    private boolean isBottomDivVisible;
    private OnMeetingSelectChange onMeetingSelectChange;
    private int mNormalCellWidth;
    private int mBaseMinHeight, mCellItemHeight, mCellHeight, mOneDp;

    public ScheduleMajorAdapter(List<String> data,
                                Context context) {
        this.mContext = context;
        setData(data);
        initViewStatus();
    }

    @Override
    public ScheduleHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.schedule_major_recyclerview, parent, false);
        FrameLayout frameLayout = view.findViewById(R.id.grid_frame);
        frameLayout.setMinimumHeight(mBaseMinHeight);
        View coverItemContainerLayout = LayoutInflater.from(mContext).inflate(R.layout.cover_item_container_layout, frameLayout, false);
        frameLayout.addView(coverItemContainerLayout, new FrameLayout.LayoutParams(mNormalCellWidth, ViewGroup.LayoutParams.MATCH_PARENT));
        return new ScheduleHolder(view);
    }

    @Override
    public void onBindViewHolder(final ScheduleHolder holder, final int position) {
        holder.root.setTag(position);

        int tempRow = -1;
        int k = 0;

        holder.scrollView.requestDisallowInterceptTouchEvent(false);
        holder.frameLayout.setMinimumHeight(mBaseMinHeight);
        holder.frameLayout.removeAllViews();

        if (mPreView != null && position == getSelectColPosition()) {
            for (int i = 0; i < 19; i++) {
//            记录已经选中的区域
                for (int j = 0; j < 4; j++) {
                    if (mStatusBean.get(position).get(i * 4 + j).isHadColor()) {
                        if (tempRow == -1) {
                            tempRow = i * 4 + j;
                        }
                        k++;
                    }
                }
            }
        }

//      是否有已经选中的区域
        if (tempRow >= 0 && k > 0) {
            View selectView = LayoutInflater.from(mContext).inflate(R.layout.select_layout, holder.frameLayout, false);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) selectView.getLayoutParams();
            lp.topMargin = ScreenUtils.getTopMargin(mContext, tempRow);
            lp.width = mNormalCellWidth;
            lp.height = ScreenUtils.dp2px(mContext, k * ScreenUtils.NORMAL_CELL_ITEM_HEIGHT);
            selectView.setLayoutParams(lp);
            TextView tvView = selectView.findViewById(R.id.select_time);
            tvView.setText(getSelectAreaTime(tempRow, tempRow + k - 1));
            View bottomDivider = selectView.findViewById(R.id.select_bottom_divider);
            if (isBottomDivVisible) {
                bottomDivider.setVisibility(View.VISIBLE);
            }
            if (mPreView != null && mPreContainer != null) {
                mPreContainer.removeView(mPreView);
            }
            holder.frameLayout.addView(selectView);
            mPreView = selectView;
            mPreContainer = holder.frameLayout;
        }

        holder.scrollView.setScrollMotionEventListener(new ScrollMotionEventListener() {
            @Override
            public void onActionDown(float x, float y) {

            }

            @Override
            public void onActionMove(float x, float y) {

            }

            @Override
            public void onActionUp(float x, float y) {
                mEndTime = System.currentTimeMillis();
                if (mEndTime - mStartTime >= 0 && mEndTime - mStartTime < ViewConfiguration.getLongPressTimeout()
                        && mStatusBean.get(position).get(mCurrentRow).getStatus() != ScheduleStatus.OUTDATED) {
                    setSelectColPosition(position);
                    if (mPrePosition != -1) {
                        // 不同列
                        if (position != mPrePosition) {
                            actionByDiffCol(holder.frameLayout, position);
                            mPrePosition = position;
                        }
                        // 同一列
                        else {
                            actionBySameCol(holder.frameLayout, position);
                        }
                    } else {
                        actionBySameCol(holder.frameLayout, position);
                        mPrePosition = position;
                    }
                }
            }
        });

        holder.frameContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartTime = System.currentTimeMillis();
                        mCurrentRow = getPosition(event.getY());
                        break;
                }
                return false;
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    static class ScheduleHolder extends ViewHolder {

        public ObserverScrollView scrollView;
        public FrameLayout frameLayout;
        public FrameLayout frameContainer;
        public View root;

        public ScheduleHolder(View itemView) {
            super(itemView);
            root = itemView;
            frameContainer = itemView.findViewById(R.id.frame_container);
            frameLayout = itemView.findViewById(R.id.major_frame);
            scrollView = itemView.findViewById(R.id.major_scrollView);
        }
    }

    public void setData(List<String> data) {
        this.mData = data;
        if (data != null) {
            mCellHeight = ScreenUtils.dp2px(mContext, ScreenUtils.NORMAL_CELL_HEIGHT);
            mBaseMinHeight = mCellHeight * 19;
            mCellItemHeight = ScreenUtils.dp2px(mContext, ScreenUtils.NORMAL_CELL_ITEM_HEIGHT);
            mOneDp = ScreenUtils.dp2px(mContext, 1);
            int size = data.size();
            int leftCellWidth = ScreenUtils.dp2px(mContext, ScreenUtils.LEFT_CELL_WIDTH);
            switch (size) {
                case 1:
                    mNormalCellWidth = ScreenUtils.getScreenWidth(mContext) - leftCellWidth;
                    break;
                case 2:
                    mNormalCellWidth = (ScreenUtils.getScreenWidth(mContext) - leftCellWidth) / 2;
                    break;
                case 3:
                    mNormalCellWidth = (ScreenUtils.getScreenWidth(mContext) - leftCellWidth) / 3;
                    break;
                default:
                    mNormalCellWidth = (ScreenUtils.getScreenWidth(mContext) - leftCellWidth - ScreenUtils.dp2px(mContext, ScreenUtils.RIGHT_SHOW_LENGTH)) / 3;
                    break;
            }
        }
    }

    /**
     * 同一列
     */
    private void actionBySameCol(ViewGroup container, int position) {
        mPreContainer = container;
        if (mPreRow != -1) {
            if (mPreRow < mCurrentRow) {
                if (mStatusBean.get(position).get(mCurrentRow).isHadColor()) {
                    for (int j = mPreRow; j <= mCurrentRow; j++) {
                        mStatusBean.get(position).get(j).setHadColor(false);
                    }
                    if (mPreView != null) {
                        container.removeView(mPreView);
                    }
                    isBottomDivVisible = false;
                    mPreRow = -1;
                    setSelectColPosition(-1);
                    if (onMeetingSelectChange != null) {
                        onMeetingSelectChange.onIsBottomDivVisible(isBottomDivVisible);
                        onMeetingSelectChange.onTimeAndPlace("", -1);
                    }
                } else {
                    for (int j = mPreRow + 1; j <= mCurrentRow; j++) {
                        mStatusBean.get(position).get(j).setHadColor(true);
                    }
                    int diff = mCurrentRow - mPreRow + 1;
                    View view = LayoutInflater.from(mContext).inflate(R.layout.select_layout, container, false);
                    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
                    layoutParams.topMargin = ScreenUtils.getTopMargin(mContext, mPreRow);
                    layoutParams.width = mNormalCellWidth;
                    layoutParams.height = ScreenUtils.dp2px(mContext, diff * ScreenUtils.NORMAL_CELL_ITEM_HEIGHT);
                    view.setLayoutParams(layoutParams);
                    TextView textView = view.findViewById(R.id.select_time);
                    textView.setText(getSelectAreaTime(mPreRow, mCurrentRow));
                    View bottomDivider = view.findViewById(R.id.select_bottom_divider);
                    bottomDivider.setVisibility(View.VISIBLE);
                    isBottomDivVisible = true;
                    if (mPreView != null) {
                        container.removeView(mPreView);
                    }
                    container.addView(view);
                    mPreView = view;
                    mPreRow = -1;

                    if (onMeetingSelectChange != null) {
                        onMeetingSelectChange.onIsBottomDivVisible(isBottomDivVisible);
                    }
                }
            }
//            重置，相当于重新选择，可考虑封装成一个方法
            else if (mPreRow > mCurrentRow) {
                resetSelect(position, -1, container);
            } else if (mPreRow == mCurrentRow) {
                mStatusBean.get(position).get(mCurrentRow).setHadColor(false);
                if (mStatusBean.get(position).get(mCurrentRow + 1).isHadColor()) {
                    mStatusBean.get(position).get(mCurrentRow + 1).setHadColor(false);
                }
                if (mPreView != null) {
                    container.removeView(mPreView);
                }
                mPreRow = -1;
                setSelectColPosition(-1);
                if (onMeetingSelectChange != null) {
                    onMeetingSelectChange.onIsBottomDivVisible(isBottomDivVisible);
                    onMeetingSelectChange.onTimeAndPlace("", -1);
                }
            }
        } else {
            resetSelect(position, -1, container);
        }
    }

    /**
     * 不同列
     */
    private void actionByDiffCol(ViewGroup container, int position) {
        resetSelect(position, mPrePosition, container);
    }

    /**
     * 获取点击哪一行
     */
    private int getPosition(float y) {
        int position = 0;
        for (int i = 1; i <= 19 * 4; i++) {
            if (i * mCellItemHeight < y && (i + 1) * mCellItemHeight > y) {
                position = i;
                break;
            }
        }
        return position;
    }

    /**
     * 添加选中区域
     */
    private void addSelectArea(ViewGroup container, int rowNum, int topRow) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.select_layout, container, false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.topMargin = ScreenUtils.getTopMargin(mContext, topRow);
        layoutParams.width = mNormalCellWidth;
        layoutParams.height = rowNum * ScreenUtils.dp2px(mContext, ScreenUtils.NORMAL_CELL_ITEM_HEIGHT);
        view.setLayoutParams(layoutParams);
        if (mPreView != null) {
            container.removeView(mPreView);
        }
        container.addView(view);
        mPreView = view;
    }


    /**
     * 重置选择，相当于重新选择
     */
    private void resetSelect(int position, int prePosition, ViewGroup container) {
        int diff = 1;
        int size = mStatusBean.get(position).size();
        if (prePosition != -1) {
            for (int i = 4; i < size - 4; i++) {
                mStatusBean.get(prePosition).get(i).setHadColor(false);
            }
        } else {
            for (int i = 4; i < size - 4; i++) {
                mStatusBean.get(position).get(i).setHadColor(false);
            }
        }
        mStatusBean.get(position).get(mCurrentRow).setHadColor(true);
        mPreRow = mCurrentRow;
        if (!((mStatusBean.get(position).get(mCurrentRow + 1).getStatus() == ScheduleStatus.UNRELATED_MEETING)
                || (mStatusBean.get(position).get(mCurrentRow + 1).getStatus() == ScheduleStatus.RELATED_MEETING)
                || (mStatusBean.get(position).get(mCurrentRow + 1).getStatus() == ScheduleStatus.OUTDATED))) {
            if (!mStatusBean.get(position).get(mCurrentRow + 1).isHadColor()) {
                mStatusBean.get(position).get(mCurrentRow + 1).setHadColor(true);
                diff = 2;
            }
        }

        View view = LayoutInflater.from(mContext).inflate(R.layout.select_layout, container, false);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.topMargin = ScreenUtils.getTopMargin(mContext, mCurrentRow);
        layoutParams.width = mNormalCellWidth;
        layoutParams.height = ScreenUtils.dp2px(mContext, diff * ScreenUtils.NORMAL_CELL_ITEM_HEIGHT);
        view.setLayoutParams(layoutParams);
        TextView textView = view.findViewById(R.id.select_time);

        if (diff == 1) {
            View bottomDivider = view.findViewById(R.id.select_bottom_divider);
            bottomDivider.setVisibility(View.VISIBLE);
            isBottomDivVisible = true;
            textView.setText(getSelectAreaTime(mCurrentRow, mCurrentRow));
        } else {
            if (!((mStatusBean.get(position).get(mCurrentRow + 2).getStatus() == ScheduleStatus.UNRELATED_MEETING)
                    || (mStatusBean.get(position).get(mCurrentRow + 2).getStatus() == ScheduleStatus.RELATED_MEETING)
                    || (mStatusBean.get(position).get(mCurrentRow + 2).getStatus() == ScheduleStatus.OUTDATED))) {
                View bottomDivider = view.findViewById(R.id.select_bottom_divider);
                bottomDivider.setVisibility(View.GONE);
                isBottomDivVisible = false;
            } else {
                View bottomDivider = view.findViewById(R.id.select_bottom_divider);
                bottomDivider.setVisibility(View.VISIBLE);
                isBottomDivVisible = true;
            }
            textView.setText(getSelectAreaTime(mCurrentRow, mCurrentRow + 1));
        }

        if (mPreView != null) {
            container.removeView(mPreView);
        }
        if (mPreContainer != null && mPreView != null) {
            mPreContainer.removeView(mPreView);
        }
        container.addView(view);
        mPreContainer = container;
        mPreView = view;
        if (onMeetingSelectChange != null) {
            onMeetingSelectChange.onIsBottomDivVisible(isBottomDivVisible);
        }

    }


    /**
     * 获取选择区域的时间
     */
    private String getSelectAreaTime(int startRow, int endRow) {
        StringBuilder result = new StringBuilder();
        int startH = startRow / 4 + 6;
        int startM = startRow % 4 * 15;
        int endH = endRow / 4 + 6;
        if (endRow % 4 == 3) {
            ++endH;
        }
        int endM = endRow % 4 * 15 + 15;
        if (endM == 60) {
            endM = 0;
        }

        result.append(startH).append(":");
        if (startM <= 9 && startM >= 0) {
            result.append("0").append(startM);
        } else {
            result.append(startM);
        }
        result.append("-").append(endH).append(":");
        if (endM <= 9 && endM >= 0) {
            result.append("0").append(endM);
        } else {
            result.append(endM);
        }
        if (onMeetingSelectChange != null) {
            onMeetingSelectChange.onTimeAndPlace(result.toString(), mSelectColPosition);
        }
        return result.toString();
    }


    private void initViewStatus() {
        mStatusBean = new ArrayList<>();
        int size = mData.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                List<ViewStatusBean> list = new ArrayList<>();
                for (int j = 0; j < 19 * 4; j++) {
                    ViewStatusBean viewStatusBean = new ViewStatusBean();
                    list.add(viewStatusBean);
                }
                mStatusBean.add(list);
                mStatusBean.get(i).get(0).setStatus(ScheduleStatus.OUTDATED);
                mStatusBean.get(i).get(1).setStatus(ScheduleStatus.OUTDATED);
                mStatusBean.get(i).get(2).setStatus(ScheduleStatus.OUTDATED);
                mStatusBean.get(i).get(3).setStatus(ScheduleStatus.OUTDATED);
                mStatusBean.get(i).get(18 * 4).setStatus(ScheduleStatus.OUTDATED);
                mStatusBean.get(i).get(18 * 4 + 1).setStatus(ScheduleStatus.OUTDATED);
                mStatusBean.get(i).get(18 * 4 + 2).setStatus(ScheduleStatus.OUTDATED);
                mStatusBean.get(i).get(18 * 4 + 3).setStatus(ScheduleStatus.OUTDATED);
            }
        }
    }


    /**
     * 重置，回到刚开始进来的状态
     */
    public void resetToInitStatus() {

    }

    public int getSelectColPosition() {
        return mSelectColPosition;
    }

    public void setSelectColPosition(int selectColPosition) {
        this.mSelectColPosition = selectColPosition;
    }

    public void setOnMeetingSelectChange(OnMeetingSelectChange onMeetingSelectChange) {
        this.onMeetingSelectChange = onMeetingSelectChange;
    }

}
