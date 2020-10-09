package com.github.recyclerviewutils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * 置于页面end并设置marginEnd之后触摸位置会向左偏移,只能不设置marginEnd拓宽触摸范围
 */
public class QuickSideBarView extends View {

    public interface OnQuickSideBarTouchListener {
        void onLetterStateChanged(boolean needShowTips, String letter, int position, float y);
    }

    private String[] letters;
    private int choose = -1;
    private float textSize = 20;
    private float textSizeChoose = 15;
    private int textColor;
    private int textColorChoose;
    private float letterMargin;
    private Paint paint = new Paint();
    private Rect rect = new Rect();
    private OnQuickSideBarTouchListener listener;
    private float maxTextCenterX;

    public QuickSideBarView(Context context) {
        this(context, null);
    }

    public QuickSideBarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickSideBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        letters = context.getResources().getStringArray(com.github.recyclerviewutils.R.array.quickSideBarLetters);
        textColor = Color.BLACK;
        textColorChoose = Color.BLACK;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, com.github.recyclerviewutils.R.styleable.QuickSideBarView);
            textColor = a.getColor(com.github.recyclerviewutils.R.styleable.QuickSideBarView_sidebarTextColor, textColor);
            textColorChoose = a.getColor(com.github.recyclerviewutils.R.styleable.QuickSideBarView_sidebarChooseTextColor, textColorChoose);
            textSize = a.getDimension(com.github.recyclerviewutils.R.styleable.QuickSideBarView_sidebarTextSize, textSize);
            textSizeChoose = a.getDimension(com.github.recyclerviewutils.R.styleable.QuickSideBarView_sidebarChooseTextSize, textSizeChoose);
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(textColor);
        int lettersHeight = 0;
        float rectMaxWith = 0f;
        for (String s : letters) {
            rect.setEmpty();
            paint.getTextBounds(s, 0, s.length(), rect);
            if (rectMaxWith < rect.width()) {
                rectMaxWith = rect.width();
            }
            lettersHeight += rect.height();
        }
        maxTextCenterX = rectMaxWith / 2;
        letterMargin = ((float) (getMeasuredHeight() - lettersHeight)) / letters.length;
    }

    protected void onDraw(Canvas canvas) {
        float currentY = letterMargin / 2;
        for (int i = 0; i < letters.length; i++) {
            String s = letters[i];
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            if (i == choose) {
                paint.setColor(textColorChoose);
                paint.setTextSize(textSizeChoose);
            } else {
                paint.setColor(textColor);
                paint.setTextSize(textSize);
            }
            rect.setEmpty();
            paint.getTextBounds(s, 0, s.length(), rect);

            Paint.FontMetrics fontMetrics = paint.getFontMetrics();
            float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;

            float drawRectY = Math.abs(rect.centerY()) + distance;

            currentY += drawRectY;
            canvas.drawText(s, maxTextCenterX, currentY, paint);

            currentY += (rect.height() - drawRectY);
            currentY += letterMargin;
            paint.reset();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float y = event.getY();
        float rawY = event.getRawY();
        float yMargin = rawY - y;
        float current = 0;
        int newChoose = 0;
        float letterCenterRawY = 0;

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setTextSize(textSizeChoose);
        for (int i = 0; i < letters.length; i++) {
            String s = letters[i];
            rect.setEmpty();
            paint.getTextBounds(s, 0, s.length(), rect);
            letterCenterRawY = current + letterMargin / 2f + Math.abs(rect.centerY());
            current += (rect.height() + letterMargin);
            if (current > y) {
                newChoose = i;
                break;
            }
        }
        letterCenterRawY += yMargin;

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                choose = newChoose;
                invalidate();
                if (listener != null) {
                    listener.onLetterStateChanged(true, letters[newChoose], choose, letterCenterRawY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() < 0 || event.getY() < 0 || event.getX() > getWidth() || event.getY() > getHeight()) {
                    listener.onLetterStateChanged(false, letters[newChoose], choose, letterCenterRawY);
                } else {
                    if (choose != newChoose) {
                        choose = newChoose;
                        invalidate();
                        if (listener != null) {
                            listener.onLetterStateChanged(true, letters[newChoose], choose, letterCenterRawY);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (listener != null) {
                    listener.onLetterStateChanged(false, letters[newChoose], choose, letterCenterRawY);
                }
                choose = -1;
                invalidate();
                break;
        }
        return true;
    }

    public void setOnQuickSideBarTouchListener(OnQuickSideBarTouchListener listener) {
        this.listener = listener;
    }

    public void setLetters(String[] letters) {
        this.letters = new String[letters.length];
        System.arraycopy(letters, 0, this.letters, 0, letters.length);
        invalidate();
    }
}

