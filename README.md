# 目录
- [项目概览](#项目概览)  
- [项目使用框架](#项目使用框架)  
- [项目实现功能](#项目实现功能)  
  - [用户相关](##用户相关) 
  - [Design inspirations](#design-inspirations) 
  - [Innovation point](#innovation-point)  
  - [Developmental vision](#developmental-vision) 
- [开发遇到问题](#开发遇到问题) 
 
---

# 项目概览 

项目来源：[牛客网项目](https://www.nowcoder.com/courses/semester/senior)

# 项目使用框架
![系统图](https://s1.ax1x.com/2020/08/19/dMIkVO.png)

# 项目实现功能

## 用户相关

用户注册、邮件发送、账号激活

用户登录、验证码、设置cookie、用户退出

个人主页、头像修改、密码修改

网页菜单登录状态变化、网页访问登录状态限制

## 2.2 帖子相关

发布帖子、敏感词过滤

帖子列表、帖子详情

评论列表、新增评论

私信列表、发送私信

帖子搜索

ElasticSearch

使用之前需要安装中文分词插件ik https://github.com/medcl/elasticsearch-analysis-ik

使用Postman作为工具学习ElasticSearch的使用

实现搜索引擎，通过关键词搜索

发布帖子时，使用kafka将新的帖子同步到搜索引擎中

搜索引擎的版本和JDK版本冲突
https://blog.csdn.net/chentyit/article/details/101193838

## 2.3 日志记录

统一处理异常

记录日志（AOP）

## 2.4 性能提升
Redis

实现点赞、关注的功能

优化登录流程：验证码、登录凭证、缓存用户信息

统计网站的UV、DAU

Caffeine

增加缓存，在查询帖子总数/查询帖子列表时，加入一级缓存

使用JMeter进行压力测试，吞吐量100.5/sec --> 190.2/sec

## 2.5 系统通知

kafka

评论/点赞/关注，使用生产者消费者模式，向用户发送系统通知

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

## 2.6 项目完善 
### 用户权限
spring security

权限管理：管理员、版主、普通用户

功能管理：置顶、加精、删除

### 帖子排名
QUARTZ

定时任务，根据公式计算帖子热度

维护列表，仅更新近期修改过的帖子的分数

### 项目监控
Spring Boot Actuator

页面很粗糙，本以为是带各种图表的监控。。注意需要加上访问权限。

#3.开发遇到问题
maven的gav写错，有些不同版本的包不兼容

String用==判断相等吗，导致了条件永远不执行的bug

Config类上忘写@Configuration注解

utf-8写错写成urf-8

jdk版本问题，最开始用了14，发现和搜索引擎的包冲突，后修改为8
