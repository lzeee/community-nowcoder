# community-nowcoder

来源：[牛客网项目](https://www.nowcoder.com/courses/semester/senior)

### 0.项目使用框架
![系统图](https://s1.ax1x.com/2020/08/19/dMIkVO.png)


### 1.项目实现的功能
#### 用户

用户注册、邮件发送、账号激活

用户登录、验证码、设置cookie、用户退出

修改头像、修改密码

个人主页

网页菜单登录状态变化、网页访问登录状态限制

#### 帖子

发布帖子、敏感词过滤

帖子列表、帖子信息

加载评论、增加评论

私信列表、私信发送

#### 日志

统一处理异常、记录日志（AOP）

#### Redis
内存型数据库

点赞、关注

优化登录流程：验证码、登录凭证、缓存用户信息

#### Kafka

消息队列服务器

评论/点赞/关注后，生产者消费者模式，发布通知

项目启动之前必须先启动kafka，而kafka启动过程比较繁琐

1.启动zookeeper

bin\windows\zookeeper-server-start.bat config\zookeeper.properties

2.启动kafka

bin\windows\kafka-server-start.bat config\server.properties

3.新建主题

bin\windows\kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test

4.查看主题

bin\windows\kafka-topics.bat --list --bootstrap-server localhost:9092

服务打开之后就可以在springboot中调用

#### ElasticSearch

使用之前需要安装中文分词插件ik https://github.com/medcl/elasticsearch-analysis-ik

服务启动之前需要启动ElasticSearch服务器

Postman作为工具学习ElasticSearch的使用

实现搜索引擎，通过关键词搜索

发布帖子时，使用kafka将新的帖子同步到搜索引擎中

搜索引擎的版本和JDK版本冲突
https://blog.csdn.net/chentyit/article/details/101193838


#### 项目完善 
##### spring security

权限管理：管理员、版主、普通用户

功能管理：置顶、加精、删除

##### Redis

UV DAU统计

##### QUARTZ

定时任务，计算帖子热度

##### 性能提升

增加缓存 Caffeine，在查询帖子总数/查询帖子列表时，加入一级缓存

压力测试 JMeter 使用缓存之前吞吐量100.5/sec，之后190.2/sec

##### 项目监控
Spring Boot Actuator

页面很粗糙，本以为是带各种图表的监控。。注意需要加上访问权限。


### 3.开发中遇到问题
maven的gav写错，不同包版本不兼容
String用==判断相等
Config类上忘写@Configuration注解
utf-8写错写成urf-8
不同包版本之间的兼容问题
jdk版本问题