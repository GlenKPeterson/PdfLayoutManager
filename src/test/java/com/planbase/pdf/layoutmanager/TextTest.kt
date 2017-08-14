package com.planbase.pdf.layoutmanager

import com.planbase.pdf.layoutmanager.Text.RowIdx
import com.planbase.pdf.layoutmanager.Text.WrappedRow
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TextTest {
    @Test fun testText() {
        val tStyle = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is a long enough line of text.")
        var ri : RowIdx = Text.tryGettingText(50f, 0, txt)
        assertFalse(ri.foundCr)
        val wrappedRow : WrappedRow = ri.row
        var idx = ri.idx
        assertEquals("This is a", wrappedRow.string)
        assertEquals(10, idx)
        assertEquals(34.903126f, wrappedRow.rowDim.width())
        assertEquals(tStyle.ascent(), wrappedRow.ascent())
        assertEquals(tStyle.descent() + tStyle.leading(), wrappedRow.descentAndLeading())
        assertEquals(tStyle.lineHeight(), wrappedRow.rowDim.height())

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("long", ri.row.string)
        assertEquals(15, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
    }

    @Test fun testTextTerminal() {
        val tStyle = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is\na long enough line of text.")
        var ri = Text.tryGettingText(50f, 0, txt)
        assertFalse(ri.foundCr)
        val wrappedRow : WrappedRow = ri.row
        var idx = ri.idx
        assertEquals("This is", wrappedRow.string)
        assertEquals(8, idx)
        assertEquals(27.084375f, wrappedRow.rowDim.width())
        assertEquals(tStyle.ascent(), wrappedRow.ascent())
        assertEquals(tStyle.descent() + tStyle.leading(), wrappedRow.descentAndLeading())
        assertEquals(tStyle.lineHeight(), wrappedRow.rowDim.height())

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("a long", ri.row.string)
        assertEquals(15, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("enough line", ri.row.string)
        assertEquals(27, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        assertFalse(ri.foundCr)

        idx = ri.idx
        assertEquals("of text.", ri.row.string)
        assertEquals(36, idx)
    }

    @Test fun testSubstrNoLeadingSpaceUntilRet() {
        var ret = Text.substrNoLeadingSpaceUntilRet("Hello", 0)
        assertEquals("Hello", ret.trimmedStr)
        assertFalse(ret.foundCr)
        assertEquals(5, ret.totalCharsConsumed)

        ret = Text.substrNoLeadingSpaceUntilRet(" Hello", 0)
        assertEquals("Hello", ret.trimmedStr)
        assertFalse(ret.foundCr)
        assertEquals(6, ret.totalCharsConsumed)

        ret = Text.substrNoLeadingSpaceUntilRet(" Hello\n", 0)
        assertEquals("Hello", ret.trimmedStr)
        assertTrue(ret.foundCr)
        assertEquals(7, ret.totalCharsConsumed)

        ret = Text.substrNoLeadingSpaceUntilRet("  Hello there\n world.", 7)
        assertEquals("there", ret.trimmedStr)
        assertTrue(ret.foundCr)
        assertEquals(7, ret.totalCharsConsumed)
    }

    @Test fun testRenderator() {
        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is a long enough line of text.")
        val rend = txt.renderator()
        assertTrue(rend.hasMore())
        val ri:ContTerm = rend.getSomething(40f)
        assertFalse(ri.foundCr)
        val row = ri.item
        assertEquals(tStyle.ascent(), row.ascent())
        assertEquals(tStyle.descent() + tStyle.leading(),
                row.descentAndLeading())
        assertEquals(tStyle.lineHeight(), row.lineHeight())
        assertEquals(tStyle.lineHeight(), row.xyDim().height())
        assertEquals(28.250002f, row.xyDim().width())

        assertTrue(rend.getIfFits(5f).isNone)

        val row3 = rend.getIfFits(20f).match({it},{it},null)
        assertNotNull(row3)
        assertEquals(14.816668f, row3.xyDim().width())
    }

    @Test fun testRenderator2() {
        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is a long enough line of text.")
        val rend = txt.renderator()
        assertTrue(rend.hasMore())
        val ri:ContTerm = rend.getSomething(40f)
        assertFalse(ri.foundCr)
        val row = ri.item
        assertEquals(tStyle.ascent(), row.ascent())
        assertEquals(tStyle.descent() + tStyle.leading(),
                     row.descentAndLeading())
        assertEquals(tStyle.lineHeight(), row.lineHeight())
        assertEquals(tStyle.lineHeight(), row.xyDim().height())
        assertEquals(28.250002f, row.xyDim().width())

        assertTrue(rend.getIfFits(5f).isNone)

        val row3 = rend.getIfFits(40f).match({it},{it},null)
        assertNotNull(row3)
        assertEquals(14.816668f, row3.xyDim().width())
        assertEquals(tStyle.lineHeight(), row3.xyDim().height())
    }

    @Test fun testCalcDimensions() {
        val tStyle = TextStyle(PDType1Font.TIMES_ITALIC, 8f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is a long enough line of text.")

        val dim: XyDim = txt.calcDimensions(40f)
        println(dim)
        assertEquals(tStyle.lineHeight() * 2, dim.height())
        assertEquals(28.250002f, dim.width())
    }

}