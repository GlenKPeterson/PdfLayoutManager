package com.planbase.pdf.layoutmanager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.organicdesign.testUtils.EqualsContract.equalsDistinctHashCode;
import static org.organicdesign.testUtils.EqualsContract.equalsSameHashCode;

public class PaddingTest {
    @Test public void staticFactoryTest() {
        assertTrue(Padding.NO_PADDING == Padding.of(0));
        assertTrue(Padding.NO_PADDING == Padding.of(0,0,0,0));
        assertTrue(Padding.DEFAULT_TEXT_PADDING == Padding.of(1.5, 1.5, 2, 1.5));

        Padding p2 = Padding.of(2);
        assertEquals(2.0, p2.top(), 0.0);
        assertEquals(2.0, p2.right(), 0.0);
        assertEquals(2.0, p2.bottom(), 0.0);
        assertEquals(2.0, p2.left(), 0.0);

        Padding pPrime = Padding.of(3, 5, 7, 11);
        assertEquals(3.0, pPrime.top(), 0.0);
        assertEquals(5.0, pPrime.right(), 0.0);
        assertEquals(7.0, pPrime.bottom(), 0.0);
        assertEquals(11.0, pPrime.left(), 0.0);
    }

    @Test public void equalHashTest() {
        // Test first item different
        equalsDistinctHashCode(Padding.of(1), Padding.of(1,1,1,1), Padding.of(1),
                               Padding.of(2,1,1,1));

        // Test transposed middle items are different (but have same hashcode)
        equalsSameHashCode(Padding.of(3, 5, 7, 1.1), Padding.of(3, 5, 7, 1.1),
                           Padding.of(3, 5, 7, 1.1),
                           Padding.of(3, 7, 5, 1.1));

        // Padding values that differ by less than 0.1 have the same hashcode
        // but are not equal.  Prove it (also tests last item is different):
        equalsSameHashCode(Padding.of(1), Padding.of(1, 1, 1, 1), Padding.of(1),
                           Padding.of(1, 1, 1, 1.0001));
    }

}