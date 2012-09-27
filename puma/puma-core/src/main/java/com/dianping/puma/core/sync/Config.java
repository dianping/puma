package com.dianping.puma.core.sync;

public class Config {

    private String host;
    private Integer port;
    private String password;
    private String username;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Config [host=" + host + ", port=" + port + ", password=" + password + ", username=" + username + "]";
    }

}
