package ru.j2dev.gameserver.listener.reflection;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.gameserver.model.entity.Reflection;

public interface OnReflectionCollapseListener extends Listener<Reflection> {
    void onReflectionCollapse(final Reflection p0);
}
