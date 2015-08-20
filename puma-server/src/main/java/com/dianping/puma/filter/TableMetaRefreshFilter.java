package com.dianping.puma.filter;

import java.util.ArrayList;
import java.util.List;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.model.Table;
import com.dianping.puma.core.model.TableSet;
import com.dianping.puma.core.util.sql.DDLType;

public class TableMetaRefreshFilter {

    private String name;

    private TableSet acceptedTables = new TableSet();

    private static final List<DDLType> ddlTypes = new ArrayList<DDLType>();

    static {
        ddlTypes.add(DDLType.ALTER_TABLE);
    }

    public boolean accept(ChangedEvent changedEvent) {

        if (changedEvent == null) {
            return false;
        }

        if (!ddlTypes.contains(((DdlEvent) changedEvent).getDDLType())) {
            return false;
        }

        Table table = new Table(changedEvent.getDatabase(), changedEvent.getTable());
        if (!acceptedTables.contains(table)) {
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TableSet getAcceptedTables() {
        return acceptedTables;
    }

    public void setAcceptedTables(TableSet acceptedTables) {
        this.acceptedTables = acceptedTables;
    }

}
