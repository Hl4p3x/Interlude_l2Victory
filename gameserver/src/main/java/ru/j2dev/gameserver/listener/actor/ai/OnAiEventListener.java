package ru.j2dev.gameserver.listener.actor.ai;

import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.listener.AiListener;
import ru.j2dev.gameserver.model.Creature;

public interface OnAiEventListener extends AiListener {
    void onAiEvent(final Creature p0, final CtrlEvent p1, final Object[] p2);
}
