package com.dianping.puma.filter;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.model.Schema;
import com.dianping.puma.model.SchemaSet;

public class TransactionEventFilter extends AbstractEventFilter {

    protected String name;

    private boolean begin = true;

    private boolean commit = true;

    private SchemaSet acceptedSchemas = new SchemaSet();

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
            return acceptedSchemas.contains(schema);

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
