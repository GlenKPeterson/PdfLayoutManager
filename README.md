# PdfLayoutManager

A wrapper for PDFBox to add line-breaking, page-breaking, and tables.
Uses a box-model (like HTML) for styles.
Requires PDFBox which in turn requires Log4J or apache commons Logging.

![Sample Output](sampleScreenShot.png)

# NOTICE: No longer Apache!
You can use this as free software under the [Affero GPL](https://www.gnu.org/licenses/agpl-3.0.en.html) or contact PlanBase about a commercial license.
See the [FAQ](#faq) below

## Usage

Example: [TestManualllyPdfLayoutMgr.java](src/test/java/TestManualllyPdfLayoutMgr.java)

[API Docs](https://glenkpeterson.github.io/PdfLayoutManager/apidocs/)

# Maven Dependency
```xml
    <!-- Now Affero GPL - no longer Apache -->
    <dependency>
        <groupId>com.planbase.pdf</groupId>
        <artifactId>PdfLayoutManager</artifactId>
        <version>1.0.0-ALPHA</version>
    </dependency>
```

# Building from Source

Requires Maven 3, Java 8, and Kotlin 1.1.51 or greater.  Jar file ends up in the `target/` sub-folder.

API documentation can be built with `mvn javadoc:javadoc` and is then found at `target/site/apidocs/index.html`

A sample PDF named `test.pdf` shows up in the root folder of this project when you run `mvn test`.

A jar file can be built with `mvn clean package` and ends up in the `target/` sub-folder.  Or type `mvn clean install` to build and install into your local maven repository.

FAQ
===
***Q: Can I use this in closed-source software?***
**A:** You can purchase a commercial license from [PlanBase Inc.](https://planbase.com)
Otherwise, you must comply with all the terms of the [Affero GPL](https://www.gnu.org/licenses/agpl-3.0.en.html).
Affero GPL software cannot be combined with closed-source software in the same JVM even if the AGPL software is used over a network or the Internet.

***Q: Can I use this in non-AfferoGPL Open-Sourced software?***
**A:** No.
Any software that uses AfferoGPL code (in the same JVM) must be released under the AfferoGPL. 

***Q: Why isn't this Apache-licensed any more?***
**A:** The recent version required a near-total rewrite in order to accommodate inline images and style changes.
PlanBase paid for that.  It was a significant investment and they deserve the chance to profit from it.
You can still use the old PdfLayoutManager versions 0.x under the Apache license, but it lacks inline styles and images.

***Q: What languages/character sets does PdfLayoutManager support?***
**A:** The PDF spec guarantees support for [WinAnsiEncoding AKA Windows Code Page 1252](http://en.wikipedia.org/wiki/Windows-1252) and maybe four PDType1Fonts fonts without any font embedding.  WinAnsi covers the following languages:

Afrikaans (af), Albanian (sq), Basque (eu), Catalan (ca), Danish (da), Dutch (nl), English (en), Faroese (fo), Finnish (fi), French (fr), Galician (gl), German (de), Icelandic (is), Irish (ga), Italian (it), Norwegian (no), Portuguese (pt), Scottish (gd), Spanish (es), and Swedish (sv)

If you embed fonts, you can use whatever characters are in that font.  PDFBox throws an exception if you request a character that's not covered by your font list.

***Q: I don't want text wrapping.  I just want to set the size of a cell and let it chop off whatever I put in there.***
**A:** PdfLayoutManager was intended to provide html-table-like flowing of text and resizing of cells to fit whatever you put in them, even across multiple pages.  If you don't need that, use PDFBox directly.  If you need other features of PdfLayoutManager, there is a minHeight() setting on table rows.  Combined with padding and alignment, that may get you what you need to layout things that will always fit in the box.

***Q: Will PdfLayoutManager ever support cropping the contents of a fixed-size box?***
**A:** If individual letters or images have a dimension which is bigger than the same dimension of their bounding box, we either have to suppress their display, or crop them.  The PDF spec mentions something about a "clipping path" that might be usable for cropping overflow if you turn it on, render your object, then turn it off again.  I'm not currently aware of PDFBox support for this (if it's even possible).

If the contents are all little things, we could just show as many little letters or images as completely fit, then no more (truncate the list of contents).  Showing none could make truncation work for big objects too, but I'm not in a rush to implement that since it's conceptually so different from the very reason for the existence of PdfLayoutManager.

Maybe some day I'll provide some sample code so you can do truncation yourself.  [TextStyle](src/main/java/com/planbase/pdf/layoutmanager/TextStyle.java) has lineHeight() and stringWidthInDocUnits() that you may find useful for writing your own compatible cropping algorithm.  If you do that (and it works well), I hope you'll consider contributing it back to PdfLayoutManager (at least to this doc) so that others can benefit!

***Q: Why doesn't PdfLayoutManager line-wrap my insanely long single-word test string properly?***
**A:** For text wrapping to work, the text needs occasional whitespace.  In HTML, strings without whitespace do not wrap at all!  In PdfLayoutManager, a long enough string will wrap at some point wider than the cell.

The text wrapping algorithm picks a slightly long starting guess for where to wrap the text, then steps backward looking for whitespace. If it doesn't find any whitspace, it splits the first line at it's original guess length and continues trying to wrap the rest of the text on the next line.

0.6 Migration
======
* replace `.yPageBottom()` with `.yBodyBottom()`
* `xyDim.y()` is now `xyDim.height()` (replace manually)
* `xyDim.x()` is now `xyDim.width()` (replace manually)
* replace LogicalPage.Orientation with PdfLayoutMgr.Orientation
* replace LogicalPage with PageGrouping
* If you usee `cellbuilder.add()` and there are errors, try replacing with `.addStrs(`
* Replace all java.awt.Color with org.apache.pdfbox.pdmodel.graphics.color.PDColor.  Yeah, really.

Recent Changes
==============
***0.7.6-ALPHA***
 - DO NOT USE THIS!  Severely broken for table contents - all goes in first cell!
 - Changed cells to try to put as many elements in each line as possible (formerly put each new element on a new line)

***0.6.6-ALPHA***
 - Make sure Cells get their default style if none is specified (avoids a NPE).
 - Moved TestManual2 to no-package so it will prove that everything it needs is public.
 - Added example of putting a whole Table within a cell in order to add bullets.

***0.6.5-ALPHA***
 - TableBuilder now produces a Table which you can call render() on.
 Instead of starting with logicalPage.tableBuilder(topLeft) or TableBuilder.of(logicalPage, topLeft), just start with TableBuilder.of().
 tB.buildTable() now returns a new Table class.
 On that, you call .render(logicalPage, topLeft, null);
 It's roughly the same number of characters of typing, but frees you from needing a logicalPage or
 any coordinates in order to construct a Table.
 Once constructed, you can throw it into anything that takes a Renderable (and is wide enough since a table's width is fixed.)

***0.6.4-ALPHA***
 - Changed from java.awt.Color to org.apache.pdfbox.pdmodel.graphics.color.PDColor.  Java Color was nasty to use with CMYK. 

***0.6.3-ALPHA***
 - Forced body images on page breaks to fall entirely within the body of the next page.
 Currently breaks multi-cell rows in tables that have this data condition (visible in the demo PDF).
 - Renamed LogicalPage to PageGrouping and PageBuffer to SinglePage.

***0.6.2-ALPHA***
 - All LogicalPage.put___ methods are now .draw___.
 - PageBuffer is now public
 - New interface RenderTarget now unifies LogicalPage and PageBuffer
 - LogicalPage is for a grouping of logical pages where the contents spill from one page to the next.
 - PageBuffer is a single physical page.  You can print off the paper, but it doesn't end up on a different page.
 - LogicalPage.putCellAsHeaderFooter(...) is replaced by pageMgr.logicalPageStart(Orientation, Fn2<Integer,PageBuffer,Float>).

***0.6.1-ALPHA***
 - Added PdfLayoutMgr.loadTrueTypeFont() to give access to the PDFBox 2.0 improved font loading capabilities.
 - Changed TextStyle to take a PDFont instead of the more specific PDType1Font
 - Bundled LiberationMono-Bold font in the test resources (for testing).
   I'm pretty sure this is excluded from the JAR file, it's just to prove that font loading works.
   If I'm doing something wrong, I hope someone will tell me and/or provide a liberal open-source font with a small file size as a substitute.
   RedHat or whoever else is responsible for this font is not endorsing, supporting, or probably even aware of this project.
   I appreciate their work and will give them credit if I can figure out who they are.
   Any font would do, I just picked this one because of the small file size and (presumably) open-source license.
 - Added PdfLayoutMgr.pageReactor to (so far) move body content left and right on alternate pages.
 - Added Paguro and bumped minimum Java version from 1.6 to 1.8.

***0.6.0-ALPHA***
 - Renamed LogicalPage yPageTop() to yBodyTop() and yPageBottom() to yBodyBottom() and fixed them
   to return correct and reasonable values based on page orientation (previously only checked
   pageHeight assuming portrait).
 - Added new static factory for LogicalPage to specify MarginBodyTop and MarginBodyBottom
 (see ASCII-art image in JavaDoc).  The defaults are the same as the previously hard-coded values.

***Version 0.5.1***
 - Added `CellBuilder.width()`.
 - Made some changes to comply with Bloch's Item 41, "never export two overloadings with the same number of parameters"
     - Changed `CellBuilder.add(Renderable...)` to `CellBuilder.add(Renderable)` (without the varargs).
     - Renamed `CellBuilder.add(String...)` to `CellBuilder.addStrs(String...)`.
     - Broadened type of `CellBuilder.add(List<Renderable>)` and renamed to `CellBuilder.addAll(Collection<? extends Renderable>)`
 - Added some @Override and @inheritDoc annotations.
 - Added `CellStyle.cellBuilder(float width)` to create a builder from a cell style.
 - Changed sign-artifacts phase from verify (which happens before install) to deploy (which happens after)
 - Regenerated JavaDocs.

***Version 0.5.0***
Upgraded to PdfBox 2.0.6 and removed Russian transliteration (because PDF box handles strange characters better).

***Version 0.4.0***
Merged @Kevindum's changes:
 - Custom page sizes: `PdfLayoutMgr.of(PDColorSpace cs, PDRectangle pageSize))`
 - Variable leading: `TextStyle.of(PDType1Font f, float sz, Color tc, float leadingFactor)`
Nice work @Kevindum - thank you!

***Version 0.3.4***
Minimum Java version is now officially 1.6.
Removed main() method in unit test (you can run junit instead).
Added some tests for default values before potentially merging @Kevindum's changes.
Upgraded TestUtils package to 0.0.6.

***Version 0.3.3***
Fixed Padding.of() static constructor issue reported by @enm260 and added unit tests for Padding.
Also slightly changed how Padding.hashCode was calculated.
Building PdfLayoutManager now requires org.organicdesign.testUtils.TestUtils as a test dependency.

***Version 0.3.2***
Added doc.close() inside the PdfLayoutMgr.save() method to ensure we free resources.  Thank you @kbdguy for pointing
this out!  Upgraded PdfBox dependency to 1.8.10.  Possibly figured out how to deploy to Nexus Sonatype so you don't
have to build from source any more!

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
and needs a cleanup before this can happen.  If users follow the sample code in test/java/TestManualllyPdfLayoutMgr and don't
try accessing default/package-scoped methods, they should not need to make any changes when this cleanup happens.

 - Consider rapping checked exceptions for functional programmers.

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
