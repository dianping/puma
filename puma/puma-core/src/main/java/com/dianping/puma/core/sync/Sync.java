package com.dianping.puma.core.sync;

public class Sync {

    private Config src;
    private Config dest;
    private String name;
    private Long serverId;
    private String target;
    private Boolean transaction;
    private Boolean ddl;
    private Boolean dml;
    private Instance instance;

    public Config getSrc() {
        return src;
    }

    public void setSrc(Config src) {
        this.src = src;
    }

    public Config getDest() {
        return dest;
    }

    public void setDest(Config dest) {
        this.dest = dest;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Boolean getTransaction() {
        return transaction;
    }

    public void setTransaction(Boolean transaction) {
        this.transaction = transaction;
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

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return "Sync [src=" + src + ", dest=" + dest + ", name=" + name + ", serverId=" + serverId + ", target=" + target
                + ", transaction=" + transaction + ", ddl=" + ddl + ", dml=" + dml + ", instance=" + instance + "]";
    }

}
