package com.planbase.pdf.layoutmanager;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holds utility functions.
 */
final class Utils {
    private Utils() { throw new UnsupportedOperationException("No instances!"); }

    public static String toString(Color c) {
        if (c == null) { return "null"; }
        return new StringBuilder("#").append(twoDigitHex(c.getRed()))
                .append(twoDigitHex(c.getGreen()))
                .append(twoDigitHex(c.getBlue())).toString();
    }
    public static String twoDigitHex(int i) {
        String h = Integer.toHexString(i);
        return (h.length() < 2) ? "0" + h : h;
    }
//    public static void println(CharSequence cs) { System.out.println(cs); }

    public static boolean equals(Object o1, Object o2) {
        return (o1 == o2) ||
                ((o1 != null) && o1.equals(o2));
    }

    public static int floatHashCode(float value) {
        return Float.floatToIntBits(value);
    }

    private static final String ISO_8859_1 = "ISO_8859_1";
    private static final String UNICODE_BULLET = "\u2022";

    // PDFBox uses an encoding that the PDF spec calls WinAnsiEncoding.  The spec says this is
    // Windows Code Page 1252.
    // http://en.wikipedia.org/wiki/Windows-1252
    // It has a lot in common with ISO-8859-1, but it defines some additional characters such as
    // the Euro symbol.
    private static final Map<String,String> utf16ToWinAnsi;
    static {
        Map<String,String> tempMap = new HashMap<String,String>();

        try {
            // 129, 141, 143, 144, and 157 are undefined in WinAnsi.
            // I had mapped A0-FF to 160-255 without noticing that that maps each character to
            // itself, meaning that Unicode and WinAnsii are the same in that range.

            // Unicode characters with exact WinAnsi equivalents
            tempMap.put("\u0152", new String(new byte[]{0,(byte)140},ISO_8859_1)); // OE
            tempMap.put("\u0153", new String(new byte[]{0,(byte)156},ISO_8859_1)); // oe
            tempMap.put("\u0160", new String(new byte[]{0,(byte)138},ISO_8859_1)); // S Acron
            tempMap.put("\u0161", new String(new byte[]{0,(byte)154},ISO_8859_1)); // s acron
            tempMap.put("\u0178", new String(new byte[]{0,(byte)159},ISO_8859_1)); // Y Diaeresis
            tempMap.put("\u017D", new String(new byte[]{0,(byte)142},ISO_8859_1)); // Capital Z-caron
            tempMap.put("\u017E", new String(new byte[]{0,(byte)158},ISO_8859_1)); // Lower-case Z-caron
            tempMap.put("\u0192", new String(new byte[]{0,(byte)131},ISO_8859_1)); // F with a hook (like jf put together)
            tempMap.put("\u02C6", new String(new byte[]{0,(byte)136},ISO_8859_1)); // circumflex (up-caret)
            tempMap.put("\u02DC", new String(new byte[]{0,(byte)152},ISO_8859_1)); // Tilde

            // Cyrillic letters map to their closest Romanizations according to ISO 9:1995
            // http://en.wikipedia.org/wiki/ISO_9
            // http://en.wikipedia.org/wiki/A_(Cyrillic)

            // Cyrillic extensions
            // 0400 Ѐ Cyrillic capital letter IE WITH GRAVE
            // ≡ 0415 Е  0300 (left-accent)
            tempMap.put("\u0400", new String(new byte[]{0,(byte)200},ISO_8859_1));
            // 0401 Ё Cyrillic capital letter IO
            // ≡ 0415 Е  0308 (diuresis)
            tempMap.put("\u0401", new String(new byte[]{0,(byte)203},ISO_8859_1));
            // 0402 Ђ Cyrillic capital letter DJE
            tempMap.put("\u0402", new String(new byte[]{0,(byte)208},ISO_8859_1));
            // 0403 Ѓ Cyrillic capital letter GJE
            // ≡ 0413 Г  0301 (accent)
            // Ghe only maps to G-acute, which is not in our charset.
            // 0404 Є Cyrillic capital letter UKRAINIAN IE
            tempMap.put("\u0404", new String(new byte[]{0,(byte)202},ISO_8859_1));
            // 0405 Ѕ Cyrillic capital letter DZE
            tempMap.put("\u0405", "S"); //
            // 0406 І Cyrillic capital letter BYELORUSSIAN-
            // UKRAINIAN I
            // → 0049 I  latin capital letter i
            // → 0456 і  cyrillic small letter byelorussian-
            // ukrainian i
            // → 04C0 Ӏ  cyrillic letter palochka
            tempMap.put("\u0406", new String(new byte[]{0,(byte)204},ISO_8859_1));
            // 0407 Ї Cyrillic capital letter YI
            // ≡ 0406 І  0308 (diuresis)
            tempMap.put("\u0407", new String(new byte[]{0,(byte)207},ISO_8859_1));
            // 0408 Ј Cyrillic capital letter JE
            // 0409 Љ Cyrillic capital letter LJE
            // 040A Њ Cyrillic capital letter NJE
            // 040B Ћ Cyrillic capital letter TSHE
            // 040C Ќ Cyrillic capital letter KJE
            // ≡ 041A К  0301 (accent)
            // 040D Ѝ Cyrillic capital letter I WITH GRAVE
            // ≡ 0418 И  0300 (accent)
            // 040E Ў Cyrillic capital letter SHORT U
            // ≡ 0423 У  0306 (accent)
            // 040F Џ Cyrillic capital letter DZHE

            // Basic Russian alphabet
            // See: http://www.unicode.org/charts/PDF/U0400.pdf
            // 0410 А Cyrillic capital letter A => Latin A
            tempMap.put("\u0410", "A");
            // 0411 Б Cyrillic capital letter BE => Latin B
            // → 0183 ƃ  latin small letter b with topbar
            tempMap.put("\u0411", "B");
            // 0412 В Cyrillic capital letter VE => Latin V
            tempMap.put("\u0412", "V");
            // 0413 Г Cyrillic capital letter GHE => Latin G
            tempMap.put("\u0413", "G");
            // 0414 Д Cyrillic capital letter DE => Latin D
            tempMap.put("\u0414", "D");
            // 0415 Е Cyrillic capital letter IE => Latin E
            tempMap.put("\u0415", "E");
            // 0416 Ж Cyrillic capital letter ZHE => Z-caron
            tempMap.put("\u0416", new String(new byte[]{0,(byte)142},ISO_8859_1));
            // 0417 З Cyrillic capital letter ZE => Latin Z
            tempMap.put("\u0417", "Z");
            // 0418 И Cyrillic capital letter I => Latin I
            tempMap.put("\u0418", "I");
            // 0419 Й Cyrillic capital letter SHORT I => Latin J
            // ≡ 0418 И  0306 (a little mark)
            // The two-character form (reversed N plus the mark) is not supported.
            tempMap.put("\u0419", "J");
            // 041A К Cyrillic capital letter KA => Latin K
            tempMap.put("\u041A", "K");
            // 041B Л Cyrillic capital letter EL => Latin L
            tempMap.put("\u041B", "L");
            // 041C М Cyrillic capital letter EM => Latin M
            tempMap.put("\u041C", "M");
            // 041D Н Cyrillic capital letter EN => Latin N
            tempMap.put("\u041D", "N");
            // 041E О Cyrillic capital letter O => Latin O
            tempMap.put("\u041E", "O");
            // 041F П Cyrillic capital letter PE => Latin P
            tempMap.put("\u041F", "P");
            // 0420 Р Cyrillic capital letter ER => Latin R
            tempMap.put("\u0420", "R");
            // 0421 С Cyrillic capital letter ES => Latin S
            tempMap.put("\u0421", "S");
            // 0422 Т Cyrillic capital letter TE => Latin T
            tempMap.put("\u0422", "T");
            // 0423 У Cyrillic capital letter U => Latin U
            // → 0478 Ѹ  cyrillic capital letter uk
            // → 04AF ү  cyrillic small letter straight u
            // → A64A Ꙋ  cyrillic capital letter monograph uk
            tempMap.put("\u0423", "U");
            tempMap.put("\u0478", "U"); // Is this right?
            tempMap.put("\u04AF", "U"); // Is this right?
            tempMap.put("\uA64A", "U"); // Is this right?
            // 0424 Ф Cyrillic capital letter EF => Latin F
            tempMap.put("\u0424", "F");
            // 0425 Х Cyrillic capital letter HA => Latin H
            tempMap.put("\u0425", "H");
            // 0426 Ц Cyrillic capital letter TSE => Latin C
            tempMap.put("\u0426", "C");
            // 0427 Ч Cyrillic capital letter CHE => Mapping to "Ch" because there is no
            // C-caron - hope this is the best choice!  A also had this as "CH" but some make it
            // Tch as in Tchaikovsky, really didn't know what to do here.
            tempMap.put("\u0427", "Ch");
            // 0428 Ш Cyrillic capital letter SHA => S-caron
            tempMap.put("\u0428", new String(new byte[]{0,(byte)138},ISO_8859_1));
            // 0429 Щ Cyrillic capital letter SHCHA => Latin "Shch" because there is no
            // S-circumflex to map it to.  Should it go to S-caron like SHA?
            tempMap.put("\u0429", "Shch");
            // 042A Ъ Cyrillic capital letter HARD SIGN => Latin double prime, or in this case,
            // right double-quote.
            tempMap.put("\u042A", new String(new byte[]{0,(byte)148},ISO_8859_1));
            // 042B Ы Cyrillic capital letter YERU => Latin Y
            tempMap.put("\u042B", "Y");
            // 042C Ь Cyrillic capital letter SOFT SIGN => Latin prime, or in this case,
            // the right-single-quote.
            tempMap.put("\u042C", new String(new byte[]{0,(byte)146},ISO_8859_1));
            // 042D Э Cyrillic capital letter E => Latin E-grave
            tempMap.put("\u042D", new String(new byte[]{0,(byte)200},ISO_8859_1));
            // 042E Ю Cyrillic capital letter YU => Latin U-circumflex
            tempMap.put("\u042E", new String(new byte[]{0,(byte)219},ISO_8859_1));
            // 042F Я Cyrillic capital letter YA => A-circumflex
            tempMap.put("\u042F", new String(new byte[]{0,(byte)194},ISO_8859_1));
            // 0430 а Cyrillic small letter A
            tempMap.put("\u0430", "a");
            // 0431 б Cyrillic small letter BE
            tempMap.put("\u0431", "b");
            // 0432 в Cyrillic small letter VE
            tempMap.put("\u0432", "v");
            // 0433 г Cyrillic small letter GHE
            tempMap.put("\u0433", "g");
            // 0434 д Cyrillic small letter DE
            tempMap.put("\u0434", "d");
            // 0435 е Cyrillic small letter IE
            tempMap.put("\u0435", "e");
            // 0436 ж Cyrillic small letter ZHE
            tempMap.put("\u0436", new String(new byte[]{0,(byte)158},ISO_8859_1));
            // 0437 з Cyrillic small letter ZE
            tempMap.put("\u0437", "z");
            // 0438 и Cyrillic small letter I
            tempMap.put("\u0438", "i");
            // 0439 й Cyrillic small letter SHORT I
            // ≡ 0438 и  0306 (accent)
            tempMap.put("\u0439", "j");
            // 043A к Cyrillic small letter KA
            tempMap.put("\u043A", "k");
            // 043B л Cyrillic small letter EL
            tempMap.put("\u043B", "l");
            // 043C м Cyrillic small letter EM
            tempMap.put("\u043C", "m");
            // 043D н Cyrillic small letter EN
            tempMap.put("\u043D", "n");
            // 043E о Cyrillic small letter O
            tempMap.put("\u043E", "o");
            // 043F п Cyrillic small letter PE
            tempMap.put("\u043F", "p");
            // 0440 р Cyrillic small letter ER
            tempMap.put("\u0440", "r");
            // 0441 с Cyrillic small letter ES
            tempMap.put("\u0441", "s");
            // 0442 т Cyrillic small letter TE
            tempMap.put("\u0442", "t");
            // 0443 у Cyrillic small letter U
            tempMap.put("\u0443", "u");
            // 0444 ф Cyrillic small letter EF
            tempMap.put("\u0444", "f");
            // 0445 х Cyrillic small letter HA
            tempMap.put("\u0445", "h");
            // 0446 ц Cyrillic small letter TSE
            tempMap.put("\u0446", "c");
            // 0447 ч Cyrillic small letter CHE - see notes on capital letter.
            tempMap.put("\u0447", "ch");
            // 0448 ш Cyrillic small letter SHA
            tempMap.put("\u0448", new String(new byte[]{0,(byte)154},ISO_8859_1));
            // 0449 щ Cyrillic small letter SHCHA
            tempMap.put("\u0449", "shch");
            // 044A ъ Cyrillic small letter HARD SIGN
            tempMap.put("\u044A", new String(new byte[]{0,(byte)148},ISO_8859_1));
            // 044B ы Cyrillic small letter YERU
            // → A651 ꙑ  cyrillic small letter yeru with back yer
            tempMap.put("\u044B", "y");
            // 044C ь Cyrillic small letter SOFT SIGN
            // → 0185 ƅ  latin small letter tone six
            // → A64F ꙏ  cyrillic small letter neutral yer
            tempMap.put("\u044C", new String(new byte[]{0,(byte)146},ISO_8859_1));
            // 044D э Cyrillic small letter E
            tempMap.put("\u044D", new String(new byte[]{0,(byte)232},ISO_8859_1));
            // 044E ю Cyrillic small letter YU
            // → A655 ꙕ  cyrillic small letter reversed yu
            tempMap.put("\u044E", new String(new byte[]{0,(byte)251},ISO_8859_1));
            tempMap.put("\uA655", new String(new byte[]{0,(byte)251},ISO_8859_1)); // is this right?
            // 044F я Cyrillic small letter YA => a-circumflex
            tempMap.put("\u044F", new String(new byte[]{0,(byte)226},ISO_8859_1));

            // Cyrillic extensions
            // 0450 ѐ CYRILLIC SMALL LETTER IE WITH GRAVE
            // • Macedonian
            // ≡ 0435 е  0300 $̀
            tempMap.put("\u0450", new String(new byte[]{0,(byte)232},ISO_8859_1)); // e-grave => e-grave
            // 0451 ё CYRILLIC SMALL LETTER IO
            // • Russian, ...
            // ≡ 0435 е  0308 $̈
            tempMap.put("\u0451", new String(new byte[]{0,(byte)235},ISO_8859_1));
            // 0452 ђ CYRILLIC SMALL LETTER DJE
            // • Serbian
            // → 0111 đ  latin small letter d with stroke
            tempMap.put("\u0452", new String(new byte[]{0,(byte)240},ISO_8859_1));
            // 0453 ѓ CYRILLIC SMALL LETTER GJE - only maps to g-acute, which is not in our charset.
            // • Macedonian
            // ≡ 0433 г  0301 $́
            // 0454 є CYRILLIC SMALL LETTER UKRAINIAN IE
            // = Old Cyrillic yest
            tempMap.put("\u0454", new String(new byte[]{0,(byte)234},ISO_8859_1));
            // 0455 ѕ CYRILLIC SMALL LETTER DZE
            // • Macedonian
            // → A643 ꙃ  cyrillic small letter dzelo
            tempMap.put("\u0455", "s");
            // 0456 CYRILLIC SMALL LETTER BYELORUSSIAN-
            // UKRAINIAN I
            // = Old Cyrillic i
            tempMap.put("\u0456", new String(new byte[]{0,(byte)236},ISO_8859_1));
            // 0457 ї CYRILLIC SMALL LETTER YI
            // • Ukrainian
            // ≡ 0456 і  0308 $̈
            tempMap.put("\u0457", new String(new byte[]{0,(byte)239},ISO_8859_1));
            // 0458 ј CYRILLIC SMALL LETTER JE
            // • Serbian, Azerbaijani, Altay
            // 0459 љ CYRILLIC SMALL LETTER LJE
            // • Serbian, Macedonian
            // → 01C9 lj  latin small letter lj
            // 045A њ CYRILLIC SMALL LETTER NJE
            // • Serbian, Macedonian
            // → 01CC nj  latin small letter nj
            // 045B ћ CYRILLIC SMALL LETTER TSHE
            // • Serbian
            // → 0107 ć  latin small letter c with acute
            // → 0127 ħ  latin small letter h with stroke
            // → 040B Ћ  cyrillic capital letter tshe
            // → 210F ħ  planck constant over two pi
            // → A649 ꙉ  cyrillic small letter djerv
            // 045C ќ CYRILLIC SMALL LETTER KJE
            // • Macedonian
            // ≡ 043A к  0301 $́
            // 045D ѝ CYRILLIC SMALL LETTER I WITH GRAVE
            // • Macedonian, Bulgarian
            // ≡ 0438 и  0300 $̀
            // 045E ў CYRILLIC SMALL LETTER SHORT U
            // • Byelorussian, Uzbek
            // ≡ 0443 у  0306 $̆
            // 045F џ CYRILLIC SMALL LETTER DZHE
            // • Serbian, Macedonian, Abkhasian
            // → 01C6 dž  latin small letter dz with caron

            // Extended Cyrillic
            // ...
            // 0490 Ґ CYRILLIC CAPITAL LETTER GHE WITH UPTURN => G ?
            tempMap.put("\u0490", "G"); // Ghe with upturn
            // 0491 ґ CYRILLIC SMALL LETTER GHE WITH UPTURN
            // • Ukrainian
            tempMap.put("\u0491", "g");

            // Other commonly-used unicode characters with exact WinAnsi equivalents
            tempMap.put("\u2013", new String(new byte[]{0,(byte)150},ISO_8859_1)); // En-dash
            tempMap.put("\u2014", new String(new byte[]{0,(byte)151},ISO_8859_1)); // Em-dash
            tempMap.put("\u2018", new String(new byte[]{0,(byte)145},ISO_8859_1)); // Curved single open quote
            tempMap.put("\u2019", new String(new byte[]{0,(byte)146},ISO_8859_1)); // Curved single close-quote
            tempMap.put("\u201A", new String(new byte[]{0,(byte)130},ISO_8859_1)); // Low single curved-quote
            tempMap.put("\u201C", new String(new byte[]{0,(byte)147},ISO_8859_1)); // Curved double open quote
            tempMap.put("\u201D", new String(new byte[]{0,(byte)148},ISO_8859_1)); // Curved double close-quote
            tempMap.put("\u201E", new String(new byte[]{0,(byte)132},ISO_8859_1)); // Low right double quote.
            tempMap.put("\u2020", new String(new byte[]{0,(byte)134},ISO_8859_1)); // Dagger
            tempMap.put("\u2021", new String(new byte[]{0,(byte)135},ISO_8859_1)); // Double dagger
            tempMap.put(UNICODE_BULLET, new String(new byte[]{0,(byte)149},ISO_8859_1)); // Bullet - use this as replacement character.
            tempMap.put("\u2026", new String(new byte[]{0,(byte)133},ISO_8859_1)); // Ellipsis
            tempMap.put("\u2030", new String(new byte[]{0,(byte)137},ISO_8859_1)); // Permille
            tempMap.put("\u2039", new String(new byte[]{0,(byte)139},ISO_8859_1)); // Left angle-quote
            tempMap.put("\u203A", new String(new byte[]{0,(byte)155},ISO_8859_1)); // Right angle-quote
            tempMap.put("\u20ac", new String(new byte[]{0,(byte)128},ISO_8859_1)); // Euro symbol
            tempMap.put("\u2122", new String(new byte[]{0,(byte)153},ISO_8859_1)); // Trademark symbol

        } catch (UnsupportedEncodingException uee) {
            throw new IllegalStateException("Problem creating translation table due to Unsupported Encoding (coding error)", uee);
        }
        utf16ToWinAnsi = Collections.unmodifiableMap(tempMap);
    }


