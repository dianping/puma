package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.model.Schema;
import com.dianping.puma.core.model.SchemaSet;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.eventbus.DefaultEventBus;
import com.dianping.puma.taskexecutor.change.TargetChangedEvent;
import com.google.common.eventbus.Subscribe;

public class TransactionEventFilter extends AbstractEventFilter {

    protected String name;

    private boolean begin = true;

    private boolean commit = true;

    private SchemaSet acceptedSchemas = new SchemaSet();

    public TransactionEventFilter() {
        DefaultEventBus.INSTANCE.register(this);
    }

    @Subscribe
    public void listenTargetChangedEvent(TargetChangedEvent event) {
        if (event.getTaskName().equals(name)) {
            TableSet tableSet = event.getTableSet();
            SchemaSet schemaSet = new SchemaSet();
            for (Table table : tableSet.listSchemaTables()) {
                schemaSet.add(new Schema(table.getSchemaName()));
            }
            setAcceptedSchemas(schemaSet);
        }
    }

    protected boolean checkEvent(ChangedEvent changedEvent) {
        if (changedEvent instanceof RowChangedEvent) {

            // Transaction or not.
            if (!((RowChangedEvent) changedEvent).isTransactionBegin()
                    && !((RowChangedEvent) changedEvent).isTransactionCommit()) {
                return true;
            }

            // Need begin or not.
            if (((RowChangedEvent) changedEvent).isTransactionBegin() && !begin) {
                return false;
            }

            // Need commit or not.
            if (((RowChangedEvent) changedEvent).isTransactionCommit() && !commit) {
                return false;
            }

            // In accepted table list.
            String database = changedEvent.getDatabase();
            if (database == null || database.length() == 0) {
                return true;
            }

            Schema schema = new Schema(database);
            if (!acceptedSchemas.contains(schema)) {
                return false;
            }

            return true;
        }

        return true;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBegin(boolean begin) {
        this.begin = begin;
    }

    public void setCommit(boolean commit) {
        this.commit = commit;
    }

    public void setAcceptedSchemas(SchemaSet acceptedSchemas) {
        this.acceptedSchemas = acceptedSchemas;
    }
}
