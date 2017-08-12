// Copyright 2014-08-18 PlanBase Inc. & Glen Peterson
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
import java.util.List;

/**
 Unsynchronized mutable class which is not thread-safe.  The internal tracking of cells and widths
 allows you to make a cell builder for a cell at a given column, add cells in subsequent columns,
 then complete (buildCell()) the cell and have it find its proper (now previous) column.
 */
public class TableRowBuilder {
    private final TablePart tablePart;
    private TextStyle textStyle;
    private CellStyle cellStyle;
    private final List<Cell> cells;
    private float minRowHeight = 0;
    private int nextCellIdx = 0;

//    private TableRow(TablePart tp, float[] a, Cell[] b, CellStyle c, TextStyle d) {
//        tablePart = tp; cellWidths = a; cells = b; cellStyle = c; textStyle = d;
//    }

    private TableRowBuilder(TablePart tp) {
        tablePart = tp;
        cells = new ArrayList<Cell>(tp.cellWidths().size());
        textStyle = tp.textStyle();
        cellStyle = tp.cellStyle();
        minRowHeight = tp.minRowHeight();
    }

    public float nextCellSize() {
        if (tablePart.numCellWidths() <= nextCellIdx) {
            throw new IllegalStateException("Tried to add more cells than you set sizes for");
        }
        return tablePart.cellWidths().get(nextCellIdx).floatValue();
    }

    public static TableRowBuilder of(TablePart tp) { return new TableRowBuilder(tp); }

    public TextStyle textStyle() { return textStyle; }
    public TableRowBuilder textStyle(TextStyle x) { textStyle = x; return this; }

    public CellStyle cellStyle() { return cellStyle; }

    public TableRowBuilder addCells(Cell... cs) {
        Collections.addAll(cells, cs);
        nextCellIdx += cs.length;
        return this;
    }

    public int nextCellIdx() { return nextCellIdx; }

    public TableRowBuilder addTextCells(String... ss) {
        if (textStyle == null) {
            throw new IllegalStateException("Tried to add a text cell without setting a default text style");
        }
        for (String s : ss) {
            addCellAt(Cell.of(cellStyle, nextCellSize(), new Text(textStyle, s)), nextCellIdx);
            nextCellIdx++;
        }
        return this;
    }

    // TODO: This should be add Renderable Cells.
    public TableRowBuilder addJpegCells(ScaledJpeg... js) {
        for (ScaledJpeg j : js) {
            addCellAt(Cell.of(cellStyle, nextCellSize(), j), nextCellIdx);
            nextCellIdx++;
        }
        return this;
    }

// Because cells are renderable, this would accept one which could result in duplicate cells
// when Cell.buildCell() creates a cell and passes it in here.
//    public TableRowBuilder addCell(CellStyle.Align align, Renderable... things) {
//            cells.add(Cell.builder(this).add(things).build());
//        return this;
//    }

    public TableRowBuilder addCell(Cell c) {
        cells.add(c);
        nextCellIdx++;
        return this;
    }

    public TableRowBuilder addCellAt(Cell c, int idx) {
        // Ensure capacity in the list.
        while (cells.size() < (idx + 1)) {
            cells.add(null);
        }
        if (cells.get(idx) != null) {
            // System.out.println("Existing cell was: " + cells.get(idx) + "\n Added cell was: " + c);
            throw new IllegalStateException("Tried to add a cell built from a table row back to the row after adding a free cell in its spot.");
        }
        cells.set(idx, c);
        return this;
    }

    public TableRowBuilder minRowHeight(float f) { minRowHeight = f; return this; }

    public RowCellBuilder cellBuilder() {
        RowCellBuilder cb = new RowCellBuilder(this);
        nextCellIdx++;
        return cb;
    }

    public TablePart buildRow() {
        // Do we want to fill out the row with blank cells?
        if (cells.contains(null)) {
            throw new IllegalStateException("Cannot build row when some TableRowCellBuilders have been created but the cells not built and added back to the row.");
        }
        return tablePart.addRow(this);
    }

    public XyDim calcDimensions() {
        XyDim maxDim = XyDim.ZERO;
        // Similar to PdfLayoutMgr.putRow().  Should be combined?
        for (Cell cell : cells) {
            XyDim wh = cell.calcDimensions(cell.width());
            maxDim = XyDim.of(wh.width() + maxDim.width(),
                              Math.max(maxDim.height(), wh.height()));
        }
        return maxDim;
    }

