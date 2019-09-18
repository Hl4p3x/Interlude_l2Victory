package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.tables.SkillTable;

public class _615_MagicalPowerofFire1 extends Quest {
    private final int NARAN = 31378;
    private final int UDAN = 31379;
    private final int BOX_OF_ASEFA = 31559;
    private final int STOLEN_RED_TOTEM = 7242;
    private final int DIVINE_STONE_OF_WISDOM = 7081;
    private final int RED_TOTEM = 7243;
    private final int MARK_OF_VARKA_ALLIANCE2 = 7222;
    private final int MARK_OF_VARKA_ALLIANCE3 = 7223;
    private final int MARK_OF_VARKA_ALLIANCE4 = 7224;
    private final int MARK_OF_VARKA_ALLIANCE5 = 7225;
    private final int THIEF_KEY = 1661;
    private final int[] KETRA_NPC_LIST;

    public _615_MagicalPowerofFire1() {
        super(false);
        KETRA_NPC_LIST = new int[19];
        addStartNpc(31378);
        addTalkId(31378);
        addTalkId(31379);
        addTalkId(31559);
        KETRA_NPC_LIST[0] = 21324;
        KETRA_NPC_LIST[1] = 21325;
        KETRA_NPC_LIST[2] = 21327;
        KETRA_NPC_LIST[3] = 21328;
        KETRA_NPC_LIST[4] = 21329;
        KETRA_NPC_LIST[5] = 21331;
        KETRA_NPC_LIST[6] = 21332;
        KETRA_NPC_LIST[7] = 21334;
        KETRA_NPC_LIST[8] = 21335;
        KETRA_NPC_LIST[9] = 21336;
        KETRA_NPC_LIST[10] = 21338;
        KETRA_NPC_LIST[11] = 21339;
        KETRA_NPC_LIST[12] = 21340;
        KETRA_NPC_LIST[13] = 21342;
        KETRA_NPC_LIST[14] = 21343;
        KETRA_NPC_LIST[15] = 21344;
        KETRA_NPC_LIST[16] = 21345;
        KETRA_NPC_LIST[17] = 21346;
        KETRA_NPC_LIST[18] = 21347;
        for (final int npcId : KETRA_NPC_LIST) {
            addAttackId(npcId);
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "herald_naran_q0615_02.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("615_1".equalsIgnoreCase(event) && st.getCond() == 2) {
            if (st.getQuestItemsCount(1661) < 1L) {
                htmltext = "asefas_box_q0615_02.htm";
            } else if (st.getInt("proval") == 1) {
                htmltext = "asefas_box_q0615_04.htm";
                st.takeItems(1661, 1L);
            } else {
                st.takeItems(1661, 1L);
                st.giveItems(7242, 1L);
                htmltext = "asefas_box_q0615_03.htm";
                st.setCond(3);
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final int proval = st.getInt("proval");
        switch (npcId) {
            case 31378: {
                if (cond == 0) {
                    if (st.getPlayer().getLevel() < 74) {
                        htmltext = "herald_naran_q0615_01b.htm";
                        st.exitCurrentQuest(true);
                        break;
                    }
                    if (st.getQuestItemsCount(7222) != 1L && st.getQuestItemsCount(7223) != 1L && st.getQuestItemsCount(7224) != 1L && st.getQuestItemsCount(7225) != 1L) {
                        htmltext = "herald_naran_q0615_01a.htm";
                        st.exitCurrentQuest(true);
                        break;
                    }
                    if (st.getQuestItemsCount(7081) == 0L) {
                        htmltext = "herald_naran_q0615_01.htm";
                        break;
                    }
                    htmltext = "completed";
                    st.exitCurrentQuest(true);
                    break;
                } else {
                    if (cond == 1) {
                        htmltext = "herald_naran_q0615_03.htm";
                        break;
                    }
                    break;
                }
            }
            case 31379: {
                if (cond == 1) {
                    htmltext = "shaman_udan_q0615_01.htm";
                    st.setCond(2);
                    break;
                }
                if (cond == 2 && proval == 1) {
                    htmltext = "shaman_udan_q0615_03.htm";
                    npc.doCast(SkillTable.getInstance().getInfo(4548, 1), st.getPlayer(), true);
                    st.set("proval", "0");
                    break;
                }
                if (cond == 3 && st.getQuestItemsCount(7242) >= 1L) {
                    htmltext = "shaman_udan_q0615_04.htm";
                    st.takeItems(7242, st.getQuestItemsCount(7242));
                    st.giveItems(7243, 1L);
                    st.giveItems(7081, 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(true);
                    break;
                }
                break;
            }
            case 31559: {
                if (cond == 2) {
                    htmltext = "asefas_box_q0615_01.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onAttack(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        final int proval = st.getInt("proval");
        if (cond == 2 && proval == 0) {
            npc.doCast(SkillTable.getInstance().getInfo(4547, 1), st.getPlayer(), true);
            st.set("proval", "1");
        }
        return null;
    }
}
