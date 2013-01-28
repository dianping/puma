package com.dianping.puma.core.sync.delete;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

@Entity
public class SyncConfig {

    @Id
    private ObjectId id;
    private SyncDest dest;
    private SyncSrc src;
    private InstanceConfig instance;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public SyncDest getDest() {
        return dest;
    }

    public void setDest(SyncDest dest) {
        this.dest = dest;
    }

    public InstanceConfig getInstance() {
        return instance;
    }

    public void setInstance(InstanceConfig instance) {
        this.instance = instance;
    }

    public SyncSrc getSrc() {
        return src;
    }

    public void setSrc(SyncSrc src) {
        this.src = src;
    }

    @Override
    public String toString() {
        return "SyncConfig [id=" + id + ", dest=" + dest + ", src=" + src + ", instance=" + instance + "]";
    }

}
