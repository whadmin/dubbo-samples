package org.apache.dubbo.samples.callback.api;

import java.math.BigDecimal;

// 回调监听器接口（由消费者实现）
public interface StockPriceListener {
    // 价格变化时，服务端调用此方法通知客户端
    void onPriceChanged(String code, BigDecimal price);
}
