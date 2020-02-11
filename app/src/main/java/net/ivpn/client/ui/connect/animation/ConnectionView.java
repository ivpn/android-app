package net.ivpn.client.ui.connect.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import net.ivpn.client.R;
import net.ivpn.client.ui.connect.ConnectionState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionView extends View {

    private static final String TAG = ConnectionView.class.getSimpleName();
    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectionView.class);

    private ConnectionAnimationState state;
    private ValueAnimator connectionProgressAnimator;
    private ValueAnimator disconnectionProgressAnimator;
    private ValueAnimator finalDisconnectionProgressAnimator;
    private ValueAnimator finalConnectionProgressAnimator;
    private ValueAnimator disconnectionAnimator;
    private ValueAnimator connectionAnimator;

    private Handler waveHandler;

    private Paint paintProgress;
    private Paint paintProgressBackground;
    private Paint paintCircleWave;
    private Paint paintCircleActive;
    private Paint paintCircleBackground;
    private Paint paintWave;
    private Paint paintPauseActive;

    private Drawable pauseBackgroundDrawable;
    private Drawable connectBackgroundDrawable;
    private Drawable stopBackgroundDrawable;

    private float progressAngle = 0;
    private float progressConnection = 0;
    private float progressWave = 0;
    private float strokeProgressWidth = 0;
    private boolean animationShouldBeFinished;
    private boolean shouldBeWaved;
    private boolean isInit;
    private boolean isPaused;

    public ConnectionView(Context context) {
        super(context);
        init();
    }

    public ConnectionView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ConnectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ConnectionView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        state = ConnectionAnimationState.NOT_CONNECTED;
        pauseBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.pause_round_btn);
        connectBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.connection_round_btn);
        stopBackgroundDrawable = ContextCompat.getDrawable(getContext(), R.drawable.connection_round_btn);
        waveHandler = new Handler();
    }

    private void initPaints(int width, int height) {
        RectF rect = AnimationUtils.getProgressRect(width, height);
        strokeProgressWidth = rect.width() / 17;

        paintProgress = new Paint();
        paintProgress.setAntiAlias(true);
        paintProgress.setStyle(Paint.Style.STROKE);
        paintProgress.setStrokeWidth(strokeProgressWidth);
        paintProgress.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_main));

        paintProgressBackground = new Paint();
        paintProgressBackground.setAntiAlias(true);
        paintProgressBackground.setStyle(Paint.Style.STROKE);
        paintProgressBackground.setStrokeWidth(strokeProgressWidth);
        paintProgressBackground.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_gray));

        paintWave = new Paint();
        paintWave.setAntiAlias(true);
        paintWave.setStyle(Paint.Style.STROKE);
        paintWave.setStrokeWidth(strokeProgressWidth);
        paintWave.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_main_opacity));

        paintCircleWave = new Paint();
        paintCircleWave.setAntiAlias(true);
        paintCircleWave.setStyle(Paint.Style.FILL);
        paintCircleWave.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_main));

        paintCircleBackground = new Paint();
        paintCircleBackground.setAntiAlias(true);
        paintCircleBackground.setStyle(Paint.Style.FILL);
        paintCircleBackground.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_background));

        paintCircleActive = new Paint();
        paintCircleActive.setAntiAlias(true);
        paintCircleActive.setStyle(Paint.Style.FILL);
        paintCircleActive.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_main));
        isInit = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        switch (state) {
            case NOT_CONNECTED:
                drawNotConnected(canvas);
                break;
            case CONNECTION_PROGRESS:
                drawConnectingProgress(canvas);
                break;
            case CONNECTING_ANIMATION:
                drawConnection(canvas);
                break;
            case CONNECTED:
                drawConnected(canvas);
                break;
            case DISCONNECTING_ANIMATION:
                drawDisconnection(canvas);
                break;
            case DISCONNECTING_PROGRESS:
                drawDisconnectingProgress(canvas);
                break;
        }
    }

    private void drawNotConnected(Canvas canvas) {
        RectF rect = AnimationUtils.getProgressRect(getWidth(), getHeight());
        canvas.drawArc(rect, AnimationUtils.START_ANGLE, AnimationUtils.FULL_CIRCLE, false, paintProgressBackground);

        int grayIconAlpha = AnimationUtils.MAX_ALPHA;
        getIcon(rect, R.color.color_animation_gray, grayIconAlpha).draw(canvas);
        Log.d(TAG, "drawNotConnected: isPaused = " + isPaused);
        if (isPaused) {
            if (stopBackgroundDrawable != null) {
                stopBackgroundDrawable.setBounds(AnimationUtils.getPauseBtnRect(rect));
                stopBackgroundDrawable.draw(canvas);
            }
            getStopIcon(AnimationUtils.getPauseBtnRect(rect), R.color.color_animation_white, AnimationUtils.MAX_ALPHA).draw(canvas);
        }
    }

    private void drawConnectingProgress(Canvas canvas) {
        RectF rect = AnimationUtils.getProgressRect(getWidth(), getHeight());
        canvas.drawArc(rect, AnimationUtils.START_ANGLE, AnimationUtils.FULL_CIRCLE, false, paintProgressBackground);

        int grayIconAlpha = AnimationUtils.MAX_ALPHA;
        getIcon(rect, R.color.color_animation_gray, grayIconAlpha).draw(canvas);

        float angle = Math.min(progressAngle * AnimationUtils.FULL_CIRCLE, AnimationUtils.FULL_CIRCLE);
        canvas.drawArc(rect, AnimationUtils.START_ANGLE, angle, false, paintProgress);
    }

    private void drawConnection(Canvas canvas) {
        RectF rect = AnimationUtils.getProgressRect(getWidth(), getHeight());
        int bigCircleRadius = getWidth();
        paintProgress.setAlpha((int) (AnimationUtils.MAX_ALPHA * (1 - progressConnection)));
        canvas.drawArc(rect, AnimationUtils.START_ANGLE, AnimationUtils.FULL_CIRCLE, false, paintProgress);

        paintCircleBackground.setAlpha((int) (AnimationUtils.MAX_ALPHA * progressConnection));
        canvas.drawCircle(rect.centerX(), rect.centerY(), bigCircleRadius * progressConnection, paintCircleBackground);
        paintCircleWave.setAlpha((int) (AnimationUtils.MAX_ALPHA * (1 - progressConnection) / 3));
        canvas.drawCircle(rect.centerX(), rect.centerY(), bigCircleRadius * progressConnection, paintCircleWave);

        Drawable connectedBtnBackground = ContextCompat.getDrawable(getContext(), R.drawable.connection_round_btn);
        if (connectedBtnBackground != null) {
            connectedBtnBackground.setBounds(AnimationUtils.getConnectedBtnRect(rect, progressConnection));
            connectedBtnBackground.draw(canvas);
        }

        int grayIconAlpha = (int) (AnimationUtils.MAX_ALPHA * (1 - progressConnection));
        getIcon(rect, R.color.color_animation_gray, grayIconAlpha).draw(canvas);

        int whiteIconAlpha = (int) (AnimationUtils.MAX_ALPHA * progressConnection);
        getIcon(rect, R.color.color_animation_white, whiteIconAlpha).draw(canvas);
    }

    private void drawConnected(Canvas canvas) {
        RectF rect = AnimationUtils.getProgressRect(getWidth(), getHeight());
        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.color_animation_background));
        if (shouldBeWaved) {
            drawWave(canvas, rect);
        }

        if (connectBackgroundDrawable != null) {
            connectBackgroundDrawable.setBounds(AnimationUtils.getConnectedBtnRect(rect, 1f));
            connectBackgroundDrawable.draw(canvas);
        }
        if (pauseBackgroundDrawable != null) {
            pauseBackgroundDrawable.setBounds(AnimationUtils.getPauseBtnRect(rect));
            pauseBackgroundDrawable.draw(canvas);
        }

        int iconAlpha = AnimationUtils.MAX_ALPHA;
        getIcon(rect, R.color.color_animation_white, iconAlpha).draw(canvas);
        getPauseIcon(AnimationUtils.getPauseBtnRect(rect), R.color.color_animation_white, iconAlpha).draw(canvas);
    }

    private void drawDisconnection(Canvas canvas) {
        RectF rect = AnimationUtils.getProgressRect(getWidth(), getHeight());
        int bigCircleRadius = getWidth();

        canvas.drawColor(ContextCompat.getColor(getContext(), R.color.color_animation_white));
        paintCircleWave.setAlpha((int) (AnimationUtils.MAX_ALPHA * (1 - progressConnection) / 3));
        canvas.drawCircle(rect.centerX(), rect.centerY(), bigCircleRadius * progressConnection, paintCircleWave);

        canvas.drawArc(rect, AnimationUtils.START_ANGLE, AnimationUtils.FULL_CIRCLE, false, paintProgress);

        float radius = rect.width() / 2;
        paintCircleActive.setAlpha(Math.max((int) (AnimationUtils.MAX_ALPHA * (1 - 1.3f * progressConnection)), 0));
        canvas.drawCircle(rect.centerX(), rect.centerY(), radius, paintCircleActive);

        int whiteIconAlpha = (int) (AnimationUtils.MAX_ALPHA * (1 - progressConnection));
        getIcon(rect, R.color.color_animation_white, whiteIconAlpha).draw(canvas);

        int grayIconAlpha = (int) (AnimationUtils.MAX_ALPHA * (progressConnection));
        getIcon(rect, R.color.color_animation_gray, grayIconAlpha).draw(canvas);

        if (isPaused) {
            if (stopBackgroundDrawable != null) {
                stopBackgroundDrawable.setBounds(AnimationUtils.getPauseBtnRect(rect));
                stopBackgroundDrawable.draw(canvas);
            }
            getStopIcon(AnimationUtils.getPauseBtnRect(rect), R.color.color_animation_white, AnimationUtils.MAX_ALPHA).draw(canvas);
        }
    }

    private void drawDisconnectingProgress(Canvas canvas) {
        RectF rect = AnimationUtils.getProgressRect(getWidth(), getHeight());
        paintProgress.setAlpha(AnimationUtils.MAX_ALPHA);
        canvas.drawArc(rect, AnimationUtils.START_ANGLE, AnimationUtils.FULL_CIRCLE, false, paintProgress);

        int grayIconAlpha = AnimationUtils.MAX_ALPHA;
        getIcon(rect, R.color.color_animation_gray, grayIconAlpha).draw(canvas);

        float angle = Math.min(progressAngle * AnimationUtils.FULL_CIRCLE, AnimationUtils.FULL_CIRCLE);
        paintProgressBackground.setAlpha(AnimationUtils.MAX_ALPHA);
        canvas.drawArc(rect, AnimationUtils.START_ANGLE, -angle, false, paintProgressBackground);

        if (isPaused) {
            if (stopBackgroundDrawable != null) {
                stopBackgroundDrawable.setBounds(AnimationUtils.getPauseBtnRect(rect));
                stopBackgroundDrawable.draw(canvas);
            }
            getStopIcon(AnimationUtils.getPauseBtnRect(rect), R.color.color_animation_white, AnimationUtils.MAX_ALPHA).draw(canvas);
        }
    }

    private Drawable getIcon(RectF rect, int tintColor, int alpha) {
        Drawable iconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_logo_sq);
        if (iconDrawable != null) {
            iconDrawable.setTint(ContextCompat.getColor(getContext(), tintColor));
            iconDrawable.setBounds(AnimationUtils.getIconRect(rect));
            iconDrawable.setAlpha(alpha);
        }

        return iconDrawable;
    }

    private Drawable getPauseIcon(Rect rect, int tintColor, int alpha) {
        Drawable iconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_pause);
        if (iconDrawable != null) {
            iconDrawable.setTint(ContextCompat.getColor(getContext(), tintColor));
            iconDrawable.setBounds(AnimationUtils.getActionIconRect(rect));
            iconDrawable.setAlpha(alpha);
        }

        return iconDrawable;
    }

    private Drawable getStopIcon(Rect rect, int tintColor, int alpha) {
        Drawable iconDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_stop);
        if (iconDrawable != null) {
            iconDrawable.setTint(ContextCompat.getColor(getContext(), tintColor));
            iconDrawable.setBounds(AnimationUtils.getActionIconRect(rect));
            iconDrawable.setAlpha(alpha);
        }

        return iconDrawable;
    }

    private void drawWave(Canvas canvas, RectF rect) {
        float basicRadius = rect.width() / 2;
        float strokeWidth = basicRadius / 5 - (2 * basicRadius * progressWave) / 15;
        float waveRadius = basicRadius * (1 + progressWave * 3) - strokeWidth / 2;
        paintWave.setStrokeWidth(strokeWidth);
        paintWave.setAlpha((int) (AnimationUtils.MAX_ALPHA * (1 - progressWave)));
        canvas.drawCircle(rect.centerX(), rect.centerY(), waveRadius, paintWave);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        initPaints(width, height);
    }

    private void startConnectionProgressAnimation() {
        if (!isInit) {
            return;
        }
        setupConnectionProgressAnimation();
        progressAngle = 0f;

        connectionProgressAnimator = ValueAnimator.ofFloat(0f, 0.87f);
        connectionProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        connectionProgressAnimator.setDuration(AnimationUtils.CONNECTION_PROGRESS_ANIMATION_DURATION);

        connectionProgressAnimator.addUpdateListener(valueAnimator -> {
            progressAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });

        connectionProgressAnimator.start();
    }

    private void startConnectionAnimation() {
        if (!isInit) {
            return;
        }
        connectionAnimator = ValueAnimator.ofFloat(0f, 1f);
        connectionAnimator.setDuration(AnimationUtils.CONNECT_ANIMATION_DURATION);
        connectionAnimator.setInterpolator(new DecelerateInterpolator(2f));
        connectionAnimator.addUpdateListener(valueAnimator -> {
            progressConnection = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        connectionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state = ConnectionAnimationState.CONNECTED;
                initWaveAnimation();
                invalidate();
            }
        });

        connectionAnimator.start();
    }

    private void startFinalConnectionProgressAnimation() {
        if (!isInit) {
            return;
        }
        if (connectionProgressAnimator != null && connectionProgressAnimator.isRunning()) {
            connectionProgressAnimator.cancel();
        }

        finalConnectionProgressAnimator = ValueAnimator.ofFloat(progressAngle, 1f);
        finalConnectionProgressAnimator.setDuration(AnimationUtils.FINAL_PROGRESS_ANIMATION_DURATION);

        finalConnectionProgressAnimator.addUpdateListener(valueAnimator -> {
            progressAngle = (float) valueAnimator.getAnimatedValue();
            invalidate();
        });
        finalConnectionProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state = ConnectionAnimationState.CONNECTING_ANIMATION;
                startConnectionAnimation();
            }
        });

        finalConnectionProgressAnimator.start();
    }

    private void startDisconnectionAnimation() {
        if (!isInit) {
            return;
        }
        setupDisconnectionAnimation();

        disconnectionAnimator = ValueAnimator.ofFloat(0f, 1f);
        disconnectionAnimator.setDuration(AnimationUtils.CONNECT_ANIMATION_DURATION);
        disconnectionAnimator.setInterpolator(new DecelerateInterpolator(2f));
        disconnectionAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                progressConnection = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        disconnectionAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                state = ConnectionAnimationState.DISCONNECTING_PROGRESS;
                if (animationShouldBeFinished) {
                    startFinalDisconnectionProgressAnimation();
                } else {
                    startDisconnectionProgressAnimation();
                }
            }
        });

        disconnectionAnimator.start();
    }

    private void startDisconnectionProgressAnimation() {
        if (!isInit) {
            return;
        }
        setupDisconnectionProgressAnimation();
        progressAngle = 0f;

        disconnectionProgressAnimator = ValueAnimator.ofFloat(0f, 0.7f);
        disconnectionProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        disconnectionProgressAnimator.setDuration(AnimationUtils.DISCONNECTION_PROGRESS_ANIMATION_DURATION);

        disconnectionProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                progressAngle = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        disconnectionProgressAnimator.start();
    }

    private void startFinalDisconnectionProgressAnimation() {
        if (!isInit) {
            return;
        }
        if (disconnectionProgressAnimator != null && disconnectionProgressAnimator.isRunning()) {
            disconnectionProgressAnimator.cancel();
        }

        setupDisconnectionProgressAnimation();

        finalDisconnectionProgressAnimator = ValueAnimator.ofFloat(progressAngle, 1f);
        finalDisconnectionProgressAnimator.setDuration(AnimationUtils.FINAL_PROGRESS_ANIMATION_DURATION);

        finalDisconnectionProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                progressAngle = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });
        finalDisconnectionProgressAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "onAnimationEnd: finalDisconnectionProgressAnimator");
                state = ConnectionAnimationState.NOT_CONNECTED;
            }
        });

        finalDisconnectionProgressAnimator.start();
    }

    private void reverseConnectionProgressAnimation() {
        if (!isInit) {
            return;
        }
        progressAngle = 1 - progressAngle;
        disconnectionProgressAnimator = ValueAnimator.ofFloat(progressAngle, 1 - (1 - progressAngle) * 0.3f);
        disconnectionProgressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        disconnectionProgressAnimator.setDuration(AnimationUtils.DISCONNECTION_PROGRESS_ANIMATION_DURATION);

        disconnectionProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                progressAngle = (float) valueAnimator.getAnimatedValue();
                invalidate();
            }
        });

        disconnectionProgressAnimator.start();
    }

    private void startWaveAnimation() {
        if (!isInit) {
            return;
        }
        ValueAnimator waveAnimator = ValueAnimator.ofFloat(0f, 1f);
        waveAnimator.setDuration(AnimationUtils.WAVES_ANIMATION_DURATION);
        waveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                progressWave = (float) valueAnimator.getAnimatedValue();
                if (shouldBeWaved) {
                    invalidate();
                }
            }
        });

        waveAnimator.start();
    }

    private void initWaveAnimation() {
        shouldBeWaved = true;
        waveHandler.removeCallbacks(null);
        waveHandler.postDelayed(getWaveRunnable(), AnimationUtils.WAVES_ANIMATION_GAP_DURATION / 2);
    }

    public void updateConnectionState(ConnectionState connectionState) {
        LOGGER.info("Update connection state, new state = " + connectionState);
        switch (connectionState) {
            case CONNECTED:
                isPaused = false;
                handleConnectedState();
                break;
            case CONNECTING:
                isPaused = false;
                handleConnectingState();
                break;
            case DISCONNECTING:
                isPaused = false;
                handleDisconnectingState();
                break;
            case NOT_CONNECTED:
                isPaused = false;
                handleNotConnectedState();
                break;
            case PAUSING:
                isPaused = true;
                handleDisconnectingState();
                break;
            case PAUSED:
                isPaused = true;
                handleNotConnectedState();
                break;
        }
    }

    private void handleConnectedState() {
        LOGGER.info("Handling CONNECTED state, old one = " + state);
        switch (state) {
            case NOT_CONNECTED:
            case DISCONNECTING_ANIMATION:
            case DISCONNECTING_PROGRESS:
                //reset state to correct
                stopAnimation();
                state = ConnectionAnimationState.CONNECTED;
                initWaveAnimation();
                invalidate();
                break;
            case CONNECTED:
                //it's already connected
                break;
            case CONNECTION_PROGRESS:
                startFinalConnectionProgressAnimation();
                break;
            case CONNECTING_ANIMATION:
                //connecting animation will automatically change state from CONNECTING_ANIMATION -> CONNECTED
                break;
        }
        LOGGER.info("Handling CONNECTED state, new one = " + state);
    }

    private void handleConnectingState() {
        LOGGER.info("Handling CONNECTING state, old one = " + state);
        switch (state) {
            case NOT_CONNECTED:
            case DISCONNECTING_ANIMATION:
            case DISCONNECTING_PROGRESS:
            case CONNECTED:
                stopAnimation();
                connect();
                break;
            case CONNECTION_PROGRESS:
            case CONNECTING_ANIMATION:
                //ignore this request
                break;
        }
        LOGGER.info("Handling CONNECTING state, new one = " + state);
    }

    private void handleDisconnectingState() {
        LOGGER.info("Handling DISCONNECTING state, old one = " + state);
        switch (state) {
            case NOT_CONNECTED:
                //This case should never happen
                break;
            case DISCONNECTING_ANIMATION:
            case DISCONNECTING_PROGRESS:
                //it's already disconnecting
                break;
            case CONNECTION_PROGRESS:
                stopAnimation();
                state = ConnectionAnimationState.DISCONNECTING_PROGRESS;
                reverseConnectionProgressAnimation();
                break;
            case CONNECTED:
            case CONNECTING_ANIMATION:
                stopAnimation();
                disconnect();
                break;
        }
        LOGGER.info("Handling DISCONNECTING state, new one = " + state);
    }

    private void handleNotConnectedState() {
        LOGGER.info("Handling NOT CONNECTED state, old one = " + state);
        switch (state) {
            case CONNECTED:
            case CONNECTION_PROGRESS:
            case CONNECTING_ANIMATION:
                //reset state to correct
                stopAnimation();
                state = ConnectionAnimationState.NOT_CONNECTED;
//                invalidate();
                break;
            case NOT_CONNECTED:
                //It's already not connected
                break;
            case DISCONNECTING_ANIMATION:
                animationShouldBeFinished = true;
                break;
            case DISCONNECTING_PROGRESS:
                startFinalDisconnectionProgressAnimation();
                break;
        }
        invalidate();
        LOGGER.info("Handling NOT CONNECTED state, new one = " + state);
    }

    private void stopAnimation() {
        LOGGER.info("Stop animations");
        //finish all animations
        if (disconnectionProgressAnimator != null && disconnectionProgressAnimator.isRunning()) {
            disconnectionProgressAnimator.removeAllListeners();
            disconnectionProgressAnimator.cancel();
        }
        if (connectionProgressAnimator != null && connectionProgressAnimator.isRunning()) {
            connectionProgressAnimator.removeAllListeners();
            connectionProgressAnimator.cancel();
        }
        if (finalDisconnectionProgressAnimator != null && finalDisconnectionProgressAnimator.isRunning()) {
            finalDisconnectionProgressAnimator.removeAllListeners();
            finalDisconnectionProgressAnimator.cancel();
        }
        if (finalConnectionProgressAnimator != null && finalConnectionProgressAnimator.isRunning()) {
            finalConnectionProgressAnimator.removeAllListeners();
            finalConnectionProgressAnimator.cancel();
        }
        if (disconnectionAnimator != null && disconnectionAnimator.isRunning()) {
            disconnectionAnimator.removeAllListeners();
            disconnectionAnimator.cancel();
        }
        if (connectionAnimator != null && connectionAnimator.isRunning()) {
            connectionAnimator.removeAllListeners();
            connectionAnimator.cancel();
        }
    }

    private void connect() {
        state = ConnectionAnimationState.CONNECTION_PROGRESS;
        startConnectionProgressAnimation();
    }

    private void disconnect() {
        state = ConnectionAnimationState.DISCONNECTING_ANIMATION;
        startDisconnectionAnimation();
    }

    private void setupDisconnectionAnimation() {
        shouldBeWaved = false;
        progressAngle = 0;
        paintProgress.setAlpha(AnimationUtils.MAX_ALPHA);
        paintCircleBackground.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_white));
    }

    private void setupConnectionProgressAnimation() {
        if (paintProgress == null) {
            return;
        }
        shouldBeWaved = false;
        paintProgress.setAlpha(AnimationUtils.MAX_ALPHA);
        paintProgressBackground.setAlpha(AnimationUtils.MAX_ALPHA);
        paintCircleBackground.setColor(ContextCompat.getColor(getContext(), R.color.color_animation_background));

        //fix for mystic bug with different size of two arc that actually are same!
        paintProgress.setStrokeWidth(strokeProgressWidth);
        paintProgressBackground.setStrokeWidth(strokeProgressWidth - 1);
    }

    private void setupDisconnectionProgressAnimation() {
        shouldBeWaved = false;
        paintProgress.setAlpha(AnimationUtils.MAX_ALPHA);
        paintProgressBackground.setAlpha(AnimationUtils.MAX_ALPHA);

//        fix for mystic bug with different size of two arc that actually are same!
        paintProgress.setStrokeWidth(strokeProgressWidth - 1);
        paintProgressBackground.setStrokeWidth(strokeProgressWidth);
    }

    public int getStatusTopMargin() {
        int height = getHeight();
        int width = getWidth();

        RectF rect = AnimationUtils.getProgressRect(width, height);

        return (int) ((height - rect.height()) / 4);
    }

    public boolean validateMainTouchEvent(MotionEvent event) {
        RectF rectF = AnimationUtils.getProgressRect(getWidth(), getHeight());
        return rectF.contains(event.getX(), event.getY());
    }

    public boolean validateStopTouchEvent(MotionEvent event) {
        if (state == null || !(state.equals(ConnectionAnimationState.NOT_CONNECTED) && isPaused)) {
            return false;
        }
        RectF rectF = AnimationUtils.getProgressRect(getWidth(), getHeight());
        Rect pauseRect = AnimationUtils.getPauseBtnRect(rectF);
        return pauseRect.contains((int) event.getX(), (int) event.getY());
    }

    public boolean validatePauseTouchEvent(MotionEvent event) {
        if (state == null || !state.equals(ConnectionAnimationState.CONNECTED)) {
            return false;
        }
        RectF rectF = AnimationUtils.getProgressRect(getWidth(), getHeight());
        Rect pauseRect = AnimationUtils.getPauseBtnRect(rectF);
        return pauseRect.contains((int) event.getX(), (int) event.getY());
    }

    public void reset() {
        LOGGER.info("Reset");
        state = ConnectionAnimationState.NOT_CONNECTED;
        invalidate();
    }

    public ConnectionAnimationState getState() {
        return state;
    }

    private Runnable getWaveRunnable() {
        return new WaveRunnable();
    }

    private class WaveRunnable implements Runnable {

        @Override
        public void run() {
            startWaveAnimation();
            if (shouldBeWaved && state == ConnectionAnimationState.CONNECTED) {
                waveHandler.postDelayed(getWaveRunnable(), AnimationUtils.WAVES_ANIMATION_GAP_DURATION);
            }
        }
    }
}