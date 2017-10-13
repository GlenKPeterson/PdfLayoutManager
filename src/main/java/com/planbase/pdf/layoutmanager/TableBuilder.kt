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
 * Use this to create Tables.  This strives to remind the programmer of HTML tables but because you
 * can resize and scroll a browser window, and not a piece of paper, this is fundamentally different.
 * Still familiarity with HTML may make this class easier to use.
 */
class TableBuilder(val cellWidths:MutableList<Float> = mutableListOf(),
                   var cellStyle: CellStyle? = null,
                   var textStyle: TextStyle? = null,
                   val parts:MutableList<TablePart> = mutableListOf()) {


    /** Sets default widths for all table parts.  */
    fun addCellWidths(x: List<Float>): TableBuilder {
        cellWidths.addAll(x)
        return this
    }

    fun addCellWidths(vararg ws: Float): TableBuilder {
        for (w in ws) {
            cellWidths.add(w)
        }
        return this
    }

    fun addCellWidth(x: Float): TableBuilder {
        cellWidths.add(x)
        return this
    }

    fun cellStyle(x: CellStyle): TableBuilder {
        cellStyle = x
        return this
    }

    fun textStyle(x: TextStyle): TableBuilder {
        textStyle = x
        return this
    }

    fun addPart(tp: TablePart): TableBuilder {
        parts.add(tp)
        return this
    }

    fun partBuilder(): TablePart {
        return TablePart(this)
    }

    fun buildTable() = Table(parts)
}
