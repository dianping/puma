package com.dianping.puma.parser.mysql.packet;

import com.dianping.puma.common.PumaContext;
import com.dianping.puma.utils.PacketUtils;

import java.io.IOException;
import java.nio.ByteBuffer;

public class FieldPacket extends AbstractResponsePacket {

    private static final long serialVersionUID = -870179242685869534L;
    private String catalog;
    private String db;
    private String table;
    private String originalTable;
    private String name;
    private String originalName;
    private int character;
    private long length;
    private byte type;
    private int flags;
    private byte decimals;
    private String definition;

    @Override
    protected void doReadPacket(ByteBuffer buf, PumaContext context) throws IOException {
        // TODO Auto-generated method stub
        catalog = PacketUtils.readLengthCodedString(buf);
        db = PacketUtils.readLengthCodedString(buf);
        table = PacketUtils.readLengthCodedString(buf);
        originalTable = PacketUtils.readLengthCodedString(buf);
        name = PacketUtils.readLengthCodedString(buf);
        originalName = PacketUtils.readLengthCodedString(buf);
        character = PacketUtils.readInt(buf, 2);
        length = PacketUtils.readLong(buf, 4);
        type = buf.get();
        flags = PacketUtils.readInt(buf, 2);
        decimals = buf.get();

        //skip the filter
        buf.get();
        buf.get();

        if (buf.hasRemaining()) {
            definition = PacketUtils.readLengthCodedString(buf);
        }

    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getDb() {
        return db;
    }

    public void setDb(String db) {
        this.db = db;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getOriginalTable() {
        return originalTable;
    }

    public void setOriginalTable(String originalTable) {
        this.originalTable = originalTable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public int getCharacter() {
        return character;
    }

    public void setCharacter(int character) {
        this.character = character;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public byte getDecimals() {
        return decimals;
    }

    public void setDecimals(byte decimals) {
        this.decimals = decimals;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

}
