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

import org.apache.pdfbox.pdmodel.PDPageContentStream

import java.io.IOException

/**
 * Represents items to be later drawn to the page of a PDF file.  The z-index allows items to be drawn
 * from back (lower-z-values) to front (higher-z-values).  When the z-index of two items is the same
 * they will be drawn in the order they were created.  Implementing classes should give PdfItems
 * ascending serialNumbers as they are created by calling super(num, z);  PdfItems are comparable
 * and their natural ordering is the same order as they will be drawn: ascending by z-index,
 * then by creation order.  The default z-index is zero.
 */
abstract class PdfItem(private val serialNumber: Long, val z: Float) : Comparable<PdfItem> {
    //    public static PdfItem of(final long ord, final float zIndex) {
    //        return new PdfItem(ord, zIndex);
    //    }

    @Throws(IOException::class)
    abstract fun commit(stream: PDPageContentStream)

    // @Override
    override fun compareTo(other: PdfItem): Int {
        // Ascending by Z (draw the lower-order background items first)
        val zDiff = this.z - other.z
        if (zDiff > 0) {
            return 1
        } else if (zDiff < 0) {
            return -1
        }
        // Ascending by creation order
        val oDiff = this.serialNumber - other.serialNumber
        if (oDiff > 0) {
            return 1
        } else if (oDiff < 0) {
            return -1
        }
        return 0
    }

    override fun equals(other: Any?): Boolean {
        // Cheapest operation first...
        if (this === other) {
            return true
        }
        // Return false if can't be equal
        if (other == null ||
            other !is PdfItem ||
            this.hashCode() != other.hashCode()) {
            return false
        }
        // Details...
        val that = other as PdfItem?
        return compareTo(that!!) == 0
    }

    override fun hashCode(): Int {
        return (z * 1000).toInt() + serialNumber.toInt()
    }

    companion object {
        val DEFAULT_Z_INDEX = 0f
    }
}
