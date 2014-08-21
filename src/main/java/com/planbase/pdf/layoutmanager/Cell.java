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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 A styled table cell or layout block with a pre-set horizontal width.  Vertical height is calculated 
 based on how the content is rendered with regard to line-breaks and page-breaks.
 */
public class Cell implements Renderable {

    // These are limits of the cell, not the contents.
    private final CellStyle cellStyle;
    private final float width;

    // A list of the contents.  It's pretty limiting to have one item per row.
    private final List<Renderable> rows;

    private final Map<Float,PreCalcRows> preCalcRows = new HashMap<Float,PreCalcRows>(0);

    private static class PreCalcRow {
        Renderable row;
        XyDim blockDim;
        public static PreCalcRow of(Renderable r, XyDim d) {
            PreCalcRow pcr = new PreCalcRow(); pcr.row = r; pcr.blockDim = d; return pcr;
        }
    }

    private static class PreCalcRows {
        List<PreCalcRow> rows = new ArrayList<PreCalcRow>(1);
        XyDim blockDim;
    }

    private Cell(CellStyle cs, float w, List<Renderable> r) {
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
        return new Cell(cs, w, Collections.<Renderable>emptyList());
//                        (r == null) ? Collections.emptyList()
//                                    : Arrays.asList(r));
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float w, TextStyle ts, String s) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(Text.of(ts, s));
        return new Cell(cs, w, ls);
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float w, Text t) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(t);
        return new Cell(cs, w, ls);
    }

    public static Cell of(CellStyle cs, float w, ScaledJpeg j) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(j);
        return new Cell(cs, w, ls);
    }

    public static Cell of(CellStyle cs, float w, Renderable r) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(r);
        return new Cell(cs, w, ls);
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float w, Cell c) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(c);
        return new Cell(cs, w, ls);
    }

    public CellStyle cellStyle() { return cellStyle; }
    // public BorderStyle border() { return borderStyle; }
    public float width() { return width; }
    // public Color bgColor() { return bgColor; }

