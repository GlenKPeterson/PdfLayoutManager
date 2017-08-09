package com.planbase.pdf.layoutmanager

import com.planbase.pdf.layoutmanager.Text.RowIdx
import com.planbase.pdf.layoutmanager.Text.WrappedRow
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Test
import kotlin.test.assertEquals

class TextTest {
    @Test fun testText() {
        val tStyle = TextStyle.of(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt = Text.of(tStyle, "This is a long enough line of text.")
        var ri : RowIdx = Text.tryGettingText(50f, 0, txt)
        var wrappedRow : WrappedRow = ri.row()
        var idx = ri.idx()
        assertEquals("This is a", wrappedRow.string)
        assertEquals(10, idx)
        assertEquals(34.903126f, wrappedRow.rowDim.width())
        assertEquals(tStyle.ascent(), wrappedRow.ascent())
        assertEquals(tStyle.descent(), wrappedRow.descent())
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
}