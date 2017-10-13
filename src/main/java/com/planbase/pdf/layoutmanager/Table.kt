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

package com.planbase.pdf.layoutmanager

/**
 *
 * It used to be that you'd build a table and that act would commit it to a logical page.
 */
// TODO: This should probably freeze all the underlying stuff, but good enough for now.
class Table(private val parts: List<TablePart>) : FixedItem {
    override val xyDim: XyDim = parts.fold(XyDim.ZERO,
                                           { acc, part -> acc.plus(part.calcDimensions()) })
    override val ascent: Float = xyDim.height
    override val descentAndLeading: Float = 0f
    override val lineHeight: Float = xyDim.height
    //    @Override  public XyDim calcDimensions(float maxWidth) {
    //        XyDim maxDim = XyDim.ZERO;
    //        for (TablePart part : parts) {
    //            XyDim wh = part.calcDimensions();
    //            maxDim = XyDim.of(Math.max(wh.width(), maxDim.width()),
    //                              maxDim.height() + wh.height());
    //        }
    //        return maxDim;
    //    }


    /*
        Renders item and all child-items with given width and returns the x-y pair of the
        lower-right-hand corner of the last line (e.g. of text).
        */
    override fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        var rightmostLowest = outerTopLeft
        for (part in parts) {
            //            System.out.println("About to render part: " + part);
            val rl = part.render(lp, XyOffset(outerTopLeft.x, rightmostLowest.y))
            rightmostLowest = XyOffset(Math.max(rl.x, rightmostLowest.x),
                                       Math.min(rl.y, rightmostLowest.y))
        }
        return rightmostLowest
    }
}
