package org.chuggol.crypto.gateway.gdax;

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
    private Gson gson = new GsonBuilder().create();
    private Publisher publisher;

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
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
                        publisher.publish(pubsubMessage);

                    }
                }, throwable -> {
                    LOG.error("Error in subscribing trades.", throwable);
                });
    }

    private String toJson(Trade trade) {
        return gson.toJson(trade);
    }

    private Trade getDomainTrade(org.knowm.xchange.dto.marketdata.Trade gdaxTrade) {
        Trade aTrade = new Trade();
        aTrade.setCurrencyPair(gdaxTrade.getCurrencyPair().toString());
        aTrade.setId(gdaxTrade.getId());
        aTrade.setPrice(gdaxTrade.getPrice());
        aTrade.setQuantity(gdaxTrade.getTradableAmount());
        aTrade.setId(aTrade.getId());
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
}