//    /**
//     Shows text without any boxing or background.
//
//     @return the final y-value
//     @throws java.io.IOException if there is an error reading the font metrics from the underlying font
//     file.  I think with a built-in font this is not possible, but it's in the signature of
//     the PDFBox class, so I have to throw it too.
//
//     @param x the left-most (least) x-value.
//     @param origY the top-most (greatest) y-value.
//     @param allPages set to true if this should be treated as a header or footer for all pages.
//     @param mgr the page manager this Cell belongs to.  Probably should be set at creation
//     time.
//     */
//    float processRows(final float x, final float origY, boolean allPages, PdfLayoutMgr mgr) {
//        // Note: Always used as: y = origY - TextStyle.BREADCRUMB.height,
//        if ( (rows == null) || (rows.size() < 1) ) {
//            return 0;
//        }
//        // Text is displayed based on its baseline, but this method takes a top-left corner of the
//        // "cell" that contains the text.  This is the translation:
//
//        float y = origY - cellStyle.padding().top();
//        for (Renderable renderable : rows) {
//            if (renderable == null) {
//                y -= 4;
//                continue;
//            }
//
//            XyPair p = renderable.render(XyPair.of(x, y), allPages, mgr, width);
//            y = p.y();
//        } // end for each row
//
//        return origY - y - cellStyle.padding().bottom(); // numLines * height;
//    } // end processRows();

    private XyDim calcDimensionsForReal(final float maxWidth) {
        PreCalcRows pcrs = new PreCalcRows();
        XyDim actualDim = XyDim.ORIGIN;
        Padding padding = cellStyle.padding();
        float innerWidth = maxWidth;
        if (padding != null) {
            innerWidth -= (padding.left() + padding.right());
            actualDim = padding.topLeftPadDim();
        }
        for (Renderable row : rows) {
            XyDim rowDim = row.calcDimensions(innerWidth);
            actualDim = actualDim.plus(rowDim);
//            System.out.println("\trow = " + row);
//            System.out.println("\trowDim = " + rowDim);
//            System.out.println("\tactualDim = " + actualDim);
            pcrs.rows.add(PreCalcRow.of(row, rowDim));
        }
        if (padding != null) {
            actualDim = actualDim.plus(padding.botRightPadDim());
        }
        pcrs.blockDim = actualDim;
        preCalcRows.put(maxWidth, pcrs);
        return actualDim;
    }

    private PreCalcRows ensurePreCalcRows(final float maxWidth) {
        PreCalcRows pcr = preCalcRows.get(maxWidth);
        if (pcr == null) {
            calcDimensionsForReal(maxWidth);
            pcr = preCalcRows.get(maxWidth);
        }
        return pcr;
    }

    public XyDim calcDimensions(final float maxWidth) {
        return ensurePreCalcRows(maxWidth).blockDim;
    }

    /*
    Renders item and all child-items with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).
    */
    public XyOffset render(PdfLayoutMgr mgr, XyOffset outerTopLeft, XyDim outerDimensions,
                           boolean allPages) {
        System.out.println("Cell.render(" + this.toString());


        float maxWidth = outerDimensions.x();
        PreCalcRows pcrs = ensurePreCalcRows(maxWidth);

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (cellStyle.bgColor() != null) {
            mgr.putRect(outerTopLeft, outerDimensions, cellStyle.bgColor());
        }

        // Draw contents over background, but under border
        XyOffset innerTopLeft = outerTopLeft;
        XyDim innerDimensions = outerDimensions;
        Padding padding = cellStyle.padding();
        if (padding != null) {
            innerTopLeft = XyOffset.of((outerTopLeft.x() + padding.left()),
                                       (outerTopLeft.y() - padding.top()));
            innerDimensions = XyDim.of(
                    (outerDimensions.x() - padding.left() - padding.right()),
                    (outerDimensions.y() - padding.top() - padding.bottom()));
        }
        XyDim wrappedBlockDim = pcrs.blockDim;
        Padding alignPad = cellStyle.align().calcPadding(innerDimensions, wrappedBlockDim);
        if (alignPad != null) {
            innerTopLeft = XyOffset.of(innerTopLeft.x() + alignPad.left(),
                                       innerTopLeft.y() - alignPad.top());
        }

        XyOffset outerLowerRight = innerTopLeft;
        for (int i = 0; i < rows.size(); i++) {
            Renderable row = rows.get(i);
            PreCalcRow pcr = pcrs.rows.get(i);
            outerLowerRight = row.render(mgr, innerTopLeft, pcr.blockDim, allPages);
            innerTopLeft = outerLowerRight.x(innerTopLeft.x());
        }

        // Draw border last to cover anything that touches it?
        BorderStyle border = cellStyle.borderStyle();
        if (border != null) {
            float origX = outerTopLeft.x();
            float origY = outerTopLeft.y();
            float rightX = outerTopLeft.x() + outerDimensions.x();
            float bottomY = outerTopLeft.y() - outerDimensions.y();
            // Like CSS it's listed Top, Right, Bottom, left
            if (border.top() != null) {
                mgr.putLine(origX, origY, rightX, origY, border.top());
            }
            if (border.right() != null) {
                mgr.putLine(rightX, origY, rightX, bottomY, border.right());
            }
            if (border.bottom() != null) {
                mgr.putLine(origX, bottomY, rightX, bottomY, border.bottom());
            }
            if (border.left() != null) {
                mgr.putLine(origX, origY, origX, bottomY, border.left());
            }
        }

        return outerLowerRight;
    }

    public static Builder builder(CellStyle cellStyle, float width) {
        return new Builder(cellStyle, width);
    }

    public static class Builder {
        private final float width; // Both require this.
        private final CellStyle cellStyle; // Both require this.
        private final List<Renderable> rows = new ArrayList<Renderable>();
        private TextStyle textStyle;

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
        public Builder textStyle(TextStyle x) { textStyle = x; return this; }

        public Builder add(Cell c) { rows.add(c); return this; }

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

    @Override public String toString() {
        StringBuilder sB = new StringBuilder("Cell(").append(cellStyle).append(" width=")
                .append(width).append(" rows=[");

        for (int i = 0; (i < rows.size()) && (i < 3); i++) {
            if (i > 0) { sB.append(" "); }
            sB.append(rows.get(i));
        }
        return sB.append("])").toString();
    }
}
