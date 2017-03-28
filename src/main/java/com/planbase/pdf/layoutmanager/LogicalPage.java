package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Maybe better called a "DocumentSection" this represents a group of Renderables that logically belong on the same
 * page, but may spill over multiple subsequent pages as necessary in order to fit.  Headers and footers are tied to
 * this Logical Page / Document Section.
 */
public class LogicalPage { // AKA Document Section
    public enum Orientation { PORTRAIT, LANDSCAPE; }

    private final PdfLayoutMgr mgr;
    private final boolean portrait;
    // borderItems apply to a logical section
    private Set<PdfItem> borderItems = new TreeSet<PdfItem>();
    private int borderOrd = 0;
    boolean valid = true;
    
    private float topMargin = 37;
    private float bottomMargin = 0;
    
    /** The Y-value for the top margin of the page (in document units) */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public float yPageTop() { return return mgr.getMediaBox().getHeight() - topMargin; }
    /** The Y-value for the bottom margin of the page (in document units) */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public float yPageBottom() { return portrait ? bottomMargin : 230; }

    /** Height of the printable area (in document units) */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public float printAreaHeight() { return yPageTop() - yPageBottom(); }
    /** Width of the printable area (in document units) */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public float pageWidth() {
        return portrait ? mgr.getMediaBox().getWidth()
                : mgr.getMediaBox().getHeight();
    }

    private LogicalPage(PdfLayoutMgr m, boolean p) { mgr = m; portrait = p; }

    public static LogicalPage of(PdfLayoutMgr m) { return new LogicalPage(m, false); }
    public static LogicalPage of(PdfLayoutMgr m, Orientation orientation) {
        return new LogicalPage(m, orientation == Orientation.PORTRAIT);
    }

    /** The orientation of this logical page grouping */
    public Orientation orientation() { return portrait ? Orientation.PORTRAIT : Orientation.LANDSCAPE; }

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

    LogicalPage drawStyledText(float x, float y, String s, TextStyle textStyle) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        PageBufferAndY pby = mgr.appropriatePage(this, y);
        pby.pb.drawStyledText(x, pby.y, s, textStyle);
        return this;
    }

    LogicalPage drawJpeg(final float xVal, final float yVal, final ScaledJpeg sj) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Calculate what page image should start on
        PageBufferAndY pby = mgr.appropriatePage(this, yVal);
        // draw image based on baseline and decrement y appropriately for image.
        pby.pb.drawJpeg(xVal, pby.y, sj, mgr);
        return this;
    }

    LogicalPage drawPng(final float xVal, final float yVal, final ScaledPng sj) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Calculate what page image should start on
        PageBufferAndY pby = mgr.appropriatePage(this, yVal);
        // draw image based on baseline and decrement y appropriately for image.
        pby.pb.drawPng(xVal, pby.y, sj, mgr);
        return this;
    }

    public LogicalPage putRect(XyOffset outerTopLeft, XyDim outerDimensions, final Color c) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
