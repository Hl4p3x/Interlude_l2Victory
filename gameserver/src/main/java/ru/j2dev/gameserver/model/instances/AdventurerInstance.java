package ru.j2dev.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.manager.RaidBossSpawnManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowQuestInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RadarControl;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class AdventurerInstance extends NpcInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdventurerInstance.class);

    public AdventurerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("npcfind_byid")) {
            try {
                final int bossId = Integer.parseInt(command.substring(12).trim());
                switch (RaidBossSpawnManager.getInstance().getRaidBossStatusId(bossId)) {
                    case ALIVE:
                    case DEAD: {
                        final Spawner spawn = RaidBossSpawnManager.getInstance().getSpawnTable().get(bossId);
                        final Location loc = spawn.getCurrentSpawnRange().getRandomLoc(spawn.getReflection().getGeoIndex());
                        player.sendPacket(new RadarControl(2, 2, loc), new RadarControl(0, 1, loc));
                        break;
                    }
                    case UNDEFINED: {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2AdventurerInstance.BossNotInGame", player).addNumber(bossId));
                        break;
                    }
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("AdventurerInstance: Invalid Bypass to Server command parameter.");
            }
        } else if (command.startsWith("raidInfo")) {
            final int bossLevel = Integer.parseInt(command.substring(9).trim());
            String filename = "adventurer_guildsman/raid_info/info.htm";
            if (bossLevel != 0) {
                filename = "adventurer_guildsman/raid_info/level" + bossLevel + ".htm";
            }
            showChatWindow(player, filename);
        } else if ("questlist".equalsIgnoreCase(command)) {
            player.sendPacket(ExShowQuestInfo.STATIC);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        return "adventurer_guildsman/" + pom + ".htm";
    }
}
