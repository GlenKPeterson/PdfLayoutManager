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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated 
 based on how the content is rendered with regard to line-breaks and page-breaks.
 */
public class Cell {

    private static final String INVALID_ROW_TYPE_STR = "Found a row that wasn't a String, BufferedImage, or null - no other types allowed!";

    // These are limits of the cell, not the contents.
    private final CellStyle cellStyle;
    private final float width;

    // A list of the contents.  It's pretty limiting to have one item per row.
    private final List<?> rows;

    private Cell(CellStyle cs, float w, List<?> r) {
        if (w < 0) {
            throw new IllegalArgumentException("A cell cannot have a negative width");
        }
        cellStyle = cs; width = w; rows = r;
    }

    /**
     Creates a new cell.

     @param w the width (height will be calculated based on how objects can be rendered within this
         width).
     @param cs the cell style
     @return a cell suitable for rendering.
     */

//    @param s the style to show any text objects in.
    //      @param r the text (String) and/or pictures (Jpegs as BufferedImages) to render in this cell.
//         Pictures are assumed to print 300DPI with 72 document units per inch.  A null in this list
//         adds a little vertical space, like a half-line between paragraphs.

    public static Cell of(CellStyle cs, float w) { //, final Object... r) {
        return new Cell(cs, w, Collections.emptyList());
//                        (r == null) ? Collections.emptyList()
//                                    : Arrays.asList(r));
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float w, TextStyle ts, String s) {
        List<Text> ls = new ArrayList<Text>();
        ls.add(Text.of(ts, s));
        return new Cell(cs, w, ls);
    }

    public static Cell of(CellStyle cs, float w, ScaledJpeg j) {
        List<ScaledJpeg> ls = new ArrayList<ScaledJpeg>();
        ls.add(j);
        return new Cell(cs, w, ls);
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
        if ( (rows == null) || (rows.size() < 1) ) {
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
            if (rowObj instanceof Text) {
                Text txt = (Text) rowObj;
                TextStyle textStyle = txt.style();
                String row = PdfLayoutMgr.convertJavaStringToWinAnsi(txt.text());

                String text = substrNoLeadingWhitespace(row, 0);
                int charWidthGuess = txt.avgCharsForWidth(width);

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
                throw new IllegalStateException(INVALID_ROW_TYPE_STR);
            }
        } // end for each row

        return origY - y - cellStyle.padding().bottom(); // numLines * height;
    } // end processRows();

    public static Builder builder(CellStyle cellStyle, float width) {
        return new Builder(cellStyle, width);
    }

    public static class Builder {
        private final float width; // Both require this.
        private final CellStyle cellStyle; // Both require this.
        private final List<Object> rows = new ArrayList<Object>();

        private Builder(CellStyle cs, float w) { width = w; cellStyle = cs; }

        public Builder add(Text t) { rows.add(t); return this; }
        public Builder addAll(TextStyle ts, List<String> ls) {
            if (ls != null) {
                for (String s : ls) {
                    rows.add(Text.of(ts, s));
                }
            }
            return this;
        }

        public Builder add(ScaledJpeg j) { rows.add(j); return this; }
        public Builder addAll(List<ScaledJpeg> js) {
            if (js != null) { rows.addAll(js); }
            return this;
        }

        public Cell build() { return new Cell(cellStyle, width, rows); }
    }

    /*
    These are limits of the cell, not the contents.

    float width is a limit of the cell, not of the contents.
    CellStyle cellStyle is the over-all style of the cell, inherited by all contents for which
    it is relevant.

    public static interface CellContents {
        // This is just some junk to indicate that this method will handle anything of this type.
        // Don't go implementing your own stuff and passing it to this method.

    }

    public static class CellText implements CellContents {
        private final float width; // Both require this.
        private final CellStyle cellStyle; // Both require this.
        private final TextStyle textStyle; // Required for Strings.  Unnecessary for Images.
        private final int avgCharsForWidth; // Required for Strings.  Unnecessary for Images.

        private CellText(final TextStyle ts, final float w, CellStyle cs) {
            if (w < 0) {
                throw new IllegalArgumentException("A cell cannot have a negative width");
            }
            textStyle = ts; width = w; cellStyle = cs;
            avgCharsForWidth = (int) ((width * 1220) / textStyle.avgCharWidth());
        }

        public float width() { return width; }
        public CellStyle cellStyle() { return cellStyle; }
    }

    public static class CellImage implements CellContents {
        private final float width; // Both require this.
        private final CellStyle cellStyle; // Both require this.

        private CellImage(final float w, CellStyle cs) {
            if (w < 0) {
                throw new IllegalArgumentException("A cell cannot have a negative width");
            }
            width = w; cellStyle = cs;
        }
        public float width() { return width; }
        public CellStyle cellStyle() { return cellStyle; }
    }
     */
}
