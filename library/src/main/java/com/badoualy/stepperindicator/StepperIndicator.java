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
 * <p>Step indicator that can be used with (or without) a {@link ViewPager} to display current progress through an Onboarding or any process in multiple steps. </p>
 * <p> The default main primary color if not specified in the XML attributes will use the theme primary color defined via {@code colorPrimary} attribute. </p>
 * <p> If this view is used on a device below API 11, animations will not be used. </p>
 * <p> Usage of stepper custom attributes: </p>
 * <table summary="Attributes"><thead>
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
 * <p> Updated by Ionut Negru on 08/08/16 to add the stepClickListener feature.</p>
 * <p> Updated by Ionut Negru on 09/08/16 to add support for customizations like: multiple colors, step text number, bottom indicator.</p>
 */
@SuppressWarnings("unused")
public class StepperIndicator extends View implements ViewPager.OnPageChangeListener {

    private static final String TAG = "StepperIndicator";

    /** Duration of the line drawing animation (ms) */
    private static final int DEFAULT_ANIMATION_DURATION = 250;
    /** Max multiplier of the radius when a step is being animated to the "done" state before going to it's normal radius */
    private static final float EXPAND_MARK = 1.3f;

    private static final int STEP_INVALID = -1;

    /** Paint used to draw circle */
    private Paint circlePaint;
    /** List of {@link Paint} objects used to draw the circle for each step. */
    private List<Paint> stepsCirclePaintList;

    /**
     * The radius for the circle which describes an step.
     * <p>
     * This is either declared via XML or default is used.
     * </p>
     */
    private float circleRadius;

    /**
     * <p>
     * Flag indicating if the steps should be displayed with an number instead of empty circles and current animated
     * with bullet.
     * </p>
     */
    private boolean showStepTextNumber;

    /** Paint used to draw the number indicator for all steps. */
    private Paint stepTextNumberPaint;

    /** List of {@link Paint} objects used to draw the number indicator for each step. */
    private List<Paint> stepsTextNumberPaintList;

    /** Paint used to draw the indicator circle for the current and cleared steps */
    private Paint indicatorPaint;

    /**
     * List of {@link Paint} objects used by each step indicating the current and cleared steps.
     * <p> If this is set, it will override the default. </p>
     */
    private List<Paint> stepsIndicatorPaintList;

    /** Paint used to draw the line between steps - as default. */
    private Paint linePaint;

    /** Paint used to draw the line between steps when done. */
    private Paint lineDonePaint;

    /** Paint used to draw the line between steps when animated. */
    private Paint lineDoneAnimatedPaint;

    /** List of {@link Path} for each line between steps */
    private List<Path> linePathList = new ArrayList<>();

    /**
     * The progress of the animation.
     * DO NOT DELETE OR RENAME: Will be used by animations logic.
     */
    @SuppressWarnings("unused")
    private float animProgress;
    /**
     * The radius for the animated indicator.
     * DO NOT DELETE OR RENAME: Will be used by animations logic.
     */
    private float animIndicatorRadius;
    /**
     * The radius for the animated check mark.
     * DO NOT DELETE OR RENAME: Will be used by animations logic.
     */
    private float animCheckRadius;

    /**
     * Flag indicating if the indicator for the current step should be displayed at the bottom.
     * <p>
     * This is useful if you want to use text number indicator for the steps as the bullet indicator will be
     * disabled for that flow.
     * </p>
     */
    private boolean useBottomIndicator;
    /** The top margin of the bottom indicator. */
    private float bottomIndicatorMarginTop = 0;
    /** The width of the bottom indicator. */
    private float bottomIndicatorWidth = 0;
    /** The height of the bottom indicator. */
    private float bottomIndicatorHeight = 0;
    /** Flag indicating if the bottom indicator should use the same colors as the steps. */
    private boolean useBottomIndicatorWithStepColors;

    /** "Constant" size of the lines between steps */
    private float lineLength;

    // Values retrieved from xml (or default values)
    private float checkRadius;
    private float indicatorRadius;
    private float lineMargin;
    private int animDuration;

    /**
     * Custom step click listener which will notify any component which sets an listener of any events (touch events)
     * that happen regarding the steps widget.
     */
    private List<OnStepClickListener> onStepClickListeners = new ArrayList<>(0);
    /** Click areas for each of the steps supported by the StepperIndicator widget. */
    private List<RectF> stepsClickAreas;

