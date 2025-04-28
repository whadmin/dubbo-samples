package org.apache.dubbo.samples.generic.call;

import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.service.GenericService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

/**
 * 泛化调用消费者示例
 * 演示了通过泛化调用方式调用远程服务的各种模式
 */
public class GenericCallConsumer {

    // 泛化服务接口，用于执行泛化调用
    private static GenericService genericService;

    public static void main(String[] args) throws Exception {
        // 创建应用配置
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName("generic-call-consumer");

        // 创建注册中心配置
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress("zookeeper://127.0.0.1:2181");

        // 创建泛化引用配置
        ReferenceConfig<GenericService> referenceConfig = new ReferenceConfig<>();
        // 设置接口名称，虽然是泛化调用，但仍需指定接口名称用于服务发现
        referenceConfig.setInterface("org.apache.dubbo.samples.generic.call.api.HelloService");
        referenceConfig.setApplication(applicationConfig);
        // 设置为泛化调用模式
        referenceConfig.setGeneric("true");
        // 设置为异步调用模式
        referenceConfig.setAsync(true);
        // 设置超时时间
        referenceConfig.setTimeout(7000);
        applicationConfig.setRegistry(registryConfig);

        // 获取泛化服务
        genericService = referenceConfig.get();

        // 执行各种不同模式的泛化调用
        invokeSayHello();                     // 同步泛化调用同步方法
        invokeSayHelloAsync();                // 同步泛化调用异步方法
        invokeAsyncSayHelloAsync();           // 异步泛化调用异步方法
        invokeAsyncSayHello();                // 异步泛化调用同步方法
//        invokeSayHelloAsyncComplex();         // 同步泛化调用返回复杂对象的异步方法
        asyncInvokeSayHelloAsyncComplex();    // 异步泛化调用返回复杂对象的异步方法
//        invokeSayHelloAsyncGenericComplex();  // 同步泛化调用返回泛型复杂对象的异步方法
        asyncInvokeSayHelloAsyncGenericComplex(); // 异步泛化调用返回泛型复杂对象的异步方法
    }

    /**
     * 同步泛化调用同步方法示例
     * 调用 sayHello 方法，并通过 RpcContext 获取异步结果
     */
    public static void invokeSayHello() throws InterruptedException {
        // 执行泛化调用，传入方法名、参数类型和参数值
        Object result = genericService.$invoke("sayHello", new String[]{"java.lang.String"}, new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);

        // 获取异步结果
        CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
        future.whenComplete((value, t) -> {
            System.err.println("invokeSayHello(whenComplete): " + value);
            latch.countDown();
        });

        // 由于设置了异步调用，这里的返回值为null
        System.err.println("invokeSayHello(return): " + result);
        latch.await();
    }

    /**
     * 同步泛化调用异步方法示例
     * 调用 sayHelloAsync 方法，该方法本身返回 CompletableFuture
     */
    public static void invokeSayHelloAsync() throws InterruptedException {
        // 执行泛化调用
        Object result = genericService.$invoke("sayHelloAsync", new String[]{"java.lang.String"}, new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);

        // 获取异步结果
        CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
        future.whenComplete((value, t) -> {
            System.err.println("invokeSayHelloAsync(whenComplete): " + value);
            latch.countDown();
        });

        // 由于设置了异步调用，这里的返回值为null
        System.err.println("invokeSayHelloAsync(return): " + result);
        latch.await();
    }

    /**
     * 异步泛化调用异步方法示例
     * 使用 $invokeAsync 方法调用 sayHelloAsync 异步方法
     */
    public static void invokeAsyncSayHelloAsync() throws Exception {
        // 执行异步泛化调用
        CompletableFuture<Object> future = genericService.$invokeAsync("sayHelloAsync",
                new String[]{"java.lang.String"}, new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);
        future.whenComplete((value, t) -> {
            System.err.println("invokeAsyncSayHelloAsync(whenComplete): " + value);
            latch.countDown();
        });
        latch.await();
    }

    /**
     * 异步泛化调用同步方法示例
     * 使用 $invokeAsync 方法调用 sayHello 同步方法
     */
    public static void invokeAsyncSayHello() throws Exception {
        // 执行异步泛化调用
        CompletableFuture<Object> future = genericService.$invokeAsync("sayHello",
                new String[]{"java.lang.String"}, new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);
        future.whenComplete((value, t) -> {
            System.err.println("invokeAsyncSayHello(whenComplete): " + value);
            latch.countDown();
        });
        latch.await();
    }

    /**
     * 同步泛化调用返回复杂对象的异步方法示例
     * 调用 sayHelloAsyncComplex 方法，该方法返回复杂对象
     */
    public static void invokeSayHelloAsyncComplex() throws Exception {
        // 执行泛化调用
        Object result = genericService.$invoke("sayHelloAsyncComplex", new String[]{"java.lang.String"},
                new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);

        // 获取异步结果
        CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
        future.whenComplete((value, t) -> {
            System.err.println("invokeSayHelloAsyncComplex(whenComplete): " + value);
            latch.countDown();
        });

        System.err.println("invokeSayHelloAsync(return): " + result);
        latch.await();
    }

    /**
     * 异步泛化调用返回复杂对象的异步方法示例
     * 使用 $invokeAsync 方法调用 sayHelloAsyncComplex 方法
     */
    public static void asyncInvokeSayHelloAsyncComplex() throws Exception {
        // 执行异步泛化调用
        CompletableFuture<Object> future = genericService.$invokeAsync("sayHelloAsyncComplex",
                new String[]{"java.lang.String"}, new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);

        future.whenComplete((value, t) -> {
            System.err.println("asyncInvokeSayHelloAsyncComplex(whenComplete): " + value);
            latch.countDown();
        });

        latch.await();
    }

    /**
     * 同步泛化调用返回泛型复杂对象的异步方法示例
     * 调用 sayHelloAsyncGenericComplex 方法，该方法返回泛型复杂对象
     */
    public static void invokeSayHelloAsyncGenericComplex() throws Exception {
        // 执行泛化调用
        Object result = genericService.$invoke("sayHelloAsyncGenericComplex", new String[]{"java.lang.String"},
                new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);

        // 获取异步结果
        CompletableFuture<String> future = RpcContext.getContext().getCompletableFuture();
        future.whenComplete((value, t) -> {
            System.err.println("invokeSayHelloAsyncGenericComplex(whenComplete): " + value);
            latch.countDown();
        });

        System.err.println("invokeSayHelloAsyncGenericComplex(return): " + result);
        latch.await();
    }

    /**
     * 异步泛化调用返回泛型复杂对象的异步方法示例
     * 使用 $invokeAsync 方法调用 sayHelloAsyncGenericComplex 方法
     */
    public static void asyncInvokeSayHelloAsyncGenericComplex() throws Exception {
        // 执行异步泛化调用
        CompletableFuture<Object> future = genericService.$invokeAsync("sayHelloAsyncGenericComplex",
                new String[]{"java.lang.String"}, new Object[]{"world"});
        CountDownLatch latch = new CountDownLatch(1);

        future.whenComplete((value, t) -> {
            System.err.println("asyncInvokeSayHelloAsyncGenericComplex(whenComplete): " + value);
            latch.countDown();
        });

        latch.await();
    }
}