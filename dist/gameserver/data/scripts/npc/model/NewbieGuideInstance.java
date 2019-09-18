package npc.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RadarControl;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Arrays;
import java.util.List;

public class NewbieGuideInstance extends NpcInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(NewbieGuideInstance.class);
    private static final List<?> mainHelpers = Arrays.asList(30598, 30599, 30600, 30601, 30602, 32135);

    public NewbieGuideInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        if (val != 0 || !mainHelpers.contains(getNpcId())) {
            super.showChatWindow(player, val);
            return;
        }
        if (player.getClassId().getLevel() != 1) {
            player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q-no.htm", val));
            return;
        }
        if (player.getVar("NewGuidReward") == null) {
            final QuestState qs = player.getQuestState("_999_T1Tutorial");
            if (qs != null) {
                qs.unset("step");
            }
            player.setVar("NewGuidReward", "1", -1L);
            final boolean isMage = player.getClassId().getRace() != Race.orc && player.getClassId().isMage();
            if (isMage) {
                player.sendPacket(new PlaySound("tutorial_voice_027"));
                Functions.addItem(player, 5790, 100L);
            } else {
                player.sendPacket(new PlaySound("tutorial_voice_026"));
                Functions.addItem(player, 5789, 200L);
            }
            Functions.addItem(player, 8594, 2L);
        }
        if (player.getLevel() < 6) {
            if (player.isQuestCompleted("_001_LettersOfLove") || player.isQuestCompleted("_002_WhatWomenWant") || player.isQuestCompleted("_004_LongLivethePaagrioLord") || player.isQuestCompleted("_005_MinersFavor") || player.isQuestCompleted("_166_DarkMass")) {
                if (!player.getVarB("ng1")) {
                    final String oldVar = player.getVar("ng1");
                    player.setVar("ng1", (oldVar == null) ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1L);
                    player.addAdena(11567L);
                }
                player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q1-2.htm", val));
                return;
            }
            player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q1-1.htm", val).replace("%tonpc%", getQuestNpc(1, player)));
        } else if (player.getLevel() < 10) {
            if (player.getVarB("p1q2")) {
                if (!player.getVarB("ng2")) {
                    final String oldVar = player.getVar("ng2");
                    player.setVar("ng2", (oldVar == null) ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1L);
                }
                player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q3-1.htm", val).replace("%tonpc%", getQuestNpc(3, player)));
                return;
            }
            player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q2-1.htm", val).replace("%tonpc%", getQuestNpc(2, player)));
        } else if (player.getLevel() < 15) {
            if (player.getVarB("p1q3")) {
                if (!player.getVarB("ng3")) {
                    final String oldVar = player.getVar("ng3");
                    player.setVar("ng3", (oldVar == null) ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1L);
                    player.addAdena(38180L);
                }
                player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q4-1.htm", val).replace("%tonpc%", getQuestNpc(4, player)));
                return;
            }
            player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q3-1.htm", val).replace("%tonpc%", getQuestNpc(3, player)));
        } else {
            if (player.getLevel() >= 18) {
                player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q-no.htm", val));
                return;
            }
            if (player.getVarB("p1q4")) {
                if (!player.getVarB("ng4")) {
                    final String oldVar = player.getVar("ng4");
                    player.setVar("ng4", (oldVar == null) ? "1" : String.valueOf(Integer.parseInt(oldVar) + 1), -1L);
                    player.addAdena(10018L);
                }
                player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q4-2.htm", val));
                return;
            }
            player.sendPacket(new NpcHtmlMessage(player, this, "newbiehelper/q4-1.htm", val).replace("%tonpc%", getQuestNpc(4, player)));
        }
    }

    public String getQuestNpc(final int quest, final Player player) {
        int val = 0;
        switch (quest) {
            case 1: {
                switch (getNpcId()) {
                    case 30598: {
                        val = 30048;
                        break;
                    }
                    case 30599: {
                        val = 30223;
                        break;
                    }
                    case 30600: {
                        val = 30130;
                        break;
                    }
                    case 30601: {
                        val = 30554;
                        break;
                    }
                    case 30602: {
                        val = 30578;
                        break;
                    }
                }
                break;
            }
            case 2: {
                switch (getNpcId()) {
                    case 30598: {
                        val = 30039;
                        break;
                    }
                    case 30599: {
                        val = 30221;
                        break;
                    }
                    case 30600: {
                        val = 30357;
                        break;
                    }
                    case 30601: {
                        val = 30535;
                        break;
                    }
                    case 30602: {
                        val = 30566;
                        break;
                    }
                }
                break;
            }
            case 3: {
                switch (player.getClassId()) {
                    case fighter: {
                        val = 30008;
                        break;
                    }
                    case mage: {
                        val = 30017;
                        break;
                    }
                    case elvenFighter:
                    case elvenMage: {
                        val = 30218;
                        break;
                    }
                    case darkFighter:
                    case darkMage: {
                        val = 30358;
                        break;
                    }
                    case orcFighter:
                    case orcMage: {
                        val = 30568;
                        break;
                    }
                    case dwarvenFighter: {
                        val = 30523;
                        break;
                    }
                }
                break;
            }
            case 4: {
                switch (getNpcId()) {
                    case 30598: {
                        val = 30050;
                        break;
                    }
                    case 30599: {
                        val = 30222;
                        break;
                    }
                    case 30600: {
                        val = 30145;
                        break;
                    }
                    case 30601: {
                        val = 30519;
                        break;
                    }
                    case 30602: {
                        val = 30571;
                        break;
                    }
                }
                break;
            }
        }
        if (val == 0) {
            LOGGER.warn("WTF? L2NewbieGuideInstance {} not found next step {} for {}", new String[]{String.valueOf(getNpcId()), String.valueOf(quest), String.valueOf(player.getClassId())});
            return null;
        }
        final NpcInstance npc = GameObjectsStorage.getByNpcId(val);
        if (npc == null) {
            return "";
        }
        player.sendPacket(new RadarControl(2, 1, npc.getLoc()));
        player.sendPacket(new RadarControl(0, 2, npc.getLoc()));
        return npc.getName();
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        return "newbiehelper/" + pom + ".htm";
    }
}
