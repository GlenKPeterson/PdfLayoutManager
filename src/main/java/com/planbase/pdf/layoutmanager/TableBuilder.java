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
 * Use this to create Tables.  This strives to remind the programmer of HTML tables but because you can resize and
 * scroll a browser window, but not a piece of paper, this is fundamentally different.  Still familiarity with HTML may
 * make this class easier to use.
 */
public class TableBuilder implements Renderable {
    private final LogicalPage logicalPage;
    private final Coord topLeft;
    private final List<Double> cellWidths = new ArrayList<Double>(1);
    private CellStyle cellStyle;
    private TextStyle textStyle;
    private final List<TablePart> parts = new ArrayList<TablePart>(2);

    private TableBuilder(LogicalPage lp, Coord tl) {
        logicalPage = lp; topLeft = tl;
    }
    public static TableBuilder of(LogicalPage lp, Coord tl) {
        return new TableBuilder(lp, tl);
    }

    public Coord topLeft() { return topLeft; }

    public List<Double> cellWidths() { return Collections.unmodifiableList(cellWidths); }
    public TableBuilder addCellWidths(List<Double> x) { cellWidths.addAll(x); return this; }
    public TableBuilder addCellWidths(double... ws) {
        for (double w : ws) { cellWidths.add(w); }
        return this;
    }
    public TableBuilder addCellWidth(Double x) { cellWidths.add(x); return this; }

    public CellStyle cellStyle() { return cellStyle; }
    public TableBuilder cellStyle(CellStyle x) { cellStyle = x; return this; }

    public TextStyle textStyle() { return textStyle; }
    public TableBuilder textStyle(TextStyle x) { textStyle = x; return this; }

    public TableBuilder addPart(TablePart tp) { parts.add(tp); return this; }

    public TablePart partBuilder() { return TablePart.of(this); }

    public Coord buildTable() { return logicalPage.addTable(this); }

    public Dim calcDimensions(double maxWidth) {
        Dim maxDim = Dim.ZERO;
        for (TablePart part : parts) {
            Dim wh = part.calcDimensions();
            maxDim = Dim.of(Math.max(wh.getWidth(), maxDim.getWidth()),
                            maxDim.getHeight() + wh.getHeight());
        }
        return maxDim;
    }

    /*
    Renders item and all child-items with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).
    */
    public Coord render(LogicalPage lp, Coord outerTopLeft, Dim outerDimensions,
                        boolean allPages) {
        Coord rightmostLowest = outerTopLeft;
        for (TablePart part : parts) {
//            System.out.println("About to render part: " + part);
            Coord rl = part.render(lp, Coord.of(outerTopLeft.getX(), rightmostLowest.getY()),
                                   allPages);
            rightmostLowest = Coord.of(Math.max(rl.getX(), rightmostLowest.getX()),
                                       Math.min(rl.getY(), rightmostLowest.getY()));
        }
        return rightmostLowest;
    }

    @Override
    public String toString() {
        return new StringBuilder("TableBuilder(").append(System.identityHashCode(this))
                .append(")").toString();

    }
}
