package com.dianping.puma.biz.entity;

/**
 * Created by xiaotian.li on 16/3/1.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAckEntity extends BaseEntity {

    private String clientName;

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
