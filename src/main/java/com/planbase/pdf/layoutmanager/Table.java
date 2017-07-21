package com.planbase.pdf.layoutmanager;

import java.util.List;

/**

 It used to be that you'd build a table and that act would commit it to a logical page.
 */
public class Table implements Renderable {
    private final List<TablePart> parts;

    Table(List<TablePart> ps) { parts = ps; }

    @Override  public XyDim calcDimensions(float maxWidth) {
        XyDim maxDim = XyDim.ZERO;
        for (TablePart part : parts) {
            XyDim wh = part.calcDimensions();
            maxDim = XyDim.of(Math.max(wh.width(), maxDim.width()),
                              maxDim.height() + wh.height());
        }
        return maxDim;
    }

    /*
    Renders item and all child-items with given width and returns the x-y pair of the
    lower-right-hand corner of the last line (e.g. of text).
    */
    @Override public XyOffset render(RenderTarget lp, XyOffset outerTopLeft,
                                     XyDim outerDimensions) {
        XyOffset rightmostLowest = outerTopLeft;
        for (TablePart part : parts) {
//            System.out.println("About to render part: " + part);
            XyOffset rl = part.render(lp, XyOffset.of(outerTopLeft.x(), rightmostLowest.y()));
            rightmostLowest = XyOffset.of(Math.max(rl.x(), rightmostLowest.x()),
                                          Math.min(rl.y(), rightmostLowest.y()));
        }
        return rightmostLowest;
    }
}
