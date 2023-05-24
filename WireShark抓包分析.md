# WireShark抓包分析

## 查看主机IP

在使用WireShark之前先了解一下自己计算机的一些信息

查看自己主机的IP地址，这里用的Win11电脑，直接使用cmd输入 `ipconfig` 查看本机IP信息

![image-20230523190332176](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305231903966.png)



## 抓包

### ping命令

ping命令是个使用频率极高的 **网络诊断工具** ，在Windows、Unix和Linux系统下均适用。它是TCP/IP协议的一部分，**用于确定本地主机是否能与另一台主机交换数据报 **。根据返回的信息，我们可以推断TCP/IP参数设置是否正确以及运行是否正常。需要注意的是，成功与另一台主机进行一次或两次数据报交换并不表示TCP/IP配置就是正确的，必须成功执行大量的数据报交换，才能确信TCP/IP的正确性。



这里有一张图了解一下ping命令的过程

<img src="https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305231958104.png" alt="img"  />

 具体的执行过程：

假设主机A的IP地址是192.168.1.1，主机B的IP地址是192.168.1.2，他们都在同一个子网。那么当你在主机A上运行“ping 192.168.1.2”后，会发生什么呢？

（1）ping命令执行的时候，源主机首先会构建一个ICMP请求数据包，ICMP数据包内包含多个字段。最重要的是两个，第一个是类型字段，对于请求数据包而言该字段为8；另一个是顺序号，主要用于区分连续ping的时候发出的多个数据包。每发出一个数据包，顺序号会自动加1，为了能够计算往返的时间RTT，它会在报文的数据部分插入发送时间。

![image-20230523205908515](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305232059565.png)

（2）由ICMP协议将这个数据包，连同地址192.168.1.2一起交给IP层，IP层将以192.168.1.2作为目的地址，本机IP作为源地址，加上一些其他的信息，构建一个IP数据包。

![img](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305231956316.webp)

（3）接下来，需要加入MAC头，如果在本节ARP映射表中查找出IP地址192.168.1.2所对应的MAC地址，由数据链路层构建一个数据帧，目的地址是IP层传过来的MAC地址，源地址则是本机的MAC 地址；还要加上一些控制信息，依据以太网的介质访问规则，将它们传送出去。

（4）主机B收到这个数据帧后，先检查它的目的MAC地址，并和本机的MAC地址对比，如符合，则接收，否则就丢弃，接收后检查该数据帧，将IP包从数据帧中取出来，交给本机的IP层，同样，IP层检查后，将有用的信息提取出来后交给ICMP协议。

（5）主机B会构建一个ICMP应答包，应答数据包的类型字段为0，顺序号为接收到的请求数据包中的顺序号，然后再发送给主机A

分析：

在规定的时间内，源主机如果没有接收到ICMP的应答包，则说明目标主机不可达。

如果接收到了ICMP的应答包，则说明目标主机可达

此时，源主机会检查，用当前时刻减去该数据包最初从源主机上发出的时刻，就是ICMP数据包的时间延迟



### 使用WireShark

打开WireShark，选择自己使用的电脑使用的网卡，可以看自己IP信息中使用的IP对应的网卡，我这里使用的是WLAN，直接在WireShark中选择WLAN并启动

![image-20230523191125018](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305231911096.png)



启动后，WireShark处于抓包状态中

![image-20230523191245368](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305231912403.png)



用ping命令进行抓包的操作以及分析抓包后的报文

![image-20230523191547624](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305231915680.png)



为防止其他无用数据包影响分析，使用过滤器将数据包列表进行过滤，**只查看ICMP类型的包，并且源IP地址或目的IP地址为本机本机IP地址以及目标IP的数据包**

（目标的IP在使用ping命令时已经有显示，如上图中的 120.232.145.144 ）

![image-20230523211000282](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305232110327.png)

此时已经获取到使用ping命令时对应的数据包

![image-20230523211329941](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305232113000.png)

在cmd中使用刚才的ping命令时，最后会显示一个ping统计信息，“数据包，已发送 = 4，已接收 = 4”，经过过滤后这里一共有8个包，并且4个是发送的（request），4个是接收的（reply）



## 分析

双击第一个 **request类型** 的数据包可以查看详细内容

![image-20230524115905449](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305241159548.png)

这里一共有四条：

+ 第一条是物理层的帧，包括多少字节，在哪个网卡被捕获到的
+ 第二条是Ethernet II，意思是以太网第二层，也就是MAC层，包含源mac地址（src）和目的mac地址（dst）
+ 第三条是IP协议，包含着源IP地址和目标IP地址
+ 第四条是ICMP协议



