package com.planbase.pdf.layoutmanager;

import org.junit.Test;
import org.organicdesign.fp.oneOf.Option;

import static org.junit.Assert.*;

public class SingleItemRenderatorTest {
    @Test public void testSingleItemRenderator() {
        Renderable r = new Renderable() {
            @Override public XyDim calcDimensions(float maxWidth) { return XyDim.of(5f, 7f); }

            @Override public XyOffset render(RenderTarget lp, XyOffset outerTopLeft, XyDim outerDimensions) {
                return null;
            }

            @Override public Renderator renderator() {
                return new Renderator.SingleItemRenderator(this);
            }
        };
        Renderator tor = r.renderator();
        assertTrue(tor.hasMore());
        assertEquals(r.calcDimensions(0), tor.getSomething(9f).xyDim());
        assertFalse(tor.hasMore());

        tor = r.renderator();
        assertTrue(tor.hasMore());
        assertEquals(Option.NONE, tor.getIfFits(3f));
        assertTrue(tor.hasMore());
        assertEquals(r.calcDimensions(0), tor.getIfFits(9f).get().xyDim());
        assertFalse(tor.hasMore());
    }
}