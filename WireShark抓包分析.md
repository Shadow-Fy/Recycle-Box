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





## TCP

### 数据包

![image-20230526154127678](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305261541744.png)

**Seq：**表示该数据段的序号

+ TCP提供有序的传输，所以每个数据段都要标上一个序号。我们不需要知道 Seq 起始号值是怎么出来的，但是需要知道他的增长方式：数据段 1 的起始 Seq = 1， 长度为 1448（意味着他包含了 1448 个字符），那么数据段 2 的 Seq 号就为 1 + 1448 = 1449，同理可得数据段 3 的 Seq

| 数据段 1 | 数据段 2     | 数据段 3   |
| -------- | ------------ | ---------- |
| Seq = 1  | Seq = 1449   | Seq = 2897 |
| n = 1448 | LeLen = 1448 | Len = 1448 |



+ 一个 Seq 号的大小是根据上一个数据段的 Seq 号和长度相加而来的，由于TCP是双向的，在一个连接中双方都可以是发送方，所以各自负责维护自己的Seq号。1059号包和1062号包都是由 120.39.212.79 维护，由于1059号包的 Seq = 22149， Len = 1380，所以1062号包的 Seq = 22149 + 1380 = 23529

**Len：**该数据段的长度，这个长度不包括 TCP 头。虽然上图中 1058 号包 Len = 0 ，但其实是有TCP头的，头部本身携带很多信息，所以 Len = 0 不代表没有意义。

**Ack**：确认号，如上图中的 1059 号包 Ack = 1897 ，接收方向发送方确认已经收到了哪些字节。

+ 比如甲发送了“Seq ：x， Len：y”的数据段给乙，那乙恢复的确认号就是 x + y ，这意味着它收到了 x + y 之前的所有字节。 1058 号包的 Seq = 1897 ， Len = 0 ，所以来自接收方的 1059 号包的 Ack = 1897 + 0 = 1897 ， 表示收到了 1897 之前的所有字节。理论上，接收方回复的 Ack 号恰好就等于发送方下一个 Seq 号，可以看到 1058 号包的 Ack 号和 1059 号包的 Seq号相等。
+ 在一个TCP连接中，因为双方都可以是接收方，所以它们维护各自的 Ack 号， 例如 1059 号包以及之后几个包 Ack 都是 1897 没有变更



除此之外，TCP 头还附带了很多标志位，在 WireShark上经常可以看到下面这些：

+ **SYN：**携带这个标志的包表示正在发起连接请求。因为连接是双向的，所以建立连接时，双方都要发送一个 SYN。
+ **FIN：**携带这个标志的包表示正在请求终止连接。因为连接是双向的，所以彻底关闭一个连接时，双方都要发一个 FIN。
+ **RST：**用于重置一个混乱的连接，或者拒绝一个无效的请求。



### 三次握手

![image-20230526130801399](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305261308435.png)

**连接过程：**

1. 客户端发送一个SYN=1，ACK=0标志的数据包给服务端，请求进行连接，这是第一次握手；
2. 服务端收到请求并且允许连接的话，就会发送一个SYN=1，ACK=1标志的数据包给发送端，告诉它，可以通讯了，并且让客户端发送一个确认数据包，这是第二次握手；
3. 服务端发送一个SYN=0，ACK=1的数据包给客户端端，告诉它连接已被确认，这就是第三次握手。TCP连接建立，开始通讯。

下图中 161 号包到 163号包即 3 次握手

![image-20230526201354281](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305262013414.png)



## TLSv2

在浏览器访问网址时会出现 TLSv1.2 协议的包，分析一下这个是来做什么的：

> 正常来说三次握手之后应该显示的是 HTTP 协议的包，而现在我们的浏览器随便打开一个网址你会发现是 HTTPS ，HTTP是明文传输的协议，可能受到第三方的攻击，非常不安全，因此诞生了 HTTPS
>
> 这个 “S“ 表示的是 SSL/TLS 协议，用公式说明就是 HTTPS = HTTP + SSL(TLS)
>
> 其中 SSL 即安全套接层，处于 OSI 七层模型中的会话层
>
> 通过抓包会发现，HTTPS 首次通信一共需要握手 7 次， TCP 三次握手和之后的 TLS四次握手

SSL/TLS是一种密码通信框架，他是世界上使用最广泛的密码通信方法。SSL/TLS综合运用了密码学中的对称密码，消息认证码，公钥密码，数字签名，伪随机数生成器等，可以说是密码学中的集大成者。

为了防止网络犯罪分子通过互联网访问敏感数据，人们引入了各种加密协议。TLS(Transport Layer Security) 安全传输层协议，是 SSL(Secure Socket Layer) 安全套接层协议的后续版本。

![img](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305271744362.webp)

> （可以作为一个了解）
>
> TLS的三个作用
> （1）身份认证
> 通过证书认证来确认对方的身份，防止中间人攻击
> （2）数据私密性
> 使用对称性密钥加密传输的数据，由于密钥只有客户端/服务端有，其他人无法窥探。
> （3）数据完整性
> 使用摘要算法对报文进行计算，收到消息后校验该值防止数据被篡改或丢失。

### HTTPS 建立连接——四次握手

![](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202305281253155.png)

1. 浏览器给出协议版本号、一个客户端生成的随机数（Client random），以及客户端支持的加密方法。
2. 服务器确认双方使用的加密方法，使用的tls版本号和一个随机数。
3. 并给出数字证书、以及一个服务器运行Diffie-Hellman算法生成的参数，比如pubkey。
4. 浏览器获取服务器发来的pubkey，计算出另一个pubkey，发给服务器。
5. 服务器发给浏览器一个session ticket。



