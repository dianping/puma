package com.dianping.puma.biz.entity;

import com.google.common.base.Objects;

import java.util.Set;

public class SrcDbEntity extends BaseEntity {
    public static String TAG_WRITE = "write";

    public static String TAG_READ = "read";

    private String host;

    private int port;

    private String username;

    private String password;

    private long serverId;

    private Set<String> tags;

    public String getHost() {
        return host;
    }

    public SrcDbEntity setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public SrcDbEntity setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public SrcDbEntity setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SrcDbEntity setPassword(String password) {
        this.password = password;
        return this;
    }

    public long getServerId() {
        return serverId;
    }

    public SrcDbEntity setServerId(long serverId) {
        this.serverId = serverId;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public SrcDbEntity setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public String toString() {
        return "SrcDbEntity{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SrcDbEntity that = (SrcDbEntity) o;
        return Objects.equal(port, that.port) &&
                Objects.equal(host, that.host) &&
                Objects.equal(username, that.username) &&
                Objects.equal(password, that.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(host, port, username, password);
    }
}
