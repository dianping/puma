package com.dianping.puma.storage;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

public class Sequence {
	private int				creationDate;
	private int				number;
	private AtomicInteger	offset	= new AtomicInteger(0);

	Sequence(int creationDate, int number) {
		this(creationDate, number, 0);
	}

	public Sequence(int creationDate, int number, int offset) {
		this.creationDate = creationDate;
		this.number = number;
		this.offset = new AtomicInteger(offset);
	}

	public Sequence(long seq) {
		parse(seq);
	}

	public void clearOffset() {
		this.offset.set(0);
	}

	public int getCreationDate() {
		return creationDate;
	}

	public void addOffset(int inc) {
		offset.addAndGet(inc);
	}

	public Sequence clone() {
		return new Sequence(creationDate, number, offset.intValue());
	}

	public Sequence getNext(boolean forSameDay) {
		if (forSameDay) {
			return new Sequence(creationDate, number + 1);
		} else {
			Calendar cal = Calendar.getInstance();

			cal.set(2000 + creationDate / 10000, (creationDate % 10000) / 100 - 1, creationDate % 100);
			cal.add(Calendar.DATE, 1);

			int date = (cal.get(Calendar.YEAR) - 2000) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100
					+ cal.get(Calendar.DATE);
			return new Sequence(date, 0);
		}
	}

	public int getNumber() {
		return number;
	}

	public int getOffset() {
		return offset.intValue();
	}

	public long longValue() {
		return ((long) creationDate << 46) | ((long) (number & 0x3FFF)) << 32 | offset.longValue();
	}

	private void parse(long seq) {
		creationDate = (int) (seq >>> 46);
		number = (int) ((seq >>> 32) & 0x3FFF);
		offset.set((int) (seq & 0xFFFFFFFF));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Sequence [creationDate=" + creationDate + ", number=" + number + ", offset=" + offset + "]";
	}

}
