package com.github.recyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.github.recyclerviewutils.BaseAdapter;
import com.github.recyclerviewutils.IType;
import com.github.recyclerviewutils.MViewHolder;
import com.github.recyclerviewutils.SimpleDecoration;
import com.github.recyclerviewutils.SwipyRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 多type的示例
 */
public class MultipleActivity extends AppCompatActivity implements
        SwipyRefreshLayout.OnRefreshListener, BaseAdapter.OnItemLongClickListener,
        BaseAdapter.OnItemClickListener {

    private List<String> list;
    private SwipyRefreshLayout swipyrefreshlayout;
    private RecyclerView recyclerview;
    private Handler handler = new Handler();
    private BaseAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple);
        list = new ArrayList<>();
        list.add("" + 1);
        for (int i = 'A'; i < 'F'; i++) {
            list.add("" + (char) i);
        }
        list.add("" + 2);
        for (int i = 'F'; i < 'K'; i++) {
            list.add("" + (char) i);
        }
        list.add("" + 3);
        for (int i = 'K'; i < 'S'; i++) {
            list.add("" + (char) i);
        }
        list.add("" + 4);
        for (int i = 'S'; i < 'Z'; i++) {
            list.add("" + (char) i);
        }
        list.add("" + 5);
        for (int i = 'a'; i < 'f'; i++) {
            list.add("" + (char) i);
        }

        //获取下拉刷新和上拉刷新的控件并设置各种颜色
        swipyrefreshlayout = findViewById(R.id.swipyrefreshlayout);
        // 设置是下拉刷新还是上拉加载更多还是都有,也可以在属性中设置,此处已在属性中设置
        //swipyrefreshlayout.setRefreshMode(SwipyRefreshLayout.BOTH);
        swipyrefreshlayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipyrefreshlayout.setProgressBackgroundColorSchemeResource(android.R.color.darker_gray);
        swipyrefreshlayout.setOnRefreshListener(this);

        //获取recyclerView,设置各种属性e
        recyclerview = findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                //如果是GridLayout在布局文件修改一下宽的设置
                //new GridLayoutManager(this,3,GridLayoutManager.VERTICAL,false)
                //new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        );
        recyclerview.addItemDecoration(new SimpleDecoration(this));  //设置分割线
        //adapter = new BaseAdapter<>(this, list);  //不需要设置哪些是否可以点击可以直接new
        adapter = new BaseAdapter<String>(this, list) {
            @Override
            public boolean isClickEnabled(int viewType) {    //这里将数字type不可点击
                return viewType != 1;
            }
        };
        adapter.addType(new IType<String>() {
            @Override
            public int getLayoutId() {
                return R.layout.item_type1;
            }

            @Override
            public boolean isThisTypeItem(@NonNull String item, int position) {//不是数字的使用该type,是灰色背景
                try {
                    Integer.parseInt(item);
                    return false;
                } catch (NumberFormatException e) {
                    return true;
                }
            }

            @Override
            public void setData(@NonNull MViewHolder holder, @NonNull String s, int position) {
                holder.setText(R.id.tv_center, s)
                        .setDragListener(R.id.iv_hand);
            }
        });
        adapter.addType(new IType<String>() {
            @Override
            public int getLayoutId() {
                return R.layout.item_type2;
            }

            @Override
            public boolean isThisTypeItem(@NonNull String item, int position) {  //是数字的使用该type,是红色背景
                try {
                    Integer.parseInt(item);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            @Override
            public void setData(@NonNull MViewHolder holder, @NonNull String s, int position) {
                holder.setText(R.id.tv_center, s);
            }
        });
        adapter.openItemTouch(recyclerview, false, true, swipyrefreshlayout);

        adapter.setOnItemLongClickListener(this);
        adapter.setOnItemClickListener(this);

        recyclerview.setAdapter(adapter);
    }

    @Override
    public void onRefresh(int direction) {
        switch (direction) {
            case SwipyRefreshLayout.TOP:
                list.clear();
                for (int i = 'A'; i < 'z'; i++) {
                    list.add("" + (char) i);
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        swipyrefreshlayout.setRefreshing(false);
                    }
                }, 2000);
                break;
            case SwipyRefreshLayout.BOTTOM:
                final int lastListSize = list.size();
                for (int i = 1; i < 9; i++) {
                    list.add("" + i);
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                        recyclerview.scrollToPosition(lastListSize);
                        swipyrefreshlayout.setRefreshing(false);
                    }
                }, 2000);
                break;
        }
    }

    private Toast toast;

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        }
        toast.setText(s);
        toast.show();
    }

    @Override
    public void onItemClick(View view, MViewHolder holder, int position) {
        showToast("onItemClick" + position);

    }

    @Override
    public boolean onItemLongClick(View view, MViewHolder holder, int position) {
        showToast("onItemLongClick" + position);
        return false;
    }
}