    /** The gesture detector at which all the touch events will be propagated to. */
    private GestureDetector gestureDetector;
    private int stepCount;
    private int currentStep;
    private int previousStep;

    // X position of each step indicator's center
    private float[] indicators;
    // Utils to avoid object instanciation during onDraw
    private Rect stepAreaRect = new Rect();
    private RectF stepAreaRectF = new RectF();

    private ViewPager pager;
    private Bitmap doneIcon;
    private boolean showDoneIcon;

    // Running animations
    private AnimatorSet animatorSet;
    private ObjectAnimator lineAnimator, indicatorAnimator, checkAnimator;

    /**
     * Custom gesture listener though which all the touch events are propagated.
     * <p>
     * The whole purpose of this listener is to correctly detect which step was touched by the user and notify
     * the component which registered to receive event updates through
     * {@link #addOnStepClickListener(OnStepClickListener)}
     * </p>
     */
    private GestureDetector.OnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            float xCord = e.getX();
            float yCord = e.getY();

            int clickedStep = STEP_INVALID;
            if (isOnStepClickListenerAvailable()) {
                for (int i = 0; i < stepsClickAreas.size(); i++) {
                    if (stepsClickAreas.get(i).contains(xCord, yCord)) {
                        clickedStep = i;
                        // Stop as we found the step which was clicked
                        break;
                    }
                }
            }

            // If the clicked step is valid and an listener was setup - send the event
            if (clickedStep != STEP_INVALID) {
                for (OnStepClickListener listener : onStepClickListeners) {
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

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        // Create a PathEffect to set on a Paint to only draw some part of the line
        return new DashPathEffect(new float[]{pathLength, pathLength}, Math.max(phase * pathLength, offset));
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

        circlePaint = new Paint();
        circlePaint.setStrokeWidth(a.getDimension(R.styleable.StepperIndicator_stpi_circleStrokeWidth, defaultCircleStrokeWidth));
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_circleColor, defaultCircleColor));
        circlePaint.setAntiAlias(true);

        // Call this as early as possible as other properties are configured based on the number of steps
        setStepCount(a.getInteger(R.styleable.StepperIndicator_stpi_stepCount, 2));

        final int stepsCircleColorsResId = a.getResourceId(R.styleable.StepperIndicator_stpi_stepsCircleColors, 0);
        if (stepsCircleColorsResId != 0) {
            stepsCirclePaintList = new ArrayList<>(stepCount);

            for (int i = 0; i < stepCount; i++) {
                // Based on the main indicator paint object, we create the customized one
                Paint circlePaint = new Paint(this.circlePaint);
                if (isInEditMode()) {
                    // Fallback for edit mode - to show something in the preview
                    circlePaint.setColor(getRandomColor());
                } else {
                    // Get the array of attributes for the colors
                    TypedArray colorResValues = context.getResources().obtainTypedArray(stepsCircleColorsResId);

                    if (stepCount > colorResValues.length()) {
                        throw new IllegalArgumentException(
                                "Invalid number of colors for the circles. Please provide a list " +
                                        "of colors with as many items as the number of steps required!");
                    }

                    circlePaint.setColor(colorResValues.getColor(i, 0)); // specific color
                    // No need for the array anymore, recycle it
                    colorResValues.recycle();
                }

                stepsCirclePaintList.add(circlePaint);
            }
        }

