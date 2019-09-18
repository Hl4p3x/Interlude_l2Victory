package ru.j2dev.gameserver.handler.npcdialog;


import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;

import java.util.List;

/**
 * @author VISTALL
 * @date 15:32 13.08.11
 */
public interface INpcDialogAppender {
    String getAppend(Player player, NpcInstance npc, int val);

    List<Integer> getNpcIds();
}
