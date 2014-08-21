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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Text implements Renderable {
    private final TextStyle textStyle;
    private final String text;
    private final Map<Float,WrappedBlock> dims = new HashMap<Float,WrappedBlock>();
    private final CellStyle.Align align = CellStyle.DEFAULT_ALIGN;

    private static class WrappedRow {
        String string;
        XyDim rowDim;
        public static WrappedRow of(String s, float x, float y) {
            WrappedRow wr = new WrappedRow();
            wr.string = s;
            wr.rowDim = XyDim.of(x, y);
            return wr;
        }
    }

    private static class WrappedBlock {
        List<WrappedRow> rows = new ArrayList<WrappedRow>();
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
        WrappedBlock wb = new WrappedBlock();
        float x = 0;
        float y = 0;
        float maxX = x;
        Text txt = this;
        String row = PdfLayoutMgr.convertJavaStringToWinAnsi(txt.text());

        String text = substrNoLeadingWhitespace(row, 0);
        int charWidthGuess = txt.avgCharsForWidth(maxWidth);

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
            while ( (strWidth < maxWidth) && (idx < textLen) ) {
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
            while ( (strWidth > maxWidth) && (idx > 0) ) {
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

            wb.rows.add(WrappedRow.of(substr, strWidth, textStyle.lineHeight()));
            y -= textStyle.lineHeight();

            // Chop off section of substring that we just wrote out.
            text = substrNoLeadingWhitespace(text, substr.length());
            if (strWidth > maxX) { maxX = strWidth; }
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

    public XyDim calcDimensions(final float maxWidth) {
        return ensureWrappedBlock(maxWidth).blockDim;
    }

    public XyOffset render(PdfLayoutMgr mgr, XyOffset outerTopLeft, XyDim outerDimensions,
                           boolean allPages) {

//        System.out.println("\tText.render(" + this.toString());
//        System.out.println("\t\ttext.render(outerTopLeft=" + outerTopLeft +
//                           ", outerDimensions=" + outerDimensions);

        float maxWidth = outerDimensions.x();
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
            if (allPages) {
                mgr.borderStyledText(x, y, wr.string, textStyle);
            } else {
                PdfLayoutMgr.PageBufferAndY pby = mgr.appropriatePage(y);
                pby.pb.drawStyledText(x, pby.y, wr.string, textStyle);
            }
            y -= textStyle.descent();
            y -= textStyle.leading();
        }
        return XyOffset.of(outerTopLeft.x() + wb.blockDim.x(),
                           outerTopLeft.y() - wb.blockDim.y());
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
}
