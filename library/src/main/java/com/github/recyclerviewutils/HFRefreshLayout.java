package com.github.recyclerviewutils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.NestedScrollingChildHelper;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.ViewCompat;


/**
 * 使用头尾布局的下拉刷新和加载更多
 */
@SuppressWarnings("unused")
public class HFRefreshLayout extends ViewGroup implements NestedScrollingChild, NestedScrollingParent {

    private static final int INVALID_POINTER_ID = -1;
    private static final int DROP_CIRCLE_ANIMATOR_DURATION = 200;

    private final int[] parentOffsetInWindow = new int[2];
    private final int[] parentScrollConsumed = new int[2];
    private NestedScrollingParentHelper nestedScrollingParentHelper;
    private NestedScrollingChildHelper nestedScrollingChildHelper;
    private Scroller scroller;
    private boolean nestedScrollInProgress;
    private boolean isRequestedLayout = false;
    private int activePointerId;
    private float firstTouchDownPointY; //第一次点击的位置
    private int totalOffset;            //总偏移值
    private int lastEventOffset;        //最后touch事件偏移值
    private MotionEvent lastMoveEvent;

    public interface LoaderDecor {
        /**
         * 默认状态
         */
        int STATE_NORMAL = 0;
        /**
         * 松开可以刷新或加载更多
         */
        int STATE_READY = 1;
        /**
         * 刷新中 或 加载更多中
         */
        int STATE_REFRESHING = 2;
        /**
         * 刷新成功
         */
        int STATE_SUCCESS = 5;
        /**
         * 已经加载全部,没有更多可以加载
         */
        int STATE_HAS_LOAD_ALL = 4;

        /**
         * 滑动时调用
         *
         * @param y 下拉的距离
         */
        void refreshScrollRate(int y);

        /**
         * 状态改变
         */
        void onStateChange(int state);

        void setStateNormalHint(String s);

        void setStateReadyHint(String s);

        void setStateRefreshingHint(String s);

        void setStateSuccessHint(String s);

        void setStateHasLoadAll(String s);
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private OnRefreshListener onRefreshListener;
    private OnLoadMoreListener onLoadMoreListener;
    protected View onEmptyView;
    protected View contentView;

    protected View headerView;
    private boolean isEnableRefresh = true;
    private boolean isRefreshListenerInvoked = false;
    private boolean onRefreshing;

    protected View footerView;
    private boolean isEnableLoadMore = true;
    private boolean isLoadMoreListenerInvoked = false;
    private boolean isNoMoreLoaded = false;
    private boolean onLoadingMore;
    private boolean attached;

    public HFRefreshLayout(Context context) {
        this(context, null);
    }

