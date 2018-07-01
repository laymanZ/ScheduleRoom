package com.example.z.scheduleroom.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.AttributeSet;


public class NestedObserverScrollView extends NestedScrollView {

    private ScrollViewListener scrollViewListener;

    public NestedObserverScrollView(@NonNull Context context) {
        super(context);
    }

    public NestedObserverScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NestedObserverScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setNestedScrollViewListener(ScrollViewListener scrollViewListener) {
        this.scrollViewListener = scrollViewListener;
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

        void onScrollChanged(NestedObserverScrollView scrollView, int newX, int newY, int oldX, int oldY);

        void onOverScrolled(NestedObserverScrollView scrollView, int scrollX, int scrollY, boolean clampedX, boolean clampedY);

    }

}
