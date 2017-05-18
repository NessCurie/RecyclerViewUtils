package com.github.recyclerview;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.github.recyclerviewutils.BaseAdapter;
import com.github.recyclerviewutils.MViewHolder;
import com.github.recyclerviewutils.SimpleAdapter;
import com.github.recyclerviewutils.SimpleDecoration;
import com.github.recyclerviewutils.SwipyRefreshLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * 单type的示例
 */
public class SimpleActivity extends AppCompatActivity implements
        SwipyRefreshLayout.OnRefreshListener, BaseAdapter.OnItemLongClickListener,
        BaseAdapter.OnItemClickListener {

    private List<String> list;
    private SwipyRefreshLayout swipyrefreshlayout;
    private RecyclerView recyclerview;
    private SimpleAdapter<String> simpleAdapter;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple);
        list = new ArrayList<>();
        for (int i = 'A'; i < 'z'; i++) {
            list.add("" + (char) i);
        }

        //获取下拉刷新和上拉刷新的控件并设置各种颜色
        swipyrefreshlayout = (SwipyRefreshLayout) findViewById(R.id.swipyrefreshlayout);
        // 设置是下拉刷新还是上拉加载更多还是都有,也可以在属性中设置,此处已在属性中设置
        //swipyrefreshlayout.setRefreshMode(SwipyRefreshLayout.BOTH);
        swipyrefreshlayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        swipyrefreshlayout.setProgressBackgroundColorSchemeResource(android.R.color.darker_gray);
        swipyrefreshlayout.setOnRefreshListener(this);

        //获取recyclerView,设置各种属性
        recyclerview = (RecyclerView) findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
                //如果是GridLayout在布局文件修改一下宽的设置
                //new GridLayoutManager(this,3,GridLayoutManager.VERTICAL,false)
                //new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        );
        recyclerview.addItemDecoration(new SimpleDecoration(this));  //设置分割线
        //将指定控件设置为触发拖动
        simpleAdapter = new SimpleAdapter<String>(this, R.layout.item_simple, list) {
            @Override
            public void setItemData(final MViewHolder holder, String s, int position) {
                holder.setText(R.id.tv_center, s)
                        .setDragListener(R.id.iv_hand);           //将指定控件设置为触发拖动
            }
        };

        //开启拖动和侧拉删除相关,关联recyclerView,开启长按拖动,开启侧拉删除,关联下拉刷新的控件
        simpleAdapter.openItemTouch(recyclerview, true, true, swipyrefreshlayout);
        simpleAdapter.setOnItemLongClickListener(this);         //长按事件
        simpleAdapter.setOnItemClickListener(this);             //点击事件

        recyclerview.setAdapter(simpleAdapter);
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
                        simpleAdapter.notifyDataSetChanged();
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
                        simpleAdapter.notifyDataSetChanged();
                        recyclerview.scrollToPosition(lastListSize);
                        swipyrefreshlayout.setRefreshing(false);
                    }
                }, 2000);
                break;
        }
    }

    @Override
    public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
        showToast("onItemLongClick" + position);
        return false;
    }

    @Override
    public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
        showToast("onItemClick" + position);
    }

    private Toast toast;

    private void showToast(String s) {
        if (toast == null) {
            toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
        }
        toast.setText(s);
        toast.show();
    }
}
