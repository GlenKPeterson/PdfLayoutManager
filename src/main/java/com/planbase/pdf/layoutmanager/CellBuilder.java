package com.planbase.pdf.layoutmanager;

import java.util.Collection;

/**
 Something that can be built into a table cell, OR just something rendered within a box model
 (like HTML) where the table-free cell is the box.
 */
public interface CellBuilder {
    /** Creates a new CellBuilder with the given CellStyle */
    CellBuilder cellStyle(CellStyle cs);

    /** Creates a new CellBuilder with the given alignment */
    CellBuilder align(CellStyle.Align align);

    /** Creates a new CellBuilder with the given TextStyle */
    CellBuilder textStyle(TextStyle x);

    /**
     Adds the given {@link Renderable} content to this cell.
     To add multiple Renderables at once, use {@link #addAll(Collection)} instead.
     */
    CellBuilder add(Renderable rs);

    /**
     Adds the given list of {@link Renderable} content to this cell.
     */
    CellBuilder addAll(Collection<? extends Renderable> js);

    /**
     Adds text, but you must have textStyle set properly (or inherited) before calling this.
     */
    CellBuilder addStrs(String... ss);

    /** Adds a list of text with the given textStyle */
    CellBuilder add(TextStyle ts, Iterable<String> ls);

    /** Returns the width of the cell being built. */
    double width();
}
