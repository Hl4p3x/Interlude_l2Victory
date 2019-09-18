package ru.j2dev.gameserver.model.reference;

import ru.j2dev.commons.lang.reference.AbstractHardReference;

public class L2Reference<T> extends AbstractHardReference<T> {
    public L2Reference(final T reference) {
        super(reference);
    }
}
