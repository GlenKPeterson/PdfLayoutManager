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
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public static final double DOC_UNITS_PER_INCH = 72;

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

    private PDImageXObject ensureCached(final ScaledJpeg sj) {
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

    private PDImageXObject ensureCached(final ScaledPng sj) {
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

    /**
     * Please don't access this class directly if you don't have to.  It's a little bit like a model for stuff that
     * needs to be drawn on a page, but much more like a heap of random functionality that sort of landed in an
     * inner class.  This will probably be refactored away in future releases.
     */
    static class PageBuffer {
        public final int pageNum;
        private long lastOrd = 0;
        private final Set<PdfItem> items = new TreeSet<>();

        private PageBuffer(int pn) {
            pageNum = pn;
        }

        void fillRect(final double xVal, final double yVal, final double w, final double h,
                             final Color c, final double z) {
            items.add(FillRect.of(xVal, yVal, w, h, c, lastOrd++, z));
        }

//        public void fillRect(final double xVal, final double yVal, final double w, final Color c,
//                             final double h) {
//            fillRect(xVal, yVal, w, h, c, PdfItem.DEFAULT_Z_INDEX);
//        }
//
//        public void drawJpeg(final double xVal, final double yVal, final BufferedImage bi,
//                             final PdfLayoutMgr mgr, final double z) {
//            items.add(DrawJpeg.of(xVal, yVal, bi, mgr, lastOrd++, z));
//        }

        void drawJpeg(final double xVal, final double yVal, final ScaledJpeg sj,
                      final PdfLayoutMgr mgr) {
            items.add(DrawJpeg.of(xVal, yVal, sj, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX));
        }

        void drawPng(final double xVal, final double yVal, final ScaledPng sj,
                      final PdfLayoutMgr mgr) {
            items.add(DrawPng.of(xVal, yVal, sj, mgr, lastOrd++, PdfItem.DEFAULT_Z_INDEX));
        }

        private void drawLine(final double xa, final double ya, final double xb,
                              final double yb, final LineStyle ls, final double z) {
            items.add(DrawLine.of(xa, ya, xb, yb, ls, lastOrd++, z));
        }
        void drawLine(final double xa, final double ya, final double xb, final double yb,
                              final LineStyle ls) {
            drawLine(xa, ya, xb, yb, ls, PdfItem.DEFAULT_Z_INDEX);
        }

        private void drawStyledText(final double xCoord, final double yCoord, final String text,
                                   TextStyle s, final double z) {
            items.add(Text.of(xCoord, yCoord, text, s, lastOrd++, z));
        }
        void drawStyledText(final double xCoord, final double yCoord, final String text,
                                   TextStyle s) {
            drawStyledText(xCoord, yCoord, text, s, PdfItem.DEFAULT_Z_INDEX);
        }

        private void commit(PDPageContentStream stream) throws IOException {
            // Since items are z-ordered, then sub-ordered by entry-order, we will draw
            // everything in the correct order.
            for (PdfItem item : items) { item.commit(stream); }
        }

        private static class DrawLine extends PdfItem {
            private final double x1, y1, x2, y2;
            private final LineStyle style;
            private DrawLine(final double xa, final double ya, final double xb, final double yb,
                             LineStyle s,
                             final long ord, final double z) {
                super(ord, z);
                x1 = xa; y1 = ya; x2 = xb; y2 = yb; style = s;
            }
            public static DrawLine of(final double xa, final double ya, final double xb,
                                      final double yb, LineStyle s,
                                      final long ord, final double z) {
                return new DrawLine(xa, ya, xb, yb, s, ord, z);
            }
            @Override
            public void commit(PDPageContentStream stream) throws IOException {
                stream.setStrokingColor(style.color());
                stream.setLineWidth(toFloat(style.width()));
                stream.moveTo(toFloat(x1), toFloat(y1));
                stream.lineTo(toFloat(x2), toFloat(y2));
                stream.stroke();
            }
        }

        private static class FillRect extends PdfItem {
            private final double x, y, width, height;
            private final Color color;
            private FillRect(final double xVal, final double yVal, final double w, final double h,
                             final Color c, final long ord, final double z) {
                super(ord, z);
                x = xVal; y = yVal; width = w; height = h; color = c;
            }
            public static FillRect of(final double xVal, final double yVal, final double w,
                                      final double h, final Color c, final long ord, final double z) {
                return new FillRect(xVal, yVal, w, h, c, ord, z);
            }
            @Override
            public void commit(PDPageContentStream stream) throws IOException {
                stream.setNonStrokingColor(color);
                stream.addRect(toFloat(x), toFloat(y), toFloat(width), toFloat(height));
                stream.fill();
            }
        }

        static class Text extends PdfItem {
            public final double x, y;
            public final String t;
            public final TextStyle style;
            private Text(final double xCoord, final double yCoord, final String text,
                         TextStyle s, final long ord, final double z) {
                super(ord, z);
                x = xCoord; y = yCoord; t = text; style = s;
            }
            public static Text of(final double xCoord, final double yCoord, final String text,
                                  TextStyle s, final long ord, final double z) {
                return new Text(xCoord, yCoord, text, s, ord, z);
            }
            @Override
            public void commit(PDPageContentStream stream) throws IOException {
                stream.beginText();
                stream.setNonStrokingColor(style.textColor());
                stream.setFont(style.font(), toFloat(style.fontSize()));
                stream.newLineAtOffset(toFloat(x), toFloat(y));
                stream.showText(t);
                stream.endText();
            }
        }

        private static class DrawPng extends PdfItem {
            private final double x, y;
            private final PDImageXObject png;
            private final ScaledPng scaledPng;

            // private Log logger = LogFactory.getLog(DrawPng.class);

            private DrawPng(final double xVal, final double yVal, final ScaledPng sj,
                             final PdfLayoutMgr mgr,
                             final long ord, final double z) {
                super(ord, z);
                x = xVal; y = yVal;
                png = mgr.ensureCached(sj);
                scaledPng = sj;
            }
            public static DrawPng of(final double xVal, final double yVal, final ScaledPng sj,
                                      final PdfLayoutMgr mgr,
                                      final long ord, final double z) {
                return new DrawPng(xVal, yVal, sj, mgr, ord, z);
            }
            @Override
            public void commit(PDPageContentStream stream) throws IOException {
                // stream.drawImage(png, x, y);
                Dim dim = scaledPng.dimensions();
                stream.drawImage(png, toFloat(x), toFloat(y), toFloat(dim.getWidth()), toFloat(dim.getHeight()));
            }
        }

        private static class DrawJpeg extends PdfItem {
            private final double x, y;
            private final PDImageXObject jpeg;
            private final ScaledJpeg scaledJpeg;

            // private Log logger = LogFactory.getLog(DrawJpeg.class);

            private DrawJpeg(final double xVal, final double yVal, final ScaledJpeg sj,
                             final PdfLayoutMgr mgr,
                             final long ord, final double z) {
                super(ord, z);
                x = xVal; y = yVal;
                jpeg = mgr.ensureCached(sj);
                scaledJpeg = sj;
            }
            public static DrawJpeg of(final double xVal, final double yVal, final ScaledJpeg sj,
                                      final PdfLayoutMgr mgr,
                                      final long ord, final double z) {
                return new DrawJpeg(xVal, yVal, sj, mgr, ord, z);
            }
            @Override
            public void commit(PDPageContentStream stream) throws IOException {
                // stream.drawImage(jpeg, x, y);
                Dim dim = scaledJpeg.dimensions();
                stream.drawImage(jpeg, toFloat(x), toFloat(y), toFloat(dim.getWidth()), toFloat(dim.getHeight()));
            }
        }
    }

    private final List<PageBuffer> pages = new ArrayList<>();
    private final PDDocument doc;

    // pages.size() counts the first page as 1, so 0 is the appropriate sentinel value
    private int unCommittedPageIdx = 0;

    private final PDColorSpace colorSpace;
    private final PDRectangle pageSize;

    List<PageBuffer> pages() { return Collections.unmodifiableList(pages); }

    private PdfLayoutMgr(PDColorSpace cs, PDRectangle mb) {
        doc = new PDDocument();
        colorSpace = cs;
        pageSize = (mb == null) ? PDRectangle.LETTER
                                : mb;
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
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public static PdfLayoutMgr newRgbPageMgr() {
        return new PdfLayoutMgr(PDDeviceRGB.INSTANCE, null);
    }

    /** Returns the page width given the defined PDRectangle pageSize */
    public double pageWidth() { return pageSize.getWidth(); }

    /** Returns the page height given the defined PDRectangle pageSize */
    public double pageHeight() { return pageSize.getHeight(); }

    /**
     Returns the correct page for the given value of y.  This lets the user use any Y value and
     we continue extending their canvas downward (negative) by adding extra pages.
     @param y the un-adjusted y value.
     @return the proper page and adjusted y value for that page.
     */
    LogicalPage.PageBufferAndY appropriatePage(LogicalPage lp, double y) {
        if (pages.size() < 1) {
            throw new IllegalStateException("Cannot work with the any pages until one has been created by calling newPage().");
        }
        int idx = unCommittedPageIdx;
        // Get the first possible page

        while (y < lp.yPageBottom()) {
            // logger.info("Adjusting y.  Was: " + y + " about to add " + printAreaHeight);
            y += lp.printAreaHeight(); // y could even be negative.  Just keep moving to the top of the next
            // page until it's in the printable area.
            idx++;
            if (pages.size() <= idx) {
                pages.add(new PageBuffer(pages.size() + 1));
            }
        }
        PageBuffer ps = pages.get(idx);
        return new LogicalPage.PageBufferAndY(ps, y);
    }

    /**
    Call this to commit the PDF information to the underlying stream after it is completely built.
    */
    public void save(OutputStream os) throws IOException {
        doc.save(os);
        doc.close();
    }

    // TODO: Add logicalPage() method and call pages.add() lazily for the first item actually shown on a page, and logicalPageEnd called before a save.
    // TODO: Add feature for different paper size or orientation for each group of logical pages.
    /**
     Tells this PdfLayoutMgr that you want to start a new logical page (which may be broken across
     two or more physical pages) in the requested page orientation.
     */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public LogicalPage logicalPageStart(LogicalPage.Orientation o) {
        PageBuffer pb = new PageBuffer(pages.size() + 1);
        pages.add(pb);
        return LogicalPage.of(this, o);
    }

    /**
     Get a new logical page (which may be broken across two or more physical pages) in Landscape orientation.
     */
    public LogicalPage logicalPageStart() { return logicalPageStart(LogicalPage.Orientation.LANDSCAPE); }

//    void addLogicalPage(PageBuffer pb) {
//        pages.add(pb);
//    }

    /**
     Call this when you are through with your current set of pages to commit all pending text and
     drawing operations.  This is the only method that throws an IOException because the purpose of
     PdfLayoutMgr is to buffer all operations until a page is complete so that it can safely be
     written to the underlying stream.  This method turns the potential pages into real output.
     Call when you need a page break, or your document is done and you need to write it out.

     @throws IOException - if there is a failure writing to the underlying stream.
     */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    void logicalPageEnd(LogicalPage lp) throws IOException {

        // Write out all uncommitted pages.
        while (unCommittedPageIdx < pages.size()) {
            PDPage pdPage = new PDPage(pageSize);
            if (lp.orientation() == LogicalPage.Orientation.LANDSCAPE) {
                pdPage.setRotation(90);
            }
            PDPageContentStream stream = null;
            try {
                stream = new PDPageContentStream(doc, pdPage);
                doc.addPage(pdPage);

                if (lp.orientation() == LogicalPage.Orientation.LANDSCAPE) {
                    stream.transform(new Matrix(0f, 1f, -1f, 0f, toFloat(lp.pageWidth()), 0f));
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
//    public double putCell(final double x, double origY, final Cell cell) {
//        return cell.processRows(x, origY, false, this);
//    }



    private static final String ISO_8859_1 = "ISO_8859_1";
    private static final String UNICODE_BULLET = "\u2022";

    // PDFBox uses an encoding that the PDF spec calls WinAnsiEncoding.  The spec says this is
    // Windows Code Page 1252.
    // http://en.wikipedia.org/wiki/Windows-1252
    // It has a lot in common with ISO-8859-1, but it defines some additional characters such as
    // the Euro symbol.  Unfortunately, just because the PDF Spec has a euro symbol in this encoding
    // does *not* mean that the built-in fonts support it!  So really this code is not about converting to
    // WinAnsi as the name implies, but making every character printable in the built-in PDF fonts.
    private static final Map<String,String> utf16ToWinAnsi;
    static {
        Map<String,String> tempMap = new HashMap<>();

        try {
            // These are the low ASCII control codes.  Most of which are non-printing characters and
            // meaningless for our purposes, so can be replaced with an empty string.
            tempMap.put("\u0000", ""); // null
            tempMap.put("\u0001", ""); // Start of Heading
            tempMap.put("\u0002", ""); // Start of Text
            tempMap.put("\u0003", ""); // End of Text
            tempMap.put("\u0004", ""); // End of Transmission
            tempMap.put("\u0005", ""); // Enquiry
            tempMap.put("\u0006", ""); // Acknowledgement
            tempMap.put("\u0007", ""); // Bell
            tempMap.put("\u0008", ""); // Backspace

            // Let these fall through for now.
//            tempMap.put("\u0009", ""); // Tab
//            tempMap.put("\n", ""); // Line Feed

            tempMap.put("\u000b", ""); // Vertical tab
            tempMap.put("\u000c", "\n"); // Form Feed
            tempMap.put("\r", "\n"); // Carriage Return
            tempMap.put("\u000e", ""); // Shift out
            tempMap.put("\u000f", ""); // Shift in
            tempMap.put("\u0010", ""); // Data Link Escape
            tempMap.put("\u0011", ""); // Device Control 1
            tempMap.put("\u0012", ""); // DC2
            tempMap.put("\u0013", ""); // DC3
            tempMap.put("\u0014", ""); // DC4
            tempMap.put("\u0015", ""); // Negative Acknowledgement
            tempMap.put("\u0016", ""); // Synchronous Idle
            tempMap.put("\u0017", ""); // End of Transmission Block
            tempMap.put("\u0018", ""); // Cancel
            tempMap.put("\u0019", ""); // End of Medium
            tempMap.put("\u001a", ""); // Substitute
            tempMap.put("\u001b", ""); // Escape
            tempMap.put("\u001c", ""); // File Separator
            tempMap.put("\u001d", ""); // Group Separator
            tempMap.put("\u001e", ""); // Record Separator
            tempMap.put("\u001f", ""); // Unit Separator

            // ASCII of course has one high control code.
            tempMap.put("\u007f", ""); // Delete

            // 129, 141, 143, 144, and 157 are undefined in WinAnsi.
            // I had mapped A0-FF to 160-255 without noticing that that maps each character to
            // itself, meaning that Unicode and WinAnsii are the same in that range.

            // Unicode characters with exact WinAnsi equivalents
            tempMap.put("\u0152", new String(new byte[]{0,(byte)140},ISO_8859_1)); // OE
            tempMap.put("\u0153", new String(new byte[]{0,(byte)156},ISO_8859_1)); // oe
            tempMap.put("\u0160", new String(new byte[]{0,(byte)138},ISO_8859_1)); // S Acron
            tempMap.put("\u0161", new String(new byte[]{0,(byte)154},ISO_8859_1)); // s acron
            tempMap.put("\u0178", new String(new byte[]{0,(byte)159},ISO_8859_1)); // Y Diaeresis
            tempMap.put("\u017D", new String(new byte[]{0,(byte)142},ISO_8859_1)); // Capital Z-caron
            tempMap.put("\u017E", new String(new byte[]{0,(byte)158},ISO_8859_1)); // Lower-case Z-caron
            tempMap.put("\u0192", new String(new byte[]{0,(byte)131},ISO_8859_1)); // F with a hook (like jf put together)
            tempMap.put("\u02C6", new String(new byte[]{0,(byte)136},ISO_8859_1)); // circumflex (up-caret)
            tempMap.put("\u02DC", new String(new byte[]{0,(byte)152},ISO_8859_1)); // Tilde

            // Cyrillic letters map to their closest Romanizations according to ISO 9:1995
            // http://en.wikipedia.org/wiki/ISO_9
            // http://en.wikipedia.org/wiki/A_(Cyrillic)

            // Cyrillic extensions
            // 0400 Ѐ Cyrillic capital letter IE WITH GRAVE
            // ≡ 0415 Е  0300 (left-accent)
            tempMap.put("\u0400", new String(new byte[]{0,(byte)200},ISO_8859_1));
            // 0401 Ё Cyrillic capital letter IO
            // ≡ 0415 Е  0308 (diuresis)
            tempMap.put("\u0401", new String(new byte[]{0,(byte)203},ISO_8859_1));
            // 0402 Ђ Cyrillic capital letter DJE
            tempMap.put("\u0402", new String(new byte[]{0,(byte)208},ISO_8859_1));
            // 0403 Ѓ Cyrillic capital letter GJE
            // ≡ 0413 Г  0301 (accent)
            // Ghe only maps to G-acute, which is not in our charset.
            // 0404 Є Cyrillic capital letter UKRAINIAN IE
            tempMap.put("\u0404", new String(new byte[]{0,(byte)202},ISO_8859_1));
            // 0405 Ѕ Cyrillic capital letter DZE
            tempMap.put("\u0405", "S"); //
            // 0406 І Cyrillic capital letter BYELORUSSIAN-
            // UKRAINIAN I
            // → 0049 I  latin capital letter i
            // → 0456 і  cyrillic small letter byelorussian-
            // ukrainian i
            // → 04C0 Ӏ  cyrillic letter palochka
            tempMap.put("\u0406", new String(new byte[]{0,(byte)204},ISO_8859_1));
            // 0407 Ї Cyrillic capital letter YI
            // ≡ 0406 І  0308 (diuresis)
            tempMap.put("\u0407", new String(new byte[]{0,(byte)207},ISO_8859_1));
            // 0408 Ј Cyrillic capital letter JE
            // 0409 Љ Cyrillic capital letter LJE
            // 040A Њ Cyrillic capital letter NJE
            // 040B Ћ Cyrillic capital letter TSHE
            // 040C Ќ Cyrillic capital letter KJE
            // ≡ 041A К  0301 (accent)
            // 040D Ѝ Cyrillic capital letter I WITH GRAVE
            // ≡ 0418 И  0300 (accent)
            // 040E Ў Cyrillic capital letter SHORT U
            // ≡ 0423 У  0306 (accent)
            // 040F Џ Cyrillic capital letter DZHE

            // Basic Russian alphabet
            // See: http://www.unicode.org/charts/PDF/U0400.pdf
            // 0410 А Cyrillic capital letter A => Latin A
            tempMap.put("\u0410", "A");
            // 0411 Б Cyrillic capital letter BE => Latin B
            // → 0183 ƃ  latin small letter b with topbar
            tempMap.put("\u0411", "B");
            // 0412 В Cyrillic capital letter VE => Latin V
            tempMap.put("\u0412", "V");
            // 0413 Г Cyrillic capital letter GHE => Latin G
            tempMap.put("\u0413", "G");
            // 0414 Д Cyrillic capital letter DE => Latin D
            tempMap.put("\u0414", "D");
            // 0415 Е Cyrillic capital letter IE => Latin E
            tempMap.put("\u0415", "E");
            // 0416 Ж Cyrillic capital letter ZHE => Z-caron
            tempMap.put("\u0416", new String(new byte[]{0,(byte)142},ISO_8859_1));
            // 0417 З Cyrillic capital letter ZE => Latin Z
            tempMap.put("\u0417", "Z");
            // 0418 И Cyrillic capital letter I => Latin I
            tempMap.put("\u0418", "I");
            // 0419 Й Cyrillic capital letter SHORT I => Latin J
            // ≡ 0418 И  0306 (a little mark)
            // The two-character form (reversed N plus the mark) is not supported.
            tempMap.put("\u0419", "J");
            // 041A К Cyrillic capital letter KA => Latin K
            tempMap.put("\u041A", "K");
            // 041B Л Cyrillic capital letter EL => Latin L
            tempMap.put("\u041B", "L");
            // 041C М Cyrillic capital letter EM => Latin M
            tempMap.put("\u041C", "M");
            // 041D Н Cyrillic capital letter EN => Latin N
            tempMap.put("\u041D", "N");
            // 041E О Cyrillic capital letter O => Latin O
            tempMap.put("\u041E", "O");
            // 041F П Cyrillic capital letter PE => Latin P
            tempMap.put("\u041F", "P");
            // 0420 Р Cyrillic capital letter ER => Latin R
            tempMap.put("\u0420", "R");
            // 0421 С Cyrillic capital letter ES => Latin S
            tempMap.put("\u0421", "S");
            // 0422 Т Cyrillic capital letter TE => Latin T
            tempMap.put("\u0422", "T");
            // 0423 У Cyrillic capital letter U => Latin U
            // → 0478 Ѹ  cyrillic capital letter uk
            // → 04AF ү  cyrillic small letter straight u
            // → A64A Ꙋ  cyrillic capital letter monograph uk
            tempMap.put("\u0423", "U");
            tempMap.put("\u0478", "U"); // Is this right?
            tempMap.put("\u04AF", "U"); // Is this right?
            tempMap.put("\uA64A", "U"); // Is this right?
            // 0424 Ф Cyrillic capital letter EF => Latin F
            tempMap.put("\u0424", "F");
            // 0425 Х Cyrillic capital letter HA => Latin H
            tempMap.put("\u0425", "H");
            // 0426 Ц Cyrillic capital letter TSE => Latin C
            tempMap.put("\u0426", "C");
            // 0427 Ч Cyrillic capital letter CHE => Mapping to "Ch" because there is no
            // C-caron - hope this is the best choice!  A also had this as "CH" but some make it
            // Tch as in Tchaikovsky, really didn't know what to do here.
            tempMap.put("\u0427", "Ch");
            // 0428 Ш Cyrillic capital letter SHA => S-caron
            tempMap.put("\u0428", new String(new byte[]{0,(byte)138},ISO_8859_1));
            // 0429 Щ Cyrillic capital letter SHCHA => Latin "Shch" because there is no
            // S-circumflex to map it to.  Should it go to S-caron like SHA?
            tempMap.put("\u0429", "Shch");
            // 042A Ъ Cyrillic capital letter HARD SIGN => Latin double prime, or in this case,
            // right double-quote.
            tempMap.put("\u042A", new String(new byte[]{0,(byte)148},ISO_8859_1));
            // 042B Ы Cyrillic capital letter YERU => Latin Y
            tempMap.put("\u042B", "Y");
            // 042C Ь Cyrillic capital letter SOFT SIGN => Latin prime, or in this case,
            // the right-single-quote.
            tempMap.put("\u042C", new String(new byte[]{0,(byte)146},ISO_8859_1));
            // 042D Э Cyrillic capital letter E => Latin E-grave
            tempMap.put("\u042D", new String(new byte[]{0,(byte)200},ISO_8859_1));
            // 042E Ю Cyrillic capital letter YU => Latin U-circumflex
            tempMap.put("\u042E", new String(new byte[]{0,(byte)219},ISO_8859_1));
            // 042F Я Cyrillic capital letter YA => A-circumflex
            tempMap.put("\u042F", new String(new byte[]{0,(byte)194},ISO_8859_1));
            // 0430 а Cyrillic small letter A
            tempMap.put("\u0430", "a");
            // 0431 б Cyrillic small letter BE
            tempMap.put("\u0431", "b");
            // 0432 в Cyrillic small letter VE
            tempMap.put("\u0432", "v");
            // 0433 г Cyrillic small letter GHE
            tempMap.put("\u0433", "g");
            // 0434 д Cyrillic small letter DE
            tempMap.put("\u0434", "d");
            // 0435 е Cyrillic small letter IE
            tempMap.put("\u0435", "e");
            // 0436 ж Cyrillic small letter ZHE
            tempMap.put("\u0436", new String(new byte[]{0,(byte)158},ISO_8859_1));
            // 0437 з Cyrillic small letter ZE
            tempMap.put("\u0437", "z");
            // 0438 и Cyrillic small letter I
            tempMap.put("\u0438", "i");
            // 0439 й Cyrillic small letter SHORT I
            // ≡ 0438 и  0306 (accent)
            tempMap.put("\u0439", "j");
            // 043A к Cyrillic small letter KA
            tempMap.put("\u043A", "k");
            // 043B л Cyrillic small letter EL
            tempMap.put("\u043B", "l");
            // 043C м Cyrillic small letter EM
            tempMap.put("\u043C", "m");
            // 043D н Cyrillic small letter EN
            tempMap.put("\u043D", "n");
            // 043E о Cyrillic small letter O
            tempMap.put("\u043E", "o");
            // 043F п Cyrillic small letter PE
            tempMap.put("\u043F", "p");
            // 0440 р Cyrillic small letter ER
            tempMap.put("\u0440", "r");
            // 0441 с Cyrillic small letter ES
            tempMap.put("\u0441", "s");
            // 0442 т Cyrillic small letter TE
            tempMap.put("\u0442", "t");
            // 0443 у Cyrillic small letter U
            tempMap.put("\u0443", "u");
            // 0444 ф Cyrillic small letter EF
            tempMap.put("\u0444", "f");
            // 0445 х Cyrillic small letter HA
            tempMap.put("\u0445", "h");
            // 0446 ц Cyrillic small letter TSE
            tempMap.put("\u0446", "c");
            // 0447 ч Cyrillic small letter CHE - see notes on capital letter.
            tempMap.put("\u0447", "ch");
            // 0448 ш Cyrillic small letter SHA
            tempMap.put("\u0448", new String(new byte[]{0,(byte)154},ISO_8859_1));
            // 0449 щ Cyrillic small letter SHCHA
            tempMap.put("\u0449", "shch");
            // 044A ъ Cyrillic small letter HARD SIGN
            tempMap.put("\u044A", new String(new byte[]{0,(byte)148},ISO_8859_1));
            // 044B ы Cyrillic small letter YERU
            // → A651 ꙑ  cyrillic small letter yeru with back yer
            tempMap.put("\u044B", "y");
            // 044C ь Cyrillic small letter SOFT SIGN
            // → 0185 ƅ  latin small letter tone six
            // → A64F ꙏ  cyrillic small letter neutral yer
            tempMap.put("\u044C", new String(new byte[]{0,(byte)146},ISO_8859_1));
            // 044D э Cyrillic small letter E
            tempMap.put("\u044D", new String(new byte[]{0,(byte)232},ISO_8859_1));
            // 044E ю Cyrillic small letter YU
            // → A655 ꙕ  cyrillic small letter reversed yu
            tempMap.put("\u044E", new String(new byte[]{0,(byte)251},ISO_8859_1));
            tempMap.put("\uA655", new String(new byte[]{0,(byte)251},ISO_8859_1)); // is this right?
            // 044F я Cyrillic small letter YA => a-circumflex
            tempMap.put("\u044F", new String(new byte[]{0,(byte)226},ISO_8859_1));

            // Cyrillic extensions
            // 0450 ѐ CYRILLIC SMALL LETTER IE WITH GRAVE
            // • Macedonian
            // ≡ 0435 е  0300 $̀
            tempMap.put("\u0450", new String(new byte[]{0,(byte)232},ISO_8859_1)); // e-grave => e-grave
            // 0451 ё CYRILLIC SMALL LETTER IO
            // • Russian, ...
            // ≡ 0435 е  0308 $̈
            tempMap.put("\u0451", new String(new byte[]{0,(byte)235},ISO_8859_1));
            // 0452 ђ CYRILLIC SMALL LETTER DJE
            // • Serbian
            // → 0111 đ  latin small letter d with stroke
            tempMap.put("\u0452", new String(new byte[]{0,(byte)240},ISO_8859_1));
            // 0453 ѓ CYRILLIC SMALL LETTER GJE - only maps to g-acute, which is not in our charset.
            // • Macedonian
            // ≡ 0433 г  0301 $́
            // 0454 є CYRILLIC SMALL LETTER UKRAINIAN IE
            // = Old Cyrillic yest
            tempMap.put("\u0454", new String(new byte[]{0,(byte)234},ISO_8859_1));
            // 0455 ѕ CYRILLIC SMALL LETTER DZE
            // • Macedonian
            // → A643 ꙃ  cyrillic small letter dzelo
            tempMap.put("\u0455", "s");
            // 0456 CYRILLIC SMALL LETTER BYELORUSSIAN-
            // UKRAINIAN I
            // = Old Cyrillic i
            tempMap.put("\u0456", new String(new byte[]{0,(byte)236},ISO_8859_1));
            // 0457 ї CYRILLIC SMALL LETTER YI
            // • Ukrainian
            // ≡ 0456 і  0308 $̈
            tempMap.put("\u0457", new String(new byte[]{0,(byte)239},ISO_8859_1));
            // 0458 ј CYRILLIC SMALL LETTER JE
            // • Serbian, Azerbaijani, Altay
            // 0459 љ CYRILLIC SMALL LETTER LJE
            // • Serbian, Macedonian
            // → 01C9 lj  latin small letter lj
            // 045A њ CYRILLIC SMALL LETTER NJE
            // • Serbian, Macedonian
            // → 01CC nj  latin small letter nj
            // 045B ћ CYRILLIC SMALL LETTER TSHE
            // • Serbian
            // → 0107 ć  latin small letter c with acute
            // → 0127 ħ  latin small letter h with stroke
            // → 040B Ћ  cyrillic capital letter tshe
            // → 210F ħ  planck constant over two pi
            // → A649 ꙉ  cyrillic small letter djerv
            // 045C ќ CYRILLIC SMALL LETTER KJE
            // • Macedonian
            // ≡ 043A к  0301 $́
            // 045D ѝ CYRILLIC SMALL LETTER I WITH GRAVE
            // • Macedonian, Bulgarian
            // ≡ 0438 и  0300 $̀
            // 045E ў CYRILLIC SMALL LETTER SHORT U
            // • Byelorussian, Uzbek
            // ≡ 0443 у  0306 $̆
            // 045F џ CYRILLIC SMALL LETTER DZHE
            // • Serbian, Macedonian, Abkhasian
            // → 01C6 dž  latin small letter dz with caron

            // Extended Cyrillic
            // ...
            // 0490 Ґ CYRILLIC CAPITAL LETTER GHE WITH UPTURN => G ?
            tempMap.put("\u0490", "G"); // Ghe with upturn
            // 0491 ґ CYRILLIC SMALL LETTER GHE WITH UPTURN
            // • Ukrainian
            tempMap.put("\u0491", "g");

            // Other commonly-used unicode characters with exact WinAnsi equivalents
            tempMap.put("\u2013", new String(new byte[]{0,(byte)150},ISO_8859_1)); // En-dash
            tempMap.put("\u2014", new String(new byte[]{0,(byte)151},ISO_8859_1)); // Em-dash
            tempMap.put("\u2018", new String(new byte[]{0,(byte)145},ISO_8859_1)); // Curved single open quote
//            tempMap.put("\u2019", new String(new byte[]{0,(byte)146},ISO_8859_1)); // Curved single close-quote
            tempMap.put("\u2019", "'"); // Curved single close-quote
            tempMap.put("\u201A", new String(new byte[]{0,(byte)130},ISO_8859_1)); // Low single curved-quote
            tempMap.put("\u201C", new String(new byte[]{0,(byte)147},ISO_8859_1)); // Curved double open quote
            tempMap.put("\u201D", new String(new byte[]{0,(byte)148},ISO_8859_1)); // Curved double close-quote
            tempMap.put("\u201E", new String(new byte[]{0,(byte)132},ISO_8859_1)); // Low right double quote.
            tempMap.put("\u2020", new String(new byte[]{0,(byte)134},ISO_8859_1)); // Dagger
            tempMap.put("\u2021", new String(new byte[]{0,(byte)135},ISO_8859_1)); // Double dagger
            // 3. In WinAnsiEncoding, all unused codes greater than 40 map to the bullet character. However, only
            // code 225 octal (149 decimal) shall be specifically assigned to the bullet character; other codes
            // are subject to future reassignment.
            tempMap.put(UNICODE_BULLET, new String(new byte[]{0,(byte)149},ISO_8859_1)); // Bullet - use this as replacement character.
            tempMap.put("\u2026", new String(new byte[]{0,(byte)133},ISO_8859_1)); // Ellipsis
            tempMap.put("\u2030", new String(new byte[]{0,(byte)137},ISO_8859_1)); // Permille
            tempMap.put("\u2039", new String(new byte[]{0,(byte)139},ISO_8859_1)); // Left angle-quote
            tempMap.put("\u203A", new String(new byte[]{0,(byte)155},ISO_8859_1)); // Right angle-quote
            // NO - if this is true, then the font does not support it!
            // 1. In PDF 1.3, the euro character was added to the Adobe standard Latin character set. It shall be
            // encoded as 200 octal (128 decimal) in WinAnsiEncoding and 240 in PDFDocEncoding, assigning codes
            // that were previously unused. Apple changed the Mac OS Latin-text encoding for code 333 from the
            // currency character to the euro character. However, this incompatible change has not been reflected
            // in PDF’s MacRomanEncoding, which shall continue to map code 333 to currency. If the euro character is
            // desired, an encoding dictionary may be used to specify this single difference from
            // MacRomanEncoding.
//            tempMap.put("\u20ac", new String(new byte[]{0,(byte)128},ISO_8859_1)); // Euro symbol
            tempMap.put("\u20ac", "EUR"); // Euro symbol is missing in the font, so use the 3-letter currency code instead.
            tempMap.put("\u2122", new String(new byte[]{0,(byte)153},ISO_8859_1)); // Trademark symbol

        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Problem creating translation table due to Unsupported Encoding (coding error)", uee);
        }
        utf16ToWinAnsi = Collections.unmodifiableMap(tempMap);
    }


    // private static final Pattern whitespacePattern = Pattern.compile("\\p{Z}+");
    // What about \u00ba??
    // \u00a0-\u00a9 \u00ab-\u00b9 \u00bb-\u00bf \u00d7 \u00f7
    private static final Pattern nonAsciiPattern = Pattern.compile("[^\u0000-\u00ff]");

    /**
     <p>Converts UTF-16 Java Strings to safe WinAnsi characters that the built-in
     "Standard Type 1 Fonts (Standard 14 Fonts)" mentioned in section 9.6.2.2 of the PDF spec actually support,
     substituting bullets for characters that have no reasonable substitutes.
     Well, ZapfDingbats does not appear to be built-in, so it's more like the "Standard 13".
     Though "€" is supported in the encoding and in the spec, it's not supported by the fonts, so it's encoded as "EUR".
     Not sure if this method works around any PDFBox limitations/transliteration,
     or if it's all PDF spec and implementations.</p>

     <p>PDF files are limited to the 217 characters of Windows-1252 which the PDF spec calls WinAnsi
     and Java calls ISO-8859-1.  This method transliterates the standard Java UTF-16 character
     representations to their Windows-1252 equivalents where such translation is possible. This transliteration
     covers the modern alphabets of the following languages:<br>
     
     Afrikaans (af),
     Albanian (sq), Basque (eu), Catalan (ca), Danish (da), Dutch (nl), English (en), Faroese (fo),
     Finnish (fi), French (fr), Galician (gl), German (de), Icelandic (is), Irish (ga), 
     Italian (it), Norwegian (no), Portuguese (pt), Scottish (gd), Spanish (es), Swedish (sv).</p>
     
     <p>Romanized substitutions are used for the Cyrillic characters of the modern Russian (ru)
     alphabet according to ISO 9:1995 with the following phonetic substitutions: 'Ch' for Ч and
     'Shch' for Щ.</p>

     @param in a string in the standard Java UTF-16 encoding
     @return a string in Windows-1252 (informally called ISO-8859-1 or WinAnsi)
     */
    public static String toWinAnsi(String in) {
//        ByteBuffer bb = StandardCharsets.UTF_16.encode(CharBuffer.wrap(in));
//        // then decode those bytes as US-ASCII
//        return StandardCharsets.ISO_8859_1.decode(bb).toString();
        // return java.nio.charset.StandardCharsets.ISO_8859_1.encode(in);

        // Got to replace two-character return sequences (Windows Line Feeds) with one character.
        in = in.replace("\r\n", "\n");

        // TODO: Add combining diacritical marks such as converting n~ to ñ
        // TODO: For speed, this could all be loaded into one big Trie.  That would be an interesting project
        // TODO: Create a meaningful unit test.

        Matcher m = nonAsciiPattern.matcher(in);

        StringBuilder sB = new StringBuilder();
        int idx = 0;
        while (m.find()) {

            int start = m.start(); // first character of match.
            if (idx < start) {
                // Append everything from the last match up to this one.
                sB.append(in.subSequence(idx, start));
            }

            String s = utf16ToWinAnsi.get(m.group());

            // "In WinAnsiEncoding, all unused codes greater than 40 map to the bullet character."
            // source: PDF spec, Annex D.3 PDFDocEncoding Character Set p. 656 footnote about
            // WinAnsiEncoding.
            //
            // I think the bullet is the closest thing to a "replacement character" in the
            // WinAnsi character set, so that's what I'll use it for.  It looks tons better than
            // nullnullnull...
            if (s == null) {
                s = utf16ToWinAnsi.get(UNICODE_BULLET);
            }
            sB.append(s);

            idx = m.end(); // m.end() is exclusive
        }
        if (idx < in.length()) {
            sB.append(in.subSequence(idx, in.length()));
        }
        return sB.toString();
    }

    public static float toFloat(double d) {
        return Double.valueOf(d).floatValue();
    }
}
