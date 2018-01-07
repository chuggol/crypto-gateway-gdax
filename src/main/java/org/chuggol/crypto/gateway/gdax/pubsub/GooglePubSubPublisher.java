package org.chuggol.crypto.gateway.gdax.pubsub;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

import java.io.IOException;

public class GooglePubSubPublisher implements Function<String, Observable<String>> {
    private final Publisher publisher;

    public GooglePubSubPublisher(String projectId, String topic) throws IOException {
        TopicName topicName = TopicName.create(projectId, topic);
        publisher = Publisher.defaultBuilder(topicName).build();
    }

    @Override
    public Observable<String> apply(String message) throws Exception {
        ByteString data = ByteString.copyFromUtf8(message);
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
        ApiFuture<String> future = publisher.publish(pubsubMessage);
        try {
            String messageId = future.get();
            return Observable.just(messageId);
        } catch (Exception ex) {
            return Observable.error(ex);
        }
    }
}
