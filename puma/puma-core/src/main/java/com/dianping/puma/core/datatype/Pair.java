/**
 * Project: puma-core
 * 
 * File Created at 2012-7-6
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
package com.dianping.puma.core.datatype;

/**
 * TODO Comment of Pair
 * 
 * @author Leo Liang
 * 
 */
public class Pair<T, F> {

	private T	first;
	private F	second;

	/**
	 * @param first
	 */
	public Pair(T first, F second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * @return the first
	 */
	public T getFirst() {
		return first;
	}

	/**
	 * @param first
	 *            the first to set
	 */
	public void setFirst(T first) {
		this.first = first;
	}

	/**
	 * @return the second
	 */
	public F getSecond() {
		return second;
	}

	/**
	 * @param second
	 *            the second to set
	 */
	public void setSecond(F second) {
		this.second = second;
	}

}
