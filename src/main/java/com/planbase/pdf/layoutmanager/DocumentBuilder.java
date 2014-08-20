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

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class DocumentBuilder {
    private final LayoutManager layoutManager;
    private TextStyle textStyle;
    private PDRectangle pageDimensions;
    private Padding pageMargins;
    private PDRectangle printableArea;
    private List<LogicalPageBuilder> pages = new ArrayList<LogicalPageBuilder>(1);

    private DocumentBuilder(LayoutManager lm) { layoutManager = lm; }

//    private DocumentBuilder(LayoutManager lm, TextStyle a, PDRectangle b, Padding c, PDRectangle d) {
//        layoutManager = lm;
//        textStyle = a; pageDimensions = b; pageMargins = c; printableArea = d;
//    }

    public static DocumentBuilder of(LayoutManager lm) { return new DocumentBuilder(lm); }
//    , TextStyle textStyle, PDRectangle pageDimensions, Padding pageMargins,
//                              PDRectangle printableArea) {
//        return new DocumentBuilder(textStyle, pageDimensions, pageMargins, printableArea);
//    }

    public LogicalPageBuilder logicalPageBuilder() { return LogicalPageBuilder.of(this); }

    public DocumentBuilder addLogicalPage(LogicalPageBuilder lpb) { pages.add(lpb); return this; }

    public TextStyle textStyle() { return textStyle; }
    public DocumentBuilder textStyle(TextStyle x) { textStyle = x; return this; }

    public PDRectangle pageDimensions() { return pageDimensions; }
    public DocumentBuilder pageDimensions(PDRectangle x) { pageDimensions = x; return this; }

    public Padding pageMargins() { return pageMargins; }
    public DocumentBuilder pageMargins(Padding x) { pageMargins = x; return this; }

    public PDRectangle printableArea() { return printableArea; }
    public DocumentBuilder printableArea(PDRectangle x) { printableArea = x; return this; }

    public LayoutManager layoutManager() { return layoutManager; }
}
