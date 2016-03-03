package com.dianping.puma.biz.entity;

import java.sql.Date;

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

    private Boolean dml;

    private Boolean ddl;

    // client connect.

    private String clientAddress;

    private String serverAddress;

    // client ack.

    private Long serverId;

    private String filename;

    private Long position;

    private Date timestamp;

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

    public Boolean getDml() {
        return dml;
    }

    public void setDml(Boolean dml) {
        this.dml = dml;
    }

    public Boolean getDdl() {
        return ddl;
    }

    public void setDdl(Boolean ddl) {
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

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Long getPosition() {
        return position;
    }

    public void setPosition(Long position) {
        this.position = position;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
