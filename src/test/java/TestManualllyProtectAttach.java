// Copyright 2013-03-13 PlanBase Inc. & Glen Peterson
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

import com.planbase.pdf.layoutmanager.Cell;
import com.planbase.pdf.layoutmanager.CellStyle;
import com.planbase.pdf.layoutmanager.LogicalPage;
import com.planbase.pdf.layoutmanager.PdfLayoutMgr;
import com.planbase.pdf.layoutmanager.TextStyle;
import com.planbase.pdf.layoutmanager.XyOffset;

import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.awt.Color;

public class TestManualllyProtectAttach {

//    public static void main(String... args) throws IOException, COSVisitorException {
//        new TestManualllyPdfLayoutMgr().testPdf();
//    }

    @Test
    public void testProtectAttach() throws IOException {
        // Nothing happens without a PdfLayoutMgr.
        PdfLayoutMgr pageMgr = PdfLayoutMgr.newRgbPageMgr();

        // A LogicalPage is a group of pages with the same settings.  When your contents scroll off
        // the bottom of a page, a new page is automatically created for you with the settings taken
        // from the LogicPage grouping. If you don't want a new page, be sure to stay within the
        // bounds of the current one!
        LogicalPage lp = pageMgr.logicalPageStart();


        // Include a simple description
        Cell protectAttach = Cell.builder(CellStyle.DEFAULT, lp.pageWidth() - 100f)
            .textStyle(TextStyle.of(PDType1Font.HELVETICA, 12f, Color.BLACK))
            .addStrs("This document should be protected against writing", "Check the document properties for Security.",
                "It should be password protected with 40-bit encryption", "There should also be a PNG image attached.")
            .build();
        XyOffset xya = lp.putCell(20f, lp.yPageTop(), protectAttach);
        
        lp.commit();
        
        // Setup basic access permission
        AccessPermission ap = new AccessPermission();
        ap.setCanModify(false);
        
        // Setup a protection policy
        StandardProtectionPolicy sp = new StandardProtectionPolicy("ownerPassword","",ap);
        
        // Apply protection
        pageMgr.protect(sp);
        
        // Add an attachemnt
        PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();
        Map<String,PDComplexFileSpecification> efMap = new HashMap<String,PDComplexFileSpecification>(1);
        

        PDComplexFileSpecification pf = new PDComplexFileSpecification();
        pf.setFile("sampleScreenShot.png");
        File png = new File("sampleScreenShot.png");
        FileInputStream fis = new FileInputStream(png);
        PDEmbeddedFile ef = pageMgr.addEmbeddedFile(fis);
        fis.close();
        ef.setSubtype("image/png");
        ef.setSize((int)png.length());
        ef.setCreationDate(new GregorianCalendar());
        pf.setEmbeddedFile(ef);
        efMap.put("Picture", pf);
        efTree.setNames(efMap);
        PDDocumentNameDictionary dict = new PDDocumentNameDictionary(pageMgr.getDocumentCatalog());
        dict.setEmbeddedFiles(efTree);
        pageMgr.getDocumentCatalog().setNames(dict);

        // All done - write it out!

        // In a web application, this could be:
        //
        // httpServletResponse.setContentType("application/pdf") // your server may do this for you.
        // os = httpServletResponse.getOutputStream()            // you probably have to do this
        //
        // Also, in a web app, you probably want name your action something.pdf and put
        // target="_blank" on your link to the PDF download action.

        // We're just going to write to a file.
        OutputStream os = new FileOutputStream("testProtectAttach.pdf");

        // Commit it to the output stream!
        pageMgr.save(os);
    }
}
