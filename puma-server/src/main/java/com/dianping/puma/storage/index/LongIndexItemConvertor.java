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
package com.dianping.puma.storage.index;

import org.apache.commons.lang.math.NumberUtils;

/**
 * @author Leo Liang
 *
 */
public class LongIndexItemConvertor implements IndexItemConvertor<Long> {

	@Override
	public Long convertFromObj(Object value) {
		String stringValue = (String) value;
		if (NumberUtils.isNumber(stringValue)) {
			return Long.valueOf(stringValue);
		}
		return null;
	}

	@Override
	public String convertToObj(Long value) {
		return Long.toString(value);
	}

}
