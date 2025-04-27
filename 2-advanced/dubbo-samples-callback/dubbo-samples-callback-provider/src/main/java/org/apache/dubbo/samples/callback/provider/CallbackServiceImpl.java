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

package org.apache.dubbo.samples.callback.provider;

import org.apache.dubbo.config.annotation.Argument;
import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.samples.callback.api.CallbackListener;
import org.apache.dubbo.samples.callback.api.CallbackService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 回调服务实现类
 * 
 * @DubboService 注解暴露服务
 * @Method 注解配置方法特性
 * @Argument 注解标记回调参数
 */
@DubboService(methods = {@Method(name = "addListener", arguments = {@Argument(index = 1, callback = true)})})
public class CallbackServiceImpl implements CallbackService {

    // 使用线程安全的Map存储所有注册的回调监听器
    private final Map<String, CallbackListener> listeners = new ConcurrentHashMap<String, CallbackListener>();

    /**
     * 构造函数
     * 初始化后台线程，定期向所有注册的监听器发送回调通知
     */
    public CallbackServiceImpl() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // 遍历所有监听器并发送通知
                        for (Map.Entry<String, CallbackListener> entry : listeners.entrySet()) {
                            try {
                                // 调用消费者的回调方法
                                entry.getValue().changed(getChanged(entry.getKey()));
                            } catch (Throwable t1) {
                                // 如果回调失败，移除该监听器
                                listeners.remove(entry.getKey());
                            }
                        }
                        // 每5秒触发一次回调
                        Thread.sleep(5000);
                    } catch (Throwable t1) {
                        t1.printStackTrace();
                    }
                }
            }
        });
        // 设置为守护线程，不阻止JVM退出
        t.setDaemon(true);
        t.start();
    }

    /**
     * 添加回调监听器实现
     * 当消费者注册监听器时调用此方法
     */
    @Override
    public void addListener(String key, CallbackListener listener) {
        // 保存监听器
        listeners.put(key, listener);
        // 立即发送一次通知
        listener.changed(getChanged(key));
    }

    /**
     * 生成变更消息
     * 这里简单返回当前时间作为变更内容
     */
    private String getChanged(String key) {
        return "Changed: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
