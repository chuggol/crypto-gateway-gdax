package org.chuggol.crypto.gateway.gdax;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.gdax.GDAXStreamingExchange;
import io.reactivex.disposables.Disposable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.concurrent.*;

@Service
public class GdaxInboundGatewayRunner implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(GdaxInboundGatewayRunner.class);
    private final StreamingExchange exchange = StreamingExchangeFactory.INSTANCE.createExchange(GDAXStreamingExchange.class.getName());
    private Disposable tradeSubscription;
    private Gson gson = new GsonBuilder().create();
    private Publisher publisher;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception{
        connectAndStart();
        initWatchDog();
    }

    private void initWatchDog() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new WatchDog(), 10, 10, TimeUnit.SECONDS);
    }

    private void connectAndStart() throws IOException {
        exchange.connect().blockingAwait();
        TopicName topicName = TopicName.create("crypto-175617", "inbound-trades");
        publisher = Publisher.defaultBuilder(topicName).build();


        tradeSubscription = exchange.getStreamingMarketDataService()
                .getTrades(CurrencyPair.BTC_USD)
                .subscribe(gdaxTrade -> {
                    if (!"0".equals(gdaxTrade.getId())) {
                        Trade trade = getDomainTrade(gdaxTrade);
                        String tradeJson = toJson(trade);
                        ByteString tradeData = ByteString.copyFromUtf8(tradeJson);
                        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(tradeData).build();
                        LOG.info("Incoming gdaxTrade: {}", tradeJson);
                        ApiFuture<String> future = publisher.publish(pubsubMessage);
                        try {
                            LOG.info("Published with id: {}", future.get());
                        } catch (Exception ex) {
                            LOG.error("Got exception: ", ex);
                        }


                    }
                }, throwable -> {
                    LOG.error("Error in subscribing trades.", throwable);
                });

    }

    private boolean isHealthy() {
        return !tradeSubscription.isDisposed() && exchange.isAlive();
    }

    private String toJson(Trade trade) {
        return gson.toJson(trade);
    }

    private Trade getDomainTrade(org.knowm.xchange.dto.marketdata.Trade gdaxTrade) {
        Trade aTrade = new Trade();
        aTrade.setId(gdaxTrade.getId());
        aTrade.setMarket("GDAX");
        aTrade.setCurrency(gdaxTrade.getCurrencyPair().counter.toString());
        aTrade.setTradedAsset(gdaxTrade.getCurrencyPair().base.toString());
        aTrade.setExecutionTime(gdaxTrade.getTimestamp().toInstant());
        aTrade.setPrice(gdaxTrade.getPrice());
        aTrade.setQuantity(gdaxTrade.getTradableAmount());
        aTrade.setSide(gdaxTrade.getType() == Order.OrderType.BID ? "BUY" : "SELL");

        return aTrade;
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
