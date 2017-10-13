// Copyright 2013-03-03 PlanBase Inc.
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

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 * Holds the LineStyles for the top, right, bottom, and left borders of a PdfItem.  For an equal
 * border on all sides, use:
 * <pre>`BorderStyle b = new BorderStyle(color, width);`</pre>
 * For an unequal border, use named parameters in Kotlin.
 * This class works just like styles in CSS in terms of specifying one style, then
 * overwriting it with another.  This example sets all borders except to black and the default width,
 * then removes the top border:
 * <pre>`BorderStyle topBorderStyle = BorderStyle(LineStyle(PDColor.BLACK))
 * .top(LineStyle(PDColor.RED, 2f));`</pre>
 * If neighboring cells in a cell-row have the same border, only one will be printed.  If different,
 * the left-border of the right cell will override.  You have to manage your own top borders
 * manually.
 *
 */
// Like CSS it's listed Top, Right, Bottom, left
data class BorderStyle(val top: LineStyle?,
                       val right: LineStyle?,
                       val bottom: LineStyle?,
                       val left: LineStyle?) {

    /**
     * Returns an equal border on all sides
     * @param ls the line style
     * @return a new immutable border object
     */
    constructor(ls:LineStyle) : this(ls, ls, ls, ls)

    /**
     * Returns an equal border on all sides
     * @param c the border color
     * @param w the width of the border.
     * @return a new immutable border object
     */
    constructor(c: PDColor, w: Float) : this (LineStyle(c, w))

    /**
     * Returns an equal border on all sides
     * @param c the border color
     * @return a new immutable border object with default width
     */
    constructor(c: PDColor) : this (LineStyle(c))

    fun top(ls: LineStyle) = BorderStyle(ls, right, bottom, left)
    fun right(ls: LineStyle) = BorderStyle(top, ls, bottom, left)
    fun bottom(ls: LineStyle) = BorderStyle(top, right, ls, left)
    fun left(ls: LineStyle) = BorderStyle(top, right, bottom, ls)

    companion object {
        val NO_BORDERS = BorderStyle(null, null, null, null)
    }
}
