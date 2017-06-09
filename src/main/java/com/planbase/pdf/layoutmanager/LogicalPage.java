package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 <p>Maybe better called a "DocumentSection" this represents a group of Renderables that logically
 belong on the same page, but may spill over multiple subsequent pages as necessary in order to
 fit.  Headers and footers are tied to this Logical Page / Document Section.</p>

 <p>Here is a typical page layout:</p>
 <pre><code>
 +---------------------+ -.
 | M  Margin Header  M |  |
 | a +-------------+ a |   &gt; Margin body top
 | r |    Header   | r |  |
 | g +-------------+ g | -'
 |   |             |   |
 | B |             | B |
 | o |     Body    | o |
 | d |             | d |
 | y |             | y |
 |   +-------------+   | -.
 | L |    Footer   | R |  |
 | e +-------------+ t |   &gt; Margin body bottom
 | f  Margin Footer    |  |
 +---------------------+ -'
 </code></pre>

 <p>Here is our model</p>
 <pre><code>
  +--------------------+
  |                    |
  |                    |
  |   +------------+   | &lt;- yBodyTop()
  |   |           h|   |
  |   |           e|   |
  |   |           i|   |
  |   |    Body   g|   |
  |   |           h|   |
  |   |w i d t h  t|   |
  |   #------------+   | &lt;- yBodyBottom()
  |   ^                |
  | Body               |
  | Offset             |
  #--------------------+
(0,0)
 </code></pre>

 <p>Put header/footer content wherever you want.  We move the body as a unit as needed.</p>
 */
public class LogicalPage implements RenderTarget { // AKA Document Section
    // These can be made configurable some day.  But until then, they are named.

    public enum Orientation { PORTRAIT, LANDSCAPE; }

    // Some printers need at least 1/2" of margin (36 "pixels") in order to accept a print job.
    // This amount seems to accommodate all printers.
    static final float DEFAULT_MARGIN = 37f;
//    private static final XyOffset DEFAULT_OFFSET = XyOffset.of(DEFAULT_MARGIN, DEFAULT_MARGIN);
    private static final XyDim DEFAULT_DOUBLE_MARGIN_DIM =
            XyDim.of(DEFAULT_MARGIN * 2, DEFAULT_MARGIN * 2);

    private final PdfLayoutMgr mgr;
    private final boolean portrait;
    private final PDRectangle bodyRect;
    // borderItems apply to a logical section
    private Set<PdfItem> borderItems = new TreeSet<PdfItem>();
    private int borderOrd = 0;
    private boolean valid = true;

    /**
     Private constructor
     @param m the PdfLayoutMgr this will work on.
     @param p portrait if true, landscape otherwise.
     @param bodyOff the offset (in document units) from the upper-left hand corner of the page to
     the start of the body area.
     @param bodyDim the dimensions of the body area.
     */
    private LogicalPage(PdfLayoutMgr m, boolean p, XyOffset bodyOff, XyDim bodyDim) {
        mgr = m; portrait = p;
        bodyRect = new PDRectangle(bodyOff.x(), bodyOff.y(), bodyDim.width(), bodyDim.height());
    }

    /**
     The full factory.
     @param m the PdfLayoutMgr you are using.
     @param orientation page orientation for this logical page grouping.
     @param bodyOff the offset (in document units) from the lower-left hand corner of the page to
     the lower-left of the body area.
     @param bodyDim the dimensions of the body area.
     @return a new LogicalPage with the given settings.
     */
    static LogicalPage of(PdfLayoutMgr m, Orientation orientation,
                          XyOffset bodyOff, XyDim bodyDim) {
        return new LogicalPage(m, orientation == Orientation.PORTRAIT, bodyOff, bodyDim);
    }

    /**
     Create a LogicalPage with default margins for body top and bottom.
     @param m the PdfLayoutMgr you are using.
     @param orientation page orientation for this logical page grouping.
     @return a new LogicalPage with the given settings.
     */
    static LogicalPage of(PdfLayoutMgr m, Orientation orientation) {

        return of(m, orientation, XyOffset.of(DEFAULT_MARGIN, DEFAULT_MARGIN),
                  (orientation == Orientation.PORTRAIT)
                  ? m.pageDim().minus(DEFAULT_DOUBLE_MARGIN_DIM)
                  : m.pageDim().swapWh().minus(DEFAULT_DOUBLE_MARGIN_DIM));
    }

    // ===================================== Instance Methods =====================================

    /** The Y-value for top of the body section (in document units) */
    public float yBodyTop() {
        return bodyRect.getUpperRightY();
    }

    /**
     The Y-value for the bottom of the body section (in document units).  The bottom of the page is
     always zero, so this is always equivalent to the margin body bottom.
     */
    public float yBodyBottom() { return bodyRect.getLowerLeftY(); }

    /** Height (dimension, not offset) of the body section (in document units) */
    @SuppressWarnings("WeakerAccess") // part of public interface
    public float bodyHeight() { return bodyRect.getHeight(); }

