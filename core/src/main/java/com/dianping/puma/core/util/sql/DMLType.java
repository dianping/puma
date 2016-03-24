package com.dianping.puma.core.util.sql;

public enum DMLType {

    NULL(0), INSERT(1), DELETE(2), UPDATE(3);

    private int type;

    DMLType(int type) {
        this.type = type;
    }

    public int getDMLType() {
        return this.type;
    }

    public static DMLType getDMLType(int type) {
        for (DMLType dmlType : DMLType.values()) {
            if (dmlType.getDMLType() == type) {
                return dmlType;
            }
        }

        return null;
    }
}
