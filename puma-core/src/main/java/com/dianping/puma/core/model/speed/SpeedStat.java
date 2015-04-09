package com.dianping.puma.core.model.speed;

import java.util.Date;

public class SpeedStat {

	private boolean init = false;

	private double value;

	private Date time;

	private double speedPerMilliSecond;

	private double speedPerSecond;

	private double speedPerMinute;

	private double speedPerHour;

	public void record(double value, Date time) {
		if (!init) {
			// First record.
			init = true;
			this.speedPerMilliSecond = 0.0;
			this.speedPerSecond = 0.0;
			this.speedPerMinute = 0.0;
			this.speedPerHour = 0.0;
		} else {
			long timeDelta = time.getTime() - this.time.getTime();
			if (timeDelta != 0) {
				speedPerMilliSecond = (value - this.value) / timeDelta;
				speedPerSecond = speedPerMilliSecond / 1000;
				speedPerMinute = speedPerMinute / 60;
				speedPerMinute = speedPerMinute / 24;
			}
		}

		this.value = value;
		this.time = time;
	}

	public double getSpeedPerMilliSecond() {
		return speedPerMilliSecond;
	}

	public double getSpeedPerSecond() {
		return speedPerSecond;
	}

	public double getSpeedPerMinute() {
		return speedPerMinute;
	}

	public double getSpeedPerHour() {
		return speedPerHour;
	}
}
