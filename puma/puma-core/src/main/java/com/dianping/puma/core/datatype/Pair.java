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

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

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
	
	
	
	@Override
    public String toString() {
	    Gson gson = new Gson();
	    String firstJson = gson.toJson(first);
	    String secondJson = gson.toJson(second);
	    Map<String,String> map = new HashMap<String,String>();
	    map.put(firstJson, secondJson);
        return gson.toJson(map);
    }
	
//	public static <T,F> Pair<T,F> fromString(String src){
//	    Gson gson = new Gson();
//	    Map<String,String> map = gson.fromJson(src, Map.class);
//	    String key = (String) map.keySet().toArray()[0];
//	    String value = map.get(key);
//	    gson.fromJson(key);
//	}

    @SuppressWarnings("rawtypes")
    public static void main(String[] args) {
	    Map<Pair,String> map = new HashMap<Pair,String>();
	    Pair<String,String> pair = new Pair<String,String>("a","b");
	    map.put(pair,"key");
	    
	    Type mType = new TypeToken<Map<Pair<String,String>,String>>() {}.getType();
	    Gson gson = new Gson();
	    JsonElement je = gson.toJsonTree(map,mType);
        System.out.println(je);
    }

}
