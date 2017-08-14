package com.planbase.pdf.layoutmanager

import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.junit.Test

import org.junit.Assert.*

class TextStyleTest {
    @Test fun basics() {
        // Just testing some default values before potentially merging changes that could make
        // these variable.
        assertEquals(1.0242186784744263, TextStyle(PDType1Font.HELVETICA_BOLD, 9.5f, Utils.CMYK_BLACK).leading().toDouble(),
                     0.00000001)
        assertEquals(1.0242186784744263, TextStyle(PDType1Font.HELVETICA, 9.5f, Utils.CMYK_BLACK).leading().toDouble(),
                     0.00000001)
        assertEquals(0.981249988079071, TextStyle(PDType1Font.COURIER, 12f, Utils.CMYK_BLACK).leading().toDouble(),
                     0.00000001)
        assertEquals(0.981249988079071, TextStyle(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, Utils.CMYK_BLACK).leading().toDouble(),
                     0.00000001)
        assertEquals(0.754687488079071, TextStyle(PDType1Font.HELVETICA, 7f, Utils.CMYK_BLACK).leading().toDouble(),
                     0.00000001)
    }
}