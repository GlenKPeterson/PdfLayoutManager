// Copyright 2012-01-10 PlanBase Inc. & Glen Peterson
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.PDPageContentStream;

import java.io.IOException;

/**
Represents items to be later drawn to the page of a PDF file.  The z-index allows items to be drawn
 from back (lower-z-values) to front (higher-z-values).  When the z-index of two items is the same
 they will be drawn in the order they were created.  Implementing classes should give PdfItems
 ascending serialNumbers as they are created by calling super(num, z);  PdfItems are comparable
 and their natural ordering is the same order as they will be drawn: ascending by z-index,
 then by creation order.  The default z-index is zero.
 */
public abstract class PdfItem implements Comparable<PdfItem> {

    public static final double DEFAULT_Z_INDEX = 0;

    private final long serialNumber;
    private final double z;

    PdfItem(final long ord, final double zIndex) { z = zIndex; serialNumber = ord; }
//    public static PdfItem of(final long ord, final double zIndex) {
//        return new PdfItem(ord, zIndex);
//    }

    public abstract void commit(PDPageContentStream stream) throws IOException;

    // @Override
    public int compareTo(PdfItem that) {
        // Ascending by Z (draw the lower-order background items first)
        double zDiff = this.z - that.z;
        if (zDiff > 0) { return 1; } else if (zDiff < 0) { return -1; }
        // Ascending by creation order
        long oDiff = this.serialNumber - that.serialNumber;
        if (oDiff > 0) { return 1; } else if (oDiff < 0) { return -1; }
        return 0;
    }

    @Override
    public boolean equals(Object other) {
        // Cheapest operation first...
        if (this == other) { return true; }
        // Return false if can't be equal
        if ( (other == null) ||
             !(other instanceof PdfItem) ||
             (this.hashCode() != other.hashCode()) ) {
            return false;
        }
        // Details...
        final PdfItem that = (PdfItem) other;
        return compareTo(that) == 0;
    }

    @Override
    public int hashCode() {
        return ((int) (z * 1000)) + (int) serialNumber;
    }
}
