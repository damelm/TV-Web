package com.tv.webview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/** Puntero (cursor) que se dibuja encima del contenido y se mueve con el mando. */
public class CursorView extends View {

    private final Paint fill;
    private final Paint stroke;
    private float x = -100, y = -100;
    private final float radius;

    public CursorView(Context ctx) {
        super(ctx);
        float density = ctx.getResources().getDisplayMetrics().density;
        radius = 11 * density;

        fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.argb(210, 255, 255, 255));
        fill.setStyle(Paint.Style.FILL);

        stroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        stroke.setColor(Color.argb(230, 0, 0, 0));
        stroke.setStyle(Paint.Style.STROKE);
        stroke.setStrokeWidth(3 * density);
    }

    public void setPos(float nx, float ny) {
        x = nx;
        y = ny;
        invalidate();
    }

    public float getX() { return x; }
    public float getY() { return y; }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(x, y, radius, fill);
        canvas.drawCircle(x, y, radius, stroke);
    }
}