    public HFRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HFRefreshLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        scroller = new Scroller(context, new DecelerateInterpolator());

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HFRefreshLayout, defStyle, 0);
        int footerViewResource = R.layout.layout_list_loadmore;
        int headerViewResource = R.layout.layout_list_refresh;
        final int N = a.getIndexCount();
        for (int i = 0; i < N; i++) {
            int attr = a.getIndex(i);
            if (attr == R.styleable.HFRefreshLayout_footerNestLayout) {
                footerViewResource = a.getResourceId(attr, R.layout.layout_list_loadmore);
            } else if (attr == R.styleable.HFRefreshLayout_headerNestLayout) {
                headerViewResource = a.getResourceId(attr, R.layout.layout_list_refresh);
            }
        }
        setFooterView(View.inflate(context, footerViewResource, null));
        setHeaderView(View.inflate(context, headerViewResource, null));
        a.recycle();
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        // if this is a List < L or another view that doesn't support nested
        // scrolling, ignore this request so that the vertical scroll event
        // isn't stolen
        if ((android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP
                && contentView instanceof AbsListView)
                || (contentView != null && !ViewCompat.isNestedScrollingEnabled(contentView))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    @Override
    public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
        // Reset the counter of how much leftover scroll needs to be consumed.
        nestedScrollInProgress = true;
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        // Dispatch up to the nested parent
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
        int oldScrollY = totalOffset;
        if (dy * oldScrollY > 0) {
            final int moveY = Math.abs(dy) > Math.abs(oldScrollY) ? oldScrollY : dy;
            consumed[1] = moveY;
            if (offsetLayout(-moveY)) {
                totalOffset -= moveY;
            }
        }
        // Now let our nested parent consume the leftovers
        final int[] parentConsumed = parentScrollConsumed;
        if (dispatchNestedPreScroll(dx - consumed[0], dy - consumed[1], parentConsumed, null)) {
            consumed[0] += parentConsumed[0];
            consumed[1] += parentConsumed[1];
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return nestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onStopNestedScroll(@NonNull View target) {
        nestedScrollingParentHelper.onStopNestedScroll(target);
        nestedScrollInProgress = false;
        touchUp();
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(@NonNull final View target, int dxConsumed, int dyConsumed,
                               final int dxUnconsumed, final int dyUnconsumed) {

        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, parentOffsetInWindow);

        // This is a bit of a hack. Nested scrolling works from the bottom up, and as we are
        // sometimes between two nested scrolling views, we need a way to be able to know when any
        // nested scrolling parent has stopped handling events. We do that by using the
        // 'offset in window 'functionality to see if we have been moved from the event.
        // This is a decent indication of whether we should take over the event stream or not.
        int dy = dyUnconsumed + parentOffsetInWindow[1];
        dy /= 2;
        if (dy < 0 && isEnableRefresh) {
            if (offsetLayout(-dy)) {
                totalOffset -= dy;
            }
        } else if (dy > 0 && isEnableLoadMore) {
            if (offsetLayout(-dy)) {
                totalOffset -= dy;
            }
        }
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return nestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return nestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean onNestedPreFling(@NonNull View target, float velocityX,
                                    float velocityY) {
        resetView();
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    /**
     * @param headerView 下拉刷新的
     */
    public void setHeaderView(View headerView) {
        if (headerView instanceof LoaderDecor) {
            this.headerView = headerView;
            addView(headerView);
        }
    }

    /**
     * 设置加载更多的view
     *
     * @param footerView 加载更多
     */
    public void setFooterView(View footerView) {
        if (footerView instanceof LoaderDecor) {
            this.footerView = footerView;
            addView(footerView);
        }
    }

    /**
     * Set view show when list is empty
     *
     * @param view Empty View
     */
    public void setEmptyView(View view) {
        if (onEmptyView == view) return;
        if (onEmptyView != null) removeViewInLayout(onEmptyView);
        onEmptyView = view;
        if (onEmptyView.getParent() != null) {
            ((ViewGroup) onEmptyView.getParent()).removeView(onEmptyView);
        }
        onEmptyView.setVisibility(GONE);
        if (contentView != null) {
            super.addView(onEmptyView, 1, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        } else {
            addView(onEmptyView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        }
    }

    public void showEmptyView() {
        onEmptyView.setVisibility(VISIBLE);
    }

    public void hideEmptyView() {
        onEmptyView.setVisibility(GONE);
    }

    /**
     * @return The Empty view
     */
    public View getEmptyView() {
        return onEmptyView;
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        super.addView(child, index, params);
        if (child != headerView && child != footerView && contentView == null)
            contentView = child;
    }

    /**
     * @param view The content view
     */
    public void setContentView(View view) {
        contentView = view;
        addView(view);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth());
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight());
                childState = View.combineMeasuredStates(childState, child.getMeasuredState());
            }
        }
        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(View.resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                View.resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << View.MEASURED_HEIGHT_STATE_SHIFT));

        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = child.getLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth());
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0,
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight());
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, 0,
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    public interface OnInitializedListener {
        void onInitialize();
    }

    private OnInitializedListener onInitializedListener;
    public boolean isInitialized = false;

    public void setOnInitializedListener(OnInitializedListener listener) {
        onInitializedListener = listener;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (onEmptyView != null && onEmptyView.getVisibility() == VISIBLE) {
            onEmptyView.layout(0, 0, onEmptyView.getMeasuredWidth(), onEmptyView.getMeasuredHeight());
        }
        if (contentView != null) {
            contentView.layout(0, 0, contentView.getMeasuredWidth(), contentView.getMeasuredHeight());
        }
        if (headerView != null) {
            headerView.layout(0, 0 - headerView.getMeasuredHeight(), headerView.getMeasuredWidth(), 0);
        }
        if (footerView != null) {
            footerView.layout(0, getMeasuredHeight(), footerView.getMeasuredWidth(),
                    getMeasuredHeight() + footerView.getMeasuredHeight());
        }
        if (!isInitialized) {
            isInitialized = true;
            if (onInitializedListener != null) {
                onInitializedListener.onInitialize();
            }
        }
    }

    @Override
    public void requestLayout() {
        if (isRequestedLayout) return;
        isRequestedLayout = true;
        super.requestLayout();
        isRequestedLayout = false;
    }

    /**
     * enable or disable pull down refresh feature.
     */
    public void setPullRefreshEnable(boolean enable) {
        isEnableRefresh = enable;
    }

