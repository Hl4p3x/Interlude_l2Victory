package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _640_TheZeroHour extends Quest {
    private static final int KAHMAN = 31554;
    private static final int FANG = 8085;
    private static final int Enria = 4042;
    private static final int Asofe = 4043;
    private static final int Thons = 4044;
    private static final int Varnish_of_Purity = 1887;
    private static final int Synthetic_Cokes = 1888;
    private static final int Compound_Braid = 1889;
    private static final int Durable_Metal_Plate = 5550;
    private static final int Mithril_Alloy = 1890;
    private static final int Oriharukon = 1893;
    private static final int DROP_CHANCE = 50;
    private static final int[] mobs = {22105, 22106, 22107, 22108, 22109, 22110, 22111, 22115, 22116, 22117, 22118, 22119, 22120, 22121, 22112, 22113, 22114};

    public _640_TheZeroHour() {
        super(true);
        addStartNpc(KAHMAN);
        addKillId(mobs);
        addQuestItem(FANG);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int cond = st.getCond();
        String htmltext = event;
        if ("merc_kahmun_q0640_0103.htm".equals(event) && cond == 0) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        if (cond == 1) {
            if ("0".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 12L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 12L);
                    st.giveItems(Enria, 1L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("1".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 6L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 6L);
                    st.giveItems(Asofe, 1L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("2".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 6L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 6L);
                    st.giveItems(Thons, 1L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("3".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 81L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 81L);
                    st.giveItems(Varnish_of_Purity, 10L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("4".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 33L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 33L);
                    st.giveItems(Synthetic_Cokes, 5L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("5".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 30L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 30L);
                    st.giveItems(Compound_Braid, 10L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("6".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 150L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 150L);
                    st.giveItems(Durable_Metal_Plate, 10L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("7".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 131L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 131L);
                    st.giveItems(Mithril_Alloy, 10L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
            if ("8".equals(event)) {
                if (st.getQuestItemsCount(FANG) >= 123L) {
                    htmltext = "merc_kahmun_q0640_0203.htm";
                    st.takeItems(FANG, 123L);
                    st.giveItems(Oriharukon, 5L);
                } else {
                    htmltext = "merc_kahmun_q0640_0201.htm";
                }
            }
        }
        if ("close".equals(event)) {
            htmltext = "merc_kahmun_q0640_0205.htm";
            st.takeItems(FANG, -1L);
            st.exitCurrentQuest(true);
        }
        if ("more".equals(event)) {
            htmltext = "merc_kahmun_q0640_0101.htm";
            st.unset("cond");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        final QuestState InSearchOfTheNest = st.getPlayer().getQuestState(_109_InSearchOfTheNest.class);
        if (npcId == KAHMAN) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 66) {
                    if (InSearchOfTheNest != null && InSearchOfTheNest.isCompleted()) {
                        htmltext = "merc_kahmun_q0640_0101.htm";
                    } else {
                        htmltext = "merc_kahmun_q0640_0104.htm";
                    }
                } else {
                    htmltext = "merc_kahmun_q0640_0102.htm";
                }
            }
            if (cond == 1) {
                htmltext = "merc_kahmun_q0640_0105.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getState() == 2) {
            st.rollAndGive(FANG, 1, (double) DROP_CHANCE);
        }
        return null;
    }
}
