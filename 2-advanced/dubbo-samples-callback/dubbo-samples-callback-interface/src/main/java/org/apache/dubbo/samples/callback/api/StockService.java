package org.apache.dubbo.samples.callback.api;

// 服务接口
public interface StockService {
    // 订阅股票价格变动，传入回调对象
    void subscribePrice(String stockCode, StockPriceListener listener);

    // 取消订阅
    void unsubscribePrice(String stockCode, StockPriceListener listener);
}