    public XyOffset render(RenderTarget lp, XyOffset outerTopLeft) {
        XyDim maxDim = XyDim.ZERO.height(minRowHeight);
        for (Cell cell : cells) {
            XyDim wh = cell.calcDimensions(cell.width());
            maxDim = XyDim.of(maxDim.width() + cell.width(),
                              Math.max(maxDim.height(), wh.height()));
        }
        float maxHeight = maxDim.height();

        float x = outerTopLeft.x();
        for (Cell cell : cells) {
//            System.out.println("\t\tAbout to render cell: " + cell);
            // TODO: Cache the duplicate cell.calcDimensions call!!!
            cell.render(lp, XyOffset.of(x, outerTopLeft.y()),
                        XyDim.of(cell.width(), maxHeight));
            x += cell.width();
        }
        return XyOffset.of(x, outerTopLeft.y() - maxHeight);
    }

    @Override
    public String toString() {
        return new StringBuilder("TableRowBuilder(").append(tablePart).append(" ")
                .append(System.identityHashCode(this)).append(")").toString();

    }

    public class RowCellBuilder implements CellBuilder {

        private final TableRowBuilder tableRowBuilder;
        private final float width; // Both require this.
        private CellStyle cellStyle; // Both require this.
        private final List<Renderable> rows = new ArrayList<Renderable>();
        private TextStyle textStyle;
        private final int colIdx;

        private RowCellBuilder(TableRowBuilder trb) {
            tableRowBuilder = trb; width = trb.nextCellSize(); cellStyle = trb.cellStyle();
            textStyle = trb.textStyle(); colIdx = trb.nextCellIdx();
        }

        // I think setting the width after creation is a pretty bad idea for this class since so much
        // is put into getting the width and column correct.
        // public TableRowCellBuilder width(float w) { width = w; return this; }

        /** {@inheritDoc} */
        @Override public RowCellBuilder cellStyle(CellStyle cs) { cellStyle = cs; return this;}

        public RowCellBuilder borderStyle(BorderStyle bs) {
            cellStyle = cellStyle.borderStyle(bs); return this;
        }

        /** {@inheritDoc} */
        @Override public RowCellBuilder align(CellStyle.Align align) {
            cellStyle = cellStyle.align(align); return this;
        }

        /** {@inheritDoc} */
        @Override public RowCellBuilder textStyle(TextStyle x) { textStyle = x; return this; }

        /** {@inheritDoc} */
        @Override public RowCellBuilder add(Renderable rs) {
            // TODO: Is this correct???  Adding rows and returning a row cell builder???
            Collections.addAll(rows, rs); return this;
        }

        /** {@inheritDoc} */
        @Override public RowCellBuilder addAll(Collection<? extends Renderable> js) {
            if (js != null) { rows.addAll(js); } return this;
        }

        /** {@inheritDoc} */
        @Override public RowCellBuilder addStrs(String... ss) {
            if (textStyle == null) {
                throw new IllegalStateException("Must set a default text style before adding" +
                                                " raw strings");
            }
            for (String s : ss) {
                rows.add(new Text(textStyle, s));
            }
            return this;
        }

        /** {@inheritDoc} */
        @Override public RowCellBuilder add(TextStyle ts, Iterable<String> ls) {
            if (ls != null) {
                for (String s : ls) {
                    rows.add(new Text(ts, s));
                }
            }
            return this;
        }

        public TableRowBuilder buildCell() {
            Cell c = Cell.of(cellStyle, width, rows);
            return tableRowBuilder.addCellAt(c, colIdx);
        }

        /** {@inheritDoc} */
        @Override  public float width() { return width; }

        @Override public String toString() {
            return new StringBuilder("RowCellBuilder(").append(tableRowBuilder).append(" colIdx=")
                    .append(colIdx).append(")").toString();
        }

        @Override
        public int hashCode() {
            return tableRowBuilder.hashCode() + colIdx;
        }

        @Override
        public boolean equals(Object other) {
            // Cheapest operation first...
            if (this == other) { return true; }

            if ((other == null) ||
                !(other instanceof RowCellBuilder) ||
                (this.hashCode() != other.hashCode())) {
                return false;
            }
            // Details...
            final RowCellBuilder that = (RowCellBuilder) other;

            return (this.colIdx == that.colIdx) && tableRowBuilder.equals(that.tableRowBuilder);
        }
    }
}
