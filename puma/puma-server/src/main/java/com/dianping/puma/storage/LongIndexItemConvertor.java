/**
 * Project: puma-server
 * 
 * File Created at 2013-1-9
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

import org.codehaus.plexus.util.StringUtils;

/**
 * TODO Comment of LongIndexItemConvertor
 * @author Leo Liang
 *
 */
public class LongIndexItemConvertor implements IndexItemConvertor<Long> {

    /* (non-Javadoc)
     * @see com.dianping.puma.storage.IndexItemConvertor#convertFromString(java.lang.String)
     */
    @Override
    public Long convertFromString(String stringValue) {
        if(StringUtils.isNumeric(stringValue)){
            return Long.valueOf(stringValue);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see com.dianping.puma.storage.IndexItemConvertor#convertToString(java.lang.Object)
     */
    @Override
    public String convertToString(Long value) {
        return Long.toString(value);
    }

}
