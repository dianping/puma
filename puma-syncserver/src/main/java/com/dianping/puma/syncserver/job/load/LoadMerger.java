package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.syncserver.job.load.model.BatchRows;

public class LoadMerger {

	public static void merge(RowChangedEvent row, BatchRows batchRows) {
		RowChangedEvent oriRow = batchRows.getOriRow(row);
		if (oriRow == null) {
			batchRows.add(row);
		} else {
			// @TODO
			// Detailed merge process.

			batchRows.replace(oriRow, row);
		}
	}
}
