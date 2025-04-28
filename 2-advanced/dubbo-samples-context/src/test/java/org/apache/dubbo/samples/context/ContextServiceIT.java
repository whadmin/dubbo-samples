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

package org.apache.dubbo.samples.context;

import org.apache.dubbo.samples.context.api.ContextService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath*:/spring/dubbo-context-consumer.xml"})
public class ContextServiceIT {
    @Autowired
    private ContextService contextService;

    @Test
    public void test() throws Exception {
        // 测试RPC调用和上下文传递
        String result = contextService.sayHello("dubbo");
        // 验证结果中包含预期的上下文信息
        Assert.assertTrue(result.startsWith("Hello dubbo, response from"));
        Assert.assertTrue(result.contains("isProviderSide: true"));
        Assert.assertTrue(result.contains("local: context-"));
    }
}
