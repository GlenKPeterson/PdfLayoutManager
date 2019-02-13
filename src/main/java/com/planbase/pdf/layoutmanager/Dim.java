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

/**
 Immutable 2D dimension in terms of non-negative width and height.
 Do not confuse a dimension (measurement) with a Coord(inate) which represents an offset from
 (0, 0) at the bottom of the page.
 Remember: a Dimensions on a Portrait orientation may have the width and height *reversed*.
 */
public class Dim {
    public static final Dim ZERO = new Dim(0, 0) {
        @Override public int hashCode() { return 0; }
    };
    private final double width;
    private final double height;
    private Dim(double width, double height) {
        if ( (width < 0) || (height < 0) ) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        this.width = width;
        this.height = height;
    }
    public static Dim of(double x, double y) {
        if ((x == 0) && (y == 0)) { return ZERO; }
        return new Dim(x, y);
    }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public Dim withWidth(double newX) { return of(newX, height); }
    public Dim withHeight(double newY) { return of(width, newY); }

    public Dim minus(Dim that) { return of(this.width - that.getWidth(), this.height - that.getHeight()); }
    public Dim plus(Dim that) { return of(this.width + that.getWidth(), this.height + that.getHeight()); }

//    public XyPair plusXMinusY(XyPair that) { return of(this.width + that.width(), this.height - that.height()); }

//    public Dim maxXandY(Dim that) {
//        if ((this.width >= that.width()) && (this.height >= that.height())) { return this; }
//        if ((this.width <= that.width()) && (this.height <= that.height())) { return that; }
//        return of((this.width > that.width()) ? this.width : that.width(),
//                  (this.height > that.height()) ? this.height : that.height());
//    }
//    public XyPair maxXMinY(XyPair that) {
//        if ((this.width >= that.width()) && (this.height <= that.height())) { return this; }
//        if ((this.width <= that.width()) && (this.height >= that.height())) { return that; }
//        return of((this.width > that.width()) ? this.width : that.width(),
//                  (this.height < that.height()) ? this.height : that.height());
//    }

    /** Compares dimensions */
    public boolean lte(Dim that) { return (this.width <= that.getWidth()) && (this.height <= that.getHeight()); }

    @Override public String toString() { return "Dim(" + width + ", " + height + ")"; }

    @Override public int hashCode() { return Double.hashCode(width) ^ Double.hashCode(height); }

    @Override public boolean equals(Object other) {
        // Cheapest operation first...
        if (this == other) { return true; }

        if ( !(other instanceof Dim) ) {
            return false;
        }
        // Details...
        final Dim that = (Dim) other;

        // Compare "significant" fields here.
        return (width == that.getWidth()) && (height == that.getHeight());
    }
}
