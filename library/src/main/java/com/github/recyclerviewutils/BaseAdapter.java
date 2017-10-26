package com.github.recyclerviewutils;

import android.content.Context;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * 继承于recyclerView.Adapter,封装了viewHolder支持多type的item类型的BaseAdapter
 * <p>
 * 多type需要实现IType接口 {@link IType},调用{@link BaseAdapter#addType(IType)}
 * 或{@link BaseAdapter#addType(int, IType)}添加Type
 * <p>
 * 添加item的{@link BaseAdapter#setOnItemClickListener(OnItemClickListener)} 点击事件
 * 和{@link BaseAdapter#setOnItemLongClickListener(OnItemLongClickListener)} 长按事件
 * <p>
 * 可以使用 {@link BaseAdapter#openItemTouch(RecyclerView, boolean, boolean, SwipyRefreshLayout)}
 * 开启条目从侧拉删除和长按拖动,如果设置了长按拖动长按点击事件不会失效,会和长按拖动事件同时触发
 * 如果不需要长按拖动事件,
 * 可以在 {@link #openItemTouch(RecyclerView, boolean, boolean, SwipyRefreshLayout)}第二个参数传为false
 * 如果需要设置item中某个元素为触发拖动的元素,在{@link IType#setData(MViewHolder, Object, int)}中使用holder
 * 中的{@link MViewHolder#setDragListener(int)} 设置
 */
public class BaseAdapter<T> extends RecyclerView.Adapter<MViewHolder> implements ItemTouchDataCallBack {

    private Context context;
    private List<T> list;
    private SparseArrayCompat<IType<T>> types = new SparseArrayCompat<>();

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;
    private ItemTouchHelper helper;

    /**
     * 构造方法
     *
     * @param context 上下文
     * @param list    数据的集合
     */
    public BaseAdapter(Context context, List<T> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (!isMoreOneType()) {
            return 0;
        } else {
            for (int i = 0; i < types.size(); i++) {
                if (types.valueAt(i).isThisTypeItem(list.get(position), position)) {
                    return types.keyAt(i);
                }
            }
            return 0;
        }
    }

    /**
     * 创建viewHolder对象,会使用{@link IType#getLayoutId()}返回的Layout创建
     */
    @Override
    public MViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MViewHolder holder = MViewHolder.createHolder(context, types.get(viewType).getLayoutId(), helper);
        onViewHolderCreated(holder);
        return holder;
    }

    /**
     * 绑定数据
     * 会根据{@link IType}中的 {@link IType#isThisTypeItem(Object, int)}判断是否是对应的item
     * 使用{@link IType}中的{@link IType#setData(MViewHolder, Object, int)}为item设置数据
     */
    @Override
    public void onBindViewHolder(MViewHolder holder, int position) {
        T t = list.get(position);
        for (int i = 0; i < types.size(); i++) {
            IType<T> type = types.valueAt(i);
            if (type.isThisTypeItem(t, position)) {
                type.setData(holder, t, position);
                setListener(holder, types.indexOfValue(type), position);
                return;
            }
        }
    }

    /**
     * 可以重写此方法,ViewHolderCreated创建完毕之后调用
     *
     * @param holder 对应的viewHolder
     */
    public void onViewHolderCreated(MViewHolder holder) {

    }

    /**
     * 可重写此方法,设置哪些可以点击哪些不可以
     */
    public boolean isClickEnabled(int viewType) {
        return true;
    }

    /**
     * 设置点击事件
     *
     * @param viewHolder viewHolder
     * @param viewType   view的类型
     */
    private void setListener(final MViewHolder viewHolder, int viewType, final int position) {
        if (!isClickEnabled(viewType)) return;
        viewHolder.getItemView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(v, viewHolder, position);
                }
            }
        });

        viewHolder.getItemView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return onItemLongClickListener != null
                        && onItemLongClickListener.onItemLongClick(v, viewHolder, position);
            }
        });
    }

    /**
     * 获取集合
     */
    public List<T> getLists() {
        return list;
    }

    /**
     * 添加item的type
     *
     * @param type item的type {@link IType}
     */
    public void addType(IType<T> type) {
        if (type != null) {
            types.put(types.size(), type);
        }
    }

    /**
     * 在集合添加指定type和type的实现,当指定type已存在时不会添加
     *
     * @param type  指定的type
     * @param iType item的type实现  {@link IType}
     */
    public void addType(int type, IType<T> iType) {
        if (types.get(type) == null) {
            types.put(type, iType);
        }
    }

    /**
     * item是否是多type类型
     */
    private boolean isMoreOneType() {
        return types.size() > 1;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, RecyclerView.ViewHolder holder, int position);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position);
    }

    /**
     * 设置长按事件
     *
     * @param onItemLongClickListener imp{@link OnItemLongClickListener}
     */
    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    /**
     * 设置点击事件
     *
     * @param onItemClickListener imp{@link OnItemClickListener}
     */
    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    /**
     * 根据参数开启recycler的侧拉删除和长按拖动
     *
     * @param recyclerView      adapter的RecyclerView的对象
     * @param openLongPressDrag 是否开启长按拖动
     * @param openSwipeDelete   是否开启侧滑删除
     * @param refreshLayout     因为在最顶部和最底部时拖动会和下拉刷新以及加载更多冲突,所以当有
     *                          添加{@link SwipyRefreshLayout}的时候需要和下拉刷新的控件关联,
     *                          如果没有添加下拉刷新和上拉的传null即可
     */
    public void openItemTouch(RecyclerView recyclerView, boolean openLongPressDrag,
                              boolean openSwipeDelete, SwipyRefreshLayout refreshLayout) {
        helper = new ItemTouchHelper(
                new SimpleItemTouchCallBack(this, openLongPressDrag, openSwipeDelete, refreshLayout));
        helper.attachToRecyclerView(recyclerView);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(list, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemDismiss(int position) {
        list.remove(position);
        notifyItemRemoved(position);
    }
}
