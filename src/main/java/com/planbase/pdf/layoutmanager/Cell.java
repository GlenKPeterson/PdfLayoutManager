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
import java.util.Collection;
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

    private Cell(CellStyle cs, float w, List<Renderable> rs) {
        if (w < 0) {
            throw new IllegalArgumentException("A cell cannot have a negative width");
        }
//        for (Renderable r : rs) {
//            if (r == null) {
//                throw new IllegalArgumentException("How am I supposed to render a null?");
//            }
//        }
        cellStyle = cs; width = w; rows = rs;
    }

    /**
     Creates a new cell with the given style and width.

     @param cs the cell style
     @param width the width (height will be calculated based on how objects can be rendered within
     this width).
     @return a cell suitable for rendering.
     */
    public static Cell of(CellStyle cs, float width) { //, final Object... r) {
        return new Cell(cs, width, Collections.<Renderable>emptyList());
//                        (r == null) ? Collections.emptyList()
//                                    : Arrays.asList(r));
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float width, TextStyle ts, String s) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(Text.of(ts, s));
        return new Cell(cs, width, ls);
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float width, Text t) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(t);
        return new Cell(cs, width, ls);
    }

    public static Cell of(CellStyle cs, float width, ScaledJpeg j) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(j);
        return new Cell(cs, width, ls);
    }

    public static Cell of(CellStyle cs, float width, Renderable r) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(r);
        return new Cell(cs, width, ls);
    }

    public static Cell of(CellStyle cs, float width, List<Renderable> ls) {
        return new Cell(cs, width, ls);
    }

    // Simple case of a single styled String
    public static Cell of(CellStyle cs, float width, Cell c) {
        List<Renderable> ls = new ArrayList<Renderable>(1);
        ls.add(c);
        return new Cell(cs, width, ls);
    }

    public CellStyle cellStyle() { return cellStyle; }
    // public BorderStyle border() { return borderStyle; }
    public float width() { return width; }

    private void calcDimensionsForReal(final float maxWidth) {
        PreCalcRows pcrs = new PreCalcRows();
        XyDim blockDim = XyDim.ZERO;
        Padding padding = cellStyle.padding();
        float innerWidth = maxWidth;
        if (padding != null) {
            innerWidth -= (padding.left() + padding.right());
        }
        for (Renderable row : rows) {
            XyDim rowDim = (row == null) ? XyDim.ZERO : row.calcDimensions(innerWidth);
            blockDim = XyDim.of(Math.max(blockDim.width(), rowDim.width()),
                                blockDim.height() + rowDim.height());
//            System.out.println("\trow = " + row);
//            System.out.println("\trowDim = " + rowDim);
//            System.out.println("\tactualDim = " + actualDim);
            pcrs.rows.add(PreCalcRow.of(row, rowDim));
        }
        pcrs.blockDim = blockDim;
        preCalcRows.put(maxWidth, pcrs);
    }

    private PreCalcRows ensurePreCalcRows(final float maxWidth) {
        PreCalcRows pcr = preCalcRows.get(maxWidth);
        if (pcr == null) {
            calcDimensionsForReal(maxWidth);
            pcr = preCalcRows.get(maxWidth);
        }
        return pcr;
    }

    /** {@inheritDoc} */
    @Override public XyDim calcDimensions(final float maxWidth) {
        // I think zero or negative width cells might be OK to ignore.  I'd like to try to make
        // Text.calcDimensionsForReal() handle this situation before throwing an error here.
//        if (maxWidth < 0) {
//            throw new IllegalArgumentException("maxWidth must be positive, not " + maxWidth);
//        }
        XyDim blockDim = ensurePreCalcRows(maxWidth).blockDim;
        return ((cellStyle.padding() == null) ? blockDim : cellStyle.padding().addTo(blockDim));
//        System.out.println("Cell.calcDimensions(" + maxWidth + ") blockDim=" + blockDim +
//                           " returns " + ret);
    }

    /*
    Renders item and all child-items with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).

    {@inheritDoc}
    */
    @Override public XyOffset render(LogicalPage lp, XyOffset outerTopLeft,
                                     final XyDim outerDimensions, boolean allPages) {
//        System.out.println("Cell.render(" + this.toString());
//        new Exception().printStackTrace();

        float maxWidth = outerDimensions.width();
        PreCalcRows pcrs = ensurePreCalcRows(maxWidth);
        final Padding padding = cellStyle.padding();
        // XyDim outerDimensions = padding.addTo(pcrs.blockDim);

        // Draw background first (if necessary) so that everything else ends up on top of it.
        if (cellStyle.bgColor() != null) {
//            System.out.println("\tCell.render calling putRect...");
            lp.putRect(outerTopLeft, outerDimensions, cellStyle.bgColor());
//            System.out.println("\tCell.render back from putRect");
        }

        // Draw contents over background, but under border
        XyOffset innerTopLeft;
        final XyDim innerDimensions;
        if (padding == null) {
            innerTopLeft = outerTopLeft;
            innerDimensions = outerDimensions;
        } else {
//            System.out.println("\tCell.render outerTopLeft before padding=" + outerTopLeft);
            innerTopLeft = padding.applyTopLeft(outerTopLeft);
//            System.out.println("\tCell.render innerTopLeft after padding=" + innerTopLeft);
            innerDimensions = padding.subtractFrom(outerDimensions);
        }
        XyDim wrappedBlockDim = pcrs.blockDim;
//        System.out.println("\tCell.render cellStyle.align()=" + cellStyle.align());
//        System.out.println("\tCell.render outerDimensions=" + outerDimensions);
//        System.out.println("\tCell.render padding=" + padding);
//        System.out.println("\tCell.render innerDimensions=" + innerDimensions);
//        System.out.println("\tCell.render wrappedBlockDim=" + wrappedBlockDim);
        Padding alignPad = cellStyle.align().calcPadding(innerDimensions, wrappedBlockDim);
//        System.out.println("\tCell.render alignPad=" + alignPad);
        if (alignPad != null) {
            innerTopLeft = XyOffset.of(innerTopLeft.x() + alignPad.left(),
                                       innerTopLeft.y() - alignPad.top());
        }

        XyOffset outerLowerRight = innerTopLeft;
        for (int i = 0; i < rows.size(); i++) {
            Renderable row = rows.get(i);
            if (row == null) {
                continue;
            }
            PreCalcRow pcr = pcrs.rows.get(i);
            float rowXOffset = cellStyle.align().leftOffset(wrappedBlockDim.width(), pcr.blockDim.width());
            outerLowerRight = row.render(lp,
                                         innerTopLeft.x(innerTopLeft.x() + rowXOffset),
                                         pcr.blockDim, allPages);
            innerTopLeft = outerLowerRight.x(innerTopLeft.x());
        }

        // Draw border last to cover anything that touches it?
        BorderStyle border = cellStyle.borderStyle();
        if (border != null) {
            float origX = outerTopLeft.x();
            float origY = outerTopLeft.y();
            float rightX = outerTopLeft.x() + outerDimensions.width();
            float bottomY = outerTopLeft.y() - outerDimensions.height();
            // Like CSS it's listed Top, Right, Bottom, left
            if (border.top() != null) {
                lp.putLine(origX, origY, rightX, origY, border.top());
            }
            if (border.right() != null) {
                lp.putLine(rightX, origY, rightX, bottomY, border.right());
            }
            if (border.bottom() != null) {
                lp.putLine(origX, bottomY, rightX, bottomY, border.bottom());
            }
            if (border.left() != null) {
                lp.putLine(origX, origY, origX, bottomY, border.left());
            }
        }

        return outerLowerRight;
    }

    public static Builder builder(CellStyle cellStyle, float width) {
        return new Builder(cellStyle, width);
    }

    // Replaced with TableRow.CellBuilder.of()
