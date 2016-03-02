package com.dianping.puma.filter;

import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.model.Schema;
import com.dianping.puma.model.SchemaSet;
import com.dianping.puma.model.Table;
import com.dianping.puma.model.TableSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class StorageEventFilterChainTest {

    DefaultEventFilterChain storageEventFilterChain = new DefaultEventFilterChain();

    @Before
    public void before() {
        List<EventFilter> eventFilters = new ArrayList<EventFilter>();

        TableSet tableSet = new TableSet();
        tableSet.add(new Table("schema", "table"));
        tableSet.add(new Table("puma", "test"));

        SchemaSet schemaSet = new SchemaSet();
        schemaSet.add(new Schema("schema"));
        schemaSet.add(new Schema("puma"));

        DMLEventFilter dmlEventFilter = new DMLEventFilter();
        dmlEventFilter.setName("puma");
        dmlEventFilter.setDml(true);
        dmlEventFilter.setAcceptedTables(tableSet);
        eventFilters.add(dmlEventFilter);

        DDLEventFilter ddlEventFilter = new DDLEventFilter();
        ddlEventFilter.setName("puma");
        ddlEventFilter.setDdl(true);
        ddlEventFilter.setAcceptedTables(tableSet);
        List<DDLType> ddlTypes = new ArrayList<DDLType>();
        ddlTypes.add(DDLType.ALTER_TABLE);
        ddlTypes.add(DDLType.CREATE_INDEX);
        ddlTypes.add(DDLType.DROP_INDEX);
        ddlEventFilter.setDdlTypes(ddlTypes);
        eventFilters.add(ddlEventFilter);

        TransactionEventFilter transactionEventFilter = new TransactionEventFilter();
        transactionEventFilter.setName("puma");
        transactionEventFilter.setBegin(false);
        transactionEventFilter.setCommit(true);
        transactionEventFilter.setAcceptedSchemas(schemaSet);
        eventFilters.add(transactionEventFilter);

        storageEventFilterChain.setEventFilters(eventFilters);
    }

    @Test
    public void testDoNext() {
        // Case 1.
        storageEventFilterChain.reset();
        RowChangedEvent rowChangedEvent0 = new RowChangedEvent();
        rowChangedEvent0.setDatabase("schema");
        rowChangedEvent0.setTable("table");
        rowChangedEvent0.setTransactionBegin(false);
        rowChangedEvent0.setTransactionCommit(false);
        Assert.assertTrue(storageEventFilterChain.doNext(rowChangedEvent0));

        // Case 2.
        storageEventFilterChain.reset();
        RowChangedEvent rowChangedEvent1 = new RowChangedEvent();
        rowChangedEvent1.setDatabase("schema");
        rowChangedEvent1.setTable("hello");
        rowChangedEvent1.setTransactionBegin(false);
        rowChangedEvent1.setTransactionCommit(false);
        Assert.assertFalse(storageEventFilterChain.doNext(rowChangedEvent1));

        // Case 3.
        storageEventFilterChain.reset();
        RowChangedEvent rowChangedEvent2 = new RowChangedEvent();
        rowChangedEvent2.setDatabase("puma");
        rowChangedEvent2.setTransactionBegin(true);
        rowChangedEvent2.setTransactionCommit(false);
        Assert.assertFalse(storageEventFilterChain.doNext(rowChangedEvent2));

        // Case 4.
        storageEventFilterChain.reset();
        RowChangedEvent rowChangedEvent3 = new RowChangedEvent();
        rowChangedEvent3.setDatabase("puma");
        rowChangedEvent3.setTransactionBegin(false);
        rowChangedEvent3.setTransactionCommit(true);
        Assert.assertTrue(storageEventFilterChain.doNext(rowChangedEvent3));

        // Case 5.
        storageEventFilterChain.reset();
        DdlEvent ddlEvent0 = new DdlEvent();
        ddlEvent0.setDDLType(DDLType.ALTER_TABLE);
        ddlEvent0.setDatabase("puma");
        ddlEvent0.setTable("test");
        Assert.assertTrue(storageEventFilterChain.doNext(ddlEvent0));

        // Case 6.
        storageEventFilterChain.reset();
        DdlEvent ddlEvent1 = new DdlEvent();
        ddlEvent1.setDDLType(DDLType.ALTER_TABLE);
        ddlEvent1.setDatabase("puma");
        ddlEvent1.setTable(null);
        Assert.assertFalse(storageEventFilterChain.doNext(ddlEvent1));
    }

}
