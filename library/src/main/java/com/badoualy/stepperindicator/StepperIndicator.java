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
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.Rect;
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
import java.util.Random;

/**
 * <p>Step indicator that can be used with (or without) a {@link ViewPager} to display current progress through an
 * Onboarding or any process in
 * multiple steps.
 * </p>
 * <p>
 * The default main primary color if not specified in the XML attributes will use the theme primary color defined via
 * {@code colorPrimary} attribute.
 * </p>
 * <p>
 * If this view is used on a device below API 11, animations will not be used.
 * </p>
 * <p>
 * Usage of stepper custom attributes:
 * </p>
 * <table><thead>
 * <tr>
 * <th>Name</th>
 * <th>Description</th>
 * <th>Default value</th>
 * </tr>
 * </thead><tbody>
 * <tr>
 * <td>stpi_animDuration</td>
 * <td>duration of the line tracing animation</td>
 * <td>250 ms</td>
 * </tr>
 * <tr>
 * <td>stpi_stepCount</td>
 * <td>number of pages/steps</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>stpi_circleColor</td>
 * <td>color of the stroke circle</td>
 * <td>#b3bdc2 (grey)</td>
 * </tr>
 * <tr>
 * <td>stpi_circleRadius</td>
 * <td>radius of the circle</td>
 * <td>10dp</td>
 * </tr>
 * <tr>
 * <td>stpi_circleStrokeWidth</td>
 * <td>width of circle's radius</td>
 * <td>4dp</td>
 * </tr>
 * <tr>
 * <td>stpi_indicatorColor</td>
 * <td>color for the current page indicator</td>
 * <td>#00b47c (green)</td>
 * </tr>
 * <tr>
 * <td>stpi_indicatorRadius</td>
 * <td>radius for the circle of the current page indicator</td>
 * <td>4dp</td>
 * </tr>
 * <tr>
 * <td>stpi_lineColor</td>
 * <td>color of the line between indicators</td>
 * <td>#b3bdc2 (grey)</td>
 * </tr>
 * <tr>
 * <td>stpi_lineDoneColor</td>
 * <td>color of a line when step is done</td>
 * <td>#00b47c (green)</td>
 * </tr>
 * <tr>
 * <td>stpi_lineStrokeWidth</td>
 * <td>width of the line stroke</td>
 * <td>2dp</td>
 * </tr>
 * <tr>
 * <td>stpi_lineMargin</td>
 * <td>margin at each side of the line</td>
 * <td>5dp</td>
 * </tr>
 * <tr>
 * <td>stpi_showDoneIcon</td>
 * <td>show the done check icon or not</td>
 * <td>true</td>
 * </tr>
 * <tr>
 * <td>stpi_showStepNumberInstead</td>
 * <td>display text number for each step instead of bullets</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>stpi_useBottomIndicator</td>
 * <td>display the indicator for the current step at the bottom instead of inside bullet</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>stpi_useBottomIndicatorWithStepColors</td>
 * <td>use the same color for the bottom indicator as the step color</td>
 * <td>false</td>
 * </tr>
 * <tr>
 * <td>stpi_bottomIndicatorHeight</td>
 * <td>set the height for the bottom indicator component</td>
 * <td>3dp</td>
 * </tr>
 * <tr>
 * <td>stpi_bottomIndicatorWidth</td>
 * <td>set the width for the bottom indicator component</td>
 * <td>50dp</td>
 * </tr>
 * <tr>
 * <td>stpi_bottomIndicatorMarginTop</td>
 * <td>set the top margin for the bottom indicator component</td>
 * <td>10dp</td>
 * </tr>
 * <tr>
 * <td>stpi_stepsCircleColors</td>
 * <td>use multiple colors for each step (array of colors with the size at least the same size as the stpi_stepCount
 * value)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>stpi_stepsIndicatorColors</td>
 * <td>use multiple colors for each step indicator (array of colors with the size at least the same size as the
 * stpi_stepCount value)</td>
 * <td></td>
 * </tr>
 * </tbody></table>
 *
 * <p>
 * Updated by Ionut Negru on 08/08/16 to add the stepClickListener feature.
 * Updated by Ionut Negru on 09/08/16 to add support for customizations like: multiple colors, step text number, bottom indicator.
 * </p>
 */
 @SuppressWarnings("unused")
public class StepperIndicator extends View implements ViewPager.OnPageChangeListener {

    /**
     * TAG for debugging purposes.
     */
    private static final String TAG_DEBUG = "StepperIndicator";

    /** Duration of the line drawing animation (ms) */
    private static final int DEFAULT_ANIMATION_DURATION = 250;
    /**
     * Max multiplier of the radius when a step is being animated to the "done" state before going to it's normal
     * radius
     */
    private static final float EXPAND_MARK = 1.3f;

    /**
     * Value for invalid step.
     */
    private static final int STEP_INVALID = -1;

    /**
     * Paint used to draw grey circle
     */
    private Paint mCirclePaint;
    /**
     * List of {@link Paint} objects used to draw the circle for each step.
     */
    private List<Paint> mStepsCirclePaintList;

