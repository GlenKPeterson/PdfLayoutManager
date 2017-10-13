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

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 * Represents the color and width of a line.  DashStyle (and maybe capStyle or joinStyle?) could be
 * added later.  Immutable.
 */
data class LineStyle(val color: PDColor, val width: Float) {
    constructor(color: PDColor) : this(color, DEFAULT_WIDTH)

    init {
        if (width <= 0) { throw IllegalArgumentException("Line Style must have a positive width.") }
    }

    companion object {
        val DEFAULT_WIDTH = 1f
    }
}
