/**
 * Project: ${puma-common.aid}
 * 
 * File Created at 2012-7-3
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
package com.dianping.puma.storage;

import java.io.IOException;

import com.dianping.puma.common.LifeCycle;
import com.dianping.puma.storage.exception.StorageClosedException;

/**
 * 单个文件存储的抽象
 * 
 * @author Leo Liang
 * 
 */
public interface Bucket extends LifeCycle<IOException> {
    /**
     * 获得当前存储对应的起始sequence(Offset一定为0)
     * 
     */
    public Sequence getStartingSequece();

    /**
     * 往存储中增加一个事件
     * 
     * @throws IOException
     */
    public void append(byte[] data) throws StorageClosedException, IOException;

    /**
     * 从存储中获得下一个事件 <br>
     * 如果没有，则抛出EOFException，否则一直block到读取完一个事件
     * 
     * @return
     * @throws IOException
     */
    public byte[] getNext() throws StorageClosedException, IOException;

    /**
     * 把文件指针移动到某个offset上
     * 
     * @param offset
     * @throws IOException
     */
    public void skip(int offset) throws StorageClosedException, IOException;

    /**
     * 判断当前存储是否还有剩余空间可写
     * 
     * @return
     * @throws IOException
     */
    public boolean hasRemainingForWrite() throws StorageClosedException, IOException;

    /**
     * 获得写入的seq
     * 
     * @return
     */
    public long getCurrentWritingSeq();

    /**
     * 获得BucketFileName
     * 
     * @return
     */
    public String getBucketFileName();

}