    /**
     * The radius for the circle which describes an step.
     * <p>
     * This is either declared via XML or default is used.
     * </p>
     */
    private float mCircleRadius;

    /**
     * <p>
     * Flag indicating if the steps should be displayed with an number instead of empty circles and current animated
     * with bullet.
     * </p>
     */
    private boolean mShowStepTextNumber;

    /**
     * Paint used to draw the number indicator for all steps.
     *
     * @see #mShowStepTextNumber
     */
    private Paint mStepTextNumberPaint;

    /**
     * List of {@link Paint} objects used to draw the number indicator for each step.
     *
     * @see #mShowStepTextNumber
     */
    private List<Paint> mStepsTextNumberPaintList;

    /**
     * Paint used to draw the indicator circle for the current and cleared steps
     */
    private Paint mIndicatorPaint;

    /**
     * List of {@link Paint} objects used by each step indicating the current and cleared steps.
     * <p>
     * If this is set, it will override the default.
     * </p>
     */
    private List<Paint> mStepsIndicatorPaintList;

    /**
     * Paint used to draw the line between steps - as default.
     */
    private Paint mLinePaint;

    /**
     * Paint used to draw the line between steps when done.
     */
    private Paint mLineDonePaint;

    /**
     * Paint used to draw the line between steps when animated.
     */
    private Paint mLineDoneAnimatedPaint;

    /**
     * List of {@link Path} for each line between steps
     */
    private List<Path> mLinePathList = new ArrayList<>();

    /**
     * The progress of the animation.
     * <p>
     * <font color="red">DO NOT DELETE OR RENAME</font>: Will be used by animations logic.
     * </p>
     */
    @SuppressWarnings("unused")
    private float animProgress;
    /**
     * The radius for the animated indicator.
     * <p>
     * <font color="red">DO NOT DELETE OR RENAME</font>: Will be used by animations logic.
     * </p>
     */
    private float animIndicatorRadius;
    /**
     * The radius for the animated check mark.
     * <p>
     * <font color="red">DO NOT DELETE OR RENAME</font>: Will be used by animations logic.
     * </p>
     */
    private float animCheckRadius;

    /**
     * Flag indicating if the indicator for the current step should be displayed at the bottom.
     * <p>
     * This is useful if you want to use text number indicator for the steps as the bullet indicator will be
     * disabled for that flow.
     * </p>
     */
    private boolean mUseBottomIndicator;
    /**
     * The top margin of the bottom indicator.
     */
    private float mBottomIndicatorMarginTop = 0;
    /**
     * The width of the bottom indicator.
     */
    private float mBottomIndicatorWidth = 0;
    /**
     * The height of the bottom indicator.
     */
    private float mBottomIndicatorHeight = 0;
    /**
     * Flag indicating if the bottom indicator should use the same colors as the steps.
     */
    private boolean mUseBottomIndicatorWithStepColors;

    /**
     * "Constant" size of the lines between steps
     */
    private float mLineLength;

    // Values retrieved from xml (or default values)
    private float mCheckRadius;
    private float mIndicatorRadius;
    private float mLineMargin;
    private int mAnimDuration;
    /**
     * Custom step click listener which will notify any component which sets an listener of any events (touch events)
     * that happen regarding the steps widget.
     */
    private List<OnStepClickListener> mOnStepClickListeners = new ArrayList<>(0);
    /**
     * Click areas for each of the steps supported by the StepperIndicator widget.
     */
    private List<RectF> mStepsClickAreas;

    /**
     * The gesture detector at which all the touch events will be propagated to.
     */
    private GestureDetector mGestureDetector;
    private int mStepCount;
    private int mCurrentStep;
    private int mPreviousStep;

    // X position of each step indicator's center
    private float[] mIndicators;

    private ViewPager mPager;
    private Bitmap mDoneIcon;
    private boolean mShowDoneIcon;

    // Running animations
    private AnimatorSet mAnimatorSet;
    private ObjectAnimator mLineAnimator, mIndicatorAnimator, mCheckAnimator;

    /**
     * Custom gesture listener though which all the touch events are propagated.
     * <p>
     * The whole purpose of this listener is to correctly detect which step was touched by the user and notify
     * the component which registered to receive event updates through
     * {@link #addOnStepClickListener(OnStepClickListener)}
     * </p>
     */
    private GestureDetector.OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float xCord = e.getX();
            float yCord = e.getY();

            int clickedStep = STEP_INVALID;
            if (isOnStepClickListenerAvailable()) {
                for (int i = 0; i < mStepsClickAreas.size(); i++) {
                    if (mStepsClickAreas.get(i).contains(xCord, yCord)) {
                        clickedStep = i;
                        // Stop as we found the step which was clicked
                        break;
                    }
                }
            }

            // If the clicked step is valid and an listener was setup - send the event
            if (clickedStep != STEP_INVALID) {
                for (OnStepClickListener listener : mOnStepClickListeners) {
                    listener.onStepClicked(clickedStep);
                }
            }

