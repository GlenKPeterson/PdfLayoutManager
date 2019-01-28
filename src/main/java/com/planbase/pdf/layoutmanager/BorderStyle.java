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

// TODO: This class should at least allow the user to provide LineStyles instead of colors and widths directly.

/**
 Holds the LineStyles for the top, right, bottom, and left borders of a PdfItem.  For an equal
  border on all sides, use:
 <pre><code>BorderStyle b = BorderStyle.of(color, width);</code></pre>
 For an unequal border, Java doesn't have named parameters, so this classes uses a builder pattern
 instead.  This class works just like styles in CSS in terms of specifying one style, then
 overwriting it with another.  This example sets all borders except to black and the default width,
 then removes the top border:
 <pre><code>BorderStyle topBorderStyle = BorderStyle.builder().color(Color.BLACK)
                                                  .width(LineStyle.DEFAULT_WIDTH)
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

    public static final BorderStyle NO_BORDERS = new BorderStyle(null,null,null,null) {
        @Override public String toString() { return "BorderStyle.NO_BORDERS"; }
    };

    /** Factory helper-class for constructing immutable BorderStyle instances. */
    public static class Builder {
        private LineStyle top;
        private LineStyle right;
        private LineStyle bottom;
        private LineStyle left;

        private Builder(BorderStyle bs) {
            if (bs != null) { top = bs.top; right = bs.right; bottom = bs.bottom; left = bs.left; }
        }

        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public Builder top(LineStyle ls) { top = ls; return this; }
        public Builder right(LineStyle ls) { right = ls; return this; }
        public Builder bottom(LineStyle ls) { bottom = ls; return this; }
        public Builder left(LineStyle ls) { left = ls; return this; }

        /** Sets top, right, bottom, and left color */
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        private Builder color(Color c) {
            if (top == null) {
                top = LineStyle.of(c);
            } else if (!Utils.equals(top.color(), c)) {
                top = LineStyle.of(c, top.width());
            }

            if (right == null) {
                right = LineStyle.of(c);
            } else if (!Utils.equals(right.color(), c)) {
                right = LineStyle.of(c, right.width());
            }

            if (bottom == null) {
                bottom = LineStyle.of(c);
            } else if (!Utils.equals(bottom.color(), c)) {
                bottom = LineStyle.of(c, bottom.width());
            }

            if (left == null) {
                left = LineStyle.of(c);
            } else if (!Utils.equals(left.color(), c)) {
                left = LineStyle.of(c, left.width());
            }
            return this;
        }
//        /** Sets top, right, bottom, and left width */
//        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
//        public Builder width(double w) {
//            tWidth = w; rWidth = w; bWidth = w; lWidth = w;
//            return this;
//        }

        /** Call this to make an immutable BorderStyle object based on your settings. */
        @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
        public BorderStyle build() {
            return new BorderStyle(this);
        }
    }

    private BorderStyle(LineStyle t, LineStyle r, LineStyle b, LineStyle l) {
        top = t; right = r; bottom = b; left = l;
    }

    private BorderStyle(Builder b) {
        top = b.top;
        right = b.right;
        bottom = b.bottom;
        left = b.left;
    }

    private BorderStyle(Color c, double w) {
        LineStyle style = LineStyle.of(c, w);
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
    public static BorderStyle of(Color c, double w) {
        return new BorderStyle(c, w);
    }

    /**
     Returns an equal border on all sides
     @param c the border color
     @return a new immutable border object with default width
     */
    public static BorderStyle of(Color c) {
        return new BorderStyle(c, LineStyle.DEFAULT_WIDTH);
    }

    /** Returns a mutable helper class for building an immutable BorderStyle object. */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public static Builder builder() { return new Builder(null); }

    public LineStyle top() { return top; }
    public LineStyle right() { return right; }
    public LineStyle bottom() { return bottom; }
    public LineStyle left() { return left; }

    public BorderStyle top(LineStyle ls) { return new Builder(this).top(ls).build(); }
    public BorderStyle right(LineStyle ls) { return new Builder(this).right(ls).build(); }
    public BorderStyle bottom(LineStyle ls) { return new Builder(this).bottom(ls).build(); }
    public BorderStyle left(LineStyle ls) { return new Builder(this).left(ls).build(); }

    @Override public String toString() {
        StringBuilder sB = new StringBuilder("BorderStyle(");
        if (top != null) { sB.append("t=").append(top); }
        if (right != null) { sB.append("r=").append(right); }
        if (bottom != null) { sB.append("b=").append(bottom); }
        if (left != null) { sB.append("l=").append(left); }
        return sB.append(")").toString();
    }
}
