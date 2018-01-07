package org.chuggol.crypto.gateway.gdax;

import com.google.cloud.pubsub.v1.Publisher;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;
import info.bitrich.xchangestream.service.netty.WebSocketClientHandler;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.currency.CurrencyPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class GdaxInboundGatewayRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(GdaxInboundGatewayRunner.class);
    private final GDAXStreamingExchange exchange = (GDAXStreamingExchange)StreamingExchangeFactory.INSTANCE.createExchange(GDAXStreamingExchange.class.getName());
    private Disposable tradeSubscription;

    private Publisher publisher;
    private boolean isDisconnectedAlert = false;
    @Autowired
    PubSubFactory pubSubFactory;
    private TradeParser toLocalDomain = new TradeParser();
    private TradeToJsonMarshaller toJson = new TradeToJsonMarshaller();

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        connectAndStart();
        initWatchDog();
    }

    private void initWatchDog() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new WatchDog(), 10, 10, TimeUnit.SECONDS);
    }

    private void connectAndStart() throws IOException {
        ProductSubscription subscription = ProductSubscription.create()
                .addTrades(CurrencyPair.BTC_USD)
                .build();
        exchange.connect(subscription).blockingAwait();

        tradeSubscription = exchange.getStreamingMarketDataService()
                .getTrades(CurrencyPair.BTC_USD)
                .map(toLocalDomain)
                .map(toJson)
                .map(message -> {
                    LOG.info(message);
                    return message;
                })
                .flatMap(pubSubFactory.outboundTradesObserver())
                .subscribe();

        exchange.setChannelInactiveHandler(new WebSocketClientHandler.WebSocketMessageHandler() {
            @Override
            public void onMessage(String message) {
                isDisconnectedAlert = true;
            }
        });

    }

    private boolean isHealthy() {
        return !tradeSubscription.isDisposed() && exchange.isAlive() && !isDisconnectedAlert;
    }



    @PreDestroy
    public void cleanupBeforeExit() throws Exception {
        if (tradeSubscription != null && !tradeSubscription.isDisposed()) {
            tradeSubscription.dispose();
        }

        exchange.disconnect().blockingAwait();

        publisher.shutdown();
    }

    class WatchDog implements Runnable {

        @Override
        public void run() {
            LOG.info("Checking subscription health");
            if (!isHealthy()) {
                try {
                    cleanupBeforeExit();
                } catch (Exception ex) {
                    LOG.warn("Caught exception during restart/cleanup", ex);
                }

                try {
                    connectAndStart();
                } catch (Exception ex) {
                    LOG.warn("Caught exception during start", ex);
                }
            }
        }
    }
}
