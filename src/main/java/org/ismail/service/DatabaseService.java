package org.ismail.service;

import lombok.extern.slf4j.Slf4j;
import org.ismail.model.MarketDepthData;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DatabaseService {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveMarketData(MarketDepthData data) {
        String sql = """
            INSERT INTO market_depth_data
            (pair, top_bid, top_ask, top_bid_quantity, top_ask_quantity, timestamp)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        jdbcTemplate.update(sql,
                data.getPair(),
                data.getTopBid(),
                data.getTopAsk(),
                data.getTopBidQuantity(),
                data.getTopAskQuantity(),
                data.getTimestamp()
        );

        log.info("Saved market data for pair: {} at timestamp: {}",
                data.getPair(), data.getTimestamp());
    }
}

