package com.badoualy.stepperindicator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.UiThread;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
/**
 * Step indicator that can be used with (or without) a {@link ViewPager} to display current progress through an Onboarding or any process in
 * multiple steps.
 * The default main primary color if not specified in the XML attributes will use the theme primary color defined via {@link R.attr.colorPrimary}.
 * <p>
 * Updated by Ionut Negru on 08/08/16 to add the stepClickListener feature.
 * </p>
 */
public class StepperIndicator extends View implements ViewPager.OnPageChangeListener {

    /** Duration of the line drawing animation (ms) */
    private static final int DEFAULT_ANIMATION_DURATION = 250;
    /** Max multiplier of the radius when a step is being animated to the "done" state before going to it's normal radius */
    private static final float EXPAND_MARK = 1.3f;

    /** Paint used to draw grey circle */
    private Paint circlePaint;
    // Paint used to draw the different line states
    private Paint linePaint, lineDonePaint, lineDoneAnimatedPaint;
    /** Paint used to draw the indicator circle for the current and cleared steps */
    private Paint indicatorPaint;

    /** List of {@link Path} for each line between steps */
    private List<Path> linePathList = new ArrayList<>();
    /** Click areas for each of the steps supported by the StepperIndicator widget. */
    private List<RectF> stepsClickAreas;
    private float animProgress;
    private float animIndicatorRadius;
    private float animCheckRadius;
    /** "Constant" size of the lines between steps */
    private float lineLength;

    // Values retrieved from xml (or default values)
    private float circleRadius;
    private float checkRadius;
    private float indicatorRadius;
    private float lineMargin;
    private int animDuration;

    /**
     * Custom step click listener which will notify any component which sets an listener of any events (touch events)
     * that happen regarding the steps widget.
     */
    private final List<OnStepClickListener> onStepClickListeners = new ArrayList<>(0);

    /**
     * Custom gesture listener though which all the touch events are propagated.
     * <p>
     * The whole purpose of this listener is to correctly detect which step was touched by the user and notify
     * the component which registered to receive event updates through {@link #addOnStepClickListener(OnStepClickListener)}
     * </p>
     */
    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (onStepClickListeners.isEmpty())
                return super.onSingleTapConfirmed(e);
            float xCord = e.getX();
            float yCord = e.getY();
            Log.d("StepperIndicator", "onSingleTapConfirmed: clicked = [" + xCord + ", " + yCord + "]");

            // TODO: 05/08/16 Improve look up for the clicked step - binary search could be an option

            int clickedStep = -1;
            for (int i = 0; i < stepsClickAreas.size(); i++) {
                if (stepsClickAreas.get(i).contains(xCord, yCord)) {
                    Log.d("StepperIndicator", "onSingleTapConfirmed: step #" + i + " clicked!");
                    clickedStep = i;
                    // Stop as we found the step which was clicked
                    break;
                }
            }

            // If the clicked step is valid and an listener was setup - send the event
            if (clickedStep != -1) {
                for (OnStepClickListener listener : onStepClickListeners) {
                    listener.onStepClicked(clickedStep);
                }
            }

