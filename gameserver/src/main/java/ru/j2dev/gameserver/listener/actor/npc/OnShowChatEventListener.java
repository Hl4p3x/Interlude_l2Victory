package ru.j2dev.gameserver.listener.actor.npc;


import ru.j2dev.gameserver.listener.NpcListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;

/**
 * Created by JunkyFunky
 * on 30.05.2016.
 * group j2dev
 */
public interface OnShowChatEventListener extends NpcListener {
    void onShowChatEvent(NpcInstance actor, Player player);
}
