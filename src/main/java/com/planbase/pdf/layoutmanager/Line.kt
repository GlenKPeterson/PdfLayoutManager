package com.planbase.pdf.layoutmanager

import org.organicdesign.fp.StaticImports.mutableVec
import org.organicdesign.fp.StaticImports.vec
import org.organicdesign.fp.collections.ImList
import org.organicdesign.fp.collections.MutableList
import org.organicdesign.fp.oneOf.OneOf2
import org.organicdesign.fp.oneOf.OneOf2OrNone
import org.organicdesign.fp.type.RuntimeTypes

/** Represents a fixed-size item  */

interface FixedItem {
    fun xyDim(): XyDim
//    fun width(): Float = width
//    fun totalHeight(): Float = heightAboveBase + depthBelowBase

    fun ascent(): Float
    fun descentAndLeading(): Float
    fun lineHeight(): Float

    fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset
}

data class FixedItemImpl(val item: Renderable,
                         val width: Float,
                         val ascent: Float,
                         val descentAndLeading: Float,
                         val lineHeight:Float) : FixedItem {
    override fun ascent(): Float = ascent
    override fun descentAndLeading(): Float = descentAndLeading
    override fun lineHeight(): Float = lineHeight
    override fun xyDim(): XyDim = XyDim.of(width, lineHeight)
//    fun width(): Float = width
//    fun totalHeight(): Float = heightAboveBase + depthBelowBase

    override fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset =
            item.render(lp, outerTopLeft.y(outerTopLeft.y() + ascent), xyDim())
}

/**
Represents a continuing or terminal FixedItem where Continuing means there could be more on this
line (no hard line break) and Terminal means a hard-coded line-break was encountered.
 */
// TODO: This might be better as a data class than a oneOf
class ContTerm<T> private constructor(c: T?, t: T?, n:Int)
    : OneOf2<T,T>(CONT_TERM_TYPES, c, t, n) {

    @Suppress("UNCHECKED_CAST")
    fun getEither():T = super.item as T

//    /** Returns true if this is Continuing.  */
//    val isContinuing: Boolean
//        get() = sel == 1
//    /** Returns true if this is Terminal.  */
//    val isTerminal: Boolean
//        get() = sel == 2
//
//    /** Returns the good value if this is a Good, or throws an exception if this is a Bad.  */
//    fun continuing(): T = match({ c -> c },
//            { super.throw2(it) })
//
//    /** Returns the bad value if this is a Bad, or throws an exception if this is a Good.  */
//    fun terminal(): T = match({ super.throw1(it) },
//            { t -> t })

    /** Represents the presence of a Continuing line.  */
    private class Continuing

    /** Represents the presence of a Terminal line.  */
    private class Terminal

    companion object {
        private val CONT_TERM_TYPES = RuntimeTypes.registerClasses(vec(Continuing::class.java,
                Terminal::class.java))

        /** Construct a new Continuing from the given object.  */
        fun <T> continuing(continuing: T?): ContTerm<T> = ContTerm(continuing, null, 1)

        /** Construct a new Terminal from the given object.  */
        fun <T> terminal(terminal: T?): ContTerm<T> = ContTerm(null, terminal, 2)
    }
}

// TODO: This might be better as a data class than a oneOf
class ContTermNone private constructor(c: FixedItem?, t: FixedItem?, n:Int)
    : OneOf2OrNone<FixedItem,FixedItem>(c, t, n) {

    override fun typeName(selIdx: Int): String = NAMES[selIdx - 1]

    /** Returns true if this is Continuing.  */
    val isContinuing: Boolean
        get() = sel == 1
    /** Returns true if this is Terminal.  */
    val isTerminal: Boolean
        get() = sel == 2

    val isNone: Boolean
        get() = sel == 3

    companion object {
        private val NAMES = arrayOf("Continuing", "Terminal", "None")

        /** Construct a new Continuing from the given object.  */
        fun continuing(continuing: FixedItem): ContTermNone = ContTermNone(continuing, null, 1)

        /** Construct a new Terminal from the given object.  */
        fun terminal(terminal: FixedItem): ContTermNone = ContTermNone(null, terminal, 2)

        /** Construct a new None from the given object.  */
        fun none(): ContTermNone = ContTermNone(null, null, 3)
    }
}

class Line {
    var width: Float = 0f
    var maxAscent: Float = 0f
    var maxDescentAndLeading: Float = 0f
    val items: MutableList<FixedItem> = mutableVec()

    fun isEmpty() = items.isEmpty()
    fun append(fi : FixedItem):Line {
        maxAscent = maxOf(maxAscent, fi.ascent())
        maxDescentAndLeading = maxOf(maxDescentAndLeading, fi.descentAndLeading())
        width += fi.xyDim().width()
        items.append(fi)
        return this
    }
    fun height(): Float = maxAscent + maxDescentAndLeading
//    fun xyDim(): XyDim = XyDim.of(width, height())
    fun render(lp:RenderTarget, outerTopLeft:XyOffset):XyOffset {
        var x:Float = outerTopLeft.x()
        val y = outerTopLeft.y()
        for (item:FixedItem in items) {
            item.render(lp, XyOffset.of(x, y - item.ascent()))
            x += item.xyDim().width()
        }
        return XyOffset.of(x, height())
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
    val lines: MutableList<Line> = mutableVec()
    var line: Line = Line()

    for (item in itemsInBlock) {
        val rtor:Renderator = item.renderator()
        while (rtor.hasMore()) {
            if (line.isEmpty()) {
                rtor.getSomething(maxWidth)
                        .match( { c -> line.append(c) },
                                { t -> line.append(t)
                                    line = Line()
                                    line
                                })
            } else {
                rtor.getIfFits(maxWidth - line.width)
                        .match({ c -> line.append(c) },
                                { t -> line.append(t)
                                    line = Line()
                                    line
                                },
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