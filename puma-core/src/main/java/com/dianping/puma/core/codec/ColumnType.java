package com.dianping.puma.core.codec;

public enum ColumnType {
	ByteArray(1),
	Decimal(2),
	Date(3),
	Float(4),
	Int(5),
	Long(6),
	Null(7),
	Short(8),
	String(9),
	Double(10),
	Time(11),
	Timestamp(12),
	BigInteger(13);
	
	private int type;

	ColumnType(int type) {
		this.type = type;
	}

	public int getType() {
		return type;
	}

	public static ColumnType getType(int type) {
		for (ColumnType columnType : ColumnType.values()) {
			if (columnType.getType() == type) {
				return columnType;
			}
		}

		return null;
	}
}
