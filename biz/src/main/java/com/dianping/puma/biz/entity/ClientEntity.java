package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/2/22.
 * Email: lixiaotian07@gmail.com
 */
public class ClientEntity {

    // client.

    private String clientName;

    private String groupName;

    // client config.

    private String databaseName;

    private String tableRegex;

    private boolean dml;

    private boolean ddl;

    // client connect.

    private String clientAddress;

    private String serverAddress;

    // client ack.

    private long serverId;

    private String filename;

    private long position;

    private long timestamp;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getTableRegex() {
        return tableRegex;
    }

    public void setTableRegex(String tableRegex) {
        this.tableRegex = tableRegex;
    }

    public boolean getDml() {
        return dml;
    }

    public void setDml(boolean dml) {
        this.dml = dml;
    }

    public boolean getDdl() {
        return ddl;
    }

    public void setDdl(boolean ddl) {
        this.ddl = ddl;
    }

    public String getClientAddress() {
        return clientAddress;
    }

    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public long getServerId() {
        return serverId;
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