        indicatorPaint = new Paint(circlePaint);
        indicatorPaint.setStyle(Paint.Style.FILL);
        indicatorPaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_indicatorColor, defaultIndicatorColor));
        indicatorPaint.setAntiAlias(true);

        stepTextNumberPaint = new Paint(indicatorPaint);
        stepTextNumberPaint.setTextSize(getResources().getDimension(R.dimen.stpi_default_text_size));

        showStepTextNumber = a.getBoolean(R.styleable.StepperIndicator_stpi_showStepNumberInstead, false);

        // Get the resource from the context style properties
        final int stepsIndicatorColorsResId = a.getResourceId(R.styleable.StepperIndicator_stpi_stepsIndicatorColors, 0);
        if (stepsIndicatorColorsResId != 0) {
            // init the list of colors with the same size as the number of steps
            stepsIndicatorPaintList = new ArrayList<>(stepCount);
            if (showStepTextNumber) {
                stepsTextNumberPaintList = new ArrayList<>(stepCount);
            }

            for (int i = 0; i < stepCount; i++) {
                Paint indicatorPaint = new Paint(this.indicatorPaint);

                Paint textNumberPaint = showStepTextNumber ? new Paint(stepTextNumberPaint) : null;
                if (isInEditMode()) {
                    // Fallback for edit mode - to show something in the preview

                    indicatorPaint.setColor(getRandomColor()); // random color
                    if (null != textNumberPaint) {
                        textNumberPaint.setColor(indicatorPaint.getColor());
                    }
                } else {
                    // Get the array of attributes for the colors
                    TypedArray colorResValues = context.getResources().obtainTypedArray(stepsIndicatorColorsResId);

                    if (stepCount > colorResValues.length()) {
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

                stepsIndicatorPaintList.add(indicatorPaint);
                if (showStepTextNumber && null != textNumberPaint) {
                    stepsTextNumberPaintList.add(textNumberPaint);
                }
            }
        }

        linePaint = new Paint();
        linePaint.setStrokeWidth(a.getDimension(R.styleable.StepperIndicator_stpi_lineStrokeWidth, defaultLineStrokeWidth));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineColor, defaultLineColor));
        linePaint.setAntiAlias(true);

        lineDonePaint = new Paint(linePaint);
        lineDonePaint.setColor(a.getColor(R.styleable.StepperIndicator_stpi_lineDoneColor, defaultLineDoneColor));

        lineDoneAnimatedPaint = new Paint(lineDonePaint);

        // Check if we should use the bottom indicator instead of the bullet one
        useBottomIndicator = a.getBoolean(R.styleable.StepperIndicator_stpi_useBottomIndicator, false);
        if (useBottomIndicator) {
            // Get the default height(stroke width) for the bottom indicator
            float defaultHeight = resources.getDimension(R.dimen.stpi_default_bottom_indicator_height);

            bottomIndicatorHeight = a.getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorHeight, defaultHeight);

            if (bottomIndicatorHeight <= 0) {
                Log.d(TAG, "init: Invalid indicator height, disabling bottom indicator feature! Please provide " +
                        "a value greater than 0.");
                useBottomIndicator = false;
            }

            // Get the default width for the bottom indicator
            float defaultWidth = resources.getDimension(R.dimen.stpi_default_bottom_indicator_width);
            bottomIndicatorWidth = a.getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorWidth, defaultWidth);

            // Get the default top margin for the bottom indicator
            float defaultTopMargin = resources.getDimension(R.dimen.stpi_default_bottom_indicator_margin_top);
            bottomIndicatorMarginTop = a.getDimension(R.styleable.StepperIndicator_stpi_bottomIndicatorMarginTop, defaultTopMargin);

            useBottomIndicatorWithStepColors = a.getBoolean(R.styleable.StepperIndicator_stpi_useBottomIndicatorWithStepColors, false);
        }

        circleRadius = a.getDimension(R.styleable.StepperIndicator_stpi_circleRadius, defaultCircleRadius);
        checkRadius = circleRadius + circlePaint.getStrokeWidth() / 2f;
        indicatorRadius = a.getDimension(R.styleable.StepperIndicator_stpi_indicatorRadius, defaultIndicatorRadius);
        animIndicatorRadius = indicatorRadius;
        animCheckRadius = checkRadius;
        lineMargin = a.getDimension(R.styleable.StepperIndicator_stpi_lineMargin, defaultLineMargin);

        animDuration = a.getInteger(R.styleable.StepperIndicator_stpi_animDuration, DEFAULT_ANIMATION_DURATION);
        showDoneIcon = a.getBoolean(R.styleable.StepperIndicator_stpi_showDoneIcon, true);

        a.recycle();

        if (showDoneIcon) {
            doneIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_done_white_18dp);
        }

        // Display at least 1 cleared step for preview in XML editor
        if (isInEditMode()) {
            currentStep = Math.max((int) Math.ceil(stepCount / 2f), 1);
        }
	if (isInEditMode()) return; // this will ensure that the view will render properly on the Layout Editor.
        // Initialize the gesture detector, setup with our custom gesture listener
        gestureDetector = new GestureDetector(getContext(), gestureListener);
    }

    /**
     * Get an random color {@link Paint} object.
     *
     * @return {@link Paint} object with the same attributes as {@link #circlePaint} and with an random color.
     * @see #circlePaint
     * @see #getRandomColor()
     */
    private Paint getRandomPaint() {
        Paint paint = new Paint(indicatorPaint);
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
        gestureDetector.onTouchEvent(event);
        return true; // we handle the event in the gesture detector
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        compute(); // for setting up the indicator based on the new position
    }

    /**
     * Make calculations for establishing the exact positions of each step component, for the line dividers, for
     * bottom indicators, etc.
     * <p>
     * Call this whenever there is an layout change for the widget.
     * </p>
     */
    private void compute() {
        if (null == circlePaint) {
            throw new IllegalArgumentException("circlePaint is invalid! Make sure you setup the field circlePaint " +
                                                       "before calling compute() method!");
        }

        indicators = new float[stepCount];
        linePathList.clear();

        float startX = circleRadius * EXPAND_MARK + circlePaint.getStrokeWidth() / 2f;
        if (useBottomIndicator) {
            startX += bottomIndicatorWidth / 2 - startX;
        }

        // Compute position of indicators and line length
        float divider = (getMeasuredWidth() - startX * 2f) / (stepCount - 1);
        lineLength = divider - (circleRadius * 2f + circlePaint.getStrokeWidth()) - (lineMargin * 2);

        // Compute position of circles and lines once
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = startX + divider * i;
        }
        for (int i = 0; i < indicators.length - 1; i++) {
            float position = ((indicators[i] + indicators[i + 1]) / 2) - lineLength / 2;
            final Path linePath = new Path();
            float lineY = getStepCenterY();
            linePath.moveTo(position, lineY);
            linePath.lineTo(position + lineLength, lineY);
            linePathList.add(linePath);
        }

        computeStepsClickAreas(); // update the position of the steps click area also
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
        if (stepCount == STEP_INVALID) {
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
            float left = indicator - circleRadius * 2;
            float right = indicator + circleRadius * 2;
            float top = getStepCenterY() - circleRadius * 2;
            float bottom = getStepCenterY() + circleRadius + getBottomIndicatorHeight();

            // Store the click area for the step
            RectF area = new RectF(left, top, right, bottom);
            stepsClickAreas.add(area);
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
        if (useBottomIndicator) {
            return (int) (bottomIndicatorHeight + bottomIndicatorMarginTop);
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
        boolean inAnimation = animatorSet != null && animatorSet.isRunning();
        boolean inLineAnimation = lineAnimator != null && lineAnimator.isRunning();
        boolean inIndicatorAnimation = indicatorAnimator != null && indicatorAnimator.isRunning();
        boolean inCheckAnimation = checkAnimator != null && checkAnimator.isRunning();

        boolean drawToNext = previousStep == currentStep - 1;
        boolean drawFromNext = previousStep == currentStep + 1;

        for (int i = 0; i < indicators.length; i++) {
            final float indicator = indicators[i];

            // We draw the "done" check if previous step, or if we are going back (if going back, animated value will reduce radius to 0)
            boolean drawCheck = i < currentStep || (drawFromNext && i == currentStep);

            // Draw back circle
            canvas.drawCircle(indicator, centerY, circleRadius, getStepCirclePaint(i));

            // Draw the step number inside the back circle if the flag for this is set to true
            if (showStepTextNumber) {
                final String stepLabel = String.valueOf(i + 1);

                stepAreaRect.set((int) (indicator - circleRadius), (int) (centerY - circleRadius),
                                 (int) (indicator + circleRadius), (int) (centerY + circleRadius));
                stepAreaRectF.set(stepAreaRect);

                Paint stepTextNumberPaint = getStepTextNumberPaint(i);

                // measure text width
                stepAreaRectF.right = stepTextNumberPaint.measureText(stepLabel, 0, stepLabel.length());
                // measure text height
                stepAreaRectF.bottom = stepTextNumberPaint.descent() - stepTextNumberPaint.ascent();

                stepAreaRectF.left += (stepAreaRect.width() - stepAreaRectF.right) / 2.0f;
                stepAreaRectF.top += (stepAreaRect.height() - stepAreaRectF.bottom) / 2.0f;

                canvas.drawText(stepLabel, stepAreaRectF.left, stepAreaRectF.top - stepTextNumberPaint.ascent(), stepTextNumberPaint);
            }

            if (useBottomIndicator) {
                // Show the current step indicator as bottom line
                if (i == currentStep) {
                    // Draw custom indicator for current step only
                    canvas.drawRect(indicator - bottomIndicatorWidth / 2, getHeight() - bottomIndicatorHeight,
                                    indicator + bottomIndicatorWidth / 2, getHeight(),
                                    useBottomIndicatorWithStepColors ? getStepIndicatorPaint(i) : indicatorPaint);
                }
            } else {
                // Show the current step indicator as bullet
                // If current step, or coming back from next step and still animating
                if ((i == currentStep && !drawFromNext) || (i == previousStep && drawFromNext && inAnimation)) {
                    // Draw animated indicator
                    canvas.drawCircle(indicator, centerY, animIndicatorRadius, getStepIndicatorPaint(i));
                }
            }

            // Draw check mark
            if (drawCheck) {
                float radius = checkRadius;
                // Use animated radius value?
                if ((i == previousStep && drawToNext) || (i == currentStep && drawFromNext)) radius = animCheckRadius;
                canvas.drawCircle(indicator, centerY, radius, getStepIndicatorPaint(i));

                // Draw check bitmap
                if (!isInEditMode() && showDoneIcon) {
                    if ((i != previousStep && i != currentStep) ||
                            (!inCheckAnimation && !(i == currentStep && !inAnimation))) {
                        canvas.drawBitmap(doneIcon, indicator - (doneIcon.getWidth() / 2),
                                          centerY - (doneIcon.getHeight() / 2), null);
                    }
                }
            }

            // Draw lines
            if (i < linePathList.size()) {
                if (i >= currentStep) {
                    canvas.drawPath(linePathList.get(i), linePaint);
                    if (i == currentStep && drawFromNext && (inLineAnimation || inIndicatorAnimation)) {
                        // Coming back from n+1
                        canvas.drawPath(linePathList.get(i), lineDoneAnimatedPaint);
                    }
                } else {
                    if (i == currentStep - 1 && drawToNext && inLineAnimation) {
                        // Going to n+1
                        canvas.drawPath(linePathList.get(i), linePaint);
                        canvas.drawPath(linePathList.get(i), lineDoneAnimatedPaint);
                    } else {
                        canvas.drawPath(linePathList.get(i), lineDonePaint);
                    }
                }
            }
        }
    }

    /**
     * Get the {@link Paint} object which should be used for displaying the current step indicator.
     *
     * @param stepPosition The step position for which to retrieve the {@link Paint} object
     * @return The {@link Paint} object for the specified step position
     */
    private Paint getStepIndicatorPaint(final int stepPosition) {
        return getPaint(stepPosition, stepsIndicatorPaintList, indicatorPaint);
    }

    /**
     * Get the {@link Paint} object which should be used for drawing the text number the current step.
     *
     * @param stepPosition The step position for which to retrieve the {@link Paint} object
     * @return The {@link Paint} object for the specified step position
     */
    private Paint getStepTextNumberPaint(final int stepPosition) {
        return getPaint(stepPosition, stepsTextNumberPaintList, stepTextNumberPaint);
    }

    /**
     * Get the {@link Paint} object which should be used for drawing the circle for the step.
     *
     * @param stepPosition The step position for which to retrieve the {@link Paint} object
     * @return The {@link Paint} object for the specified step position
     */
    private Paint getStepCirclePaint(final int stepPosition) {
        return getPaint(stepPosition, stepsCirclePaintList, circlePaint);
    }

    /**
     * Get the {@link Paint} object based on the step position and the source list of {@link Paint} objects.
     * <p>
     * If none found, will try to use the provided default. If not valid also, an random {@link Paint} object
     * will be returned instead.
     * </p>
     *
     * @param stepPosition The step position for which the {@link Paint} object is needed
     * @param sourceList   The source list of {@link Paint} object.
     * @param defaultPaint The default {@link Paint} object which will be returned if the source list does not
     *                     contain the specified step.
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
                Log.d(TAG, "getPaint: could not find the specific step paint to use! Try to use default instead!");
            }
        }

        if (null == paint && null != defaultPaint) {
            // Try to use the default
            paint = defaultPaint;
        }

        if (null == paint) {
            Log.d(TAG, "getPaint: could not use default paint for the specific step! Using random Paint instead!");
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
     * @param stepPos The step position to verify
     * @return {@code true} if the step is valid, otherwise it will throw an exception.
     */
    private boolean isStepValid(final int stepPos) {
        if (stepPos < 0 || stepPos > stepCount - 1) {
            throw new IllegalArgumentException("Invalid step position. " + stepPos + " is not a valid position! it " +
                                                       "should be between 0 and stepCount(" + stepCount + ")");
        }

        return true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Compute the necessary height for the widget
        int desiredHeight = (int) Math.ceil((circleRadius * EXPAND_MARK * 2) + circlePaint.getStrokeWidth() +
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
        return stepCount;
    }

    public void setStepCount(int stepCount) {
        if (stepCount < 2) {
            throw new IllegalArgumentException("stepCount must be >= 2");
        }

        this.stepCount = stepCount;
        currentStep = 0;
        compute();
        invalidate();
    }

    @SuppressWarnings("unused")
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
        if (currentStep < 0 || currentStep > stepCount) {
            throw new IllegalArgumentException("Invalid step value " + currentStep);
        }

        previousStep = this.currentStep;
        this.currentStep = currentStep;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // Cancel any running animations
            if (animatorSet != null) {
                animatorSet.cancel();
            }

            animatorSet = null;
            lineAnimator = null;
            indicatorAnimator = null;

            // TODO: 05/08/16 handle cases where steps are skipped - need to animate all of them

            if (currentStep == previousStep + 1) {
                // Going to next step
                animatorSet = new AnimatorSet();

                // First, draw line to new
                lineAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animProgress", 1.0f, 0.0f);

                // Same time, pop check mark
                checkAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animCheckRadius", indicatorRadius,
                                                       checkRadius * EXPAND_MARK, checkRadius);

                // Finally, pop current step indicator
                animIndicatorRadius = 0;
                indicatorAnimator = ObjectAnimator.ofFloat(StepperIndicator.this, "animIndicatorRadius", 0f,
                                                           indicatorRadius * 1.4f, indicatorRadius);

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

    /**
     * <p>
     * Setter method for the animation progress.
     * </p>
     * <font color="red">DO NOT CALL, DELETE OR RENAME</font>: Will be used by animation.
     */
    @SuppressWarnings("unused")
    public void setAnimProgress(float animProgress) {
        this.animProgress = animProgress;
        lineDoneAnimatedPaint.setPathEffect(createPathEffect(lineLength, animProgress, 0.0f));
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
     * @param pager {@link ViewPager} to attach
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
     * @param pager        {@link ViewPager} to attach
     * @param keepLastPage {@code true} if the widget should not create an indicator for the last page, to use it as the final page
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
     * @param pager     {@link ViewPager} to attach
     * @param stepCount The real page count to display (use this if you are using looped viewpager to indicate the real number
     *                  of pages)
     */
    public void setViewPager(ViewPager pager, int stepCount) {
        if (this.pager == pager) {
            return;
        }
        if (this.pager != null) {
            pager.removeOnPageChangeListener(this);
        }
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }

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
    @SuppressWarnings("unused")
    public void removeOnStepClickListener(OnStepClickListener listener) {
        onStepClickListeners.remove(listener);
    }

    /**
     * Remove all {@link OnStepClickListener} listeners from the StepperIndicator widget.
     * No more events will be propagated.
     */
    @SuppressWarnings("unused")
    public void clearOnStepClickListeners() {
        onStepClickListeners.clear();
    }

    /**
     * Check if the widget has any valid {@link OnStepClickListener} listener set for receiving events from the steps.
     *
     * @return {@code true} if there are listeners registered, {@code false} otherwise.
     */
    public boolean isOnStepClickListenerAvailable() {
        return null != onStepClickListeners && !onStepClickListeners.isEmpty();
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
        currentStep = savedState.mCurrentStep;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        // Store current stop so that it can be resumed when restored
        savedState.mCurrentStep = currentStep;
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
