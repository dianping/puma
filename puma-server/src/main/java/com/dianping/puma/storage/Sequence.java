package com.dianping.puma.storage;

import java.util.Calendar;

/**
 * <p>
 * 文件存储的Sequence的包装类
 * </p>
 * <p>
 * 在存储中实际存储的是Sequence的longValue<br>
 * longValue的组成：
 * <ol>
 * <li>高18位代表创建日期，如120717</li>
 * <li>中间14位代表文件编号，如1,2,3</li>
 * <li>低32位代表文件指针的偏移量</li>
 * </ol>
 * </p>
 * <p>
 * <b>由上可见，每个文件最大不能大于2G</b>
 * </p>
 * 
 * @author Leo Liang
 * 
 */
public class Sequence {
    private int creationDate;
    private int number;
    private int offset;

    public Sequence(int creationDate, int number) {
        this(creationDate, number, 0);
    }

    public Sequence(int creationDate, int number, int offset) {
        this.creationDate = creationDate;
        this.number = number;
        this.offset = offset;
    }

    public Sequence(Sequence sequence) {
        this.creationDate = sequence.creationDate;
        this.number = sequence.number;
        this.offset = sequence.offset;
    }

    public Sequence(long seq) {
        parse(seq);
    }

    public int getCreationDate() {
        return creationDate;
    }

    /**
     * 获得本sequence对应的偏移量增加delta后的sequence的新实例
     * 
     * @param delta
     * @return
     */
    public Sequence addOffset(int delta) {
        return new Sequence(creationDate, number, this.offset + delta);
    }

    /**
     * 获得本sequence对应的偏移量为0的sequence的新实例
     * 
     */
    public Sequence clearOffset() {
        return new Sequence(creationDate, number, 0);
    }

    /**
     * 获得下一个sequence
     * 
     * @param renewDate
     *            是否需要重新生成创建日期
     * @return 如果不renewDate为true，则返回以今天日期为创建日期的0号文件，并且偏移量为0的sequence；
     *         否则返回同一创建日期的下一个编号的并且偏移量为0的文件对应的sequence
     */
    public Sequence getNext(boolean renewDate) {
        if (!renewDate) {
            return new Sequence(creationDate, number + 1);
        } else {
            Calendar cal = Calendar.getInstance();

            int date = (cal.get(Calendar.YEAR) - 2000) * 10000 + (cal.get(Calendar.MONTH) + 1) * 100
                    + cal.get(Calendar.DATE);
            return new Sequence(date, 0);
        }
    }

    public int getNumber() {
        return number;
    }

    public int getOffset() {
        return offset;
    }

    /**
     * 获得对应的long值
     */
    public long longValue() {
        return ((long) creationDate << 46) | ((long) (number & 0x3FFF)) << 32 | offset;
    }

    private void parse(long seq) {
        creationDate = (int) (seq >>> 46);
        number = (int) ((seq >>> 32) & 0x3FFF);
        offset = (int) (seq & 0xFFFFFFFF);
    }

    public static void main(String[] args) {
    	Sequence seq = new Sequence(-7869618084825256538l);
    	System.out.println(seq);
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
