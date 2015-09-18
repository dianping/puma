概述
--------

## 项目背景：

*  随着网站业务不断发展，各业务对数据的实时性，数据库的可用性要求越来越高。
*  本系统可以实时获得数据库的变更并通过消息方式发布出来，供各业务线订阅。
*  同时，本系统还会实现数据库同步（同构和异构），以满足数据库冗余备份，数据迁移的需求。

## 项目主要完成的功能：

*  实时获得数据库变化
*  实时发布各个数据库变化事件
*  多个不同MySQL实例到一个MySQL实例的数据同步
*  异构数据表同步

## 准备工作
1. Java 1.6
2. Pom文件中加入依赖

```
<dependency>
    <groupId>com.dianping.puma</groupId>
    <artifactId>puma-client</artifactId>
    <version>${version}</version>
</dependency>
```

最新版本为`2.0.0`

## 使用实例

### Single Client
```java
PumaClient client = new PumaClientConfig()
	.setClientName("name")
	.setDatabase("database")
	.setTables(Lists.newArrayList("table0", "table1"))
	.buildClusterPumaClient();

while(true) {
	try {
		BinlogMessage binlogMessage = client.get(10, 1, TimeUnit.SECOND);
		// Do business logic.
		// ...
		client.ack(binlogMessage.getBinlogInfo());
	} catch(Throwable t) {
		// Error handling.
	}
}
```

### Distributed Clients
```java
PumaClient client = new PumaClientConfig()
	.setClientName("name")
	.setDatabase("database")
	.setTables(Lists.newArrayList("table0", "table1"))
	.buildClusterPumaClient();

PumaClientLock lock = new PumaClient("name");
try {
	while (!Thread.interrupted()) {
		if (!lock.isLocked()) {
			lock.lock();
		}

		try {
			BinlogMessage message = client.get(size, 1, TimeUnit.SECONDS);
			client.ack(message.getLastBinlogInfo());
		} catch (Throwable e) {
			// Error handling.
		}
	}
} catch (Throwable t) {
	// Error handling.
} finally {
	lock.unlockQuietly();
}
```

## Document

### PumaClient

#### get(int batchSize)

Gets batch of binlog events from puma server.

**Arguments**

* batchSize `int` - number of binlog events in a batch.

**Return**

* binlogMessage `BinlogMessage` - batch of binlog events.

#### get(int batchSize, long timeout, TimeUnit timeUnit)

Gets batch of binlog events from puma server in a given time.

**Arguments**

* batchSize `int` - number of binlog events in batch.
* timeout `long` - timeout for getting binlog events.
* timeUnit `TimeUnit` - time unit for timeout

#### getWithAck(int batchSize)

Gets batch of binlog events from puma server and acknowledges back automatically.

**Arguments**

* batchSize `int` - number of binlog events in batch.

#### getWithAck(int batchSize, long timeout, TimeUnit timeUnit)

Gets batch of binlog events from puma server in a given time and acknowledges back automatically if necessary.

**Arguments**

* batchSize `int` - number of binlog events in batch.
* timeout `long` - timeout for getting binlog events.
* timeUnit `TimeUnit` - time unit for timeout

#### ack(BinlogInfo binlogInfo)

Acknowledges back to puma server manually.

**Arguments**

* binlogInfo `BinlogInfo` - object contains binlog position info.

#### rollback()

Rolls back to the latest acknowledge binlog position.

#### rollback(BinlogInfo binlogInfo)

Rolls back to the given binlog position.

**Arguments**

* binlogInfo `BinlogInfo` - binlog position to be rolled back.


### BinlogMessage

#### getBinlogEvents()

**Return**

* `List<Event>` - list of binlog events.

#### size()

**Return**

* `int` - number of binlog events.


### PumaClientLock

#### lock()

Acquires the puma client distributed lock.

#### tryLock()

Acquires the puma client distributed lock only if it is free at the time of invocation.

**Return**

* `boolean` - lock was acquired or not.

#### tryLock(long time, TimeUnit timeUnit)

Acquires the puma client distributed lock if it is free within the given waiting time
and the current thread has not been interrupted.

**Return**

* `boolean` - lock was acquired or not.

#### unlock()

Releases the puma client distributed lock.

#### unlockQuietly()

Releases the puma client distributed lock quietly.

#### isLock()

Queries whether the puma client distributed lock hold is lost or not.

**Return**

* `boolean` - lock hold is lost or not.

