package com.github.recyclerviewutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.util.Linkify;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

/**
 * ViewHolder,都已经封装在{@link BaseAdapter} 中,不需要再实现,提供获取对应控件和根据id设置数据的方式
 */
public class MViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    /**
     * item的View对象
     */
    private View itemView;
    /**
     * 为了不多次查找id的集合,查找一次后就存入集合,下次直接从集合中获取
     */
    private SparseArray<View> views;

    private ItemTouchHelper helper;

    private MViewHolder(Context context, View itemView, ItemTouchHelper helper) {
        super(itemView);
        this.context = context;
        this.itemView = itemView;
        this.helper = helper;
        views = new SparseArray<>();
    }

    public static MViewHolder createHolder(Context context, int layoutId, ViewGroup parent, ItemTouchHelper helper) {
        return new MViewHolder(context, LayoutInflater.from(context).inflate(layoutId, parent, false), helper);
    }

    /**
     * 获取item的view对象
     */
    public View getItemView() {
        return itemView;
    }

    /**
     * 通过viewId获取控件
     */
    public <T extends View> T getView(int id) {
        T view = (T) views.get(id);
        if (view == null) {
            view = itemView.findViewById(id);
            views.put(id, view);
        }
        return view;
    }

    /**
     * 将指定id的控件设置为触发拖动元素
     *
     * @param id 控件
     */
    public MViewHolder setDragListener(int id) {
        View view = getView(id);
        if (view != null) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        if (helper != null) {
                            helper.startDrag(MViewHolder.this);
                        }
                    }
                    return false;
                }
            });
        }
        return this;
    }

    public MViewHolder setText(int viewId, String text) {
        TextView tv = getView(viewId);
        if (tv != null) tv.setText(text);
        return this;
    }

    public MViewHolder setText(int viewId, int resid) {
        TextView tv = getView(viewId);
        if (tv != null) tv.setText(resid);
        return this;
    }

    public MViewHolder setImageResource(int viewId, int resId) {
        ImageView iv = getView(viewId);
        if (iv != null) iv.setImageResource(resId);
        return this;
    }

    public MViewHolder setImageBitmap(int viewId, Bitmap bitmap) {
        ImageView iv = getView(viewId);
        if (iv != null) iv.setImageBitmap(bitmap);
        return this;
    }

    public MViewHolder setImageDrawable(int viewId, Drawable drawable) {
        ImageView iv = getView(viewId);
        if (iv != null) iv.setImageDrawable(drawable);
        return this;
    }

    public MViewHolder setBackground(int viewId, Drawable drawable) {
        View iv = getView(viewId);
        if (iv != null) iv.setBackgroundDrawable(drawable);
        return this;
    }

    public MViewHolder setBackgroundColor(int viewId, int color) {
        View iv = getView(viewId);
        if (iv != null) iv.setBackgroundColor(color);
        return this;
    }

    public MViewHolder setBackgroundResource(int viewId, int backgroundRes) {
        View view = getView(viewId);
        if (view != null) view.setBackgroundResource(backgroundRes);
        return this;
    }

    public MViewHolder setTextColor(int viewId, int textColor) {
        TextView tv = getView(viewId);
        if (tv != null) tv.setTextColor(textColor);
        return this;
    }

    public MViewHolder setTextColorRes(int viewId, int textColorRes) {
        TextView tv = getView(viewId);
        if (tv != null) tv.setTextColor(context.getResources().getColor(textColorRes));
        return this;
    }

    public MViewHolder linkify(int viewId) {
        TextView tv = getView(viewId);
        if (tv != null) Linkify.addLinks(tv, Linkify.ALL);
        return this;
    }

    public MViewHolder setAlpha(int viewId, float value) {
        View view = getView(viewId);
        if (view != null) view.setAlpha(value);
        return this;
    }

    public MViewHolder setVisible(int viewId, boolean visible) {
        View view = getView(viewId);
        if (view != null) view.setVisibility(visible ? View.VISIBLE : View.GONE);
        return this;
    }

    public MViewHolder setTypeface(Typeface typeface, int... viewIds) {
        for (int viewId : viewIds) {
            TextView view = getView(viewId);
            if (view != null) {
                view.setTypeface(typeface);
                view.setPaintFlags(view.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG);
            }
        }
        return this;
    }

    public MViewHolder setProgress(int viewId, int progress) {
        ProgressBar pb = getView(viewId);
        if (pb != null) pb.setProgress(progress);
        return this;
    }

    public MViewHolder setProgress(int viewId, int progress, int max) {
        ProgressBar pb = getView(viewId);
        if (pb != null) {
            pb.setMax(max);
            pb.setProgress(progress);
        }
        return this;
    }

    public MViewHolder setMax(int viewId, int max) {
        ProgressBar pb = getView(viewId);
        if (pb != null) pb.setMax(max);
        return this;
    }

    public MViewHolder setRating(int viewId, float rating) {
        RatingBar rb = getView(viewId);
        if (rb != null) rb.setRating(rating);
        return this;
    }

    public MViewHolder setRating(int viewId, float rating, int max) {
        RatingBar rb = getView(viewId);
        if (rb != null) {
            rb.setMax(max);
            rb.setRating(rating);
        }
        return this;
    }

    public MViewHolder setTag(int viewId, Object tag) {
        View view = getView(viewId);
        if (view != null) view.setTag(tag);
        return this;
    }

    public MViewHolder setTag(int viewId, int key, Object tag) {
        View view = getView(viewId);
        if (view != null) view.setTag(key, tag);
        return this;
    }

    public MViewHolder setChecked(int viewId, boolean checked) {
        Checkable view = getView(viewId);
        if (view != null) view.setChecked(checked);
        return this;
    }

    public MViewHolder setOnClickListener(int viewId, View.OnClickListener listener) {
        View view = getView(viewId);
        if (view != null) view.setOnClickListener(listener);
        return this;
    }

    public MViewHolder setOnTouchListener(int viewId, View.OnTouchListener listener) {
        View view = getView(viewId);
        if (view != null) view.setOnTouchListener(listener);
        return this;
    }

    public MViewHolder setOnLongClickListener(int viewId, View.OnLongClickListener listener) {
        View view = getView(viewId);
        if (view != null) view.setOnLongClickListener(listener);
        return this;
    }
}
