package org.ismail.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "market_depth_data")
public class MarketDepthData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String pair;
    private Double topBid;
    private Double topAsk;
    private Double topBidQuantity;
    private Double topAskQuantity;
    private Long timestamp;

}

