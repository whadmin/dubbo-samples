# Dubbo回调示例

## 概述

本示例展示了Dubbo框架的参数回调(Parameter Callback)功能。参数回调允许服务提供者主动调用消费者，实现服务端向客户端的反向通知，适用于需要服务器向客户端推送数据的场景。

## 核心流程

1. **接口定义**
   - 定义服务接口`CallbackService`，包含注册回调的方法
   - 定义回调接口`CallbackListener`，包含服务端将调用的方法

2. **服务提供者**
   - 使用`@DubboService`暴露服务
   - 用`@Argument(index = 1, callback = true)`标记回调参数
   - 存储消费者的回调对象
   - 通过后台线程定期触发回调

3. **服务消费者**
   - 使用`@DubboReference`引用远程服务
   - 实现回调接口
   - 将回调实现注册到提供者

4. **回调执行**
   - 提供者保存回调对象引用
   - 提供者定期或在特定事件触发时调用回调方法
   - 消费者接收并处理回调请求

5. **错误处理**
   - 当回调执行失败时自动移除失效的回调对象

## 示例结构

```
dubbo-samples-callback/
├── dubbo-samples-callback-interface/    # 接口定义
├── dubbo-samples-callback-provider/     # 服务提供者实现
└── dubbo-samples-callback-consumer/     # 服务消费者实现
```

## 运行步骤

1. 启动服务提供者：运行`ProviderApplication`
2. 启动服务消费者：运行`ConsumerApplication`
3. 观察消费者控制台输出，可以看到每5秒接收一次服务端的回调通知

## 核心代码

### 回调参数标记
```java
@DubboService(methods = {@Method(name = "addListener", arguments = {@Argument(index = 1, callback = true)})})
```

### 回调执行
```java
// 服务提供者调用消费者的回调方法
entry.getValue().changed(getChanged(entry.getKey()));
```

### 错误处理
```java
try {
    entry.getValue().changed(getChanged(entry.getKey()));
} catch (Throwable t1) {
    // 移除失效的回调对象
    listeners.remove(entry.getKey());
}
```

## 应用场景

- 实时数据推送
- 状态变更通知
- 事件监听
- 异步操作完成通知