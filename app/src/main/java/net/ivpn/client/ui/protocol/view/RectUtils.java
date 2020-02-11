package net.ivpn.client.ui.protocol.view;

import android.graphics.Path;
import android.graphics.RectF;

public class RectUtils {

    private int width;
    private int height;

    RectUtils(int width, int height) {
        this.height = height;
        this.width = width;
    }

    RectF getIncrementRectF() {
        RectF rectF = new RectF();

        rectF.left = 2 * width / 3f;
        rectF.top = 0;
        rectF.right = width;
        rectF.bottom = height;

        return rectF;
    }

    RectF getDecrementRectF() {
        RectF rectF = new RectF();

        rectF.left = 0;
        rectF.top = 0;
        rectF.right = width / 3f;
        rectF.bottom = height;

        return rectF;
    }

    RectF getValueRectF() {
        RectF rectF = new RectF();

        rectF.left = width / 3f;
        rectF.top = 0;
        rectF.right = 2 * width / 3f;
        rectF.bottom = height;

        return rectF;
    }

    Path RoundedRect(float left, float top, float right, float bottom, float rx, float ry,
                                   boolean tl, boolean tr, boolean br, boolean bl) {
        Path path = new Path();
        if (rx < 0) rx = 0;
        if (ry < 0) ry = 0;
        float width = right - left;
        float height = bottom - top;
        if (rx > width / 2) rx = width / 2;
        if (ry > height / 2) ry = height / 2;
        float widthMinusCorners = (width - (2 * rx));
        float heightMinusCorners = (height - (2 * ry));

        path.moveTo(right, top + ry);
        if (tr)
            path.rQuadTo(0, -ry, -rx, -ry);//top-right corner
        else {
            path.rLineTo(0, -ry);
            path.rLineTo(-rx, 0);
        }
        path.rLineTo(-widthMinusCorners, 0);
        if (tl)
            path.rQuadTo(-rx, 0, -rx, ry); //top-left corner
        else {
            path.rLineTo(-rx, 0);
            path.rLineTo(0, ry);
        }
        path.rLineTo(0, heightMinusCorners);

        if (bl)
            path.rQuadTo(0, ry, rx, ry);//bottom-left corner
        else {
            path.rLineTo(0, ry);
            path.rLineTo(rx, 0);
        }

        path.rLineTo(widthMinusCorners, 0);
        if (br)
            path.rQuadTo(rx, 0, rx, -ry); //bottom-right corner
        else {
            path.rLineTo(rx, 0);
            path.rLineTo(0, -ry);
        }

        path.rLineTo(0, -heightMinusCorners);

        path.close();//Given close, last lineto can be removed.

        return path;
    }
}
