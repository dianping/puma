/**
 * Project: puma-syncserver
 * 
 * File Created at 2013-1-17
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
package com.dianping.puma.syncserver.job.executor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Leo Liang
 * 
 */
public class NO_TaskExecutionResult implements Serializable {

    private static final long   serialVersionUID = 5195419929153387418L;
    private ReturnCode          returnCode;
    private String              msg;
    private Map<String, String> returnParams;

    public NO_TaskExecutionResult() {
    }

    /**
     * @return the returnCode
     */
    public ReturnCode getReturnCode() {
        return returnCode;
    }

    /**
     * @param returnCode
     *            the returnCode to set
     */
    public void setReturnCode(ReturnCode returnCode) {
        this.returnCode = returnCode;
    }

    /**
     * @return the msg
     */
    public String getMsg() {
        return msg;
    }

    /**
     * @param msg
     *            the msg to set
     */
    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * @return the returnParams
     */
    public Map<String, String> getReturnParams() {
        return returnParams;
    }

    /**
     * @param returnParams
     *            the returnParams to set
     */
    public void setReturnParams(Map<String, String> returnParams) {
        this.returnParams = returnParams;
    }

    public NO_TaskExecutionResult(ReturnCode returnCode, String msg, Map<String, String> returnParams) {
        this.returnCode = returnCode;
        this.msg = msg;
        this.returnParams = returnParams;
    }

    @Override
    public String toString() {
        return "TaskExecutionResult [returnCode=" + returnCode + ", msg=" + msg + ", returnParams=" + returnParams
                + "]";
    }

    public static enum ReturnCode {
        SUCCESS, FAIL;
    }

}
