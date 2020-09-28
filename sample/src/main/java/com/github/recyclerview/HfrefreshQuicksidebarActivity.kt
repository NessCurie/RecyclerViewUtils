package com.github.recyclerview

import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.github.recyclerviewutils.MViewHolder
import com.github.recyclerviewutils.SimpleAdapter
import kotlinx.android.synthetic.main.activity_hfrefresh_quicksidebar.*

/**
 * 模块:
 * 用途:
 * 作者: Created by NessCure
 * 日期: 2020/9/27
 */
class HfrefreshQuicksidebarActivity : AppCompatActivity() {

    private val hanziToPinyin: HanziToPinyin by lazy { HanziToPinyin.getInstance() }
    private val handler = Handler()

    data class PhoneBook(val displayName: String, val number: String) {
        override fun hashCode(): Int {
            return super.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (other is PhoneBook) {
                return this.displayName == other.displayName && this.number == other.number
            }
            return false
        }
    }

    private val list = ArrayList<PhoneBook>()
    private val layoutManager: LinearLayoutManager by lazy { LinearLayoutManager(this) }
    private val adapter: SimpleAdapter<PhoneBook> by lazy {
        object : SimpleAdapter<PhoneBook>(this, R.layout.item_phone_book, list) {
            override fun setItemData(holder: MViewHolder, vCard: PhoneBook, position: Int) {
                holder.setText(R.id.tvName, vCard.displayName)
                holder.setText(R.id.tvNumber, vCard.number)  //service已过滤
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hfrefresh_quicksidebar)
        val listEmpty = View.inflate(this, R.layout.layout_list_empty, null)
        hfRlPhoneBook.run {
            emptyView = listEmpty
            setRefreshOnStateNormalHint(context.getString(R.string.pull_to_sync))
            setRefreshOnStateReadyHint(context.getString(R.string.ready_to_sync))
            setRefreshOnStateRefreshingHint(context.getString(R.string.syncing))
            setRefreshOnStateSuccessHint(context.getString(R.string.sync_success))
        }
        rvPhoneBook.run {
            layoutManager = this@HfrefreshQuicksidebarActivity.layoutManager
            adapter = this@HfrefreshQuicksidebarActivity.adapter
        }

        hfRlPhoneBook.setOnRefreshListener {
            handler.postDelayed({
                showList(arrayListOf(PhoneBook("张三", "123455"), PhoneBook("张三", "123455"),
                        PhoneBook("张三", "123455"), PhoneBook("张三", "123455"),
                        PhoneBook("张三", "123455"), PhoneBook("张三", "123455")))
            }, 2000)
        }

        hfRlPhoneBook.setOnLoadMoreListener {
            handler.postDelayed({
                this.list.addAll(arrayListOf(PhoneBook("张三", "123455"), PhoneBook("张三", "123455"),
                        PhoneBook("张三", "123455"), PhoneBook("张三", "123455"),
                        PhoneBook("张三", "123455"), PhoneBook("张三", "123455")))
                if (hfRlPhoneBook.isLoadingMore) {
                    hfRlPhoneBook.onLoadMoreFinished(1000)
                }
                adapter.notifyDataSetChanged()
            }, 2000)
        }

        adapter.setOnItemClickListener { _, _, position ->
            //toast(list[position].number)
        }

        quickSideBarView.setOnQuickSideBarTouchListener { needShowTips, letter, _, y ->
            flSideBarTips.visibility = if (needShowTips) View.VISIBLE else View.INVISIBLE
            if (needShowTips) {
                tvTips.text = letter
                val tvTipsParams = tvTips.layoutParams as FrameLayout.LayoutParams
                tvTipsParams.topMargin = (y - tvTips.height / 2f).toInt()
                tvTips.layoutParams = tvTipsParams

                val chose = letter[0].toUpperCase()
                for (i in 0 until list.size) {
                    val currentChar0 = hanziToPinyin.transliterate(list[i].displayName)[0].toUpperCase()
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

    private fun showList(list: List<PhoneBook>) {
        this.list.clear()
        Log.e("wtf", "list = ${list.size}")
        if (list.isNotEmpty()) {
            hfRlPhoneBook.hideEmptyView()
            this.list.addAll(list)
            quickSideBarView.visibility = View.VISIBLE
        } else {
            hfRlPhoneBook.showEmptyView()
            quickSideBarView.visibility = View.GONE
        }
        if (hfRlPhoneBook.isRefreshing) {
            hfRlPhoneBook.onRefreshFinished(1000)
        }
        adapter.notifyDataSetChanged()
    }

}