package com.planbase.pdf.layoutmanager;

import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.junit.Test;
import org.organicdesign.fp.collections.ImList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;

import static com.planbase.pdf.layoutmanager.CellStyle.Align.MIDDLE_CENTER;
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

        LogicalPage lp = pageMgr.logicalPageStart(LogicalPage.Orientation.PORTRAIT, pageNum -> {
            boolean isLeft = pageNum % 2 == 1;
//            System.out.println("pageNum " + pageNum);
            return isLeft ? 37f : 45f;
        });
        lp.putCell(0, PDRectangle.A6.getHeight() - 40f,
                   Cell.of(CellStyle.of(TOP_LEFT, Padding.of(2), decode("#ccffcc"),
                                        BorderStyle.of(DARK_GRAY)), PDRectangle.A6.getWidth() - 80f,
                           Text.of(TextStyle.of(PDType1Font.HELVETICA, 12f, BLACK),
                                   mumble(50))));
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