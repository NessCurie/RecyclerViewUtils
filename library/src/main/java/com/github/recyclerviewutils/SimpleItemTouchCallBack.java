package com.github.recyclerviewutils;

import android.graphics.Canvas;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * 一个简单的对Android的ItemTouchHelper.Callback进行实现了的类
 * <p>
 * 当是LinearLayoutManager时,支持侧拉删除和长按条目拖动
 * 当是GridLayoutManager和StaggeredGridLayoutManager时,只有长按条目拖动,如果需要支持Grid的侧拉删除
 * 需要在{@link SimpleItemTouchCallBack#getMovementFlags(RecyclerView, RecyclerView.ViewHolder)}
 * 中进行修改
 */
public class SimpleItemTouchCallBack extends ItemTouchHelper.Callback {

    private BaseAdapter adapter;

    private boolean longPressDrag;
    private boolean itemViewSwipe;
    private SwipyRefreshLayout swipyrefreshlayout;

    /**
     * 默认开启长按拖动和侧拉删除
     *
     * @param adapter recyclerView适配器
     */
    public SimpleItemTouchCallBack(BaseAdapter adapter) {
        this.adapter = adapter;
        longPressDrag = true;
        itemViewSwipe = true;
    }

    /**
     * 根据传来的参数决定是否开启长按拖动和侧拉删除
     *
     * @param adapter           recyclerView适配器
     * @param openLongPressDrag 开启长按拖动
     * @param openSwipeDelete   开启侧拉删除
     */
    public SimpleItemTouchCallBack(BaseAdapter adapter, boolean openLongPressDrag,
                                   boolean openSwipeDelete, SwipyRefreshLayout swipyrefreshlayout) {
        this.adapter = adapter;
        longPressDrag = openLongPressDrag;
        itemViewSwipe = openSwipeDelete;
        this.swipyrefreshlayout = swipyrefreshlayout;
    }

    /**
     * 指定可以支持的拖放和滑动的方向
     * makeMovementFlags(int, int)来构造返回的flag
     */
    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager
                || layoutManager instanceof StaggeredGridLayoutManager) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN |
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    /**
     * 支持长按RecyclerView item进入拖动操作
     * 也可以调用ItemTouchHelper.startDrag(RecyclerView.ViewHolder) 方法来开始一个拖动
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return longPressDrag;
    }

    /**
     * 在view任意位置触摸事件发生时启用滑动操作
     * 主动调用ItemTouchHelper.startSwipe(RecyclerView.ViewHolder) 来开始滑动操作
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return itemViewSwipe;
    }

    /**
     * 当在移动
     */
    @Override
    public boolean onMove(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * 当侧拉时
     */
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        adapter.onItemDismiss(viewHolder.getAdapterPosition());
    }

    /**
     * 拖动或侧拉效果开始
     */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder,
                                  int actionState) {
        if (actionState != ItemTouchHelper.ACTION_STATE_IDLE) {
            if (swipyrefreshlayout != null) {
                swipyrefreshlayout.dragStart();
            }
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    /**
     * 拖动或侧拉完成
     */
    @Override
    public void clearView(RecyclerView recyclerView,
                          RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        if (swipyrefreshlayout != null) {
            swipyrefreshlayout.dragEnd();
        }
    }

    /**
     * 动画效果,目前只写了删除的淡入淡出效果,可以重写此方法,
     * 当所有动画都设置了的情况下不需要加super,否则需要加上,不然会有一些动作没有动画效果
     */
    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            float width = (float) viewHolder.itemView.getWidth();
            float alpha = 1.0f - Math.abs(dX) / width;
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }
}