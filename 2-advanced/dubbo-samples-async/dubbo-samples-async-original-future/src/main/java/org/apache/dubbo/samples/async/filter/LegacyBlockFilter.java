/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.samples.async.filter;

import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 传统的同步阻塞式过滤器示例
 * 
 * 该过滤器在异步调用场景下可能会有问题，因为它试图立即获取结果值，
 * 而在异步场景中，结果可能尚未准备好。此过滤器用于展示在异步调用中
 * 不应该采用的传统同步处理方式。
 * 
 * 通过@Activate注解激活该过滤器，同时应用于服务提供方和消费方。
 */
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class LegacyBlockFilter implements Filter {
    private static Logger logger = LoggerFactory.getLogger(LegacyBlockFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取RPC上下文
        RpcContext context = RpcContext.getContext();
        
        // 获取并更新过滤器链追踪信息，用于验证调用链中的过滤器执行顺序
        String filters = (String) context.getAttachment("filters");
        if (StringUtils.isEmpty(filters)) {
            filters = "";
        }
        filters += " legacy-block-filter";
        context.setAttachment("filters", filters);

        // 执行实际的服务调用（同步等待结果）
        Result result = invoker.invoke(invocation);

        // 警告：这里试图直接获取结果值，在异步场景下可能会出现问题
        // 因为异步调用时，此时结果可能尚未准备好
        logger.info("This is the default return value: " + result.getValue());

        // 处理异常情况
        if (result.hasException()) {
            System.out.println("LegacyBlockFilter: This will only happen when the real exception returns: " + result.getException());
            logger.warn("This will only happen when the real exception returns", result.getException());
        }

        // 这条日志可能在异步结果返回前就已经打印
        logger.info("LegacyBlockFilter: This msg should not be blocked.");
        return result;
    }
}
