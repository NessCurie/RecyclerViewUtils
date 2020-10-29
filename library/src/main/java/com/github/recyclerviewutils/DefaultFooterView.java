package com.github.recyclerviewutils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class DefaultFooterView extends RelativeLayout implements HFRefreshLayout.LoaderDecor {

    private int state = STATE_NORMAL;
    private View progressBar;
    private TextView tvHint;

    private String stateNormalHint;
    private String stateReadyHint;
    private String stateRefreshingHint;
    private String stateSuccessHint;
    private String stateHasLoadAllHint;

    public DefaultFooterView(Context context) {
        this(context, null);
    }

    public DefaultFooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(getContext(), R.layout.view_footer, this);
        progressBar = findViewById(R.id.pb_footer_progress);
        tvHint = findViewById(R.id.tv_hint);
        final float scale = context.getResources().getDisplayMetrics().density;
        int padding = (int) (10 / scale + 0.5f);
        setPadding(0, padding, 0, padding);

        stateNormalHint = context.getString(R.string.loader_load_more);
        stateReadyHint = context.getString(R.string.loader_load_ready);
        stateRefreshingHint = context.getString(R.string.loader_loading);
        stateSuccessHint = context.getString(R.string.loader_load_success);
        stateHasLoadAllHint = context.getString(R.string.loader_no_more);
    }

    @Override
    public void refreshScrollRate(int y) {
    }

    @Override
    public void onStateChange(int state) {
        if (state == this.state) return;
        if (state == STATE_READY) {
            progressBar.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(stateReadyHint);
        } else if (state == STATE_REFRESHING) {
            tvHint.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            tvHint.setText(stateRefreshingHint);
        } else if (state == STATE_NORMAL) {
            progressBar.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(stateNormalHint);
        } else if (state == STATE_SUCCESS) {
            progressBar.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(stateSuccessHint);
        } else if (state == STATE_HAS_LOAD_ALL) {
            progressBar.setVisibility(View.INVISIBLE);
            tvHint.setVisibility(View.VISIBLE);
            tvHint.setText(stateHasLoadAllHint);
        } else {
            tvHint.setVisibility(View.GONE);
        }
        this.state = state;
    }

    @Override
    public void setStateNormalHint(String s) {
        stateNormalHint = s;
        if (state == STATE_NORMAL) tvHint.setText(stateNormalHint);
    }

    @Override
    public void setStateReadyHint(String s) {
        stateReadyHint = s;
        if (state == STATE_READY) tvHint.setText(stateReadyHint);
    }

    @Override
    public void setStateRefreshingHint(String s) {
        stateRefreshingHint = s;
        if (state == STATE_REFRESHING) tvHint.setText(stateRefreshingHint);
    }

    @Override
    public void setStateSuccessHint(String s) {
        stateSuccessHint = s;
        if (state == STATE_SUCCESS) tvHint.setText(stateSuccessHint);
    }

    @Override
    public void setStateHasLoadAll(String s) {
        stateHasLoadAllHint = s;
        if (state == STATE_HAS_LOAD_ALL) tvHint.setText(stateHasLoadAllHint);
    }
}