package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.RecipeHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Recipe;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeItemMakeInfo;

public class RequestRecipeItemMakeInfo extends L2GameClientPacket {
    private int _id;

    @Override
    protected void readImpl() {
        _id = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Recipe recipe = RecipeHolder.getInstance().getRecipeById(_id);
        if (recipe == null) {
            activeChar.sendActionFailed();
            return;
        }
        sendPacket(new RecipeItemMakeInfo(activeChar, recipe, -1));
    }
}
