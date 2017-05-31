// Copyright 2013-04-01 PlanBase Inc. & Glen Peterson
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

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColorSpace;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.junit.Test;

import java.io.IOException;

import static com.planbase.pdf.layoutmanager.LogicalPage.Orientation.LANDSCAPE;
import static com.planbase.pdf.layoutmanager.LogicalPage.Orientation.PORTRAIT;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.A1;
import static org.apache.pdfbox.pdmodel.common.PDRectangle.LETTER;
import static org.junit.Assert.assertEquals;

// TODO: This is LogicalPage test and should be renamed to that.
public class LogicalPageTest {
    @Test public void testBasics() throws IOException {
        PdfLayoutMgr pageMgr = PdfLayoutMgr.newRgbPageMgr();
        LogicalPage lp = pageMgr.logicalPageStart();

        // Just testing some default values before potentially merging changes that could make
        // these variable.
        assertEquals(LETTER.getWidth() - 37f, lp.yPageTop(), 0.000000001);
        assertEquals(230, lp.yPageBottom(), 0.000000001);
        assertEquals(LETTER.getHeight(), lp.pageWidth(), 0.000000001);

        lp = pageMgr.logicalPageStart(PORTRAIT);

        assertEquals(LETTER.getHeight() - 37f, lp.yPageTop(), 0.000000001);
        assertEquals(0.0, lp.yPageBottom(), 0.000000001);
        assertEquals(LETTER.getWidth(), lp.pageWidth(), 0.000000001);

        pageMgr = PdfLayoutMgr.of(PDDeviceCMYK.INSTANCE, PDRectangle.A1);
        lp = pageMgr.logicalPageStart(PORTRAIT);

        assertEquals(A1.getHeight() - 37f, lp.yPageTop(), 0.000000001);
        assertEquals(0.0, lp.yPageBottom(), 0.000000001);
        assertEquals(A1.getWidth(), lp.pageWidth(), 0.000000001);

        lp = pageMgr.logicalPageStart(LANDSCAPE);

        assertEquals(A1.getWidth() - 37f, lp.yPageTop(), 0.000000001);
//        assertEquals(0.0, lp.yPageBottom(), 0.000000001);
        assertEquals(A1.getHeight(), lp.pageWidth(), 0.000000001);

    }
}