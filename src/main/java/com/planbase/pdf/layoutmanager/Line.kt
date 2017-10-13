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

import org.organicdesign.fp.StaticImports.mutableVec
import org.organicdesign.fp.collections.ImList
import org.organicdesign.fp.collections.MutList

/**
Represents a continuing or terminal FixedItem where Continuing means there could be more on this
line (no hard line break) and Terminal means a hard-coded line-break was encountered.
 */
data class ContTerm(val item: FixedItem, val foundCr: Boolean) {
    fun toContTermNone() : ContTermNone =
            if (foundCr) {
                Terminal(item)
            } else {
                Continuing(item)
            }
    companion object {
        /** Construct a new Continuing from the given object.  */
        fun continuing(continuing: FixedItem): ContTerm = ContTerm(continuing, false)

        /** Construct a new Terminal from the given object.  */
        fun terminal(terminal: FixedItem): ContTerm = ContTerm(terminal, true)
    }
}

sealed class ContTermNone
data class Continuing(val item:FixedItem):ContTermNone()
data class Terminal(val item:FixedItem):ContTermNone()
object None:ContTermNone()

/** A mutable data structure to hold a line. */
class Line {
    var width: Float = 0f
    var maxAscent: Float = 0f
    var maxDescentAndLeading: Float = 0f
    val items: MutList<FixedItem> = mutableVec()

    fun isEmpty() = items.isEmpty()
    fun append(fi : FixedItem):Line {
        maxAscent = maxOf(maxAscent, fi.ascent)
        maxDescentAndLeading = maxOf(maxDescentAndLeading, fi.descentAndLeading)
        width += fi.xyDim.width
        items.append(fi)
        return this
    }
    fun height(): Float = maxAscent + maxDescentAndLeading
//    fun xyDim(): XyDim = XyDim.of(width, height())
    fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset {
        var x:Float = outerTopLeft.x
        val y = outerTopLeft.y
        for (item: FixedItem in items) {
            item.render(lp, XyOffset(x, y - item.ascent))
            x += item.xyDim.width
        }
        return XyOffset(x, height())
    }

    override fun toString(): String {
        return "Line(\n" +
                "               width=$width\n" +
                "           maxAscent=$maxAscent\n" +
                "maxDescentAndLeading=$maxDescentAndLeading\n" +
                "              height=${height()}\n" +
                "               items=\n" +
                "$items)\n"
    }
}

/**
 Given a maximum width, turns a list of renderables into a list of fixed-item lines.
 This allows each line to contain multiple Renderables.  They are baseline-aligned.
 If any renderables are not text, their bottom is aligned to the text baseline.
 
Start a new line.
For each renderable
  While renderable is not empty
    If our current line is empty
      add items.getSomething(blockWidth) to our current line.
    Else
      If getIfFits(blockWidth - line.widthSoFar)
        add it to our current line.
      Else
        complete our line
        Add it to finishedLines
        start a new line.
 */
fun renderablesToLines(itemsInBlock: List<Renderable>, maxWidth: Float) : ImList<Line> {
    if (maxWidth < 0) {
        throw IllegalArgumentException("maxWidth must be >= 0, not " + maxWidth)
    }
    val lines: MutList<Line> = mutableVec()
    var line = Line()

    for (item in itemsInBlock) {
        val rtor:Renderator = item.renderator()
        while (rtor.hasMore()) {
            if (line.isEmpty()) {
                val something : ContTerm = rtor.getSomething(maxWidth)
                line.append(something.item)
                if (something.foundCr) {
                    line = Line()
                }
            } else {
                val ctn:ContTermNone = rtor.getIfFits(maxWidth - line.width)

                when (ctn) {
                    is Continuing ->
                        line.append(ctn.item)
                    is Terminal -> {
                        line.append(ctn.item)
                        line = Line()
                    }
                    None -> {
                        lines.append(line)
                        line = Line()
                    }}
            }
        }
    }
    return lines.immutable()
}