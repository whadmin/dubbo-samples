# Dubbo 中环境变量和系统属性机制实现配置灵活性

Dubbo 提供了强大的环境变量和系统属性配置机制，可以大幅提高应用的灵活性和可移植性。以下是几个具体示例：

## 1. 服务注册中心地址配置

**传统方式**：在 XML 配置文件中硬编码
```xml
<dubbo:registry address="zookeeper://192.168.1.100:2181"/>
```

**使用系统属性的灵活方式**：
```xml
<dubbo:registry address="${dubbo.registry.address}"/>
```

然后可以通过多种方式设置这个属性：
- JVM 启动参数: `-Ddubbo.registry.address=zookeeper://192.168.1.100:2181`
- 环境变量: `export DUBBO_REGISTRY_ADDRESS=zookeeper://192.168.1.100:2181`
- 在 `dubbo.properties` 文件中设置: `dubbo.registry.address=zookeeper://192.168.1.100:2181`

## 2. 服务端口配置

**XML配置**:
```xml
<dubbo:protocol name="dubbo" port="${dubbo.protocol.port:20880}"/>
```

这里使用了默认值语法 `:20880`，表示如果没有配置该属性，则使用 20880 作为默认值。

**容器化环境应用示例**：
```bash
# 开发环境
docker run -e DUBBO_PROTOCOL_PORT=20880 my-dubbo-app

# 测试环境
docker run -e DUBBO_PROTOCOL_PORT=20881 my-dubbo-app

# 生产环境
docker run -e DUBBO_PROTOCOL_PORT=20882 my-dubbo-app
```

## 3. 超时时间及重试配置

**配置文件**:
```xml
<dubbo:reference id="demoService" interface="org.apache.dubbo.samples.api.DemoService"
    timeout="${dubbo.consumer.timeout:3000}" 
    retries="${dubbo.consumer.retries:2}"/>
```

**不同环境的灵活配置**:
```properties
# 开发环境 (application-dev.properties)
dubbo.consumer.timeout=5000
dubbo.consumer.retries=3

# 生产环境 (application-prod.properties)
dubbo.consumer.timeout=2000
dubbo.consumer.retries=1
```

## 4. 配置优先级应用

Dubbo 配置遵循特定的优先级顺序：
1. JVM -D 参数
2. 环境变量
3. properties 文件
4. XML/注解配置

示例应用场景：

```java
public class DubboApplication {
    public static void main(String[] args) {
        // 优先级 1: 命令行参数
        // java -Ddubbo.application.name=app-cli DubboApplication
        
        // 优先级 2: 环境变量
        // export DUBBO_APPLICATION_NAME=app-env
        
        // 优先级 3: 配置文件中的配置
        // dubbo.application.name=app-props
        
        SpringApplication.run(DubboApplication.class, args);
    }
}
```

## 5. 在 Kubernetes 环境中的应用

Kubernetes 的 ConfigMap 和 Secret 资源可以作为环境变量注入到容器中：

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: dubbo-config
data:
  DUBBO_REGISTRY_ADDRESS: "zookeeper://zookeeper-service:2181"
  DUBBO_PROTOCOL_PORT: "20880"
  DUBBO_APPLICATION_NAME: "user-service"
```

部署时引用此配置：

```yaml
spec:
  containers:
  - name: dubbo-app
    image: my-dubbo-app:1.0
    envFrom:
    - configMapRef:
        name: dubbo-config
```

这种配置方式使得应用不需要修改代码或重新构建镜像，就可以适应不同的环境和场景，极大提高了微服务架构的灵活性和可移植性。