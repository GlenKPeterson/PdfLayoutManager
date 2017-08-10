package com.planbase.pdf.layoutmanager

import com.planbase.pdf.layoutmanager.Text.RowIdx
import com.planbase.pdf.layoutmanager.Text.WrappedRow
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Test
import org.organicdesign.fp.oneOf.Option
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TextTest {
    @Test fun testText() {
        val tStyle = TextStyle.of(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is a long enough line of text.")
        var ri : RowIdx = Text.tryGettingText(50f, 0, txt)
        val wrappedRow : WrappedRow = ri.row()
        var idx = ri.idx()
        assertEquals("This is a", wrappedRow.string)
        assertEquals(10, idx)
        assertEquals(34.903126f, wrappedRow.rowDim.width())
        assertEquals(tStyle.ascent(), wrappedRow.ascent())
        assertEquals(tStyle.descent() + tStyle.leading(), wrappedRow.descentAndLeading())
        assertEquals(tStyle.lineHeight(), wrappedRow.rowDim.height())

        ri = Text.tryGettingText(50f, idx, txt)
        idx = ri.idx()
        assertEquals("long", ri.row().string)
        assertEquals(15, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        idx = ri.idx()
        assertEquals("enough line", ri.row().string)
        assertEquals(27, idx)

        ri = Text.tryGettingText(50f, idx, txt)
        idx = ri.idx()
        assertEquals("of text.", ri.row().string)
        assertEquals(36, idx)
    }

    @Test fun testRenderator() {
        val tStyle = TextStyle.of(PDType1Font.TIMES_ITALIC, 8f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is a long enough line of text.")
        val rend = txt.renderator()
        assertTrue(rend.hasMore())
        val row = rend.getSomething(40f)
        assertEquals(tStyle.ascent(), row.ascent())
        assertEquals(tStyle.descent() + tStyle.leading(),
                row.descentAndLeading())
        assertEquals(tStyle.lineHeight(), row.lineHeight())
        assertEquals(tStyle.lineHeight(), row.xyDim().height())
        assertEquals(28.250002f, row.xyDim().width())

        val row2 = rend.getIfFits(5f)
        assertEquals(Option.NONE, row2)

        val row3 = rend.getIfFits(20f)
        assertTrue(row3.isSome)
        assertEquals(14.816668f, row3.get().xyDim().width())
    }
}