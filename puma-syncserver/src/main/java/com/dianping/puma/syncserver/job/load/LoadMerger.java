package com.dianping.puma.syncserver.job.load;

import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.util.sql.DMLType;
import com.dianping.puma.syncserver.job.load.model.BatchRows;
import com.dianping.puma.syncserver.job.load.model.RowKey;

public class LoadMerger {

	public static void merge(RowChangedEvent row, BatchRows batchRows) {
		switch (row.getDMLType()) {
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
		RowKey rowKey = RowKey.getNewRowKey(row);
		RowChangedEvent oriRow = batchRows.get(rowKey);

		if (oriRow == null) {
			batchRows.add(rowKey, row);
		} else {

			switch (row.getDMLType()) {
			case INSERT:
				// insert + insert = insert.
				batchRows.replace(rowKey, row);
				break;
			case DELETE:
				// delete + insert = insert.
				batchRows.replace(rowKey, row);
				break;
			case UPDATE:
				// update + insert = insert.
				batchRows.replace(rowKey, row);
			}

		}
	}

	private static void mergeDelete(RowChangedEvent row, BatchRows batchRows) {
		RowKey rowKey = RowKey.getOldRowKey(row);
		RowChangedEvent oriRow = batchRows.get(rowKey);

		if (oriRow == null) {
			batchRows.add(rowKey, row);
		} else {
			// DELETE statement overrides all the operations.
			// insert + delete = delete.
			// delete + delete = delete.
			// update + delete = delete.
			batchRows.replace(rowKey, row);
		}
	}

	private static void mergeUpdate(RowChangedEvent row, BatchRows batchRows) {
		RowKey oldRowKey = RowKey.getOldRowKey(row);
		RowKey newRowKey = RowKey.getNewRowKey(row);

		if (oldRowKey.equals(newRowKey)) {
			RowChangedEvent oriRow = batchRows.get(newRowKey);

			if (oriRow == null) {
				batchRows.add(newRowKey, row);
			} else {

				switch (row.getDMLType()) {
				case INSERT:
					// insert + update = insert.
					row.setDMLType(DMLType.INSERT);
					batchRows.replace(newRowKey, row);
					break;
				case DELETE:
					// delete + update = update.
					batchRows.replace(newRowKey, row);
					break;
				case UPDATE:
					// update + update = update.
					batchRows.replace(newRowKey, row);
					break;
				}

			}
		} else {
			// Rare case.
			// UPDATE statement updates primary key value.
			// update = delete + insert.
			RowChangedEvent oldRow = row.clone();
			oldRow.setDMLType(DMLType.DELETE);
			RowChangedEvent newRow = row.clone();
			newRow.setDMLType(DMLType.INSERT);

			merge(oldRow, batchRows);
			merge(newRow, batchRows);
		}

	}
}
