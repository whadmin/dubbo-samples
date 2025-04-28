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

package org.apache.dubbo.samples.context;

import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.samples.context.api.ContextService;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Dubbo上下文传递示例消费者
 * 演示各种场景下的上下文传递功能
 */
public class ContextConsumer {
    // 用于存储客户端本地上下文的Map（模拟ClientContext）
    private static final Map<String, Object> LOCAL_CONTEXT = new HashMap<>();
    
    public static void main(String[] args) throws Exception {
        // 加载Spring配置，启动Dubbo消费者
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring/dubbo-context-consumer.xml");
        context.start();
        
        // 获取远程服务代理
        ContextService contextService = context.getBean("contextService", ContextService.class);

        // 测试基础场景：获取基本上下文信息
        testBasicInfo(contextService);
        
        // 测试链路追踪场景
        testTrace(contextService);
        
        // 测试异步调用场景
        testAsyncCall(contextService);
        
        // 测试级联调用场景
        testCascadeCall(contextService);
        
        // 测试服务器状态查询
        testServerState(contextService);
        
        // 测试参数传递方式
        testParameterPassing(contextService);
        
        System.out.println("所有测试完成!");
        context.close();
    }
    
    /**
     * 测试基础场景：获取基本上下文信息
     */
    private static void testBasicInfo(ContextService contextService) {
        System.out.println("\n======= 测试基础场景：获取基本上下文信息 =======");
        
        // 设置本地上下文数据
        String callId = UUID.randomUUID().toString();
        LOCAL_CONTEXT.put("callId", callId);
        LOCAL_CONTEXT.put("startTime", System.currentTimeMillis());
        
        // 设置要传递给服务提供方的附件
        RpcContext.getClientAttachment().setAttachment("clientId", "consumer-" + System.currentTimeMillis());
        RpcContext.getClientAttachment().setAttachment("userId", "user_" + System.currentTimeMillis() % 1000);
        RpcContext.getClientAttachment().setAttachment("clientVersion", "JavaClient/2.0");
        
        System.out.println("发送基础信息请求，客户端ID: " + RpcContext.getClientAttachment().getAttachment("clientId"));
        
        try {
            // 调用远程服务
            String result = contextService.getBasicInfo("BasicInfoClient");
            
            // 计算调用耗时
            long startTime = (long) LOCAL_CONTEXT.get("startTime");
            long duration = System.currentTimeMillis() - startTime;
            
            System.out.println("基础信息调用耗时: " + duration + "ms");
            System.out.println("基础信息服务响应结果: \n" + result);
            
        } catch (Exception e) {
            System.err.println("调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试链路追踪场景
     */
    private static void testTrace(ContextService contextService) {
        System.out.println("\n======= 测试链路追踪场景 =======");
        
        // 生成跟踪ID
        String traceId = UUID.randomUUID().toString();
        String spanId = "1";
        
        // 1. 通过RpcContext隐式传递跟踪信息
        RpcContext.getClientAttachment().setAttachment("traceId", traceId);
        RpcContext.getClientAttachment().setAttachment("spanId", spanId);
        RpcContext.getClientAttachment().setAttachment("parentSpanId", "0");
        
        System.out.println("发送跟踪请求（隐式传递），TraceId: " + traceId);
        
        try {
            // 调用远程服务，不传递显式跟踪头
            String implicitResult = contextService.trace("Implicit Trace Request", null);
            System.out.println("隐式跟踪结果: \n" + implicitResult);
            
            // 2. 通过方法参数显式传递跟踪信息
            Map<String, String> traceHeaders = new HashMap<>();
            traceId = UUID.randomUUID().toString();
            traceHeaders.put("traceId", traceId);
            traceHeaders.put("spanId", "1.0");
            traceHeaders.put("parentSpanId", "0");
            traceHeaders.put("samplingRate", "1.0");
            
            System.out.println("发送跟踪请求（显式传递），TraceId: " + traceId);
            
            // 同时，也保留一些隐式传递的信息，用于对比
            RpcContext.getClientAttachment().setAttachment("hiddenTraceId", "hidden-" + UUID.randomUUID().toString());
            
            String explicitResult = contextService.trace("Explicit Trace Request", traceHeaders);
            System.out.println("显式跟踪结果: \n" + explicitResult);
            
        } catch (Exception e) {
            System.err.println("跟踪请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试异步调用场景
     */
    private static void testAsyncCall(ContextService contextService) throws Exception {
        System.out.println("\n======= 测试异步调用场景 =======");
        
        // 设置异步调用相关的上下文
        LOCAL_CONTEXT.put("asyncStartTime", System.currentTimeMillis());
        
        // 设置传递给服务方的附件
        String asyncTraceId = "ASYNC_" + UUID.randomUUID().toString();
        RpcContext.getClientAttachment().setAttachment("traceId", asyncTraceId);
        RpcContext.getClientAttachment().setAttachment("asyncMarker", "true");
        RpcContext.getClientAttachment().setAttachment("timestamp", String.valueOf(System.currentTimeMillis()));
        
        System.out.println("发送异步请求，AsyncTraceId: " + asyncTraceId);
        
        // 异步调用需要等待结果
        CountDownLatch latch = new CountDownLatch(1);
        
        try {
            // 直接调用返回CompletableFuture的方法
            CompletableFuture<String> future = contextService.getInfoAsync("Async Request");
            
            // 添加完成回调
            future.whenComplete((result, exception) -> {
                try {
                    if (exception != null) {
                        System.err.println("异步调用异常: " + exception.getMessage());
                    } else {
                        long startTime = (long) LOCAL_CONTEXT.get("asyncStartTime");
                        long duration = System.currentTimeMillis() - startTime;
                        
                        System.out.println("异步调用耗时: " + duration + "ms");
                        System.out.println("异步服务响应结果: \n" + result);
                    }
                } finally {
                    latch.countDown();
                }
            });
            
            // 等待异步调用完成或超时
            if (!latch.await(5, TimeUnit.SECONDS)) {
                System.err.println("异步调用超时");
            }
            
        } catch (Exception e) {
            System.err.println("异步调用发生异常: " + e.getMessage());
            latch.countDown();
        }
    }
    
    /**
     * 测试级联调用场景
     */
    private static void testCascadeCall(ContextService contextService) {
        System.out.println("\n======= 测试级联调用场景 =======");
        
        // 设置级联调用的跟踪ID
        String traceId = "CASCADE_" + UUID.randomUUID().toString();
        RpcContext.getClientAttachment().setAttachment("traceId", traceId);
        RpcContext.getClientAttachment().setAttachment("spanId", "root");
        RpcContext.getClientAttachment().setAttachment("level", "1");
        
        System.out.println("发起级联调用请求，TraceId: " + traceId);
        
        try {
            // 调用级联服务，第二个参数为true表示需要继续级联调用
            String result = contextService.cascadeCall("Cascade Request", true);
            
            System.out.println("级联调用结果: \n" + result);
            
        } catch (Exception e) {
            System.err.println("级联调用失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试服务器状态查询
     */
    private static void testServerState(ContextService contextService) {
        System.out.println("\n======= 测试服务器状态查询 =======");
        
        try {
            // 查询服务器状态
            Map<String, Object> serverState = contextService.getServerState();
            
            System.out.println("服务器状态信息:");
            for (Map.Entry<String, Object> entry : serverState.entrySet()) {
                System.out.println("  " + entry.getKey() + ": " + entry.getValue());
            }
            
        } catch (Exception e) {
            System.err.println("获取服务器状态失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试参数传递方式
     */
    private static void testParameterPassing(ContextService contextService) {
        System.out.println("\n======= 测试参数传递方式 =======");
        
        // 1. 设置显式传递的上下文参数
        Map<String, Object> contextParams = new HashMap<>();
        contextParams.put("explicitParam1", "value1");
        contextParams.put("explicitParam2", 100);
        contextParams.put("explicitParam3", true);
        
        // 2. 设置隐式传递的上下文参数
        RpcContext.getClientAttachment().setAttachment("implicitParam1", "implicitValue1");
        RpcContext.getClientAttachment().setAttachment("implicitParam2", "implicitValue2");
        
        System.out.println("发送参数传递请求");
        System.out.println("显式参数: " + contextParams);
        System.out.println("隐式参数: implicitParam1=implicitValue1, implicitParam2=implicitValue2");
        
        try {
            // 调用服务，同时使用显式和隐式参数
            String result = contextService.passParameters("normalValue", contextParams);
            
            System.out.println("参数传递测试结果: \n" + result);
            
        } catch (Exception e) {
            System.err.println("参数传递测试失败: " + e.getMessage());
        }
    }
}
