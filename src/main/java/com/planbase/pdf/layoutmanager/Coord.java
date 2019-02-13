// Copyright 2014-08-20 PlanBase Inc. & Glen Peterson
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
 An immutable 2D Coordinate, offset, or point in terms of X and Y.  Often measured from the lower-left corner.
 Do not confuse this with an Dim which represents positive width and height.
 This is called Coord because Point and Point2D are already classes in Java and they are mutable.
 It's pronounced "co-ward" as in, "coordinate."  It's not called Xy because that's too easy to confuse
 with width and height, which this is not - it's an offset from the origin.
 */
public class Coord {
    private final double x;
    private final double y;
    public Coord(double x, double y) { this.x = x; this.y = y; }
    public static Coord of(double x, double y) {
        if ((x == 0) && (y == 0)) { return ORIGIN; }
        return new Coord(x, y);
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public Coord withX(double newX) { return of(newX, y); }
    public Coord withY(double newY) { return of(x, newY); }

//    public XyOffset minus(XyOffset that) { return of(this.x - that.x(), this.y - that.y()); }
//    public XyOffset plus(XyOffset that) { return of(this.x + that.x(), this.y + that.y()); }

    public Coord plusXMinusY(Coord that) { return of(this.x + that.getX(), this.y - that.getY()); }

//    public XyOffset maxXandY(XyOffset that) {
//        if ((this.x >= that.x()) && (this.y >= that.y())) { return this; }
//        if ((this.x <= that.x()) && (this.y <= that.y())) { return that; }
//        return of((this.x > that.x()) ? this.x : that.x(),
//                  (this.y > that.y()) ? this.y : that.y());
//    }
    public Coord maxXMinY(Coord that) {
        if ((this.x >= that.getX()) && (this.y <= that.getY())) { return this; }
        if ((this.x <= that.getX()) && (this.y >= that.getY())) { return that; }
        return of((this.x > that.getX()) ? this.x : that.getX(),
                  (this.y < that.getY()) ? this.y : that.getY());
    }

    /** Compares dimensions */
    public boolean lte(Coord that) { return (this.x <= that.getX()) && (this.y >= that.getY()); }

    @Override public String toString() { return "Coord(" + x + ", " + y + ")"; }

    @Override public int hashCode() { return Double.hashCode(x) ^ Double.hashCode(y); }

    @Override public boolean equals(Object other) {
        // Cheapest operation first...
        if (this == other) { return true; }

        if (!(other instanceof Coord)) {
            return false;
        }
        // Details...
        final Coord that = (Coord) other;

        // Compare "significant" fields here.
        return (x == that.getX()) && (y == that.getY());
    }

    public static final Coord ORIGIN = new Coord(0, 0) {
        @Override public int hashCode() { return 0; }
    };
}
