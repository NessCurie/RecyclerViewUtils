package com.github.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.github.recyclerviewutils.HFRefreshLayout

/**
 * 模块:
 * 用途:
 * 作者: Created by NessCure
 * 日期: 2020/10/9
 */
class HeadViewCustom @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : RelativeLayout(context, attrs), HFRefreshLayout.LoaderDecor {

    override fun refreshScrollRate(y: Int) {
    }

    override fun onStateChange(state: Int) {
    }

    override fun setStateNormalHint(s: String?) {
    }

    override fun setStateReadyHint(s: String?) {
    }

    override fun setStateRefreshingHint(s: String?) {
    }

    override fun setStateSuccessHint(s: String?) {
    }

    override fun setStateHasLoadAll(s: String?) {
    }
}