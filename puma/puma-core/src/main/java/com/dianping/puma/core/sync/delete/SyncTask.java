package com.dianping.puma.core.sync.delete;

import java.util.Date;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Indexed;
import com.google.code.morphia.utils.IndexDirection;

/**
 * 该类代表一个同步的任务<br>
 * 任务由puma-admin创建后保存到数据库，然后发出异步消息通知puma-syncserver。<br>
 * puma-syncserver启动时，会查询数据库，找出属于自己的SyncTask，执行同步。<br>
 * 
 * @author wukezhu
 */
public class SyncTask {

    @Id
    private ObjectId id;
    /** puma-syncserver的host，标识着某台具体的服务器 */
    private String syncServerHost;
    /** 同步配置的id，对应SyncConfig.getId() */
    @Indexed(value = IndexDirection.ASC, unique = true, dropDups = true)
    private ObjectId syncConfigId;
    /** 创建时间 */
    private Date createDate;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getSyncServerHost() {
        return syncServerHost;
    }

    public void setSyncServerHost(String syncServerHost) {
        this.syncServerHost = syncServerHost;
    }

    public ObjectId getSyncConfigId() {
        return syncConfigId;
    }

    public void setSyncConfigId(ObjectId syncConfigId) {
        this.syncConfigId = syncConfigId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((syncConfigId == null) ? 0 : syncConfigId.hashCode());
        result = prime * result + ((syncServerHost == null) ? 0 : syncServerHost.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof SyncTask))
            return false;
        SyncTask other = (SyncTask) obj;
        if (createDate == null) {
            if (other.createDate != null)
                return false;
        } else if (!createDate.equals(other.createDate))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (syncConfigId == null) {
            if (other.syncConfigId != null)
                return false;
        } else if (!syncConfigId.equals(other.syncConfigId))
            return false;
        if (syncServerHost == null) {
            if (other.syncServerHost != null)
                return false;
        } else if (!syncServerHost.equals(other.syncServerHost))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SyncTask [id=" + id + ", syncServerHost=" + syncServerHost + ", syncConfigId=" + syncConfigId + ", createDate="
                + createDate + "]";
    }

}
