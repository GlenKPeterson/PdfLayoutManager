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
    private final double fontSize;

    private final double avgCharWidth;
    private final double factor;
    // Java FontMetrics says getHeight() = getAscent() + getDescent() + getLeading().
    private final double ascent;
    private final double descent;
    private final double leading;

    private TextStyle(PDType1Font f, double sz, Color tc, double leadingFactor) {
        if (f == null) { throw new IllegalArgumentException("Font must not be null"); }
        if (tc == null) { tc = Color.BLACK; }

        font = f; textColor = tc; fontSize = sz;
        // Somewhere it says that font units are 1000 times page units, but my tests with
        // PDType1Font.HELVETICA and PDType1Font.HELVETICA_BOLD from size 5-200 show that 960x is
        // pretty darn good.
        // TODO: Fix font-size for other fonts.
        factor = fontSize / 960;
        PDFontDescriptor fontDescriptor = font.getFontDescriptor();
        double rawAscent = fontDescriptor.getAscent();
        double rawDescent = fontDescriptor.getDescent();
        // Characters look best with the descent size both above and below.  Also acts as a good
        // default leading.
        ascent = rawAscent * factor;
        descent = rawDescent * -factor;
        leading = descent * leadingFactor;
        // height = ascent + descent + leading;

        double avgFontWidth = 500;
        try {
            avgFontWidth = font.getAverageFontWidth();
        } catch (Exception ioe) {
            //throw new IllegalStateException("IOException probably means an issue reading font metrics from the underlying font file used in this PDF", ioe);
            ; // just use default if there's an exception.
        }
        avgCharWidth = avgFontWidth * fontSize;
    }

    /** Creates a TextStyle with the given font, size, color, and a leadingFactor of 0.5. */
    public static TextStyle of(PDType1Font f, double sz, Color tc) {
        return new TextStyle(f, sz, tc, 0.5);
    }

    /**
     Creates a TextStyle with the given font, size, color, and leadingFactor.
     The leading factor defines the actual leading (vertical space between lines) based on the
     font descent (how far letters like g, q, j, etc. go below the baseline of letters like m).
     A leadingFactor of 1 will result of a leading equal to the descent, while a leadingFactor
     of 2 will result of a leading equal to twice the descent etc...
     */
    public static TextStyle of(PDType1Font f, double sz, Color tc, double leadingFactor) {
        return new TextStyle(f, sz, tc, leadingFactor);
    }

    /**
     Assumes ISO_8859_1 encoding
     @param text ISO_8859_1 encoded text
     @return the width of this text rendered in this font.
     */
    public double stringWidthInDocUnits(String text) {
        try {
            return font.getStringWidth(text) * factor;
        } catch (IOException ioe) {
            // logger.error("IOException probably means an issue reading font metrics from the underlying font file used in this PDF");
            // Calculate our default if there's an exception.
            return text.length() * avgCharWidth;
        }
    }

    public PDType1Font font() { return font; }
    public double fontSize() { return fontSize; }

    public Color textColor() { return textColor; }
    public TextStyle textColor(Color c) { return TextStyle.of(font, fontSize, c); }
    /**
     Average character width (for this font, or maybe guessed) as a positive number in document
     units
     */
    public double avgCharWidth() { return avgCharWidth; }
    /** Ascent as a positive number in document units */
    public double ascent() { return ascent; }
    /** Descent as a positive number in document units */
    public double descent() { return descent; }
    /** Leading as a positive number in document units */
    public double leading() { return leading; }

    public double lineHeight() { return ascent + descent + leading; }
}
