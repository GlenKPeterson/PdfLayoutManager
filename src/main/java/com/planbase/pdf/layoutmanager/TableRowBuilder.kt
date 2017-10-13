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

import java.util.ArrayList
import java.util.Collections

import org.organicdesign.fp.StaticImports.vec

/**
 * Unsynchronized mutable class which is not thread-safe.  The internal tracking of cells and widths
 * allows you to make a cell builder for a cell at a given column, add cells in subsequent columns,
 * then complete (buildCell()) the cell and have it find its proper (now previous) column.
 */
class TableRowBuilder(private val tablePart: TablePart) {
    var textStyle: TextStyle? = tablePart.textStyle
    val cellStyle: CellStyle? = tablePart.cellStyle
    private val cells: MutableList<Cell?> = ArrayList(tablePart.cellWidths.size)
    private var minRowHeight = tablePart.minRowHeight
    private var nextCellIdx = 0

//    private TableRow(TablePart tp, float[] a, Cell[] b, CellStyle c, TextStyle d) {
//        tablePart = tp; cellWidths = a; cells = b; cellStyle = c; textStyle = d;
//    }

    fun nextCellSize(): Float {
        if (tablePart.cellWidths.size <= nextCellIdx) {
            throw IllegalStateException("Tried to add more cells than you set sizes for")
        }
        return tablePart.cellWidths[nextCellIdx]
    }

//    fun textStyle(x: TextStyle): TableRowBuilder {
//        textStyle = x
//        return this
//    }

    fun addCells(vararg cs: Cell): TableRowBuilder {
        Collections.addAll(cells, *cs)
        nextCellIdx += cs.size
        return this
    }

    fun nextCellIdx(): Int {
        return nextCellIdx
    }

    fun addTextCells(vararg ss: String): TableRowBuilder {
        if (textStyle == null) {
            throw IllegalStateException("Tried to add a text cell without setting a default text style")
        }
        for (s in ss) {
            addCellAt(Cell(cellStyle ?: CellStyle.DEFAULT, nextCellSize(), vec(Text(textStyle!!, s))), nextCellIdx)
            nextCellIdx++
        }
        return this
    }

    // TODO: This should be add Renderable Cells.
    fun addJpegCells(vararg js: ScaledJpeg): TableRowBuilder {
        for (j in js) {
            addCellAt(Cell(cellStyle ?: CellStyle.DEFAULT, nextCellSize(), vec(j)), nextCellIdx)
            nextCellIdx++
        }
        return this
    }

    // Because cells are renderable, this would accept one which could result in duplicate cells
    // when Cell.buildCell() creates a cell and passes it in here.
    //    public TableRowBuilder addCell(CellStyle.Align align, Renderable... things) {
    //            cells.add(Cell.builder(this).add(things).build());
    //        return this;
    //    }

    fun addCell(c: Cell): TableRowBuilder {
        cells.add(c)
        nextCellIdx++
        return this
    }

    fun addCellAt(c: Cell, idx: Int): TableRowBuilder {
        // Ensure capacity in the list.
        while (cells.size < idx + 1) {
            cells.add(null)
        }
        if (cells[idx] != null) {
            // System.out.println("Existing cell was: " + cells.get(idx) + "\n Added cell was: " + c);
            throw IllegalStateException("Tried to add a cell built from a table row back to the row after adding a free cell in its spot.")
        }
        cells[idx] = c
        return this
    }

    fun minRowHeight(f: Float): TableRowBuilder {
        minRowHeight = f
        return this
    }

    fun cellBuilder(): RowCellBuilder {
        val cb = RowCellBuilder(this)
        nextCellIdx++
        return cb
    }

    fun buildRow(): TablePart {
        // Do we want to fill out the row with blank cells?
        if (cells.contains(null)) {
            throw IllegalStateException("Cannot build row when some TableRowCellBuilders have been created but the cells not built and added back to the row.")
        }
        return tablePart.addRow(this)
    }

    fun calcDimensions(): XyDim {
        var maxDim = XyDim.ZERO
        // Similar to PdfLayoutMgr.putRow().  Should be combined?
        for (cell in cells) {
            if (cell != null) {
                val wh = cell.calcDimensions(cell.width)
                maxDim = XyDim(wh!!.width + maxDim.width,
                               Math.max(maxDim.height, wh.height))
            }
        }
        return maxDim
    }

    fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        var maxDim = XyDim.ZERO.height(minRowHeight)
        for (cell in cells) {
            if (cell != null) {
                val wh = cell.calcDimensions(cell.width)
                maxDim = XyDim(maxDim.width + cell.width,
                               Math.max(maxDim.height, wh!!.height))
            }
        }
        val maxHeight = maxDim.height

        var x = outerTopLeft.x
        for (cell in cells) {
            if (cell != null) {
                //            System.out.println("\t\tAbout to render cell: " + cell);
                // TODO: Cache the duplicate cell.calcDimensions call!!!
                cell.render(lp, XyOffset(x, outerTopLeft.y),
                            XyDim(cell.width, maxHeight))
                x += cell.width
            }
        }
        return XyOffset(x, outerTopLeft.y - maxHeight)
    }

    override fun toString(): String {
        return StringBuilder("TableRowBuilder(").append(tablePart).append(" ")
                .append(System.identityHashCode(this)).append(")").toString()

    }

    inner class RowCellBuilder(private val tableRowBuilder: TableRowBuilder) : CellBuilder {
        /** {@inheritDoc}  */
        override val width: Float = tableRowBuilder.nextCellSize() // Both require this.
        private var cellStyle: CellStyle? = tableRowBuilder.cellStyle // Both require this.
        private val rows = ArrayList<Renderable>()
        private var textStyle: TextStyle? = tableRowBuilder.textStyle
        private val colIdx: Int = tableRowBuilder.nextCellIdx()

        // I think setting the width after creation is a pretty bad idea for this class since so much
        // is put into getting the width and column correct.
        // public TableRowCellBuilder width(float w) { width = w; return this; }

        /** {@inheritDoc}  */
        override fun cellStyle(cs: CellStyle): RowCellBuilder {
            cellStyle = cs
            return this
        }

        fun borderStyle(bs: BorderStyle): RowCellBuilder {
            cellStyle = cellStyle!!.borderStyle(bs)
            return this
        }

        /** {@inheritDoc}  */
        override fun align(align: CellStyle.Align): RowCellBuilder {
            cellStyle = cellStyle!!.align(align)
            return this
        }

        /** {@inheritDoc}  */
        override fun textStyle(x: TextStyle): RowCellBuilder {
            textStyle = x
            return this
        }

        /** {@inheritDoc}  */
        override fun add(rs: Renderable): RowCellBuilder {
            // TODO: Is this correct???  Adding rows and returning a row cell builder???
            Collections.addAll(rows, rs)
            return this
        }

        /** {@inheritDoc}  */
        override fun addAll(js: Collection<Renderable>): RowCellBuilder {
            rows.addAll(js)
            return this
        }

        /** {@inheritDoc}  */
        override fun addStrs(vararg ss: String): RowCellBuilder {
            if (textStyle == null) {
                throw IllegalStateException("Must set a default text style before adding" + " raw strings")
            }
            for (s in ss) {
                rows.add(Text(textStyle!!, s))
            }
            return this
        }

        /** {@inheritDoc}  */
        override fun add(ts: TextStyle, ls: Iterable<String>): RowCellBuilder {
            for (s in ls) {
                rows.add(Text(ts, s))
            }
            return this
        }

        fun buildCell(): TableRowBuilder {
            val c = Cell(cellStyle!!, width, rows)
            return tableRowBuilder.addCellAt(c, colIdx)
        }

        override fun toString(): String {
            return StringBuilder("RowCellBuilder(").append(tableRowBuilder).append(" colIdx=")
                    .append(colIdx).append(")").toString()
        }

        override fun hashCode(): Int {
            return tableRowBuilder.hashCode() + colIdx
        }

        override fun equals(other: Any?): Boolean {
            // Cheapest operation first...
            if (this === other) {
                return true
            }

            if (other == null ||
                other !is RowCellBuilder ||
                this.hashCode() != other.hashCode()) {
                return false
            }
            // Details...
            val that = other as RowCellBuilder?

            return this.colIdx == that!!.colIdx && tableRowBuilder == that.tableRowBuilder
        }
    }

    companion object {

        fun of(tp: TablePart): TableRowBuilder {
            return TableRowBuilder(tp)
        }
    }
}
