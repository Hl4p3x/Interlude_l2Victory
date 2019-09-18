package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.tables.SkillTable;

public class _609_MagicalPowerofWater1 extends Quest {
    private static final int WAHKAN = 31371;
    private static final int ASEFA = 31372;
    private static final int UDANS_MARDUI_BOX = 31561;
    private static final int STOLEN_GREEN_TOTEM = 7237;
    private static final int DIVINE_STONE_OF_WISDOM = 7081;
    private static final int GREEN_TOTEM = 7238;
    private static final int MARK_OF_KETRA_ALLIANCE2 = 7212;
    private static final int MARK_OF_KETRA_ALLIANCE3 = 7213;
    private static final int MARK_OF_KETRA_ALLIANCE4 = 7214;
    private static final int MARK_OF_KETRA_ALLIANCE5 = 7215;
    private static final int THIEF_KEY = 1661;

    public _609_MagicalPowerofWater1() {
        super(false);
        addStartNpc(31371);
        addTalkId(31372);
        addTalkId(31561);
        int[] VARKA_NPC_LIST = new int[20];
        VARKA_NPC_LIST[0] = 21350;
        VARKA_NPC_LIST[1] = 21351;
        VARKA_NPC_LIST[2] = 21353;
        VARKA_NPC_LIST[3] = 21354;
        VARKA_NPC_LIST[4] = 21355;
        VARKA_NPC_LIST[5] = 21357;
        VARKA_NPC_LIST[6] = 21358;
        VARKA_NPC_LIST[7] = 21360;
        VARKA_NPC_LIST[8] = 21361;
        VARKA_NPC_LIST[9] = 21362;
        VARKA_NPC_LIST[10] = 21364;
        VARKA_NPC_LIST[11] = 21365;
        VARKA_NPC_LIST[12] = 21366;
        VARKA_NPC_LIST[13] = 21368;
        VARKA_NPC_LIST[14] = 21369;
        VARKA_NPC_LIST[15] = 21370;
        VARKA_NPC_LIST[16] = 21371;
        VARKA_NPC_LIST[17] = 21372;
        VARKA_NPC_LIST[18] = 21373;
        VARKA_NPC_LIST[19] = 21374;
        addAttackId(VARKA_NPC_LIST);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equals(event)) {
            htmltext = "herald_wakan_q0609_02.htm";
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("609_1".equals(event) && st.getCond() == 2) {
            if (st.getQuestItemsCount(1661) < 1L) {
                htmltext = "udans_box_q0609_02.htm";
            } else if (st.getInt("proval") == 1) {
                htmltext = "udans_box_q0609_04.htm";
                st.takeItems(1661, 1L);
            } else {
                st.takeItems(1661, 1L);
                st.giveItems(7237, 1L);
                htmltext = "udans_box_q0609_03.htm";
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
        if (npcId == 31371) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 74) {
                    if (st.getQuestItemsCount(7212) == 1L || st.getQuestItemsCount(7213) == 1L || st.getQuestItemsCount(7214) == 1L || st.getQuestItemsCount(7215) == 1L) {
                        if (st.getQuestItemsCount(7081) == 0L) {
                            htmltext = "herald_wakan_q0609_01.htm";
                        } else {
                            htmltext = "completed";
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "herald_wakan_q0609_01a.htm";
                        st.exitCurrentQuest(true);
                    }
                } else {
                    htmltext = "herald_wakan_q0609_01b.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1) {
                htmltext = "herald_wakan_q0609_03.htm";
            }
        } else if (npcId == 31372) {
            if (cond == 1) {
                htmltext = "shaman_asefa_q0609_01.htm";
                st.setCond(2);
            } else if (cond == 2 && proval == 1) {
                htmltext = "shaman_asefa_q0609_03.htm";
                npc.doCast(SkillTable.getInstance().getInfo(4548, 1), st.getPlayer(), true);
                st.set("proval", "0");
            } else if (cond == 3 && st.getQuestItemsCount(7237) >= 1L) {
                htmltext = "shaman_asefa_q0609_04.htm";
                st.takeItems(7237, st.getQuestItemsCount(7237));
                st.giveItems(7238, 1L);
                st.giveItems(7081, 1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 31561 && cond == 2) {
            htmltext = "udans_box_q0609_01.htm";
        }
        return htmltext;
    }

    @Override
    public String onAttack(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 2 && st.getInt("proval") == 0) {
            npc.doCast(SkillTable.getInstance().getInfo(4547, 1), st.getPlayer(), true);
            st.set("proval", "1");
        }
        return null;
    }
}
