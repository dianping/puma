package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.model.Table;
import com.dianping.puma.model.TableSet;

public class DMLEventFilter extends AbstractEventFilter {

    protected String name;

    private boolean dml = true;

    private TableSet acceptedTables = new TableSet();

    protected boolean checkEvent(ChangedEvent changedEvent) {
        if (changedEvent == null) {
            return false;
        }

        if (changedEvent instanceof RowChangedEvent) {

            // Need dml or not.
            if (!dml) {
                return false;
            }

            // Transaction or not.
            if (((RowChangedEvent) changedEvent).isTransactionBegin()
                    || ((RowChangedEvent) changedEvent).isTransactionCommit()) {
                return true;
            }

            // In accepted table list.
            Table table = new Table(changedEvent.getDatabase(), changedEvent.getTable());
            return acceptedTables.contains(table);

        }

        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDml(boolean dml) {
        this.dml = dml;
    }

    public void setAcceptedTables(TableSet acceptedTables) {
        this.acceptedTables = acceptedTables;
    }
}
