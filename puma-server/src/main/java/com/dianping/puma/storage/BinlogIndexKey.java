/**
 * Project: puma-server
 * 
 * File Created at 2013-1-8
 * $Id$
 * 
 * Copyright 2010 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.puma.storage;

/**
 * 
 * @author Leo Liang
 * 
 */
public class BinlogIndexKey implements DataIndexKey<BinlogIndexKey> {
    private String binlogFile;
    private long   binlogPos;
    private long   serverId;

    public BinlogIndexKey(String binlogFile, long binlogPos, long serverId) {
        this.binlogFile = binlogFile;
        this.binlogPos = binlogPos;
        this.serverId = serverId;
    }

    /**
     * @return the binlogFile
     */
    public String getBinlogFile() {
        return binlogFile;
    }

    /**
     * @param binlogFile
     *            the binlogFile to set
     */
    public void setBinlogFile(String binlogFile) {
        this.binlogFile = binlogFile;
    }

    /**
     * @return the binlogPos
     */
    public long getBinlogPos() {
        return binlogPos;
    }

    /**
     * @param binlogPos
     *            the binlogPos to set
     */
    public void setBinlogPos(long binlogPos) {
        this.binlogPos = binlogPos;
    }

    /**
     * @return the serverId
     */
    public long getServerId() {
        return serverId;
    }

    /**
     * @param serverId
     *            the serverId to set
     */
    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    @Override
    public int compareTo(BinlogIndexKey key) {
        if (this.serverId == key.getServerId()) {
            if (this.binlogFile.equals(key.getBinlogFile())) {
                if (this.binlogPos == key.getBinlogPos()) {
                    return 0;
                } else {
                    return this.binlogPos > key.getBinlogPos() ? 1 : -1;
                }
            } else {
                return this.binlogFile.compareTo(key.getBinlogFile());
            }
        } else {
            return this.serverId > key.getServerId() ? 1 : -1;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((binlogFile == null) ? 0 : binlogFile.hashCode());
        result = prime * result + (int) (binlogPos ^ (binlogPos >>> 32));
        result = prime * result + (int) (serverId ^ (serverId >>> 32));
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BinlogIndexKey other = (BinlogIndexKey) obj;
        if (binlogFile == null) {
            if (other.binlogFile != null)
                return false;
        } else if (!binlogFile.equals(other.binlogFile))
            return false;
        if (binlogPos != other.binlogPos)
            return false;
        if (serverId != other.serverId)
            return false;
        return true;
    }

}
