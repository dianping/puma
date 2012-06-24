/**
 * Project: ${puma-common.aid}
 * 
 * File Created at 2012-6-24
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
package com.dianping.puma.common.mysql;

/**
 * TODO Comment of Pair
 * 
 * @author Leo Liang
 * 
 */
public final class RowChangedData<T> {
	private T	before;
	private T	after;

	public RowChangedData() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "RowChangedData [before=" + before + ", after=" + after + "]";
	}

	public RowChangedData(T before, T after) {
		this.before = before;
		this.after = after;
	}

	public T getBefore() {
		return before;
	}

	public void setBefore(T before) {
		this.before = before;
	}

	public T getAfter() {
		return after;
	}

	public void setAfter(T after) {
		this.after = after;
	}

	public void swap() {
		final T t = this.before;
		this.before = this.after;
		this.after = t;
	}

	public static void swap(RowChangedData<?> p) {
		doSwap(p);
	}

	private static <T> void doSwap(RowChangedData<T> p) {
		synchronized (p) {
			final T t = p.before;
			p.before = p.after;
			p.after = t;
		}
	}
}
