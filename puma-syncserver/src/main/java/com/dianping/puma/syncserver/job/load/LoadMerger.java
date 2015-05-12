package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.model.RowKey;

/*
public class LoadMerger {

	public static void merge(RowChangedEvent row, BatchRows batchRows) {
		switch (row.getDmlType()) {
		case INSERT:
			mergeInsert(row, batchRows);
			break;
		case DELETE:
			mergeDelete(row, batchRows);
			break;
		case UPDATE:
			mergeUpdate(row, batchRows);
			break;
		}
	}

	private static void mergeInsert(RowChangedEvent row, BatchRows batchRows) {
		// INSERT statement overrides previous statements.
		batchRows.replace(row);
	}

	private static void mergeDelete(RowChangedEvent row, BatchRows batchRows) {
		// DELETE statement overrides previous statements.
		batchRows.replace(row);
	}

	private static void mergeUpdate(RowChangedEvent row, BatchRows batchRows) {
		RowKey oldRowKey = RowKey.getOldRowKey(row);
		RowKey newRowKey = RowKey.getNewRowKey(row);

		if (oldRowKey.equals(newRowKey)) {

			RowChangedEvent oriRow = batchRows.get(newRowKey);

			if (oriRow == null) {
				batchRows.replace(row);
			} else {

				if (row.getDmlType() == DMLType.INSERT) {
					// insert + update = insert.
					row.setDmlType(DMLType.INSERT);
				}
				batchRows.replace(row);
			}
		} else {
			// Rare case.
			// UPDATE statement updates primary key value.
			// update = delete + insert.
			RowChangedEvent oldRow = row.clone();
			oldRow.setDmlType(DMLType.DELETE);
			RowChangedEvent newRow = row.clone();
			newRow.setDmlType(DMLType.INSERT);

			merge(oldRow, batchRows);
			merge(newRow, batchRows);
		}

	}
}*/
