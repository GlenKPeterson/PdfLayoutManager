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

package com.planbase.pdf.layoutmanager;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;

public class TestPdfLayoutMgr {

    public static void main(String... args) throws IOException, COSVisitorException {
        new TestPdfLayoutMgr().testPdf();
    }

    @Test
    public void testPdf() throws IOException, COSVisitorException {
        PdfLayoutMgr pageMgr = PdfLayoutMgr.newRgbPageMgr();

        OutputStream os = new FileOutputStream("test.pdf");
        final float lMargin = 40;
        final float tableWidth = pageMgr.pageWidth() - (2 * lMargin);
        final float pageRMargin = lMargin + tableWidth;
        final float colWidth = tableWidth/4f;
        final float[] colWidths = new float[] { colWidth + 10, colWidth + 10, colWidth + 10, colWidth - 30 };

        float y = pageMgr.yPageTop();

        TextStyle heading = TextStyle.of(PDType1Font.HELVETICA_BOLD, 9.5f, Color.WHITE);
        CellStyle headingCell = CellStyle.of(CellStyle.HorizAlign.CENTER, null, Color.BLUE,
                                                  BorderStyle.builder()
                                                          .left(LineStyle.of(Color.BLUE))
                                                          .right(LineStyle.of(Color.WHITE))
                                                          .build());
        CellStyle headingCellR = CellStyle.of(CellStyle.HorizAlign.CENTER, null, Color.BLACK,
                                                   BorderStyle.builder()
                                                           .left(LineStyle.of(Color.WHITE))
                                                           .right(LineStyle.of(Color.BLACK))
                                                           .build());

        TextStyle regular = TextStyle.of(PDType1Font.HELVETICA, 9.5f, Color.BLACK);
        CellStyle regularCell = CellStyle.of(CellStyle.HorizAlign.LEFT, null, null,
                                                  BorderStyle.builder()
                                                          .left(LineStyle.of(Color.BLACK))
                                                          .right(LineStyle.of(Color.BLACK))
                                                          .bottom(LineStyle.of(Color.BLACK))
                                                          .build());

        pageMgr.logicalPageStart();

        TextStyle pageHeadTextStyle = TextStyle.of(PDType1Font.HELVETICA, 7f, Color.BLACK);
        CellStyle pageHeadCellStyle = CellStyle.of(CellStyle.HorizAlign.CENTER, null, null, null);

        pageMgr.putCellAsHeaderFooter(lMargin, pageMgr.yPageTop() + 10,
                                      Cell.of(pageHeadTextStyle, tableWidth,
                                                   pageHeadCellStyle, "Test Logical Page One"));
        y = pageMgr.putRow(lMargin, y,
                           Cell.of(heading, colWidths[0], headingCell, "Transliterated Russian (with un-transliterated Chinese below)"),
                           Cell.of(heading, colWidths[1], headingCellR,
                                        "US English"),
                           Cell.of(heading, colWidths[2], headingCellR, "Finnish"),
                           Cell.of(heading, colWidths[3], headingCellR,
                                        "German"));

        File f = new File("target/test-classes/melon.jpg");
        System.out.println(f.getAbsolutePath());
        BufferedImage melonPic = ImageIO.read(f);

        y = pageMgr.putRow(lMargin, y,
                           Cell.of(regular, colWidths[0], regularCell,
                                        "Россия – священная наша держава,",
                                        "Россия – любимая наша страна.",
                                        "Могучая воля, великая слава –",
                                        "Твоё достоянье на все времена!",
                                        null,
                                        "Chorus:",
                                        null,
                                        "Славься, Отечество наше свободное, Братских народов союз вековой, Предками данная мудрость народная! Славься, страна! Мы гордимся тобой!",
                                        null,
                                        "От южных морей до полярного края Раскинулись наши леса и поля. Одна ты на свете! Одна ты такая – Хранимая Богом родная земля!",
                                        null,
                                        "Chorus:",
                                        null,
                                        "Широкий простор для мечты и для жизни",
                                        "Грядущие нам открывают года.",
                                        "Нам силу даёт наша верность Отчизне.",
                                        "Так было, так есть и так будет всегда!",
                                        null,
                                        "Chorus",
                                        null,
                                        null,
                                        null,
                                        "Chinese will not print.  The substitution character is a bullet, so below should be lots of bullets.",
                                        null,
                                        "起來！不願做奴隸的人們！ " +
                                        "把我們的血肉，築成我們新的長城！ " +
                                        "中華民族到了最危險的時候， " +
                                        "每個人被迫著發出最後的吼聲。 " +
                                        "起來！起來！起來！ " +
                                        "我們萬眾一心， " +
                                        "冒著敵人的炮火，前進！ " +
                                        "冒著敵人的炮火，前進！ " +
                                        "前進！前進！進！",
                                        null,
                                        "Here is a picture with the default and other sizes.  Though it shows up several times, the image data is only attached to the file once and reused.",
                                        ScaledJpeg.of(melonPic),
                                        ScaledJpeg.of(melonPic, 50, 50),
                                        ScaledJpeg.of(melonPic, 50, 50),
                                        ScaledJpeg.of(melonPic, 170, 100)
                           ),
                           Cell.of(regular, colWidths[1], regularCell,
                                        // Flowing text
                                        "O say can you see by the dawn's early light, " +
                                        "What so proudly we hailed at the twilight's last gleaming, " +
                                        "Whose broad stripes and bright stars through the perilous fight, " +
                                        "O'er the ramparts we watched, were so gallantly streaming? " +
                                        "And the rockets' red glare, the bombs bursting in air, " +
                                        "Gave proof through the night that our flag was still there; " +
                                        "O say does that star-spangled banner yet wave, " +
                                        "O'er the land of the free and the home of the brave? ",
                                        // Tiny space
                                        null,
                                        // Set line breaks:
                                        "On the shore dimly seen through the mists of the deep, ",
                                        "Where the foe's haughty host in dread silence reposes, ",
                                        "What is that which the breeze, o'er the towering steep, ",
                                        "As it fitfully blows, half conceals, half discloses? ",
                                        "Now it catches the gleam of the morning's first beam, ",
                                        "In full glory reflected now shines in the stream: ",
                                        "'Tis the star-spangled banner, O! long may it wave ",
                                        "O'er the land of the free and the home of the brave. ",
                                        // Big space.
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        null,
                                        // Flowing text
                                        "And where is that band who so vauntingly swore " +
                                        "That the havoc of war and the battle's confusion, " +
                                        "A home and a country, should leave us no more? " +
                                        "Their blood has washed out their foul footsteps' pollution. " +
                                        "No refuge could save the hireling and slave " +
                                        "From the terror of flight, or the gloom of the grave: " +
                                        "And the star-spangled banner in triumph doth wave, " +
                                        "O'er the land of the free and the home of the brave. " +
                                        null,
                                        "O thus be it ever, when freemen shall stand " +
                                        "Between their loved home and the war's desolation. " +
                                        "Blest with vict'ry and peace, may the Heav'n rescued land " +
                                        "Praise the Power that hath made and preserved us a nation! " +
                                        "Then conquer we must, when our cause it is just, " +
                                        "And this be our motto: \"In God is our trust.\" " +
                                        "And the star-spangled banner in triumph shall wave " +
                                        "O'er the land of the free and the home of the brave!"),
                           Cell.of(regular, colWidths[2], regularCell, "Maamme",
                                        null,
                                        "Monument to the Vårt Land poem in Helsinki. " +
                                        "Oi maamme, Suomi, synnyinmaa, " +
                                        "soi, sana kultainen! " +
                                        "Ei laaksoa, ei kukkulaa, " +
                                        "ei vettä, rantaa rakkaampaa " +
                                        "kuin kotimaa tää pohjoinen, " +
                                        "maa kallis isien. " +
                                        "Sun kukoistukses kuorestaan " +
                                        "kerrankin puhkeaa; " +
                                        "viel' lempemme saa nousemaan " +
                                        "sun toivos, riemus loistossaan, " +
                                        "ja kerran laulus, synnyinmaa " +
                                        "korkeemman kaiun saa. ",
                                        null,
                                        "Vårt land ",
                                        null,
                                        "(the original, by Johan Ludvig Runeberg) " +
                                        "Vårt land, vårt land, vårt fosterland, " +
                                        "ljud högt, o dyra ord! " +
                                        "Ej lyfts en höjd mot himlens rand, " +
                                        "ej sänks en dal, ej sköljs en strand, " +
                                        "mer älskad än vår bygd i nord, " +
                                        "än våra fäders jord! " +
                                        "Din blomning, sluten än i knopp, " +
                                        "Skall mogna ur sitt tvång; " +
                                        "Se, ur vår kärlek skall gå opp " +
                                        "Ditt ljus, din glans, din fröjd, ditt hopp. " +
                                        "Och högre klinga skall en gång " +
                                        "Vår fosterländska sång."),
                           Cell.of(regular, colWidths[3], regularCell,
                                        "Deutschland, Deutschland über alles, " +
                                        "Über alles in der Welt, " +
                                        "Wenn es stets zu Schutz und Trutze " +
                                        "Brüderlich zusammenhält. " +
                                        "Von der Maas bis an die Memel, " +
                                        "Von der Etsch bis an den Belt, " +
                                        "Deutschland, Deutschland über alles, " +
                                        "Über alles in der Welt!",
                                        null,
                                        "Deutsche Frauen, deutsche Treue, " +
                                        "Deutscher Wein und deutscher Sang " +
                                        "Sollen in der Welt behalten " +
                                        "Ihren alten schönen Klang, " +
                                        "Uns zu edler Tat begeistern " +
                                        "Unser ganzes Leben lang." +
                                        "Deutsche Frauen, deutsche Treue, " +
                                        "Deutscher Wein und deutscher Sang!",
                                        null,
                                        "Einigkeit und Recht und Freiheit " +
                                        "Für das deutsche Vaterland! " +
                                        "Danach lasst uns alle streben " +
                                        "Brüderlich mit Herz und Hand! " +
                                        "Einigkeit und Recht und Freiheit " +
                                        "Sind des Glückes Unterpfand;" +
                                        "Blüh' im Glanze dieses Glückes, " +
                                        "  Blühe, deutsches Vaterland!"));

        y = pageMgr.putRow(lMargin, y,
                           Cell.of(regular, colWidths[0], regularCell, "Another row of cells"),
                           Cell.of(regular, colWidths[1], regularCell, "On the second page"),
                           Cell.of(regular, colWidths[2], regularCell, "Just like any other page"),
                           Cell.of(regular, colWidths[3], regularCell, "That's it!"));

        pageMgr.logicalPageEnd();


        final LineStyle lineStyle = LineStyle.of(Color.BLACK, 1);

        pageMgr.logicalPageStart();

        pageMgr.putCellAsHeaderFooter(lMargin, pageMgr.yPageTop() + 10,
                                      Cell.of(pageHeadTextStyle, tableWidth,
                                                   pageHeadCellStyle, "Test Logical Page Two"));

        // Make a big 3-page X in a box

        // top lne
        pageMgr.putLine(lMargin, pageMgr.yPageTop(), pageRMargin, pageMgr.yPageTop(), lineStyle);
        // left line
        pageMgr.putLine(lMargin, pageMgr.yPageTop(), lMargin, -pageMgr.yPageTop(), lineStyle);
        // 3-page-long X
        pageMgr.putLine(lMargin, pageMgr.yPageTop(), pageRMargin, -pageMgr.yPageTop(), lineStyle);
        // middle line
        pageMgr.putLine(lMargin, 0, pageRMargin, 0, lineStyle);
        pageMgr.putLine(pageRMargin, pageMgr.yPageTop(), lMargin, -pageMgr.yPageTop(), lineStyle);
        // right line
        pageMgr.putLine(pageRMargin, pageMgr.yPageTop(), pageRMargin, -pageMgr.yPageTop(), lineStyle);
        // bottom line
        pageMgr.putLine(lMargin, -pageMgr.yPageTop(), pageRMargin, -pageMgr.yPageTop(), lineStyle);
        pageMgr.logicalPageEnd();

        pageMgr.save(os);
    }
}