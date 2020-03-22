package org.slf4j.impl;

import org.slf4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * PACKAGE: com.nkvd.disconceal.logger
 * DATE: 2019-02-17
 * TIME: 11:27
 * PROJECT: ussr-bot
 */
public class USSRLoggerFactory implements org.slf4j.ILoggerFactory {
    private ConcurrentMap<String, Logger> loggerMap;

    public USSRLoggerFactory() {
        loggerMap = new ConcurrentHashMap<>();
    }

    @Override
    public Logger getLogger(String name) {
        Logger logger = loggerMap.get(name);
        if (logger != null) {
            return logger;
        } else {
            Logger newInstance = new USSRLogger(name);
            Logger oldInstance = loggerMap.putIfAbsent(name, newInstance);
            return oldInstance == null ? newInstance : oldInstance;
        }
    }

    void reset() {
        loggerMap.clear();
    }
}
