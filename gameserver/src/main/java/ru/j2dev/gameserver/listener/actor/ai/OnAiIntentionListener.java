package ru.j2dev.gameserver.listener.actor.ai;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.listener.AiListener;
import ru.j2dev.gameserver.model.Creature;

public interface OnAiIntentionListener extends AiListener {
    void onAiIntention(final Creature p0, final CtrlIntention p1, final Object p2, final Object p3);
}
