package com.planbase.pdf.layoutmanager

import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.assertEquals
import org.junit.Test
import org.organicdesign.fp.StaticImports.vec
import org.organicdesign.fp.collections.ImList

class LineKtTest {
    @Test fun testLine() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text.of(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text.of(tStyle2, "there ")
        val txt3 = Text.of(tStyle1, "world!")
        val line = Line()
        println("txt1.style().lineHeight(): " + txt1.style().lineHeight())
        line.append(txt1.renderator().getSomething(999f).item)
        assertEquals(tStyle1.lineHeight(), line.height(), 0.000002f)

        line.append(txt2.renderator().getSomething(999f).item)
        assertEquals(tStyle2.lineHeight(), line.height(), 0.000002f)

        line.append(txt3.renderator().getSomething(999f).item)
        assertEquals(tStyle2.lineHeight(), line.height(), 0.000002f)
    }

//    @Ignore

    @Test fun testRenderablesToLines() {
        val tStyle1 = TextStyle(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text.of(tStyle1, "Hello ")
        val tStyle2 = TextStyle(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text.of(tStyle2, "there ")
        val txt3 = Text.of(tStyle1, "world! This is great stuff.")

        val lines : ImList<Line> = renderablesToLines(vec(txt1, txt2, txt3), 50f)
        println(lines)
        val line1 = lines[0]
        assertEquals(tStyle2.lineHeight(), line1.height())
    }
}