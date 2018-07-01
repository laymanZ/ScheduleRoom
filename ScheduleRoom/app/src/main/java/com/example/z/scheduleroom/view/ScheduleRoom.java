package com.example.z.scheduleroom.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.z.scheduleroom.R;
import com.example.z.scheduleroom.ScreenUtils;


public class ScheduleRoom extends FrameLayout {

    private Context mContext;

    private RecyclerView mRecyclerView;
    private RecyclerView topRecyclerView;
    //    private ObserverScrollView leftScrollView;
    private NestedObserverScrollView leftScrollView;

    private int leftCellWidth, topCellHeight, normalCellWidth;

    public ScheduleRoom(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    public ScheduleRoom(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyScheduleRoom);
        leftCellWidth = (int) a.getDimension(R.styleable.MyScheduleRoom_schedule_left_cell_width, 0);
        topCellHeight = (int) a.getDimension(R.styleable.MyScheduleRoom_schedule_top_cell_height, 0);
        normalCellWidth = (ScreenUtils.getScreenWidth(context) - leftCellWidth - ScreenUtils.dp2px(context, ScreenUtils.RIGHT_SHOW_LENGTH)) / 3;
        a.recycle();
        initWidget();
    }

    private void initWidget() {
        final int NORMAL_CELL_HEIGHT = ScreenUtils.dp2px(getContext(), ScreenUtils.NORMAL_CELL_HEIGHT);
        final int LEFT_CELL_WIDTH = ScreenUtils.dp2px(getContext(), ScreenUtils.LEFT_CELL_WIDTH);
        final int OUTSIDE_DIVIDER = ScreenUtils.dp2px(getContext(), ScreenUtils.OUTDATED_DIVIDER);
        final int PX_EIGHT_HEIGHT = ScreenUtils.dp2px(getContext(), 8);

        //major RecyclerView
        mRecyclerView = new RecyclerView(getContext());
        addView(mRecyclerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        LayoutParams mlp = (LayoutParams) mRecyclerView.getLayoutParams();
        mlp.leftMargin = leftCellWidth;
        mlp.topMargin = topCellHeight;
        mRecyclerView.setLayoutParams(mlp);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.smoothScrollToPosition(0);


        View view = new View(getContext());
        view.setBackgroundColor(Color.parseColor("#dddddd"));
        addView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ScreenUtils.dp2px(getContext(), 0.5)));
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        layoutParams.topMargin = topCellHeight;
        view.setLayoutParams(layoutParams);


        //top RecyclerView
        topRecyclerView = new RecyclerView(getContext());
        topRecyclerView.setHasFixedSize(true);
        addView(topRecyclerView, new LayoutParams(LayoutParams.WRAP_CONTENT, topCellHeight));
        LayoutParams tlp = (LayoutParams) topRecyclerView.getLayoutParams();
        tlp.leftMargin = leftCellWidth;
        topRecyclerView.setLayoutParams(tlp);
        topRecyclerView.requestDisallowInterceptTouchEvent(true);


        leftScrollView = new NestedObserverScrollView(getContext());
        addView(leftScrollView, new LayoutParams(leftCellWidth, LayoutParams.MATCH_PARENT));
        leftScrollView.setVerticalScrollBarEnabled(false);
        FrameLayout leftFrameLayout = new FrameLayout(getContext());
        leftScrollView.addView(leftFrameLayout, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        for (int i = 7; i <= 25; i++) {
            View leftTimeView = LayoutInflater.from(getContext()).inflate(R.layout.left_time_layout, this, false);
            leftFrameLayout.addView(leftTimeView, new LayoutParams(leftCellWidth, ViewGroup.LayoutParams.WRAP_CONTENT));
            LayoutParams layoutParams1 = (LayoutParams) leftTimeView.getLayoutParams();
            TextView textView = leftTimeView.findViewById(R.id.select_course_time);
            if (i == 25) {
                textView.setVisibility(GONE);
                layoutParams1.topMargin = (i - 6) * NORMAL_CELL_HEIGHT + topCellHeight;
                leftTimeView.setLayoutParams(layoutParams1);
            } else {
                textView.setText(String.valueOf(i));
                layoutParams1.topMargin = (i - 6) * NORMAL_CELL_HEIGHT - PX_EIGHT_HEIGHT + topCellHeight;
                leftTimeView.setLayoutParams(layoutParams1);
            }
        }

        View leftDivider = new View(getContext());
        leftDivider.setBackgroundColor(Color.parseColor("#e8e8e8"));
        addView(leftDivider, new LayoutParams(OUTSIDE_DIVIDER, ViewGroup.LayoutParams.MATCH_PARENT));
        LayoutParams leftDivParams = (LayoutParams) leftDivider.getLayoutParams();
        leftDivParams.leftMargin = LEFT_CELL_WIDTH;
        leftDivParams.width = OUTSIDE_DIVIDER;
        leftDivider.setLayoutParams(leftDivParams);

        //左上角的空白块
        View spaceBlock = LayoutInflater.from(getContext()).inflate(R.layout.left_top, this, false);
        addView(spaceBlock);


        listenScroll();
    }

    private void listenScroll() {
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (topRecyclerView.getScrollState() == 0) {
                    topRecyclerView.scrollBy(dx, 0);
                }
                if (mRecyclerView.getScrollState() != 0) {
                    leftScrollView.scrollBy(0, dy);
                }
            }
        });

        topRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mRecyclerView.getScrollState() == 0) {
                    mRecyclerView.scrollBy(dx, 0);
                }
            }
        });

        leftScrollView.setNestedScrollViewListener(new NestedObserverScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(NestedObserverScrollView scrollView, int newX, int newY, int oldX, int oldY) {
                if (mRecyclerView.getScrollState() == 0) {
                    mRecyclerView.scrollBy(0, newY - oldY);
                }
            }

            @Override
            public void onOverScrolled(NestedObserverScrollView scrollView, int scrollX, int scrollY, boolean clampedX, boolean clampedY) {

            }
        });
    }

    public RecyclerView getMajorRecyclerView() {
        return mRecyclerView;
    }

    public RecyclerView getTopRecyclerView() {
        return topRecyclerView;
    }

}
