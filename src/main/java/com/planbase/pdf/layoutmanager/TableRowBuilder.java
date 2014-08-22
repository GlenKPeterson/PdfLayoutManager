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
import java.util.Collections;
import java.util.List;

/* Unsynchronized mutable class which is not thread-safe. */
public class TableRowBuilder {
    private final TablePart tablePart;
    private TextStyle textStyle;
    private CellStyle cellStyle;
    private final List<Cell> cells;
    private float minRowHeight = 0;

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
        if (tablePart.numCellWidths() <= cells.size()) {
            throw new IllegalStateException("Tried to add more cells than you set sizes for");
        }
        return tablePart.cellWidths().get(cells.size()).floatValue();
    }

    public static TableRowBuilder of(TablePart tp) { return new TableRowBuilder(tp); }

    public TextStyle textStyle() { return textStyle; }
    public TableRowBuilder textStyle(TextStyle x) { textStyle = x; return this; }

    public CellStyle cellStyle() { return cellStyle; }

    public TableRowBuilder addCells(Cell... cs) {
        Collections.addAll(cells, cs);
        return this;
    }

    public TableRowBuilder addTextCells(String... ss) {
        if (textStyle == null) {
            throw new IllegalStateException("Tried to add a text cell without setting a default text style");
        }
        for (String s : ss) {
            cells.add(Cell.of(tablePart.cellStyle(),
                              nextCellSize(), Text.of(textStyle, s)));

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
        return this;
    }

    public TableRowBuilder minRowHeight(float f) { minRowHeight = f; return this; }

    public Cell.Builder cellBuilder() {
        return Cell.builder(this);
    }

    public TablePart buildRow() {
        // Do we want to fill out the row with blank cells?
        return tablePart.addRow(this);
    }

    public XyDim calcDimensions() {
        XyDim maxDim = XyDim.ZERO;
        // Similar to PdfLayoutMgr.putRow().  Should be combined?
        for (Cell cell : cells) {
            XyDim wh = cell.calcDimensions(cell.width());
            maxDim = XyDim.of(wh.x() + maxDim.x(),
                              Float.max(maxDim.y(), wh.y()));
        }
        return maxDim;
    }

    public XyOffset render(LogicalPage lp, XyOffset outerTopLeft,
                           boolean allPages) {
        XyDim maxDim = XyDim.ZERO.y(minRowHeight);
        for (Cell cell : cells) {
            XyDim wh = cell.calcDimensions(cell.width());
            maxDim = XyDim.of(maxDim.x() + cell.width(),
                              Float.max(maxDim.y(), wh.y()));
        }
        float maxHeight = maxDim.y();

        float x = outerTopLeft.x();
        for (Cell cell : cells) {
//            System.out.println("\t\tAbout to render cell: " + cell);
            // TODO: Cache the duplicate cell.calcDimensions call!!!
            cell.render(lp, XyOffset.of(x, outerTopLeft.y()),
                        XyDim.of(cell.width(), maxHeight), allPages);
            x += cell.width();
        }
        return XyOffset.of(x, outerTopLeft.y() - maxHeight);
    }

//    public static TableRow of(float[] cellWidths, Cell[] cells, CellStyle cellStyle,
//                              TextStyle textStyle) {
//        return new TableRow(cellWidths, cells, cellStyle, textStyle);
//    }

//    public float[] cellWidths() { return cellWidths; }
//    public TableRow cellWidths(float[] x) { return new Builder().cellWidths(cellWidths).build(); }

//    public Cell[] cells() { return cells; }
//    public TableRow cells(Cell[] x) { return new Builder().cells(cells).build(); }

//    public CellStyle cellStyle() { return cellStyle; }
//    public TableRow cellStyle(CellStyle x) { return new Builder().cellStyle(cellStyle).build(); }

//    public TextStyle textStyle() { return textStyle; }
//    public TableRow textStyle(TextStyle x) { return new Builder().textStyle(textStyle).build(); }

//    public static Builder builder(TablePart tp) { return new Builder(tp); }
//
//    public static class Builder {
//        private final TablePart tablePart;
//        private float[] cellWidths;
////        private Cell[] cells;
//        private CellStyle cellStyle;
//        private TextStyle textStyle;
//
//        private Builder(TablePart tp) {
//            tablePart = tp; cellWidths = tp.cellWidths(); cellStyle = tp.cellStyle();
//            textStyle = tp.textStyle();
//        }
//
//        public Builder cellWidths(float[] x) { cellWidths = x; return this; }
//
////        public Builder cells(Cell[] x) { cells = x; return this; }
//        public Builder cellStyle(CellStyle x) { cellStyle = x; return this; }
//        public Builder textStyle(TextStyle x) { textStyle = x; return this; }
//
//        public TableRow build() {
//            return new TableRow(tablePart, cellWidths, cells, cellStyle, textStyle);
//        }
//    } // end of class Builder
}
