// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr
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

/** Represents a fixed-size item  */

interface FixedItem {
    val xyDim: XyDim
//    fun width(): Float = width
//    fun totalHeight(): Float = heightAboveBase + depthBelowBase

    /** Height above the baseline of this line */
    val ascent: Float

    /** Depth below the baseline of this line */
    val descentAndLeading: Float

    /** Total vertical height this line, both above and below the baseline */
    val lineHeight: Float

    fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset
}

fun fixedItemRenderator(item:FixedItem): Renderator =
        object : Renderator {
            internal var hasMore = true
            override fun hasMore(): Boolean = hasMore

            override fun getSomething(maxWidth: Float): ContTerm {
                hasMore = false
                return ContTerm.continuing(item)
            }

            override fun getIfFits(remainingWidth: Float): ContTermNone =
                    if (hasMore && (item.xyDim.width <= remainingWidth)) {
                        hasMore = false
                        Continuing(item)
                    } else {
                        None
                    }
        }