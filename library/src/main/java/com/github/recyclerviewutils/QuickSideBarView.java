package com.github.recyclerviewutils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;


/**
 * 置于页面end并设置marginEnd之后触摸位置会向左偏移,只能不设置marginEnd拓宽触摸范围
 */
public class QuickSideBarView extends View {

    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

    public static final int CENTER = 0;
    public static final int TOP = 1;
    public static final int BOTTOM = 2;
    public static final int START = 3;
    public static final int END = 4;

    public static final int STYLE_NORMAL = 0;
    public static final int STYLE_BOLD = 1;
    public static final int STYLE_ITALIC = 2;

    public interface OnQuickSideBarTouchListener {
        /**
         * @param needShowTips 是否需要显示tipsView
         * @param rowMargin    横向时是 x  竖向时是y
         */
        void onLetterStateChanged(boolean needShowTips, String letter, int position, float rowMargin);
    }

    private ArrayList<String> letters = new ArrayList<>();
    private ArrayList<String> hasLetters = new ArrayList<>();
    private int choose = -1;
    private float textSize = 18;
    private float textSizeChoose = 22;
    private float textSizeHas = 22;
    private int textColor;
    private int textColorChoose;
    private int textColorHas;
    private float letterMargin;
    private Paint paint = new Paint();
    private Rect rect = new Rect();
    private OnQuickSideBarTouchListener listener;
    private float maxTextCenterX;
    private float maxTextCenterY;
    private int orientation = VERTICAL;
    private int gravity = CENTER;
    private int textStyle = STYLE_NORMAL;
    private int textChooseStyle = STYLE_NORMAL;
    private int textHasStyle = STYLE_NORMAL;

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
        letters.addAll(Arrays.asList(context.getResources().getStringArray(R.array.quickSideBarLetters)));
        textColor = Color.BLACK;
        textColorChoose = Color.BLACK;
        textColorHas = Color.BLACK;
        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.QuickSideBarView);
            textColor = a.getColor(R.styleable.QuickSideBarView_sidebarTextColor, textColor);
            textColorChoose = a.getColor(R.styleable.QuickSideBarView_sidebarChooseTextColor, textColorChoose);
            textColorHas = a.getColor(R.styleable.QuickSideBarView_sidebarHasTextColor, textColorHas);
            textSize = a.getDimension(R.styleable.QuickSideBarView_sidebarTextSize, textSize);
            textSizeChoose = a.getDimension(R.styleable.QuickSideBarView_sidebarChooseTextSize, textSizeChoose);
            textSizeHas = a.getDimension(R.styleable.QuickSideBarView_sidebarHasTextSize, textSizeHas);
            orientation = a.getInt(R.styleable.QuickSideBarView_sidebarOrientation, VERTICAL);
            gravity = a.getInt(R.styleable.QuickSideBarView_sidebarTextGravity, CENTER);
            textStyle = a.getInt(R.styleable.QuickSideBarView_sidebarTextStyle, STYLE_NORMAL);
            textChooseStyle = a.getInt(R.styleable.QuickSideBarView_sidebarTextStyle, STYLE_NORMAL);
            textHasStyle = a.getInt(R.styleable.QuickSideBarView_sidebarTextStyle, STYLE_NORMAL);
            a.recycle();
        }
    }

    public void setOrientation(int orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;
            requestLayout();
        }
    }

    public void setGravity(int gravity) {
        if (this.gravity != gravity) {
            if (gravity == START || gravity == END) {
                if (orientation == VERTICAL) {
                    this.gravity = gravity;
                    invalidate();
                }
            } else if (gravity == TOP || gravity == BOTTOM) {
                if (orientation == HORIZONTAL) {
                    this.gravity = gravity;
                    invalidate();
                }
            } else {
                this.gravity = gravity;
                invalidate();
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        paint.setAntiAlias(true);

        paint.setTextSize(Math.max(textSizeChoose, Math.max(textSizeHas, textSize)));
        paint.setTypeface(Typeface.create(Typeface.DEFAULT,
                Math.max(textStyle, Math.max(textChooseStyle, textHasStyle))));
        int lettersHeight = 0;
        float rectMaxHeight = 0f;
        float rectMaxWith = 0f;
        for (String s : letters) {
            rect.setEmpty();

            paint.getTextBounds(s, 0, s.length(), rect);
            if (rectMaxWith < rect.width()) {
                rectMaxWith = rect.width();
            }
            lettersHeight += rect.height();

            if (rectMaxHeight < rect.height()) {
                rectMaxHeight = rect.height();
            }
        }
        maxTextCenterX = rectMaxWith / 2;
        maxTextCenterY = rectMaxHeight / 2;
        if (orientation == VERTICAL) {
            letterMargin = ((float) (getMeasuredHeight() - lettersHeight)) / letters.size();
        } else {    //横向时每个字母宽绘制一样
            letterMargin = ((float) getMeasuredWidth() - rectMaxWith * letters.size()) / letters.size();
        }
    }

    protected void onDraw(Canvas canvas) {
        float current = letterMargin / 2;
        if (orientation == VERTICAL) {
            for (int i = 0; i < letters.size(); i++) {
                String s = letters.get(i);
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

                current += drawRectY;
                canvas.drawText(s, maxTextCenterX, current, paint);

                current += (rect.height() - drawRectY);
                current += letterMargin;
                paint.reset();
            }
        } else {
            for (int i = 0; i < letters.size(); i++) {
                String s = letters.get(i);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setAntiAlias(true);
                if (i == choose) {
                    paint.setColor(textColorChoose);
                    paint.setTextSize(textSizeChoose);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, textChooseStyle));
                } else if (hasLetters.contains(s)) {
                    paint.setColor(textColorHas);
                    paint.setTextSize(textSizeHas);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, textHasStyle));
                } else {
                    paint.setColor(textColor);
                    paint.setTextSize(textSize);
                    paint.setTypeface(Typeface.create(Typeface.DEFAULT, textStyle));
                }
                rect.setEmpty();
                paint.getTextBounds(s, 0, s.length(), rect);

                Paint.FontMetrics fontMetrics = paint.getFontMetrics();
                float distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom;
                float drawRectY;
                if (gravity == CENTER) {
                    drawRectY = getMeasuredHeight() / 2f + distance;
                } else if (gravity == BOTTOM) {
                    drawRectY = maxTextCenterY + (getMeasuredHeight() - maxTextCenterY * 2) + distance;
                } else {
                    drawRectY = maxTextCenterY + distance;
                }

                current += maxTextCenterX;
                canvas.drawText(s, current, drawRectY, paint);

                current += maxTextCenterX;
                current += letterMargin;
                paint.reset();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getAction();
        float current = 0;
        int newChoose = 0;
        float letterCenterRawMargin = 0;

        if (orientation == VERTICAL) {
            float y = event.getY();
            float rawY = event.getRawY();
            float yMargin = rawY - y;

            paint.setTextAlign(Paint.Align.CENTER);
            paint.setAntiAlias(true);
            paint.setTextSize(textSizeChoose);
            for (int i = 0; i < letters.size(); i++) {
                String s = letters.get(i);
                rect.setEmpty();
                paint.getTextBounds(s, 0, s.length(), rect);
                letterCenterRawMargin = current + letterMargin / 2f + Math.abs(rect.centerY());
                current += (rect.height() + letterMargin);
                if (current > y) {
                    newChoose = i;
                    break;
                }
            }
            letterCenterRawMargin += yMargin;
        } else {
            float x = event.getX();
            float rawX = event.getRawX();
            float xMargin = rawX - x;

            for (int i = 0; i < letters.size(); i++) {
                letterCenterRawMargin = current + letterMargin / 2f + maxTextCenterX;
                current += letterMargin + maxTextCenterX * 2;
                if (current > x) {
                    newChoose = i;
                    break;
                }
            }
            letterCenterRawMargin += xMargin;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                choose = newChoose;
                invalidate();
                if (listener != null) {
                    listener.onLetterStateChanged(true, letters.get(newChoose), choose, letterCenterRawMargin);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() < 0 || event.getY() < 0 || event.getX() > getWidth() || event.getY() > getHeight()) {
                    if (listener != null) {
                        listener.onLetterStateChanged(false, letters.get(newChoose), choose, letterCenterRawMargin);
                    }
                } else {
                    if (choose != newChoose) {
                        choose = newChoose;
                        invalidate();
                        if (listener != null) {
                            listener.onLetterStateChanged(true, letters.get(newChoose), choose, letterCenterRawMargin);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                choose = -1;
                invalidate();
                if (listener != null) {
                    listener.onLetterStateChanged(false, letters.get(newChoose), choose, letterCenterRawMargin);
                }
                break;
        }

        return true;
    }

    public void setOnQuickSideBarTouchListener(OnQuickSideBarTouchListener listener) {
        this.listener = listener;
    }

    public void setLetters(ArrayList<String> letters) {
        this.letters.clear();
        this.letters.addAll(letters);
        invalidate();
    }

    public void setHasLetters(ArrayList<String> hasLetters) {
        this.hasLetters.clear();
        this.hasLetters.addAll(hasLetters);
        invalidate();
    }

    public void setChoose(String choose) {
        this.choose = letters.indexOf(choose);
        invalidate();
    }
}

