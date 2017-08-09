// Copyright 2017-07-21 PlanBase Inc. & Glen Peterson
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

import com.planbase.pdf.layoutmanager.BorderStyle;
import com.planbase.pdf.layoutmanager.Cell;
import com.planbase.pdf.layoutmanager.CellStyle;
import com.planbase.pdf.layoutmanager.LineStyle;
import com.planbase.pdf.layoutmanager.Padding;
import com.planbase.pdf.layoutmanager.PageGrouping;
import com.planbase.pdf.layoutmanager.PdfLayoutMgr;
import com.planbase.pdf.layoutmanager.ScaledPng;
import com.planbase.pdf.layoutmanager.Table;
import com.planbase.pdf.layoutmanager.TableBuilder;
import com.planbase.pdf.layoutmanager.TablePart;
import com.planbase.pdf.layoutmanager.TableRowBuilder;
import com.planbase.pdf.layoutmanager.Text;
import com.planbase.pdf.layoutmanager.TextStyle;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceCMYK;
import org.junit.Test;
import org.organicdesign.fp.collections.ImList;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;

import static com.planbase.pdf.layoutmanager.CellStyle.Align.TOP_LEFT;
import static com.planbase.pdf.layoutmanager.CellStyle.Align.TOP_RIGHT;
import static com.planbase.pdf.layoutmanager.PdfLayoutMgr.Orientation.PORTRAIT;
import static com.planbase.pdf.layoutmanager.Utils.BULLET_CHAR;
import static com.planbase.pdf.layoutmanager.Utils.CMYK_BLACK;
import static org.organicdesign.fp.StaticImports.vec;

/**
 Created by gpeterso on 6/6/17.
 */
public class TestManual2 {

    public static final PDColor CMYK_DARK_GRAY = new PDColor(new float[] {0, 0, 0, 0.2f}, PDDeviceCMYK.INSTANCE);
    public static final PDColor CMYK_LIGHT_GREEN = new PDColor(new float[] {0.05f, 0, 0.1f, 0.01f}, PDDeviceCMYK.INSTANCE);

