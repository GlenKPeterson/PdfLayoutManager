package com.planbase.pdf.layoutmanager;

import java.awt.Color;

/**
 Represents something to be drawn to.  For page-breaking, use the {@link LogicalPage}
 implementation.  For a fixed, single page use the {@link PageBuffer} implementation.
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
     Puts styled text on this RenderTarget
     @param x the left-most X-value
     @param y the (bottom?) Y-value
     @param s the text
     @param textStyle the style
     @return the updated RenderTarget
     */
    RenderTarget drawStyledText(float x, float y, String s, TextStyle textStyle);

    /**
     Puts a jpeg on this RenderTarget
     @param x left offset
     @param y bottom? offset
     @param sj the jpeg image
     @return the updated RenderTarget
     */
    RenderTarget drawJpeg(float x, float y, ScaledJpeg sj);

    /**
     Puts a png on this RenderTarget
     @param x left offset
     @param y bottom? offset
     @param sj the png image
     @return the updated RenderTarget
     */
    RenderTarget drawPng(float x, float y, ScaledPng sj);

    /**
     Puts a colored rectangle on this RenderTarget.  There is no outline or border (that's drawn
     separately with lines).
     @param topLeft exterior x and y values of the upper-left corner
     @param dim width and height (dimensions) of rectangle
     @param c color
     @return the updated RenderTarget
     */
    RenderTarget fillRect(XyOffset topLeft, XyDim dim, Color c);
}