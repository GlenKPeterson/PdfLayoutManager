package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode;

/**
 Created by gpeterso on 6/5/17.
 */
public class XyDimTest {
    @Test public void testBasics() {
        XyDim xyd1 = XyDim.of(Float.MAX_VALUE, Float.MIN_VALUE);
        assertEquals(Float.MAX_VALUE, xyd1.width(), 0.00000001);
        assertEquals(Float.MIN_VALUE, xyd1.height(), 0.00000001);
        XyDim xyd2 = XyDim.of(new PDRectangle(Float.MAX_VALUE, Float.MIN_VALUE));
        assertEquals(Float.MAX_VALUE, xyd2.width(), 0.00000001);
        assertEquals(Float.MIN_VALUE, xyd2.height(), 0.00000001);
        XyDim xyd3 = XyDim.ZERO.width(Float.MAX_VALUE).height(Float.MIN_VALUE);
        assertEquals(Float.MAX_VALUE, xyd3.width(), 0.00000001);
        assertEquals(Float.MIN_VALUE, xyd3.height(), 0.00000001);

        equalsDistinctHashCode(xyd1, xyd2, xyd3, XyDim.ZERO);

        equalsDistinctHashCode(XyDim.of(5f, 3f).swapWh(),
                               XyDim.of(4f, 6f).minus(XyDim.of(1f, 1f)),
                               XyDim.of(2f, 4f).plus(XyDim.of(1f, 1f)),
                               XyDim.of(3.1f, 4.9f));

        assertEquals(XyDim.of(7f, 11f), XyDim.of(XyDim.of(7f, 11f).toRect()));

        assertTrue(XyDim.of(5f, 11f).lte(XyDim.of(5f, 11f)));
        assertTrue(XyDim.of(5f, 11f).lte(XyDim.of(5.000001f, 11.000001f)));
        assertFalse(XyDim.of(5f, 11f).lte(XyDim.of(4.999999f, 11f)));
        assertFalse(XyDim.of(5f, 11f).lte(XyDim.of(5f, 10.999999f)));
    }

    @Test(expected = IllegalArgumentException.class) public void testEx1() {
        XyDim.of(3.5f, -1f);
    }

    @Test(expected = IllegalArgumentException.class) public void testEx2() {
        XyDim.of(-3.5f, 1f);
    }
}