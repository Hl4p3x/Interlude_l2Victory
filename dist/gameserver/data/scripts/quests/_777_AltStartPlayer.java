package quests;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.HennaHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.TutorialCloseHtml;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.Henna;
import org.slf4j.Logger;

public class _777_AltStartPlayer extends Quest {

    public final String[][] Profa;
    public final int[][] Armor;
    Logger LOGGER = LoggerFactory.getLogger(_777_AltStartPlayer.class);

    public _777_AltStartPlayer() {
        super(false);
        Profa = new String[][] {{"0", "HF.htm"}, {"10", "HM.htm"}, {"18", "EF.htm"}, {"25", "EM.htm"}, {"31", "EDF.htm"}, {"38", "EDM.htm"}, {"44", "OF.htm"}, {"49", "OM.htm"}, {"53", "DF.htm"}};
        Armor = new int[][] {{6382,6379,6319,6381,6380,6656,6657,6658,6659,6660},{6378,6374,6373,6376,6375,6656,6657,6658,6659,6660},{5779,5767,2407,641,512,6656,6657,6658,6659,6660}};
    }

    @Override
    public String onEvent(String event, QuestState st, NpcInstance npc) {
        Player player = st.getPlayer();
        if (player == null) {
            return null;
        }
        String html = "";
        final int classId = player.getClassId().getId();
        if (event.startsWith("Prof")) {
            for (final String[] element : Profa) {
                if (classId == Integer.valueOf(element[0])) {
                    html = element[1];
                    st.setCond(1);
                }
            }
        } else if (event.startsWith("change_class")) {
            int val = Integer.parseInt(event.substring(13));
                player.sendPacket(TutorialCloseHtml.STATIC);
                chengClass(player, val);
                html = "Symbol.htm";
        } else  if (event.startsWith("chenge_symbol")) {
            int val = Integer.parseInt(event.substring(14));
            if (st.getCond() == 1) {
                if (player.getHennaEmptySlots() <=2) {
                    player.sendPacket(TutorialCloseHtml.STATIC);
                    chengHenna(player, val);
                    LOGGER.info("Хуета заработала");
                    st.setCond(2);
                    html = "Armor.htm";
                } else {
                    player.sendPacket(TutorialCloseHtml.STATIC);
                    chengHenna(player, val);
                    html = "Symbol.htm";
                }
            }
        } else if (event.startsWith("chenge_armor")) {
            int val = Integer.parseInt(event.substring(13));
            ItemInstance item;
            if (st.getCond() == 2) {
            switch (val) {
                case 1:
                    player.getInventory().addItem(57, 100);
                    for (int tra : Armor[0]) {
                        item = player.getInventory().addItem(tra, 1);
                        if (item.isEquipable()) {
                            item.setEnchantLevel(15);
                            player.getInventory().equipItem(item);
                            player.sendItemList(true);
                            player.broadcastCharInfo();
                            player.sendPacket(TutorialCloseHtml.STATIC);
                        }
                    }
                    break;
                case 2:
                    player.getInventory().addItem(57, 100);
                    for (int tra : Armor[1]) {
                        item = player.getInventory().addItem(tra, 1);
                        if (item.isEquipable()) {
                            item.setEnchantLevel(15);
                            player.getInventory().equipItem(item);
                            player.sendItemList(true);
                            player.broadcastCharInfo();
                            player.sendPacket(TutorialCloseHtml.STATIC);
                        }
                    }
                    break;
                case 3:
                    player.getInventory().addItem(57, 100);
                    for (int tra : Armor[2]) {
                        item = player.getInventory().addItem(tra, 1);
                        if (item.isEquipable()) {
                            item.setEnchantLevel(15);
                            player.getInventory().equipItem(item);
                            player.sendItemList(true);
                            player.broadcastCharInfo();
                            player.sendPacket(TutorialCloseHtml.STATIC);
                        }
                    }
                    break;
                }
                st.setCond(3);
                html = "Weapon.htm";
            }
        } else if (event.startsWith("Weapon")) {
            int even = Integer.parseInt(event.substring(7));
            if (st.getCond() == 3) {
                giveEndEqip(player, even, 15);
                st.setCond(4);
                player.sendPacket(TutorialCloseHtml.STATIC);
                html = "Buffs.htm";
            }
        } else if (event.startsWith("Buff")) {
            int val = Integer.parseInt(event.substring(4));
            if (st.getCond() == 4) {
                switch (val) {
                    case 1:
                        for (final Pair<Integer, Integer> skIdLvl : Config.OTHER_WARRIOR_BUFF_ON_CHAR_CREATE) {
                            final Skill skill = SkillTable.getInstance().getInfo(skIdLvl.getLeft(), skIdLvl.getRight());
                            skill.getEffects(player, player, false, false, Config.ALT_NPC_BUFFER_EFFECT_TIME, 1.0, false);
                        }
                        break;
                    case 2:
                        for (final Pair<Integer, Integer> skIdLvl : Config.OTHER_MAGE_BUFF_ON_CHAR_CREATE) {
                            final Skill skill = SkillTable.getInstance().getInfo(skIdLvl.getLeft(), skIdLvl.getRight());
                            skill.getEffects(player, player, false, false, Config.ALT_NPC_BUFFER_EFFECT_TIME, 1.0, false);
                        }
                        break;
                }
                player.setCurrentCp((double) player.getMaxCp());
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                player.sendPacket(TutorialCloseHtml.STATIC);
            }
        }
        if (html.isEmpty()) {
            return null;
        }
        st.showAltStartPlayer(html);
        return null;
    }

    private void chengClass(Player player, final int id) {
        player.setClassId(id, false, false);
        player.rewardSkillsAltSrart();
        player.rewardSkills();
        player.broadcastCharInfo();
        player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 0, 0L));
    }

    private void chengHenna(Player player, int id) {
        Henna temp = HennaHolder.getInstance().getHenna(id);
        if (!temp.isForThisClass(player)) {
            player.sendMessage("Данные краски нельзя нанести вашему персонажу.");
            return;
        }
        player.addHenna(temp);
        player.sendPacket(SystemMsg.THE_SYMBOL_HAS_BEEN_ADDED);
    }

    public void giveEndEqip(Player player, int id, int enchant) {
        ItemInstance item = player.getInventory().addItem(id,1);
        if (item.isEquipable()) {
            item.setEnchantLevel(enchant);
            player.getInventory().equipItem(item);
            player.sendItemList(true);
            player.broadcastCharInfo();
        }
    }
}