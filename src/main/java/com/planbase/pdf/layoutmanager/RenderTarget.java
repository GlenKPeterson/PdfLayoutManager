// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr2
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;

/**
 Represents something to be drawn to.  For page-breaking, use the {@link PageGrouping}
 implementation.  For a fixed, single page use the {@link SinglePage} implementation.
 */
public interface RenderTarget {
    /**
     Must draw from higher to lower.  Thus y1 must be &gt;= y2 (remember, higher y values
     are up).
     @param x1 first x-value
     @param y1 first (upper) y-value
     @param x2 second x-value
     @param y2 second (lower or same) y-value
     @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    RenderTarget drawLine(float x1, float y1, float x2, float y2, final LineStyle ls);

    /**
     Puts styled text on this RenderTarget
     @param x the left-most X-value
     @param y the (bottom?) Y-value
     @param s the text
     @param textStyle the style
     @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    RenderTarget drawStyledText(float x, float y, String s, TextStyle textStyle);

    /**
     Puts a jpeg on this RenderTarget
     @param x left offset
     @param y bottom offset
     @param sj the jpeg image
     @return the lowest y-value.
     */
    float drawJpeg(float x, float y, ScaledJpeg sj);

    /**
     Puts a png on this RenderTarget
     @param x left offset
     @param y bottom offset
     @param sj the png image
     @return the lowest y-value.
     */
    float drawPng(float x, float y, ScaledPng sj);

    /**
     Puts a colored rectangle on this RenderTarget.  There is no outline or border (that's drawn
     separately with lines).
     @param topLeft exterior x and y values of the upper-left corner
     @param dim width and height (dimensions) of rectangle
     @param c color
     @return the updated RenderTarget (may be changed to return the lowest y-value instead)
     */
    RenderTarget fillRect(XyOffset topLeft, XyDim dim, PDColor c);
}