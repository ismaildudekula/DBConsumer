package org.ismail;

import org.ismail.service.DatabaseService;
import org.ismail.service.KafkaConsumerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public class DBConsumerApp {
    private static final Logger logger = LoggerFactory.getLogger(DBConsumerApp.class);

    public static void main(String[] args) {
        // Initialize database connection
        DataSource dataSource = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // Initialize services
        DatabaseService databaseService = new DatabaseService(jdbcTemplate);
        KafkaConsumerService kafkaConsumerService = new KafkaConsumerService(databaseService);

        // Start consuming
        Thread consumerThread = new Thread(() -> {
            logger.info("Starting Kafka consumer for DB Ingestion - market depth data");
            kafkaConsumerService.startConsuming();
        });
        consumerThread.start();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down consumer...");
            kafkaConsumerService.shutdown();
            try {
                consumerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
    }

    private static DataSource getDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/kotbotdcx");
        dataSource.setUsername("root");
        dataSource.setPassword("ME@mysql1@ok");
        return dataSource;
    }
}