用浏览器访问 www.baidu.com 然后进行抓包测试一下，网址的 IP 地址可以使用 cmd 的 ping 命令找到（120.232.145.144 需要自己 ping 一下才知道，因为可能会有变化），然后通过过滤器查看所有源IP地址或目的IP地址是 120.232.145.144 并且是 TLSv1.2 协议的数据包

![image-20230602124302400](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021243507.png)



#### 第一次握手：

##### Client Hello：

客户端（浏览器）发送 `Client Hello` 消息给服务器，进行打招呼，并且提供一些信息。*（ 984 号包）*

> 1. 支持的协议版本，比如 TLSv1.2 。（Version）
> 2. 客户端生成的随机数，用于加密 (Random)
> 3. 用于复用 TLS 连接，防止资源的浪费。但这个要服务端支持才行。（Session ID)
> 4. 客户端支持的密码学套件，按客户端偏好排序，如果服务端没有可支持的，那就回应错误（returns a handshake failure alert）并关闭连接。(Cipher Suites)
> 4. 客户端支持的压缩方法（Compression Methods）

![image-20230602141103828](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021411882.png)



#### 第二次握手：

##### Server Hello：

服务器收到客户端的消息 `Client Hello` 后，回复消息 `Server Hello` 消息。*（ 1011 号包）*

> 1. 服务器确认支持客户端的 TLS 版本（Version）
> 2. 服务器生成的随机数（Random）
> 3. 服务端返回的会话 ID（Session ID）
>    + 如果客户端刚刚发过来的 session_id 服务端已经有了缓存，并且同意复用连接，则返回一个和客户端刚刚发来的相同的 session_id。
>    + 也可以发送一个新的 session_id，以便客户端下次将其携带并且复用。
>    + 也可以回复一个空值，表示不缓存 session_id，因此也不会复用。
> 4. 服务器从客户端发来的加密套件列表中选出一个最合适的加密组合（Cipher Suite）
>    + 用 ECDHE_RSA 作为秘钥交换算法，用 AES_128 作为通信时的对称加密算法，用 SHA256 作为哈希算法。 
> 3. 选择的压缩算法

![image-20230602142135934](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021421986.png)



##### Certificate：

随后服务器为了证明身份，会给客户端发送数字证书，即 `Certificate` 消息 *（ 1014 号包）*

> 1. 证书版本号 (version)
> 2. 证书序列号，这个每个颁发机构是唯一的（serialNumber）
> 3. 签名算法（signature）
>    + 表示用 sha256 这个哈希算法对证书进行哈希生成摘要，然后再用 RSA 这个非对称加密算法，用 CA 的私钥加密刚刚生成的摘要，形成数字签名。
> 4. 颁发者信息
> 5. 证书有效期
> 6. 证书持有者信息
> 7. 证书公钥

![image-20230602143818987](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021438044.png)



##### Server Key Exchange:

用于生成 `Premaster secret` *（ 1014 号包）*

![image-20230602150011181](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021500229.png)

##### Server Hello Done：

最后，服务器发送 `Server Hello Done` 消息给客户端，只是通知客户端第二次握手完毕*（ 1014 号包）*

![image-20230602132243288](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021322330.png)



#### 第三次握手：

##### Client Key Exchange:

用于生成 `premaster secret` ，同之前的 `ServerKeyExchange` 配合使用的。*（1036 号包）*

客户端给服务端一个用于 ECDHE 算法的公钥：0427282f4fc27c8ec96448af2e38d5af72a4a9b9dfe7e1744a37cdae2bd621ccdec878f1f964222208346bff89e6d23f56c9465225471d4e0425a6a7728a0201dc

> 在 `Client Hello` 和 `Server Hello` 中生成打的两个随机数，加上 ECDHE 算法得到的公钥一起计算出最终的对称加密秘钥 master_secret
>
> 一旦双方协商出来了这个相同的对称秘钥，那就可以开始愉快地安全通信了，TLS 层的工作也就圆满完成

![image-20230602151530913](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021515959.png)

##### 

##### Change Cipher Spec:

秘钥改变通知，客户端通过 `Change Cipher Spec` 消息将`Master Secret` 对称密钥发送给服务器，并通知服务器开始使用对称加密的方式进行通信。*（1036 号包）*

> 在此之前的握手消息都是明文的，但只要出现了"Change Cipher Spec" 消息，之后的握手消息就都是密文了，wireshark抓到的数据包也会是乱码。

![image-20230602160037917](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021600961.png)



##### Encrypted Handshake Message:

最后，客户端发送 `Encrypted Handshake Message` 消息，将之前发送的所有数据做成摘要，使用`Master Secret` 对称密钥加密（这条消息已经是对称加密），供服务器验证之前握手过程中的数据是否被其他人篡改。*（1036 号包）*

![image-20230602163445992](https://shadow-fy.oss-cn-chengdu.aliyuncs.com/img/202306021634045.png)



#### 第四次握手：

##### Change Cipher Spec :

  服务器在收到客户端的 Client Key Exchange 消息后，使用RSA私钥对其解密，得到客户端生成的随机数 PreMaster，至此服务器也拥有了与客户端相同的三个随机数：Client Random、Server Random、PreMaster，服务器也使用这三个随机数计算对称密钥，将计算后的结果通过 Change Cipher Spec 消息返回给客户端。
（RSA非对称加密的作用就在于 对第三个随机数"PreMaster"的加密，前面两个随机数都是公开的）



##### Encrypted Handshake Message :

  服务器通过 Encrypted Handshake Message 消息将之前握手过程中的数据生成的摘要 使用对称密钥加密后 发给 客户端，供客户端进行验证。至此TLS四次握手完毕。

