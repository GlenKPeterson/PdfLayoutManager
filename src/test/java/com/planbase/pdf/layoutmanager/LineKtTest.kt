package com.planbase.pdf.layoutmanager

import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Assert.*
import org.junit.Test
import org.organicdesign.fp.StaticImports
import org.organicdesign.fp.StaticImports.vec
import org.organicdesign.fp.collections.ImList

class LineKtTest {
    @Test fun testRenderablesToLines() {
        val tStyle1 = TextStyle.of(PDType1Font.HELVETICA, 9f, Utils.CMYK_BLACK)
        val txt1 = Text.of(tStyle1, "Hello ")
        val tStyle2 = TextStyle.of(PDType1Font.HELVETICA_BOLD, 13f, Utils.CMYK_BLACK)
        val txt2 = Text.of(tStyle2, "there ")
        val txt3 = Text.of(tStyle1, "world! This is great stuff.")

        val lines : ImList<Line> = renderablesToLines(vec(txt1, txt2, txt3), 50f)
        val line1 = lines[0]
        // TODO: I think this should pass, but just want to try stuff for now...
//        assertEquals(tStyle2.lineHeight(), line1.height())
    }
}