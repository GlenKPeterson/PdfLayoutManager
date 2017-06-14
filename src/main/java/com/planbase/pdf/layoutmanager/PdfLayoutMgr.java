// Copyright 2012-01-10 PlanBase Inc. & Glen Peterson
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

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;
import org.organicdesign.fp.function.Fn2;
import org.organicdesign.fp.oneOf.Option;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.planbase.pdf.layoutmanager.LogicalPage.Orientation.LANDSCAPE;

/**
 <p>The main class in this package; it handles page and line breaks.</p>

<h3>Usage (the unit test is a much better example):</h3>
<pre><code>// Create a new manager
PdfLayoutMgr pageMgr = PdfLayoutMgr.newRgbPageMgr();

LogicalPage lp = pageMgr.logicalPageStart();
// defaults to Landscape orientation
// call various lp.tableBuilder() or lp.put...() methods here.
// They will page-break and create extra physical pages as needed.
// ...
lp.commit();

lp = pageMgr.logicalPageStart(LogicalPage.Orientation.PORTRAIT);
// These pages will be in Portrait orientation
// call various lp methods to put things on the next page grouping
// ...
lp.commit();

// The file to write to
OutputStream os = new FileOutputStream("test.pdf");

// Commit all pages to output stream.
pageMgr.save(os);</code></pre>
<br>
<h3>Note:</h3>
<p>Because this class buffers and writes to an underlying stream, it is mutable, has side effects,
 and is NOT thread-safe!</p>
 */
public class PdfLayoutMgr {

    // private Logger logger = Logger.getLogger(PdfLayoutMgr.class);

//        logger.info("Ascent: " + PDType1Font.HELVETICA.getFontDescriptor().getAscent());
//        logger.info("StemH: " + PDType1Font.HELVETICA.getFontDescriptor().getStemH());
//        logger.info("CapHeight: " + PDType1Font.HELVETICA.getFontDescriptor().getCapHeight());
//        logger.info("XHeight: " + PDType1Font.HELVETICA.getFontDescriptor().getXHeight());
//        logger.info("Descent: " + PDType1Font.HELVETICA.getFontDescriptor().getDescent());
//        logger.info("Leading: " + PDType1Font.HELVETICA.getFontDescriptor().getLeading());
//
//        logger.info("Height: " + PDType1Font.HELVETICA.getFontDescriptor().getFontBoundingBox().getHeight());
//
//        Ascent:    718.0
//        StemH:       0.0
//        CapHeight: 718.0
//        XHeight:   523.0
//        Descent:  -207.0
//        Leading:     0.0
//        Height:   1156.0
    // CapHeight - descent = 925
    // 925 - descent = 1132 which is still less than 1156.
    // I'm going to make line-height =
    // Java FontMetrics says getHeight() = getAscent() + getDescent() + getLeading().
    // I think ascent and descent are compatible with this.  I'm going to make Leading be
    // -descent/2

    /**
     If you use no scaling when printing the output PDF, PDFBox shows approximately 72 
     Document-Units Per Inch.  This makes one pixel on an average desktop monitor correspond to
     roughly one document unit.  This is a useful constant for page layout math.
     */
    public static final float DOC_UNITS_PER_INCH = 72f;

// TODO: add Sensible defaults, such as textStyle?
//    private TextStyle textStyle;
//    private PDRectangle pageDimensions;
//    private Padding pageMargins;
//    private PDRectangle printableArea;
//
//    public TextStyle textStyle() { return textStyle; }
//    public PDRectangle pageDimensions() { return pageDimensions; }
//    public Padding pageMargins() { return pageMargins; }
//    public PDRectangle printableArea() { return printableArea; }

    // You can have many DrawJpegs backed by only a few images - it is a flyweight, and this
    // hash map keeps track of the few underlying images, even as intances of DrawJpeg
    // represent all the places where these images are used.
    // CRITICAL: This means that the the set of jpgs must be thrown out and created anew for each
    // document!  Thus, a private final field on the PdfLayoutMgr instead of DrawJpeg, and DrawJpeg
    // must be an inner class (or this would have to be package scoped).
    private final Map<BufferedImage,PDImageXObject> jpegMap = new HashMap<>();

    PDImageXObject ensureCached(final ScaledJpeg sj) {
        BufferedImage bufferedImage = sj.bufferedImage();
        PDImageXObject temp = jpegMap.get(bufferedImage);
        if (temp == null) {
            try {
                temp = JPEGFactory.createFromImage(doc, bufferedImage);
            } catch (IOException ioe) {
                 // can there ever be an exception here?  Doesn't it get written later?
                throw new IllegalStateException("Caught exception creating a PDImageXObject from a bufferedImage", ioe);
            }
            jpegMap.put(bufferedImage, temp);
        }
        return temp;
    }