打开第四条查看ping报文的组成：

![image-20230524121644452](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305241216516.png)`

**type = 8，code = 0** 表示这是一个Ping请求类型

**type = 0，code = 0** 表示这是一个Ping应答类型（另一个reply类型的数据包就是Ping应答）

此外还有其他类型：

| 类型TYPE | 代码CODE | 用途\|描述 Description                                       | 查询类Query | 差错类Error |
| :------- | :------- | :----------------------------------------------------------- | :---------- | :---------- |
| 0        | 0        | Echo Reply——回显应答（Ping应答）                             | x           |             |
| 3        | 0        | Network Unreachable——网络不可达                              |             | x           |
| 3        | 1        | Host Unreachable——主机不可达                                 |             | x           |
| 3        | 2        | Protocol Unreachable——协议不可达                             |             | x           |
| 3        | 3        | Port Unreachable——端口不可达                                 |             | x           |
| 3        | 4        | Fragmentation needed but no frag. bit set——需要进行分片但设置不分片比特 |             | x           |
| 3        | 5        | Source routing failed——源站选路失败                          |             | x           |
| 3        | 6        | Destination network unknown——目的网络未知                    |             | x           |
| 3        | 7        | Destination host unknown——目的主机未知                       |             | x           |
| 3        | 8        | Source host isolated (obsolete)——源主机被隔离（作废不用）    |             | x           |
| 3        | 9        | Destination network administratively prohibited——目的网络被强制禁止 |             | x           |
| 3        | 10       | Destination host administratively prohibited——目的主机被强制禁止 |             | x           |
| 3        | 11       | Network unreachable for TOS——由于服务类型TOS，网络不可达     |             | x           |
| 3        | 12       | Host unreachable for TOS——由于服务类型TOS，主机不可达        |             | x           |
| 3        | 13       | Communication administratively prohibited by filtering——由于过滤，通信被强制禁止 |             | x           |
| 3        | 14       | Host precedence violation——主机越权                          |             | x           |
| 3        | 15       | Precedence cutoff in effect——优先中止生效                    |             | x           |
| 4        | 0        | Source quench——源端被关闭（基本流控制）                      |             |             |
| 5        | 0        | Redirect for network——对网络重定向                           |             |             |
| 5        | 1        | Redirect for host——对主机重定向                              |             |             |
| 5        | 2        | Redirect for TOS and network——对服务类型和网络重定向         |             |             |
| 5        | 3        | Redirect for TOS and host——对服务类型和主机重定向            |             |             |
| 8        | 0        | Echo request——回显请求（Ping请求）                           | x           |             |
| 9        | 0        | Router advertisement——路由器通告                             |             |             |
| 10       | 0        | Route solicitation——路由器请求                               |             |             |
| 11       | 0        | TTL equals 0 during transit——传输期间生存时间为0             |             | x           |
| 11       | 1        | TTL equals 0 during reassembly——在数据报组装期间生存时间为0  |             | x           |
| 12       | 0        | IP header bad (catchall error)——坏的IP首部（包括各种差错）   |             | x           |
| 12       | 1        | Required options missing——缺少必需的选项                     |             | x           |
| 13       | 0        | Timestamp request (obsolete)——时间戳请求（作废不用）         | x           |             |
| 14       |          | Timestamp reply (obsolete)——时间戳应答（作废不用）           | x           |             |
| 15       | 0        | Information request (obsolete)——信息请求（作废不用）         | x           |             |
| 16       | 0        | Information reply (obsolete)——信息应答（作废不用）           | x           |             |
| 17       | 0        | Address mask request——地址掩码请求                           | x           |             |
| 18       | 0        | Address mask reply——地址掩码应答                             |             |             |

**Checksum 校验和**：

校验和是存在于各种报文中的一个字段，它存在的目的是验证报文在网络传输过程中的完整性（有的数据可能在链路传输时发生0-1数据翻转，从而导致报文出错）。因此，在报文的发送端，会根据报文中的首部或数据来计算一个校验和（IP报文的检验和只对首部进行计算，ICMP报文对报文首部和数据都进行计算），然后一旦接收端接受到相应报文，接收端也会对报文的首部或数据进行一次检验和计算，如果接收端算出来的检验和和发送端发送的不一样，那么对不起，接收端认为报文在传输过程中出了错，于是就丢掉该报文。



**identifier 标识符 和 Sequence Number 序号：**

标识符和序列号用于匹配回显应答和回显请求。具体来说，标识符确定该报文的发送者， 而序列号关联对应的请求报文和应答报文(请求报文和其对应的响应报文的序列号相同)。