    /**
     Width of the entire page (in document units).  This is the short dimension for portrait,
     the long dimension for landscape.
     */
    public float pageWidth() {
        return portrait ? mgr.pageDim().width()
                        : mgr.pageDim().height();
    }

    /** The orientation of this logical page grouping */
    public Orientation orientation() {
        return portrait ? Orientation.PORTRAIT : Orientation.LANDSCAPE;
    }

    public TableBuilder tableBuilder(XyOffset tl) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        return TableBuilder.of(this, tl);
    }

    /** Ends this logical page grouping and invalidates it for further operations. */
    public PdfLayoutMgr commit() throws IOException {
        mgr.logicalPageEnd(this);
        valid = false;
        return mgr;
    }

    /** {@inheritDoc} */
    @Override public LogicalPage drawStyledText(float x, float y, String s, TextStyle textStyle) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        PageBufferAndY pby = mgr.appropriatePage(this, y);
        pby.pb.drawStyledText(x, pby.y, s, textStyle);
        return this;
    }

    /** {@inheritDoc} */
    @Override public LogicalPage drawJpeg(float x, float y, ScaledJpeg sj) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Calculate what page image should start on
        PageBufferAndY pby = mgr.appropriatePage(this, y);
        // draw image based on baseline and decrement y appropriately for image.
        pby.pb.drawJpeg(x, pby.y, sj);
        return this;
    }

    /** {@inheritDoc} */
    @Override public LogicalPage drawPng(float x, float y, ScaledPng sj) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Calculate what page image should start on
        PageBufferAndY pby = mgr.appropriatePage(this, y);
        // draw image based on baseline and decrement y appropriately for image.
        pby.pb.drawPng(x, pby.y, sj);
        return this;
    }

    /** {@inheritDoc} */
    @Override public LogicalPage fillRect(XyOffset outerTopLeft, XyDim outerDimensions, Color c) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
//        System.out.println("putRect(" + outerTopLeft + " " + outerDimensions + " " +
//                           Utils.toString(c) + ")");
        final float left = outerTopLeft.x();
        final float topY = outerTopLeft.y();
        final float width = outerDimensions.width();
        final float maxHeight = outerDimensions.height();
        final float bottomY = topY - maxHeight;

        if (topY < bottomY) { throw new IllegalStateException("height must be positive"); }
        // logger.info("About to put line: (" + x1 + "," + y1 + "), (" + x2 + "," + y2 + ")");
        PageBufferAndY pby1 = mgr.appropriatePage(this, topY);
        PageBufferAndY pby2 = mgr.appropriatePage(this, bottomY);
        if (pby1.equals(pby2)) {
            pby1.pb.fillRect(left, pby1.y, width, maxHeight, c, -1);
        } else {
            final int totalPages = (pby2.pb.pageNum - pby1.pb.pageNum) + 1;

            PageBuffer currPage = pby1.pb;
            // The first x and y are correct for the first page.  The second x and y will need to
            // be adjusted below.
            float ya = topY, yb = 0;

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                if (pby1.pb.pageNum < currPage.pageNum) {
                    // On all except the first page the first y will start at the top of the page.
                    ya = yBodyTop();
                } else { // equals, because can never be greater than
                    ya = pby1.y;
                }

                if (pageNum == totalPages) {
                    // the second Y must be adjusted by the height of the pages already printed.
                    yb = pby2.y;
                } else {
                    // On all except the last page, the second-y will end at the bottom of the page.
                    yb = yBodyBottom();
                }

                currPage.fillRect(left, yb, width, ya - yb, c, -1);

                // pageNum is one-based while get is zero-based, so passing get the current
                // pageNum actually gets the next page.  Don't get another one after we already
                // processed the last page!
                if (pageNum < totalPages) {
                    currPage = mgr.pages().get(currPage.pageNum);
                }
            }
        }

        return this;
    }

    /** {@inheritDoc} */
    @Override
    public LogicalPage drawLine(float x1, float y1, float x2, float y2, final LineStyle ls) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