//    /**
//     Be careful when adding multiple cell builders at once because the cell size is based upon
//     a pointer into the list of cell sizes.  That pointer gets incremented each time a cell is
//     added, not each time nextCellSize() is called.  Is this a bug?  Or would fixing it create
//     too many other bugs?
//     @param trb
//     @return
//     */
//    public static Builder builder(TableRowBuilder trb) {
//        Builder b = new Builder(trb.cellStyle(), trb.nextCellSize()).textStyle(trb.textStyle());
//        b.trb = trb;
//        return b;
//    }

    /**
     * A mutable Builder for somewhat less mutable cells.
     */
    public static class Builder implements CellBuilder {
        private final float width;
        private CellStyle cellStyle;
        private final List<Renderable> rows = new ArrayList<Renderable>();
        private TextStyle textStyle;

        private Builder(CellStyle cs, float w) { width = w; cellStyle = cs; }

        // Is this necessary?
//        public Builder width(float w) { width = w; return this; }

        /** {@inheritDoc} */
        @Override public Builder cellStyle(CellStyle cs) { cellStyle = cs; return this;}

        /** {@inheritDoc} */
        @Override public Builder align(CellStyle.Align align) {
            cellStyle = cellStyle.align(align); return this;
        }

        /** {@inheritDoc} */
        @Override public Builder textStyle(TextStyle x) { textStyle = x; return this; }

        /** {@inheritDoc} */
        @Override public Builder add(Renderable rs) {
            Collections.addAll(rows, rs); return this;
        }

        /** {@inheritDoc} */
        @Override public Builder addAll(Collection<? extends Renderable> js) {
            if (js != null) { rows.addAll(js); } return this;
        }

        /** {@inheritDoc} */
        @Override public Builder add(TextStyle ts, Iterable<String> ls) {
            if (ls != null) {
                for (String s : ls) {
                    rows.add(Text.of(ts, s));
                }
            }
            return this;
        }

        /** {@inheritDoc} */
        @Override public Builder addStrs(String... ss) {
            if (textStyle == null) {
                throw new IllegalStateException("Must set a default text style before adding raw strings");
            }
            for (String s : ss) {
                rows.add(Text.of(textStyle, s));
            }
            return this;
        }
//        public Builder add(Cell c) { rows.add(c); return this; }

        public Cell build() { return new Cell(cellStyle, width, rows); }

        /** {@inheritDoc} */
        @Override  public float width() { return width; }

// Replaced with TableRow.CellBuilder.buildCell()
//        public TableRowBuilder buildCell() {
//            Cell c = new Cell(cellStyle, width, rows);
//            return trb.addCell(c);
//        }

        /** {@inheritDoc} */
        @Override public String toString() {
            StringBuilder sB = new StringBuilder("Cell.Builder(").append(cellStyle).append(" width=")
                    .append(width).append(" rows=[");

            for (int i = 0; (i < rows.size()) && (i < 3); i++) {
                if (i > 0) { sB.append(" "); }
                sB.append(rows.get(i));
            }
            return sB.append("])").toString();
        }
    }

    /** {@inheritDoc} */
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
