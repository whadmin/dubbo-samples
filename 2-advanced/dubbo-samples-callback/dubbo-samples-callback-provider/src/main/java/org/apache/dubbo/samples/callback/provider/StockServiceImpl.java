package org.apache.dubbo.samples.callback.provider;

import org.apache.dubbo.config.annotation.DubboService;
import org.apache.dubbo.config.annotation.Method;
import org.apache.dubbo.config.annotation.Argument;
import org.apache.dubbo.samples.callback.api.StockPriceListener;
import org.apache.dubbo.samples.callback.api.StockService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 股票服务实现类
 * 将监听管理和价格模拟分成两个独立的线程处理
 */
@DubboService(methods = {
        @Method(name = "subscribePrice", arguments = {@Argument(index = 1, callback = true)})
})
public class StockServiceImpl implements StockService {
    // 存储每个股票代码及其当前价格
    private final Map<String, BigDecimal> stockPrices = new ConcurrentHashMap<>();

    // 存储每个股票代码及其监听器列表
    private final Map<String, List<StockPriceListener>> listeners = new ConcurrentHashMap<>();

    // 记录每个股票的通知次数
    private final Map<String, AtomicInteger> notificationCounts = new ConcurrentHashMap<>();

    // 用于生成随机价格
    private final Random random = new Random();

    // 价格模拟定时器和通知定时器
    private final ScheduledExecutorService priceSimulator;
    private final ScheduledExecutorService notificationProcessor;

    public StockServiceImpl() {
        // 初始化一些股票数据
        initStockData();

        // 创建用于模拟价格变动的定时器
        priceSimulator = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "price-simulator");
            t.setDaemon(true);
            return t;
        });

        // 创建用于处理通知的定时器
        notificationProcessor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "notification-processor");
            t.setDaemon(true);
            return t;
        });

        // 启动价格模拟器
        startPriceSimulator();

        // 启动通知处理器
        startNotificationProcessor();

        System.out.println("股票服务已启动，开始模拟价格变动...");
    }

    /**
     * 初始化股票数据
     */
    private void initStockData() {
        // 添加一些初始股票及价格
        stockPrices.put("AAPL", new BigDecimal("150.00"));
        stockPrices.put("GOOGL", new BigDecimal("2800.00"));
        stockPrices.put("MSFT", new BigDecimal("280.00"));
        stockPrices.put("AMZN", new BigDecimal("3200.00"));
        stockPrices.put("TSLA", new BigDecimal("700.00"));

        // 初始化通知计数器
        for (String code : stockPrices.keySet()) {
            notificationCounts.put(code, new AtomicInteger(0));
        }
    }

    /**
     * 启动价格模拟器
     * 每隔2秒随机更新股票价格
     */
    private void startPriceSimulator() {
        priceSimulator.scheduleAtFixedRate(() -> {
            try {
                // 随机选择一些股票更新价格
                Set<String> codes = stockPrices.keySet();
                for (String code : codes) {
                    if (random.nextBoolean()) { // 50%的概率更新价格
                        updateStockPrice(code);
                    }
                }
            } catch (Exception e) {
                System.err.println("价格模拟器异常: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    /**
     * 启动通知处理器
     * 每隔1秒检查是否有价格变动需要通知
     */
    private void startNotificationProcessor() {
        notificationProcessor.scheduleAtFixedRate(() -> {
            try {
                // 处理所有股票的通知
                for (Map.Entry<String, List<StockPriceListener>> entry : listeners.entrySet()) {
                    String stockCode = entry.getKey();
                    // 检查此股票是否有价格数据
                    if (!stockPrices.containsKey(stockCode)) {
                        continue;
                    }

                    // 获取当前价格
                    BigDecimal currentPrice = stockPrices.get(stockCode);
                    List<StockPriceListener> stockListeners = entry.getValue();

                    // 如果没有监听器，跳过
                    if (stockListeners.isEmpty()) {
                        continue;
                    }

                    // 通知所有监听器
                    notifyListeners(stockCode, currentPrice, stockListeners);
                }
            } catch (Exception e) {
                System.err.println("通知处理器异常: " + e.getMessage());
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    /**
     * 更新指定股票的价格
     */
    private void updateStockPrice(String stockCode) {
        if (!stockPrices.containsKey(stockCode)) {
            return;
        }

        BigDecimal oldPrice = stockPrices.get(stockCode);

        // 生成-5%到+5%的随机波动
        double change = (random.nextDouble() - 0.5) * 0.1; // -0.05 到 0.05
        BigDecimal newPrice = oldPrice.multiply(BigDecimal.ONE.add(new BigDecimal(change)))
                .setScale(2, RoundingMode.HALF_UP);

        // 更新价格
        stockPrices.put(stockCode, newPrice);

        // 记录价格变动
        System.out.printf("股票 %s 价格从 %.2f 变为 %.2f%n",
                stockCode, oldPrice.doubleValue(), newPrice.doubleValue());
    }

    /**
     * 通知指定股票的所有监听器
     */
    private void notifyListeners(String stockCode, BigDecimal price, List<StockPriceListener> stockListeners) {
        List<StockPriceListener> toRemove = new ArrayList<>();

        for (StockPriceListener listener : stockListeners) {
            try {
                // 调用消费者的回调方法
                listener.onPriceChanged(stockCode, price);

                // 更新通知计数
                int count = notificationCounts.get(stockCode).incrementAndGet();
                System.out.printf("已通知股票 %s 的价格变动 %d 次%n", stockCode, count);
            } catch (Throwable t) {
                // 通知失败，记录并准备移除
                System.err.println("通知客户端失败: " + t.getMessage());
                toRemove.add(listener);
            }
        }

        // 移除失效的监听器
        if (!toRemove.isEmpty()) {
            stockListeners.removeAll(toRemove);
            System.out.printf("移除了 %d 个失效的 %s 股票监听器%n", toRemove.size(), stockCode);
        }
    }

    @Override
    public void subscribePrice(String stockCode, StockPriceListener listener) {
        // 检查股票代码是否存在
        if (!stockPrices.containsKey(stockCode)) {
            // 如果是新股票，添加到价格表
            stockPrices.put(stockCode, new BigDecimal("100.00"));
            notificationCounts.put(stockCode, new AtomicInteger(0));
            System.out.println("添加新股票: " + stockCode);
        }

        // 添加监听器
        listeners.computeIfAbsent(stockCode, k -> new CopyOnWriteArrayList<>()).add(listener);
        System.out.println("客户端订阅了股票: " + stockCode);

        // 立即发送一次当前价格
        BigDecimal currentPrice = stockPrices.get(stockCode);
        try {
            listener.onPriceChanged(stockCode, currentPrice);
            System.out.println("已发送 " + stockCode + " 的初始价格: " + currentPrice);
        } catch (Throwable t) {
            System.err.println("发送初始价格失败: " + t.getMessage());
        }
    }

    @Override
    public void unsubscribePrice(String stockCode, StockPriceListener listener) {
        // 移除监听器
        if (listeners.containsKey(stockCode)) {
            boolean removed = listeners.get(stockCode).remove(listener);
            if (removed) {
                System.out.println("客户端取消订阅股票: " + stockCode);
            }

            // 如果此股票没有监听器了，考虑清理资源
            if (listeners.get(stockCode).isEmpty()) {
                listeners.remove(stockCode);
                System.out.println("移除空的监听器列表: " + stockCode);
            }
        }
    }

    // 服务关闭时清理资源
    public void destroy() {
        priceSimulator.shutdownNow();
        notificationProcessor.shutdownNow();
        System.out.println("股票服务已关闭");
    }
}
