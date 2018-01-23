package com.pngfi.seekbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by pngfi on 2018/1/6.
 */

public class SeekBar extends View implements Thumb.OnProgressChangeListener {


    private static final String TAG="SeekBar";


    private static final int DEFAULT_LINE_HEIGHT = 5; //dp


    private int mOrientation;

    //the orientation value
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;


    //the lesser thumb
    private Thumb mLesserThumb;

    //the larger thumb
    private Thumb mLargerThumb;


    //the height of line
    private float mProgressHeight;

    //the num of step between lesserThumb and largerThumb
    private int gap;

    @ColorInt
    private int mProgressBackground;
    @ColorInt
    private int mProgressColor;

    private int mScaledTouchSlop;

    //the center of y alias
    private int mCenterY;

    private RectF mProgressLine;

    private Paint mPaint;

    private static final float DEFAULT_RADIUS_RATE = 0.5f;

    private OnSeekBarChangeListener onSeekBarChangeListener;


    private Thumb mSlidingThumb;

    private float mLastX;


    public SeekBar(Context context) {
        this(context, null);
    }

    public SeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SeekBar, defStyleAttr, 0);
        mProgressHeight = ta.getDimension(R.styleable.SeekBar_progressHeight, dp2px(DEFAULT_LINE_HEIGHT));
        mProgressBackground = ta.getColor(R.styleable.SeekBar_progressBackground, Color.GRAY);
        mProgressColor = ta.getColor(R.styleable.SeekBar_progressColor, Color.parseColor("#FF4081"));

        Drawable drawable = ta.hasValue(R.styleable.SeekBar_thumb) ? ta.getDrawable(R.styleable.SeekBar_thumb) : getResources().getDrawable(R.drawable.ic_thumb);
        float min = ta.getFloat(R.styleable.SeekBar_min, 0);
        float max = ta.getFloat(R.styleable.SeekBar_max, 100);
        if (max <= min) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        mLesserThumb = new Thumb(getContext(), drawable, min, max);
        mLargerThumb = new Thumb(getContext(), drawable.mutate().getConstantState().newDrawable(), min, max);
        mLesserThumb.setOnProgressChangeListener(this);
        mLargerThumb.setOnProgressChangeListener(this);

        int shadowRaidus = (int) ta.getDimension(R.styleable.SeekBar_shadowRadius, 0);
        int shadowColor = ta.getColor(R.styleable.SeekBar_shadowColor, Color.TRANSPARENT);
        int shadowOffsetX = (int) ta.getDimension(R.styleable.SeekBar_shadowOffsetX, 0);
        int shadowOffsetY = ta.getDimensionPixelOffset(R.styleable.SeekBar_shadowOffsetY, 0);
        mLesserThumb.setShadow(shadowRaidus, shadowOffsetX, shadowOffsetY, shadowColor);
        mLargerThumb.setShadow(shadowRaidus, shadowOffsetX, shadowOffsetY, shadowColor);

        int stepCount = ta.getInt(R.styleable.SeekBar_stepCount, 0);
        mLesserThumb.setStepCount(stepCount);
        mLargerThumb.setStepCount(stepCount);

        gap = ta.getInt(R.styleable.SeekBar_gap, 0);
        ta.recycle();
        mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        initPaint();
    }


    private void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int thumbWidth = mLesserThumb.getWidth();
        int thumbHeight = mLesserThumb.getHeight();
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (widthMode != MeasureSpec.EXACTLY) {
            widthSize = thumbWidth * 2;
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            heightSize = thumbHeight;
        }
        setMeasuredDimension(widthSize, heightSize);

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mCenterY = getHeight() / 2;
        mProgressLine = new RectF(mLesserThumb.getWidth() / 2, mCenterY - mProgressHeight / 2, getWidth() - mLesserThumb.getWidth() / 2, mCenterY + mProgressHeight / 2);

        mLesserThumb.setRect((int) mProgressLine.left - mLesserThumb.getThumbDrawable().getIntrinsicWidth() / 2, mCenterY - mLesserThumb.getThumbDrawable().getIntrinsicHeight() / 2, (int) mProgressLine.right - (int) mProgressLine.left - mLesserThumb.getThumbDrawable().getIntrinsicWidth());
        mLargerThumb.setRect((int) mProgressLine.left + mLesserThumb.getThumbDrawable().getIntrinsicHeight() / 2, mCenterY - mLargerThumb.getThumbDrawable().getIntrinsicHeight() / 2, (int) mProgressLine.right - (int) mProgressLine.left - mLesserThumb.getThumbDrawable().getIntrinsicWidth());
    }


    @Override
    protected void onDraw(Canvas canvas) {
        mPaint.setColor(mProgressBackground);
        // draw progressBackground
        canvas.drawRoundRect(mProgressLine, DEFAULT_RADIUS_RATE * mProgressHeight, DEFAULT_RADIUS_RATE * mProgressHeight, mPaint);

        //draw progress
        mPaint.setColor(mProgressColor);
        canvas.drawRect(mLesserThumb.getCenterPoint().x, mProgressLine.top, mLargerThumb.getCenterPoint().x, mProgressLine.bottom, mPaint);

        // draw thumb
        mLesserThumb.draw(canvas);
        mLargerThumb.draw(canvas);
        Log.i(TAG,"onDraw");
    }


    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener) {
        onSeekBarChangeListener = listener;
    }


    /**
     * called after
     *
     * @param lesserProgress
     * @param largerProgress
     */
    public void setProgress(float lesserProgress, float largerProgress) {
        if (lesserProgress > largerProgress) {
            throw new IllegalArgumentException("lesserProgress must be less than largerProgress");
        }
        mLesserThumb.setProgress(lesserProgress);
        mLargerThumb.setProgress(largerProgress);
        invalidate();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                boolean result = true;
                mLastX = event.getX();
                if (mLesserThumb.contains(event.getX(), event.getY())) {
                    mSlidingThumb = mLesserThumb;
                } else if (mLargerThumb.contains(event.getX(), event.getY())) {
                    mSlidingThumb = mLargerThumb;
                } else {
                    result = false;
                }
                return result;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - mLastX;
                mLastX = event.getX();
                if (mSlidingThumb == mLesserThumb) {
                    int lessStep = mLesserThumb.calculateStep(event.getX(), 0f);
                    if (lessStep > mLargerThumb.getCurrentStep() - gap && dx > 0) {
                        mSlidingThumb = mLargerThumb;
                    } else {
                        if (lessStep <= mLargerThumb.getCurrentStep() - gap) {
                            mSlidingThumb.setCurrentStep(lessStep, true);
                        }
                    }
                } else {
                    int largerStep = mLargerThumb.calculateStep(event.getX(), 0f);
                    if (largerStep < mLesserThumb.getCurrentStep() + gap && dx < 0) {
                        mSlidingThumb = mLesserThumb;
                    } else {
                        if (largerStep >= mLesserThumb.getCurrentStep() + gap)
                            mSlidingThumb.setCurrentStep(largerStep, true);
                    }
                }
                invalidate();
                break;
            case MotionEvent.ACTION_UP:

                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }


    private float dp2px(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }


    @Override
    public void onProgressChanged(Thumb thumb, float progress, boolean fromUser) {
        if (onSeekBarChangeListener != null) {
            onSeekBarChangeListener.onProgressChanged(this, mLesserThumb.getProgress(), mLargerThumb.getProgress(), fromUser);
        }
    }


    public interface OnSeekBarChangeListener {
        void onProgressChanged(SeekBar seekBar, float lesserProgress, float largerProgress, boolean fromUser);
    }




    @Override
    protected Parcelable onSaveInstanceState() {
        RangeSeekBarState state = new RangeSeekBarState(super.onSaveInstanceState());
        state.lesserStep=mLesserThumb.getCurrentStep();
        state.largerStep=mLargerThumb.getCurrentStep();
        return state;
    }


    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        RangeSeekBarState rs = (RangeSeekBarState) state;
        super.onRestoreInstanceState(((RangeSeekBarState) state).getSuperState());
        mLesserThumb.setCurrentStep(rs.lesserStep, false);
        mLargerThumb.setCurrentStep(rs.largerStep, false);
    }




    private class RangeSeekBarState extends BaseSavedState {
        private int lesserStep;
        private int largerStep;

        public RangeSeekBarState(Parcelable superState) {
            super(superState);
        }

        public RangeSeekBarState(Parcel source) {
            super(source);
            lesserStep = source.readInt();
            largerStep = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(lesserStep);
            out.writeInt(largerStep);
        }


    }

}
