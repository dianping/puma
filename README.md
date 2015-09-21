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


## API文档

### PumaClientConfig(com.dianping.puma.api.PumaClientConfig)

使用PumaClientConfig类来构造Puma客户端。
构造Puma客户端设置：
1. 客户端名称（必须）
2. 监听数据库名称（必须）
3. 监听数据库表名称（必须）
4. 是否需要DML事件（可选，默认为true）
5. 是否需要DDL事件（可选，默认为false）
6. 是否需要transaction事件（可选，默认为false）

#### PumaClientConfig setClientName(String clientName)
设置Puma客户端的名称。不同的客户端需使用不同的名称。
*参数*
* clientName `String` - 客户端名称
*返回*
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setDatabase(String database)
设置Puma客户端需要监听的数据库名称。每个客户端只能监听一个数据库。
*参数*
* database `String` - 监听数据库名称
*返回*
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setTables(List<String> tables)
设置Puma客户端需要监听的数据库表的名称列表。每个客户端可以监听一个数据库下的任意多张表。
*参数*
* tables `List<String>` - 监听数据库表名称列表
*返回*
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setDml(boolean dml)
设置Puma客户端是否需要所监听库表的DML（Data Manipulation Language）事件。
*参数*
* dml `boolean` - 是否需要DML事件
*返回*
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setDdl(boolean ddl)
设置Puma客户端是否需要所监听库表的DDL（Data Definition Language）事件。
*参数*
* ddl `boolean` - 是否需要DDL事件
*返回*
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setTransaction(boolean transaction)
设置Puma客户端是否需要所监听库表的Transaction（begin，commit）事件。
*参数*
* transaction `boolean` - 是否需要transaction事件
*返回*
* `PumaClientConfig` - Puma客户端配置

#### PumaClient buildClusterPumaClient()
根据PumaClientConfig的配置创建具备ha功能的Puma客户端。
*返回*
* `PumaClient` - Puma客户端

***

### PumaClient

#### BinlogMessage get(int batchSize)

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

