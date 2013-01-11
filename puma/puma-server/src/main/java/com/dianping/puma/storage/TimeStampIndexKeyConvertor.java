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

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author Leo Liang
 * 
 */

public class TimeStampIndexKeyConvertor implements IndexItemConvertor<TimeStampIndexKey> {

    @Override
    public TimeStampIndexKey convertFromString(String stringValue) {
        if (stringValue != null) {
            if (StringUtils.isNumeric(stringValue)) {
                return new TimeStampIndexKey(Long.valueOf(stringValue));
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.dianping.puma.storage.IndexItemConvertor#convertToString(java
     * .lang.Object)
     */
    @Override
    public String convertToString(TimeStampIndexKey value) {
        return Long.toString(value.getTimeStamp());
    }

}
