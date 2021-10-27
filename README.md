### 谷粒商城简介

###  **前言** 


`heiyingmail` 项目致力于打造一个完整的电商系统，采用现阶段流行技术来实现，采用前后端分离继续编写。

### 项目API接口文档


文档地址：https://easydoc.xyz/s/78237135/ZUqEdvA4/hKJTcbfd
### 
项目介绍


gulimall（谷粒商城） 项目是一套电商项目，包括前台商城系统以及后台管理系统，基于 SpringCloud + SpringCloudAlibaba + MyBatis-Plus实现，采用 Docker 容器化部署。后台管理系统包括：系统管理、商品系统、优惠营销、库存系统、订单系统、用户系统、内容管理等七大模块。


### 
搭建步骤
    1、克隆后端项目并导入IDEA中
    2、修改配置文件中的ip
    3、导入sql脚本
    4、启动每个服务
    5、克隆前端项目使用命令 **npm run dev**  启动

### 项目概览
     **主界面** 
![主界面](https://images.gitee.com/uploads/images/2021/1027/200119_ea75881b_9847356.png "屏幕截图.png")
     **后台部分功能** 
![输入图片说明](https://images.gitee.com/uploads/images/2021/1027/200501_cca48f05_9847356.png "屏幕截图.png")
### 项目架构

        gulimall
        ├── gulimall-common -- 工具类及通用代码
        ├── renren-generator -- 人人开源项目的代码生成器
        ├── renren-fast -- 人人开源项目的后台管理
        ├── gulimall-coupon -- 优惠卷服务
        ├── gulimall-gateway -- 统一配置网关
        ├── gulimall-order -- 订单服务
        ├── gulimall-product -- 商品服务
        ├── gulimall-third-party -- 第三方服务
        ├── gulimall-ware -- 仓储服务
        └── gulimall-member -- 会员服务
 **技术选型** 
| 技术                 | 说明           | 官网                                              |
|--------------------|--------------|-------------------------------------------------|
| SpringBoot         | 容器+MVC框架     | https://spring.io/projects/spring-boot          |
| SpringCloud        | 微服务架构        | https://spring.io/projects/spring-cloud         |
| SpringCloudAlibaba | 一系列组件        | https://spring.io/projects/spring-cloud-alibaba |
| MyBatis-Plus       | ORM框架        | https://mp.baomidou.com                         |
| renren-generator   | 人人开源项目的代码生成器 | https://gitee.com/renrenio/renren-generator     |
| Docker             | 应用容器引擎       | https://www.docker.com                          |
| OSS                | 对象云存储        | https://github.com/aliyun/aliyun-oss-java-sdk   |




