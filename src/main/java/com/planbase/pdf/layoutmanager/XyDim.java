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
 Represents a 2D dimension in terms of X and Y where both X and Y must be non-negative.
 Do not confuse a dimension (measurement) with an XyOffset which represents coordinates where
 negative y is down from the upper left-hand corner.
 */
public class XyDim {
    public static final XyDim ZERO = new XyDim(0, 0) {
        @Override public int hashCode() { return 0; }
    };
    private final double x;
    private final double y;
    private XyDim(double xCoord, double yCoord) {
        if ( (xCoord < 0) || (yCoord < 0) ) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        x = xCoord; y = yCoord;
    }
    public static XyDim of(double x, double y) {
        if ((x == 0) && (y == 0)) { return ZERO; }
        return new XyDim(x, y);
    }
    public double x() { return x; }
    public double y() { return y; }
    public XyDim x(double newX) { return of(newX, y); }
    public XyDim y(double newY) { return of(x, newY); }

    public XyDim minus(XyDim that) { return of(this.x - that.x(), this.y - that.y()); }
    public XyDim plus(XyDim that) { return of(this.x + that.x(), this.y + that.y()); }

//    public XyPair plusXMinusY(XyPair that) { return of(this.x + that.x(), this.y - that.y()); }

    public XyDim maxXandY(XyDim that) {
        if ((this.x >= that.x()) && (this.y >= that.y())) { return this; }
        if ((this.x <= that.x()) && (this.y <= that.y())) { return that; }
        return of((this.x > that.x()) ? this.x : that.x(),
                  (this.y > that.y()) ? this.y : that.y());
    }
//    public XyPair maxXMinY(XyPair that) {
//        if ((this.x >= that.x()) && (this.y <= that.y())) { return this; }
//        if ((this.x <= that.x()) && (this.y >= that.y())) { return that; }
//        return of((this.x > that.x()) ? this.x : that.x(),
//                  (this.y < that.y()) ? this.y : that.y());
//    }

    /** Compares dimensions */
    public boolean lte(XyDim that) { return (this.x <= that.x()) && (this.y <= that.y()); }

    @Override public String toString() { return "XyDim(" + x + " " + y + ")"; }

    @Override public int hashCode() { return Double.hashCode(x) ^ Double.hashCode(y); }

    @Override public boolean equals(Object other) {
        // Cheapest operation first...
        if (this == other) { return true; }

        if ( (other == null) ||
             !(other instanceof XyDim) ||
             (this.hashCode() != other.hashCode()) ) {
            return false;
        }
        // Details...
        final XyDim that = (XyDim) other;

        // Compare "significant" fields here.
        return (x == that.x()) && (y == that.y());
    }
}
