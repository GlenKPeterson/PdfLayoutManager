PdfLayoutManager
================
A wrapper for PDFBox to add line-breaking, page-breaking, and tables.
Uses a box-model (like HTML) for styles.
Requires PDFBox which in turn requires Log4J or apache commons Logging.

Usage Example
=============
The unit tests may be monolithic, but they provide a good usage example:

https://github.com/GlenKPeterson/PdfLayoutManager/blob/master/src/test/java/TestPdfLayoutMgr.java

Maven Dependency
===========================

    <dependency>
        <groupId>com.planbase.pdf</groupId>
        <artifactId>PdfLayoutManager</artifactId>
        <version>0.3.1</version>
    </dependency>

Actual files are here:
https://oss.sonatype.org/content/repositories/snapshots/com/planbase/pdf/PdfLayoutManager/0.2.2-SNAPSHOT/

Building from Source
====================
Requires Maven 3 and Java JDK 1.5 or greater (earliest verified JDK is: 1.6.0_45).  Jar file ends up in the `target/` sub-folder.

API documentation can be built with `mvn javadoc:javadoc` and is then found at `target/site/apidocs/index.html`

A sample PDF named `test.pdf` shows up in the root folder of this project when you run `mvn test`.

A jar file can be built with `mvn clean package` and ends up in the `target/` sub-folder.  Or type `mvn clean install` to build and install into your local maven repository.

Q&A
===

***Q: What languages/character sets does PdfLayoutManager support?***

**A:** The PDF spec guarantees support for [WinAnsiEncoding AKA Windows Code Page 1252](http://en.wikipedia.org/wiki/Windows-1252) and maybe four PDType1Fonts fonts without any font embedding.  WinAnsi includes the following languages:

Afrikaans (af), Albanian (sq), Basque (eu), Catalan (ca), Danish (da), Dutch (nl), English (en), Faroese (fo), Finnish (fi), French (fr), Galician (gl), German (de), Icelandic (is), Irish (ga), Italian (it), Norwegian (no), Portuguese (pt), Scottish (gd), Spanish (es), and Swedish (sv)

In addition, PdfLayoutManager uses Romanized substitutions for the Cyrillic characters of the modern Russian (ru) alphabet according to ISO 9:1995 with the following phonetic substitutions: 'Ch' for Ч and 'Shch' for Щ.

This character set is good enough for many purposes. If a character is not supported, it is converted to a bullet, so that the problem is politely, professionally visible.  [Transliteration Details...](src/main/java/com/planbase/pdf/layoutmanager/PdfLayoutMgr.java#L841)


***Q: I want different fonts and more characters!***

**A:** Fonts that support a wide range of characters tend to be large: 10MB or more. Embedding such a font in every PDF file is totally unacceptable for most people who have to build PDF files on the fly for users to download, or to send in email. To avoid this, we would have to keep track of what characters are used, then embed an appropriate subset of a font in the resulting PDF.  Different fonts are already divided into subsets, but not necessarily the same subsets.  Almost no font has every character, and it would be your responsibility to provide fonts, and maybe fallback fonts that cover all the characters you might possibly need.

If we did support this, I don't know how much it would slow down PDF creation.  Maybe we'd have a separate project that just maps characters to font fragments.  You'd run that first, then pass the ideally formatted/partitioned output to PdfLayoutManger to use on-the-fly without much performance cost.  That still requires you to do a lot of work, or at least develop an understanding of a number of underlying character/font issues that our character-set limitations allow us to ignore.

Last time I looked, PDFBox had hard-coded a character encoding that made it difficult for me to work with alternative character encodings. What they did might be correct, but I looked at it, got confused and frustrated, then gave up. Another volunteer started playing with this and gave up too.

That said, this is definitely a solvable problem. There is a broad spectrum of for-profit PDF-producing software. One of the main reasons they can charge money for their products is because this problem is so hard.  Still, I hope we can tackle this problem some day and develop a quality solution.

***Q: I don't want text wrapping.  I just want to set the size of a cell and let it chop off whatever I put in there.***

**A:** PdfLayoutManager was intended to provide html-table-like flowing of text and resizing of cells to fit whatever you put in them, even across multiple pages.  If you don't need that, use PDFBox directly.  If you need other features of PdfLayoutManager, there is a minHeight() setting on table rows.  Combined with padding and alignment, that may get you what you need to layout things that will always fit in the box.

***Q: Will PdfLayoutManager ever support cropping the contents of a fixed-size box?***

**A:** If the contents are all little things, we could just show as many little letters or images as completely fit, then no more (truncate the list of contents).  But if the contents are big compared to the bounding box, we either have to show none of them, or crop the individual images or letters.  I'm not currently aware if PDFBox or the PDF spec has cropping built in.  I guess showing none could work, but I'm not in a rush to implement that since it's conceptually so different from how the rest of PefLayoutManager works.

Maybe some day I'll provide some sample code so you can do this yourself.  [TextStyle](src/main/java/com/planbase/pdf/layoutmanager/TextStyle.java) has lineHeight() and stringWidthInDocUnits() that you may find useful for writing your own compatible cropping algorithm.  If you do that (and it works well), I hope you'll consider contributing it back to PdfLayoutManager so that others can benefit!

Recent Changes
==============
***Version 0.3.1***
Upgraded PdfBox dependency to 1.8.9

**2015-03-14 Portrait Orientation**
***Version 0.3.0-SNAPSHOT***
Added Portrait page orientation (previously only Landscape was available).  Orientation applies to a LogicalPage
(a grouping of similar pages) so that you can switch mid-document.  Public methods were moved from PdfLayoutMgr to
LogicalPage in order for this to work: pageMgr(), yPageTop(), yPageBottom(), and pageWidth(). Now you must call
them *after* LogicalPage lp = pageMgr.logicalPageStart(); Also added some comments and fixed others.  Because this
changes the API, I bumped up the middle version number.  Thank you @EricHans76 for requesting this feature.

***Version 0.2.2***
Fixed misleading use of package-scoped method in the example/test.  Thank you @wienczny

Version 0.2.1: Built with JDK 1.6.0_45 to ensure backward compatibility (Maven 3 wouldn't run with 1.5.0_22).
Made a few classes private and updated the JavaDocs.  Thank you @peterdietz

Version 0.2: Added true table builder classes, a cascade of styles from Document to LogicalPage to Renderable/Cell/Table, etc.
Tables have Parts (which you probably will define as a head and body with appropriate styles and column widths).
TableParts have Rows, Rows have Cells.
Cells are also Renderable and available outside of Tables, analogous to the HTML box-model, but without "margins" only padding and borders.
Also added PNG image support.

Version 0.1.1: Added run-time check for appropriate data types used to create a Cell to provide fail-fast behavior for
client code - if you pass an invalid object type, it throws an exception immediately instead of waiting to purge the cache.

Version 0.1: Initial working and documented version

Intended API Changes
====================
Be a little more careful about making things inside the PdfLayoutMgr class private.  Some older code in there is working
and needs a cleanup before this can happen.  If users follow the sample code in test/java/TestPdfLayoutMgr and don't
try accessing default/package-scoped methods, they should not need to make any changes when this cleanup happens.

License
=======
Copyright 2015 PlanBase Inc. and Glen Peterson

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
