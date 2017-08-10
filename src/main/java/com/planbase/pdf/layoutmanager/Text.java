// Copyright 2013-08-08 PlanBase Inc. & Glen Peterson
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

import org.jetbrains.annotations.NotNull;
import org.organicdesign.fp.oneOf.Option;
import org.organicdesign.fp.tuple.Tuple2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents styled text kind of like a #Text node in HTML.
 */
public class Text implements Renderable {
    private final TextStyle textStyle;
    private final String text;
    private final Map<Float,WrappedBlock> dims = new HashMap<>();
    private final CellStyle.Align align = CellStyle.DEFAULT_ALIGN;

    static class WrappedRow implements FixedItem {
        final String string;
        final XyDim rowDim;
        final TextStyle textStyle;
        WrappedRow(String s, float x, float y, TextStyle ts) {
            string = s; rowDim = XyDim.of(x, y); textStyle = ts;
        }
        public static WrappedRow of(String s, float x, TextStyle ts) {
            return new WrappedRow(s, x, ts.lineHeight(), ts);
        }

        @Override @NotNull
        public XyDim xyDim() { return rowDim; }
//        float width() { return rowDim.width(); }
//        float totalHeight() { return rowDim.height(); }

        @Override @NotNull
        public XyOffset render(@NotNull RenderTarget lp, @NotNull XyOffset outerTopLeft) {
            lp.drawStyledText(outerTopLeft.x(), outerTopLeft.y(), string, textStyle);
            return XyOffset.of(outerTopLeft.x() + rowDim.width(),
                               outerTopLeft.y() - rowDim.height() - textStyle.leading());
        }

        @Override public float ascent() { return textStyle.ascent(); }

        @Override public float descentAndLeading() {
            return textStyle.descent() + textStyle.leading();
        }

        @Override public float lineHeight() { return textStyle.lineHeight(); }
    }

    private static class WrappedBlock {
        List<WrappedRow> rows = new ArrayList<>();
        XyDim blockDim;
    }

    public static final Text DEFAULT = new Text(null, "");

    private Text(TextStyle s, String t) {
        textStyle = s; text = t;
    }

    public static Text of(TextStyle style, String text) {
        if (text == null) { text = ""; }
        if ( "".equals(text) && (style == null) ) {
            return DEFAULT;
        }
        return new Text(style, text);
    }

    public String text() { return text; };

    public TextStyle style() { return textStyle; }

    public int avgCharsForWidth(float width) {
        return (int) ((width * 1220) / textStyle.avgCharWidth());
    }

    public float maxWidth() { return textStyle.stringWidthInDocUnits(text.trim()); }

