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

package com.planbase.pdf.layoutmanager;

/** Renderable Iterator */
public interface Renderator {

//    class SingleItemRenderator implements Renderator {
////        private final float width;
//        private Renderable item;
//
//        SingleItemRenderator(Renderable r) { item = r; }
//
//        @Override public boolean hasMore() { return item != null; }
//
//        @Override public ContTerm getSomething(float maxWidth) {
//            XyDim dim = item.calcDimensions(maxWidth);
//            FixedItem ret = new FixedItemImpl(item, dim.width(), dim.height(), 0f, dim.height());
//            item = null;
//            return ContTerm.Companion.continuing(ret);
//        }
//
//        @Override public ContTermNone getIfFits(float remainingWidth) {
//            XyDim dim = item.calcDimensions(remainingWidth);
//            if (dim.width() <= remainingWidth) {
//                ContTerm something = getSomething(remainingWidth);
//                return something.toContTermNone();
//            }
//            return ContTermNone.Companion.none();
//        }
//    }

    boolean hasMore();

    /**
     Called when line is empty.  Returns something less than maxWidth if possible, but always
     returns something even if it won’t fit.  Call this when line is empty.
     */
    ContTerm getSomething(float maxWidth);

    /**
     Called when line is not empty to try to fit on this line.  If it doesn’t fit, then the
     caller will probably create a new line and call getSomething(maxWidth) to start that line.
     */
    ContTermNone getIfFits(float remainingWidth);
}
