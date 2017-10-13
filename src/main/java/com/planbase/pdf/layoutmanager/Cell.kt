// Copyright 2013-03-03 PlanBase Inc.
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
import java.util.HashMap

import java.lang.Math.max

/**
 A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated
 based on how the content is rendered with regard to line-breaks and page-breaks.
 */
data class Cell(val cellStyle: CellStyle = CellStyle.DEFAULT, // contents can override this style
                val width: Float,
                // A list of the contents.  It's pretty limiting to have one item per row.
                private val contents: List<Renderable>) : Renderable {

    private constructor(b:Builder) : this(b.cellStyle ?: CellStyle.DEFAULT, b.width, b.rows)

    // Caches XyDims for all content lines, indexed by desired width (we only have to lay-out again
    // when the width changes.
    private val widthCache = HashMap<Float, PreCalcLines>(0)

    // A cache for all pre-calculated lines.
    class PreCalcLines(val lines: List<Line> = ArrayList(1),
                       val totalDim: XyDim)

    init {
        if (width < 0) {
            throw IllegalArgumentException("A cell cannot have a negative width")
        }
    }

    private fun calcDimensionsForReal(maxWidth: Float) {
        val padding = cellStyle.padding
        var innerWidth = maxWidth
        if (padding != null) {
            innerWidth -= padding.left + padding.right
        }
        val lines: List<Line> = renderablesToLines(contents, innerWidth)
        var width = 0f
        var height = 0f
        for (line in lines) {
            width = max(width, line.width)
            height += line.height()
        }
        widthCache.put(maxWidth, PreCalcLines(lines, XyDim(width, height)))
    }

    private fun ensurePreCalcLines(maxWidth: Float): PreCalcLines {
        var pcl: PreCalcLines? = widthCache[maxWidth]
        if (pcl == null) {
            calcDimensionsForReal(maxWidth)
            pcl = widthCache[maxWidth]
        }
        return pcl!!
    }

    override fun renderator(): CellRenderator {
        return CellRenderator(this)
    }


    /** {@inheritDoc}  */
    fun calcDimensions(maxWidth: Float): XyDim? {
        // I think zero or negative width cells might be OK to ignore.  I'd like to try to make
        // Text.calcDimensionsForReal() handle this situation before throwing an error here.
        //        if (maxWidth < 0) {
        //            throw new IllegalArgumentException("maxWidth must be positive, not " + maxWidth);
        //        }
        val blockDim = ensurePreCalcLines(maxWidth).totalDim
        return if (cellStyle.padding == null) blockDim else cellStyle.padding.addTo(blockDim)
        //        System.out.println("Cell.calcDimensions(" + maxWidth + ") dim=" + dim +
        //                           " returns " + ret);
    }

    // TODO!
    inner class CellRenderator(c: Cell) : Renderator {

        override fun hasMore(): Boolean {
            return false
        }

        override fun getSomething(maxWidth: Float): ContTerm? {
            return null
        }

        override fun getIfFits(remainingWidth: Float): ContTermNone? {
            return null
        }
    }

    /*
    Renders item and all child-lines with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).

    {@inheritDoc}
    */
    fun render(lp: RenderTarget, outerTopLeft: XyOffset, outerDimensions: XyDim): XyOffset {
        //        System.out.println("Cell.render(" + this.toString());
        //        new Exception().printStackTrace();

        val maxWidth:Float = outerDimensions.width
        val pcls = ensurePreCalcLines(maxWidth)
        val padding = cellStyle.padding
        // XyDim outerDimensions = padding.addTo(pcrs.dim);

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (cellStyle.bgColor != null) {
            //            System.out.println("\tCell.render calling putRect...");
            lp.fillRect(outerTopLeft, outerDimensions, cellStyle.bgColor)
            //            System.out.println("\tCell.render back from putRect");
        }

        // Draw contents over background, but under border
        var innerTopLeft: XyOffset
        val innerDimensions: XyDim
        if (padding == null) {
            innerTopLeft = outerTopLeft
            innerDimensions = outerDimensions
        } else {
            //            System.out.println("\tCell.render outerTopLeft before padding=" + outerTopLeft);
            innerTopLeft = padding.applyTopLeft(outerTopLeft)
            //            System.out.println("\tCell.render innerTopLeft after padding=" + innerTopLeft);
            innerDimensions = padding.subtractFrom(outerDimensions)
        }
        val wrappedBlockDim = pcls.totalDim
        //        System.out.println("\tCell.render cellStyle.align()=" + cellStyle.align());
        //        System.out.println("\tCell.render outerDimensions=" + outerDimensions);
        //        System.out.println("\tCell.render padding=" + padding);
        //        System.out.println("\tCell.render innerDimensions=" + innerDimensions);
        //        System.out.println("\tCell.render wrappedBlockDim=" + wrappedBlockDim);
        val alignPad = cellStyle.align.calcPadding(innerDimensions, wrappedBlockDim)
        //        System.out.println("\tCell.render alignPad=" + alignPad);
        innerTopLeft = XyOffset(innerTopLeft.x + alignPad.left,
                                innerTopLeft.y - alignPad.top)

        var outerLowerRight = innerTopLeft
        var y:Float = innerTopLeft.y
        for (line in pcls.lines) {
            val rowXOffset = cellStyle.align
                    .leftOffset(wrappedBlockDim.width, line.width)
            outerLowerRight = line.render(lp,
                                          XyOffset(rowXOffset + innerTopLeft.x, y))
            y -= line.height()
            //            innerTopLeft = outerLowerRight.x(innerTopLeft.x());
        }

        // Draw border last to cover anything that touches it?
        val border = cellStyle.borderStyle
        if (border != null) {
            val origX:Float = outerTopLeft.x
            val origY:Float = outerTopLeft.y
            val rightX:Float = outerTopLeft.x + outerDimensions.width

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
            val bottomY = Math.min(outerTopLeft.y - outerDimensions.height,
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

    // Replaced with TableRow.CellBuilder()
    //    /**
    //     Be careful when adding multiple cell builders at once because the cell size is based upon
    //     a pointer into the list of cell sizes.  That pointer gets incremented each time a cell is
    //     added, not each time nextCellSize() is called.  Is this a bug?  Or would fixing it create
    //     too many other bugs?
    //     @param trb
    //     @return
    //     */
    //    public static Builder builder(TableRowBuilder trb) {
    //        Builder b = new Builder(trb.cellStyle(), trb.nextCellSize()).textStyle(trb.textStyle());
    //        b.trb = trb;
    //        return b;
    //    }

    /**
     * A mutable Builder for somewhat less mutable cells.
     */
    class Builder(var cellStyle: CellStyle?,
                  override val width: Float,
                  val rows:MutableList<Renderable>,
                  var textStyle: TextStyle?) : CellBuilder {
        constructor(cs:CellStyle, w:Float) : this(cs, w, mutableListOf(), null)

        // Is this necessary?
        //        public Builder width(float w) { width = w; return this; }

        /** {@inheritDoc}  */
        override fun cellStyle(cs: CellStyle): Builder {
            cellStyle = cs
            return this
        }

        /** {@inheritDoc}  */
        override fun align(align: CellStyle.Align): Builder {
            cellStyle = cellStyle?.align(align) ?: CellStyle(align, null, null, null)
            return this
        }

        /** {@inheritDoc}  */
        override fun textStyle(x: TextStyle): Builder {
            textStyle = x
            return this
        }

        /** {@inheritDoc}  */
        override fun add(rs: Renderable): Builder {
            rows.add(rs)
            return this
        }

        /** {@inheritDoc}  */
        override fun addAll(js: Collection<Renderable>): Builder {
            rows.addAll(js)
            return this
        }

        /** {@inheritDoc}  */
        override fun add(ts: TextStyle, ls: Iterable<String>): Builder {
            for (s in ls) {
                rows.add(Text(ts, s))
            }
            return this
        }

        /** {@inheritDoc}  */
        override fun addStrs(vararg ss: String): Builder {
            if (textStyle == null) {
                throw IllegalStateException("Must set a default text style before adding raw strings")
            }
            for (s in ss) {
                rows.add(Text(textStyle!!, s))
            }
            return this
        }
        //        public Builder add(Cell c) { contents.add(c); return this; }

        fun build(): Cell {
            return Cell(cellStyle!!, width, rows)
        }

        // Replaced with TableRow.CellBuilder.buildCell()
        //        public TableRowBuilder buildCell() {
        //            Cell c = new Cell(cellStyle, width, contents);
        //            return trb.addCell(c);
        //        }

        /** {@inheritDoc}  */
        override fun toString(): String {
            val sB = StringBuilder("Cell.Builder(").append(cellStyle).append(" width=")
                    .append(width).append(" contents=[")

            var i = 0
            while (i < rows.size && i < 3) {
                if (i > 0) {
                    sB.append(" ")
                }
                sB.append(rows[i])
                i++
            }
            return sB.append("])").toString()
        }
    }

    /** {@inheritDoc}  */
    override fun toString(): String {
        val sB = StringBuilder("Cell(").append(cellStyle).append(" width=")
                .append(width).append(" contents=[")

        var i = 0
        while (i < contents.size && i < 3) {
            if (i > 0) {
                sB.append(" ")
            }
            sB.append(contents[i])
            i++
        }
        return sB.append("])").toString()
    }

    companion object {

        /**
         * Creates a new cell with the given style and width.
         *
         * @param cs    the cell style
         * @param width the width (height will be calculated based on how objects can be rendered within
         * this width).
         * @return a cell suitable for rendering.
         */
        fun of(cs: CellStyle, width: Float): Cell { //, final Object... r) {
            return Cell(cs, width, emptyList())
            //                        (r == null) ? Collections.emptyList()
            //                                    : Arrays.asList(r));
        }

        // Simple case of a single styled String
        fun of(cs: CellStyle, width: Float, ts: TextStyle, s: String): Cell {
            val ls = ArrayList<Renderable>(1)
            ls.add(Text(ts, s))
            return Cell(cs, width, ls)
        }

        // Simple case of a single styled String
        fun of(cs: CellStyle, width: Float, t: Text): Cell {
            val ls = ArrayList<Renderable>(1)
            ls.add(t)
            return Cell(cs, width, ls)
        }

        fun of(cs: CellStyle, width: Float, j: ScaledJpeg): Cell {
            val ls = ArrayList<Renderable>(1)
            ls.add(j)
            return Cell(cs, width, ls)
        }

        fun of(cs: CellStyle, width: Float, r: Renderable): Cell {
            val ls = ArrayList<Renderable>(1)
            ls.add(r)
            return Cell(cs, width, ls)
        }

        fun of(cs: CellStyle, width: Float, ls: List<Renderable>): Cell {
            return Cell(cs, width, ls)
        }

        // Simple case of a single styled String
        fun of(cs: CellStyle, width: Float, c: Cell): Cell {
            val ls = ArrayList<Renderable>(1)
            ls.add(c)
            return Cell(cs, width, ls)
        }

        fun builder(cellStyle: CellStyle, width: Float): Builder {
            return Builder(cellStyle, width)
        }
    }
}
