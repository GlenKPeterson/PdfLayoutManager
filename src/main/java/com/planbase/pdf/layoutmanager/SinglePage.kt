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

import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.organicdesign.fp.function.Fn2
import org.organicdesign.fp.oneOf.Option

import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.io.IOException
import java.util.TreeSet

/**
 * Caches the contents of a specific, single page for later drawing.  Inner classes are what's added
 * to the cache and what controlls the drawing.  You generally want to use [PageGrouping] when
 * you want automatic page-breaking.  SinglePage is for when you want to force something onto a
 * specific page only.
 */
class SinglePage internal constructor(val pageNum: Int, private val mgr: PdfLayoutMgr, pr: Option<Fn2<Int, SinglePage, Float>>) : RenderTarget {
    // The x-offset for the body section of this page (left-margin-ish)
    private val xOff: Float
    private var lastOrd: Long = 0
    private val items = TreeSet<PdfItem>()

    init {
        xOff = pr.match({ r -> r.apply(pageNum, this) }
                       ) { 0f }
    }

    internal fun fillRect(x: Float, y: Float, width: Float, height: Float, c: PDColor, zIdx: Float) {
        items.add(FillRect(x + xOff, y, width, height, c, lastOrd++, zIdx))
    }

    /** {@inheritDoc}  */
    override fun fillRect(topLeft: XyOffset, dim: XyDim, c: PDColor): SinglePage {
        fillRect(topLeft.x, topLeft.y, dim.width, dim.height, c, -1f)
        return this
    }
    //        public void fillRect(final float xVal, final float yVal, final float w, final PDColor c,
    //                             final float h) {
    //            fillRect(xVal, yVal, w, h, c, PdfItem.DEFAULT_Z_INDEX);
    //        }
    //
    //        public void drawJpeg(final float xVal, final float yVal, final BufferedImage bi,
    //                             final PdfLayoutMgr mgr, final float z) {
    //            items.add(DrawJpeg.of(xVal, yVal, bi, mgr, lastOrd++, z));
    //        }

    /** {@inheritDoc}  */
    override fun drawJpeg(x: Float, y: Float, sj: ScaledJpeg): Float {
        items.add(DrawJpeg(x + xOff, y, sj, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX))
        // This does not account for a page break because this class represents a single page.
        return sj.xyDim.height
    }

    /** {@inheritDoc}  */
    override fun drawPng(x: Float, y: Float, sj: ScaledPng): Float {
        items.add(DrawPng(x + xOff, y, sj, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX))
        // This does not account for a page break because this class represents a single page.
        return sj.xyDim.height
    }

    private fun drawLine(xa: Float, ya: Float, xb: Float, yb: Float, ls: LineStyle, z: Float) {
        items.add(DrawLine(xa + xOff, ya, xb + xOff, yb, ls, lastOrd++, z))
    }

    /** {@inheritDoc}  */
    override fun drawLine(xa: Float, ya: Float, xb: Float, yb: Float, ls: LineStyle): SinglePage {
        drawLine(xa, ya, xb, yb, ls, PdfItem.DEFAULT_Z_INDEX)
        return this
    }

    private fun drawStyledText(x: Float, y: Float, text: String, s: TextStyle, z: Float) {
        items.add(Text(x + xOff, y, text, s, lastOrd++, z))
    }

    /** {@inheritDoc}  */
    override fun drawStyledText(x: Float, y: Float, text: String, s: TextStyle): SinglePage {
        drawStyledText(x, y, text, s, PdfItem.DEFAULT_Z_INDEX)
        return this
    }

    @Throws(IOException::class)
    fun commit(stream: PDPageContentStream) {
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (item in items) {
            item.commit(stream)
        }
    }

    private class DrawLine(private val x1: Float,
                           private val y1: Float,
                           private val x2: Float,
                           private val y2: Float,
                           private val style: LineStyle,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setStrokingColor(style.color)
            stream.setLineWidth(style.width)
            stream.moveTo(x1, y1)
            stream.lineTo(x2, y2)
            stream.stroke()
        }
    }

    private class FillRect(val x: Float,
                           val y: Float,
                           val width: Float,
                           val height: Float,
                           val color: PDColor,
                           ord: Long,
                           z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.setNonStrokingColor(color)
            stream.addRect(x, y, width, height)
            stream.fill()
        }
    }

    internal class Text(val x: Float, val y: Float, val t: String, val style: TextStyle,
                        ord: Long, z: Float) : PdfItem(ord, z) {
        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            stream.beginText()
            stream.setNonStrokingColor(style.textColor)
            stream.setFont(style.font, style.fontSize)
            stream.newLineAtOffset(x, y)
            stream.showText(t)
            stream.endText()
        }
    }

    private class DrawPng(private val x: Float,
                          private val y: Float,
                          private val scaledPng: ScaledPng,
                          mgr: PdfLayoutMgr,
                          ord: Long, z: Float) : PdfItem(ord, z) {
        private val png: PDImageXObject

        init {
            png = mgr.ensureCached(scaledPng)
        }

        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            // stream.drawImage(png, x, y);
            val (width, height) = scaledPng.xyDim
            stream.drawImage(png, x, y, width, height)
        }
    }

    private class DrawJpeg(val x: Float,
                           val y: Float,
                           val scaledJpeg: ScaledJpeg,
                           mgr: PdfLayoutMgr,
                           ord: Long, z: Float) : PdfItem(ord, z) {
        private val jpeg: PDImageXObject

        init {
            jpeg = mgr.ensureCached(scaledJpeg)
        }

        @Throws(IOException::class)
        override fun commit(stream: PDPageContentStream) {
            // stream.drawImage(jpeg, x, y);
            val (width, height) = scaledJpeg.xyDim
            stream.drawImage(jpeg, x, y, width, height)
        }
    }
}