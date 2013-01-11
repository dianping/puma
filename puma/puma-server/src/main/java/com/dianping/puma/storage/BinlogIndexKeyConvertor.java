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

public class BinlogIndexKeyConvertor implements IndexItemConvertor<BinlogIndexKey> {
    private static final String SEPARATOR = "!";

    @Override
    public BinlogIndexKey convertFromString(String stringValue) {
        if (stringValue != null) {
            String[] splits = stringValue.split(SEPARATOR);
            if (splits != null && splits.length == 3 && StringUtils.isNumeric(splits[0])
                    && StringUtils.isNotBlank(splits[1]) && StringUtils.isNumeric(splits[2])) {
                return new BinlogIndexKey(splits[1], Long.valueOf(splits[2]), Long.valueOf(splits[0]));
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
    public String convertToString(BinlogIndexKey value) {
        return value.getServerId() + SEPARATOR + value.getBinlogFile() + SEPARATOR + value.getBinlogPos();
    }

}
