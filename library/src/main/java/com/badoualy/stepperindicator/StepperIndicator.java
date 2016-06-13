package com.badoualy.stepperindicator;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

public class StepperIndicator extends View implements ViewPager.OnPageChangeListener {

    private Paint circlePaint;
    private Paint circleDonePaint;
    private Paint linePaint;
    private Paint lineDonePaint;
    private Paint indicatorPaint;

    private float circleRadius;
    private float indicatorRadius;
    private float lineMargin;

    private int stepCount;
    private int currentStep;

    private float[] indicators;

    private ViewPager pager;
    private Bitmap doneIcon;

    public StepperIndicator(Context context) {
        this(context, null);
    }

    public StepperIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepperIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StepperIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final Resources resources = getResources();

        // Default value
        int defaultCircleColor = ContextCompat.getColor(context, R.color.stpi_default_circle_color);
        float defaultCircleRadius = resources.getDimension(R.dimen.stpi_default_circle_radius);
        float defaultCircleStrokeWidth = resources.getDimension(R.dimen.stpi_default_circle_storke_width);

        int defaultIndicatorColor = ContextCompat.getColor(context, R.color.stpi_default_indicator_color);
        float defaultIndicatorRadius = resources.getDimension(R.dimen.stpi_default_indicator_radius);

        float defaultLineStrokeWidth = resources.getDimension(R.dimen.stpi_default_line_stroke_width);
        float defaultLineMargin = resources.getDimension(R.dimen.stpi_default_line_margin);
        int defaultLineColor = ContextCompat.getColor(context, R.color.stpi_default_line_color);
        int defaultLineDoneColor = ContextCompat.getColor(context, R.color.stpi_default_line_done_color);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StepperIndicator, defStyleAttr, 0);

        circlePaint = new Paint();
        circlePaint.setStrokeWidth(defaultCircleStrokeWidth);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_circleColor, defaultCircleColor));
        circlePaint.setAntiAlias(true);

        indicatorPaint = new Paint();
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_indicatorColor, defaultIndicatorColor));
        indicatorPaint.setAntiAlias(true);

        circleDonePaint = new Paint(circlePaint);
        circleDonePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        circleDonePaint.setColor(indicatorPaint.getColor());

        linePaint = new Paint();
        linePaint.setStrokeWidth(a.getDimension(R.styleable.StepperIndicator_stpi_lineStrokeWidth, defaultLineStrokeWidth));
        //linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineColor, defaultLineColor));
        linePaint.setAntiAlias(true);

        lineDonePaint = new Paint(linePaint);
        lineDonePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineDoneColor, defaultLineDoneColor));

        circleRadius = a.getDimension(R.styleable.StepperIndicator_stpi_circleRadius, defaultCircleRadius);
        indicatorRadius = a.getDimension(R.styleable.StepperIndicator_stpi_indicatorRadius, defaultIndicatorRadius);
        lineMargin = a.getDimension(R.styleable.StepperIndicator_stpi_lineMargin, defaultLineMargin);

        setStepCount(a.getInteger(R.styleable.StepperIndicator_stpi_stepCount, 2));

        a.recycle();

        doneIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_done_white_18dp);

        if (isInEditMode())
            currentStep = Math.max((int) Math.ceil(stepCount / 2f), 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerY = getMeasuredHeight() / 2f;
        float circleCenterY = centerY;
        float lineCenterY = centerY;

        float startX = circleRadius + circlePaint.getStrokeWidth() / 2f;

        // Compute position of indicators and line length
        float divider = (getMeasuredWidth() - startX * 2f) / (stepCount - 1);
        float lineLength = divider - (circleRadius * 2f + circlePaint.getStrokeWidth()) - (lineMargin * 2);

        // Compute position of circles
        for (int i = 0; i < indicators.length; i++)
            indicators[i] = startX + divider * i;

        for (int i = 0; i < indicators.length; i++) {
            final float indicator = indicators[i];
            if (i >= currentStep - 1) {
                canvas.drawCircle(indicator, circleCenterY, circleRadius, circlePaint);
                if (i == currentStep - 1)
                    canvas.drawCircle(indicator, circleCenterY, indicatorRadius, indicatorPaint);
            } else {
                canvas.drawCircle(indicator, circleCenterY, circleRadius, circleDonePaint);
                if (!isInEditMode())
                    canvas.drawBitmap(doneIcon, indicator - (doneIcon.getWidth() / 2), circleCenterY - (doneIcon.getHeight() / 2), null);
            }

            if (i != indicators.length - 1) {
                float lineCenterX = (indicator + indicators[i + 1]) / 2;
                canvas.drawLine(lineCenterX - lineLength / 2, lineCenterY, lineCenterX + lineLength / 2, lineCenterY,
                                i < currentStep - 1 ? lineDonePaint : linePaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) Math.ceil((circleRadius * 2) + circlePaint.getStrokeWidth());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = widthMode == MeasureSpec.EXACTLY ? widthSize : getSuggestedMinimumWidth();
        int height = heightMode == MeasureSpec.EXACTLY ? heightSize : desiredHeight;

        setMeasuredDimension(width, height);
    }

    public void setStepCount(int stepCount) {
        if (stepCount < 2)
            throw new IllegalArgumentException("stepCount must be >= 2");
        this.stepCount = stepCount;
        indicators = new float[stepCount];
        invalidate();
    }

    public int getStepCount() {
        return stepCount;
    }

    /**
     * Starts from 1
     *
     * @param currentStep
     */
    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
        invalidate();
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setViewPager(ViewPager pager) {
        setViewPager(pager, pager.getAdapter().getCount());
    }

    /**
     * Sets the pager associated with this indicator
     *
     * @param pager         viewpager to attach
     * @param realPageCount the real page count to display (use this if you are using looped viewpager)
     */
    public void setViewPager(ViewPager pager, int realPageCount) {
        if (this.pager == pager)
            return;
        if (this.pager != null)
            pager.removeOnPageChangeListener(this);
        if (pager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        this.pager = pager;
        this.stepCount = realPageCount;
        pager.addOnPageChangeListener(this);
        invalidate();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurrentStep(position + 1);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentStep = savedState.currentStep;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentStep = currentStep;
        return savedState;
    }

    static class SavedState extends BaseSavedState {

        private int currentStep;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentStep = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentStep);
        }

        @SuppressWarnings("UnusedDeclaration")
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
