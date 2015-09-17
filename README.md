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
```
		PumaClient client = new PumaClientConfig()
			.setClientName("name") // set your client name.
        	.setDatabase("database") // set database to subscribe.
        	.setTables(Lists.newArrayList("table1, table2, table3")) // set tables to subscribe.
        	.buildClusterPumaClient();
      while (true) {
			try {
				BinlogMessage message = client.get(size, 1, TimeUnit.SECONDS);
				for (Event event : message.getBinlogEvents()) {
					if (event instanceof DdlEvent) {
						if (!ddl) {
							continue;
						}
						// do ddl business logic here.
					} else if (event instanceof RowChangedEvent) {
						RowChangedEvent rowChangedEvent = (RowChangedEvent) event;
						if (rowChangedEvent.isTransactionBegin() || rowChangedEvent.isTransactionCommit()) {
							if (!transaction) {
								continue;
							}
							// do transaction business logic here.
						} else {
							if (!dml) {
								continue;
							}
							if (rowChangedEvent.getDmlType().equals(DMLType.INSERT)) {
								if (!insert) {
									continue;
								}
								Map<String, ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
								for (Map.Entry<String, ColumnInfo> entry : columnInfoMap.entrySet()) {
									String columnName = entry.getKey();
									ColumnInfo columnInfo = entry.getValue();
									Object insertValue = columnInfo.getNewValue();
									// do insert business logic here.
								}
							} else if (rowChangedEvent.getDmlType().equals(DMLType.UPDATE)) {
								if (!update) {
									continue;
								}
								Map<String, ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
								for (Map.Entry<String, ColumnInfo> entry : columnInfoMap.entrySet()) {
									String columnName = entry.getKey();
									ColumnInfo columnInfo = entry.getValue();
									Object oldValue = columnInfo.getOldValue();
									Object newValue = columnInfo.getNewValue();
									// do update business logic here.
								}
							} else if (rowChangedEvent.getDmlType().equals(DMLType.DELETE)) {
								if (!delete) {
									continue;
								}
								Map<String, ColumnInfo> columnInfoMap = rowChangedEvent.getColumns();
								for (Map.Entry<String, ColumnInfo> entry : columnInfoMap.entrySet()) {
									String columnName = entry.getKey();
									ColumnInfo columnInfo = entry.getValue();
									Object deleteValue = columnInfo.getOldValue();
									// do delete business logic here.
								}
							}
						}
					}
				}
				client.ack(message.getLastBinlogInfo());
			} catch (Exception exp) {
				exp.printStackTrace();
			}
		}
```

## Document