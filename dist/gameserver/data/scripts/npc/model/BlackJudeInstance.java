package npc.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class BlackJudeInstance extends NpcInstance {
    public BlackJudeInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        switch (command) {
            case "tryRemovePenalty":
                if (player.getDeathPenalty().getLevel() > 0) {
                    showChatWindow(player, 2, "%price%", getPrice(player));
                } else {
                    showChatWindow(player, 1);
                }
                break;
            case "removePenalty":
                if (player.getDeathPenalty().getLevel() > 0) {
                    if (player.getAdena() >= getPrice(player)) {
                        player.reduceAdena((long) getPrice(player), true);
                        doCast(SkillTable.getInstance().getInfo(5077, 1), player, false);
                    } else {
                        player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    }
                } else {
                    showChatWindow(player, 1);
                }
                break;
            default:
                super.onBypassFeedback(player, command);
                break;
        }
    }

    public int getPrice(final Player player) {
        final int playerLvl = player.getLevel();
        if (playerLvl <= 19) {
            return 3600;
        }
        if (playerLvl >= 20 && playerLvl <= 39) {
            return 16400;
        }
        if (playerLvl >= 40 && playerLvl <= 51) {
            return 36200;
        }
        if (playerLvl >= 52 && playerLvl <= 60) {
            return 50400;
        }
        if (playerLvl >= 61 && playerLvl <= 75) {
            return 78200;
        }
        return 102800;
    }
}
