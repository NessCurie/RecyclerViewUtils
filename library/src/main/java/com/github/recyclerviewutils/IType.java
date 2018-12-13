package com.github.recyclerviewutils;

import android.support.annotation.NonNull;

/**
 * 代表多type类型,实现此接口并添加适配器中
 *
 * @param <T> 为recyclerView的item赋值的model
 */
public interface IType<T> {

    /**
     * 获取item的layout的id
     *
     * @return item layout的id
     */
    int getLayoutId();

    /**
     * 提供判断是否是此type类型的item
     *
     * @param item     item的model的对象
     * @param position item的索引
     * @return 是否是该类型的item
     */
    boolean isThisTypeItem(@NonNull T item, int position);

    /**
     * 为item设置数据的方式
     *
     * @param holder   ViewHolder对象,可以调用{@link MViewHolder#getView(int)} 根据ID获取view对象
     *                 也可以直接使用holder中提供的方法进行赋值
     * @param t        传入适配器集合中对应索引的model对象
     * @param position item的索引
     */
    void setData(@NonNull MViewHolder holder, @NonNull T t, int position);
}
