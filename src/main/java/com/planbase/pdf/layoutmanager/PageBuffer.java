// Copyright 2017-06-09 PlanBase Inc. & Glen Peterson
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

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.organicdesign.fp.function.Fn2;
import org.organicdesign.fp.oneOf.Option;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 Caches the contents of a specific, single page for later drawing.  Inner classes are what's added
 to the cache and what controlls the drawing.  You generally want to use {@link LogicalPage} when
 you want automatic page-breaking.  PageBuffer is for when you want to force something onto a
 specific page only.
 */
public class PageBuffer implements RenderTarget {
    private final PdfLayoutMgr mgr;
    final int pageNum;
    // The x-offset for the body section of this page (left-margin-ish)
    private final float xOff;
    private long lastOrd = 0;
    private final Set<PdfItem> items = new TreeSet<>();

    PageBuffer(int pn, PdfLayoutMgr m, Option<Fn2<Integer,PageBuffer,Float>> pr) {
        pageNum = pn;
        mgr = m;
        xOff = pr.match(r -> r.apply(pageNum, this),
                        () -> 0f);
    }

    void fillRect(float x, float y, float width, float height, Color c, float zIdx) {
        items.add(new FillRect(x + xOff, y, width, height, c, lastOrd++, zIdx));
    }
    /** {@inheritDoc} */
    @Override public PageBuffer fillRect(XyOffset topLeft, XyDim dim, Color c) {
        fillRect(topLeft.x(), topLeft.y(), dim.width(), dim.height(), c, -1);
        return this;
    }
//        public void fillRect(final float xVal, final float yVal, final float w, final Color c,
//                             final float h) {
//            fillRect(xVal, yVal, w, h, c, PdfItem.DEFAULT_Z_INDEX);
//        }
//
//        public void drawJpeg(final float xVal, final float yVal, final BufferedImage bi,
//                             final PdfLayoutMgr mgr, final float z) {
//            items.add(DrawJpeg.of(xVal, yVal, bi, mgr, lastOrd++, z));
//        }

    /** {@inheritDoc} */
    @Override
    public PageBuffer drawJpeg(float x, float y, ScaledJpeg sj) {
        items.add(new DrawJpeg(x + xOff, y, sj, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX));
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PageBuffer drawPng(float x, float y, ScaledPng sj) {
        items.add(new DrawPng(x + xOff, y, sj, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX));
        return this;
    }

    private void drawLine(float xa, float ya, float xb, float yb, LineStyle ls, float z) {
        items.add(new DrawLine(xa + xOff, ya, xb + xOff, yb, ls, lastOrd++, z));
    }
    /** {@inheritDoc} */
    @Override
    public PageBuffer drawLine(float xa, float ya, float xb, float yb, LineStyle ls) {
        drawLine(xa, ya, xb, yb, ls, PdfItem.DEFAULT_Z_INDEX);
        return this;
    }

    private void drawStyledText(float x, float y, String text, TextStyle s, float z) {
        items.add(new Text(x + xOff, y, text, s, lastOrd++, z));
    }
    /** {@inheritDoc} */
    @Override
    public PageBuffer drawStyledText(float x, float y, String text, TextStyle s) {
        drawStyledText(x, y, text, s, PdfItem.DEFAULT_Z_INDEX);
        return this;
    }

    void commit(PDPageContentStream stream) throws IOException {
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (PdfItem item : items) { item.commit(stream); }
    }

    private static class DrawLine extends PdfItem {
        private final float x1, y1, x2, y2;
        private final LineStyle style;
        private DrawLine(float xa, float ya, float xb, float yb, LineStyle s,
                         long ord, float z) {
            super(ord, z);
            x1 = xa; y1 = ya; x2 = xb; y2 = yb; style = s;
        }
        @Override public void commit(PDPageContentStream stream) throws IOException {
            stream.setStrokingColor(style.color());
            stream.setLineWidth(style.width());
            stream.moveTo(x1, y1);
            stream.lineTo(x2, y2);
            stream.stroke();
        }
    }

    private static class FillRect extends PdfItem {
        private final float x, y, width, height;
        private final Color color;
        private FillRect(float xVal, float yVal, float w, float h, Color c, long ord, float z) {
            super(ord, z);
            x = xVal; y = yVal; width = w; height = h; color = c;
        }
        @Override public void commit(PDPageContentStream stream) throws IOException {
            stream.setNonStrokingColor(color);
            stream.addRect(x, y, width, height);
            stream.fill();
        }
    }

    static class Text extends PdfItem {
        public final float x, y;
        public final String t;
        public final TextStyle style;
        Text(float xCoord, float yCoord, String text, TextStyle s,
             long ord, float z) {
            super(ord, z);
            x = xCoord; y = yCoord; t = text; style = s;
        }
        @Override public void commit(PDPageContentStream stream) throws IOException {
            stream.beginText();
            stream.setNonStrokingColor(style.textColor());
            stream.setFont(style.font(), style.fontSize());
            stream.newLineAtOffset(x, y);
            stream.showText(t);
            stream.endText();
        }
    }

    private static class DrawPng extends PdfItem {
        private final float x, y;
        private final PDImageXObject png;
        private final ScaledPng scaledPng;
        private DrawPng(float xVal, float yVal, ScaledPng sj, PdfLayoutMgr mgr,
                        long ord, float z) {
            super(ord, z);
            x = xVal; y = yVal;
            png = mgr.ensureCached(sj);
            scaledPng = sj;
        }
        @Override public void commit(PDPageContentStream stream) throws IOException {
            // stream.drawImage(png, x, y);
            XyDim dim = scaledPng.dimensions();
            stream.drawImage(png, x, y, dim.width(), dim.height());
        }
    }

    private static class DrawJpeg extends PdfItem {
        private final float x, y;
        private final PDImageXObject jpeg;
        private final ScaledJpeg scaledJpeg;
        private DrawJpeg(float xVal, float yVal, ScaledJpeg sj, PdfLayoutMgr mgr,
                         long ord, float z) {
            super(ord, z);
            x = xVal; y = yVal;
            jpeg = mgr.ensureCached(sj);
            scaledJpeg = sj;
        }
        @Override public void commit(PDPageContentStream stream) throws IOException {
            // stream.drawImage(jpeg, x, y);
            XyDim dim = scaledJpeg.dimensions();
            stream.drawImage(jpeg, x, y, dim.width(), dim.height());
        }
    }
}