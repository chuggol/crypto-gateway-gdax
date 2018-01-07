package org.chuggol.crypto.gateway.gdax;

import io.reactivex.Observable;
import io.reactivex.functions.Function;
import org.chuggol.crypto.gateway.gdax.pubsub.GooglePubSubPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class PubSubFactory {
    @Value("${pubsub.outbound.trades.projectName}")
    private String projectName;
    @Value("${pubsub.outbound.trades.topicName}")
    private String topicName;

    public Function<String, Observable<String>> outboundTradesObserver() throws IOException {
        return new GooglePubSubPublisher(projectName, topicName);
    }

}
