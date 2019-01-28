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
        assertEquals(1.02421875, of(PDType1Font.HELVETICA_BOLD, 9.5, WHITE).leading(),
                     0.00000001);
        assertEquals(1.02421875, of(PDType1Font.HELVETICA, 9.5, BLACK).leading(),
                     0.00000001);
        assertEquals(0.98125, of(PDType1Font.COURIER, 12, BLACK).leading(),
                     0.00000001);
        assertEquals(0.98125, of(PDType1Font.COURIER_BOLD_OBLIQUE, 12, RED).leading(),
                     0.00000001);
        assertEquals(0.7546875, of(PDType1Font.HELVETICA, 7, BLACK).leading(),
                     0.00000001);
    }
}