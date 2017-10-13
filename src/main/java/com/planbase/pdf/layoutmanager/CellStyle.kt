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

import org.apache.pdfbox.pdmodel.graphics.color.PDColor

/**
 * The style of a cell including: horizontal alignment, padding, background color, and border style.
 * Immutable.
 */
data class CellStyle(val align: Align,
                     val padding: Padding?,
                     val bgColor: PDColor?,
                     val borderStyle: BorderStyle?) // Only for cell-style
                                   // I think it's OK if this is null.
{

    //    /** Horizontal allignment options for cell contents */
    //    public enum HorizAlign { LEFT, CENTER, RIGHT; }
    //    public enum VertAlign { TOP, MIDDLE, BOTTOM; }

    /** Horizontal and vertical alignment options for cell contents  */
    enum class Align {
        TOP_LEFT {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                return if (outer.lte(inner)) {
                    Padding.NO_PADDING
                } else Padding(0f, outer.width - inner.width, outer.height - inner.height, 0f)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return 0f
            }
        },
        TOP_CENTER {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                if (outer.lte(inner)) {
                    return Padding.NO_PADDING
                }
                val dx = (outer.width - inner.width) / 2
                return Padding(0f, dx, outer.height - inner.height, dx)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return if (innerWidth >= outerWidth) 0f else (outerWidth - innerWidth) / 2
            }
        },
        TOP_RIGHT {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                return if (outer.lte(inner)) {
                    Padding.NO_PADDING
                } else Padding(0f, 0f, outer.height - inner.height, outer.width - inner.width)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return if (innerWidth >= outerWidth) 0f else outerWidth - innerWidth
            }
        },
        MIDDLE_LEFT {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                if (outer.lte(inner)) {
                    return Padding.NO_PADDING
                }
                val dy = (outer.height - inner.height) / 2
                return Padding(dy, outer.width - inner.width, dy, 0f)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return 0f
            }
        },
        MIDDLE_CENTER {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                if (outer.lte(inner)) {
                    return Padding.NO_PADDING
                }
                val dx = (outer.width - inner.width) / 2
                val dy = (outer.height - inner.height) / 2
                return Padding(dy, dx, dy, dx)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return if (innerWidth >= outerWidth) 0f else (outerWidth - innerWidth) / 2
            }
        },
        MIDDLE_RIGHT {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                if (outer.lte(inner)) {
                    return Padding.NO_PADDING
                }
                val dy = (outer.height - inner.height) / 2
                return Padding(dy, 0f, dy, outer.width - inner.width)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return if (innerWidth >= outerWidth) 0f else outerWidth - innerWidth
            }
        },
        BOTTOM_LEFT {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                return if (outer.lte(inner)) {
                    Padding.NO_PADDING
                } else Padding(outer.height - inner.height, outer.width - inner.width, 0f, 0f)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return 0f
            }
        },
        BOTTOM_CENTER {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                //                System.out.println("\t\t\tcalcPadding(o=" + outer + " i=" + inner + ")");
                if (outer.lte(inner)) {
                    return Padding.NO_PADDING
                }
                val dx = (outer.width - inner.width) / 2
                //                System.out.println("\t\t\tcalcPadding() dx=" + dx);
                // Like HTML it's top, right, bottom, left
                //                System.out.println("\t\t\tcalcPadding() outer.y() - inner.y()=" + (outer.y() - inner.y()));
                return Padding(outer.height - inner.height, dx, 0f, dx)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return if (innerWidth >= outerWidth) 0f else (outerWidth - innerWidth) / 2
            }
        },
        BOTTOM_RIGHT {
            override fun calcPadding(outer: XyDim, inner: XyDim): Padding {
                return if (outer.lte(inner)) {
                    return Padding.NO_PADDING
                } else Padding(outer.height - inner.height, 0f, 0f, outer.width - inner.width)
            }

            override fun leftOffset(outerWidth: Float, innerWidth: Float): Float {
                return if (innerWidth >= outerWidth) 0f else outerWidth - innerWidth
            }
        };

        /*
        Given outer dimensions (make sure to add padding as necessary), and inner dimensions,
        calculates additional padding to apply.
        */
        abstract fun calcPadding(outer: XyDim, inner: XyDim): Padding

        abstract fun leftOffset(outerWidth: Float, innerWidth: Float): Float
    }

    // NOTE: This lincluded the padding!
    //    public float calcLeftX(float x, float spareRoom) {
    //        return HorizAlign.LEFT == align ? x + padding.left() :
    //               HorizAlign.CENTER == align ? x + (spareRoom / 2) :
    //               HorizAlign.RIGHT == align ? x + spareRoom - padding.right() :
    //               x;
    //    }

    fun align(a: Align) = CellStyle(a, padding, bgColor, borderStyle)
    fun padding(p: Padding) = CellStyle(align, p, bgColor, borderStyle)
    fun bgColor(c: PDColor) = CellStyle(align, padding, c, borderStyle)
    fun borderStyle(bs: BorderStyle) = CellStyle(align, padding, bgColor, bs)

    fun cellBuilder(w: Float) = Cell.builder(this, w)

    companion object {
        val DEFAULT_ALIGN = Align.TOP_LEFT
        val DEFAULT = CellStyle(DEFAULT_ALIGN, null, null, null)
    }
}
