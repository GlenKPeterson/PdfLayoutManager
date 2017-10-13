// Copyright 2017 PlanBase Inc.
//
// This file is part of PdfLayoutMgr
//
// PdfLayoutMgr is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// PdfLayoutMgr is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with PdfLayoutMgr.  If not, see <https://www.gnu.org/licenses/agpl-3.0.en.html>.
//
// If you wish to use this code with proprietary software,
// contact PlanBase Inc. <https://planbase.com> to purchase a commercial license.

package com.planbase.pdf.layoutmanager

import com.planbase.pdf.layoutmanager.ScaledJpeg.Companion.IMAGE_SCALE
import java.awt.image.BufferedImage

/**
 Represents a PNG image and the document units it should be scaled to.  When a ScaledPng is added
 to a PdfLayoutMgr, its underlying bufferedImage is compared to the images already embedded in that
 PDF file.  If an equivalent bufferedImage object is found, the underlying image is not added to
 the document twice.  Only the additional position and scaling of that image is added.  This
 significantly decreases the file size of the resulting PDF when images are reused within that
 document.

 Constructor lets you specify the document units for how you want your image displayed.

 @param bufferedImage the source BufferedImage
 @param width the width in document units
 @param height the width in document units
 @return a ScaledPng with the given width and height for that image.
 */
class ScaledPng(val bufferedImage: BufferedImage,
                private val width: Float,
                private val height: Float) : FixedItem, Renderable {

    /**
     Returns a new buffered image with width and height calculated from the source BufferedImage
     assuming that it will print at 300 DPI.  There are 72 document units per inch, so the actual
     formula is: bi.width / 300 * 72
     @param bufferedImage the source BufferedImage
     @return a ScaledPng holding the width and height for that image.
     */
    constructor(bufferedImage:BufferedImage) :
            this(bufferedImage, bufferedImage.width * IMAGE_SCALE,
                 bufferedImage.height * IMAGE_SCALE)

    init {
        if (width <= 0) {
            throw IllegalArgumentException("Width must be > 0.")
        }
        if (height <= 0) {
            throw IllegalArgumentException("Height must be > 0.")
        }
    }

    override val xyDim: XyDim = XyDim(width, height)

    override val ascent: Float = height

    override val descentAndLeading: Float = 0f

    override val lineHeight: Float = height

    /** {@inheritDoc}  */
    override fun render(lp: RenderTarget, outerTopLeft: XyOffset): XyOffset {
        // use bottom of image for page-breaking calculation.
        var y = outerTopLeft.y - height
        y = lp.drawPng(outerTopLeft.x, y, this)
        return XyOffset(outerTopLeft.x + width, y)
    }

    override fun renderator(): Renderator = fixedItemRenderator(this)
}
