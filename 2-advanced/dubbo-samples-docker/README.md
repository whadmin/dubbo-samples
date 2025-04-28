# Docker 环境下的 Dubbo 服务部署

一些部署场景需要动态指定服务注册的地址。例如，Docker 桥接网络模式需要指定一个注册的主机 IP 用于外部网络通信。Dubbo 在启动阶段提供了两对系统属性，用于设置外部通信的 IP 和端口地址：

* DUBBO_IP_TO_REGISTRY --- 注册到注册中心的 IP 地址  
* DUBBO_PORT_TO_REGISTRY --- 注册到注册中心的端口 
* DUBBO_IP_TO_BIND --- 监听的 IP 地址  
* DUBBO_PORT_TO_BIND --- 监听的端口 

> 1. 以上四项配置都是可选的。如果没有配置，Dubbo 将自动获取 IP 和端口。请根据部署场景灵活选择。
> 2. Dubbo 支持多协议。**如果一个应用同时暴露多个不同协议的服务，并且需要为每个服务单独指定 IP 或端口，请在上述属性前分别添加协议前缀。** 例如：
>    * HESSIAN_DUBBO_PORT_TO_BIND    hessian 协议绑定端口
>    * DUBBO_DUBBO_PORT_TO_BIND      dubbo 协议绑定端口
>    * HESSIAN_DUBBO_IP_TO_REGISTRY  hessian 协议注册 IP
>    * DUBBO_DUBBO_IP_TO_REGISTRY    dubbo 协议注册 IP
> 3. `PORT_TO_REGISTRY` 或 `IP_TO_REGISTRY` 不会被用作默认的 `PORT_TO_BIND` 或 `IP_TO_BIND`，但反过来是成立的。
>    * 如果设置 `PORT_TO_REGISTRY=20881` `IP_TO_REGISTRY=30.5.97.6`，那么 `PORT_TO_BIND` `IP_TO_BIND` 不会受影响。
>    * 如果设置 `PORT_TO_BIND=20881` `IP_TO_BIND=30.5.97.6`，那么默认情况下 `PORT_TO_REGISTRY=20881` `IP_TO_REGISTRY=30.5.97.6`。

[dubbo-docker-sample](https://github.com/dubbo/dubbo-docker-sample) 本地操作流程：
 
1. 克隆项目到本地
```sh
git clone git@github.com:dubbo/dubbo-docker-sample.git
cd dubbo-docker-sample
```

2. 本地 Maven 打包
```sh
mvn clean install  
```

3. 通过 Docker build 构建镜像
```sh
docker build --no-cache -t dubbo-docker-sample . 
```

Dockerfile
```sh
FROM openjdk:8-jdk-alpine
ADD target/dubbo-docker-sample-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT exec java $JAVA_OPTS -jar /app.jar
```

4. 从镜像创建并运行容器
```sh
# 由于我们使用 zk 注册中心，首先启动 zk 容器
docker run --name zkserver --restart always -d zookeeper:3.4.9
```
```sh
docker run -e DUBBO_IP_TO_REGISTRY=30.5.97.6 -e DUBBO_PORT_TO_REGISTRY=20881 -p 30.5.97.6:20881:20880 --link zkserver:zkserver -it --rm dubbo-docker-sample
```

> 假设主机 IP 是 30.5.97.6。  
> 通过环境变量 `DUBBO_IP_TO_REGISTRY=30.5.97.6` `DUBBO_PORT_TO_REGISTRY=20881` 设置提供者注册到注册中心的 IP 地址和端口。  
> 通过 `-p 30.5.97.6:20881:20880` 实现端口映射，其中 20880 是 Dubbo 自动选择的监听端口。没有监听 IP 配置，所以它将监听 0.0.0.0（所有 IP）。  
> 启动后，提供者的注册地址是 30.5.97.6:20881，容器的监听地址是：0.0.0.0:20880 

5. 测试
从另一台主机或容器执行
```sh
telnet 30.5.97.6 20881
ls
invoke org.apache.dubbo.test.docker.DemoService.hello("world")


找到具有 1 个许可证类型的类似代码