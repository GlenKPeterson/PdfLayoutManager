package com.planbase.pdf.layoutmanager;

/** Renderable Iterator */
public interface Renderator {

    class SingleItemRenderator implements Renderator {
//        private final float width;
        private Renderable item;

        SingleItemRenderator(Renderable r) { item = r; }

        @Override public boolean hasMore() { return item != null; }

        @Override public ContTerm<FixedItem> getSomething(float maxWidth) {
            XyDim dim = item.calcDimensions(maxWidth);
            FixedItem ret = new FixedItemImpl(item, dim.width(), dim.height(), 0f, dim.height());
            item = null;
            return ContTerm.Companion.continuing(ret);
        }

        @Override public ContTermNone getIfFits(float remainingWidth) {
            XyDim dim = item.calcDimensions(remainingWidth);
            if (dim.width() <= remainingWidth) {
                return getSomething(remainingWidth).match(c -> ContTermNone.Companion.continuing(c),
                                                          t -> ContTermNone.Companion.terminal(t));
            }
            return ContTermNone.Companion.none();
        }
    }

    boolean hasMore();

    /**
     Called when line is empty.  Returns something less than maxWidth if possible, but always
     returns something even if it won’t fit.  Call this when line is empty.
     */
    ContTerm<FixedItem> getSomething(float maxWidth);

    /**
     Called when line is not empty to try to fit on this line.  If it doesn’t fit, then the
     caller will probably create a new line and call getSomething(maxWidth) to start that line.
     */
    ContTermNone getIfFits(float remainingWidth);
}
