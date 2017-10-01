package org.chuggol.crypto.gateway.gdax;

import java.math.BigDecimal;
import java.time.Instant;

public class Trade {
    private final int version = 1;
    private String id;
    private Instant executionTime;
    private String market;
    private String tradedAsset;
    private String currency;
    private BigDecimal price;
    private BigDecimal quantity;
    private String side;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Instant getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Instant executionTime) {
        this.executionTime = executionTime;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getTradedAsset() {
        return tradedAsset;
    }

    public void setTradedAsset(String tradedAsset) {
        this.tradedAsset = tradedAsset;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }
}
