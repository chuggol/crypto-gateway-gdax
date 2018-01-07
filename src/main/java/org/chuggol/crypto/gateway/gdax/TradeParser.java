package org.chuggol.crypto.gateway.gdax;

import io.reactivex.functions.Function;
import org.knowm.xchange.dto.Order;

public class TradeParser implements Function<org.knowm.xchange.dto.marketdata.Trade, Trade> {
    @Override
    public Trade apply(org.knowm.xchange.dto.marketdata.Trade gdaxTrade) throws Exception {
        return Trade.builder()
                .id(gdaxTrade.getId())
                .seq(Long.parseLong(gdaxTrade.getId()))
                .market("GDAX")
                .currency(gdaxTrade.getCurrencyPair().counter.toString())
                .tradedAsset(gdaxTrade.getCurrencyPair().base.toString())
                .executionTime(gdaxTrade.getTimestamp().toInstant())
                .price(gdaxTrade.getPrice())
                .quantity(gdaxTrade.getOriginalAmount())
                .side(gdaxTrade.getType() == Order.OrderType.BID ? "BUY" : "SELL")
                .build();
    }

}
