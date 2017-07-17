package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import static com.planbase.pdf.layoutmanager.TextStyle.of;
import static java.awt.Color.*;
import static org.junit.Assert.*;

public class TextStyleTest {
    @Test public void basics() {
        // Just testing some default values before potentially merging changes that could make
        // these variable.
        assertEquals(1.0242186784744263, of(PDType1Font.HELVETICA_BOLD, 9.5f, null).leading(),
                     0.00000001);
        assertEquals(1.0242186784744263, of(PDType1Font.HELVETICA, 9.5f, null).leading(),
                     0.00000001);
        assertEquals(0.981249988079071, of(PDType1Font.COURIER, 12f, null).leading(),
                     0.00000001);
        assertEquals(0.981249988079071, of(PDType1Font.COURIER_BOLD_OBLIQUE, 12f, null).leading(),
                     0.00000001);
        assertEquals(0.754687488079071, of(PDType1Font.HELVETICA, 7f, null).leading(),
                     0.00000001);
    }
}