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
 Represents the style of a cell including: horizontal alignment, padding, background color, and
 border style.  Immutable.
 */
public class CellStyle {

    public enum HorizAlign { LEFT, CENTER, RIGHT; }

    private final HorizAlign align;
    private final Padding padding;
    private final Color bgColor;
    private final BorderStyle borderStyle; // Only for cell-style

    private CellStyle(HorizAlign a, Padding p, Color bg, BorderStyle b) {
        if (a == null) { a = HorizAlign.LEFT; }
        if (p == null) { p = Padding.DEFAULT_PADDING; }
        align = a; padding = p; bgColor = bg; borderStyle = b;
    }

    public static CellStyle valueOf(HorizAlign a, Padding p, Color bg, BorderStyle b) {
        return new CellStyle(a, p, bg, b);
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
}
