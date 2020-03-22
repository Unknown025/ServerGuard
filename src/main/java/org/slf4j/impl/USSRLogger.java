package org.slf4j.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * PACKAGE: org.slf4j.impl
 * DATE: 2019-02-17
 * TIME: 11:26
 * PROJECT: ussr-bot
 */
public class USSRLogger extends org.slf4j.helpers.MarkerIgnoringBase {
    private static final Level LOG_LEVEL_TRACE = Level.TRACE;
    private static final Level LOG_LEVEL_DEBUG = Level.DEBUG;
    private static final Level LOG_LEVEL_INFO = Level.INFO;
    private static final Level LOG_LEVEL_WARN = Level.WARN;
    private static final Level LOG_LEVEL_ERROR = Level.ERROR;
    public static Level defaultLogLevel = Level.INFO;
    private final String name;
    private final Logger logger;
    private Level currentLogLevel = defaultLogLevel;

    public USSRLogger(String name) {
        this.name = name;
        this.logger = LogManager.getLogger(name);
    }

    @Deprecated
    private void log(Level level, String message, Throwable throwable) {
        if (!isLevelEnabled(level))
            return;

        logger.log(level, message, throwable);
    }

    private void formatAndLog(Level level, String format, Object... arguments) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    private boolean isLevelEnabled(Level logLevel) {
        return (logLevel.intLevel() >= currentLogLevel.intLevel());
    }

    @Override
    public boolean isTraceEnabled() {
        return isLevelEnabled(LOG_LEVEL_TRACE);
    }

    @Override
    public void trace(String s) {
        log(LOG_LEVEL_TRACE, s, null);
    }

    @Override
    public void trace(String s, Object o) {
        formatAndLog(LOG_LEVEL_TRACE, s, o, null);
    }

    @Override
    public void trace(String s, Object o, Object o1) {
        formatAndLog(LOG_LEVEL_TRACE, s, o, o1);
    }

    @Override
    public void trace(String s, Object... objects) {
        formatAndLog(LOG_LEVEL_TRACE, s, objects);
    }

    @Override
    public void trace(String s, Throwable throwable) {
        log(LOG_LEVEL_TRACE, s, throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return isLevelEnabled(LOG_LEVEL_DEBUG);
    }

    @Override
    public void debug(String s) {
        log(LOG_LEVEL_DEBUG, s, null);
    }

    @Override
    public void debug(String s, Object o) {
        formatAndLog(LOG_LEVEL_DEBUG, s, o, null);
    }

    @Override
    public void debug(String s, Object o, Object o1) {
        formatAndLog(LOG_LEVEL_DEBUG, s, o, o1);
    }

    @Override
    public void debug(String s, Object... objects) {
        formatAndLog(LOG_LEVEL_DEBUG, s, objects);
    }

    @Override
    public void debug(String s, Throwable throwable) {
        log(LOG_LEVEL_DEBUG, s, throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return isLevelEnabled(LOG_LEVEL_INFO);
    }

    @Override
    public void info(String s) {
        log(LOG_LEVEL_INFO, s, null);
    }

    @Override
    public void info(String s, Object o) {
        formatAndLog(LOG_LEVEL_INFO, s, o, null);
    }

    @Override
    public void info(String s, Object o, Object o1) {
        formatAndLog(LOG_LEVEL_INFO, s, o, o1);
    }

    @Override
    public void info(String s, Object... objects) {
        formatAndLog(LOG_LEVEL_INFO, s, objects);
    }

    @Override
    public void info(String s, Throwable throwable) {
        log(LOG_LEVEL_INFO, s, throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    @Override
    public void warn(String s) {
        log(LOG_LEVEL_WARN, s, null);
    }

    @Override
    public void warn(String s, Object o) {
        formatAndLog(LOG_LEVEL_WARN, s, o, null);
    }

    @Override
    public void warn(String s, Object... objects) {
        formatAndLog(LOG_LEVEL_WARN, s, objects);
    }

    @Override
    public void warn(String s, Object o, Object o1) {
        formatAndLog(LOG_LEVEL_WARN, s, o, o1);
    }

    @Override
    public void warn(String s, Throwable throwable) {
        log(LOG_LEVEL_WARN, s, throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return isLevelEnabled(LOG_LEVEL_WARN);
    }

    @Override
    public void error(String s) {
        log(LOG_LEVEL_ERROR, s, null);
    }

    @Override
    public void error(String s, Object o) {
        formatAndLog(LOG_LEVEL_ERROR, s, o, null);
    }

    @Override
    public void error(String s, Object o, Object o1) {
        formatAndLog(LOG_LEVEL_ERROR, s, o, o1);
    }

    @Override
    public void error(String s, Object... objects) {
        formatAndLog(LOG_LEVEL_ERROR, s, objects);
    }

    @Override
    public void error(String s, Throwable throwable) {
        log(LOG_LEVEL_ERROR, s, throwable);
    }

    @Override
    public String toString() {
        return name;
    }
}
