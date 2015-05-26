package com.dianping.puma.syncserver.job.load.pool;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.MockTest;
import com.dianping.puma.syncserver.job.load.row.BatchRow;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

import java.util.concurrent.BlockingDeque;

public class SCWCBatchRowPoolTest extends MockTest {

	SCBatchRowPool scBatchRowPool;

	@Mock
	BlockingDeque<BatchRow> batchRows;

	@Before
	public void before() {
		scBatchRowPool = new SCBatchRowPool();
		scBatchRowPool.setPoolSize(10);
		scBatchRowPool.batchRows = batchRows;
		scBatchRowPool.init();
	}

	@After
	public void after() {
		scBatchRowPool.destroy();
	}

	@Test
	public void testPut() {
		// Begin.
		RowChangedEvent row0 = new RowChangedEvent();
		row0.setTransactionBegin(true);
		row0.setTransactionCommit(false);
		scBatchRowPool.put(row0);
		verify(batchRows, times(0)).peekLast();

		// Row, Begin.
		RowChangedEvent row1 = new RowChangedEvent();
		row1.setTransactionBegin(false);
		row1.setTransactionCommit(false);
		row1.setDatabase("puma");
		row1.setTable("puma");
		row1.setDmlType(DMLType.UPDATE);
		scBatchRowPool.put(row1);
		verify(batchRows, times(1)).peekLast();

		// Row, Row, Begin.
		RowChangedEvent row2 = new RowChangedEvent();
		row2.setTransactionBegin(false);
		row2.setTransactionCommit(false);
		row2.setDatabase("puma");
		row2.setTable("puma");
		row2.setDmlType(DMLType.UPDATE);
		scBatchRowPool.put(row2);
		verify(batchRows, times(2)).peekLast();

		// Commit, Row, Row, Begin.
		RowChangedEvent row3 = new RowChangedEvent();
		row3.setTransactionBegin(false);
		row3.setTransactionCommit(true);
		scBatchRowPool.put(row3);
		verify(batchRows, times(3)).peekLast();

		// Row, Commit, Row, Row, Begin.
		RowChangedEvent row4 = new RowChangedEvent();
		row4.setTransactionBegin(false);
		row4.setTransactionCommit(false);
		row4.setDatabase("puma");
		row4.setTable("puma");
		row4.setDmlType(DMLType.UPDATE);
		scBatchRowPool.put(row4);
		verify(batchRows, times(3)).peekLast();

		// Begin, Row, Commit, Row, Row, Begin.
		RowChangedEvent row5 = new RowChangedEvent();
		row5.setTransactionBegin(true);
		row5.setTransactionCommit(false);
		scBatchRowPool.put(row5);
		verify(batchRows, times(3)).peekLast();

		// Commit, Begin, Row, Commit, Row, Row, Begin.
		RowChangedEvent row6 = new RowChangedEvent();
		row6.setTransactionBegin(false);
		row6.setTransactionCommit(true);
		scBatchRowPool.put(row6);
		verify(batchRows, times(3)).peekLast();
	}
}
