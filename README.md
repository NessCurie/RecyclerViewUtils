## RecyclerViewUtils
1. 快速完成单类型item的Adapter;
2. 便利完成多type类型item的Adapter;
3. RecyclerView分割线的实现,支持GridLayoutManager的横竖都显示;
4. 使用Google官方ItemTouchHelper完成侧拉删除和条目拖动和指定控件触发拖动;
5. 封装item的长按和点击事件;
6. 修改SwipeRefreshLayout同时支持下拉刷新和上拉加载更多,兼容条目拖动.

For more information please see [RecyclerView总结与多功能便捷Adapter的封装](https://nesscurie.github.io/2017/03/13/3.RecyclerView%E6%80%BB%E7%BB%93%E4%B8%8EAdapter%E7%9A%84%E5%B0%81%E8%A3%85/)

## Download
Add this in your root build.gradle file (not your module build.gradle file):
<pre><code>allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
</code></pre>

Then, add the library to your module build.gradle
<pre><code>dependencies {
    compile 'com.github.NessCurie:RecyclerViewUtils:latest.release.here'
}
</code></pre>

such as release is v1.0

you can use:
<pre><code>dependencies {
    compile 'com.github.NessCurie:RecyclerViewUtils:v1.0'
}
</code></pre>

## Usage

### 单类型item:
创建SimpleAdapter对象,设置给RecyclerView即可:
<pre><code>/**
 * 创建简单的recyclerView适配器
 *
 * @param context 上下文
 * @param id      item的layout的id
 * @param list    集合
 */
public SimpleAdapter(final Context context, final int id, List<T> list)
</code></pre>

比如:
<pre><code>SimpleAdapter simpleAdapter = new SimpleAdapter<String>(this, R.layout.item_simple, list) {
    @Override
    public void setItemData(final MViewHolder holder, String s, int position) {
        holder.setText(R.id.tv_center, s);
    }
};
</code></pre>

### 多类型item
#### 1. 如果没有其他特殊需求可以直接new出BaseAdapter的对象.如果有其他需求可以继承重写部分方法.
<pre><code>可以重写其中的<code>onViewHolderCreated()</code>方法在创建完ViewHolder后做一些操作;
可以重写其中的<code>isClickEnabled()</code>设置哪些type不可点击.
</code></pre>

#### 2. 向adapter中添加IType的实现类
在<code>getLayoutId()</code>返回对应itemType的布局的id
在<code>isThisTypeItem()</code>提供判断是否是对应itemType的方法.
在<code>setData()</code>对item中对应id的控件设置对应数据.可以使用MViewHolder中提供的一系列方法进行链式调用

比如:集合中存的为String类型,分别有数字和其他字符类型,将数字类型显示为一种item,其他字符显示为一种item.

<pre><code>adapter = new BaseAdapter<String>(this, list) {
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
    public boolean isThisTypeItem(String item, int position) {//不是数字的使用该type,是灰色背景
        try {
            Integer.parseInt(item);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    @Override
    public void setData(MViewHolder holder, String s, int position) {
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
    public boolean isThisTypeItem(String item, int position) {  //是数字的使用该type,是红色背景
        try {
            Integer.parseInt(item);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void setData(MViewHolder holder, String s, int position) {
        holder.setText(R.id.tv_center, s);
    }
});
</code></pre>

### 如果需要设置分割线,可以使用SimpleDecoration.
<pre><code>/**
 * 使用系统默认分割线作为recyclerView的分割线
 */
public SimpleDecoration(Context context)
/**
 * 使用指定Drawable作为recyclerView的分割线
 */
public SimpleDecoration(Context context, Drawable drawable)
/**
 * 使用指定Drawable的id作为recyclerView的分割线
 */
public SimpleDecoration(Context context, int drawID)
</code></pre>

### 如果需要开启条目拖动和条目侧拉删除,调用adapter中的openItemTouch方法
<pre><code>/**
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
                          boolean openSwipeDelete, SwipyRefreshLayout refreshLayout)
</code></pre>

比如:
<pre><code>simpleAdapter.openItemTouch(recyclerview, true, true, swipyrefreshlayout);</code></pre>

### 如果要设置指定控件触发条目拖动
在<code>SimpleAdapter的setItemData()</code>方法或<code>IType实现类的setData()</code>方法中,使用holder调用<code>setDragListener(int id)</code>即可,需要adapter调用上面的<code>openItemTouch()</code>方法,否则不会生效.

比如:<code>holder.setDragListener(R.id.iv_hand);</code>

### 如果需要设置item的点击和长按事件
点击和长按事件封装在BaseAdapter中,如果同时设置了长按拖动和长按点击事件,会同时触发.

<pre><code>adapter.setOnItemLongClickListener(this);
adapter.setOnItemClickListener(this);

@Override
public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
    return false;
}

@Override
public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
}
</code></pre>

### 如果要添加下拉刷新或上拉加载更多
在布局文件中使用<code>com.github.recyclerviewutils.SwipyRefreshLayout</code>节点包裹RecyclerView,在xml中设置对应的<code>refresh_mode</code>或在代码中使用<code>setRefreshMode(int)</code>设置.

<pre><code>&lt;com.github.recyclerviewutils.SwipyRefreshLayout
    android:id="@+id/swipyrefreshlayout"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:refresh_mode="both">

    &lt;android.support.v7.widget.RecyclerView
        android:id="@+id/recyclerview"
        android:layout_width="match_parent"
        android:layout_height="match_parent">

    &lt;/android.support.v7.widget.RecyclerView>

&lt;/com.github.recyclerviewutils.SwipyRefreshLayout>
</code></pre>

可以使用setColorSchemeResources()设置刷新控件中间的进度条的颜色,支持多个颜色,会依次出现
<pre><code>swipyrefreshlayout.setColorSchemeResources(android.R.color.holo_blue_bright,
        android.R.color.holo_green_light, android.R.color.holo_orange_light,
        android.R.color.holo_red_light);
</code></pre>

可以使用setProgressBackgroundColor()设置刷新控件的背景.
<pre><code>swipyrefreshlayout.setProgressBackgroundColor(android.R.color.darker_gray);</code></pre>

设置刷新监听的回调,根据传来的参数判断是下拉刷新还是上拉加载更多
<pre><code>swipyrefreshlayout.setOnRefreshListener(this);

@Override
public void onRefresh(int direction) {
    switch (direction) {
        case SwipyRefreshLayout.TOP:
            break;
        case SwipyRefreshLayout.BOTTOM:         
            break;
    }
}
</code></pre>

刷新完毕后调用<code>swipyrefreshlayout.setRefreshing(false);</code>隐藏刷新控件