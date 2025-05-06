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
 * 支持异步调用的过滤器示例
 * 
 * 该过滤器实现了Filter和Filter.Listener接口，能够正确处理异步调用场景。
 * 通过实现Listener接口的onResponse和onError方法，过滤器能够在异步操作
 * 完成后（无论成功或失败）被正确回调，从而避免阻塞主线程。
 * 
 * 这是处理Dubbo异步调用的推荐方式之一。
 */
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class AsyncPostprocessFilter implements Filter, Filter.Listener {
    private static Logger logger = LoggerFactory.getLogger(AsyncPostprocessFilter.class);

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取RPC上下文
        RpcContext context = RpcContext.getContext();
        
        // 获取并更新过滤器链追踪信息
        String filters = (String) context.getAttachment("filters");
        if (StringUtils.isEmpty(filters)) {
            filters = "";
        }
        filters += " async-post-process-filter";
        context.setAttachment("filters", filters);

        // 执行实际的服务调用，但不在这里处理结果
        // 结果将通过onResponse或onError回调处理
        return invoker.invoke(invocation);
    }

    /**
     * 当异步调用成功完成时被回调
     * 这个方法会在结果准备好后被调用，而不是立即调用
     */
    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // 安全地获取结果值，因为此时结果已经准备好了
        logger.info("Filter get the value: " + appResponse.getValue());
    }

    /**
     * 当异步调用发生异常时被回调
     * 这个方法提供了异步异常处理的能力
     */
    @Override
    public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
        // 记录异步调用过程中发生的异常
        logger.error("Filter get error", t);
    }
}
