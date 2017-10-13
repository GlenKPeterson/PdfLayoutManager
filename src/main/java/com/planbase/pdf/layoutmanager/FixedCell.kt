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
 TODO: This should be a private inner class of Cell, but it's easer to break it out and work on it in Kotlin.
 */
class FixedCell(override val xyDim: XyDim,
                override val ascent: Float,
                override val descentAndLeading: Float,
                override val lineHeight: Float,
                val pcls: Cell.PreCalcLines,
                val cellStyle: CellStyle) : FixedItem {

    override fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        val padding = cellStyle.padding
        // XyDim xyDim = padding.addTo(pcrs.dim);

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (cellStyle.bgColor != null) {
            //            System.out.println("\tCell.render calling putRect...");
            lp.fillRect(outerTopLeft, xyDim, cellStyle.bgColor)
            //            System.out.println("\tCell.render back from putRect");
        }

        // Draw contents over background, but under border
        var innerTopLeft: XyOffset
        val innerDimensions: XyDim
        if (padding == null) {
            innerTopLeft = outerTopLeft
            innerDimensions = xyDim
        } else {
            //            System.out.println("\tCell.render outerTopLeft before padding=" + outerTopLeft);
            innerTopLeft = padding.applyTopLeft(outerTopLeft)
            //            System.out.println("\tCell.render innerTopLeft after padding=" + innerTopLeft);
            innerDimensions = padding.subtractFrom(xyDim)
        }
        val wrappedBlockDim = pcls.totalDim
//        System.out.println("\tCell.render cellStyle.align()=" + cellStyle.align());
//        System.out.println("\tCell.render xyDim=" + xyDim);
//        System.out.println("\tCell.render padding=" + padding);
//        System.out.println("\tCell.render innerDimensions=" + innerDimensions);
//        System.out.println("\tCell.render wrappedBlockDim=" + wrappedBlockDim);
        val alignPad = cellStyle.align.calcPadding(innerDimensions, wrappedBlockDim)
//        System.out.println("\tCell.render alignPad=" + alignPad);
        innerTopLeft = XyOffset(innerTopLeft.x + alignPad.left,
                                innerTopLeft.y - alignPad.top)

        var outerLowerRight = innerTopLeft
        var y = innerTopLeft.y
        for (line in pcls.lines) {
            val rowXOffset = cellStyle.align
                    .leftOffset(wrappedBlockDim.width, line.width)
            outerLowerRight = line.render(lp,
                                          XyOffset(rowXOffset + innerTopLeft.x, y))
            y -= line.height()
            //            innerTopLeft = outerLowerRight.x(innerTopLeft.x);
        }

        // Draw border last to cover anything that touches it?
        val border = cellStyle.borderStyle
        if (border != null) {
            val origX = outerTopLeft.x
            val origY = outerTopLeft.y
            val rightX = outerTopLeft.x + xyDim.width

            // This breaks cell rows in order to fix rendering content after images that fall
            // mid-page-break.  Math.min() below is so that when the contents overflow the bottom
            // of the cell, we adjust the cell border downward to match.  We aren't doing the same
            // for the background color, or for the rest of the row, so that's going to look bad.
            //
            // To fix these issues, I think we need to make that adjustment in the pre-calc instead
            // of here.  Which means that the pre-calc needs to be aware of page breaking because
            // the code that causes this adjustment is PdfLayoutMgr.appropriatePage().  So we
            // probably need a fake version of that that doesn't cache anything for display on the
            // page, then refactor backward from there until we enter this code with pre-corrected
            // outerLowerRight and can get rid of Math.min.
            //
            // When we do that, we also want to check PageGrouping.drawJpeg() and .drawPng()
            // to see if `return y + pby.adj;` still makes sense.
            val bottomY = Math.min(outerTopLeft.y - xyDim.height,
                                   outerLowerRight.y)

            // Like CSS it's listed Top, Right, Bottom, left
            if (border.top != null) {
                lp.drawLine(origX, origY, rightX, origY, border.top)
            }
            if (border.right != null) {
                lp.drawLine(rightX, origY, rightX, bottomY, border.right)
            }
            if (border.bottom != null) {
                lp.drawLine(origX, bottomY, rightX, bottomY, border.bottom)
            }
            if (border.left != null) {
                lp.drawLine(origX, origY, origX, bottomY, border.left)
            }
        }

        return outerLowerRight
    }
}