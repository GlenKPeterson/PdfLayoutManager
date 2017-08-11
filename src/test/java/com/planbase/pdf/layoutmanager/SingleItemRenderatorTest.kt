package com.planbase.pdf.layoutmanager

import org.junit.Test

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SingleItemRenderatorTest {
    @Test fun testSingleItemRenderator() {
        val r = object : Renderable {
            override fun calcDimensions(maxWidth: Float): XyDim {
                return XyDim.of(5f, 7f)
            }

            override fun render(lp: RenderTarget, outerTopLeft: XyOffset, outerDimensions: XyDim): XyOffset? {
                return null
            }

            override fun renderator(): Renderator {
                return Renderator.SingleItemRenderator(this)
            }
        }

        var tor = r.renderator()
        assertTrue(tor.hasMore())
        assertEquals(r.calcDimensions(0f), tor.getSomething(9f).match({it}, {it}).xyDim())
        assertFalse(tor.hasMore())

        tor = r.renderator()
        assertTrue(tor.hasMore())
        assertTrue(tor.getIfFits(3f).isNone)
        assertTrue(tor.hasMore())
        assertEquals(r.calcDimensions(0f), tor.getIfFits(9f).match({ c -> c }, { t -> t }, null).xyDim())
        assertFalse(tor.hasMore())
    }
}