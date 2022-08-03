# CodeDemo2022
## 1.并发编程的艺术文章代码
concurrentPro
## 2.工作代码复现
FanXingDemo
## 3.RocketMQ

1. 集群的搭建
2. 生产者(同步发送，异步以及单向发送消息)，消费者(负载均衡，广播模式)
3. 顺序消息生产

**ROCKETMQ问题：**

学习顺序消息的时候，理想效果应该是：一个订单信息在一个队列中，由一个消费线程进行消费；
但实际效果：多个订单信息在一个队列中，由一个消费线程进行消费。每个订单在不同的队列中，实现采用的订单号与队列数量取模的操作。由于队列数量-默认是4-(比较容易碰撞)，加上两个订单号(15103111139L
,15103117235L)之间取的比较接近，造成取模相同。

注：修改订单号，需要在个位和十位修改，百位及以上修改与4取模结果还是一样的，没有改变。

5. 延时消费
6. 批量消息的发送和消费
7. SpringBoot集成rocketMQ，搭建生产者和消费者结构

## 4.练习项目

技术选型：

- SpringBoot
- Dubbo
- Zookeeper
- RocketMQ

1. Linux搭建zookeeper集群,搭建dubbo-admin管理平台
   zookeeper启动：
   ```shell
   cd /home/ubuntu/zookeeper-cluster
   ./zookeeper-1/bin/zkServer.sh start
   -- 查看状态
   ./zookeeper-1/bin/zkServer.sh status
   ```
   注意webapps下面需要有dubbo-admin.war包
   ```shell
    cd /home/ubuntu/dubbo/tomcat/apache-tomcat-7.0.52/bin
    ./startup.sh
     ```
2. 本地代码实现dubbo的提供者和消费者，联调整体架构代码

### 分支对应关系
1. 创建并初始化项目：202207_file_commit
2. 202207_initProject
   1. 构建Mapper和xml对应文件以及添加各层次依赖；
   2. 校验订单功能实现
3. 20220801_pre_Order_commit
   1. 预订单实现
   2. 扣减库存实现
   3. 更新优惠卷以及余额功能实现
