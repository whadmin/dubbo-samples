package org.apache.dubbo.samples.context.api;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Dubbo上下文传递示例服务接口
 * 展示了Dubbo中不同场景下的上下文传递能力
 */
public interface ContextService {
    /**
     * 基础场景：获取基本的上下文信息
     * 展示获取ServiceContext, ServerAttachment等基本用法
     * 
     * @param clientName 客户端名称
     * @return 包含基本上下文信息的响应
     */
    String getBasicInfo(String clientName);
    
    /**
     * 链路追踪场景：展示如何传递和处理链路追踪信息
     * 
     * @param request 请求内容
     * @param traceHeaders 显式传递的追踪头信息
     * @return 包含追踪信息的响应
     */
    String trace(String request, Map<String, String> traceHeaders);
    
    /**
     * 异步调用场景：展示异步调用中的上下文传递
     * 
     * @param request 请求内容
     * @return 异步响应结果
     */
    CompletableFuture<String> getInfoAsync(String request);
    
    /**
     * 级联调用场景：展示跨服务的上下文传递
     * 
     * @param request 请求内容
     * @param needCascade 是否需要级联调用下一级服务
     * @return 包含级联调用信息的响应
     */
    String cascadeCall(String request, boolean needCascade);
    
    /**
     * 状态查询场景：展示如何使用服务端状态存储
     * 
     * @return 服务端状态信息
     */
    Map<String, Object> getServerState();
    
    /**
     * 参数传递场景：展示不同类型的参数传递方式
     * 
     * @param normalParam 常规参数
     * @param contextParams 显式上下文参数
     * @return 包含参数信息的响应
     */
    String passParameters(String normalParam, Map<String, Object> contextParams);
}
