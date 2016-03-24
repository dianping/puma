### Puma 设计

![Design](/docs/design.png)

* 数据库层面和以前保持一致，无需变化。
* 每个数据源会至少分配2台 Puma Server，并保持独立运行，如果当前连接的实例挂了，会自动连接同一个集群下的其它实例。
* Puma Client 会选择一个 Puma Server 连接，如果当前连接的 Puma Server 挂了，会选择另一台连接。
* Puma Client 可以跑一个多两个，底层会用分布式锁保证同时只有一个在运行，其中一个挂了，另一个得到锁后会开始运行。

&nbsp;

### 核心技术

Puma Server 会将自己伪装成一个 Mysql Slave，通过 Mysql 主从同步协议拉到数据。

Puma Client 底层利用 Http 协议和 Puma Server 通讯，拉取数据。

&nbsp;

### 多语言客户端

因为 Puma Client 底层使用 Http 协议，所以为实现多语言客户端提供了方便。

目前只完成了 Java 客户端，后续根据需求，可以实现各个语言的版本。

