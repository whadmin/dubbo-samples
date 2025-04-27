package org.apache.dubbo.samples.callback.api;

/**
 * 回调监听器接口
 * 服务提供者将调用此接口通知消费者
 */
public interface CallbackListener {
    /**
     * 当服务端有数据变更时调用此方法
     * 
     * @param msg 变更的消息内容
     */
    void changed(String msg);
}
