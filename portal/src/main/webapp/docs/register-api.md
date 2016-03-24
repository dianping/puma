Puma客户端在使用之前需要注册,注册的目的是便于运维和管理.  
我们同时也提供了客户端注册的接口,以满足大批量客户端注册的需求.

**普通用户客户端注册仍然走邮件申请流程,对于使用接口注册的用户,也请事先和我们交流.**

&nbsp;

#### 查询一个Puma客户端

* 线上环境 `GET` `http://puma.dp/a/clients`
* PPE环境 `GET` `http://ppe.puma.dp/a/clients`
* Beta环境 `GET` `http://beta.puma.dp/a/clients`

&nbsp;

JSON Response Body如下:
```
{
    clientName: "client_a",       // 客户端名称(一般以小写字母,数字和下划线组成,如"my_puma_client")
    groupName: "group_a",         // 客户端组名(一般以小写字母,数字和下划线组成,如"my_puma_group")
    timestamp: 1457539200         // 订阅起始时间戳(如果需要订阅最新日志,请将timestamp设为一个未来的时间)
    serverId: 12000123,           // Mysql服务器server id(普通用户请略过)
    filename: "mysql-bin.000001"  // Mysql服务器起始日志文件名(普通用户请略过)
    position: 4                   // Mysql服务器起始日志文件位置(普通用户请略过)
}
```

&nbsp;

#### 注册一个Puma客户端

* 线上环境 `POST` `http://puma.dp/a/clients`
* PPE环境 `POST` `http://ppe.puma.dp/a/clients`
* Beta环境 `POST` `http://beta.puma.dp/a/clients`

&nbsp;

JSON Request Body如下:
```
{
    clientName: "client_a",       // 客户端名称(一般以小写字母,数字和下划线组成,如"my_puma_client")
    groupName: "group_a",         // 客户端组名(一般以小写字母,数字和下划线组成,如"my_puma_group")
    timestamp: 1457539200         // 订阅起始时间戳(如果需要订阅最新日志,请将timestamp设为一个未来的时间)
    serverId: 12000123,           // Mysql服务器server id(普通用户请略过)
    filename: "mysql-bin.000001"  // Mysql服务器起始日志文件名(普通用户请略过)
    position: 4                   // Mysql服务器起始日志文件位置(普通用户请略过)
}
```

&nbsp;

#### 修改一个Puma客户端

* 线上环境 `PUT` `http://puma.dp/a/clients/{clientName}`
* PPE环境 `PUT` `http://ppe.puma.dp/a/clients/{clientName}`
* Beta环境 `PUT` `http://beta.puma.dp/a/clients/{clientName}`

&nbsp;

JSON Request Body如下:
```
{
    clientName: "client_a",       // 客户端名称(一般以小写字母,数字和下划线组成,如"my_puma_client")
    groupName: "group_a",         // 客户端组名(一般以小写字母,数字和下划线组成,如"my_puma_group")
    timestamp: 1457539200         // 订阅起始时间戳(如果需要订阅最新日志,请将timestamp设为一个未来的时间)
    serverId: 12000123,           // Mysql服务器server id(普通用户请略过)
    filename: "mysql-bin.000001"  // Mysql服务器起始日志文件名(普通用户请略过)
    position: 4                   // Mysql服务器起始日志文件位置(普通用户请略过)
}
```

&nbsp;

#### 删除一个Puma客户端

* 线上环境 `DELETE` `http://puma.dp/a/clients/{clientName}`
* PPE环境 `DELETE` `http://ppe.puma.dp/a/clients/{clientName}`
* Beta环境 `DELETE` `http://beta.puma.dp/a/clients/{clientName}`

&nbsp;