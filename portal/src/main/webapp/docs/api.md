### 项目简介

随着网站业务不断发展，各业务对数据的实时性，数据库的可用性要求越来越高。

本系统可以实时获得数据库的变更并通过消息方式发布出来，供各业务线订阅。

同时，本系统还会实现数据库同步（同构和异构），以满足数据库冗余备份，数据迁移的需求。

**企业QQ群：249942006**

&nbsp;

### 项目依赖

```
<dependency>
    <groupId>com.dianping.puma</groupId>
    <artifactId>puma-client</artifactId>
    <version>${version}</version>
</dependency>
```

最新版本为`2.0.0`

&nbsp;

### 接入申请
发邮件给`yuyang.gong@dianping.com`和`xiaotian.li@dianping.com`。

未申请的 ClientName 无法正常使用。

邮件标题：PumaClient 申请

邮件内容：

* ClientName
* 需要监听的库和表
* 上线时间

&nbsp;

**申请完成在本地开发时，可以用`test`结尾的`ClientName`来做开发，避免相互影响。**

&nbsp;

### 使用方法简介

```
PumaClient client = new PumaClientConfig()
	.setClientName("your-client-name")
	.setDatabase("database")
	.setTables(Lists.newArrayList("table0", "table1"))
	.buildClusterPumaClient();

while(!Thread.currentThread().isInterrupted()) {
	try {
		BinlogMessage binlogMessage = client.get(10, 1, TimeUnit.SECOND);
		//todo: 处理数据
		client.ack(binlogMessage.getLastBinlogInfo());
	} catch(Exception e) {
		//这里的异常主要是用来打点的，便于及时发现问题
	}
}
```

`PumaClient`所有操作都是同步操作，并且线程不安全。

**如果是`job`项目可以直接写上述代码。但是如果是在`service`或者`web`项目中，一定要新启一个线程来跑上述代码。**

&nbsp;

### PumaClientConfig API && PumaClient API

代码即是文档。

建议直接在项目中看源码，或者到这里看源码：

* [PumaClientConfig](http://code.dianpingoa.com/arch/puma/blob/master/puma-client/src/main/java/com/dianping/puma/api/PumaClientConfig.java)
* [PumaClient](http://code.dianpingoa.com/arch/puma/blob/master/puma-client/src/main/java/com/dianping/puma/api/PumaClient.java)

&nbsp;

### 最佳实践
```
import com.dianping.cat.Cat;
import com.dianping.puma.api.PumaClient;
import com.dianping.puma.api.PumaClientConfig;
import com.dianping.puma.api.PumaClientException;
import com.dianping.puma.core.dto.BinlogMessage;
import com.dianping.puma.core.event.Event;
import com.dianping.puma.core.event.RowChangedEvent;
import com.google.common.collect.Lists;

import java.util.concurrent.TimeUnit;

public class Example1 {

    /**
     * 假设这是一个 web 或 service 项目
     * 可以尝试运行多个,并随机关掉其中正在运行的那个,来模拟 failover
     *
     * @param args
     */
    public static final void main(String... args) {

        //不要阻塞主线程,需要自己令起线程
        Thread pumaClientThread = new Thread(new Runnable() {
            @Override
            public void run() {

                PumaClient client = new PumaClientConfig()
                        .setClientName("puma-client-example-test")
                        .setDatabase("Puma")
                        .setTables(Lists.newArrayList("PumaServer"))
                        .buildClusterPumaClient();

                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        BinlogMessage message = client.get(100, 1, TimeUnit.SECONDS);

                        for (Event event : message.getBinlogEvents()) {
                            if (event instanceof RowChangedEvent) {

                                RowChangedEvent rowChangedEvent = (RowChangedEvent) event;
                                System.out.println(rowChangedEvent.toString());

                                //todo: 处理数据
                            }
                        }

                        client.ack(message.getLastBinlogInfo());
                    } catch (PumaClientException e) {
                        Cat.logError(e.getMessage(), e);
                    }
                }
            }
        });


        pumaClientThread.setName("puma-client-example-test");
        pumaClientThread.setDaemon(true);
        pumaClientThread.start();


        while (true) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ignore) {
            }
        }
    }
}
```

&nbsp;

### FAQ

#### PumaClient 如何双机部署

`PumaClient`只支持 failover，不支持 loadbalance。

如果你担心自己的程序因为 bug 或者上线部署而停止，可以双机部署`PumaClient`，它们的`ClientName`必须相同。两台机器同时启动时，只有一台会收到数据，另外一台会阻塞。当其中一台挂掉后，另一台会开始消费数据。

更多细节可以看设计文档。

&nbsp;

#### ack 方法有什么用

`PumaClient`的同步进度会通过`ack`方法同步到云端，重启时，会读取最后一次`ack`的位置来进行同步。

&nbsp;

#### rollback 方法有什么用
`rollback`方法可以用来强制定位后续数据的起始位置。

例如 DW 团队不需要实时监听数据，而是需要在每天凌晨同步昨天的数据，那么可以在启动的时候调用`rollback`方法将任务的起始位置定位到昨天0点，然后开始同步。

使用方法是：`client.rollback(new BinlogInfo().setTimestamp(1447800000))`

备注：这里的`timestamp`是秒数，不是毫秒数。(`mysql`底层`binlog`只精确到秒)