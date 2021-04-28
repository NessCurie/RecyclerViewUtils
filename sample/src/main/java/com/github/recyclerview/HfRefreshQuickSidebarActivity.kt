package com.github.recyclerview

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import com.github.recyclerviewutils.MViewHolder
import com.github.recyclerviewutils.SimpleAdapter
import com.github.recyclerviewutils.SimpleDecoration
import kotlinx.android.synthetic.main.activity_hfrefresh_quicksidebar.*
import java.text.Collator
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

class HfRefreshQuickSidebarActivity : AppCompatActivity() {

    private val hanziToPinyin: HanziToPinyin by lazy { HanziToPinyin.getInstance() }
    private val collator: Collator by lazy { Collator.getInstance(Locale.ENGLISH) }
    private val handler = Handler()

    private val comparator = Comparator<String> { s1, s2 ->
        if (s1 != null && s2 != null) {
            val vCard1Pinyin = hanziToPinyin.transliterate(s1)
            val vCard2Pinyin = hanziToPinyin.transliterate(s2)
            val vCard1Point0 = vCard1Pinyin[0].toUpperCase()
            val vCard2Point0 = vCard2Pinyin[0].toUpperCase()
            if (vCard1Point0 in 'A'..'Z') {
                if (vCard2Point0 in 'A'..'Z') {
                    collator.compare(vCard1Pinyin, vCard2Pinyin)
                } else {
                    -1
                }
            } else {
                if (vCard2Point0 in 'A'..'Z') {
                    1
                } else {
                    collator.compare(vCard1Pinyin, vCard2Pinyin)
                }
            }
        } else {
            0
        }
    }

    private val list = ArrayList<String>()
    private val layoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }
    private val adapter: SimpleAdapter<String> by lazy {
        object : SimpleAdapter<String>(this, R.layout.item_quick_sidebar, list) {
            override fun setItemData(holder: MViewHolder, s: String, position: Int) {
                holder.setText(R.id.tvName, s)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hfrefresh_quicksidebar)
        val listEmptyView = View.inflate(this, R.layout.layout_list_empty, null)
        hfRefreshLayout.run {
            emptyView = listEmptyView
            setRefreshOnStateReadyHint(context.getString(R.string.ready_to_sync))
            setRefreshOnStateRefreshingHint(context.getString(R.string.syncing))
            setRefreshOnStateSuccessHint(context.getString(R.string.sync_success))
        }
        rvList.run {
            layoutManager = this@HfRefreshQuickSidebarActivity.layoutManager
            adapter = this@HfRefreshQuickSidebarActivity.adapter
            addItemDecoration(SimpleDecoration(this@HfRefreshQuickSidebarActivity)) //设置分割线
        }

        hfRefreshLayout.setOnRefreshListener {
            handler.postDelayed({
                showList(arrayListOf("A", "D", "C", "C", "Z", "W", "C", "C"))
            }, 2000)
        }
        hfRefreshLayout.setOnLoadMoreListener {
            handler.postDelayed({
                this.list.addAll(arrayListOf("朱", "秦", "尤", "许", "何", "吕", "施", "张", "孔",
                        "曹", "严", "华", "金", "魏", "陶", "姜"))
                list.sortWith(comparator)
                hfRefreshLayout.hideEmptyView()
                if (hfRefreshLayout.isLoadingMore) {
                    hfRefreshLayout.onLoadMoreFinished(1000)
                }
                adapter.notifyDataSetChanged()
            }, 2000)
        }

        val tv = TypedValue()
        var actionBarHeight = 0
        if (this.theme.resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, this.resources.displayMetrics)
        }


        quickSideBarView.setOnQuickSideBarTouchListener { needShowTips, letter, _, y ->
            flSideBarTips.visibility = if (needShowTips) View.VISIBLE else View.INVISIBLE
            if (needShowTips) {
                tvTips.text = letter
                val tvTipsParams = tvTips.layoutParams as FrameLayout.LayoutParams
                tvTipsParams.topMargin = (y - statusBarHeight - actionBarHeight - tvTips.height / 2f).toInt()
                tvTips.layoutParams = tvTipsParams

                val chose = letter[0].toUpperCase()
                for (i in 0 until list.size) {
                    val currentChar0 = hanziToPinyin.transliterate(list[i])[0].toUpperCase()
                    if (chose == '#') {
                        if (currentChar0 !in 'A'..'Z') {
                            layoutManager.scrollToPositionWithOffset(i, 0)
                            break
                        }
                    } else {
                        if (currentChar0 == chose) {
                            layoutManager.scrollToPositionWithOffset(i, 0)
                            break
                        }
                    }
                }
            }
        }
    }

    private var statusBarHeight = 0

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            val rect = Rect()
            window.decorView.getWindowVisibleDisplayFrame(rect)
            statusBarHeight = rect.top

            hfRefreshLayout.showEmptyView()
            hfRefreshLayout.showRefreshWithoutInvoke()
            handler.postDelayed({
                showList(arrayListOf("赵", "钱", "孙", "李", "周", "吴", "郑", "王", "冯", "陈", "褚",
                        "卫", "蒋", "沈", "韩", "杨"))
            }, 2000)
        }
    }

    private fun showList(list: ArrayList<String>) {
        this.list.clear()
        if (list.isNotEmpty()) {
            list.sortWith(comparator)
            hfRefreshLayout.hideEmptyView()
            this.list.addAll(list)
            quickSideBarView.visibility = View.VISIBLE
        } else {
            hfRefreshLayout.showEmptyView()
            quickSideBarView.visibility = View.GONE
        }
        if (hfRefreshLayout.isRefreshing) {
            hfRefreshLayout.onRefreshFinished(1000)
        }
        adapter.notifyDataSetChanged()
    }
}