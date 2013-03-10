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

/**
 A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated 
 based on how the content is rendered with regard to line-breaks and page-breaks.
 */
public class Cell {

    private final TextStyle textStyle;
    private final float width;
    private final CellStyle cellStyle;
    private final Object[] rows;
    private final int avgCharsForWidth;

    private Cell( final TextStyle ts, final float w, CellStyle cs,
                  final Object... r) {
        if (w < 0) {
            throw new IllegalArgumentException("A cell cannot have a negative width");
        }
        textStyle = ts; width = w; cellStyle = cs; rows = r;
        avgCharsForWidth = (int) ((width * 1220) / textStyle.avgCharWidth());
    }

    /**
     Creates a new cell.

     @param s the style to show any text objects in.
     @param w the width (height will be calculated based on how objects can be rendered within this
         width).
     @param cs the cell style
     @param r the text (String) and/or pictures (Jpegs as BufferedImages) to render in this cell.
     Pictures are assumed to print 300DPI with 72 document units per inch.  A null in this list
     adds a little vertical space, like a half-line between paragraphs.
     @return a cell suitable for rendering.
     */
    public static Cell valueOf(final TextStyle s, final float w, CellStyle cs, final Object... r) {
        return new Cell(s, w, cs, r);
    }

    public CellStyle cellStyle() { return cellStyle; }
    // public BorderStyle border() { return borderStyle; }
    public float width() { return width; }
    // public Color bgColor() { return bgColor; }

    private static String substrNoLeadingWhitespace(final String text, int startIdx) {
        // Drop any opening whitespace.
        while ( (startIdx < text.length()) &&
                Character.isWhitespace(text.charAt(startIdx))) {
            startIdx++;
        }
        if (startIdx > 0) {
            return text.substring(startIdx);
        }
        return text;
    }

    /**
     Shows text without any boxing or background.

     @return the final y-value
     @throws java.io.IOException if there is an error reading the font metrics from the underlying font
     file.  I think with a built-in font this is not possible, but it's in the signature of
     the PDFBox class, so I have to throw it too.

     @param x the left-most (least) x-value.
     @param origY the top-most (greatest) y-value.
     @param allPages set to true if this should be treated as a header or footer for all pages.
     @param mgr the page manager this Cell belongs to.  Probably should be set at creation
     time.
     */
    float processRows(final float x, final float origY, boolean allPages, PdfLayoutMgr mgr) {
        // Note: Always used as: y = origY - TextStyle.BREADCRUMB.height,
        if ( (rows == null) || (rows.length < 1) ) {
            return 0;
        }
        // Text is displayed based on its baseline, but this method takes a top-left corner of the
        // "cell" that contains the text.  This is the translation:

        float y = origY - cellStyle.padding().top();
        for (Object rowObj : rows) {
            if (rowObj == null) {
                y -= 4;
                continue;
            }
            if (rowObj instanceof String) {
                String row = PdfLayoutMgr.convertJavaStringToWinAnsi((String) rowObj);

                String text = substrNoLeadingWhitespace(row, 0);
                int charWidthGuess = avgCharsForWidth;

                while (text.length() > 0) {
                    int textLen = text.length();
                    // Knowing the average width of a character lets us guess and generally be near
                    // the word where the line break will occur.  Since the font reports a narrow average,
                    // (possibly due to the predominance of spaces in text) we widen it a little for a
                    // better first guess.
                    int idx = charWidthGuess;
                    if (idx > textLen) { idx = textLen; }
                    String substr = text.substring(0, idx);
                    float strWidth = textStyle.stringWidthInDocUnits(substr);

                    // If too short - find shortest string that is too long.
                    // int idx = idx;
                    // int maxTooShortIdx = -1;
                    while ( (strWidth < width) && (idx < textLen) ) {
                        // Consume any whitespace.
                        while ( (idx < textLen) &&
                                Character.isWhitespace(text.charAt(idx)) ) {
                            idx++;
                        }
                        // Find last non-whitespace character
                        while ( (idx < textLen) &&
                                !Character.isWhitespace(text.charAt(idx)) ) {
                            idx++;
                        }
                        // Test new width
                        substr = text.substring(0, idx);
                        strWidth = textStyle.stringWidthInDocUnits(substr);
                    }

                    idx--;
                    // Too long.  Find longest string that is short enough.
                    while ( (strWidth > width) && (idx > 0) ) {
                        //logger.info("strWidth: " + strWidth + " cell.width: " + cell.width + " idx: " + idx);
                        // Find previous whitespace run
                        while ( (idx > -1) && !Character.isWhitespace(text.charAt(idx)) ) {
                            idx--;
                        }
                        // Find last non-whatespace character before whitespace run.
                        while ( (idx > -1) && Character.isWhitespace(text.charAt(idx)) ) {
                            idx--;
                        }
                        if (idx < 1) {
                            break; // no spaces - have to put whole thing in cell and let it run over.
                        }
                        // Test new width
                        substr = text.substring(0, idx + 1);
                        strWidth = textStyle.stringWidthInDocUnits(substr);
                    }

                    // Here we're done whether it fits or not.
                    final float xVal = cellStyle.calcLeftX(x, (width - strWidth));

//                            CellStyle.HorizAlign.LEFT == align ? x + cellStyle.padding().left() :
//                                       CellStyle.HorizAlign.CENTER == align ? x + ((width - strWidth) / 2) :
//                                       CellStyle.HorizAlign.RIGHT == align ? x + (width - strWidth) - cellStyle.padding().right() :
//                                       x;

                    y -= textStyle.ascent();
                    if (allPages) {
                        mgr.borderStyledText(xVal, y, substr, textStyle);
                    } else {
                        PdfLayoutMgr.PageBufferAndY pby = mgr.appropriatePage(y);
                        pby.pb.drawStyledText(xVal, pby.y, substr, textStyle);
                    }
                    y -= textStyle.descent();
                    y -= textStyle.leading();

                    // Chop off section of substring that we just wrote out.
                    text = substrNoLeadingWhitespace(text, substr.length());
                }
            } else if (rowObj instanceof ScaledJpeg) {
                ScaledJpeg jpg = (ScaledJpeg) rowObj;

                final float xVal = cellStyle.calcLeftX(x, (width - jpg.width()));
//                final float xVal = HorizAlign.LEFT == align ? x + padding.left() :
//                                   HorizAlign.CENTER == align ? x + ((width - imgWidth) / 2) :
//                                   HorizAlign.RIGHT == align ? x + (width - imgWidth) - padding.right() :
//                                   x;

                // use bottom of image for page-breaking calculation.
                y -= jpg.height();
                // Calculate what page image should start on
                PdfLayoutMgr.PageBufferAndY pby = mgr.appropriatePage(y);
                // draw image based on baseline and decrement y appropriately for image.
                pby.pb.drawJpeg(xVal, pby.y, jpg, mgr);
            } else {
                throw new IllegalStateException("Found a row that wasn't a String, BufferedImage, or null - no other types allowed!");
            }
        } // end for each row

        return origY - y - cellStyle.padding().bottom(); // numLines * height;
    } // end processRows();
}
