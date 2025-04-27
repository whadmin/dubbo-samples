/*
 *
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
 *
 */

package org.apache.dubbo.samples.callback.consumer;

import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.samples.callback.api.CallbackService;
import org.apache.dubbo.samples.callback.api.StockPriceListener;
import org.apache.dubbo.samples.callback.api.StockService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 消费者任务类
 * 实现CommandLineRunner接口，在应用启动时自动执行
 */
@Component
public class Task implements CommandLineRunner {

    // 引用远程回调服务
    @DubboReference
    private StockService stockService;
    ;

    /**
     * 应用启动后执行
     * 注册回调监听器到服务提供者
     */
    @Override
    public void run(String... args) throws Exception {
        // 创建监听器实例
        StockPriceListener listener = new StockPriceListenerImpl("client001");

        // 订阅股票价格变动
        stockService.subscribePrice("AAPL", listener);
        stockService.subscribePrice("GOOGL", listener);
    }
}