    /**
     * enable or disable pull up load more feature.
     */
    public void setPullLoadEnable(boolean enable) {
        isEnableLoadMore = enable;
    }

    public boolean isRefreshEnable() {
        return isEnableRefresh;
    }

    public boolean isLoadMoreEnable() {
        return isEnableLoadMore;
    }

    public boolean isRefreshing() {
        return onRefreshing;
    }

    public boolean isLoadingMore() {
        return onLoadingMore;
    }

    public boolean isLoadAll() {
        return isNoMoreLoaded;
    }

    public void setRefreshing(boolean refresh) {
        if (refresh) {
            invokeRefresh();
        } else {
            isOnRefreshingFinished = false;
            onRefreshing = false;
            isRefreshListenerInvoked = false;
            ((LoaderDecor) headerView).onStateChange(LoaderDecor.STATE_NORMAL);
            resetView();
        }
    }

    public void setLoadingMore(boolean loadMore) {
        if (loadMore) {
            invokeLoadMore();
        } else {
            isOnLoadMoreFinished = false;
            onLoadingMore = false;
            isLoadMoreListenerInvoked = false;
            ((LoaderDecor) footerView).onStateChange(LoaderDecor.STATE_NORMAL);
            resetView();
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        onRefreshListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        onLoadMoreListener = listener;
    }

    // 加载更多判断
    protected boolean offsetLayout(float delY) {
        scrollBy(0, (int) -delY);
        return true;
    }

    @Override
    public void computeScroll() {
        if (!scroller.isFinished() && scroller.computeScrollOffset()) {
            int y = scroller.getCurrY();
            int oldY = getScrollY();
            scrollTo(0, y);
            totalOffset -= y - oldY;
            ViewCompat.postInvalidateOnAnimation(this);
        } else super.computeScroll();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (t > 0 && !isLoadingMore() && isLoadMoreEnable() && !isLoadAll()) {
            if (t > footerView.getMeasuredHeight()) {
                ((LoaderDecor) footerView).onStateChange(LoaderDecor.STATE_READY);
            } else {
                if (!isOnLoadMoreFinished) {
                    ((LoaderDecor) footerView).onStateChange(LoaderDecor.STATE_NORMAL);
                }
            }
        }
        if (t < 0 && !isRefreshing() && isRefreshEnable()) { // 未处于刷新状态，更新箭头
            ((LoaderDecor) headerView).refreshScrollRate(getOffsetY());
            if (getScrollY() < -headerView.getMeasuredHeight()) {
                ((LoaderDecor) headerView).onStateChange(LoaderDecor.STATE_READY);
            } else {
                if (!isOnRefreshingFinished) {
                    ((LoaderDecor) headerView).onStateChange(LoaderDecor.STATE_NORMAL);
                }
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        attached = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        attached = false;
        super.onDetachedFromWindow();
    }

    protected void touchUp() {
        if (getScrollY() < -headerView.getMeasuredHeight()) {// 下拉
            invokeRefresh();
        } else {
            if (footerView.getMeasuredHeight() <= getScrollY() && !isLoadAll()) {
                invokeLoadMore();
            } else {
                if (!isRefreshing() && !isLoadingMore()) {
                    resetView();
                }
            }
        }
    }

    protected void invokeRefresh() {
        ((LoaderDecor) headerView).onStateChange(LoaderDecor.STATE_REFRESHING);
        onRefreshing = true;
        removeCallbacks(refreshRunnable);
        if (attached) {
            postDelayed(refreshRunnable, animation2Y(-headerView.getMeasuredHeight()));
        } else {
            new Handler().postDelayed(refreshRunnable, animation2Y(-headerView.getMeasuredHeight()));
        }
    }

    private Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            if (onRefreshListener != null && !isRefreshListenerInvoked) {
                isRefreshListenerInvoked = true;
                onRefreshListener.onRefresh();
            } else if (!isRefreshListenerInvoked) {
                isRefreshListenerInvoked = true;
            }
        }
    };

    protected void invokeLoadMore() {
        ((LoaderDecor) footerView).onStateChange(LoaderDecor.STATE_REFRESHING);
        onLoadingMore = true;
        removeCallbacks(loadMoreRunnable);
        if (attached) {
            postDelayed(loadMoreRunnable, animation2Y(footerView.getMeasuredHeight()));
        } else {
            new Handler().postDelayed(loadMoreRunnable, animation2Y(footerView.getMeasuredHeight()));
        }
    }

    private Runnable loadMoreRunnable = new Runnable() {
        @Override
        public void run() {
            if (onLoadMoreListener != null && !isLoadMoreListenerInvoked) {
                isLoadMoreListenerInvoked = true;
                onLoadMoreListener.onLoadMore();
            } else {
                isLoadMoreListenerInvoked = true;
            }
        }
    };

    protected int getOffsetY() {
        return totalOffset;
    }

    protected void resetView() {
        animation2Y(0);
    }

    protected int animation2Y(int y) {
        scroller.abortAnimation();
        int duration = Math.abs(y - getScrollY());
        duration = Math.max(DROP_CIRCLE_ANIMATOR_DURATION, duration);
        scroller.startScroll(0, getScrollY(), 0, y - getScrollY(), duration);
        ViewCompat.postInvalidateOnAnimation(this);
        return duration;
    }

    private boolean isOnRefreshingFinished = false;

    public void showRefreshWithoutInvoke() {
        isRefreshListenerInvoked = true;
        invokeRefresh();
    }

    public void onRefreshFinished(long delayMillis) {
        isOnRefreshingFinished = true;
        onRefreshing = false;
        isRefreshListenerInvoked = false;
        ((LoaderDecor) headerView).onStateChange(LoaderDecor.STATE_SUCCESS);
        if (attached) {
            postDelayed(() -> postDelayed(() -> isOnRefreshingFinished = false, animation2Y(0)), delayMillis);
        } else {
            new Handler().postDelayed(() -> postDelayed(() -> isOnRefreshingFinished = false, animation2Y(0)), delayMillis);
        }
    }

    private boolean isOnLoadMoreFinished = false;

    public void onLoadMoreFinished(long delayMillis) {
        isOnLoadMoreFinished = true;
        onLoadingMore = false;
        isLoadMoreListenerInvoked = false;
        isNoMoreLoaded = false;
        ((LoaderDecor) footerView).onStateChange(LoaderDecor.STATE_SUCCESS);
        if (attached) {
            postDelayed(() -> postDelayed(() -> isOnLoadMoreFinished = false, animation2Y(0)), delayMillis);
        } else {
            new Handler().postDelayed(() -> postDelayed(() -> isOnLoadMoreFinished = false, animation2Y(0)), delayMillis);
        }
    }

    public void onLoadMoreNoMore(long delayMillis) {
        isOnLoadMoreFinished = true;
        onLoadingMore = false;
        isLoadMoreListenerInvoked = false;
        isNoMoreLoaded = true;
        ((LoaderDecor) footerView).onStateChange(LoaderDecor.STATE_HAS_LOAD_ALL);
        if (attached) {
            postDelayed(() -> postDelayed(() -> isOnLoadMoreFinished = false, animation2Y(0)), delayMillis);
        } else {
            new Handler().postDelayed(() -> postDelayed(() -> isOnLoadMoreFinished = false, animation2Y(0)), delayMillis);
        }
    }

    /**
     * @see ViewGroup#onInterceptTouchEvent(MotionEvent)
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (!isEnabled() || nestedScrollInProgress) {
            return false;
        }
        if (!isEnableRefresh && !isEnableLoadMore) {
            return false;
        }
        final int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = event.getPointerId(0);
                firstTouchDownPointY = getMotionEventY(event, activePointerId);
                lastEventOffset = (int) firstTouchDownPointY;
                break;

            case MotionEvent.ACTION_MOVE:
                if (activePointerId == INVALID_POINTER_ID) {
                    return false;
                }

                final float currentY = getMotionEventY(event, activePointerId);

                if (currentY == -1) {
                    return false;
                }

                if (firstTouchDownPointY == -1) {
                    firstTouchDownPointY = currentY;
                }
                if (lastEventOffset == -1)
                    lastEventOffset = (int) currentY;

                final float yDiff = currentY - firstTouchDownPointY;

                // State is changed to drag if over slop
                if (Math.abs(yDiff) > ViewConfiguration.get(getContext()).getScaledTouchSlop()) {
                    return shouldIntercept((int) yDiff);
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                activePointerId = INVALID_POINTER_ID;
                break;
        }
        return false;
    }

    private boolean shouldIntercept(int dis) {
        if (dis == 0) {
            return false;
        }
        if (!isEnableRefresh && !isEnableLoadMore) {
            return false;
        }
        dis = -dis;
        return !canScrollVertically(dis) && (dis < 0 ? isEnableRefresh : isEnableLoadMore);
    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    public boolean canScrollVertically(int direction) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (contentView instanceof AdapterView) {
                return canListScroll(direction);
            } else {
                return direction < 0 ? contentView.getScrollY() > 0
                        : contentView.getScrollY() < contentView.getMeasuredHeight();
            }
        } else {
            return contentView.canScrollVertically(direction);
        }
    }

    private boolean canListScroll(int direction) {
        AdapterView<?> absListView = (AdapterView<?>) contentView;
        final int itemCount = absListView.getCount();
        final int childCount = absListView.getChildCount();
        final int firstPosition = absListView.getFirstVisiblePosition();
        final int lastPosition = firstPosition + childCount;

        if (itemCount == 0) {
            return false;
        }
        if (direction > 0) {
            // Are we already showing the entire last item?
            if (lastPosition >= itemCount) {
                final View lastView = absListView.getChildAt(childCount - 1);
                return lastView == null || lastView.getBottom() < contentView.getHeight();
            }
        } else if (direction < 0) {
            // Are we already showing the entire first item?
            if (firstPosition <= 0) {
                final View firstView = absListView.getChildAt(0);
                return firstView == null || firstView.getTop() < 0;
            }
        }
        return true;
    }

    private float getMotionEventY(@NonNull MotionEvent ev, int activePointerId) {
        final int index = ev.findPointerIndex(activePointerId);
        if (index < 0) {
            return -1;
        }
        return ev.getY(index);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!isEnabled() || nestedScrollInProgress) {
            // Fail fast if we're not in a state where a swipe is possible
            return false;
        }
        final int action = event.getActionMasked();
        final int pointerIndex = event.findPointerIndex(activePointerId);
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                releaseEvent();
                lastMoveEvent = MotionEvent.obtain(event);
                return pointerIndex >= 0 && onMoveTouchEvent(event, pointerIndex);
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (activePointerId == INVALID_POINTER_ID) {
                    return false;
                }
                touchUp();
                lastEventOffset = -1;
                firstTouchDownPointY = -1;
                activePointerId = INVALID_POINTER_ID;
                return false;
        }
        return true;
    }

    private void releaseEvent() {
        if (lastMoveEvent != null) {
            lastMoveEvent.recycle();
            lastMoveEvent = null;
        }
    }

    private boolean onMoveTouchEvent(@NonNull MotionEvent event, int pointerIndex) {
        if (IsBeingDropped()) {
            return false;
        }
        final float y = event.getY(pointerIndex);
        float diffY = y - lastEventOffset;
        diffY /= 2;
        if (diffY >= 0 && !isRefreshEnable() || (diffY < 0 && !isLoadMoreEnable()))
            return false;
        lastEventOffset = (int) y;
        if (!shouldIntercept((int) (totalOffset + diffY))) {
            sendUpEvent();
            sendDownEvent();
            return false;
        }
        if (offsetLayout(diffY)) {
            totalOffset += diffY;
            return true;
        }
        return false;
    }

    private void sendDownEvent() {
        final MotionEvent last = lastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_DOWN, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEvent(e);
    }

    private void sendUpEvent() {
        final MotionEvent last = lastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(), last.getEventTime(),
                MotionEvent.ACTION_UP, last.getX(), last.getY(), last.getMetaState());
        dispatchTouchEvent(e);
    }

    protected boolean IsBeingDropped() {
        return false;
    }

    /**
     * @see ViewGroup#generateDefaultLayoutParams()
     */
    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    public void setRefreshOnStateNormalHint(String s) {
        ((LoaderDecor) headerView).setStateNormalHint(s);
    }

    public void setRefreshOnStateReadyHint(String s) {
        ((LoaderDecor) headerView).setStateReadyHint(s);
    }

    public void setRefreshOnStateRefreshingHint(String s) {
        ((LoaderDecor) headerView).setStateRefreshingHint(s);
    }

    public void setRefreshOnStateSuccessHint(String s) {
        ((LoaderDecor) headerView).setStateSuccessHint(s);
    }

    public void setLoadMoreOnStateNormalHint(String s) {
        ((LoaderDecor) footerView).setStateNormalHint(s);
    }

    public void setLoadMoreOnStateReadyHint(String s) {
        ((LoaderDecor) footerView).setStateReadyHint(s);
    }

    public void setLoadMoreOnStateRefreshingHint(String s) {
        ((LoaderDecor) footerView).setStateRefreshingHint(s);
    }

    public void setLoadMoreOnStateSuccessHint(String s) {
        ((LoaderDecor) footerView).setStateSuccessHint(s);
    }

    public void setLoadMoreOnStateHasLoadAll(String s) {
        ((LoaderDecor) footerView).setStateHasLoadAll(s);
    }
}
