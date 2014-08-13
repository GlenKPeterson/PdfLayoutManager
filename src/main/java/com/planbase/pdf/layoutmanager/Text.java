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

public class Text {
    private final TextStyle style;
    private final String text;

    public static final Text DEFAULT = new Text(null, "");

    private Text(TextStyle s, String t) {
        style = s; text = t;
    }

    public static Text of(TextStyle style, String text) {
        if (text == null) { text = ""; }
        if ( "".equals(text) && (style == null) ) {
            return DEFAULT;
        }
        return new Text(style, text);
    }

    public String text() { return text; };
    public TextStyle style() { return style; }
    public int avgCharsForWidth(float width) {
        return (int) ((width * 1220) / style.avgCharWidth());
    }
}