    // You can have many DrawPngs backed by only a few images - it is a flyweight, and this
    // hash map keeps track of the few underlying images, even as intances of DrawPng
    // represent all the places where these images are used.
    // CRITICAL: This means that the the set of jpgs must be thrown out and created anew for each
    // document!  Thus, a private final field on the PdfLayoutMgr instead of DrawPng, and DrawPng
    // must be an inner class (or this would have to be package scoped).
    private final Map<BufferedImage,PDImageXObject> pngMap = new HashMap<>();

    PDImageXObject ensureCached(final ScaledPng sj) {
        BufferedImage bufferedImage = sj.bufferedImage();
        PDImageXObject temp = pngMap.get(bufferedImage);
        if (temp == null) {
            try {
                temp = LosslessFactory.createFromImage(doc, bufferedImage);
            } catch (IOException ioe) {
                 // can there ever be an exception here?  Doesn't it get written later?
                throw new IllegalStateException("Caught exception creating a PDImageXObject from a bufferedImage", ioe);
            }
            pngMap.put(bufferedImage, temp);
        }
        return temp;
    }

    private final List<PageBuffer> pages = new ArrayList<>();
    private final PDDocument doc;
    // Just to start, takes a page number and returns an x-offset for that page.
    private Fn2<Integer,PageBuffer,Float> pageReactor = null;

    // pages.size() counts the first page as 1, so 0 is the appropriate sentinel value
    private int unCommittedPageIdx = 0;

    private final PDColorSpace colorSpace;
    private final XyDim pageDim;
//    private final PDRectangle pageSize;

    List<PageBuffer> pages() { return Collections.unmodifiableList(pages); }

    private PdfLayoutMgr(PDColorSpace cs, PDRectangle mb) {
        doc = new PDDocument();
        colorSpace = cs;
        pageDim = XyDim.of((mb == null) ? PDRectangle.LETTER
                                        : mb);
    }

    /**
     Returns a new PdfLayoutMgr with the given color space.
     @param cs the color-space.
     @return a new PdfLayoutMgr
     */
    public static PdfLayoutMgr of(PDColorSpace cs) {
        return new PdfLayoutMgr(cs, null);
    }

    /**
     Returns a new PdfLayoutMgr with the given color space and page size.
     @param cs the color-space.
     @param pageSize the page size.  There are a bunch of presets in
     org.apache.pdfbox.pdmodel.PDPage like PAGE_SIZE_LETTER, PAGE_SIZE_A1, and PAGE_SIZE_A4.
     @return a new PdfLayoutMgr
     */
    public static PdfLayoutMgr of(PDColorSpace cs, PDRectangle pageSize) {
        return new PdfLayoutMgr(cs, pageSize);
    }

    /**
     Creates a new PdfLayoutMgr with the PDDeviceRGB color space.
     @return a new Page Manager with an RGB color space
     */
    public static PdfLayoutMgr newRgbPageMgr() {
        return new PdfLayoutMgr(PDDeviceRGB.INSTANCE, null);
    }

    /**
     Returns the width and height of the paper-size where THE HEIGHT IS ALWAYS THE LONGER DIMENSION.
     You may need to swap these for landscape: `pageDim().swapWh()`.  For this reason, it's not a
     good idea to use this directly.  Use the corrected values through a {@link LogicalPage}
     instead.
     */
    XyDim pageDim() { return pageDim; }

    /**
     Returns the correct page for the given value of y.  This lets the user use any Y value and
     we continue extending their canvas downward (negative) by adding extra pages.
     @param y the un-adjusted y value.
     @return the proper page and adjusted y value for that page.
     */
    LogicalPage.PageBufferAndY appropriatePage(LogicalPage lp, float y, float height) {
        if (pages.size() < 1) {
            throw new IllegalStateException("Cannot work with the any pages until one has been" +
                                            " created by calling newPage().");
        }
        int idx = unCommittedPageIdx;
        // Get the first possible page.  Just keep moving to the top of the next page until it's in
        // the printable area.
        while (y < lp.yBodyBottom()) {
            y += lp.bodyHeight();
            idx++;
            if (pages.size() <= idx) {
                pages.add(new PageBuffer(pages.size() + 1, this, pageReactor()));
            }
        }
        PageBuffer ps = pages.get(idx);
        float adj = 0;
        if (y + height > lp.yBodyTop()) {
            float oldY = y;
            y = lp.yBodyTop() - height;
            adj = y - oldY;
        }
        return new LogicalPage.PageBufferAndY(ps, y, adj);
    }

    /**
    Call this to commit the PDF information to the underlying stream after it is completely built.
    */
    public void save(OutputStream os) throws IOException {
        doc.save(os);
        doc.close();
    }

