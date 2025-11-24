package com.example.kafka.config;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public final class LoggingConfigurator {
    private LoggingConfigurator() {
    }

    public static void configure() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s%n");
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "false");
        System.setProperty("org.slf4j.simpleLogger.showThreadName", "false");
        System.setProperty("org.slf4j.simpleLogger.showLogName", "false");

        Logger rootLogger = LogManager.getLogManager().getLogger("");
        if (rootLogger != null) {
            rootLogger.setLevel(Level.INFO);
            for (Handler handler : rootLogger.getHandlers()) {
                handler.setLevel(Level.INFO);
                if (handler instanceof ConsoleHandler) {
                    handler.setFormatter(new SimpleFormatter());
                }
            }
        }

        Logger kafkaLogger = Logger.getLogger("org.apache.kafka");
        kafkaLogger.setLevel(Level.WARNING);
        Logger kafkaClientLogger = Logger.getLogger("org.apache.kafka.clients");
        kafkaClientLogger.setLevel(Level.WARNING);
    }
}
