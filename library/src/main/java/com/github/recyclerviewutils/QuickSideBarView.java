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
    private int textX;
    private float letterMargin;
    private float distance;
    private Paint paint = new Paint();
    private Rect rect = new Rect();
    private OnQuickSideBarTouchListener listener;

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
        letters = context.getResources().getStringArray(R.array.quickSideBarLetters);
        textColor = Color.BLACK;
        textColorChoose = Color.BLACK;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.QuickSideBarView);
            textColor = a.getColor(R.styleable.QuickSideBarView_sidebarTextColor, textColor);
            textColorChoose = a.getColor(R.styleable.QuickSideBarView_sidebarChooseTextColor, textColorChoose);
            textSize = a.getDimension(R.styleable.QuickSideBarView_sidebarTextSize, textSize);
            textSizeChoose = a.getDimension(R.styleable.QuickSideBarView_sidebarChooseTextSize, textSizeChoose);
            a.recycle();
        }

        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(textColor);
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        textX = getMeasuredWidth() >> 1;
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        paint.setColor(textColor);
        int lettersHeight = 0;
        for (String s : letters) {
            rect.setEmpty();
            paint.getTextBounds(s, 0, s.length(), rect);
            lettersHeight += rect.height();
        }
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
            currentY += Math.abs(rect.centerY()) + distance;
            canvas.drawText(s, textX, currentY, paint);
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
        float getHeight = 0;
        int newChoose = 0;
        float letterCenterRawY = 0;
        for (int i = 0; i < letters.length; i++) {
            String s = letters[i];
            rect.setEmpty();
            paint.getTextBounds(s, 0, s.length(), rect);
            float letterHeight = rect.height() + distance;
            letterCenterRawY = getHeight + letterHeight / 2f;
            getHeight += letterHeight;
            if (getHeight > y) {
                newChoose = i;
                break;
            }
        }
        letterCenterRawY += yMargin;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setChoose(newChoose);
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

    public void setChoose(int choose) {
        if (this.choose != choose) {
            this.choose = choose;
            invalidate();
        }
    }

    public void setLetters(String[] letters) {
        this.letters = new String[letters.length];
        System.arraycopy(letters, 0, this.letters, 0, letters.length);
        invalidate();
    }
}

