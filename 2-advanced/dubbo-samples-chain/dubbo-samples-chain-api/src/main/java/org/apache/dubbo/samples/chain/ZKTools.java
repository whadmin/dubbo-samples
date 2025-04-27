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
package org.apache.dubbo.samples.chain;

import org.apache.dubbo.common.utils.StringUtils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZKTools {
    private static CuratorFramework client;

    public static void main(String[] args) throws Exception {
        client = CuratorFrameworkFactory.newClient("127.0.0.1:2181", 60 * 1000, 60 * 1000, new ExponentialBackoffRetry(1000, 3));
        client.start();

        configuratorsApp();

        System.in.read();
    }

    private void onlineOffline() {

    }

    /**
     * 应用级别配置器 - 为应用设置全局参数
     * 该方法演示如何通过 ZooKeeper 动态配置 Dubbo 应用的参数
     */
    public static void configuratorsApp() {
        String str = "---\n" + 
                    "configVersion: v2.7\n" +             // 配置版本
                    "scope: application\n" +              // 配置作用域：应用级别
                    "key: concurrency-middle\n" +         // 目标应用名称
                    "enabled: true\n" +                   // 是否启用该配置
                    "configs:\n" + 
                    "- addresses: [\"0.0.0.0\"]\n" +      // 匹配所有消费者实例
                    "  side: consumer\n" +                // 应用作为消费者角色时
                    "  parameters:\n" + 
                    "    timeout: 5000\n" +               // 设置消费方超时为5000ms
                    "- addresses: [\"0.0.0.0:20881\"]\n" + // 匹配指定端口的提供者实例
                    "  side: provider\n" +                // 应用作为提供者角色时
                    "  parameters:\n" + 
                    "    timeout: 4000";                  // 设置提供方超时为4000ms
        
        // 将配置写入ZooKeeper路径
        String path = "/dubbo/config/concurrency-middle/configurators";
        createNode(path, str);
    }

    /**
     * 标签路由配置 - 基于标签的流量路由控制
     * 该方法演示如何设置基于标签的路由规则，将带有特定标签的请求路由到指定的服务实例
     */
    public static void generateAppevelRouter() {
        String str = "---\n" + 
                    "force: false\n" +                    // 非强制执行，当没有匹配标签的提供者时可以路由到其他提供者
                    "runtime: true\n" +                   // 运行时可动态修改生效
                    "enabled: true\n" +                   // 是否启用该配置
                    "priority: 1\n" +                     // 路由规则优先级
                    "key: governance-tagrouter-provider\n" + // 目标应用名称
                    "tags:\n" + 
                    "  - name: tag1\n" +                  // 定义标签1
                    "    addresses: [\"30.5.121.131:20880\"]\n" + // 标签1对应的服务实例地址
                    "  - name: tag2\n" +                  // 定义标签2
                    "    addresses: [\"30.5.121.131:20881\"]\n" + // 标签2对应的服务实例地址
                    "...";
        
        // 将配置写入ZooKeeper路径
        String path = "/dubbo/config/governance-tagrouter-provider/tag-router";
        createNode(path, str);
        
        // 使用说明:
        // 消费者可以通过 RpcContext.getContext().setAttachment("dubbo.tag", "tag1") 设置请求标签
        // 带有tag1标签的请求将被路由到30.5.121.131:20880实例
        // 带有tag2标签的请求将被路由到30.5.121.131:20881实例
    }

    /**
     * 条件路由配置 - 基于条件表达式的精细路由控制
     * 该方法演示如何设置基于条件表达式的路由规则，通过灵活的条件筛选提供者实例
     */
    public static void generateConditionRoute() {
        String path = "/dubbo/config/org.apache.dubbo.samples.chain.BackendService:1.0.0/condition-router";
        String str = "---\n" +
                    "scope: service\n" +                  // 配置作用域：服务级别
                    "force: true\n" +                     // 强制执行，即使没有可路由的提供者也不会违反规则
                    "runtime: true\n" +                   // 运行时可动态修改生效
                    "enabled: true\n" +                   // 是否启用该配置
                    "priority: 2\n" +                     // 路由规则优先级，数字越大优先级越高
                    "key: org.apache.dubbo.samples.chain.BackendService:1.0.0\n" + // 目标服务接口和版本
                    "conditions:\n" +
                    "  - => host != 172.22.3.91\n" +      // 条件表达式：将请求路由到非172.22.3.91主机的实例
                    "...";
        createNode(path, str);
        
        // 条件表达式说明:
        // 格式为: [消费者匹配条件] => [提供者匹配条件]
        // 本例中省略了消费者条件，表示对所有消费者请求生效
        // 提供者条件 "host != 172.22.3.91" 表示排除该主机上的实例
        // 支持的条件操作符: =, !=, >, <, contains, startsWith, endsWith, &(与), |(或)
    }

    private static void setData(String path, String data) throws Exception {
        client.setData().forPath(path, data.getBytes());
    }

    private static String pathToKey(String path) {
        if (StringUtils.isEmpty(path)) {
            return path;
        }
        return path.replace("/dubbo/config/", "").replaceAll("/", ".");
    }

    private static void createNode(String path, String str) {
        try {
            if (client.checkExists().forPath(path) == null) {
                client.create().creatingParentsIfNeeded().forPath(path);
            }
            setData(path, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
