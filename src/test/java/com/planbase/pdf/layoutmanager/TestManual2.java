package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
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
import static java.awt.Color.*;
import static org.organicdesign.fp.StaticImports.vec;

/**
 Created by gpeterso on 6/6/17.
 */
public class TestManual2 {

    @Test public void testBodyMargins() throws IOException {
        // Nothing happens without a PdfLayoutMgr.
        PdfLayoutMgr pageMgr = PdfLayoutMgr.of(PDDeviceRGB.INSTANCE, PDRectangle.A6);
        float bodyWidth = PDRectangle.A6.getWidth() - 80f;

        File f = new File("target/test-classes/graph2.png");
        System.out.println(f.getAbsolutePath());
        BufferedImage graphPic = ImageIO.read(f);

        LogicalPage lp = pageMgr.logicalPageStart(LogicalPage.Orientation.PORTRAIT,
                                                  (pageNum, pb) ->
                                                  {
                                                      boolean isLeft = pageNum % 2 == 1;
                                                      float leftMargin = isLeft ? 37f : 45f;
//            System.out.println("pageNum " + pageNum);
                                                      pb.drawLine(leftMargin, 30f, leftMargin + bodyWidth, 30f,
                                                                  LineStyle.of(BLACK));
                                                      pb.drawStyledText(leftMargin, 20f, "Page # " + pageNum,
                                                                        TextStyle.of(PDType1Font.HELVETICA, 9f, BLACK));
                                                      return leftMargin;
                                                  });
        lp.drawCell(0, PDRectangle.A6.getHeight() - 40f,
                    Cell.of(CellStyle.of(TOP_LEFT, Padding.of(2), decode("#ccffcc"),
                                         BorderStyle.of(DARK_GRAY)), bodyWidth,
                            vec(Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, BLACK),
                                        "The long families needed the national " +
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
                                Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, BLACK),
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
                                Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, BLACK),
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
                                Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, BLACK),
                                        "The " +
                                        "best points got the economic waters " +
                                        "and problems gave great. The whole " +
                                        "countries went the best children and " +
                                        "eyes came able. The national " +
                                        "numbers played the recent numbers " +
                                        "and money liked old. The economic " +
                                        "mothers could the full waters and " +
                                        "books found different. The free school " +
                                        "s kept the white months and things " +
                                        "ran better. The easy points played the " +
                                        "little worlds and stories gave old. The " +
                                        "certain months needed the sure jobs " +
                                        "and countries found good. The good " +
                                        "companies made the low companies " +
                                        "Page # 2and stories felt hard. The recent " +
                                        "stories had the national stories and " +
                                        "countries helped right. The early eyes " +
                                        "played the white men and hands could " +
                                        "best. The better points thought the " +
                                        "political governments and jobs found " +
                                        "recent. The long points put the sure " +
                                        "things and schools became " +
                                        "international. The young hands came " +
                                        "the recent things and eyes tried large. " +
                                        "The good numbers could the human " +
                                        "questions and rights moved possible. " +
                                        "The recent facts found the high times " +
                                        "and nights moved old. The national " +
                                        "questions felt the able points and " +
                                        "studies seemed free. The full cases " +
                                        "got the great months and times played " +
                                        "public. The major mothers went the " +
                                        "sure businesses and books got true. " +
                                        "The right years meant the important " +
                                        "cases and men willed bad. The late " +
                                        "states turned the best companies and " +
                                        "problems came old. The recent " +
                                        "months had the black eyes and " +
                                        "systems helped small. The public " +
                                        "cases turned the low facts and money " +
                                        "turned military. The big days made " +
                                        "Page # 3the late work and governments moved " +
                                        "little. The better words said the recent " +
                                        "businesses and weeks felt bad. The " +
                                        "possible businesses wanted the large " +
                                        "days and waters could sure. The bad " +
                                        "businesses put the black money and " +
                                        "questions called right. The early " +
                                        "businesses lived the better ways and " +
                                        "Mrs told best. The political questions " +
                                        "ran the only money and jobs took bad. " +
                                        "The public things gave the recent " +
                                        "facts and governments looked good."))));
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