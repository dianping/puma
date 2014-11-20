package com.dianping.puma.syncserver.job.executor.failhandler;

public interface Handler {

    /** 返回该handler的名称 */
    String getName();

    /** 处理 */
    HandleResult handle(HandleContext context);

}