    static final CellStyle BULLET_CELL_STYLE = CellStyle.of(TOP_RIGHT, Padding.of(0, 4f, 0, 0), null, null);
    static final TextStyle BULLET_TEXT_STYLE = TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK);


    @Test public void testBodyMargins() throws IOException {
        // Nothing happens without a PdfLayoutMgr.
        PdfLayoutMgr pageMgr = PdfLayoutMgr.of(PDDeviceCMYK.INSTANCE, PDRectangle.A6);

        float bodyWidth = PDRectangle.A6.getWidth() - 80f;

        File f = new File("target/test-classes/graph2.png");
        System.out.println(f.getAbsolutePath());
        BufferedImage graphPic = ImageIO.read(f);

        PageGrouping lp = pageMgr.logicalPageStart(
                PORTRAIT,
                (pageNum, pb) ->
                {
                    boolean isLeft = pageNum % 2 == 1;
                    float leftMargin = isLeft ? 37f : 45f;
//            System.out.println("pageNum " + pageNum);
                    pb.drawLine(leftMargin, 30f, leftMargin + bodyWidth, 30f,
                                LineStyle.of(CMYK_BLACK));
                    pb.drawStyledText(leftMargin, 20f, "Page # " + pageNum,
                                      TextStyle.of(PDType1Font.HELVETICA, 9f, CMYK_BLACK));
                    return leftMargin;
                });

        TableBuilder tB = TableBuilder.of();
//        Table table = tB.addCellWidths(20f, 80f)
//                        .partBuilder()
//                        .rowBuilder()
//                        .cellBuilder().cellStyle(BULLET_CELL_STYLE)
//                        .add(BULLET_TEXT_STYLE, vec(BULLET_CHAR)).buildCell()
//                        .cellBuilder().add(TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK), vec("This is some text that has a bullet")).buildCell()
//                        .buildRow()
//                        .rowBuilder()
//                        .cellBuilder().cellStyle(BULLET_CELL_STYLE)
//                        .add(BULLET_TEXT_STYLE, vec("2.")).buildCell()
//                        .cellBuilder().add(TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK), vec("text that has a number")).buildCell()
//                        .buildRow()
//                        .buildPart()
//                        .buildTable();


        TablePart tpb = TableBuilder.of()
                                    .addCellWidths(20f, 80f).partBuilder();

        // This could be in a loop that prints out list items.
        TableRowBuilder.RowCellBuilder rcb = tpb.rowBuilder().cellBuilder();
        rcb.cellStyle(BULLET_CELL_STYLE)
           .add(BULLET_TEXT_STYLE, vec(BULLET_CHAR));
        rcb = rcb.buildCell().cellBuilder();
        rcb.add(BULLET_TEXT_STYLE, vec("This is some text that has a bullet"));
        rcb.buildCell().buildRow();

        // Next iteration in the loop
        rcb = tpb.rowBuilder().cellBuilder();
        rcb.cellStyle(BULLET_CELL_STYLE)
           .add(BULLET_TEXT_STYLE, vec("2."));
        rcb = rcb.buildCell().cellBuilder();
        rcb.add(BULLET_TEXT_STYLE, vec("text that has a number"));
        rcb.buildCell().buildRow();

        // After the loop, build the table.
        Table table = tpb.buildPart().buildTable();

        lp.drawCell(0, PDRectangle.A6.getHeight() - 40f,
                    Cell.of(CellStyle.of(TOP_LEFT, Padding.of(2), CMYK_LIGHT_GREEN,
                                         BorderStyle.of(CMYK_DARK_GRAY)), bodyWidth,
                            vec(Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK),
                                        "The long "),
                                Text.of(TextStyle.of(PDType1Font.HELVETICA_BOLD, 12f, CMYK_BLACK),
                                        "families"),
                                Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK),
                                        " needed the national " +
                                        "words and women said new. The new " +
                                        "companies told the possible hands " +
                                        "and books was low. The other " +
                                        "questions got the recent children and " +
                                        "lots felt important. The sure hands " +
                                        "moved the major stories and countries " +
                                        "showed possible. The major students " +
                                        "began the international rights and " +
                                        "places got free. The able homes said " +
                                        "the better work and cases went free."),
                                ScaledPng.of(graphPic),
                                Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK),
                                        "The hard eyes seemed the clear " +
                                        "mothers and systems came economic. " +
                                        "The high months showed the possible " +
                                        "money and eyes heard certain. The " +
                                        "true men played the different facts and " +
                                        "areas showed large. The good ways " +
                                        "lived the different countries and " +
                                        "stories found good. The certain " +
                                        "places found the political months and " +
                                        "facts told easy. The long homes ran " +
                                        "the good governments and cases " +
                                        "lived social."),
                                ScaledPng.of(graphPic),
                                Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK),
                                        "The social people ran the " +
                                        "local cases and men left local. The " +
                                        "easy areas saw the whole times and " +
                                        "systems became national. The whole " +
                                        "Page # 1questions lived the white points and " +
                                        "governments had national. The real " +
                                        "families saw the hard stories and Mrs " +
                                        "looked late. The young studies had " +
                                        "the other times and families started " +
                                        "late. The public years saw the hard " +
                                        "stories and waters used sure. The " +
                                        "clear lives showed the white work and " +
                                        "people used long. The major rights " +
                                        "was the important children and " +
                                        "mothers turned able. The " +
                                        "international men kept the real " +
                                        "questions and nights made big."),
                                ScaledPng.of(graphPic),
                                Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, CMYK_BLACK),
                                        "The " +
                                        "best points got the economic waters " +
                                        "and problems gave great. The whole " +
                                        "countries went the best children and " +
                                        "eyes came able."),
                                table)));
        lp.commit();
        // We're just going to write to a file.
        OutputStream os = new FileOutputStream("test2.pdf");

        // Commit it to the output stream!
        pageMgr.save(os);
    }

    // adj plNoun verb adj descriptiveNoun
    // and
    // subject verb pronoun matching descriptive noun

    static final ImList<String> adjs =
            vec("able", "bad", "best", "better", "big", "black", "certain", "clear", "different",
                "early", "easy", "economic", "federal", "free", "full", "good", "great", "hard",
                "high", "human", "important", "international", "large", "late", "little", "local",
                "long", "low", "major", "military", "national", "new", "old", "only", "other",
                "political", "possible", "public", "real", "recent", "right", "small", "social",
                "special", "strong", "sure", "true", "white", "whole", "young");

    static final ImList<String> verbs =
            vec("asked", "was", "became", "began", "called", "could", "came", "did", "felt",
                "found", "got", "gave", "went", "had", "heard", "helped", "kept", "knew", "left",
                "let", "liked", "lived", "looked", "made", "meant", "moved", "needed",
                "played", "put", "ran", "said", "saw", "seemed", "showed", "started", "took",
                "talked", "told", "thought", "tried", "turned", "used", "wanted", "willed",
                "worked");

    static final ImList<String> nouns =
            vec("areas", "books", "businesses", "cases", "children", "companies", "countries",
                "days", "eyes", "facts", "families", "governments", "groups", "hands", "homes",
                "jobs", "lives", "lots", "men", "money", "months", "mothers", "Mrs", "nights",
                "numbers", "parts", "people", "places", "points", "problems", "programs",
                "questions", "rights", "rooms", "schools", "states", "stories", "students",
                "studies", "systems", "things", "times", "waters", "ways",
                "weeks", "women", "words", "work", "worlds", "years");

    static String mumble(int times) {
        SecureRandom rand = new SecureRandom();
        StringBuilder sB = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sB.append("The ").append(adjs.get(rand.nextInt(adjs.size()))).append(" ")
              .append(nouns.get(rand.nextInt(nouns.size()))).append(" ")
              .append(verbs.get(rand.nextInt(verbs.size()))).append(" the ")
              .append(adjs.get(rand.nextInt(adjs.size()))).append(" ")
              .append(nouns.get(rand.nextInt(nouns.size()))).append(" and ")
              .append(nouns.get(rand.nextInt(nouns.size()))).append(" ")
              .append(verbs.get(rand.nextInt(verbs.size()))).append(" ")
              .append(adjs.get(rand.nextInt(adjs.size()))).append(".  ");
        }
        return sB.toString();
    }
}