            return super.onSingleTapConfirmed(e);
        }
    };

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

    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public StepperIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr);
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
            TypedArray t = context.obtainStyledAttributes(new int[] { android.R.attr.colorPrimary });
            color = t.getColor(0, ContextCompat.getColor(context, R.color.stpi_default_primary_color));
            t.recycle();
        } else {
            TypedArray t = context.obtainStyledAttributes(new int[] { R.attr.colorPrimary });
            color = t.getColor(0, ContextCompat.getColor(context, R.color.stpi_default_primary_color));
            t.recycle();
        }

        return color;
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        // Create a PathEffect to set on a Paint to only draw some part of the line
        return new DashPathEffect(new float[] { pathLength, pathLength }, Math.max(phase * pathLength, offset));
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        final Resources resources = getResources();

        // Default values
        int defaultPrimaryColor = getPrimaryColor(context);

        int defaultCircleColor = ContextCompat.getColor(context, R.color.stpi_default_circle_color);
        float defaultCircleRadius = resources.getDimension(R.dimen.stpi_default_circle_radius);
        float defaultCircleStrokeWidth = resources.getDimension(R.dimen.stpi_default_circle_stroke_width);

        //noinspection UnnecessaryLocalVariable
        int defaultIndicatorColor = defaultPrimaryColor;
        float defaultIndicatorRadius = resources.getDimension(R.dimen.stpi_default_indicator_radius);

        float defaultLineStrokeWidth = resources.getDimension(R.dimen.stpi_default_line_stroke_width);
        float defaultLineMargin = resources.getDimension(R.dimen.stpi_default_line_margin);
        int defaultLineColor = ContextCompat.getColor(context, R.color.stpi_default_line_color);
        //noinspection UnnecessaryLocalVariable
        int defaultLineDoneColor = defaultPrimaryColor;

        /* Customize the widget based on the properties set on XML, or use default if not provided */
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StepperIndicator, defStyleAttr, 0);

        mCirclePaint = new Paint();
        mCirclePaint.setStrokeWidth(
                a.getDimension(R.styleable.StepperIndicator_stpi_circleStrokeWidth, defaultCircleStrokeWidth));
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_circleColor, defaultCircleColor));
        mCirclePaint.setAntiAlias(true);

        // Call this as early as possible as other properties are configured based on the number of steps
        setStepCount(a.getInteger(R.styleable.StepperIndicator_stpi_stepCount, 2));

        final int stepsCircleColorsResID = a.getResourceId(R.styleable.StepperIndicator_stpi_stepsCircleColors, 0);
        if (stepsCircleColorsResID != 0) {
            mStepsCirclePaintList = new ArrayList<>(mStepCount);

            for (int i = 0; i < mStepCount; i++) {
                // Based on the main indicator paint object, we create the customized one
                Paint circlePaint = new Paint(mCirclePaint);
                if (isInEditMode()) {
                    // Fallback for edit mode - to show something in the preview
                    circlePaint.setColor(getRandomColor());
                } else {
                    // Get the array of attributes for the colors
                    TypedArray colorResValues = context.getResources().obtainTypedArray(stepsCircleColorsResID);

                    if (mStepCount > colorResValues.length()) {
                        throw new IllegalArgumentException(
                                "Invalid number of colors for the circles. Please provide a list " +
                                "of colors with as many items as the number of steps required!");
                    }

                    circlePaint.setColor(colorResValues.getColor(i, 0)); // specific color
                    // No need for the array anymore, recycle it
                    colorResValues.recycle();
                }

                mStepsCirclePaintList.add(circlePaint);
            }
        }

        mIndicatorPaint = new Paint(mCirclePaint);
        mIndicatorPaint.setStyle(Paint.Style.FILL);
        mIndicatorPaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_indicatorColor, defaultIndicatorColor));
        mIndicatorPaint.setAntiAlias(true);

        mStepTextNumberPaint = new Paint(mIndicatorPaint);
        mStepTextNumberPaint.setTextSize(getResources().getDimension(R.dimen.stpi_default_text_size));

        mShowStepTextNumber = a.getBoolean(R.styleable.StepperIndicator_stpi_showStepNumberInstead, false);

        // Get the resource from the context style properties
        final int stepsIndicatorColorsResID =
                a.getResourceId(R.styleable.StepperIndicator_stpi_stepsIndicatorColors, 0);
        if (stepsIndicatorColorsResID != 0) {
            // init the list of colors with the same size as the number of steps
            mStepsIndicatorPaintList = new ArrayList<>(mStepCount);
            if (mShowStepTextNumber) {
                mStepsTextNumberPaintList = new ArrayList<>(mStepCount);
            }

            for (int i = 0; i < mStepCount; i++) {
                Paint indicatorPaint = new Paint(mIndicatorPaint);

                Paint textNumberPaint = mShowStepTextNumber ? new Paint(mStepTextNumberPaint) : null;
                if (isInEditMode()) {
                    // Fallback for edit mode - to show something in the preview

                    indicatorPaint.setColor(getRandomColor()); // random color
                    if (null != textNumberPaint) {
                        textNumberPaint.setColor(indicatorPaint.getColor());
                    }
                } else {
                    // Get the array of attributes for the colors
                    TypedArray colorResValues = context.getResources().obtainTypedArray(stepsIndicatorColorsResID);

                    if (mStepCount > colorResValues.length()) {
                        throw new IllegalArgumentException(
                                "Invalid number of colors for the indicators. Please provide a list " +
                                "of colors with as many items as the number of steps required!");
                    }

                    indicatorPaint.setColor(colorResValues.getColor(i, 0)); // specific color
                    if (null != textNumberPaint) {
                        textNumberPaint.setColor(indicatorPaint.getColor());
                    }
                    // No need for the array anymore, recycle it
                    colorResValues.recycle();
                }

                mStepsIndicatorPaintList.add(indicatorPaint);
                if (mShowStepTextNumber && null != textNumberPaint) {
                    mStepsTextNumberPaintList.add(textNumberPaint);
                }
            }
        }

        mLinePaint = new Paint();
        mLinePaint.setStrokeWidth(
                a.getDimension(R.styleable.StepperIndicator_stpi_lineStrokeWidth, defaultLineStrokeWidth));
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineColor, defaultLineColor));
        mLinePaint.setAntiAlias(true);

        mLineDonePaint = new Paint(mLinePaint);
        mLineDonePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineDoneColor, defaultLineDoneColor));

        mLineDoneAnimatedPaint = new Paint(mLineDonePaint);

        // Check if we should use the bottom indicator instead of the bullet one
        mUseBottomIndicator = a.getBoolean(R.styleable.StepperIndicator_stpi_useBottomIndicator, false);
        if (mUseBottomIndicator) {
            // Get the default height(stroke width) for the bottom indicator
            float defaultHeight = resources.getDimension(R.dimen.stpi_default_bottom_indicator_height);

            mBottomIndicatorHeight =
                    a.getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorHeight, defaultHeight);

            if (mBottomIndicatorHeight <= 0) {
                Log.d(TAG_DEBUG, "init: Invalid indicator height, disabling bottom indicator feature! Please provide " +
                                 "a value greater than 0.");
                mUseBottomIndicator = false;
            }

            // Get the default width for the bottom indicator
            float defaultWidth = resources.getDimension(R.dimen.stpi_default_bottom_indicator_width);
            mBottomIndicatorWidth =
                    a.getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorWidth, defaultWidth);

            // Get the default top margin for the bottom indicator
            float defaultTopMargin = resources.getDimension(R.dimen.stpi_default_bottom_indicator_margin_top);
            mBottomIndicatorMarginTop =
                    a.getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorMarginTop, defaultTopMargin);

            mUseBottomIndicatorWithStepColors =
                    a.getBoolean(R.styleable.StepperIndicator_stpi_useBottomIndicatorWithStepColors, false);
        }

        mCircleRadius = a.getDimension(R.styleable.StepperIndicator_stpi_circleRadius, defaultCircleRadius);
        mCheckRadius = mCircleRadius + mCirclePaint.getStrokeWidth() / 2f;
        mIndicatorRadius = a.getDimension(R.styleable.StepperIndicator_stpi_indicatorRadius, defaultIndicatorRadius);
        animIndicatorRadius = mIndicatorRadius;
        animCheckRadius = mCheckRadius;
        mLineMargin = a.getDimension(R.styleable.StepperIndicator_stpi_lineMargin, defaultLineMargin);

        mAnimDuration = a.getInteger(R.styleable.StepperIndicator_stpi_animDuration, DEFAULT_ANIMATION_DURATION);
        mShowDoneIcon = a.getBoolean(R.styleable.StepperIndicator_stpi_showDoneIcon, true);

        a.recycle();

        if (mShowDoneIcon) {
            mDoneIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_done_white_12dp);
        }

        // Display at least 1 cleared step for preview in XML editor
        if (isInEditMode()) {
            mCurrentStep = Math.max((int) Math.ceil(mStepCount / 2f), 1);
        }

        // Initialize the gesture detector, setup with our custom gesture listener
        mGestureDetector = new GestureDetector(getContext(), mGestureListener);
    }

    /**
     * Get an random color {@link Paint} object.
     *
     * @return {@link Paint} object with the same attributes as {@link #mCirclePaint} and with an random color.
     *
     * @see #mCirclePaint
     * @see #getRandomColor()
     */
    private Paint getRandomPaint() {
        Paint paint = new Paint(mIndicatorPaint);
        paint.setColor(getRandomColor());

        return paint;
    }

    /**
     * Get an random color value.
     *
     * @return The color value as AARRGGBB
     */
    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Dispatch the touch events to our custom gesture detector.
        mGestureDetector.onTouchEvent(event);
        return true; // we handle the event in the gesture detector
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        compute(); // for setting up the indicator based on the new position
        computeStepsClickAreas(); // update the position of the steps click area also
    }

    /**
     * Make calculations for establishing the exact positions of each step component, for the line dividers, for
     * bottom indicators, etc.
     * <p>
     * Call this whenever there is an layout change for the widget.
     * </p>
     */
    private void compute() {
        if (null == mCirclePaint) {
            throw new IllegalArgumentException("mCirclePaint is invalid! Make sure you setup the field mCirclePaint " +
                                               "before calling compute() method!");
        }

        mIndicators = new float[mStepCount];
        mLinePathList.clear();

        float startX = mCircleRadius * EXPAND_MARK + mCirclePaint.getStrokeWidth() / 2f;
        if (mUseBottomIndicator) {
            startX += mBottomIndicatorWidth / 2 - startX;
        }

        // Compute position of indicators and line length
        float divider = (getMeasuredWidth() - startX * 2f) / (mStepCount - 1);
        mLineLength = divider - (mCircleRadius * 2f + mCirclePaint.getStrokeWidth()) - (mLineMargin * 2);

        // Compute position of circles and lines once
        for (int i = 0; i < mIndicators.length; i++) {
            mIndicators[i] = startX + divider * i;
        }
        for (int i = 0; i < mIndicators.length - 1; i++) {
            float position = ((mIndicators[i] + mIndicators[i + 1]) / 2) - mLineLength / 2;
            final Path linePath = new Path();
            int lineY = getStepCenterY();
            linePath.moveTo(position, lineY);
            linePath.lineTo(position + mLineLength, lineY);
            mLinePathList.add(linePath);
        }
    }

    /**
     * <p>
     * Calculate the area for each step. This ensure the correct step is detected when an click event is detected.
     * </p>
     * <p>
     * Whenever {@link #compute()} method is called, make sure to call this method also so that the steps click
     * area is updated.
     * </p>
     */
    public void computeStepsClickAreas() {
        if (mStepCount == STEP_INVALID) {
            throw new IllegalArgumentException("mStepCount wasn't setup yet. Make sure you call setStepCount() " +
                                               "before computing the steps click area!");
        }

        if (null == mIndicators) {
            throw new IllegalArgumentException("indicators wasn't setup yet. Make sure the indicators are " +
                                               "initialized and setup correctly before trying to compute the click " +
                                               "area for each step!");
        }

        // Initialize the list for the steps click area
        mStepsClickAreas = new ArrayList<>(mStepCount);

        // Compute the clicked area for each step
        for (float indicator : mIndicators) {
            // Get the indicator position
            // Calculate the bounds for the step
            float left = indicator - mCircleRadius * 2;
            float right = indicator + mCircleRadius * 2;
            float top = getStepCenterY() - mCircleRadius * 2;
            float bottom = getStepCenterY() + mCircleRadius + getBottomIndicatorHeight();

            // Store the click area for the step
            RectF area = new RectF(left, top, right, bottom);
            mStepsClickAreas.add(area);
        }
    }
    
    /**
     * Get the height of the bottom indicator.
     * <p>
     * The height will include the height necessary for correctly drawing the bottom indicator plus the margin
     * set in XML (or the default one).
     * </p>
     * <p>
     * If the widget isn't set to display the bottom indicator this will method will always return {@code 0}
     * </p>
     *
     * @return The height of the bottom indicator in pixels or {@code 0}.
     */
    private int getBottomIndicatorHeight() {
        if (mUseBottomIndicator) {
            return (int) (mBottomIndicatorHeight + mBottomIndicatorMarginTop);
        } else {
            return 0;
        }
    }
    
    private float getStepCenterY() {
        return (getMeasuredHeight() - getBottomIndicatorHeight()) / 2f;
    }


    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onDraw(Canvas canvas) {
        float centerY = getStepCenterY();

        // Currently Drawing animation from step n-1 to n, or back from n+1 to n
        boolean inAnimation = false;
        boolean inLineAnimation = false;
        boolean inIndicatorAnimation = false;
        boolean inCheckAnimation = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            inAnimation = mAnimatorSet != null && mAnimatorSet.isRunning();
            inLineAnimation = mLineAnimator != null && mLineAnimator.isRunning();
            inIndicatorAnimation = mIndicatorAnimator != null && mIndicatorAnimator.isRunning();
            inCheckAnimation = mCheckAnimator != null && mCheckAnimator.isRunning();
        }

        boolean drawToNext = mPreviousStep == mCurrentStep - 1;
        boolean drawFromNext = mPreviousStep == mCurrentStep + 1;

        for (int i = 0; i < mIndicators.length; i++) {
            final float indicator = mIndicators[i];

            // We draw the "done" check if previous step, or if we are going back (if going back, animated value will reduce radius to 0)
            boolean drawCheck = i < mCurrentStep || (drawFromNext && i == mCurrentStep);

            // Draw back circle
            canvas.drawCircle(indicator, centerY, mCircleRadius, getStepCirclePaint(i));

            // Draw the step number inside the back circle if the flag for this is set to true
            if (mShowStepTextNumber) {
                final String stepLabel = String.valueOf(i + 1);

                Rect stepAreaRect = new Rect((int) (indicator - mCircleRadius), (int) (centerY - mCircleRadius),
                                             (int) (indicator + mCircleRadius), (int) (centerY + mCircleRadius));
                RectF stepBounds = new RectF(stepAreaRect);

                Paint stepTextNumberPaint = getStepTextNumberPaint(i);

                // measure text width
                stepBounds.right = stepTextNumberPaint.measureText(stepLabel, 0, stepLabel.length());
                // measure text height
                stepBounds.bottom = stepTextNumberPaint.descent() - stepTextNumberPaint.ascent();

                stepBounds.left += (stepAreaRect.width() - stepBounds.right) / 2.0f;
                stepBounds.top += (stepAreaRect.height() - stepBounds.bottom) / 2.0f;

                canvas.drawText(stepLabel, stepBounds.left, stepBounds.top - stepTextNumberPaint.ascent(),
                                stepTextNumberPaint);
            }

            if (mUseBottomIndicator) {
                // Show the current step indicator as bottom line
                if (i == mCurrentStep) {
                    // Draw custom indicator for current step only
                    canvas.drawRect(indicator - mBottomIndicatorWidth / 2, getHeight() - mBottomIndicatorHeight,
                                    indicator + mBottomIndicatorWidth / 2, getHeight(),
                                    mUseBottomIndicatorWithStepColors ? getStepIndicatorPaint(i) : mIndicatorPaint);
                }
            } else {
                // Show the current step indicator as bullet
                // If current step, or coming back from next step and still animating
                if ((i == mCurrentStep && !drawFromNext) || (i == mPreviousStep && drawFromNext && inAnimation)) {
                    // Draw animated indicator
                    canvas.drawCircle(indicator, centerY, animIndicatorRadius, getStepIndicatorPaint(i));
                }
            }

            // Draw check mark
            if (drawCheck) {
                float radius = mCheckRadius;
                // Use animated radius value?
                if ((i == mPreviousStep && drawToNext) || (i == mCurrentStep && drawFromNext)) radius = animCheckRadius;
                canvas.drawCircle(indicator, centerY, radius, getStepIndicatorPaint(i));

                // Draw check bitmap
                if (!isInEditMode() && mShowDoneIcon) {
                    if ((i != mPreviousStep && i != mCurrentStep) ||
                        (!inCheckAnimation && !(i == mCurrentStep && !inAnimation))) {
                        canvas.drawBitmap(mDoneIcon, indicator - (mDoneIcon.getWidth() / 2),
                                          centerY - (mDoneIcon.getHeight() / 2), null);
                    }
                }
            }

            // Draw lines
            if (i < mLinePathList.size()) {
                if (i >= mCurrentStep) {
                    canvas.drawPath(mLinePathList.get(i), mLinePaint);
                    if (i == mCurrentStep && drawFromNext && (inLineAnimation || inIndicatorAnimation)) {
                        // Coming back from n+1
                        canvas.drawPath(mLinePathList.get(i), mLineDoneAnimatedPaint);
                    }
                } else {
                    if (i == mCurrentStep - 1 && drawToNext && inLineAnimation) {
                        // Going to n+1
                        canvas.drawPath(mLinePathList.get(i), mLinePaint);
                        canvas.drawPath(mLinePathList.get(i), mLineDoneAnimatedPaint);
                    } else {
                        canvas.drawPath(mLinePathList.get(i), mLineDonePaint);
                    }
                }
            }
        }
    }

    /**
     * Get the {@link Paint} object which should be used for displaying the current step indicator.
     *
     * @param stepPosition
     *         The step position for which to retrieve the {@link Paint} object
     *
     * @return The {@link Paint} object for the specified step position
     */
    private Paint getStepIndicatorPaint(final int stepPosition) {
        return getPaint(stepPosition, mStepsIndicatorPaintList, mIndicatorPaint);
    }

    /**
     * Get the {@link Paint} object which should be used for drawing the text number the current step.
     *
     * @param stepPosition
     *         The step position for which to retrieve the {@link Paint} object
     *
     * @return The {@link Paint} object for the specified step position
     */
    private Paint getStepTextNumberPaint(final int stepPosition) {
        return getPaint(stepPosition, mStepsTextNumberPaintList, mStepTextNumberPaint);
    }

    /**
     * Get the {@link Paint} object which should be used for drawing the circle for the step.
     *
     * @param stepPosition
     *         The step position for which to retrieve the {@link Paint} object
     *
     * @return The {@link Paint} object for the specified step position
     */
    private Paint getStepCirclePaint(final int stepPosition) {
        return getPaint(stepPosition, mStepsCirclePaintList, mCirclePaint);
    }

    /**
     * Get the {@link Paint} object based on the step position and the source list of {@link Paint} objects.
     * <p>
     * If none found, will try to use the provided default. If not valid also, an random {@link Paint} object
     * will be returned instead.
     * </p>
     *
     * @param stepPosition
     *         The step position for which the {@link Paint} object is needed
     * @param sourceList
     *         The source list of {@link Paint} object.
     * @param defaultPaint
     *         The default {@link Paint} object which will be returned if the source list does not
     *         contain the specified step.
     *
     * @return {@link Paint} object for the specified step position.
     */
    private Paint getPaint(final int stepPosition, final List<Paint> sourceList, final Paint defaultPaint) {
        isStepValid(stepPosition); // it will throw an error if not valid

        Paint paint = null;
        if (null != sourceList && !sourceList.isEmpty()) {
            try {
                paint = sourceList.get(stepPosition);
            } catch (IndexOutOfBoundsException e) {
                // We use an random color as this usually should not happen, maybe in edit mode
                Log.d(TAG_DEBUG,
                      "getPaint: could not find the specific step paint to use! Try to use default instead!");
            }
        }

        if (null == paint && null != defaultPaint) {
            // Try to use the default
            paint = defaultPaint;
        }

        if (null == paint) {
            Log.d(TAG_DEBUG,
                  "getPaint: could not use default paint for the specific step! Using random Paint instead!");
            // If we reached this point, not even the default is setup, rely on some random color
            paint = getRandomPaint();
        }

        return paint;
    }

    /**
     * Check if the step position provided is an valid and supported step.
     * <p>
     * This method ensured the widget doesn't try to use invalid steps. It will throw an exception whenever an
     * invalid step is detected. Catch the exception if it is expected or it doesn't affect the flow.
     * </p>
     *
     * @param stepPos
     *         The step position to verify
     *
     * @return {@code true} if the step is valid, otherwise it will throw an exception.
     */
    private boolean isStepValid(final int stepPos) {
        if (stepPos < 0 || stepPos > mStepCount - 1) {
            throw new IllegalArgumentException("Invalid step position. " + stepPos + " is not a valid position! it " +
                                               "should be between 0 and mStepCount(" + mStepCount + ")");
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Compute the necessary height for the widget
        int desiredHeight = (int) Math.ceil((mCircleRadius * EXPAND_MARK * 2) + mCirclePaint.getStrokeWidth() +
                                            getBottomIndicatorHeight());

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = widthMode == MeasureSpec.EXACTLY ? widthSize : getSuggestedMinimumWidth();
        int height = heightMode == MeasureSpec.EXACTLY ? heightSize : desiredHeight;

        setMeasuredDimension(width, height);
    }

    @SuppressWarnings("unused")
    public int getStepCount() {
        return mStepCount;
    }

    public void setStepCount(int stepCount) {
        if (stepCount < 2) {
            throw new IllegalArgumentException("mStepCount must be >= 2");
        }

        mStepCount = stepCount;
        mCurrentStep = 0;
        compute();
        computeStepsClickAreas();
        invalidate();
    }

    @SuppressWarnings("unused")
    public int getCurrentStep() {
        return mCurrentStep;
    }

    /**
     * Sets the current step
     *
     * @param currentStep
     *         a value between 0 (inclusive) and mStepCount (inclusive)
     */
    @UiThread
    public void setCurrentStep(int currentStep) {
        if (currentStep < 0 || currentStep > mStepCount) {
            throw new IllegalArgumentException("Invalid step value " + currentStep);
        }

        mPreviousStep = mCurrentStep;
        mCurrentStep = currentStep;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Cancel any running animations
            if (mAnimatorSet != null) {
                mAnimatorSet.cancel();
            }

            mAnimatorSet = null;
            mLineAnimator = null;
            mIndicatorAnimator = null;

            // TODO: 05/08/16 handle cases where steps are skipped - need to animate all of them

            if (currentStep == mPreviousStep + 1) {
                // Going to next step
                mAnimatorSet = new AnimatorSet();

                // First, draw line to new
                mLineAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animProgress", 1.0f, 0.0f);

                // Same time, pop check mark
                mCheckAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animCheckRadius", mIndicatorRadius,
                                                        mCheckRadius * EXPAND_MARK, mCheckRadius);

                // Finally, pop current step indicator
                animIndicatorRadius = 0;
                mIndicatorAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animIndicatorRadius", 0f,
                                                            mIndicatorRadius * 1.4f, mIndicatorRadius);

                mAnimatorSet.play(mLineAnimator).with(mCheckAnimator).before(mIndicatorAnimator);
            } else if (currentStep == mPreviousStep - 1) {
                // Going back to previous step
                mAnimatorSet = new AnimatorSet();

                // First, pop out current step indicator
                mIndicatorAnimator =
                        ObjectAnimator.ofFloat(StepperIndicator.this, "animIndicatorRadius", mIndicatorRadius, 0f);

                // Then delete line
                animProgress = 1.0f;
                mLineDoneAnimatedPaint.setPathEffect(null);
                mLineAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animProgress", 0.0f, 1.0f);

                // Finally, pop out check mark to display step indicator
                animCheckRadius = mCheckRadius;
                mCheckAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animCheckRadius", mCheckRadius,
                                                        mShowStepTextNumber ? 0f : mIndicatorRadius);

                mAnimatorSet.playSequentially(mIndicatorAnimator, mLineAnimator, mCheckAnimator);
            }

            if (mAnimatorSet != null) {
                // Max 500 ms for the animation
                mLineAnimator.setDuration(Math.min(500, mAnimDuration));
                mLineAnimator.setInterpolator(new DecelerateInterpolator());
                // Other animations will run 2 times faster that line animation
                mIndicatorAnimator.setDuration(mLineAnimator.getDuration() / 2);
                mCheckAnimator.setDuration(mLineAnimator.getDuration() / 2);

                mAnimatorSet.start();
            }
        }

        invalidate();
    }

    /**
     * <p>
     * Setter method for the animation progress.
     * </p>
     * <font color="red">DO NOT CALL, DELETE OR RENAME</font>: Will be used by animation.
     */
    @SuppressWarnings("unused")
    public void setAnimProgress(float animProgress) {
        this.animProgress = animProgress;
        mLineDoneAnimatedPaint.setPathEffect(createPathEffect(mLineLength, animProgress, 0.0f));
        invalidate();
    }

    /**
     * <p>
     * Setter method for the indicator radius animation.
     * </p>
     * <font color="red">DO NOT CALL, DELETE OR RENAME</font>: Will be used by animation.
     */
    @SuppressWarnings("unused")
    public void setAnimIndicatorRadius(float animIndicatorRadius) {
        this.animIndicatorRadius = animIndicatorRadius;
        invalidate();
    }

    /**
     * <p>
     * Setter method for the checkmark radius animation.
     * </p>
     * <font color="red">DO NOT CALL, DELETE OR RENAME</font>: Will be used by animation.
     */
    @SuppressWarnings("unused")
    public void setAnimCheckRadius(float animCheckRadius) {
        this.animCheckRadius = animCheckRadius;
        invalidate();
    }

    /**
     * Set the {@link ViewPager} associated with this widget indicator.
     *
     * @param pager
     *         {@link ViewPager} to attach
     */
    @SuppressWarnings("unused")
    public void setViewPager(ViewPager pager) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        setViewPager(pager, pager.getAdapter().getCount());
    }

    /**
     * Set the {@link ViewPager} associated with this widget indicator.
     *
     * @param pager
     *         {@link ViewPager} to attach
     * @param keepLastPage
     *         {@code true} if the widget should not create an indicator for the last page, to use it as the final page
     */
    public void setViewPager(ViewPager pager, boolean keepLastPage) {
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        setViewPager(pager, pager.getAdapter().getCount() - (keepLastPage ? 1 : 0));
    }

    /**
     * Set the {@link ViewPager} associated with this widget indicator.
     *
     * @param pager
     *         {@link ViewPager} to attach
     * @param stepCount
     *         The real page count to display (use this if you are using looped viewpager to indicate the real number
     *         of pages)
     */
    public void setViewPager(ViewPager pager, int stepCount) {
        if (mPager == pager) {
            return;
        }
        if (mPager != null) {
            pager.removeOnPageChangeListener(this);
        }
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

        mPager = pager;
        mStepCount = stepCount;
        mCurrentStep = 0;
        pager.addOnPageChangeListener(this);
        invalidate();
    }

    /**
     * Add the {@link OnStepClickListener} to the list of listeners which will receive events when an step is clicked.
     *
     * @param listener
     *         The {@link OnStepClickListener} which will be added
     */
    public void addOnStepClickListener(OnStepClickListener listener) {
        mOnStepClickListeners.add(listener);
    }

    /**
     * Remove the specified {@link OnStepClickListener} from the list of listeners which will receive events when an
     * step is clicked.
     *
     * @param listener
     *         The {@link OnStepClickListener} which will be removed
     */
    @SuppressWarnings("unused")
    public void removeOnStepClickListener(OnStepClickListener listener) {
        mOnStepClickListeners.remove(listener);
    }

    /**
     * Remove all {@link OnStepClickListener} listeners from the StepperIndicator widget.
     * <br/>
     * No more events will be propagated.
     */
    @SuppressWarnings("unused")
    public void clearOnStepClickListeners() {
        mOnStepClickListeners.clear();
    }

    /**
     * Check if the widget has any valid {@link OnStepClickListener} listener set for receiving events from the steps.
     *
     * @return {@code true} if there are listeners registered, {@code false} otherwise.
     */
    public boolean isOnStepClickListenerAvailable() {
        return null != mOnStepClickListeners && !mOnStepClickListeners.isEmpty();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        /* no-op */
    }

    @Override
    public void onPageSelected(int position) {
        setCurrentStep(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        /* no-op */
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        // Try to restore the current step
        mCurrentStep = savedState.mCurrentStep;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        // Store current stop so that it can be resumed when restored
        savedState.mCurrentStep = mCurrentStep;
        return savedState;
    }

    /**
     * Contract used by the StepperIndicator widget to notify any listener of steps interaction events.
     */
    public interface OnStepClickListener {
        /**
         * Step was clicked
         *
         * @param step
         *         The step position which was clicked. (starts from 0, as the ViewPager bound to the widget)
         */
        void onStepClicked(int step);
    }

    /**
     * Saved state in which information about the state of the widget is stored.
     * <p>
     * Use this whenever you want to store or restore some information about the state of the widget.
     * </p>
     */
    private static class SavedState extends BaseSavedState {

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
        private int mCurrentStep;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            mCurrentStep = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mCurrentStep);
        }
    }
}
