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

```java
PumaClient client = new PumaClientConfig()
	.setClientName("your-client-name")
	.setDatabase("database")
	.setTables(Lists.newArrayList("table0", "table1"))
	.buildClusterPumaClient();

while(!Thread.currentThread().isInterrupted()) {
	try {
		BinlogMessage binlogMessage = client.get(10, 1, TimeUnit.SECOND);
		//Todo: 处理数据
		client.ack(binlogMessage.getBinlogInfo());
	} catch(Exception e) {
		// 这里的异常主要是用来打点的，便于及时发现问题
	}
}
```

## API文档

### PumaClientConfig(com.dianping.puma.api.PumaClientConfig)

***

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
**参数**
* clientName `String` - 客户端名称（如果多台机器启动的 clientName 相同，那么只会有一个能读取到数据，其余会一直等待）
**返回**
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setDatabase(String database)
设置Puma客户端需要监听的数据库名称。每个客户端只能监听一个数据库。
**参数**
* database `String` - 监听数据库名称
**返回**
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setTables(List<String> tables)
设置Puma客户端需要监听的数据库表的名称列表。每个客户端可以监听一个数据库下的任意多张表。
**参数**
* tables `List<String>` - 监听数据库表名称列表
**返回**
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setDml(boolean dml)
设置Puma客户端是否需要所监听库表的DML（Data Manipulation Language）事件。
**参数**
* dml `boolean` - 是否需要DML事件
**返回**
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setDdl(boolean ddl)
设置Puma客户端是否需要所监听库表的DDL（Data Definition Language）事件。
**参数**
* ddl `boolean` - 是否需要DDL事件
**返回**
* `PumaClientConfig` - Puma客户端配置

#### PumaClientConfig setTransaction(boolean transaction)
设置Puma客户端是否需要所监听库表的Transaction（begin，commit）事件。
**参数**
* transaction `boolean` - 是否需要transaction事件
**返回**
* `PumaClientConfig` - Puma客户端配置

#### PumaClient buildClusterPumaClient()
根据PumaClientConfig的配置创建具备ha功能的Puma客户端。
**返回**
* `PumaClient` - Puma客户端
