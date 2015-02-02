package com.dianping.puma.core.replicate.model;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.PrePersist;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Leo Liang
 */
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 8121775127353895008L;

    @Id
    private ObjectId id;
    private Date gmtCreate;
    private Date gmtModified;
    private Long version;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (gmtCreate == null) {
            gmtCreate = now;
        }
        gmtModified = now;

        if (version == null) {
            version = 1L;
        } else {
            version++;
        }
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

}
