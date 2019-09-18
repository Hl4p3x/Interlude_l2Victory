package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.HennaEquipList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.HennaUnequipList;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class SymbolMakerInstance extends NpcInstance {

    public SymbolMakerInstance(final int objectID, final NpcTemplate template) {
        super(objectID, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        switch (command) {
            case "Draw":
                player.sendPacket(new HennaEquipList(player));
                break;
            case "RemoveList":
                player.sendPacket(new HennaUnequipList(player));
                break;
            default:
                super.onBypassFeedback(player, command);
                break;
        }
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if(getTemplate().getHtmRoot() != null) {
            if (val == 0) {
                pom = "" + npcId;
            } else {
                pom = npcId + "-" + val;
            }
            return getTemplate().getHtmRoot()+ pom +".htm";
        }
        if (val == 0) {
            pom = "SymbolMaker";
        } else {
            pom = "SymbolMaker-" + val;
        }
        return "symbolmaker/" + pom + ".htm";
    }
}
