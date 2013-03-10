// Copyright 2013-03-03 PlanBase Inc. & Glen Peterson
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

import java.awt.Color;

/**
 Holds the LineStyles for the top, right, bottom, and left borders of a PdfItem.  For an equal
  border on all sides, use:
 <pre><code>BorderStyle b = BorderStyle.valueOf(color, width);</code></pre>
 For an unequal border, Java doesn't have named parameters, so this classes uses a builder pattern
 instead.  This class works just like styles in CSS in terms of specifying one style, then
 overwriting it with another.  This example sets all borders except to black and the default width,
 then removes the top border:
 <pre><code>BorderStyle topBorderStyle = BorderStyle.builder().color(Color.BLACK)
                                                   .width(DEFAULT_WIDTH)
                                                   .top(null, 0).build();</code></pre>
 If neighboring cells in a cell-row have the same border, only one will be printed.  If different,
 the left-border of the right cell will override.  You have to manage your own top borders
 manually.
 */
public class BorderStyle {

    // Like CSS it's listed Top, Right, Bottom, left
    private final LineStyle top;
    private final LineStyle right;
    private final LineStyle bottom;
    private final LineStyle left;

    /** Factory for constructing immutable BorderStyle instances. */
    public static class Builder {
        private Color tColor;
        private Color rColor;
        private Color bColor;
        private Color lColor;

        private float tWidth;
        private float rWidth;
        private float bWidth;
        private float lWidth;

        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public Builder top(Color c, float w) { tColor = c; tWidth = w; return this; }
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public Builder right(Color c, float w) { rColor = c; rWidth = w; return this; }
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public Builder bottom(Color c, float w) { bColor = c; bWidth = w; return this; }
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public Builder left(Color c, float w) { lColor = c; lWidth = w; return this; }

        /** Sets top, right, bottom, and left color */
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        private Builder color(Color c) {
            tColor = c; rColor = c; bColor = c; lColor = c;
            return this;
        }
        /** Sets top, right, bottom, and left width */
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public Builder width(float w) {
            tWidth = w; rWidth = w; bWidth = w; lWidth = w;
            return this;
        }

        /** Call this to make an immutable BorderStyle object based on your settings. */
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public BorderStyle build() {
            return new BorderStyle(this);
        }
    }

    private BorderStyle(Builder b) {
        if ((b.tWidth > 0) && (b.tColor != null)) {
            top = LineStyle.valueOf(b.tColor, b.tWidth);
        } else {
            top = null;
        }
        if ((b.rWidth > 0) && (b.rColor != null)) {
            right = LineStyle.valueOf(b.rColor, b.rWidth);
        } else {
            right = null;
        }
        if ((b.bWidth > 0) && (b.bColor != null)) {
            bottom = LineStyle.valueOf(b.bColor, b.bWidth);
        } else {
            bottom = null;
        }
        if ((b.lWidth > 0) && (b.lColor != null)) {
            left = LineStyle.valueOf(b.lColor, b.lWidth);
        } else {
            left = null;
        }
    }

    private BorderStyle(Color c, float w) {
        LineStyle style = LineStyle.valueOf(c, w);
        top = style;
        right = style;
        bottom = style;
        left = style;
    }

    /**
     Returns an equal border on all sides
     @param c the border color
     @param w the width of the border.
     @return a new immutable border object
     */
    public static BorderStyle valueOf(Color c, float w) {
        return new BorderStyle(c, w);
    }

    /**
     Returns an equal border on all sides
     @param c the border color
     @return a new immutable border object with default width
     */
    public static BorderStyle valueOf(Color c) {
        return new BorderStyle(c, LineStyle.DEFAULT_WIDTH);
    }

    /** Returns a mutable helper class for building and immutable BorderStyle object. */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public static Builder builder() { return new Builder(); }

    public LineStyle topLineStyle() { return top; }
    public LineStyle rightLineStyle() { return right; }
    public LineStyle bottomLineStyle() { return bottom; }
    public LineStyle leftLineStyle() { return left; }
}