            return super.onSingleTapConfirmed(e);
        }
    };

    /**
     * The gesture detector at which all the touch events will be propagated to.
     */
    private GestureDetector gestureDetector;

    // Current state
    private static final int STEP_COUNT_INVALID = -1;
    private int stepCount;
    private int currentStep;
    private int previousStep;

    // X position of each step indicator's center
    private float[] indicators;

    private ViewPager pager;
    private Bitmap doneIcon;
    private boolean showDoneIcon;

    // Running animations
    private AnimatorSet animatorSet;
    private ObjectAnimator lineAnimator, indicatorAnimator, checkAnimator;

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
        float defaultCircleStrokeWidth = resources.getDimension(R.dimen.stpi_default_circle_stroke_width);

        int defaultPrimaryColor = getPrimaryColor(context);

        int defaultIndicatorColor = defaultPrimaryColor;
        float defaultIndicatorRadius = resources.getDimension(R.dimen.stpi_default_indicator_radius);

        float defaultLineStrokeWidth = resources.getDimension(R.dimen.stpi_default_line_stroke_width);
        float defaultLineMargin = resources.getDimension(R.dimen.stpi_default_line_margin);
        int defaultLineColor = ContextCompat.getColor(context, R.color.stpi_default_line_color);
        int defaultLineDoneColor = defaultPrimaryColor;

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StepperIndicator, defStyleAttr, 0);

        circlePaint = new Paint();
        circlePaint.setStrokeWidth(a.getDimension(R.styleable.StepperIndicator_stpi_circleStrokeWidth, defaultCircleStrokeWidth));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_circleColor, defaultCircleColor));
        circlePaint.setAntiAlias(true);

        indicatorPaint = new Paint(circlePaint);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_indicatorColor, defaultIndicatorColor));
        indicatorPaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setStrokeWidth(a.getDimension(R.styleable.StepperIndicator_stpi_lineStrokeWidth, defaultLineStrokeWidth));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineColor, defaultLineColor));
        linePaint.setAntiAlias(true);

        lineDonePaint = new Paint(linePaint);
        lineDonePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineDoneColor, defaultLineDoneColor));

        lineDoneAnimatedPaint = new Paint(lineDonePaint);

        circleRadius = a.getDimension(R.styleable.StepperIndicator_stpi_circleRadius, defaultCircleRadius);
        checkRadius = circleRadius + circlePaint.getStrokeWidth() / 2f;
        indicatorRadius = a.getDimension(R.styleable.StepperIndicator_stpi_indicatorRadius, defaultIndicatorRadius);
        animIndicatorRadius = indicatorRadius;
        animCheckRadius = checkRadius;
        lineMargin = a.getDimension(R.styleable.StepperIndicator_stpi_lineMargin, defaultLineMargin);

        setStepCount(a.getInteger(R.styleable.StepperIndicator_stpi_stepCount, 2));
        animDuration = a.getInteger(R.styleable.StepperIndicator_stpi_animDuration, DEFAULT_ANIMATION_DURATION);
        showDoneIcon = a.getBoolean(R.styleable.StepperIndicator_stpi_showDoneIcon, true);

        a.recycle();

        if (showDoneIcon) {
            // TODO replace with drawable
            doneIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_done_white_18dp);
        }

        // Display at least 1 cleared step for preview in XML editor
        if (isInEditMode()) {
            currentStep = Math.max((int) Math.ceil(stepCount / 2f), 1);
        }

        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    public static int getPrimaryColor(final Context context) {
        int color = context.getResources().getIdentifier("colorPrimary", "attr", context.getPackageName());
        if (color != 0) {
            // If using support library v7 primaryColor
            TypedValue t = new TypedValue();
            context.getTheme().resolveAttribute(color, t, true);
            color = t.data;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // If using native primaryColor (SDK >21)
            TypedArray t = context.obtainStyledAttributes(new int[]{android.R.attr.colorPrimary});
            color = t.getColor(0, ContextCompat.getColor(context, R.color.stpi_default_primary_color));
            t.recycle();
        } else {
            TypedArray t = context.obtainStyledAttributes(new int[]{R.attr.colorPrimary});
            color = t.getColor(0, ContextCompat.getColor(context, R.color.stpi_default_primary_color));
            t.recycle();
        }

        return color;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        compute(); // for setting up the indicator based on the new position
    }

    private void compute() {
        computeStepsClickAreas(); // update the position of the steps click area also

        indicators = new float[stepCount];
        linePathList.clear();

        float startX = circleRadius * EXPAND_MARK + circlePaint.getStrokeWidth() / 2f;

        // Compute position of indicators and line length
        float divider = (getMeasuredWidth() - startX * 2f) / (stepCount - 1);
        lineLength = divider - (circleRadius * 2f + circlePaint.getStrokeWidth()) - (lineMargin * 2);

        // Compute position of circles and lines once
        for (int i = 0; i < indicators.length; i++)
            indicators[i] = startX + divider * i;
        for (int i = 0; i < indicators.length - 1; i++) {
            float position = ((indicators[i] + indicators[i + 1]) / 2) - lineLength / 2;
            final Path linePath = new Path();
            linePath.moveTo(position, getMeasuredHeight() / 2);
            linePath.lineTo(position + lineLength, getMeasuredHeight() / 2);
            linePathList.add(linePath);
        }
    }

    /**
     * <p>
     * Calculate the area for each step. This ensure the correct step is detected when an click event is detected.
     * </p>
     */
    private void computeStepsClickAreas() {
        if (stepCount == STEP_COUNT_INVALID) {
            throw new IllegalArgumentException("stepCount wasn't setup yet. Make sure you call setStepCount() " +
                                                       "before computing the steps click area!");
        }

        if (null == indicators) {
            throw new IllegalArgumentException("indicators wasn't setup yet. Make sure the indicators are " +
                                                       "initialized and setup correctly before trying to compute the click " +
                                                       "area for each step!");
        }

        // Initialize the list for the steps click area
        stepsClickAreas = new ArrayList<>(stepCount);

        // Compute the clicked area for each step
        for (float indicator : indicators) {
            // Get the indicator position
            // Calculate the bounds for the step
            float left = indicator - circleRadius;
            float right = indicator + circleRadius;
            float top = getTop();
            float bottom = getBottom();

            // Store the click area for the step
            RectF area = new RectF(left, top, right, bottom);
            stepsClickAreas.add(area);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onDraw(Canvas canvas) {
        float centerY = getMeasuredHeight() / 2f;

        // Currently Drawing animation from step n-1 to n, or back from n+1 to n
        boolean inAnimation = false;
        boolean inLineAnimation = false;
        boolean inIndicatorAnimation = false;
        boolean inCheckAnimation = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            inAnimation = animatorSet != null && animatorSet.isRunning();
            inLineAnimation = lineAnimator != null && lineAnimator.isRunning();
            inIndicatorAnimation = indicatorAnimator != null && indicatorAnimator.isRunning();
            inCheckAnimation = checkAnimator != null && checkAnimator.isRunning();
        }

        boolean drawToNext = previousStep == currentStep - 1;
        boolean drawFromNext = previousStep == currentStep + 1;

        for (int i = 0; i < indicators.length; i++) {
            final float indicator = indicators[i];

            // We draw the "done" check if previous step, or if we are going back (if going back, animated value will reduce radius to 0)
            boolean drawCheck = i < currentStep || (drawFromNext && i == currentStep);

            // Draw back circle
            canvas.drawCircle(indicator, centerY, circleRadius, circlePaint);

            // If current step, or coming back from next step and still animating
            if ((i == currentStep && !drawFromNext) || (i == previousStep && drawFromNext && inAnimation)) {
                // Draw animated indicator
                canvas.drawCircle(indicator, centerY, animIndicatorRadius, indicatorPaint);
            }

            // Draw check mark
            if (drawCheck) {
                float radius = checkRadius;
                // Use animated radius value?
                if ((i == previousStep && drawToNext)
                        || (i == currentStep && drawFromNext))
                    radius = animCheckRadius;
                canvas.drawCircle(indicator, centerY, radius, indicatorPaint);

                // Draw check bitmap
                if (!isInEditMode() && showDoneIcon) {
                    if ((i != previousStep && i != currentStep) || (!inCheckAnimation && !(i == currentStep && !inAnimation)))
                        canvas.drawBitmap(doneIcon, indicator - (doneIcon.getWidth() / 2), centerY - (doneIcon.getHeight() / 2), null);
                }
            }

            // Draw lines
            if (i < linePathList.size()) {
                if (i >= currentStep) {
                    canvas.drawPath(linePathList.get(i), linePaint);
                    if (i == currentStep && drawFromNext && (inLineAnimation || inIndicatorAnimation)) // Coming back from n+1
                        canvas.drawPath(linePathList.get(i), lineDoneAnimatedPaint);
                } else {
                    if (i == currentStep - 1 && drawToNext && inLineAnimation) {
                        // Going to n+1
                        canvas.drawPath(linePathList.get(i), linePaint);
                        canvas.drawPath(linePathList.get(i), lineDoneAnimatedPaint);
                    } else
                        canvas.drawPath(linePathList.get(i), lineDonePaint);
                }
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredHeight = (int) Math.ceil((circleRadius * EXPAND_MARK * 2) + circlePaint.getStrokeWidth());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = widthMode == MeasureSpec.EXACTLY ? widthSize : getSuggestedMinimumWidth();
        int height = heightMode == MeasureSpec.EXACTLY ? heightSize : desiredHeight;

        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Dispatch the touch events to our custom gesture detector.
        gestureDetector.onTouchEvent(event);
        return true; // we handle the event in the gesture detector
    }

    public void setStepCount(int stepCount) {
        if (stepCount < 2)
            throw new IllegalArgumentException("stepCount must be >= 2");
        this.stepCount = stepCount;
        currentStep = 0;
        compute();
        invalidate();
    }

    public int getStepCount() {
        return stepCount;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    /**
     * Sets the current step
     *
     * @param currentStep a value between 0 (inclusive) and stepCount (inclusive)
     */
    @UiThread
    public void setCurrentStep(int currentStep) {
        if (currentStep < 0 || currentStep > stepCount)
            throw new IllegalArgumentException("Invalid step value " + currentStep);

        previousStep = this.currentStep;
        this.currentStep = currentStep;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Cancel any running animations
            if (animatorSet != null)
                animatorSet.cancel();
            animatorSet = null;
            lineAnimator = null;
            indicatorAnimator = null;

            if (currentStep == previousStep + 1) {
                // Going to next step
                animatorSet = new AnimatorSet();

                // First, draw line to new
                lineAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animProgress", 1.0f, 0.0f);

                // Same time, pop check mark
                checkAnimator = ObjectAnimator
                        .ofFloat(StepperIndicator.this, "animCheckRadius", indicatorRadius, checkRadius * EXPAND_MARK, checkRadius);

                // Finally, pop current step indicator
                animIndicatorRadius = 0;
                indicatorAnimator = ObjectAnimator
                        .ofFloat(StepperIndicator.this, "animIndicatorRadius", 0f, indicatorRadius * 1.4f, indicatorRadius);

                animatorSet.play(lineAnimator).with(checkAnimator).before(indicatorAnimator);
            } else if (currentStep == previousStep - 1) {
                // Going back to previous step
                animatorSet = new AnimatorSet();

                // First, pop out current step indicator
                indicatorAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animIndicatorRadius", indicatorRadius, 0f);

                // Then delete line
                animProgress = 1.0f;
                lineDoneAnimatedPaint.setPathEffect(null);
                lineAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animProgress", 0.0f, 1.0f);

                // Finally, pop out check mark to display step indicator
                animCheckRadius = checkRadius;
                checkAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animCheckRadius", checkRadius, indicatorRadius);

                animatorSet.playSequentially(indicatorAnimator, lineAnimator, checkAnimator);
            }

            if (animatorSet != null) {
                // Max 500 ms for the animation
                lineAnimator.setDuration(Math.min(500, animDuration));
                lineAnimator.setInterpolator(new DecelerateInterpolator());
                // Other animations will run 2 times faster that line animation
                indicatorAnimator.setDuration(lineAnimator.getDuration() / 2);
                checkAnimator.setDuration(lineAnimator.getDuration() / 2);

                animatorSet.start();
            }
        }

        invalidate();
    }

    /** DO NOT CALL, used by animation */
    public void setAnimProgress(float animProgress) {
        this.animProgress = animProgress;
        lineDoneAnimatedPaint.setPathEffect(createPathEffect(lineLength, animProgress, 0.0f));
        invalidate();
    }

    /** DO NOT CALL, used by animation */
    public void setAnimIndicatorRadius(float animIndicatorRadius) {
        this.animIndicatorRadius = animIndicatorRadius;
        invalidate();
    }

    /** DO NOT CALL, used by animation */
    public void setAnimCheckRadius(float animCheckRadius) {
        this.animCheckRadius = animCheckRadius;
        invalidate();
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        // Create a PathEffect to set on a Paint to only draw some part of the line
        return new DashPathEffect(new float[]{pathLength, pathLength},
                                  Math.max(phase * pathLength, offset));
    }

    public void setViewPager(ViewPager pager) {
        if (pager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        setViewPager(pager, pager.getAdapter().getCount());
    }

    /**
     * Sets the pager associated with this indicator
     *
     * @param pager        viewpager to attach
     * @param keepLastPage true if should not create an indicator for the last page, to use it as the final page
     */
    public void setViewPager(ViewPager pager, boolean keepLastPage) {
        if (pager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        setViewPager(pager, pager.getAdapter().getCount() - (keepLastPage ? 1 : 0));
    }

    /**
     * Sets the pager associated with this indicator
     *
     * @param pager     viewpager to attach
     * @param stepCount the real page count to display (use this if you are using looped viewpager to indicate the real number of pages)
     */
    public void setViewPager(ViewPager pager, int stepCount) {
        if (this.pager == pager)
            return;
        if (this.pager != null)
            pager.removeOnPageChangeListener(this);
        if (pager.getAdapter() == null)
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        this.pager = pager;
        this.stepCount = stepCount;
        currentStep = 0;
        pager.addOnPageChangeListener(this);
        invalidate();
    }

    /**
     * Add the {@link OnStepClickListener} to the list of listeners which will receive events when an step is clicked.
     *
     * @param listener The {@link OnStepClickListener} which will be added
     */
    public void addOnStepClickListener(OnStepClickListener listener) {
        onStepClickListeners.add(listener);
    }

    /**
     * Remove the specified {@link OnStepClickListener} from the list of listeners which will receive events when an
     * step is clicked.
     *
     * @param listener The {@link OnStepClickListener} which will be removed
     */
    public void removeOnStepClickListener(OnStepClickListener listener) {
        onStepClickListeners.remove(listener);
    }

    /**
     * Remove all {@link OnStepClickListener} listeners from the StepperIndicator widget.
     * No more events will be propagated.
     */
    public void clearOnStepClickListeners() {
        onStepClickListeners.clear();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        setCurrentStep(position);
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

    /**
     * Contract used by the StepperIndicator widget to notify any listener of steps interaction events.
     */
    public interface OnStepClickListener {
        /**
         * Step was clicked
         *
         * @param step The step position which was clicked. (starts from 0, as the ViewPager bound to the widget)
         */
        void onStepClicked(int step);
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
