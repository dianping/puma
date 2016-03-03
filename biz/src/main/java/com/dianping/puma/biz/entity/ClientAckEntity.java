package com.dianping.puma.biz.entity;

import java.sql.Date;

/**
 * Created by xiaotian.li on 16/3/1.
 * Email: lixiaotian07@gmail.com
 */
public class ClientAckEntity extends BaseEntity {

    private String clientName;

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
