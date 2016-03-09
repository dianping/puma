package com.dianping.puma.common.utils;

import java.util.Date;

/**
 * Created by xiaotian.li on 16/3/9.
 * Email: lixiaotian07@gmail.com
 */
public class Clock {

    public Date getTime() {
        return new Date();
    }

    public Long getTimestamp() {
        return new Date().getTime() / 1000;
    }
}