    private XyDim calcDimensionsForReal(final float maxWidth) {
        if (maxWidth < 0) {
            throw new IllegalArgumentException("Can't meaningfully wrap text with a negative width: " + maxWidth);
        }
        WrappedBlock wb = new WrappedBlock();
        float x = 0;
        float y = 0;
        float maxX = x;
        Text txt = this;
        String row = txt.text(); //PdfLayoutMgr.convertJavaStringToWinAnsi(txt.text());

        String text = substrNoLeadingWhitespace(row, 0);
        int charWidthGuess = txt.avgCharsForWidth(maxWidth);

        while (text.length() > 0) {
            int textLen = text.length();
//            System.out.println("text=[" + text + "] len=" + textLen);
            // Knowing the average width of a character lets us guess and generally be near
            // the word where the line break will occur.  Since the font reports a narrow average,
            // (possibly due to the predominance of spaces in text) we widen it a little for a
            // better first guess.
            int idx = charWidthGuess;
            if (idx > textLen) { idx = textLen; }
            String substr = text.substring(0, idx);
            float strWidth = textStyle.stringWidthInDocUnits(substr);

//            System.out.println("(strWidth=" + strWidth + " < maxWidth=" + maxWidth + ") && (idx=" + idx + " < textLen=" + textLen + ")");
            // If too short - find shortest string that is too long.
            // int idx = idx;
            // int maxTooShortIdx = -1;
            while ( (strWidth < maxWidth) && (idx < textLen) ) {
//                System.out.println("find shortest string that is too long");
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
//            System.out.println("(strWidth=" + strWidth + " > maxWidth=" + maxWidth + ") && (idx=" + idx + " > 0)");
            // Too long.  Find longest string that is short enough.
            while ( (strWidth > maxWidth) && (idx > 0) ) {
//                System.out.println("find longest string that is short enough");
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

            wb.rows.add(WrappedRow.of(substr, strWidth, textStyle));
//            System.out.println("added row");
            y -= textStyle.lineHeight();
//            System.out.println("y=" + y);

            // Chop off section of substring that we just wrote out.
            text = substrNoLeadingWhitespace(text, substr.length());
            if (strWidth > maxX) { maxX = strWidth; }
//            System.out.println("maxX=" + maxX);
        }
//        // Not sure what to do if passed "".  This used to mean to insert a blank line, but I'd
//        // really like to make that "\n" instead, but don't have the time.  *sigh*
//        if (y == 0) {
//            y -= textStyle.lineHeight();
//        }
        wb.blockDim = XyDim.of(maxX, 0 - y);
        dims.put(maxWidth, wb);
//        System.out.println("\tcalcWidth(" + maxWidth + ") on " + this.toString());
//        System.out.println("\t\ttext calcDim() blockDim=" + wb.blockDim);
        return wb.blockDim;
    }

    private WrappedBlock ensureWrappedBlock(final float maxWidth) {
        WrappedBlock wb = dims.get(maxWidth);
        if (wb == null) {
            calcDimensionsForReal(maxWidth);
            wb = dims.get(maxWidth);
        }
        return wb;
    }

    @Override public XyDim calcDimensions(final float maxWidth) {
        // I'd like to try to make calcDimensionsForReal() handle this situation before throwing an exception here.
//        if (maxWidth < 0) {
//            throw new IllegalArgumentException("maxWidth must be positive, not " + maxWidth);
//        }
        return ensureWrappedBlock(maxWidth).blockDim;
    }

    /** {@inheritDoc} */
    @Override public XyOffset render(RenderTarget lp, XyOffset outerTopLeft,
                                     XyDim outerDimensions) {

//        System.out.println("\tText.render(" + this.toString());
//        System.out.println("\t\ttext.render(outerTopLeft=" + outerTopLeft +
//                           ", outerDimensions=" + outerDimensions);

        float maxWidth = outerDimensions.width();
        WrappedBlock wb = ensureWrappedBlock(maxWidth);

        float x = outerTopLeft.x();
        float y = outerTopLeft.y();
        Padding innerPadding = align.calcPadding(outerDimensions, wb.blockDim);
//        System.out.println("\t\ttext align.calcPadding() returns: " + innerPadding);
        if (innerPadding != null) {
            x += innerPadding.left();
            //y -= innerPadding.top();
        }

        for (WrappedRow wr : wb.rows) {
            // Here we're done whether it fits or not.
            //final float xVal = x + align.leftOffset(wb.blockDim.x(), wr.rowDim.x());

            y -= textStyle.ascent();
//            if (allPages) {
//                lp.borderStyledText(x, y, wr.string, textStyle);
//            } else {

// TODO: Probably want this!
//            wr.render(lp, XyOffset.of(x, y));
            lp.drawStyledText(x, y, wr.string, textStyle);
//            }
            y -= textStyle.descent();
            y -= textStyle.leading();
        }
        return XyOffset.of(outerTopLeft.x() + wb.blockDim.width(),
                           outerTopLeft.y() - wb.blockDim.height());
    }

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

    @Override
    public String toString() {
        return "Text(\"" + ((text.length() > 25) ? text.substring(0,22) + "..."
                                                 : text) +
               "\")";
    }

    @Override
    public Renderator renderator() {
        return new TextRenderator(this);
    }

    static class RowIdx extends Tuple2<WrappedRow,Integer> {
        RowIdx(WrappedRow fi, Integer i) { super(fi, i); }
        WrappedRow row() { return _1; }
        Integer idx() { return _2; }
    }

    static RowIdx tryGettingText(float maxWidth, int startIdx, Text txt) {
        if (maxWidth < 0) {
            throw new IllegalArgumentException("Can't meaningfully wrap text with a negative width: " + maxWidth);
        }
        String row = txt.text(); //PdfLayoutMgr.convertJavaStringToWinAnsi(txt.text());
        if (row.length() <= startIdx) {
            throw new IllegalStateException("text length must be greater than startIdx");
        }

        String text = substrNoLeadingWhitespace(row, startIdx);

        int charWidthGuess = txt.avgCharsForWidth(maxWidth);

        int textLen = text.length();
//        System.out.println("text=[" + text + "] len=" + textLen);
        // Knowing the average width of a character lets us guess and generally be near
        // the word where the line break will occur.  Since the font reports a narrow average,
        // (possibly due to the predominance of spaces in text) we widen it a little for a
        // better first guess.
        int idx = charWidthGuess;
        if (idx > textLen) { idx = textLen; }
        String substr = text.substring(0, idx);
        float strWidth = txt.textStyle.stringWidthInDocUnits(substr);

//        System.out.println("(strWidth=" + strWidth + " < maxWidth=" + maxWidth + ") && (idx=" + idx + " < textLen=" + textLen + ")");
        // If too short - find shortest string that is too long.
        // int idx = idx;
        // int maxTooShortIdx = -1;
        while ( (strWidth < maxWidth) && (idx < textLen) ) {
//                System.out.println("find shortest string that is too long");
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
            strWidth = txt.textStyle.stringWidthInDocUnits(substr);
        }

        idx--;
//        System.out.println("(strWidth=" + strWidth + " > maxWidth=" + maxWidth + ") && (idx=" + idx + " > 0)");
        // Too long.  Find longest string that is short enough.
        while ( (strWidth > maxWidth) && (idx > 0) ) {
//            System.out.println("find longest string that is short enough");
//            System.out.println("strWidth: " + strWidth + " maxWidth: " + maxWidth + " idx: " + idx);
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
            strWidth = txt.textStyle.stringWidthInDocUnits(substr);
        }

        idx++;
        return new RowIdx(WrappedRow.of(substr, strWidth, txt.textStyle), idx + startIdx + 1);
    }


    class TextRenderator implements Renderator {
        private final Text txt;
        private int idx = 0;

        TextRenderator(Text t) { txt = t; }

        @Override public boolean hasMore() { return idx < txt.text.length(); }

        @Override public WrappedRow getSomething(float maxWidth) {
            if (maxWidth < 0) {
                throw new IllegalArgumentException("Illegal negative width: " + maxWidth);
            }
            RowIdx ri = tryGettingText(maxWidth, idx, txt);
            idx = ri.idx();
            return ri.row();
        }

        @Override public Option<FixedItem> getIfFits(float remainingWidth) {
            if (remainingWidth <= 0) {
                return Option.none();
            }
            RowIdx ri = tryGettingText(remainingWidth, idx, txt);
            WrappedRow row = ri.row();
            if (row.xyDim().width() <= remainingWidth) {
                idx = ri.idx();
                return Option.some(row);
            }
            return Option.none();
        }
    }

}
