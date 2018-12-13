package com.github.recyclerviewutils;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * 简单的单type的item的recyclerView的适配器,只需要实现一个为item赋值的方法集合简单实现一个recyclerView
 *
 * @param <T> 传入适配器集合中的model
 */
public abstract class SimpleAdapter<T> extends BaseAdapter<T> {

    /**
     * 创建简单的recyclerView适配器
     *
     * @param context 上下文
     * @param id      item的layout的id
     * @param list    集合
     */
    public SimpleAdapter(final Context context, final int id, List<T> list) {
        super(context, list);
        addType(new IType<T>() {
            @Override
            public int getLayoutId() {
                return id;
            }

            @Override
            public boolean isThisTypeItem(@NonNull T item, int position) {
                return true;
            }

            @Override
            public void setData(@NonNull MViewHolder holder, @NonNull T t, int position) {
                setItemData(holder, t, position);
            }
        });
    }

    /**
     * 为item设置数据的方式
     *
     * @param holder   ViewHolder对象,可以调用{@link MViewHolder#getView(int)} 根据ID获取view对象
     *                 也可以直接使用holder中提供的方法进行赋值
     * @param t        传入适配器集合中对应索引的model对象
     * @param position item的索引
     */
    public abstract void setItemData(@NonNull MViewHolder holder, @NonNull T t, int position);

}
