package org.apache.dubbo.samples.callback.consumer;

import org.apache.dubbo.samples.callback.api.StockPriceListener;

import java.math.BigDecimal;

public class StockPriceListenerImpl implements StockPriceListener {
    private final String clientId;

    public StockPriceListenerImpl(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void onPriceChanged(String code, BigDecimal price) {
        // 处理价格变更通知
        System.out.println("[客户端" + clientId + "] 收到股票 " + code + " 价格变动: " + price);

        // 实际业务处理...
    }
}
