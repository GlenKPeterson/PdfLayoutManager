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

import java.util.ArrayList
import java.util.Collections

/**
 * A set of styles to be the default for a table header or footer, or whatever other kind of group of table rows you
 * dream up.
 */
class TablePart(private val tableBuilder: TableBuilder) {
    val cellWidths:List<Float> = tableBuilder.cellWidths.toList()
    var cellStyle: CellStyle? = tableBuilder.cellStyle
    var textStyle: TextStyle? = tableBuilder.textStyle
    var minRowHeight = 0f
    private val rows = ArrayList<TableRowBuilder>(1)

//    fun cellStyle(x: CellStyle): TablePart {
//        cellStyle = x
//        return this
//    }

    fun align(a: CellStyle.Align): TablePart {
        cellStyle = cellStyle!!.align(a)
        return this
    }

    //    public TablePart cellStyle(CellStyle x) { return new Builder().cellStyle(cellStyle).build(); }

//    fun textStyle(x: TextStyle): TablePart {
//        textStyle = x
//        return this
//    }

//    fun minRowHeight(f: Float): TablePart {
//        minRowHeight = f
//        return this
//    }

    fun rowBuilder(): TableRowBuilder {
        return TableRowBuilder(this)
    }

    fun addRow(trb: TableRowBuilder): TablePart {
        rows.add(trb)
        return this
    }

    fun buildPart(): TableBuilder {
        return tableBuilder.addPart(this)
    }

    fun calcDimensions(): XyDim {
        var maxDim = XyDim.ZERO
        for (row in rows) {
            val (width, height) = row.calcDimensions()
            maxDim = XyDim(Math.max(width, maxDim.width),
                           maxDim.height + height)
        }
        return maxDim
    }

    fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        var rightmostLowest = outerTopLeft
        for (row in rows) {
            //            System.out.println("\tAbout to render row: " + row);
            val (x, y) = row.render(lp, XyOffset(outerTopLeft.x, rightmostLowest.y))
            rightmostLowest = XyOffset(Math.max(x, rightmostLowest.x),
                                       Math.min(y, rightmostLowest.y))
        }
        return rightmostLowest
    }

    override fun toString(): String {
        return "TablePart(" + tableBuilder + " " + System.identityHashCode(this) + ")"
    }

    //    public static Builder builder(TableBuilder t) { return new Builder(t); }
    //
    //    public static class Builder {
    //        private final TableBuilder tableBuilder;
    //        private float[] cellWidths;
    //        private CellStyle cellStyle;
    //        private TextStyle textStyle;
    //
    //        private Builder(TableBuilder t) { tableBuilder = t; }
    //
    //        public Builder cellWidths(float[] x) { cellWidths = x; return this; }
    //        public Builder cellStyle(CellStyle x) { cellStyle = x; return this; }
    //        public Builder textStyle(TextStyle x) { textStyle = x; return this; }
    //
    //        public TablePart build() { return new TablePart(tableBuilder, cellWidths, cellStyle, textStyle); }
    //    } // end of class Builder
}
