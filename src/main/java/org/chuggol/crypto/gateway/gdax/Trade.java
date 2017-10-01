package org.chuggol.crypto.gateway.gdax;

import java.math.BigDecimal;
import java.time.Instant;

public class Trade {
    private final int version = 1;
    private String id;
    private Instant executionTime;
    private String market;
    private String currencyTraded;
    private String currencyBase;
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

    public String getCurrencyTraded() {
        return currencyTraded;
    }

    public void setCurrencyTraded(String currencyTraded) {
        this.currencyTraded = currencyTraded;
    }

    public String getCurrencyBase() {
        return currencyBase;
    }

    public void setCurrencyBase(String currencyBase) {
        this.currencyBase = currencyBase;
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
