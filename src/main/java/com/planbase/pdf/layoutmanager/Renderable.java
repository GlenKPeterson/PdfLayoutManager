// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr2
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager;

/**
 Implementing Renderable means being suitable for use with a two-pass layout manager whose first
 pass says, "given this width, what is your height?" and second pass says, "Given these dimensions,
 draw yourself as best you can."
 */
// TODO: Split into Layoutable and Renderable where Layoutable just has calcDimensions() which returns a Renderable which just has render() (but without maxWidth) and getXyDim()
public interface Renderable {
//    enum Constants implements Renderable {
//        EOL {
////            @Override
////            public XyDim calcDimensions(float maxWidth) {
////                return null;
////            }
//
////            @Override
////            public XyOffset render(RenderTarget lp, XyOffset outerTopLeft, XyDim outerDimensions) {
////                return null;
////            }
//        };
//
//    }

//    /**
//    Given a width, returns the height and actual width after line wrapping.  If line wrapping is
//    not needed, just returns the static width and height.  If calculations are done, the results
//    should be cached because render() will likely be called with the same width (or at least one
//    previously given widths).
//     */
//    XyDim calcDimensions(float maxWidth);

//    /**
//     Only call this with a maxWidth that you have previously passed to calcDimensions.
//     Renders item and all child-items with given width and returns the x-y pair of the
//     lower-right-hand corner of the last line (e.g. of text).
//     @param lp the place to render to (either a single page, or logical collection of pages)
//     @param outerTopLeft the top-left position to render to
//     @param outerDimensions the width and height of the thing to render.
//     @return the bottom-right corner of the rendered result.  This is not necessarily
//     the same as topLeft + outerDimensions (it could be on a different page).
//    */
//    XyOffset render(RenderTarget lp, XyOffset outerTopLeft, XyDim outerDimensions);

    Renderator renderator();
//    {
//        return new Renderator.SingleItemRenderator(this);
//    }

}
