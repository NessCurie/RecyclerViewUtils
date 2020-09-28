package com.github.recyclerviewutils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class DefaultHeaderView extends RelativeLayout implements HFRefreshLayout.LoaderDecor {

    private static final int ROTATE_ANIM_DURATION = 180;

    private ImageView ivIcon;
    private int state = STATE_NORMAL;

    private Animation rotateUpAnim;
    private Animation rotateDownAnim;  //这种动画在view被移除之后会停止
    private ObjectAnimator rotateAnim;
    private TextView tvHint;

    private String stateNormalHint;
    private String stateReadyHint;
    private String stateRefreshingHint;
    private String stateSuccessHint;

    public DefaultHeaderView(Context context) {
        this(context, null);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultHeaderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    private void initView(Context context) {
        View.inflate(context, R.layout.view_header, this);
        ivIcon = (ImageView) findViewById(R.id.iv_icon);
        tvHint = (TextView) findViewById(R.id.tv_hint);

        rotateUpAnim = new RotateAnimation(0.0f, -180.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        rotateUpAnim.setFillAfter(true);

        rotateDownAnim = new RotateAnimation(-180.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        rotateDownAnim.setFillAfter(true);

        rotateAnim = ObjectAnimator.ofFloat(ivIcon, "rotation", 0f, 360f);
        rotateAnim.setDuration(3000);
        rotateAnim.setRepeatCount(ObjectAnimator.INFINITE);
        rotateAnim.setRepeatMode(ObjectAnimator.RESTART);
        rotateAnim.setInterpolator(new LinearInterpolator());

        stateNormalHint = context.getString(R.string.loader_pull_load);
        stateReadyHint = context.getString(R.string.loader_pull_ready);
        stateRefreshingHint = context.getString(R.string.loader_loading);
        stateSuccessHint = context.getString(R.string.loader_success);
    }

    @Override
    public void refreshScrollRate(int y) {
    }

    public void onStateChange(int state) {
        if (state == this.state) return;
        if (state == STATE_REFRESHING) {
            ivIcon.clearAnimation();
            ivIcon.setBackgroundResource(R.drawable.ic_on_refresh);
            rotateAnim.start();
        } else if (state == STATE_SUCCESS) {
            ivIcon.setBackgroundResource(R.drawable.ic_on_refresh);
            rotateAnim.pause();
        } else {
            ivIcon.setRotation(0);
            ivIcon.setBackgroundResource(R.drawable.ic_pull_refresh);
        }
        switch (state) {
            case STATE_NORMAL:
                if (this.state == STATE_READY) {
                    ivIcon.startAnimation(rotateDownAnim);
                } else if (this.state == STATE_REFRESHING) {
                    ivIcon.clearAnimation();
                }
                tvHint.setText(stateNormalHint);
                break;
            case STATE_READY:
                if (this.state != STATE_READY) {
                    ivIcon.clearAnimation();
                    ivIcon.startAnimation(rotateUpAnim);
                    tvHint.setText(stateReadyHint);
                }
                break;
            case STATE_REFRESHING:
                tvHint.setText(stateRefreshingHint);
                break;
            case STATE_SUCCESS:
                tvHint.setText(stateSuccessHint);
                break;
            default:
        }
        this.state = state;
    }

    @Override
    public void setStateNormalHint(String s) {
        stateNormalHint = s;
    }

    @Override
    public void setStateReadyHint(String s) {
        stateReadyHint = s;
    }

    @Override
    public void setStateRefreshingHint(String s) {
        stateRefreshingHint = s;
    }

    @Override
    public void setStateSuccessHint(String s) {
        stateSuccessHint = s;
    }

    @Override
    public void setStateHasLoadAll(String s) {
    }
}
