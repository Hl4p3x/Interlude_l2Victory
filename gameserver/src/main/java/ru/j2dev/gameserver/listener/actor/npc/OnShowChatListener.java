package ru.j2dev.gameserver.listener.actor.npc;


import ru.j2dev.gameserver.listener.NpcListener;
import ru.j2dev.gameserver.model.instances.NpcInstance;

/**
 * @author PaInKiLlEr
 */
public interface OnShowChatListener extends NpcListener {

    void onShowChat(NpcInstance actor);
}
