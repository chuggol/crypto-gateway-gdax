package org.chuggol.crypto.gateway.gdax;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class GdaxInboundGatewayRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(GdaxInboundGatewayRunner.class);
    private final StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(GDAXStreamingExchange.class.getName());
    private Disposable tradeSubscription;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        exchange.connect().blockingAwait();

        tradeSubscription = exchange.getStreamingMarketDataService()
                .getTrades(CurrencyPair.BTC_USD)
                .subscribe(trade -> {
                    if (!"0".equals(trade.getId())) {
                        LOG.info("Incoming trade: {}", trade);
                    }
                }, throwable -> {
                    LOG.error("Error in subscribing trades.", throwable);
                });
    }

    @PreDestroy
    public void cleanupBeforeExit() {
        if (tradeSubscription != null && !tradeSubscription.isDisposed()) {
            tradeSubscription.dispose();
        }

        exchange.disconnect().blockingAwait();
    }
}
