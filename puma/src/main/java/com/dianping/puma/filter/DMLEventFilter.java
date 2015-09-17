package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.eventbus.DefaultEventBus;
import com.dianping.puma.taskexecutor.change.TargetChangedEvent;
import com.google.common.eventbus.Subscribe;

public class DMLEventFilter extends AbstractEventFilter {

    protected String name;

    private boolean dml = true;

    private TableSet acceptedTables = new TableSet();

    public DMLEventFilter() {
        DefaultEventBus.INSTANCE.register(this);
    }

    @Subscribe
    public void listenTargetChangedEvent(TargetChangedEvent event) {
        if (event.getTaskName().equals(name)) {
            setAcceptedTables(event.getTableSet());
        }
    }

    protected boolean checkEvent(ChangedEvent changedEvent) {
        if (changedEvent == null) {
            return false;
        }

        if (changedEvent instanceof RowChangedEvent) {

            // Transaction or not.
            if (((RowChangedEvent) changedEvent).isTransactionBegin()
                    || ((RowChangedEvent) changedEvent).isTransactionCommit()) {
                return true;
            }

            // Need dml or not.
            if (!dml) {
                return false;
            }

            // In accepted table list.
            Table table = new Table(changedEvent.getDatabase(), changedEvent.getTable());
            if (!acceptedTables.contains(table)) {
                return false;
            }

            return true;
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
