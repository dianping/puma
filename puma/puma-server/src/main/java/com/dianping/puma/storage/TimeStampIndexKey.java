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
public class TimeStampIndexKey implements DataIndexKey<TimeStampIndexKey> {
    private long timeStamp;

    public TimeStampIndexKey(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public int compareTo(TimeStampIndexKey key) {
        if (this.timeStamp == key.getTimeStamp()) {
            return 0;
        }
        return this.timeStamp > key.getTimeStamp() ? 1 : -1;
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
        result = prime * result + (int) (timeStamp ^ (timeStamp >>> 32));
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
        TimeStampIndexKey other = (TimeStampIndexKey) obj;
        if (timeStamp != other.timeStamp)
            return false;
        return true;
    }

}
