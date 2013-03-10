// Copyright 2013-03-03 PlanBase Inc. & Glen Peterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.planbase.pdf.layoutmanager;

import java.awt.Color;
import java.io.IOException;

import org.apache.pdfbox.pdmodel.font.PDFontDescriptor;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
Specifies font, font-size, color, and padding.  Immutable.
 */
public class TextStyle {

    private final PDType1Font font;
    private final Color textColor;
    private final float fontSize;

    private final float avgCharWidth;
    private final float factor;
    // Java FontMetrics says getHeight() = getAscent() + getDescent() + getLeading().
    private final float ascent;
    private final float descent;
    private final float leading;

    private TextStyle(PDType1Font f, float sz, Color tc) {
        if (f == null) { throw new IllegalArgumentException("Font must not be null"); }
        if (tc == null) { tc = Color.BLACK; }

        font = f; textColor = tc; fontSize = sz;
        // Somewhere it says that font units are 1000 times page units, but my tests with
        // PDType1Font.HELVETICA and PDType1Font.HELVETICA_BOLD from size 5-200 show that 960x is
        // pretty darn good.
        // TODO: Fix font-size for other fonts.
        factor = fontSize / 960f;
        PDFontDescriptor fontDescriptor = font.getFontDescriptor();
        float rawAscent = fontDescriptor.getAscent();
        float rawDescent = fontDescriptor.getDescent();
        // Characters look best with the descent size both above and below.  Also acts as a good
        // default leading.
        ascent = rawAscent * factor;
        descent = rawDescent * -factor;
        leading = descent / 2;
        // height = ascent + descent + leading;

        float avgFontWidth = 500;
        try {
            avgFontWidth = font.getAverageFontWidth();
        } catch (IOException ioe) {
            //throw new IllegalStateException("IOException probably means an issue reading font metrics from the underlying font file used in this PDF", ioe);
            ; // just use default if there's an exception.
        }
        avgCharWidth = avgFontWidth * fontSize;
    }

    public static TextStyle valueOf(PDType1Font f, float sz, Color tc) {
        return new TextStyle(f, sz, tc);
    }

    /**
     Assumes ISO_8859_1 encoding
     @param text ISO_8859_1 encoded text
     @return the width of this text rendered in this font.
     */
    public float stringWidthInDocUnits(String text) {
        try {
            return font.getStringWidth(text) * factor;
        } catch (IOException ioe) {
            // logger.error("IOException probably means an issue reading font metrics from the underlying font file used in this PDF");
            // Calculate our default if there's an exception.
            return text.length() * avgCharWidth;
        }
    }

    public PDType1Font font() { return font; }
    public float fontSize() { return fontSize; }

    public Color textColor() { return textColor; }
    /**
     Average character width (for this font, or maybe guessed) as a positive number in document
     units
     */
    public float avgCharWidth() { return avgCharWidth; }
    /** Ascent as a positive number in document units */
    public float ascent() { return ascent; }
    /** Descent as a positive number in document units */
    public float descent() { return descent; }
    /** Leading as a positive number in document units */
    public float leading() { return leading; }
}
