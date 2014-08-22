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

public class TableBuilder implements Renderable {
    private final LogicalPage logicalPage;
    private final XyOffset topLeft;
    private final List<Float> cellWidths = new ArrayList<Float>(1);
    private CellStyle cellStyle;
    private TextStyle textStyle;
    private final List<TablePart> parts = new ArrayList<TablePart>(2);

    private TableBuilder(LogicalPage lp, XyOffset tl) {
        logicalPage = lp; topLeft = tl;
    }
    public static TableBuilder of(LogicalPage lp, XyOffset tl) {
        return new TableBuilder(lp, tl);
    }

    public XyOffset topLeft() { return topLeft; }

    public List<Float> cellWidths() { return Collections.unmodifiableList(cellWidths); }
    public TableBuilder addCellWidths(List<Float> x) { cellWidths.addAll(x); return this; }
    public TableBuilder addCellWidths(float... ws) {
        for (float w : ws) { cellWidths.add(w); }
        return this;
    }
    public TableBuilder addCellWidth(Float x) { cellWidths.add(x); return this; }

    public CellStyle cellStyle() { return cellStyle; }
    public TableBuilder cellStyle(CellStyle x) { cellStyle = x; return this; }

    public TextStyle textStyle() { return textStyle; }
    public TableBuilder textStyle(TextStyle x) { textStyle = x; return this; }

    public TableBuilder addPart(TablePart tp) { parts.add(tp); return this; }

    public TablePart partBuilder() { return TablePart.of(this); }

    public XyOffset buildTable() { return logicalPage.addTable(this); }

    public XyDim calcDimensions(float maxWidth) {
        XyDim maxDim = XyDim.ZERO;
        for (TablePart part : parts) {
            XyDim wh = part.calcDimensions();
            maxDim = XyDim.of(Float.max(wh.x(), maxDim.x()),
                              maxDim.y() + wh.y());
        }
        return maxDim;
    }

    /*
    Renders item and all child-items with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).
    */
    public XyOffset render(LogicalPage lp, XyOffset outerTopLeft, XyDim outerDimensions,
                           boolean allPages) {
        XyOffset rightmostLowest = outerTopLeft;
        for (TablePart part : parts) {
//            System.out.println("About to render part: " + part);
            XyOffset rl = part.render(lp, XyOffset.of(outerTopLeft.x(), rightmostLowest.y()),
                                      allPages);
            rightmostLowest = XyOffset.of(Float.max(rl.x(), rightmostLowest.x()),
                                          Float.min(rl.y(), rightmostLowest.y()));
        }
        return rightmostLowest;
    }

}
