package com.dianping.puma.core.codec;

import com.dianping.puma.core.event.ChangedEvent;
import com.dianping.puma.core.event.DdlEvent;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.dianping.puma.core.event.RowChangedEvent.ColumnInfo;
import com.dianping.puma.core.model.BinlogInfo;
import com.dianping.puma.core.util.constant.DdlEventSubType;
import com.dianping.puma.core.util.constant.DdlEventType;
import com.dianping.puma.core.util.sql.DDLType;
import com.dianping.puma.core.util.sql.DMLType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class RawEventCodec implements EventCodec {

    public static final byte VERSION1 = 1;

    @Override
    public byte[] encode(Event event) throws IOException {
        ByteBuf buf = Unpooled.buffer();

        buf.writeByte(VERSION1);

        if (event instanceof DdlEvent) {
            buf.writeByte(DDL_EVENT);
        } else if (event instanceof RowChangedEvent) {
            buf.writeByte(DML_EVENT);
        } else {
            return null;
        }

        ChangedEvent chEvent = (ChangedEvent) event;
        buf.writeLong(chEvent.getSeq());
        buf.writeLong(chEvent.getExecuteTime());
        buf.writeLong(chEvent.getServerId());
        buf.writeLong(chEvent.getBinlogInfo().getServerId());
        writeIntoBuf(chEvent.getBinlogInfo().getBinlogFile(), buf);
        buf.writeLong(chEvent.getBinlogInfo().getBinlogPosition());
        buf.writeInt(chEvent.getBinlogInfo().getEventIndex());
        buf.writeLong(chEvent.getBinlogInfo().getTimestamp());
        writeIntoBuf(chEvent.getDatabase(), buf);
        writeIntoBuf(chEvent.getTable(), buf);

        if (event instanceof DdlEvent) {
            DdlEvent ddlEvent = (DdlEvent) event;

            writeIntoBuf(ddlEvent.getSql(), buf);
            buf.writeInt(0); // deprecated.
            buf.writeInt(0); // deprecated.
            buf.writeInt(0); // deprecated.
        } else if (event instanceof RowChangedEvent) {
            RowChangedEvent rcEvent = (RowChangedEvent) event;
            Map<String, ColumnInfo> columns = rcEvent.getColumns();
            int columnNum = columns.size();

            //已经废弃的 getActionType
            buf.writeInt(0);

            if (rcEvent.getDmlType() != null) {
                buf.writeInt(rcEvent.getDmlType().getDMLType());
            } else {
                buf.writeInt(DMLType.NULL.getDMLType());
            }
            buf.writeBoolean(rcEvent.isTransactionBegin());
            buf.writeBoolean(rcEvent.isTransactionCommit());

            if (rcEvent.getDmlType() != null) {
                switch (rcEvent.getDmlType()) {
                    case INSERT: {
                        buf.writeInt(columnNum);
                        byte[] bitSetForType = new byte[columnNum];
                        ByteBuf columnValues = encodeColumnValues(columns, bitSetForType, true);

                        buf.writeBytes(bitSetForType);
                        buf.writeBytes(columnValues);
                        break;
                    }
                    case UPDATE: {
                        buf.writeInt(columnNum);
                        byte[] columnOldTypeSet = new byte[columnNum];
                        ByteBuf columnOldValues = encodeColumnValues(columns, columnOldTypeSet, false);
                        buf.writeBytes(columnOldTypeSet);
                        buf.writeBytes(columnOldValues);

                        buf.writeInt(columnNum);
                        byte[] columnNewTypeSet = new byte[columnNum];
                        ByteBuf columnNewValues = encodeColumnValues(columns, columnNewTypeSet, true);
                        buf.writeBytes(columnNewTypeSet);
                        buf.writeBytes(columnNewValues);

                        break;
                    }
                    case DELETE: {
                        buf.writeInt(columnNum);
                        byte[] columnOldTypeSet = new byte[columnNum];
                        ByteBuf columnOldValues = encodeColumnValues(columns, columnOldTypeSet, false);
                        buf.writeBytes(columnOldTypeSet);
                        buf.writeBytes(columnOldValues);

                        break;
                    }
                    case NULL:
                }
            }
        }

        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);

        return data;
    }

    @Override
    public byte[] encodeList(List<Event> events) throws IOException {
        byte[][] byteArray = new byte[events.size()][];
        int length = 0;

        int index = 0;
        for (Event event : events) {
            byte[] data = encode(event);
            byteArray[index++] = data;

            length += data.length;
            length += 4;
        }

        byte[] result = new byte[length];
        int offset = 0;
        for (byte[] data : byteArray) {
            result[offset++] = (byte) (data.length >> 24);
            result[offset++] = (byte) (data.length >> 16);
            result[offset++] = (byte) (data.length >> 8);
            result[offset++] = (byte) (data.length);
            System.arraycopy(data, 0, result, offset, data.length);
            offset += data.length;
        }

        return result;
    }

    private ByteBuf encodeColumnValues(Map<String, ColumnInfo> columns, byte[] bitSetForType, boolean useNew) {
        ByteBuf columnValues = Unpooled.buffer();

        int index = 0;
        for (Entry<String, ColumnInfo> entry : columns.entrySet()) {
            String columnName = entry.getKey();
            ColumnInfo columnInfo = entry.getValue();
            Object columnObject = useNew ? columnInfo.getNewValue() : columnInfo.getOldValue();

            writeIntoBuf(columnName, columnValues);
            columnValues.writeBoolean(columnInfo.isKey());

            if (columnObject instanceof byte[]) {
                bitSetForType[index++] = (byte) ColumnType.ByteArray.getType();

                byte[] value = (byte[]) columnObject;
                columnValues.writeInt(value.length);
                columnValues.writeBytes(value);
            } else if (columnObject instanceof BigDecimal) {
                bitSetForType[index++] = (byte) ColumnType.Decimal.getType();

                BigDecimal column = (BigDecimal) columnObject;
                byte[] value = column.toPlainString().getBytes();
                columnValues.writeByte(value.length);
                columnValues.writeBytes(value);
            } else if (columnObject instanceof Double) {
                bitSetForType[index++] = (byte) ColumnType.Double.getType();

                Double column = (Double) columnObject;
                columnValues.writeDouble(column.doubleValue());
            } else if (columnObject instanceof Float) {
                bitSetForType[index++] = (byte) ColumnType.Float.getType();

                Float column = (Float) columnObject;
                columnValues.writeFloat(column.floatValue());
            } else if (columnObject instanceof Integer) {
                bitSetForType[index++] = (byte) ColumnType.Int.getType();

                Integer column = (Integer) columnObject;
                columnValues.writeInt(column.intValue());
            } else if (columnObject instanceof Long) {
                bitSetForType[index++] = (byte) ColumnType.Long.getType();

                Long column = (Long) columnObject;
                columnValues.writeLong(column.longValue());
            } else if (columnObject == null) {
                bitSetForType[index++] = (byte) ColumnType.Null.getType();
            } else if (columnObject instanceof Short) {
                bitSetForType[index++] = (byte) ColumnType.Short.getType();

                Short column = (Short) columnObject;
                columnValues.writeShort(column.shortValue());
            } else if (columnObject instanceof String) {
                bitSetForType[index++] = (byte) ColumnType.String.getType();

                String column = (String) columnObject;
                byte[] value = column.getBytes();
                columnValues.writeInt(value.length);
                columnValues.writeBytes(value);
            } else if (columnObject instanceof Time) {
                bitSetForType[index++] = (byte) ColumnType.Time.getType();

                Time column = (Time) columnObject;
                columnValues.writeLong(column.getTime());
            } else if (columnObject instanceof Timestamp) {
                bitSetForType[index++] = (byte) ColumnType.Timestamp.getType();

                Timestamp column = (Timestamp) columnObject;
                columnValues.writeLong(column.getTime());
            } else if (columnObject instanceof Date) {
                bitSetForType[index++] = (byte) ColumnType.Date.getType();

                Date column = (Date) columnObject;
                columnValues.writeLong(column.getTime());
            } else if (columnObject instanceof BigInteger) {
                bitSetForType[index++] = (byte) ColumnType.BigInteger.getType();

                BigInteger column = (BigInteger) columnObject;
                columnValues.writeLong(column.longValue());
            } else {
                System.out.println("Unsupported type!");
            }
        }

        return columnValues;
    }

    @Override
    public Event decode(byte[] data) throws IOException {
        ByteBuf buf = Unpooled.wrappedBuffer(data);

        byte version = buf.readByte();
        if (version != VERSION1) {
            return null;
        }

        ChangedEvent event = null;
        int type = buf.readByte();
        if (type == DDL_EVENT) {
            event = new DdlEvent();
        } else if (type == DML_EVENT) {
            event = new RowChangedEvent();
        } else {
            return null;
        }

        event.setSeq(buf.readLong());
        event.setExecuteTime(buf.readLong());
        event.setServerId(buf.readLong());
        BinlogInfo binlogInfo = new BinlogInfo(buf.readLong(), (String) readFromBuf(buf, String.class), buf.readLong(), buf.readInt(), buf.readLong());
        event.setBinlogInfo(binlogInfo);
        event.setDatabase((String) readFromBuf(buf, String.class));
        event.setTable((String) readFromBuf(buf, String.class));

        if (type == DDL_EVENT) {
            DdlEvent ddlEvent = (DdlEvent) event;

            ddlEvent.setSql((String) readFromBuf(buf, String.class));
            ddlEvent.setDDLType(DDLType.getDDLType(buf.readInt()));
            ddlEvent.setDdlEventType(DdlEventType.getEventType(buf.readInt()));
            ddlEvent.setDdlEventSubType(DdlEventSubType.getDdlEventSubType(buf.readInt()));
        } else {
            RowChangedEvent rcEvent = (RowChangedEvent) event;

            //已经废弃的 getActionType
            buf.readInt();

            DMLType dmlType = DMLType.getDMLType(buf.readInt());
            rcEvent.setDmlType(dmlType);
            rcEvent.setTransactionBegin(buf.readBoolean());
            rcEvent.setTransactionCommit(buf.readBoolean());
            Map<String, ColumnInfo> columns = rcEvent.getColumns();

            switch (dmlType) {
                case INSERT:
                    decodeColumnValues(buf, columns, true);
                    break;
                case UPDATE:
                    decodeColumnValues(buf, columns, false);
                    decodeColumnValues(buf, columns, true);
                    break;
                case DELETE:
                    decodeColumnValues(buf, columns, false);
                    break;
                case NULL:
                    break;
            }

        }

        return event;
    }

    @Override
    public List<Event> decodeList(byte[] data) throws IOException {
        List<Event> result = new ArrayList<Event>();
        int offset = 0;

        while (offset < data.length) {
            int length = data[offset] << 24 | (data[offset + 1] & 0xFF) << 16 | (data[offset + 2] & 0xFF) << 8 | (data[offset + 3] & 0xFF);
            offset += 4;
            byte[] eventData = new byte[length];
            System.arraycopy(data, offset, eventData, 0, length);
            offset += length;
            result.add(decode(eventData));
        }

        return result;
    }

    private void decodeColumnValues(ByteBuf buf, Map<String, ColumnInfo> columns, boolean useNew) {
        int columnNum = buf.readInt();
        byte[] columnValueTypes = new byte[columnNum];
        buf.readBytes(columnValueTypes);

        for (int i = 0; i < columnNum; i++) {
            ColumnType columnType = ColumnType.getType(columnValueTypes[i]);
            String columnName = (String) readFromBuf(buf, String.class);
            boolean isKey = buf.readBoolean();
            ColumnInfo columnInfo = columns.get(columnName);

            if (columnInfo == null) {
                columnInfo = new ColumnInfo();
            }

            columnInfo.setKey(isKey);
            columns.put(columnName, columnInfo);

            Object value = null;

            switch (columnType) {
                case ByteArray: {
                    int len = buf.readInt();
                    byte[] byteArray = new byte[len];
                    buf.readBytes(byteArray);
                    value = byteArray;
                    break;
                }
                case Date: {
                    long time = buf.readLong();
                    value = new Date(time);
                    break;
                }
                case Decimal: {
                    String planText = (String) readFromBuf(buf, String.class);
                    value = new BigDecimal(planText);
                    break;
                }
                case Float: {
                    value = new Float(buf.readFloat());
                    break;
                }
                case Int: {
                    value = new Integer(buf.readInt());
                    break;
                }
                case Long: {
                    value = new Long(buf.readLong());
                    break;
                }
                case Null: {
                    value = null;
                    break;
                }
                case Short: {
                    value = new Short(buf.readShort());
                    break;
                }
                case String: {
                    int len = buf.readInt();
                    byte[] byteArray = new byte[len];
                    buf.readBytes(byteArray);
                    value = new String(byteArray);
                    break;
                }
                case Time: {
                    value = new Time(buf.readLong());
                    break;
                }
                case Timestamp: {
                    value = new Timestamp(buf.readLong());
                    break;
                }
                case Double: {
                    value = new Double(buf.readDouble());
                    break;
                }
                case BigInteger: {
                    value = BigInteger.valueOf(buf.readLong());
                    break;
                }
            }

            if (useNew) {
                columnInfo.setNewValue(value);
            } else {
                columnInfo.setOldValue(value);
            }
        }
    }

    private void writeIntoBuf(Object object, ByteBuf buf) {
        if (object != null) {
            if (object instanceof String) {
                byte[] bytes = ((String) object).getBytes();

                buf.writeByte(bytes.length);
                buf.writeBytes(bytes);
            }
        } else {
            buf.writeByte(0);
        }
    }

    private Object readFromBuf(ByteBuf buf, Class<?> clazz) {
        int len = buf.readByte();

        if (len != 0) {
            if (clazz.getName().equals("java.lang.String")) {
                byte[] bytes = new byte[len];
                buf.readBytes(bytes);
                return new String(bytes);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}
