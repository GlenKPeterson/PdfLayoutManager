// Copyright 2014-08-14 PlanBase Inc. & Glen Peterson
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
 * Implementing Renderable means being suitable for use with a two-pass layout manager whose first pass says, "given
 * this width, what is your height?" and second pass says, "Given these dimensions, draw yourself as best you can."
 */
// TODO: Split into Layoutable and Renderable where Layoutable just has calcDimensions() which returns a Renderable which just has render() (but without maxWidth) and getXyDim()
public interface Renderable {
    /**
    Given a width, returns the height and actual width after line wrapping.  If line wrapping is
    not needed, just returns the static width and height.  If calculations are done, the results
    should be cached because render() will likely be called with the same width (or at least one
    previously given widths).
     */
    public Dim calcDimensions(double maxWidth);

    /**
     Only call this with a maxWidth that you have previously passed to calcDimensions.
     Renders item and all child-items with given width and returns the x-y pair of the
     lower-right-hand corner of the last line (e.g. of text).
    */
    public Coord render(LogicalPage lp, Coord outerTopLeft, Dim outerDimensions,
                        boolean allPages);
}
