package org.nesscurie.recyclerviewutils;

/**
 * 用于对侧滑删除和移动时的数据的处理
 */
public interface ItemTouchDataCallBack {

    /**
     * 当是进行移动的时候的回调
     *
     * @param fromPosition 开始索引
     * @param toPosition   到达索引
     */
    void onItemMove(int fromPosition, int toPosition);

    /**
     * 当侧滑删除的时候的回调
     *
     * @param position 删除的item的索引
     */
    void onItemDismiss(int position);
}

