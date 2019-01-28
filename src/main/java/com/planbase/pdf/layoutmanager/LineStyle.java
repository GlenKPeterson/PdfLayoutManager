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
 Represents the color and width of a line.  DashStyle (and maybe capStyle or joinStyle?) could be
 added later.  Immutable.
 */
public class LineStyle {
    public static final double DEFAULT_WIDTH = 1;

    private final Color color;
    private final double width;

    private LineStyle(Color c, double w) {
        if (c == null) { throw new IllegalArgumentException("Line Style must have a color."); }
        if (w <= 0) {
            throw new IllegalArgumentException("Line Style must have a positive width.");
        }
        color = c; width = w;
    }
    /** Factory method */
    public static LineStyle of(Color c, double w) { return new LineStyle(c, w); }

    public static LineStyle of(Color c) { return new LineStyle(c, DEFAULT_WIDTH); }

    public Color color() { return color; }
    public double width() { return width; }

    @Override
    public int hashCode() {
        int code = (int) width * 100;
        code |= color.hashCode();
        return code;
    }

    @Override
    public boolean equals(Object other) {
        // Cheapest operation first...
        if (this == other) { return true; }
        // Ensure compatibility with HashCode
        if ( (other == null) ||
             !(other instanceof LineStyle) ||
             (this.hashCode() != other.hashCode()) ) {
            return false;
        }
        // Make a meaningful content-based comparison
        final LineStyle that = (LineStyle) other;
        return (this.width == that.width) && this.color().equals(that.color());
    }

    @Override public String toString() {
        return "LineStyle(" + Utils.toString(color) + " w=" + width + ")";
    }
}
