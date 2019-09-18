package ru.j2dev.commons.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class LoggerObject {
    protected final Logger LOGGER;

    public LoggerObject() {
        LOGGER = LoggerFactory.getLogger(getClass());
    }

    public void error(final String st, final Exception e) {
        LOGGER.error(getClass().getSimpleName() + ": " + st, e);
    }

    public void error(final String st) {
        LOGGER.error(getClass().getSimpleName() + ": " + st);
    }

    public void warn(final String st, final Exception e) {
        LOGGER.warn(getClass().getSimpleName() + ": " + st, e);
    }

    public void warn(final String st) {
        LOGGER.warn(getClass().getSimpleName() + ": " + st);
    }

    public void info(final String st, final Exception e) {
        LOGGER.info(getClass().getSimpleName() + ": " + st, e);
    }

    public void info(final String st) {
        LOGGER.info(getClass().getSimpleName() + ": " + st);
    }
}
