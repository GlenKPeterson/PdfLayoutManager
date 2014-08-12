// Copyright 2013-03-03 PlanBase Inc. & Glen Peterson
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

import java.awt.Color;

/**
 The style of a cell including: horizontal alignment, padding, background color, and border style.
 Immutable.
 */
public class CellStyle {

    public static final CellStyle DEFAULT = new CellStyle(HorizAlign.LEFT, Padding.DEFAULT_PADDING,
                                                          null, BorderStyle.NO_BORDERS);

    /** Horizontal allignment options for cell contents */
    public enum HorizAlign { LEFT, CENTER, RIGHT; }

    private final HorizAlign align;
    private final Padding padding;
    private final Color bgColor;
    private final BorderStyle borderStyle; // Only for cell-style

    private CellStyle(HorizAlign a, Padding p, Color bg, BorderStyle b) {
        align = a; padding = p; bgColor = bg; // I think it's OK if this is null.
        borderStyle = b;
    }

    public static CellStyle of(HorizAlign a, Padding p, Color bg, BorderStyle b) {
        return builder().align(a).padding(p).bgColor(bg).borderStyle(b).build();
    }

    public Padding padding() { return padding; }
    public Color bgColor() { return bgColor; }
    public BorderStyle borderStyle() { return borderStyle; }

    public float calcLeftX(float x, float spareRoom) {
        return HorizAlign.LEFT == align ? x + padding.left() :
               HorizAlign.CENTER == align ? x + (spareRoom / 2) :
               HorizAlign.RIGHT == align ? x + spareRoom - padding.right() :
               x;
    }

    public CellStyle align(HorizAlign ha) { return new Builder(this).align(ha).build(); }
    public CellStyle padding(Padding p) { return new Builder(this).padding(p).build(); }
    public CellStyle bgColor(Color c) { return new Builder(this).bgColor(c).build(); }
    public CellStyle borderStyle(BorderStyle bs) {
        return new Builder(this).borderStyle(bs).build();
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private HorizAlign align;
        private Padding padding;
        private Color bgColor;
        private BorderStyle borderStyle; // Only for cell-style

        private Builder() {}

        private Builder(CellStyle cs) {
            align = cs.align; padding = cs.padding; bgColor = cs.bgColor;
            borderStyle = cs.borderStyle;
        }

        public CellStyle build() {
            if (align == null) { align = HorizAlign.LEFT; }
            if (padding == null) { padding = Padding.DEFAULT_PADDING; }
            if (borderStyle == null) { borderStyle = BorderStyle.NO_BORDERS; }

            if ( (align == HorizAlign.LEFT) &&
                 (padding == Padding.DEFAULT_PADDING) &&
                 ((bgColor == null) || bgColor.equals(Color.WHITE)) &&
                 (borderStyle == BorderStyle.NO_BORDERS) ) {
                return DEFAULT;
            }
            return new CellStyle(align, padding, bgColor, borderStyle);
        }

        public Builder align(HorizAlign h) { align = h; return this; }

        public Builder alignLeft() { align = HorizAlign.LEFT; return this; }
        public Builder alignCenter() { align = HorizAlign.CENTER; return this; }
        public Builder alignRight() { align = HorizAlign.RIGHT; return this; }

        public Builder padding(Padding p) { padding = p; return this; }
        public Builder bgColor(Color c) { bgColor = c; return this; }
        public Builder borderStyle(BorderStyle bs) { borderStyle = bs; return this; }
    }
}
