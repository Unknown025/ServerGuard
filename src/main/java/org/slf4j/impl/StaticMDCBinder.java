package org.slf4j.impl;

import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;

/**
 * PACKAGE: org.slf4j.impl
 * DATE: 2019-02-17
 * TIME: 12:37
 * PROJECT: ussr-bot
 */
public class StaticMDCBinder {
    /**
     * The unique instance of this class.
     */
    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    private StaticMDCBinder() {
    }

    /**
     * Return the singleton of this class.
     *
     * @return the StaticMDCBinder singleton
     * @since 1.7.14
     */
    public static final StaticMDCBinder getSingleton() {
        return SINGLETON;
    }

    /**
     * Currently this method always returns an instance of
     * {@link StaticMDCBinder}.
     * @return Returns a new MDCAdapter.
     */
    public MDCAdapter getMDCA() {
        return new NOPMDCAdapter();
    }

    public String getMDCAdapterClassStr() {
        return NOPMDCAdapter.class.getName();
    }
}
