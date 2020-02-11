package net.ivpn.client.ui.connect.animation;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

class AnimationUtils {

    private static final String TAG = AnimationUtils.class.getSimpleName();

    static final long FINAL_PROGRESS_ANIMATION_DURATION = 300;
    static final long CONNECTION_PROGRESS_ANIMATION_DURATION = 5000;
    static final long DISCONNECTION_PROGRESS_ANIMATION_DURATION = 750;
    static final long CONNECT_ANIMATION_DURATION = 1000;
    static final long WAVES_ANIMATION_DURATION = 1500;
    static final long WAVES_ANIMATION_GAP_DURATION = 5000;

    static final int MAX_ALPHA = 255;
    static final int START_ANGLE = 270;
    static final int FULL_CIRCLE = 360;

    static RectF getProgressRect(int width, int height) {
        int actualWidth = 2 * width / 5;
        int actualHeight = height / 2;
        int size = actualHeight < actualWidth ? actualHeight : actualWidth;

        RectF rect = new RectF();
        rect.left = width / 2 - size / 2;
        rect.right = rect.left + size;
        rect.top = (height - size) / 2;
        rect.bottom = rect.top + size;

        return rect;
    }

    static Rect getIconRect(RectF rectF) {
        float size = rectF.width();

        Rect rect = new Rect();
        rect.left = (int) (rectF.left + size / 4);
        rect.right = (int) (rectF.right - size / 4);
        rect.top = (int) (rectF.top + size / 4);
        rect.bottom = (int) (rectF.bottom - size / 4);

        return rect;
    }

    static Rect getActionIconRect(Rect rect) {
        float size = rect.width();

        Rect iconRect = new Rect();
        iconRect.left = (int) (rect.left + size / 5);
        iconRect.right = (int) (rect.right - size / 5);
        iconRect.top = (int) (rect.top + size / 5);
        iconRect.bottom = (int) (rect.bottom - size / 5);

        return iconRect;
    }

    static Rect getConnectedBtnRect(RectF rectF, float animationProgress) {
        float size = rectF.width();
        Rect rect = new Rect();
        //magic coefficient
        float correctionFactor = 7.2f;
        float animationFactor = -(size / 3) * (1 - animationProgress) + size / correctionFactor;
        rect.left = (int) (rectF.left - animationFactor);
        rect.right = (int) (rectF.right + animationFactor);
        rect.top = (int) (rectF.top - animationFactor);
        rect.bottom = (int) (rectF.bottom + animationFactor);

        return rect;
    }

    static Rect getPauseBtnRect(RectF rectF) {
        float size = rectF.width() / 3;
        float shadowFactor = size * 0.15f;
        Rect rect = new Rect();
        rect.left = (int) (rectF.right);
        rect.right = (int) (rectF.right + size + 2 * shadowFactor);
        rect.top = (int) (rectF.bottom - size - shadowFactor);
        rect.bottom = (int) (rectF.bottom + shadowFactor);

        return rect;
    }
}
