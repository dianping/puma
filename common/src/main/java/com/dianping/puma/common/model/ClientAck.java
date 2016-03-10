package com.dianping.puma.common.model;

import lombok.ToString;

/**
 * Created by xiaotian.li on 16/3/3.
 * Email: lixiaotian07@gmail.com
 */
@ToString
public class ClientAck {

    private Long serverId;

    private String filename;

    private Long position;

    private Long timestamp;

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

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
