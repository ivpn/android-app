package net.ivpn.client.ui.protocol.view;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import net.ivpn.client.R;

public class ValueSelectionView extends View {

    private static final int MAX_VALUE = 30;
    private static final int MIN_VALUE = 1;

    private float height;
    private float actionWidth;
    private float valueFieldWidth;
    private float iconSize;
    private float roundRadius;

    private int value;

    private Paint actionPaint;
    private Paint textPaint;

    private RectUtils rectUtils;
    private OnValueChangeListener listener;

    private GestureDetector gestureDetector;

    private Drawable incrementDrawable;
    private Drawable decrementDrawable;

    public ValueSelectionView(Context context) {
        super(context);
        init();
    }

    public ValueSelectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ValueSelectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ValueSelectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Resources resources = getResources();

        actionPaint = new Paint();
        actionPaint.setStyle(Paint.Style.FILL);
        actionPaint.setColor(resources.getColor(R.color.colorPrimary));

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(resources.getDimension(R.dimen.regeneration_value_text_size));

        Drawable addNDrawable = resources.getDrawable(R.drawable.ic_add);
        incrementDrawable = DrawableCompat.wrap(addNDrawable);
        DrawableCompat.setTint(incrementDrawable, Color.WHITE);

        Drawable removeNDrawable = resources.getDrawable(R.drawable.ic_remove);
        decrementDrawable = DrawableCompat.wrap(removeNDrawable);
        DrawableCompat.setTint(decrementDrawable, Color.WHITE);

        gestureDetector = new GestureDetector(this.getContext(), getGestureDetector());
        setOnTouchListener((view, motionEvent) -> gestureDetector.onTouchEvent(motionEvent));
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        rectUtils = new RectUtils(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawDecrementAction(canvas);
        drawValue(canvas);
        drawIncrementAction(canvas);
    }

    private void drawIncrementAction(Canvas canvas) {
        RectF rectF = rectUtils.getIncrementRectF();
        float paddingHorizontal = (rectF.width() - iconSize) / 2;
        float paddingVertical = (rectF.height() - iconSize) / 2;

        Path path = rectUtils.RoundedRect(rectF.left,
                (int) rectF.top,
                (int) rectF.right,
                (int) rectF.bottom,
                roundRadius, roundRadius,
                false, true, true, false);
        canvas.drawPath(path, actionPaint);

        incrementDrawable.setBounds((int) (rectF.left + paddingHorizontal),
                (int) (rectF.top + paddingVertical),
                (int) (rectF.right - paddingHorizontal),
                (int) (rectF.bottom - paddingVertical));
        incrementDrawable.draw(canvas);
    }

    private void drawValue(Canvas canvas) {
        RectF rectF = rectUtils.getValueRectF();
        Rect valueRect = new Rect();

        String text = String.valueOf(value);
        textPaint.getTextBounds(text, 0, text.length(), valueRect);

        canvas.drawText(text,
                rectF.left + (rectF.width() - valueRect.width()) / 2f,
                rectF.bottom - (rectF.height() - valueRect.height()) / 2f,
                textPaint);
    }

    private void drawDecrementAction(Canvas canvas) {
        RectF rectF = rectUtils.getDecrementRectF();
        float paddingHorizontal = (rectF.width() - iconSize) / 2;
        float paddingVertical = (rectF.height() - iconSize) / 2;

        Path path = rectUtils.RoundedRect(rectF.left,
                (int) rectF.top,
                (int) rectF.right,
                (int) rectF.bottom,
                roundRadius, roundRadius,
                true, false, false, true);
        canvas.drawPath(path, actionPaint);

        decrementDrawable.setBounds((int) (rectF.left + paddingHorizontal),
                (int) (rectF.top + paddingVertical),
                (int) (rectF.right - paddingHorizontal),
                (int) (rectF.bottom - paddingVertical));
        decrementDrawable.draw(canvas);
    }

    public void setValue(int value) {
        this.value = value;
        invalidate();
    }

    public void setOnValueChangedListener(OnValueChangeListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Resources resources = getResources();

        height = resources.getDimension(R.dimen.regeneration_view_height);
        actionWidth = resources.getDimension(R.dimen.regeneration_view_action_width);
        valueFieldWidth = resources.getDimension(R.dimen.regeneration_view_value_field_width);
        iconSize = resources.getDimension(R.dimen.regeneration_icon_size);
        roundRadius = getResources().getDimension(R.dimen.regeneration_view_round_radius);

        int width = (int) (2 * actionWidth + valueFieldWidth);
        setMeasuredDimension(width, (int) height);
    }

    private void handleSingleTap(MotionEvent event) {
        RectF incrementRectF = rectUtils.getIncrementRectF();
        RectF decrementRectF = rectUtils.getDecrementRectF();

        if (incrementRectF.contains(event.getX(), event.getY())) {
            incrementValue();
        } else if (decrementRectF.contains(event.getX(), event.getY())) {
            decrementValue();
        }
    }

    private void incrementValue() {
        if (value == MAX_VALUE) {
            return;
        }

        value++;
        if (listener != null) {
            listener.onValueChanged(value);
        }
        invalidate();
    }

    private void decrementValue() {
        if (value == MIN_VALUE) {
            return;
        }

        value--;
        if (listener != null) {
            listener.onValueChanged(value);
        }
        invalidate();
    }

    private GestureDetector.SimpleOnGestureListener getGestureDetector() {
        return new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onSingleTapUp(MotionEvent event) {
                handleSingleTap(event);
                return true;
            }
        };
    }
}