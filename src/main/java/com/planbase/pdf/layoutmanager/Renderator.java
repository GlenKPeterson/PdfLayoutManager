package com.planbase.pdf.layoutmanager;

import org.organicdesign.fp.oneOf.Option;

/** Renderable Iterator */
public interface Renderator {

    class SingleItemRenderator implements Renderator {
//        private final float width;
        private Renderable item;

        SingleItemRenderator(Renderable r) { item = r; }

        @Override public boolean hasMore() { return item != null; }

        @Override public FixedItem getSomething(float maxWidth) {
            XyDim dim = item.calcDimensions(maxWidth);
            FixedItem ret = new FixedItemImpl(item, dim.width(), dim.height(), 0f, dim.height());
            item = null;
            return ret;
        }

        @Override public Option<FixedItem> getIfFits(float remainingWidth) {
            XyDim dim = item.calcDimensions(remainingWidth);
            if (dim.width() <= remainingWidth) {
                return Option.some(getSomething(remainingWidth));
            }
            return Option.none();
        }
    }

    boolean hasMore();

    // Called when line is empty.  Returns something less than maxWidth if possible, but always returns something even if it won’t fit.  Call this when line is empty.
    FixedItem getSomething(float maxWidth);

    // Called when line is not empty to try to fit on this line.  If it doesn’t fit, then the caller will probably create a new line and call getSomething(maxWidth) to start that line.
    Option<FixedItem> getIfFits(float remainingWidth);
}
