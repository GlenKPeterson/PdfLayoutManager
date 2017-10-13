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

import org.apache.pdfbox.pdmodel.common.PDRectangle

/**
 * Represents a 2D coordinate in terms of X and Y where negative y is down from the upper left-hand
 * corner.  Do not confuse this with an XyDim which represents positive width and height.
 */
data class XyOffset(val x: Float, val y: Float) {

    constructor(rect: PDRectangle) : this(rect.lowerLeftX, rect.lowerLeftY)

    fun x(newX: Float) = XyOffset(newX, y)

    fun y(newY: Float) = XyOffset(x, newY)

    //    public XyOffset minus(XyOffset that) { return of(this.x - that.x(), this.y - that.y()); }
    //    public XyOffset plus(XyOffset that) { return of(this.x + that.x(), this.y + that.y()); }

    fun plusXMinusY(that: XyOffset) = XyOffset(x + that.x, y - that.y)

    //    public XyOffset maxXandY(XyOffset that) {
    //        if ((this.x >= that.x()) && (this.y >= that.y())) { return this; }
    //        if ((this.x <= that.x()) && (this.y <= that.y())) { return that; }
    //        return of((this.x > that.x()) ? this.x : that.x(),
    //                  (this.y > that.y()) ? this.y : that.y());
    //    }
    fun maxXMinY(that: XyOffset): XyOffset =
            if (this.x >= that.x && this.y <= that.y) {
                this
            } else if (this.x <= that.x && this.y >= that.y) {
                that
            } else {
                XyOffset(if (this.x > that.x) this.x else that.x,
                         if (this.y < that.y) this.y else that.y)
            }

    /** Compares dimensions  */
    fun lte(that: XyOffset): Boolean = this.x <= that.x && this.y >= that.y

    companion object {
        val ORIGIN = XyOffset(0f, 0f)
    }
}
