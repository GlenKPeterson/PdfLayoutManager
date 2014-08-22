package com.planbase.pdf.layoutmanager;

import java.awt.Color;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;

public class LogicalPage { // AKA Document Section
    private PdfLayoutMgr mgr;
    // borderItems apply to a logical section
    private Set<PdfItem> borderItems = new TreeSet<PdfItem>();
    private int borderOrd = 0;
    boolean valid = true;

    LogicalPage(PdfLayoutMgr m) { mgr = m;}

    public static LogicalPage of(PdfLayoutMgr m) { return new LogicalPage(m); }

    public TableBuilder tableBuilder(XyOffset tl) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        return TableBuilder.of(this, tl);
    }

    public PdfLayoutMgr commit() throws IOException {
        mgr.logicalPageEnd(this);
        valid = false;
        return mgr;
    }

    LogicalPage drawStyledText(float x, float y, String s, TextStyle textStyle) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        PdfLayoutMgr.PageBufferAndY pby = mgr.appropriatePage(y);
        pby.pb.drawStyledText(x, pby.y, s, textStyle);
        return this;
    }

    LogicalPage drawJpeg(final float xVal, final float yVal, final ScaledJpeg sj) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        // Calculate what page image should start on
        PdfLayoutMgr.PageBufferAndY pby = mgr.appropriatePage(yVal);
        // draw image based on baseline and decrement y appropriately for image.
        pby.pb.drawJpeg(xVal, pby.y, sj, mgr);
        return this;
    }

    public LogicalPage putRect(XyOffset outerTopLeft, XyDim outerDimensions, final Color c) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
//        System.out.println("putRect(" + outerTopLeft + " " + outerDimensions + " " +
//                           Utils.toString(c) + ")");
        mgr.putRect(outerTopLeft.x(), outerTopLeft.y(), outerDimensions.x(), outerDimensions.y(), c);
        return this;
    }

    public LogicalPage putLine(final float x1, final float y1, final float x2, final float y2, final LineStyle ls) {
        if (!valid) { throw new IllegalStateException("Logical page accessed after commit"); }
        mgr.putLine(x1, y1, x2, y2, ls);
        return this;
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
                              Float.max(maxDim.y(), wh.y()));
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
}
