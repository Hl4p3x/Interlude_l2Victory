package ru.j2dev.commons.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class RunnableImpl implements Runnable {
    public static final Logger LOGGER = LoggerFactory.getLogger(RunnableImpl.class);

    public abstract void runImpl();

    @Override
    public final void run() {
        try {
            runImpl();
        } catch (Throwable e) {
            LOGGER.error("Exception: RunnableImpl.run(): " + e, e);
        }
    }
}
