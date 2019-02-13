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

/**
 Represents minimum spacing of the top, right, bottom, and left sides of PDF Page Items.
 */
public class Padding {

    /**
     Default padding of 1.5, 1.5, 2. 1.5 (top, right, bottom, left)
     */
    public static final Padding DEFAULT_TEXT_PADDING = new Padding(1.5, 1.5, 2, 1.5);
    public static final Padding NO_PADDING = new Padding(0, 0, 0, 0);

    private final double top;
    private final double right;
    private final double bottom;
    private final double left;
    private Padding(final double t, final double r, final double b, final double l) {
        top = t; right = r; bottom = b; left = l;
    }
    /** Like HTML it's top, right, bottom, left */
    public static Padding of(double t, double r, double b, double l) {
        if ((t == 0) && (r == 0) && (b == 0) && (l == 0)) { return NO_PADDING; }
        if ((t == 1.5) && (r == 1.5) && (b == 2) && (l == 1.5)) {
            return DEFAULT_TEXT_PADDING;
        }
        return new Padding(t, r, b, l);
    }
    /** Sets all padding values equally */
    public static Padding of(double a) {
        if (a == 0) { return NO_PADDING; }
        return new Padding(a,a,a,a);
    }

    public Dim topLeftPadDim() { return Dim.of(left, top); }
    public Dim botRightPadDim() { return Dim.of(right, bottom); }

    public Dim subtractFrom(Dim outer) {
        return Dim.of(outer.getWidth() - (left + right),
                      outer.getHeight() - (top + bottom));
    }

    public Dim addTo(Dim outer) {
        return Dim.of(outer.getWidth() + (left + right),
                      outer.getHeight() + (top + bottom));
    }

    public Coord applyTopLeft(Coord orig) {
        return Coord.of(orig.getX() + left, orig.getY() - top);
    }

//    public XyOffset topLeftPadOffset() { return XyOffset.of(left, -top); }
//    public XyOffset botRightPadOffset() { return XyOffset.of(right, -bottom); }

    @Override public boolean equals(Object other) {
        // Cheapest operations first...
        if (this == other) { return true; }
        if ( !(other instanceof Padding) ) { return false; }
        // Details...
        final Padding that = (Padding) other;

        return (this.top == that.top) &&
               (this.right == that.right) &&
               (this.bottom == that.bottom) &&
               (this.left == that.left);
    }

    @Override public int hashCode() {
        // There is generally no reason to ever have padding amounts that differ by less than 0.1
        // which in HTML-land would be a tenth of a pixel.  Since we're adding four roughly equal
        // values, if only one is off by 0.1, that becomes 0.025, hence multiplying times 40 instead
        // of times 10.
        return (int) ((top + right + bottom + left) * 40);
    }

    @Override
    public String toString() {
        if ( (top == right) && (top == bottom) && (top == left) ) {
            return "Padding(" + top + ")";
        }
        return "Padding(t=" + top + ", r=" + right + ", b=" + bottom + ", l=" + left + ")";
    }

    public double top() { return top; }
    public double right() { return right; }
    public double bottom() { return bottom; }
    public double left() { return left; }
}
