// Copyright 2013-03-04 PlanBase Inc. & Glen Peterson
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

import java.awt.image.BufferedImage;

/**
 Represents a Jpeg image and the document units it should be scaled to.  When a ScaledJpeg is added
 to a PdfLayoutMgr, its underlying bufferedImage is compared to the images already embedded in that
 PDF file.  If an equivalent bufferedImage object is found, the underlying image is not added to
 the document twice.  Only the additional position and scaling of that image is added.  This
 significantly decreases the file size of the resulting PDF when images are reused within that
 document.
 */
public class ScaledJpeg implements Renderable {
    public static final double ASSUMED_IMAGE_DPI = 300;
    public static final double IMAGE_SCALE = 1 / ASSUMED_IMAGE_DPI * PdfLayoutMgr.DOC_UNITS_PER_INCH;

    private final BufferedImage bufferedImage;
    private final double width;
    private final double height;

    private ScaledJpeg(BufferedImage bi, double w, double h) {
        if (w <= 0) { w = bi.getWidth() * IMAGE_SCALE; }
        if (h <= 0) { h = bi.getHeight() * IMAGE_SCALE; }
        bufferedImage = bi; width = w; height = h;
    }

    /**
     Lets you specify the document units for how you want your image displayed.

     @param bi the source BufferedImage
     @param w the width in document units
     @param h the width in document units
     @return a ScaledJpeg with the given width and height for that image.
     */
    public static ScaledJpeg of(BufferedImage bi, double w, double h) {
        return new ScaledJpeg(bi, w, h);
    }

    /**
     Returns a new buffered image with width and height calculated from the source BufferedImage
     assuming that it will print at 300 DPI.  There are 72 document units per inch, so the actual
     formula is: bi.width / 300 * 72
     @param bi the source BufferedImage
     @return a ScaledJpeg holding the width and height for that image.
     */
    public static ScaledJpeg of(BufferedImage bi) { return new ScaledJpeg(bi, 0, 0); }

    /** @return the underlying buffered image */
    public BufferedImage bufferedImage() { return bufferedImage; }

    public Dim dimensions() { return Dim.of(width, height); }

    public Dim calcDimensions(double maxWidth) { return dimensions(); }

    public Coord render(LogicalPage lp, Coord outerTopLeft, Dim outerDimensions, boolean allPages) {
        // use bottom of image for page-breaking calculation.
        double y = outerTopLeft.getY() - height;
        lp.drawJpeg(outerTopLeft.getX(), y, this);
        return Coord.of(outerTopLeft.getX() + width, y);
    }
}
