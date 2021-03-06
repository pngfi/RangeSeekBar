package com.pngfi.rangeseekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;


public class Thumb {

    //the x of drawable center when the progress is min
    private int left;
    private int top;


    private PointF centerPoint = new PointF();


    private float min;
    private float max;


    private Drawable thumbDrawable;


    /**
     * the real slide width of thumb
     * progressWidth=getWidth()-thumb.getIntrinsicWidth()
     */
    private int progressWidth;

    //the count of steps
    private int stepCount;

    //the progress value of per stepProgress
    private float stepProgress = 1f;


    private Paint shadowPaint;
    private int shadowRadius;
    private int shadowOffsetX;
    private int shadowOffsetY;

    private int shadowColor;


    private int currentStep = 0;


    private OnProgressChangeListener onProgressChangeListener;


    private Context context;


    public Thumb(Context context, Drawable drawable, float min, float max) {
        this.thumbDrawable = drawable;
        this.min = min;
        this.max = max;
        this.context = context;

        shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        shadowPaint.setColor(Color.TRANSPARENT);
        shadowPaint.setStyle(Paint.Style.FILL);
    }


    public void setShadow(int shadowRadius, int shadowOffsetX, int shadowOffsetY, int shadowColor) {
        this.shadowColor = shadowColor;
        this.shadowOffsetX = shadowOffsetX;
        this.shadowOffsetY = shadowOffsetY;
        this.shadowRadius = shadowRadius;
    }


    public void setRect(int left, int top, int progressWidth) {
        this.left = left;
        this.top = top;
        this.progressWidth = progressWidth;

        refreshCenterPoint();
    }


    public void setProgress(float progress) {
        if (progress < min || progress > max) {
            throw new IllegalArgumentException("progress must be between min and max ");
        }
        int cStep = (int) ((progress - min) / stepProgress);
        if ((progress - cStep * stepProgress) >= stepProgress / 2)
            cStep++;
        setCurrentStep(cStep, false,true);
    }

     public void onPressed(boolean pressed) {
        int[] pressedState = new int[]{android.R.attr.state_pressed};
        int[] noState=new int[]{};
        thumbDrawable.getState();
        if (thumbDrawable instanceof StateListDrawable) {
            if (pressed) {
                thumbDrawable.setState(pressedState);
            }else {
                thumbDrawable.setState(noState);
            }
        }

    }


    public boolean contains(float x, float y) {
        //increase the range of touch
        final float extra = context.getResources().getDisplayMetrics().density * 5;
        return x >= thumbDrawable.getBounds().left - extra && x <= thumbDrawable.getBounds().right + extra && y >= top - extra && y <= top + thumbDrawable.getIntrinsicHeight() + extra;
    }


    public void setCurrentStep(int cStep, boolean fromUser,boolean anim) {
        if (cStep == currentStep)
            return;
        if (cStep < 0)
            cStep = 0;
        if (cStep > stepCount)
            cStep = stepCount;
        currentStep = cStep;
        refreshCenterPoint();
        if (onProgressChangeListener != null) {
            onProgressChangeListener.onProgressChanged(this, min + stepProgress * currentStep, fromUser);
        }
    }


    private void refreshCenterPoint() {
        float percent = (currentStep * 1f) / stepCount;
        float centerX = left + percent * progressWidth;
        setCenterPoint(centerX);
    }


    public void setCenterPoint(float centerX) {
        centerPoint.x = centerX;
        centerPoint.y = (top + top + thumbDrawable.getIntrinsicHeight()) / 2;
        setThumbBounds(centerX);
    }

    private void setThumbBounds(float centerX) {
        thumbDrawable.setBounds((int) centerX - thumbDrawable.getIntrinsicWidth() / 2, top, (int) centerX + thumbDrawable.getIntrinsicWidth() / 2, top + thumbDrawable.getIntrinsicHeight());
    }


    public int getCurrentStep() {
        return currentStep;
    }


    public int calculateStep(float eventX) {
        if (eventX < left ) {
            eventX = left ;
        }
        if (eventX > left + progressWidth ) {
            eventX = left + progressWidth ;
        }
        int cStep = 0;
            Float offset = (eventX - left ) / progressWidth * (max - min);
            float mod = offset % stepProgress;
            cStep = (int) (offset / stepProgress);
            if (Math.abs(mod) > stepProgress / 2) {
                cStep++;
            }
        return cStep;
    }


    public void setStepCount(int stepCount) {
        if (stepCount == 0) {
            this.stepCount = (int) (max - min);
            stepProgress = 1;
        } else {
            this.stepCount = stepCount;
            stepProgress = (max - min) / stepCount;
        }
    }


    public float getProgress() {
        return min + stepProgress * currentStep;
    }

    /**
     * @return the height the contains the shadow
     */
    public int getHeight() {
        return thumbDrawable.getIntrinsicHeight() + (shadowRadius + shadowOffsetY) * 2;
    }


    /**
     * @return the width that contains the shadow
     */
    public int getWidth() {
        return thumbDrawable.getIntrinsicWidth() + (shadowRadius + shadowOffsetX) * 2;
    }


    public Drawable getThumbDrawable() {
        return thumbDrawable;
    }


    public PointF getCenterPoint() {
        return centerPoint;
    }


    public void draw(Canvas canvas) {

        //draw shadow
        shadowPaint.setShadowLayer(shadowRadius, shadowOffsetX, shadowOffsetY, shadowColor);
        canvas.drawOval(new RectF(thumbDrawable.getBounds()), shadowPaint);

        thumbDrawable.draw(canvas);
    }


    interface OnProgressChangeListener {
        void onProgressChanged(Thumb thumb, float progress, boolean fromUser);
    }


    public void setOnProgressChangeListener(OnProgressChangeListener listener) {
        onProgressChangeListener = listener;
    }

}