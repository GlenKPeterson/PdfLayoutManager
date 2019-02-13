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

/**
 * A set of styles to be the default for a table header or footer, or whatever other kind of group of table rows you
 * dream up.
 */
public class TablePart {
    private final TableBuilder tableBuilder;
    private List<Double> cellWidths = new ArrayList<Double>();
    private CellStyle cellStyle;
    private TextStyle textStyle;
    private double minRowHeight = 0;
    private final List<TableRowBuilder> rows = new ArrayList<TableRowBuilder>(1);

    private TablePart(TableBuilder t) {
        tableBuilder = t; cellWidths.addAll(t.cellWidths()); cellStyle = t.cellStyle();
        textStyle = t.textStyle();
    }

    public static TablePart of(TableBuilder t) { return new TablePart(t); }

//    private TablePart(Table t, double[] a, CellStyle b, TextStyle c) {
//        table = t; cellWidths = a; cellStyle = b; textStyle = c;
//    }

//    public static TablePart of(double[] cellWidths, CellStyle cellStyle, TextStyle textStyle) {
//        return new TablePart(cellWidths, cellStyle, textStyle);
//    }

    public List<Double> cellWidths() { return Collections.unmodifiableList(cellWidths); }
    public double cellWidth(int i) { return cellWidths.get(i); }

//    public TablePart replaceAllCellWidths(List<Double> x) { cellWidths = x; return this; }
//    public TablePart addCellWidths(List<Double> x) { cellWidths.addAll(x); return this; }
    public TablePart addCellWidths(double... ws) {
        for (double w : ws) { cellWidths.add(w); }
        return this;
    }
    public TablePart addCellWidth(Double x) { cellWidths.add(x); return this; }

    public int numCellWidths() { return cellWidths.size(); }

//    public TablePart cellWidths(double[] x) { return new Builder().cellWidths(cellWidths).build(); }

    public CellStyle cellStyle() { return cellStyle; }
    public TablePart cellStyle(CellStyle x) { cellStyle = x; return this; }
    public TablePart align(CellStyle.Align a) { cellStyle = cellStyle.align(a); return this; }

//    public TablePart cellStyle(CellStyle x) { return new Builder().cellStyle(cellStyle).build(); }

    public TextStyle textStyle() { return textStyle; }
    public TablePart textStyle(TextStyle x) { textStyle = x; return this; }

    public double minRowHeight() { return minRowHeight; }
    public TablePart minRowHeight(double f) { minRowHeight = f; return this; }

    public TableRowBuilder rowBuilder() { return TableRowBuilder.of(this); }

    public TablePart addRow(TableRowBuilder trb) { rows.add(trb); return this; }

    public TableBuilder buildPart() { return tableBuilder.addPart(this); }

    public Dim calcDimensions() {
        Dim maxDim = Dim.ZERO;
        for (TableRowBuilder row : rows) {
            Dim wh = row.calcDimensions();
            maxDim = Dim.of(Math.max(wh.getWidth(), maxDim.getWidth()),
                            maxDim.getHeight() + wh.getHeight());
        }
        return maxDim;
    }

    public Coord render(LogicalPage lp, Coord outerTopLeft, boolean allPages) {
        Coord rightmostLowest = outerTopLeft;
        for (TableRowBuilder row : rows) {
//            System.out.println("\tAbout to render row: " + row);
            Coord rl = row.render(lp, Coord.of(outerTopLeft.getX(), rightmostLowest.getY()),
                                  allPages);
            rightmostLowest = Coord.of(Math.max(rl.getX(), rightmostLowest.getX()),
                                       Math.min(rl.getY(), rightmostLowest.getY()));
        }
        return rightmostLowest;
    }

    @Override
    public String toString() {
        return new StringBuilder("TablePart(").append(tableBuilder).append(" ")
                .append(System.identityHashCode(this)).append(")").toString();

    }

//    public static Builder builder(TableBuilder t) { return new Builder(t); }
//
//    public static class Builder {
//        private final TableBuilder tableBuilder;
//        private double[] cellWidths;
//        private CellStyle cellStyle;
//        private TextStyle textStyle;
//
//        private Builder(TableBuilder t) { tableBuilder = t; }
//
//        public Builder cellWidths(double[] x) { cellWidths = x; return this; }
//        public Builder cellStyle(CellStyle x) { cellStyle = x; return this; }
//        public Builder textStyle(TextStyle x) { textStyle = x; return this; }
//
//        public TablePart build() { return new TablePart(tableBuilder, cellWidths, cellStyle, textStyle); }
//    } // end of class Builder
}
