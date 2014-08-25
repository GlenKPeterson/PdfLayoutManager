package com.planbase.pdf.layoutmanager;

import java.util.List;

public interface CellBuilder {
    public CellBuilder cellStyle(CellStyle cs);

    public CellBuilder align(CellStyle.Align align);

    public CellBuilder textStyle(TextStyle x);

    // This is a builder which is not Renderable.  No way to add something to itself *here*.
    public CellBuilder add(Renderable... rs);

    public CellBuilder add(List<Renderable> js);

    /** Must have textStyle set properly (or inherited) before calling this */
    public CellBuilder add(String... ss);

    public CellBuilder add(TextStyle ts, List<String> ls);

}
