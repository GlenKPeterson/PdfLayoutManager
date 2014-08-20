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
import java.util.List;

/* Unsynchronized mutable class which is not thread-safe. */
public class TableRowBuilder {
    private final TablePart tablePart;
    private final List<Cell> cells;

//    private TableRow(TablePart tp, float[] a, Cell[] b, CellStyle c, TextStyle d) {
//        tablePart = tp; cellWidths = a; cells = b; cellStyle = c; textStyle = d;
//    }

    private TableRowBuilder(TablePart tp) {
        tablePart = tp;
        cells = new ArrayList<Cell>(tp.cellWidths().size());
    }

    public static TableRowBuilder of(TablePart tp) { return new TableRowBuilder(tp); }

    public TableRowBuilder addCell(Renderable... things) {
        for (Renderable thing : things) {
            cells.add(Cell.of(tablePart.cellStyle(),
                              tablePart.cellWidths().get(cells.size()).floatValue(), thing));
        }
        return this;
    }

    public Cell.Builder cellBuilder() {
        return Cell.builder(tablePart.cellStyle(),
                            tablePart.cellWidths().get(cells.size()).floatValue())
                .textStyle(tablePart.textStyle());
    }

    public TablePart buildRow() {
        // TODO: Do we want to fill out the row with blank cells?
        return tablePart.addRow(this);
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
