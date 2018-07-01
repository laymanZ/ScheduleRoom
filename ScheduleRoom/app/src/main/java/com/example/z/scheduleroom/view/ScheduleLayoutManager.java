package com.example.z.scheduleroom.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.List;


public class ScheduleLayoutManager extends RecyclerView.LayoutManager {

    private static final int DEFAULT_COUNT = 1;

    private static final int REMOVE_VISIBLE = 0;
    private static final int REMOVE_INVISIBLE = 1;

    private static final int DIRECTION_NONE = -1;
    private static final int DIRECTION_START = 0;
    private static final int DIRECTION_END = 1;
    private static final int DIRECTION_UP = 2;
    private static final int DIRECTION_DOWN = 3;

    private int mFirstVisiblePosition;
    private int mDecoratedChildWidth;
    private int mDecoratedChildHeight;
    private int mTotalColumnCount = DEFAULT_COUNT;
    private int mVisibleColumnCount;
    private int mVisibleRowCount;

    private int mFirstChangedPosition;
    private int mChangedPositionCount;

    /**
     * 设置最大列数
     *
     * @param count 列数量
     */
    public void setTotalColumnCount(int count) {
        mTotalColumnCount = count;
        requestLayout();
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return true;
    }

    @Override
    public void onItemsRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        mFirstChangedPosition = positionStart;
        mChangedPositionCount = itemCount;
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getItemCount() == 0) {
            detachAndScrapAttachedViews(recycler);
            return;
        }
        if (getChildCount() == 0 && state.isPreLayout()) {
            return;
        }

        if (!state.isPreLayout()) {
            mFirstChangedPosition = mChangedPositionCount = 0;
        }

        if (getChildCount() == 0) {
            View scrap = recycler.getViewForPosition(0);
            addView(scrap);
            measureChildWithMargins(scrap, 0, 0);

            mDecoratedChildWidth = getDecoratedMeasuredWidth(scrap);
            mDecoratedChildHeight = getDecoratedMeasuredHeight(scrap);

            detachAndScrapView(scrap, recycler);
        }

        updateWindowSizing();
        SparseIntArray removedCache = null;

        if (state.isPreLayout()) {
            removedCache = new SparseIntArray(getChildCount());
            for (int i = 0; i < getChildCount(); i++) {
                final View view = getChildAt(i);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();

                if (lp.isItemRemoved()) {
                    removedCache.put(lp.getViewLayoutPosition(), REMOVE_VISIBLE);
                }
            }
            if (removedCache.size() == 0 && mChangedPositionCount > 0) {
                for (int i = mFirstChangedPosition; i < (mFirstChangedPosition + mChangedPositionCount); i++) {
                    removedCache.put(i, REMOVE_INVISIBLE);
                }
            }
        }

        int childLeft;
        int childTop;
        if (getChildCount() == 0) {
            mFirstVisiblePosition = 0;
            childLeft = getPaddingLeft();
            childTop = getPaddingTop();
        } else if (!state.isPreLayout()
                && getVisibleChildCount() >= state.getItemCount()) {
            mFirstVisiblePosition = 0;
            childLeft = getPaddingLeft();
            childTop = getPaddingTop();
        } else {
            final View topChild = getChildAt(0);
            childLeft = getDecoratedLeft(topChild);
            childTop = getDecoratedTop(topChild);
            if (!state.isPreLayout() && getVerticalSpace() > (getTotalRowCount() * mDecoratedChildHeight)) {
                mFirstVisiblePosition = mFirstVisiblePosition % getTotalColumnCount();
                childTop = getPaddingTop();
                if ((mFirstVisiblePosition + mVisibleColumnCount) > state.getItemCount()) {
                    mFirstVisiblePosition = Math.max(state.getItemCount() - mVisibleColumnCount, 0);
                    childLeft = getPaddingLeft();
                }
            }
            int maxFirstRow = getTotalRowCount() - (mVisibleRowCount - 1);
            int maxFirstCol = getTotalColumnCount() - (mVisibleColumnCount - 1);
            boolean isOutOfRowBounds = getFirstVisibleRow() > maxFirstRow;
            boolean isOutOfColBounds = getFirstVisibleColumn() > maxFirstCol;
            if (isOutOfRowBounds || isOutOfColBounds) {
                int firstRow;
                if (isOutOfRowBounds) {
                    firstRow = maxFirstRow;
                } else {
                    firstRow = getFirstVisibleRow();
                }
                int firstCol;
                if (isOutOfColBounds) {
                    firstCol = maxFirstCol;
                } else {
                    firstCol = getFirstVisibleColumn();
                }
                mFirstVisiblePosition = firstRow * getTotalColumnCount() + firstCol;

                childLeft = getHorizontalSpace() - (mDecoratedChildWidth * mVisibleColumnCount);
                childTop = getVerticalSpace() - (mDecoratedChildHeight * mVisibleRowCount);

                if (getFirstVisibleRow() == 0) {
                    childTop = Math.min(childTop, getPaddingTop());
                }
                if (getFirstVisibleColumn() == 0) {
                    childLeft = Math.min(childLeft, getPaddingLeft());
                }
            }
        }

        detachAndScrapAttachedViews(recycler);

        fillGrid(DIRECTION_NONE, childLeft, childTop, recycler, state, removedCache);

        if (!state.isPreLayout() && !recycler.getScrapList().isEmpty()) {
            final List<RecyclerView.ViewHolder> scrapList = recycler.getScrapList();
            final HashSet<View> disappearingViews = new HashSet<View>(scrapList.size());

            for (RecyclerView.ViewHolder holder : scrapList) {
                final View child = holder.itemView;
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (!lp.isItemRemoved()) {
                    disappearingViews.add(child);
                }
            }
            for (View child : disappearingViews) {
                layoutDisappearingView(child);
            }
        }
    }

    @Override
    public void onAdapterChanged(RecyclerView.Adapter oldAdapter, RecyclerView.Adapter newAdapter) {
        removeAllViews();
    }

    private void updateWindowSizing() {
        mVisibleColumnCount = (getHorizontalSpace() / mDecoratedChildWidth) + 1;
        if (getHorizontalSpace() % mDecoratedChildWidth > 0) {
            mVisibleColumnCount++;
        }
        if (mVisibleColumnCount > getTotalColumnCount()) {
            mVisibleColumnCount = getTotalColumnCount();
        }
        mVisibleRowCount = (getVerticalSpace() / mDecoratedChildHeight) + 1;
        if (getVerticalSpace() % mDecoratedChildHeight > 0) {
            mVisibleRowCount++;
        }
        if (mVisibleRowCount > getTotalRowCount()) {
            mVisibleRowCount = getTotalRowCount();
        }
    }

    private void fillGrid(int direction, RecyclerView.Recycler recycler, RecyclerView.State state) {
        fillGrid(direction, 0, 0, recycler, state, null);
    }

    private void fillGrid(int direction, int emptyLeft, int emptyTop,
                          RecyclerView.Recycler recycler,
                          RecyclerView.State state,
                          SparseIntArray removedPositions) {
        if (mFirstVisiblePosition < 0) mFirstVisiblePosition = 0;
        if (mFirstVisiblePosition >= getItemCount()) mFirstVisiblePosition = (getItemCount() - 1);

        SparseArray<View> viewCache = new SparseArray<View>(getChildCount());
        int startLeftOffset = emptyLeft;
        int startTopOffset = emptyTop;
        if (getChildCount() != 0) {
            final View topView = getChildAt(0);
            startLeftOffset = getDecoratedLeft(topView);
            startTopOffset = getDecoratedTop(topView);
            switch (direction) {
                case DIRECTION_START:
                    startLeftOffset -= mDecoratedChildWidth;
                    break;
                case DIRECTION_END:
                    startLeftOffset += mDecoratedChildWidth;
                    break;
                case DIRECTION_UP:
                    startTopOffset -= mDecoratedChildHeight;
                    break;
                case DIRECTION_DOWN:
                    startTopOffset += mDecoratedChildHeight;
                    break;
            }
            for (int i = 0; i < getChildCount(); i++) {
                int position = positionOfIndex(i);
                final View child = getChildAt(i);
                viewCache.put(position, child);
            }

            for (int i = 0; i < viewCache.size(); i++) {
                detachView(viewCache.valueAt(i));
            }
        }

        switch (direction) {
            case DIRECTION_START:
                mFirstVisiblePosition--;
                break;
            case DIRECTION_END:
                mFirstVisiblePosition++;
                break;
            case DIRECTION_UP:
                mFirstVisiblePosition -= getTotalColumnCount();
                break;
            case DIRECTION_DOWN:
                mFirstVisiblePosition += getTotalColumnCount();
                break;
        }

        int leftOffset = startLeftOffset;
        int topOffset = startTopOffset;
        for (int i = 0; i < getVisibleChildCount(); i++) {
            int nextPosition = positionOfIndex(i);
            int offsetPositionDelta = 0;
            if (state.isPreLayout()) {
                int offsetPosition = nextPosition;

                for (int offset = 0; offset < removedPositions.size(); offset++) {
                    if (removedPositions.valueAt(offset) == REMOVE_INVISIBLE
                            && removedPositions.keyAt(offset) < nextPosition) {
                        offsetPosition--;
                    }
                }
                offsetPositionDelta = nextPosition - offsetPosition;
                nextPosition = offsetPosition;
            }

            if (nextPosition < 0 || nextPosition >= state.getItemCount()) {
                continue;
            }

            View view = viewCache.get(nextPosition);
            if (view == null) {
                view = recycler.getViewForPosition(nextPosition);
                addView(view);
                if (!state.isPreLayout()) {
                    LayoutParams lp = (LayoutParams) view.getLayoutParams();
                    lp.row = getGlobalRowOfPosition(nextPosition);
                    lp.column = getGlobalColumnOfPosition(nextPosition);
                }
                measureChildWithMargins(view, 0, 0);
                layoutDecorated(view, leftOffset, topOffset,
                        leftOffset + mDecoratedChildWidth,
                        topOffset + mDecoratedChildHeight);

            } else {
                attachView(view);
                viewCache.remove(nextPosition);
            }

            if (i % mVisibleColumnCount == (mVisibleColumnCount - 1)) {
                leftOffset = startLeftOffset;
                topOffset += mDecoratedChildHeight;
                if (state.isPreLayout()) {
                    layoutAppearingViews(recycler, view, nextPosition, removedPositions.size(), offsetPositionDelta);
                }
            } else {
                leftOffset += mDecoratedChildWidth;
            }
        }
        for (int i = 0; i < viewCache.size(); i++) {
            final View removingView = viewCache.valueAt(i);
            recycler.recycleView(removingView);
        }
    }


    @Override
    public void scrollToPosition(int position) {
        if (position >= getItemCount()) {
            return;
        }
        mFirstVisiblePosition = position;
        removeAllViews();
        requestLayout();
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        if (position >= getItemCount()) {
            return;
        }
        LinearSmoothScroller scroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            public PointF computeScrollVectorForPosition(int targetPosition) {
                final int rowOffset = getGlobalRowOfPosition(targetPosition)
                        - getGlobalRowOfPosition(mFirstVisiblePosition);
                final int columnOffset = getGlobalColumnOfPosition(targetPosition)
                        - getGlobalColumnOfPosition(mFirstVisiblePosition);

                return new PointF(columnOffset * mDecoratedChildWidth, rowOffset * mDecoratedChildHeight);
            }
        };
        scroller.setTargetPosition(position);
        startSmoothScroll(scroller);
    }

    @Override
    public boolean canScrollHorizontally() {
        return true;
    }

    @Override
    public int scrollHorizontallyBy(int dx, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }

        final View topView = getChildAt(0);
        final View bottomView = getChildAt(mVisibleColumnCount - 1);

        int viewSpan = getDecoratedRight(bottomView) - getDecoratedLeft(topView);
        if (viewSpan < getHorizontalSpace()) {
            return 0;
        }

        int delta;
        boolean leftBoundReached = getFirstVisibleColumn() == 0;
        boolean rightBoundReached = getLastVisibleColumn() >= getTotalColumnCount();
        if (dx > 0) {
            if (rightBoundReached) {
                int rightOffset = getHorizontalSpace() - getDecoratedRight(bottomView) + getPaddingRight();
                delta = Math.max(-dx, rightOffset);
            } else {
                delta = -dx;
            }
        } else {
            if (leftBoundReached) {
                int leftOffset = -getDecoratedLeft(topView) + getPaddingLeft();
                delta = Math.min(-dx, leftOffset);
            } else {
                delta = -dx;
            }
        }

        offsetChildrenHorizontal(delta);

        if (dx > 0) {
            if (getDecoratedRight(topView) < 0 && !rightBoundReached) {
                fillGrid(DIRECTION_END, recycler, state);
            } else if (!rightBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state);
            }
        } else {
            if (getDecoratedLeft(topView) > 0 && !leftBoundReached) {
                fillGrid(DIRECTION_START, recycler, state);
            } else if (!leftBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state);
            }
        }
        return -delta;
    }


    @Override
    public boolean canScrollVertically() {
        return true;
    }


    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        if (getChildCount() == 0) {
            return 0;
        }
        final View topView = getChildAt(0);
        final View bottomView = getChildAt(getChildCount() - 1);

        int viewSpan = getDecoratedBottom(bottomView) - getDecoratedTop(topView);
        if (viewSpan < getVerticalSpace()) {
            return 0;
        }

        int delta;
        int maxRowCount = getTotalRowCount();
        boolean topBoundReached = getFirstVisibleRow() == 0;
        boolean bottomBoundReached = getLastVisibleRow() >= maxRowCount;
        if (dy > 0) {
            if (bottomBoundReached) {
                int bottomOffset;
                if (rowOfIndex(getChildCount() - 1) >= (maxRowCount - 1)) {
                    bottomOffset = getVerticalSpace() - getDecoratedBottom(bottomView)
                            + getPaddingBottom();
                } else {
                    bottomOffset = getVerticalSpace() - (getDecoratedBottom(bottomView)
                            + mDecoratedChildHeight) + getPaddingBottom();
                }

                delta = Math.max(-dy, bottomOffset);
            } else {
                delta = -dy;
            }
        } else {
            if (topBoundReached) {
                int topOffset = -getDecoratedTop(topView) + getPaddingTop();
                delta = Math.min(-dy, topOffset);
            } else {
                delta = -dy;
            }
        }

        offsetChildrenVertical(delta);

        if (dy > 0) {
            if (getDecoratedBottom(topView) < 0 && !bottomBoundReached) {
                fillGrid(DIRECTION_DOWN, recycler, state);
            } else if (!bottomBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state);
            }
        } else {
            if (getDecoratedTop(topView) > 0 && !topBoundReached) {
                fillGrid(DIRECTION_UP, recycler, state);
            } else if (!topBoundReached) {
                fillGrid(DIRECTION_NONE, recycler, state);
            }
        }
        return -delta;
    }


    @Override
    public View findViewByPosition(int position) {
        for (int i = 0; i < getChildCount(); i++) {
            if (positionOfIndex(i) == position) {
                return getChildAt(i);
            }
        }
        return null;
    }


    @Override
    public RecyclerView.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
        return new LayoutParams(c, attrs);
    }

    @Override
    public RecyclerView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return new LayoutParams((ViewGroup.MarginLayoutParams) lp);
        } else {
            return new LayoutParams(lp);
        }
    }

    @Override
    public boolean checkLayoutParams(RecyclerView.LayoutParams lp) {
        return lp instanceof LayoutParams;
    }

    public static class LayoutParams extends RecyclerView.LayoutParams {

        public int row;
        public int column;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(RecyclerView.LayoutParams source) {
            super(source);
        }
    }

    private void layoutAppearingViews(RecyclerView.Recycler recycler, View referenceView, int referencePosition, int extraCount, int offset) {
        if (extraCount < 1) {
            return;
        }
        for (int extra = 1; extra <= extraCount; extra++) {
            final int extraPosition = referencePosition + extra;
            if (extraPosition < 0 || extraPosition >= getItemCount()) {
                continue;
            }
            View appearing = recycler.getViewForPosition(extraPosition);
            addView(appearing);
            final int newRow = getGlobalRowOfPosition(extraPosition + offset);
            final int rowDelta = newRow - getGlobalRowOfPosition(referencePosition + offset);
            final int newCol = getGlobalColumnOfPosition(extraPosition + offset);
            final int colDelta = newCol - getGlobalColumnOfPosition(referencePosition + offset);
            layoutTempChildView(appearing, rowDelta, colDelta, referenceView);
        }
    }

    private void layoutDisappearingView(View disappearingChild) {
        addDisappearingView(disappearingChild);
        final LayoutParams lp = (LayoutParams) disappearingChild.getLayoutParams();
        final int newRow = getGlobalRowOfPosition(lp.getViewAdapterPosition());
        final int rowDelta = newRow - lp.row;
        final int newCol = getGlobalColumnOfPosition(lp.getViewAdapterPosition());
        final int colDelta = newCol - lp.column;
        layoutTempChildView(disappearingChild, rowDelta, colDelta, disappearingChild);
    }

    private void layoutTempChildView(View child, int rowDelta, int colDelta, View referenceView) {
        int layoutTop = getDecoratedTop(referenceView) + rowDelta * mDecoratedChildHeight;
        int layoutLeft = getDecoratedLeft(referenceView) + colDelta * mDecoratedChildWidth;
        measureChildWithMargins(child, 0, 0);
        layoutDecorated(child, layoutLeft, layoutTop,
                layoutLeft + mDecoratedChildWidth,
                layoutTop + mDecoratedChildHeight);
    }

    private int getGlobalColumnOfPosition(int position) {
        return position % mTotalColumnCount;
    }

    private int getGlobalRowOfPosition(int position) {
        return position / mTotalColumnCount;
    }


    private int positionOfIndex(int childIndex) {
        int row = childIndex / mVisibleColumnCount;
        int column = childIndex % mVisibleColumnCount;
        return mFirstVisiblePosition + (row * getTotalColumnCount()) + column;
    }

    private int rowOfIndex(int childIndex) {
        int position = positionOfIndex(childIndex);
        return position / getTotalColumnCount();
    }

    private int getFirstVisibleColumn() {
        return (mFirstVisiblePosition % getTotalColumnCount());
    }

    private int getLastVisibleColumn() {
        return getFirstVisibleColumn() + mVisibleColumnCount;
    }

    private int getFirstVisibleRow() {
        return (mFirstVisiblePosition / getTotalColumnCount());
    }

    private int getLastVisibleRow() {
        return getFirstVisibleRow() + mVisibleRowCount;
    }

    private int getVisibleChildCount() {
        return mVisibleColumnCount * mVisibleRowCount;
    }

    private int getTotalColumnCount() {
        if (getItemCount() < mTotalColumnCount) {
            return getItemCount();
        }
        return mTotalColumnCount;
    }

    private int getTotalRowCount() {
        if (getItemCount() == 0 || mTotalColumnCount == 0) {
            return 0;
        }
        int maxRow = getItemCount() / mTotalColumnCount;
        if (getItemCount() % mTotalColumnCount != 0) {
            maxRow++;
        }
        return maxRow;
    }

    private int getHorizontalSpace() {
        return getWidth() - getPaddingRight() - getPaddingLeft();
    }

    private int getVerticalSpace() {
        return getHeight() - getPaddingBottom() - getPaddingTop();
    }
}
