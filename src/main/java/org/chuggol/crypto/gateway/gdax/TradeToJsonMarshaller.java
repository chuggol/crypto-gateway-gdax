package org.chuggol.crypto.gateway.gdax;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.reactivex.functions.Function;

public class TradeToJsonMarshaller implements Function<Trade, String> {
    private Gson gson = new GsonBuilder().create();

    @Override
    public String apply(Trade trade) throws Exception {
        return gson.toJson(trade);
    }
}
