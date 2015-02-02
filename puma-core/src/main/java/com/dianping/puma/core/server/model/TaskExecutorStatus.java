package com.dianping.puma.core.server.model;

public enum TaskExecutorStatus {
	 WAITING("刚刚创建或加载，未有状态信息"),
     PREPARING("准备运行"),
     RUNNING("运行中"),
     STOPPING("停止中"),
     STOPPED("已停止"),
     FAILED("结束-失败");

     private final String desc;

     private TaskExecutorStatus(String desc) {
         this.desc = desc;
     }

     public String getDesc() {
         return desc;
     }
}
