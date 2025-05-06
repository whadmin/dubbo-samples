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

import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.common.utils.StringUtils;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.ListenableFilter;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;

import org.apache.dubbo.common.constants.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于ListenableFilter的异步过滤器示例
 * 
 * 该过滤器继承了ListenableFilter抽象类，提供了更结构化的方式来处理异步调用。
 * 通过内部类CallbackListener实现监听器逻辑，实现了过滤器和回调逻辑的分离，
 * 使代码更加清晰和易于维护。
 * 
 * 这是处理Dubbo异步调用的另一种推荐方式，适合更复杂的过滤器场景。
 */
@Activate(group = {CommonConstants.PROVIDER, CommonConstants.CONSUMER})
public class LegacyListenableFilter extends ListenableFilter {
    private static Logger logger = LoggerFactory.getLogger(LegacyListenableFilter.class);

    /**
     * 构造函数中设置监听器实例
     * ListenableFilter要求在构造函数中初始化listener属性
     */
    public LegacyListenableFilter() {
        super.listener = new CallbackListener();
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        // 获取RPC上下文
        RpcContext context = RpcContext.getContext();
        
        // 获取并更新过滤器链追踪信息
        String filters = (String) context.getAttachment("filters");
        if (StringUtils.isEmpty(filters)) {
            filters = "";
        }
        filters += " legacy-listenable-filter";
        context.setAttachment("filters", filters);

        // 执行实际的服务调用
        // 调用结果将通过CallbackListener的回调方法处理
        return invoker.invoke(invocation);
    }

    /**
     * 内部监听器类，用于处理异步调用的结果
     * 通过内部类的方式将监听逻辑与过滤器逻辑分离
     */
    private static class CallbackListener implements Filter.Listener {
        /**
         * 当异步调用成功完成时被回调
         */
        @Override
        public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
            System.out.println("Callback received in ListenableFilter.onResponse .");
        }

        /**
         * 当异步调用发生异常时被回调
         */
        @Override
        public void onError(Throwable t, Invoker<?> invoker, Invocation invocation) {
            // 异步调用异常处理逻辑
        }
    }
}
