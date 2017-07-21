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
 Use this to create Tables.  This strives to remind the programmer of HTML tables but because you
 can resize and scroll a browser window, and not a piece of paper, this is fundamentally different.
 Still familiarity with HTML may make this class easier to use.
 */
public class TableBuilder {
    private final List<Float> cellWidths = new ArrayList<Float>(1);
    private CellStyle cellStyle;
    private TextStyle textStyle;
    private final List<TablePart> parts = new ArrayList<TablePart>(2);

    private TableBuilder() {}

    public static TableBuilder of() {
        return new TableBuilder();
    }

    /** Returns the default widths for all table parts (if set). */
    public List<Float> cellWidths() { return Collections.unmodifiableList(cellWidths); }

    /** Sets default widths for all table parts. */
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

    public Table buildTable() { return new Table(parts); }
}
