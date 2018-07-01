package com.example.z.scheduleroom.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

import com.example.z.scheduleroom.api.ScrollMotionEventListener;


public class ObserverScrollView extends ScrollView {

    private ScrollViewListener scrollViewListener;
    private float mTempX, mTempY;
    private ScrollMotionEventListener mScrollMotionEventListener;

    public ObserverScrollView(Context context) {
        super(context);
    }

    public ObserverScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ObserverScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
    }

    public void setScrollMotionEventListener(ScrollMotionEventListener eventListener) {
        this.mScrollMotionEventListener = eventListener;
    }


    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (scrollViewListener != null) {
            scrollViewListener.onScrollChanged(this, x, y, oldX, oldY);
        }
    }

    @Override
    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
        if (scrollViewListener != null) {
            scrollViewListener.onOverScrolled(this, scrollX, scrollY, clampedX, clampedY);
        }
    }

    public interface ScrollViewListener {

        void onScrollChanged(ObserverScrollView scrollView, int newX, int newY, int oldX, int oldY);

        void onOverScrolled(ObserverScrollView scrollView, int scrollX, int scrollY, boolean clampedX, boolean clampedY);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTempX = event.getX();
                mTempY = event.getY();
                if (mScrollMotionEventListener != null) {
                    mScrollMotionEventListener.onActionDown(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                int disX = (int) (event.getX() - mTempX);
                int disY = (int) (event.getY() - mTempY);
                mTempX = event.getX();
                mTempY = event.getY();
                if (mScrollMotionEventListener != null) {
                    mScrollMotionEventListener.onActionMove(event.getX(), event.getY());
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mScrollMotionEventListener != null) {
                    mScrollMotionEventListener.onActionUp(event.getX(), event.getY());
                }
                break;
        }
        return super.onTouchEvent(event);
    }

}