    // private static final Pattern whitespacePattern = Pattern.compile("\\p{Z}+");
    // What about \u00ba??
    // \u00a0-\u00a9 \u00ab-\u00b9 \u00bb-\u00bf \u00d7 \u00f7
    private static final Pattern nonAsciiPattern = Pattern.compile("[^\u0000-\u00ff]");

    /**
     <p>PDF files are limited to the 217 characters of Windows-1252 which the PDF spec calls WinAnsi
     and Java calls ISO-8859-1.  This method transliterates the standard Java UTF-16 character
     representations to their Windows-1252 equivalents where such translation is possible.  Any
     character (e.g. Kanji) which does not have an appropriate substitute in Windows-1252 will be
     mapped to the bullet character (a round dot).</p>

     <p>This transliteration covers the modern alphabets of the following languages:<br>

     Afrikaans (af),
     Albanian (sq), Basque (eu), Catalan (ca), Danish (da), Dutch (nl), English (en), Faroese (fo),
     Finnish (fi), French (fr), Galician (gl), German (de), Icelandic (is), Irish (ga),
     Italian (it), Norwegian (no), Portuguese (pt), Scottish (gd), Spanish (es), Swedish (sv).</p>

     <p>Romanized substitutions are used for the Cyrillic characters of the modern Russian (ru)
     alphabet according to ISO 9:1995 with the following phonetic substitutions: 'Ch' for Ч and
     'Shch' for Щ.</p>

     <p>The PdfLayoutMgr calls this method internally whenever it renders text (transliteration has
     to happen before line breaking), but is available externally in case you wish to use it
     directly with PDFBox.</p>

     @param in a string in the standard Java UTF-16 encoding
     @return a string in Windows-1252 (informally called ISO-8859-1 or WinAnsi)
     */
    public static String convertJavaStringToWinAnsi(String in) {
//        ByteBuffer bb = StandardCharsets.UTF_16.encode(CharBuffer.wrap(in));
//        // then decode those bytes as US-ASCII
//        return StandardCharsets.ISO_8859_1.decode(bb).toString();
        // return java.nio.charset.StandardCharsets.ISO_8859_1.encode(in);

        Matcher m = nonAsciiPattern.matcher(in);

        StringBuilder sB = new StringBuilder();
        int idx = 0;
        while (m.find()) {

            int start = m.start(); // first character of match.
            if (idx < start) {
                // Append everything from the last match up to this one.
                sB.append(in.subSequence(idx, start));
            }

            String s = utf16ToWinAnsi.get(m.group());

            // "In WinAnsiEncoding, all unused codes greater than 40 map to the bullet character."
            // source: PDF spec, Annex D.3 PDFDocEncoding Character Set p. 656 footnote about
            // WinAnsiEncoding.
            //
            // I think the bullet is the closest thing to a "replacement character" in the
            // WinAnsi character set, so that's what I'll use it for.  It looks tons better than
            // nullnullnull...
            if (s == null) {
                s = utf16ToWinAnsi.get(UNICODE_BULLET);
            }
            sB.append(s);

            idx = m.end(); // m.end() is exclusive
        }
        if (idx < in.length()) {
            sB.append(in.subSequence(idx, in.length()));
        }
        return sB.toString();
    }

}
