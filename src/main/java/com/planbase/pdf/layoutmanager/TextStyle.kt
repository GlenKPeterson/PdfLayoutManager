// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr2
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager

import org.apache.pdfbox.pdmodel.graphics.color.PDColor
import java.io.IOException

import org.apache.pdfbox.pdmodel.font.PDFont

/*
        ----    __----__
        ^     ,'  ."".  `,
        |    /   /    \   \
   Ascent   (   (      )   )
        V    \   \_  _/   /
        ____  `.__ ""  _,'
Descent/          """\ \,.
       \____          '--"
Leading ____  _  _  _  _  _  _

Line height = ascent + descent + leading.
 */
/**
 * Specifies font, font-size, color, and padding.  Immutable.
 */
data class TextStyle private constructor(val font: PDFont, val fontSize: Float,
                                         val textColor: PDColor,
                                         private val adl: AscDescLead,
                                         /**
                                         Average character width (for this font, or maybe guessed) as a positive number in document
                                         units
                                          */
                                         val avgCharWidth: Float) {
    /**
     * Creates a TextStyle with the given font, size, color, and leadingFactor.
     * The leading factor defines the actual leading (vertical space between lines) based on the
     * font descent (how far letters like g, q, j, etc. go below the baseline of letters like m).
     * A leadingFactor of 1 will result of a leading equal to the descent, while a leadingFactor
     * of 2 will result of a leading equal to twice the descent etc...
     */
    constructor(f: PDFont, sz: Float, tc: PDColor, leadingFactor: Float) :
            this(f, sz, tc , AscDescLead.fromLeadingFactor(f, sz, leadingFactor),
                 Companion.avgCharWidth(f, sz))

    /** Creates a TextStyle with the given font, size, color, and a leadingFactor of 0.5.  */
    constructor(f: PDFont, sz: Float, tc: PDColor) :
            this(f, sz, tc, AscDescLead.fromLeadingFactor(f, sz, 0.5f),
                 Companion.avgCharWidth(f, sz))

    private val factor = factorFromFontSize(fontSize)

    /**
     Assumes ISO_8859_1 encoding
     @param text ISO_8859_1 encoded text
     @return the width of this text rendered in this font.
     */
    fun stringWidthInDocUnits(text: String): Float {
        try {
            return font.getStringWidth(text) * factor
        } catch (ioe: IOException) {
            // logger.error("IOException probably means an issue reading font metrics from the underlying font file used in this PDF");
            // Calculate our default if there's an exception.
            return text.length * avgCharWidth
        }
    }

//    fun font(): PDFont = font
//
//    fun fontSize(): Float = fontSize
//
//    fun textColor(): PDColor = textColor

    fun textColor(c: PDColor): TextStyle = TextStyle(font, fontSize, c)

//    fun avgCharWidth(): Float = avgCharWidth

    /** Ascent as a positive number in document units  */
    fun ascent(): Float = adl.ascent

    /** Descent as a positive number in document units  */
    fun descent(): Float = adl.descent

    /** Leading as a positive number in document units  */
    fun leading(): Float = adl.leading

    fun lineHeight(): Float = adl.lineHeight()

    internal data class AscDescLead(val ascent: Float, val descent: Float, val leading: Float) {
        // Java FontMetrics says getHeight() = getAscent() + getDescent() + getLeading().
        fun lineHeight(): Float {
            return ascent + descent + leading
        }
        companion object {
            fun fromLeadingFactor(font: PDFont, fontSize: Float, leadingFactor: Float) : AscDescLead {
                val factor = factorFromFontSize(fontSize)
                val fontDescriptor = font.fontDescriptor
                val rawAscent = fontDescriptor.ascent
                val rawDescent = fontDescriptor.descent
                // Characters look best with the descent size both above and below.  Also acts as a good
                // default leading.
                val ascent = rawAscent * factor
                val descent = rawDescent * -factor
                val leading = descent * leadingFactor
                return AscDescLead(ascent, descent, leading)
            }
        }
    }

    companion object {

        // Somewhere it says that font units are 1000 times page units, but my tests with
        // PDType1Font.HELVETICA and PDType1Font.HELVETICA_BOLD from size 5-200 show that 960x is
        // pretty darn good.
        // TODO: Fix font-size for other fonts.
        fun factorFromFontSize(fontSize: Float) : Float = fontSize / 960f

        fun avgCharWidth(f : PDFont, sz:Float) : Float {
            var avgFontWidth = 500f
            try {
                avgFontWidth = f.averageFontWidth
            } catch (ioe: Exception) {
                //throw new IllegalStateException("IOException probably means an issue reading font
                // metrics from the underlying font file used in this PDF", ioe);
                // just use default if there's an exception.
            }

            return avgFontWidth * sz
        }
    }
}
