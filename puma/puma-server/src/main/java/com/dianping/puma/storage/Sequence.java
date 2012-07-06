package com.dianping.puma.storage;

import java.util.Calendar;
import java.util.Date;

public class Sequence {
	private Date creationDate;

	private int number;

	private int offset;

	Sequence(Date creationDate, int number) {
		this.creationDate = creationDate;
		this.number = number;
		this.offset = 0;
	}

	public Sequence(long seq) {
		parse(seq);
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Sequence getNext(boolean forSameDay) {
		if (forSameDay) {
			return new Sequence(creationDate, number + 1);
		} else {
			Calendar cal = Calendar.getInstance();

			cal.setTime(creationDate);
			cal.add(Calendar.DATE, 1);

			return new Sequence(cal.getTime(), 0);
		}
	}

	public int getNumber() {
		return number;
	}

	public int getOffset() {
		return offset;
	}

	public long longValue() {
		Calendar cal = Calendar.getInstance();

		cal.setTime(creationDate);

		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		long time = (year - 2000) * 10000 + month * 100 + day;

		return time << 46 | ((long) (number & 0x3FFF)) << 32 | offset;
	}

	private void parse(long seq) {
		int time = (int) (seq >>> 46);
		int year = 2000 + time / 10000;
		int month = (time % 10000) / 100;
		int day = time % 100;
		Calendar cal = Calendar.getInstance();

		cal.set(year, month - 1, day, 0, 0, 0);

		creationDate = cal.getTime();
		number = (int) ((seq >>> 32) & 0x3FFF);
		offset = (int) (seq & 0xFFFFFFFF);
	}
}
