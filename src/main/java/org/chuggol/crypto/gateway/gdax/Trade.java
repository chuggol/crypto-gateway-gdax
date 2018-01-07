package org.chuggol.crypto.gateway.gdax;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Builder
@AllArgsConstructor
@Getter
public class Trade {
    private final int version = 2;
    private String id;
    private long seq;
    private Instant executionTime;
    private String market;
    private String tradedAsset;
    private String currency;
    private BigDecimal price;
    private BigDecimal quantity;
    private String side;
}
