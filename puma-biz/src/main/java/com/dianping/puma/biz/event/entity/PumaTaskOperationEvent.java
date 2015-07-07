package com.dianping.puma.biz.event.entity;


import com.dianping.puma.biz.entity.PumaTask;

public class PumaTaskOperationEvent extends TaskOperationEvent {

	PumaTask pumaTask;

	PumaTask oriPumaTask;

	public PumaTask getPumaTask() {
		return pumaTask;
	}

	public void setPumaTask(PumaTask pumaTask) {
		this.pumaTask = pumaTask;
	}

	public PumaTask getOriPumaTask() {
		return oriPumaTask;
	}

	public void setOriPumaTask(PumaTask oriPumaTask) {
		this.oriPumaTask = oriPumaTask;
	}
}
