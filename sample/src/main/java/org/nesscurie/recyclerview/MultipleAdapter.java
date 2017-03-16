package org.nesscurie.recyclerview;

import android.content.Context;

import org.nesscurie.recyclerviewutils.BaseAdapter;

import java.util.List;

/**
 * 模块:
 * 用途:
 * 作者: Created by NessCurie
 * 日期: 2017/3/16
 */
public class MultipleAdapter extends BaseAdapter<String> {
    /**
     * 构造方法
     *
     * @param context 上下文
     * @param list    数据的集合
     */
    public MultipleAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public boolean isEnabled(int viewType) {
        return viewType != 1;
    }
}
