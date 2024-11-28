package org.ismail.service;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.ismail.model.MarketDepthData;
import org.json.JSONObject;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class KafkaConsumerService {
    private final DatabaseService databaseService;
    private final KafkaConsumer<String, String> consumer;
    private volatile boolean running = true;

    public KafkaConsumerService(DatabaseService databaseService) {
        this.databaseService = databaseService;
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "market-depth-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        this.consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList("coindcx-updates"));
    }

    private MarketDepthData convertToMarketData(String key, String jsonValue) {
        JSONObject data = new JSONObject(jsonValue);
        JSONObject bids = data.getJSONObject("bids");
        JSONObject asks = data.getJSONObject("asks");

        // Get first entries from bids and asks
        String firstBidKey = bids.keys().next();
        String firstAskKey = asks.keys().next();

        MarketDepthData marketData = new MarketDepthData();
        marketData.setPair(key);
        marketData.setTopBid(Double.parseDouble(firstBidKey));
        marketData.setTopAsk(Double.parseDouble(firstAskKey));
        marketData.setTopBidQuantity(bids.getDouble(firstBidKey));
        marketData.setTopAskQuantity(asks.getDouble(firstAskKey));
        marketData.setTimestamp(data.getLong("ts"));

        return marketData;
    }

    public void startConsuming() {
        try {
            while (running) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
                for (ConsumerRecord<String, String> record : records) {
                    if (isConversionRateKey(record.key())) {
                        MarketDepthData marketData = convertToMarketData(record.key(), record.value());
                        databaseService.saveMarketData(marketData);
                    }
                }
            }
        } catch (WakeupException e) {
            // Ignore for shutdown
        } finally {
            consumer.close();
        }
    }

    private boolean isConversionRateKey(String key) {
        return key.endsWith("USDT_INRorderbook") || key.endsWith("TUSD_INRorderbook") || key.endsWith("USDC_INRorderbook") ||
                key.endsWith("BTC_INRorderbook") || key.endsWith("ETH_INRorderbook") || key.endsWith("DAI_INRorderbook") ||
                key.endsWith("BNB_INRorderbook") || key.endsWith("TRX_INRorderbook") || key.endsWith("XRP_INRorderbook");
    }

    public void shutdown() {
        running = false;
        consumer.wakeup();
    }
}