    // TODO: Add logicalPage() method and call pages.add() lazily for the first item actually shown on a page, and logicalPageEnd called before a save.
    /**
     Tells this PdfLayoutMgr that you want to start a new logical page (which may be broken across
     two or more physical pages) in the requested page orientation.
     */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public LogicalPage logicalPageStart(LogicalPage.Orientation o,
                                        Fn2<Integer,PageBuffer,Float> pr) {
        pageReactor = pr;
        PageBuffer pb = new PageBuffer(pages.size() + 1, this, pageReactor());
        pages.add(pb);
        return LogicalPage.of(this, o);
    }

    /**
     Tells this PdfLayoutMgr that you want to start a new logical page (which may be broken across
     two or more physical pages) in the requested page orientation.
     */
    public LogicalPage logicalPageStart(LogicalPage.Orientation o) {
        return logicalPageStart(o, null);
    }

    /**
     Get a new logical page (which may be broken across two or more physical pages) in Landscape orientation.
     */
    public LogicalPage logicalPageStart() { return logicalPageStart(LANDSCAPE, null); }

    /**
     Loads a TrueType font (and embeds it into the document?) from the given file into a
     PDType0Font object.
     */
    public PDType0Font loadTrueTypeFont(File fontFile) throws IOException {
        return PDType0Font.load(doc, fontFile);
    }

    /**
     Call this when you are through with your current set of pages to commit all pending text and
     drawing operations.  This is the only method that throws an IOException because the purpose of
     PdfLayoutMgr is to buffer all operations until a page is complete so that it can safely be
     written to the underlying stream.  This method turns the potential pages into real output.
     Call when you need a page break, or your document is done and you need to write it out.

     @throws IOException - if there is a failure writing to the underlying stream.
     */
    void logicalPageEnd(LogicalPage lp) throws IOException {

        // Write out all uncommitted pages.
        while (unCommittedPageIdx < pages.size()) {
            PDPage pdPage = new PDPage(pageDim.toRect());
            if (lp.orientation() == LANDSCAPE) {
                pdPage.setRotation(90);
            }
            PDPageContentStream stream = null;
            try {
                stream = new PDPageContentStream(doc, pdPage);
                doc.addPage(pdPage);

                if (lp.orientation() == LANDSCAPE) {
                    stream.transform(new Matrix(0, 1, -1, 0, pageDim.width(), 0));
                }
                stream.setStrokingColor(colorSpace.getInitialColor());
                stream.setNonStrokingColor(colorSpace.getInitialColor());

                PageBuffer pb = pages.get(unCommittedPageIdx);
                pb.commit(stream);
                lp.commitBorderItems(stream);

                stream.close();
                // Set to null to show that no exception was thrown and no need to close again.
                stream = null;
            } finally {
                // Let it throw an exception if the closing doesn't work.
                if (stream != null) {
                    stream.close();
                }
            }
            unCommittedPageIdx++;
        }
    }

    /** Sets the pageReactor function. */
    public PdfLayoutMgr pageReactor(Fn2<Integer,PageBuffer,Float> pr) { pageReactor = pr; return this; }

    /** Returns the pageReactor function. */
    public Option<Fn2<Integer,PageBuffer,Float>> pageReactor() {
        return pageReactor == null ? Option.none() : Option.of(pageReactor);
    }

    @Override
    public boolean equals(Object other) {
        // First, the obvious...
        if (this == other) { return true; }
        if (other == null) { return false; }
        if (!(other instanceof PdfLayoutMgr)) { return false; }
        // Details...
        final PdfLayoutMgr that = (PdfLayoutMgr) other;
        return this.doc.equals(that.doc) && (this.pages.equals(that.pages));
    }

    @Override
    public int hashCode() {
        return doc.hashCode() + pages.hashCode();
    }

//    public XyOffset putRect(XyOffset outerTopLeft, XyDim outerDimensions, final Color c) {
////        System.out.println("putRect(" + outerTopLeft + " " + outerDimensions + " " +
////                           Utils.toString(c) + ")");
//        putRect(outerTopLeft.x(), outerTopLeft.y(), outerDimensions.x(), outerDimensions.y(), c);
//        return XyOffset.of(outerTopLeft.x() + outerDimensions.x(),
//                           outerTopLeft.y() - outerDimensions.y());
//    }

//    /**
//     Puts text on the page.
//     @param x the x-value of the top-left corner.
//     @param origY the logical-page Y-value of the top-left corner.
//     @param cell the cell containing the styling and text to render.
//     @return the bottom Y-value (logical-page) of the rendered cell.
//     */
//    public float putCell(final float x, float origY, final Cell cell) {
//        return cell.processRows(x, origY, false, this);
//    }

}
