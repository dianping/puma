package com.dianping.puma.core.event;

/**
 * Dozer @ 7/6/15
 * mail@dozer.cc
 * http://www.dozer.cc
 */
public class EventWrap {

    private DdlEvent ddlEvent;

    private RowChangedEvent rowChangedEvent;

    private int type;

    public final static int DDL = 1;

    public final static int DML = 2;

    public EventWrap() {
    }

    public EventWrap(ChangedEvent event) {
        if (event instanceof DdlEvent) {
            setDdlEvent((DdlEvent) event);
        } else if (event instanceof RowChangedEvent) {
            setRowChangedEvent((RowChangedEvent) event);
        }
    }

    public DdlEvent getDdlEvent() {
        return ddlEvent;
    }

    public void setDdlEvent(DdlEvent ddlEvent) {
        this.ddlEvent = ddlEvent;
        this.rowChangedEvent = null;
        this.setType(DDL);
    }

    public RowChangedEvent getRowChangedEvent() {
        return rowChangedEvent;
    }

    public void setRowChangedEvent(RowChangedEvent rowChangedEvent) {
        this.rowChangedEvent = rowChangedEvent;
        this.ddlEvent = null;
        this.setType(DML);
    }

	public int getType() {
	   return type;
   }

	public void setType(int type) {
	   this.type = type;
   }
}
