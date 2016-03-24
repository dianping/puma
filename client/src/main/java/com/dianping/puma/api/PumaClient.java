package com.dianping.puma.api;

import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.model.BinlogInfo;

import java.util.concurrent.TimeUnit;

/**
 * 使用示例：
 * PumaClient client = new PumaClientConfig()
 *    .setClientName("your-client-name")
 *   .setDatabase("database")
 *   .setTables(Lists.newArrayList("table0", "table1"))
 *   .buildClusterPumaClient();
 *
 * while(!Thread.interrupted()) {
 *       try {
 *          BinlogMessage binlogMessage = client.get(10, 1, TimeUnit.SECOND);
 *          // 处理数据
 *          client.ack(binlogMessage.getLastBinlogInfo());
 *       } catch(Exception e) {
 *          // 这里的异常主要是用来打点的，便于及时发现
 *       }
 *   }
 */
public interface PumaClient {

    /**
     * 获得一批数据
     * 该方法会一直等待,直到服务器新数据量达到 batchSize 后返回
     * @param batchSize
     * @return 满足 batchSize 的数据
     * @throws PumaClientException
     */
    BinlogMessage get(int batchSize) throws PumaClientException;

    /**
     * 获得一批数据，并设置超时时间
     * 如果超过超时时间或者达到 batchSize,该方法就会返回。
     * @param batchSize
     * @param timeout
     * @param timeUnit
     * @return 有多少新数据就返回多少新数据
     * @throws PumaClientException
     */
    BinlogMessage get(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

    /**
     * 得到数据后自动 ack,使用该方法可能会在极少数情况下丢失数据
     * 只有当不太关心数据准确性的情况下才使用
     * @param batchSize
     * @return
     * @throws PumaClientException
     */
    BinlogMessage getWithAck(int batchSize) throws PumaClientException;

    /**
     * 得到数据后自动 ack,使用该方法可能会在极少数情况下丢失数据
     * 只有当不太关心数据准确性的情况下才使用
     * @param batchSize
     * @param timeout
     * @param timeUnit
     * @return
     * @throws PumaClientException
     */
    BinlogMessage getWithAck(int batchSize, long timeout, TimeUnit timeUnit) throws PumaClientException;

    /**
     * 通知服务器客户端当前最后处理完成的数据位置，遇到问题重新连接后，会从这个位置开始继续接收数据
     * @param binlogInfo
     * @throws PumaClientException
     */
    void ack(BinlogInfo binlogInfo) throws PumaClientException;

    /**
     * 回滚到指定位置重新开始,下一次调用 get 的时候会从该位置开始
     * @param binlogInfo 可以指定 binlog 位置或直接指定时间
     * @throws PumaClientException
     */
    void rollback(BinlogInfo binlogInfo) throws PumaClientException;
}
