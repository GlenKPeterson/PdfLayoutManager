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

@Deprecated // User TablePart instead
public class TableHead {
    private final TableBuilder tableBuilder;
    private final float[] cellWidths;
    private final CellStyle cellStyle;
    private final TextStyle textStyle;

    private TableHead(TableBuilder t, float[] a, CellStyle b, TextStyle c) {
        tableBuilder = t; cellWidths = a; cellStyle = b; textStyle = c;
    }

//    public static TableHead of(float[] cellWidths, CellStyle cellStyle, TextStyle textStyle) {
//        return new TableHead(cellWidths, cellStyle, textStyle);
//    }

    public float[] cellWidths() { return cellWidths; }
//    public TableHead cellWidths(float[] x) { return new Builder().cellWidths(cellWidths).build(); }

    public CellStyle cellStyle() { return cellStyle; }
//    public TableHead cellStyle(CellStyle x) { return new Builder().cellStyle(cellStyle).build(); }

    public TextStyle textStyle() { return textStyle; }
//    public TableHead textStyle(TextStyle x) { return new Builder().textStyle(textStyle).build(); }

    public static Builder builder(TableBuilder t) { return new Builder(t); }

    public static class Builder {
        private final TableBuilder tableBuilder;
        private float[] cellWidths;
        private CellStyle cellStyle;
        private TextStyle textStyle;

        private Builder(TableBuilder t) { tableBuilder = t; }

        public Builder cellWidths(float[] x) { cellWidths = x; return this; }
        public Builder cellStyle(CellStyle x) { cellStyle = x; return this; }
        public Builder textStyle(TextStyle x) { textStyle = x; return this; }

        public TableHead build() { return new TableHead(tableBuilder, cellWidths, cellStyle, textStyle); }
    } // end of class Builder
}
