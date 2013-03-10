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
    public static final Padding DEFAULT_PADDING = Padding.valueOf(1.5f, 1.5f, 2f, 1.5f);

    private final float top;
    private final float right;
    private final float bottom;
    private final float left;
    private Padding(final float t, final float r, final float b, final float l) {
        top = t; right = r; bottom = b; left = l;
    }
    /** Like HTML it's top, right, bottom, left */
    public static Padding valueOf(final float t, final float r, final float b,
                                   final float l) {
        return new Padding(t, r, b, l);
    }
    /** Sets all padding values equally */
    public static Padding valueOf(final float a) {
        return new Padding(a,a,a,a);
    }

    @Override
    public boolean equals(Object other) {
        // Cheapest operation first...
        if (this == other) { return true; }
        if ( (other == null) ||
             !(other instanceof Padding) ||
             (this.hashCode() != other.hashCode()) ) {
            return false;
        }
        // Details...
        final Padding that = (Padding) other;

        return (this.top == that.top) &&
               (this.right == that.right) &&
               (this.bottom == that.bottom) &&
               (this.left == that.left);
    }

    @Override
    public int hashCode() {
        return (int) ((top + right + bottom + left) * 10);
    }

    public float top() { return top; }
    public float right() { return right; }
    public float bottom() { return bottom; }
    public float left() { return left; }
}
