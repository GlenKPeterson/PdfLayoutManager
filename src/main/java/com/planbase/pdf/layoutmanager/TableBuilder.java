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

public class TableBuilder { // implements Renderable {
    private final LogicalPageBuilder logicalPageBuilder;
    private final XyPair topLeft;
    private final List<Float> cellWidths = new ArrayList<Float>(1);
    private CellStyle cellStyle;
    private TextStyle textStyle;
    private final List<TablePart> parts = new ArrayList<TablePart>(2);

    private TableBuilder(LogicalPageBuilder lp, XyPair tl) {
        logicalPageBuilder = lp; topLeft = tl;
    }
    public static TableBuilder of(LogicalPageBuilder lp, XyPair tl) {
        return new TableBuilder(lp, tl);
    }

    public XyPair topLeft() { return topLeft; }

    public List<Float> cellWidths() { return Collections.unmodifiableList(cellWidths); }
    public TableBuilder addCellWidths(List<Float> x) { cellWidths.addAll(x); return this; }
    public TableBuilder addCellWidth(Float x) { cellWidths.add(x); return this; }

    public CellStyle cellStyle() { return cellStyle; }
    public TableBuilder cellStyle(CellStyle x) { cellStyle = x; return this; }

    public TextStyle textStyle() { return textStyle; }
    public TableBuilder textStyle(TextStyle x) { textStyle = x; return this; }

    public TableBuilder addPart(TablePart tp) { parts.add(tp); return this; }

    public TablePart partBuilder() { return TablePart.of(this); }

//    public LogicalPageBuilder buildTable() { return logicalPageBuilder.addRenderable(this); }

//    public XyPair calcDimensions(float maxWidth) {
//
//    }
//
//
//    public XyPair render(XyPair p, boolean allPages, PdfLayoutMgr mgr, float maxWidth) {
//        if (p == null) { p = topLeft; }
//        if (mgr == null) { mgr = logicalPageBuilder.documentBuilder().layoutMgr(); }
//
//    }

}
