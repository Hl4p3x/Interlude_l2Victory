package ru.j2dev.gameserver.listener.inventory;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;

/**
 * Created by JunkyFunky
 * on 12.06.2018 16:44
 * group j2dev
 */
public interface OnItemUseListener extends PlayerListener {
    void onItemUse(int itemId, Player player);
}
