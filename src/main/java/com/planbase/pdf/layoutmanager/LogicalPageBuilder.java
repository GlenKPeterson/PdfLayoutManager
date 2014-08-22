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
import java.util.Set;
import java.util.TreeSet;

import org.apache.pdfbox.pdmodel.common.PDRectangle;

public class LogicalPageBuilder {
    private final DocumentBuilder documentBuilder;
    private TextStyle textStyle;
    private PDRectangle pageDimensions;
    private Padding pageMargins;
    private PDRectangle printableArea;

    private Set<PdfItem> borderItems = new TreeSet<PdfItem>();

    public final int pageNum;
    private long lastOrd = 0;
    private final Set<PdfItem> items = new TreeSet<PdfItem>();


    private List<Renderable> renderables = new ArrayList<Renderable>();

    private LogicalPageBuilder(DocumentBuilder d, int pn) {
        pageNum = pn;
        documentBuilder = d; textStyle = d.textStyle(); pageDimensions = d.pageDimensions();
        pageMargins = d.pageMargins(); printableArea = d.printableArea();
    }

    public static LogicalPageBuilder of(DocumentBuilder d, int pn) {
        return new LogicalPageBuilder(d, pn);
    }

    public TextStyle textStyle() { return textStyle; }
    public LogicalPageBuilder textStyle(TextStyle x) { textStyle = x; return this; }

    public PDRectangle pageDimensions() { return pageDimensions; }
    public LogicalPageBuilder pageDimensions(PDRectangle x) { pageDimensions = x; return this; }

    public Padding pageMargins() { return pageMargins; }
    public LogicalPageBuilder pageMargins(Padding x) { pageMargins = x; return this; }

    public PDRectangle printableArea() { return printableArea; }
    public LogicalPageBuilder printableArea(PDRectangle x) { printableArea = x; return this; }

    public DocumentBuilder documentBuilder() { return documentBuilder; }

    public DocumentBuilder buildLogicalPage() { return documentBuilder.addLogicalPage(this); }

    public TableBuilder tableBuilder(XyOffset tl) { return TableBuilder.of(this, tl); }

    public LogicalPageBuilder addRenderable(Renderable r) { renderables.add(r); return this; }

}
