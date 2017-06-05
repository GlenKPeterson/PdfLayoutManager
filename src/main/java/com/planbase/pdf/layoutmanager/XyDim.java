// Copyright 2014-08-18 PlanBase Inc. & Glen Peterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

/**
 Immutable 2D dimension in terms of non-negative width and height.
 Do not confuse a dimension (measurement) with an XyOffset which represents coordinates where
 the bottom of the page is zero and positive height is up from there.
 Remember: an XyDim on a Portrait orientation page has the width and height *reversed*.
 */
public class XyDim {
    public static final XyDim ZERO = new XyDim(0f, 0f) {
        @Override public int hashCode() { return 0; }
    };
    private final float width;
    private final float height;
    private XyDim(float w, float h) {
        if ( (w < 0) || (h < 0) ) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        width = w; height = h;
    }

    /** Returns an XyDim of the given width and height. */
    public static XyDim of(float width, float height) {
        if ((width == 0f) && (height == 0f)) { return ZERO; }
        return new XyDim(width, height);
    }

    /**
     Returns an XyDim with the width and height taken from the same-named fields on the given
     rectangle.
     */
    public static XyDim of(PDRectangle rect) {
        return of(rect.getWidth(), rect.getHeight());
    }

    public float width() { return width; }

    public float height() { return height; }

    public XyDim width(float newX) { return of(newX, height); }

    public XyDim height(float newY) { return of(width, newY); }

    /**
     If true, returns this, if false, returns a new XyDim with width and height values swapped.
     PDFs think the long dimension is always the height of the page, regardless of portrait vs.
     landscape, so we need to conditionally adjust what we call width and height.
     */
    // This is suspicious because we're swapping width and height and the compiler thinks
    // we might be doing so by accident.
    @SuppressWarnings("SuspiciousNameCombination")
    public XyDim swapWh() { return of(height, width); }

    /** Returns a PDRectangle with the given width and height (but no/0 offset) */
    public PDRectangle toRect() { return new PDRectangle(width, height); }

    /** Subtracts the given XyDim from this one (remember, these can't be negative). */
    public XyDim minus(XyDim that) { return of(this.width - that.width(), this.height - that.height()); }

    /** Adds the given XyDim from this one */
    public XyDim plus(XyDim that) { return of(this.width + that.width(), this.height + that.height()); }

//    public XyPair plusXMinusY(XyPair that) { return of(this.width + that.width(), this.height - that.height()); }

//    public XyDim maxXandY(XyDim that) {
//        if ((this.width >= that.width()) && (this.height >= that.height())) { return this; }
//        if ((this.width <= that.width()) && (this.height <= that.height())) { return that; }
//        return of((this.width > that.width()) ? this.width : that.width(),
//                  (this.height > that.height()) ? this.height : that.height());
//    }

    /** Compares dimensions and returns true if that dimension doesn't extend beyond this one. */
    public boolean lte(XyDim that) {
        return (this.width <= that.width()) && (this.height <= that.height());
    }

    @Override public String toString() { return "XyDim(" + width + " " + height + ")"; }

    @Override public int hashCode() {
        return Utils.floatHashCode(width) ^ Utils.floatHashCode(height);
    }

    @Override public boolean equals(Object other) {
        // Cheapest operation first...
        if (this == other) { return true; }
        if (!(other instanceof XyDim)) { return false; }

        // Details...
        final XyDim that = (XyDim) other;

        // Compare "significant" fields here.
        return (width == that.width()) && (height == that.height());
    }
}