//        System.out.println("putRect(" + outerTopLeft + " " + outerDimensions + " " +
//                           Utils.toString(c) + ")");
        final float left = outerTopLeft.x();
        final float topY = outerTopLeft.y();
        final float width = outerDimensions.x();
        final float maxHeight = outerDimensions.y();
        final float bottomY = topY - maxHeight;

        if (topY < bottomY) { throw new IllegalStateException("height must be positive"); }
        // logger.info("About to put line: (" + x1 + "," + y1 + "), (" + x2 + "," + y2 + ")");
        PageBufferAndY pby1 = mgr.appropriatePage(this, topY);
        PageBufferAndY pby2 = mgr.appropriatePage(this, bottomY);
        if (pby1.equals(pby2)) {
            pby1.pb.fillRect(left, pby1.y, width, maxHeight, c, -1);
        } else {
            final int totalPages = (pby2.pb.pageNum - pby1.pb.pageNum) + 1;

            PdfLayoutMgr.PageBuffer currPage = pby1.pb;
            // The first x and y are correct for the first page.  The second x and y will need to
            // be adjusted below.
            float ya = topY, yb = 0;

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                if (pby1.pb.pageNum < currPage.pageNum) {
                    // On all except the first page the first y will start at the top of the page.
                    ya = yPageTop();
                } else { // equals, because can never be greater than
                    ya = pby1.y;
                }

                if (pageNum == totalPages) {
                    // the second Y must be adjusted by the height of the pages already printed.
                    yb = pby2.y;
                } else {
                    // On all except the last page, the second-y will end at the bottom of the page.
                    yb = yPageBottom();
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

    /**
     Must draw from higher to lower.  Thus y1 must be &gt;= y2 (remember, higher y values
     are up).
     @param x1 first x-value
     @param y1 first (upper) y-value
     @param x2 second x-value
     @param y2 second (lower or same) y-value
     */
    public LogicalPage putLine(final float x1, final float y1, final float x2, final float y2, final LineStyle ls) {
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

            PdfLayoutMgr.PageBuffer currPage = pby1.pb;
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
                    ya = yPageTop();
                } else { // equals, because can never be greater than
                    ya = pby1.y;
                }

                if (pageNum == totalPages) {
                    xb = x2;
                    // the second Y must be adjusted by the height of the pages already printed.
                    yb = pby2.y;
                } else {
                    // On all except the last page, the second-y will end at the bottom of the page.
                    yb = yPageBottom();

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

    /** You can draw a cell without a table (for a heading, or paragraph of same-format text, or whatever). */
    public XyOffset putCell(final float topLeftX, final float topLeftY, Cell cell) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Similar to TableBuilder and TableRowBuilder.calcDimensions().  Should be combined?
        XyDim maxDim = XyDim.ZERO;
        XyDim wh = cell.calcDimensions(cell.width());
        maxDim = XyDim.of(wh.x() + maxDim.x(), Math.max(maxDim.y(), wh.y()));
        float maxHeight = maxDim.y();

        // render the row with that maxHeight.
        cell.render(this, XyOffset.of(topLeftX, topLeftY), XyDim.of(cell.width(), maxHeight), false);

        return XyOffset.of(topLeftX + wh.x(), topLeftY - wh.y());
    }


    public XyOffset addTable(TableBuilder tb) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        return tb.render(this, tb.topLeft(), null, false);
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
            maxDim = XyDim.of(wh.x() + maxDim.x(),
                              Math.max(maxDim.y(), wh.y()));
        }
        float maxHeight = maxDim.y();

//        System.out.println("putRow: maxHeight=" + maxHeight);

        // render the row with that maxHeight.
        float x = initialX;
        for (Cell cell : cells) {
            cell.render(this, XyOffset.of(x, origY), XyDim.of(cell.width(), maxHeight), false);
            x += cell.width();
        }

        return origY - maxHeight;
    }

    /**
     For header or footer text on all pages in this logical page grouping.
     @param x the x-value on all pages.
     @param origY the y-value on all pages (probably outside the normal margins)
     @param cell the cell containing the styling and text to render.
     @return the bottom Y-value of the rendered cell (on all pages).
     */
    @SuppressWarnings("UnusedDeclaration") // Part of end-user public interface
    public float putCellAsHeaderFooter(final float x, float origY, final Cell cell) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        float outerWidth = cell.width();
        XyDim innerDim = cell.calcDimensions(outerWidth);
        return cell.render(this, XyOffset.of(x, origY), innerDim.x(outerWidth), true).y();
    }
    
    public void setTopMargin(float topMargin) {
		this.topMargin = topMargin;
	}
    
	public void setBottomMargin(float bottomMargin) {
		this.bottomMargin = bottomMargin;
	}

    void commitBorderItems(PDPageContentStream stream) throws IOException {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Since items are z-ordered, then sub-ordered by entry-order, we will draw
        // everything in the correct order.
        for (PdfItem item : borderItems) { item.commit(stream); }
    }

    private void borderStyledText(final float xCoord, final float yCoord, final String text,
                               TextStyle s, final float z) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        borderItems.add(PdfLayoutMgr.PageBuffer.Text.of(xCoord, yCoord, text, s, borderOrd++, z));
    }

    /**
     Adds items to every page in page grouping.  You should not need to use this directly.  It only
     has package scope so that Cell can access it for one thing.  It may become private in the
     future.
      */
    void borderStyledText(final float xCoord, final float yCoord, final String text,
                               TextStyle s) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        borderStyledText(xCoord, yCoord, text, s, PdfItem.DEFAULT_Z_INDEX);
    }

    static class PageBufferAndY {
        public final PdfLayoutMgr.PageBuffer pb;
        public final float y;
        public PageBufferAndY(PdfLayoutMgr.PageBuffer p, float theY) { pb = p; y = theY; }
    }
}
