/*
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to You under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.dubbo.samples.context.impl;

import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.samples.context.api.ContextService;

import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ContextServiceImpl implements ContextService {
    // 用于存储服务端状态的线程安全Map
    private static final Map<String, Object> SERVER_STATE = new ConcurrentHashMap<>();
    // 用于计数的原子整数
    private static final AtomicInteger REQUEST_COUNTER = new AtomicInteger(0);
    // 模拟的远程服务，用于级联调用场景
    private ContextService nextService = this;

    /**
     * 基础场景：获取基本的上下文信息 
     * 展示获取ServiceContext, ServerAttachment等基本用法
     */
    @Override
    public String getBasicInfo(String clientName) {
        // 记录处理开始时间
        long startTime = System.currentTimeMillis();
        
        // 增加请求计数
        int requestCount = REQUEST_COUNTER.incrementAndGet();
        SERVER_STATE.put("totalRequests", requestCount);
        SERVER_STATE.put("lastClientName", clientName);
        SERVER_STATE.put("lastAccessTime", System.currentTimeMillis());
        
        // 获取ServiceContext信息
        boolean isProviderSide = RpcContext.getServiceContext().isProviderSide();
        String clientIP = RpcContext.getServiceContext().getRemoteHost();
        String localAddress = RpcContext.getServiceContext().getLocalAddress().toString();
        String methodName = RpcContext.getServiceContext().getMethodName();
        String remoteApplication = RpcContext.getServiceContext().getRemoteApplicationName();
        
        // 获取ServerAttachment中的附件
        Map<String, String> attachments = RpcContext.getServerAttachment().getAttachments();
        StringJoiner attachmentsInfo = new StringJoiner(", ", "[", "]");
        for (Map.Entry<String, String> entry : attachments.entrySet()) {
            attachmentsInfo.add(entry.getKey() + "=" + entry.getValue());
        }
        
        // 构建响应
        StringBuilder response = new StringBuilder();
        response.append("Hello ").append(clientName).append(", Basic Information:\n");
        response.append("Provider Side: ").append(isProviderSide).append("\n");
        response.append("Client IP: ").append(clientIP).append("\n");
        response.append("Local Address: ").append(localAddress).append("\n");
        response.append("Method Called: ").append(methodName).append("\n");
        response.append("Remote Application: ").append(remoteApplication).append("\n");
        response.append("Received Attachments: ").append(attachmentsInfo).append("\n");
        response.append("Request Count: ").append(requestCount).append("\n");
        response.append("Processing Time: ").append(System.currentTimeMillis() - startTime).append("ms\n");
        
        return response.toString();
    }

    /**
     * 链路追踪场景：展示如何传递和处理链路追踪信息
     */
    @Override
    public String trace(String request, Map<String, String> traceHeaders) {
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        // 从RpcContext获取隐式传递的追踪信息
        String traceId = RpcContext.getServerAttachment().getAttachment("traceId");
        String spanId = RpcContext.getServerAttachment().getAttachment("spanId");
        String parentSpanId = RpcContext.getServerAttachment().getAttachment("parentSpanId");
        
        // 如果traceHeaders不为空，则优先使用显式传递的追踪信息
        if (traceHeaders != null && !traceHeaders.isEmpty()) {
            traceId = traceHeaders.getOrDefault("traceId", traceId);
            spanId = traceHeaders.getOrDefault("spanId", spanId);
            parentSpanId = traceHeaders.getOrDefault("parentSpanId", parentSpanId);
        }
        
        // 生成当前服务的跟踪信息
        String currentSpanId = (spanId != null) ? spanId + ".1" : "1";
        
        // 构建响应
        StringBuilder response = new StringBuilder();
        response.append("Trace Response for: ").append(request).append("\n");
        response.append("TraceId: ").append(traceId != null ? traceId : "Not provided").append("\n");
        response.append("ParentSpanId: ").append(parentSpanId != null ? parentSpanId : "Not provided").append("\n");
        response.append("SpanId: ").append(spanId != null ? spanId : "Not provided").append("\n");
        response.append("CurrentSpanId: ").append(currentSpanId).append("\n");
        response.append("Processing Time: ").append(System.currentTimeMillis() - startTime).append("ms\n");
        
        // 记录追踪信息日志
        System.out.println("===== Trace Processing Log =====");
        System.out.println(response.toString());
        System.out.println("=============================");
        
        return response.toString();
    }

    /**
     * 异步调用场景：展示异步调用中的上下文传递
     */
    @Override
    public CompletableFuture<String> getInfoAsync(String request) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 获取主线程传递过来的附件
                String traceId = RpcContext.getServerAttachment().getAttachment("traceId");
                String asyncMarker = RpcContext.getServerAttachment().getAttachment("asyncMarker");
                
                StringBuilder response = new StringBuilder();
                response.append("Async Response for: ").append(request).append("\n");
                response.append("Processed in thread: ").append(Thread.currentThread().getName()).append("\n");
                response.append("TraceId from original request: ").append(traceId != null ? traceId : "Not provided").append("\n");
                response.append("Async Marker: ").append(asyncMarker != null ? asyncMarker : "Not provided").append("\n");
                response.append("Processing Time: ").append(System.currentTimeMillis()).append("\n");
                return response.toString();
            } catch (Exception e) {
                return "Async processing failed: " + e.getMessage();
            }
        });
    }

    /**
     * 级联调用场景：展示跨服务的上下文传递
     */
    @Override
    public String cascadeCall(String request, boolean needCascade) {
        // 获取上下文信息
        String traceId = RpcContext.getServerAttachment().getAttachment("traceId");
        String spanId = RpcContext.getServerAttachment().getAttachment("spanId");
        
        // 生成当前服务的跟踪信息
        String currentSpanId = (spanId != null) ? spanId + ".1" : "1";
        
        StringBuilder response = new StringBuilder();
        response.append("Cascade Response Level 1 for: ").append(request).append("\n");
        response.append("TraceId: ").append(traceId != null ? traceId : "Generated-" + System.currentTimeMillis()).append("\n");
        response.append("CurrentSpanId: ").append(currentSpanId).append("\n");
        
        // 如果需要级联调用下一级服务
        if (needCascade) {
            try {
                // 设置传递给下一级服务的上下文信息
                RpcContext.getClientAttachment().setAttachment("traceId", traceId);
                RpcContext.getClientAttachment().setAttachment("parentSpanId", currentSpanId);
                RpcContext.getClientAttachment().setAttachment("spanId", currentSpanId + ".1");
                RpcContext.getClientAttachment().setAttachment("level", "2");
                
                // 级联调用（这里简单地递归调用自身，实际中可能调用其他服务）
                // 为避免无限递归，第二个参数传false
                String nextLevelResponse = nextService.cascadeCall("Cascaded-" + request, false);
                response.append("\n--- Next Level Response ---\n");
                response.append(nextLevelResponse);
            } catch (Exception e) {
                response.append("\nCascade call failed: ").append(e.getMessage()).append("\n");
            }
        }
        
        return response.toString();
    }

    /**
     * 状态查询场景：展示如何使用服务端状态存储
     */
    @Override
    public Map<String, Object> getServerState() {
        // 更新一些实时状态
        SERVER_STATE.put("currentTime", System.currentTimeMillis());
        SERVER_STATE.put("availableProcessors", Runtime.getRuntime().availableProcessors());
        SERVER_STATE.put("freeMemory", Runtime.getRuntime().freeMemory());
        SERVER_STATE.put("activeThreads", Thread.activeCount());
        
        // 返回所有服务器状态
        return new HashMap<>(SERVER_STATE);
    }

    /**
     * 参数传递场景：展示不同类型的参数传递方式
     */
    @Override
    public String passParameters(String normalParam, Map<String, Object> contextParams) {
        StringBuilder response = new StringBuilder();
        response.append("Parameter Passing Demo:\n");
        
        // 1. 处理常规参数
        response.append("Normal Parameter: ").append(normalParam).append("\n");
        
        // 2. 处理Map中的显式上下文参数
        response.append("Explicit Context Parameters:\n");
        if (contextParams != null && !contextParams.isEmpty()) {
            for (Map.Entry<String, Object> entry : contextParams.entrySet()) {
                response.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        } else {
            response.append("  No explicit parameters provided\n");
        }
        
        // 3. 处理RpcContext中的隐式上下文参数
        response.append("Implicit Context Parameters:\n");
        Map<String, String> attachments = RpcContext.getServerAttachment().getAttachments();
        if (attachments != null && !attachments.isEmpty()) {
            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                response.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        } else {
            response.append("  No implicit parameters provided\n");
        }
        
        return response.toString();
    }
}
