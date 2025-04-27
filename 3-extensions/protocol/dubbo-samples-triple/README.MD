whadmin: 翻译成中文

GitHub Copilot: # Triple-示例

[Triple](https://dubbo.apache.org/zh/docs/concepts/rpc-protocol/)是一个基于HTTP/2的应用层协议，它使用Protobuf序列化自然地支持与GRPC协议的互操作。对于非protobuf应用，triple提供了一种兼容模式，可以在不修改代码的情况下进行升级。因此迁移到triple是非常平滑的。

这个示例项目包含了Dubbo和非Dubbo互操作的大多数场景。

## 开始之前

为IDE提示生成protobuf类。

```shell
mvn clean -DskipTests=true
```

### 对于初学者

1. 启动zookeeper或在`org.apache.dubbo.sample.tri.stub.TriStubServer`和`org.apache.dubbo.sample.tri.stub.TriStubClient`中配置其他注册中心
2. 使用`mvn clean install`构建以生成protobuf类
3. 运行`org.apache.dubbo.sample.tri.stub.TriStubServer`
4. 运行`org.apache.dubbo.sample.tri.stub.TriStubClient`

### 对于dubbo2或非protobuf用户

运行`org.apache.dubbo.sample.tri.pojo.TriPojoServer`和`org.apache.dubbo.sample.tri.pojo.TriPojoClient`。

Dubbo3将自动使用protobuf包装器包装请求和响应，因此它仍然与GRPC兼容。

### 对于Dubbo开发者

test文件夹下的代码是Dubbo/Dubbo和Dubbo/GRPC的通用互操作测试套件。

#### Dubbo/Dubbo测试

1. 运行`org.apache.dubbo.sample.tri.TriGrpcProvider`
2. 运行`org.apache.dubbo.sample.tri.TriClientTest`、`org.apache.dubbo.sample.tri.TriPojoClientTest`和`org.apache.dubbo.sample.tri.TriGenericTest`

#### Dubbo/GRPC测试

##### GRPC --> Dubbo

1. 运行`org.apache.dubbo.sample.tri.TriGrpcProvider`
2. 运行`org.apache.dubbo.sample.tri.grpc.GrpcClientTest`

##### Dubbo --> GRPC

1. 运行`org.apache.dubbo.sample.tri.grpc.GrpcServer`
2. 运行`org.apache.dubbo.sample.tri.TriClientTest`