//        mgr.putLine(x1, y1, x2, y2, ls);

        if (y1 < y2) { throw new IllegalStateException("y1 param must be >= y2 param"); }
        // logger.info("About to put line: (" + x1 + "," + y1 + "), (" + x2 + "," + y2 + ")");
        PageBufferAndY pby1 = mgr.appropriatePage(this, y1);
        PageBufferAndY pby2 = mgr.appropriatePage(this, y2);
        if (pby1.equals(pby2)) {
            pby1.pb.drawLine(x1, pby1.y, x2, pby2.y, ls);
        } else {
            final int totalPages = (pby2.pb.pageNum - pby1.pb.pageNum) + 1;
            final float xDiff = x2 - x1;
            final float yDiff = y1 - y2;
            // totalY

            PageBuffer currPage = pby1.pb;
            // The first x and y are correct for the first page.  The second x and y will need to
            // be adjusted below.
            float xa = x1, ya = y1, xb = 0, yb = 0;

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                if (pageNum > 1) {
                    // The x-value at the start of the new page will be the same as
                    // it was on the bottom of the previous page.
                    xa = xb;
                }

                if (pby1.pb.pageNum < currPage.pageNum) {
                    // On all except the first page the first y will start at the top of the page.
                    ya = yBodyTop();
                } else { // equals, because can never be greater than
                    ya = pby1.y;
                }

                if (pageNum == totalPages) {
                    xb = x2;
                    // the second Y must be adjusted by the height of the pages already printed.
                    yb = pby2.y;
                } else {
                    // On all except the last page, the second-y will end at the bottom of the page.
                    yb = yBodyBottom();

                    // This represents the x-value of the line at the bottom of one page and later
                    // becomes the x-value for the top of the next page.  It should work whether
                    // slope is negative or positive, because the sign of xDiff will reflect the
                    // slope.
                    //
                    // x1 is the starting point.
                    // xDiff is the total deltaX over all pages so it needs to be scaled by:
                    // (ya - yb) / yDiff is the proportion of the line shown on this page.
                    xb = (xa + (xDiff * ((ya - yb)/yDiff)));
                }

                currPage.drawLine(xa, ya, xb, yb, ls);

                // pageNum is one-based while get is zero-based, so passing get the current
                // pageNum actually gets the next page.  Don't get another one after we already
                // processed the last page!
                if (pageNum < totalPages) {
                    currPage = mgr.pages().get(currPage.pageNum);
                }
            }
        }

        return this;
    }

    /**
     You can draw a cell without a table (for a heading, or paragraph of same-format text, or
     whatever).
     */
    public XyOffset drawCell(float x, float y, Cell cell) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Similar to TableBuilder and TableRowBuilder.calcDimensions().  Should be combined?
        XyDim maxDim = XyDim.ZERO;
        XyDim wh = cell.calcDimensions(cell.width());
        maxDim = XyDim.of(wh.width() + maxDim.width(), Math.max(maxDim.height(), wh.height()));
        float maxHeight = maxDim.height();

        // render the row with that maxHeight.
        cell.render(this, XyOffset.of(x, y), XyDim.of(cell.width(), maxHeight));

        return XyOffset.of(x + wh.width(), y - wh.height());
    }

    public XyOffset drawTable(TableBuilder tb) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        return tb.render(this, tb.topLeft(), null);
    }

    /**
     Shows the given cells plus either a background or an outline as appropriate.

     @param initialX the left-most x-value.
     @param origY the starting y-value
     @param cells the Cells to display
     @return the final y-value
     @throws IOException if there is an error writing to the underlying stream.
     */
    public float putRow(final float initialX, final float origY, final Cell... cells)
            throws IOException {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }

        // Similar to TableBuilder and TableRowBuilder.calcDimensions().  Should be combined?
        XyDim maxDim = XyDim.ZERO;
        for (Cell cell : cells) {
            XyDim wh = cell.calcDimensions(cell.width());
            maxDim = XyDim.of(wh.width() + maxDim.width(),
                              Math.max(maxDim.height(), wh.height()));
        }
        float maxHeight = maxDim.height();

//        System.out.println("putRow: maxHeight=" + maxHeight);

        // render the row with that maxHeight.
        float x = initialX;
        for (Cell cell : cells) {
            cell.render(this, XyOffset.of(x, origY), XyDim.of(cell.width(), maxHeight));
            x += cell.width();
        }

        return origY - maxHeight;
    }

//    /**
//     Header and footer in this case means anything that doesn't have to appear within the body
//     of the page.  Most commonly used for headers and footers, but could be watermarks, background
//     images, or anything outside the normal page flow.  I believe these get drawn first so
//     the body text will render over the top of them.  Items put here will *not* wrap to the next
//     page.
//
//     @param x the x-value on all pages (often set outside the normal margins)
//     @param origY the y-value on all pages (often set outside the normal margins)
//     @param cell the cell containing the styling and text to render
//     @return the bottom Y-value of the rendered cell (on all pages)
//     */
//    public float drawCellAsWatermark(float x, float origY, Cell cell) {
//        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
//        float outerWidth = cell.width();
//        XyDim innerDim = cell.calcDimensions(outerWidth);
//        PageBufferAndY pby = mgr.appropriatePage(this, origY);
//        return cell.render(pby.pb, XyOffset.of(x, pby.y), innerDim.width(outerWidth)).y();
//    }

    void commitBorderItems(PDPageContentStream stream) throws IOException {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (PdfItem item : borderItems) { item.commit(stream); }
    }

    /**
     Adds items to every page in page grouping.  You should not need to use this directly.  It only
     has package scope so that Text can access it for one thing.  It may become private in the
     future.
      */
    void borderStyledText(float xCoord, float yCoord, String text, TextStyle s) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        borderItems.add(new PageBuffer.Text(xCoord, yCoord, text, s, borderOrd++,
                                            PdfItem.DEFAULT_Z_INDEX));
    }

    static class PageBufferAndY {
        final PageBuffer pb;
        final float y;
        PageBufferAndY(PageBuffer p, float theY) { pb = p; y = theY; }
    }
}
