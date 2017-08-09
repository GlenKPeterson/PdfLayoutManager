package com.planbase.pdf.layoutmanager

import org.organicdesign.fp.StaticImports.mutableVec
import org.organicdesign.fp.collections.ImList
import org.organicdesign.fp.collections.MutableList

/** Represents a fixed-size item  */

interface FixedItem {
    fun xyDim(): XyDim
//    fun width(): Float = width
//    fun totalHeight(): Float = heightAboveBase + depthBelowBase

    fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset
}

data class FixedItemImpl(val item: Renderable,
                     val width: Float,
                     val heightAboveBase: Float,
                     val depthBelowBase: Float) : FixedItem {
    override fun xyDim(): XyDim = XyDim.of(width, heightAboveBase + depthBelowBase)
//    fun width(): Float = width
//    fun totalHeight(): Float = heightAboveBase + depthBelowBase

    override fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset =
            item.render(lp, outerTopLeft, xyDim())
}

class Line {
    var width: Float = 0f
    val items: MutableList<FixedItem> = mutableVec()
    fun isEmpty() = items.isEmpty()
    fun append(fi : FixedItem):Line {
        width += fi.xyDim().width()
        items.append(fi)
        return this
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
    val lines: MutableList<Line> = mutableVec()
    var line: Line = Line()

    for (item in itemsInBlock) {
        val rtor:Renderator = item.renderator()
        while (rtor.hasMore()) {
            if (line.isEmpty()) {
                line.append(rtor.getSomething(maxWidth))
            } else {
                rtor.getIfFits(maxWidth - line.width)
                        .match({ line.append(it) },
                                {
                                    lines.append(line)
                                    line = Line()
                                    line
                                })
            }
        }
    }
    return lines.immutable()
}