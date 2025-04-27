package org.apache.dubbo.samples.callback.api;

/**
 * 回调服务接口
 * 定义消费者如何向提供者注册回调监听器
 */
public interface CallbackService {
    /**
     * 添加回调监听器
     * 
     * @param key 监听器标识键
     * @param listener 回调监听器实现
     */
    void addListener(String key, CallbackListener listener);
}
