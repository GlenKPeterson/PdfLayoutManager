package com.planbase.pdf.layoutmanager;

/**
 Created by gpeterso on 6/9/17.
 */
public interface RenderTarget {
    /**
     Must draw from higher to lower.  Thus y1 must be &gt;= y2 (remember, higher y values
     are up).
     @param x1 first x-value
     @param y1 first (upper) y-value
     @param x2 second x-value
     @param y2 second (lower or same) y-value
     */
    RenderTarget drawLine(float x1, float y1, float x2, float y2, final LineStyle ls);

    /**
     Puts styled text on a RenderTarget
     @param x the left-most X-value
     @param y the (bottom?) Y-value
     @param s the text
     @param textStyle the style
     @return the updated RenderTarget
     */
    RenderTarget drawStyledText(float x, float y, String s, TextStyle textStyle);
}
