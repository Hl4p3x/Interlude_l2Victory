package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _602_ShadowofLight extends Quest {
    private static final int eye_of_argos = 31683;
    private static final int buffalo_slave = 21299;
    private static final int grendel_slave = 21304;
    private static final int sealed_sanddragons_earing_piece = 6698;
    private static final int sealed_ring_of_aurakyria_gem = 6699;
    private static final int sealed_dragon_necklace_wire = 6700;
    private static final int q_dark_eye = 7189;

    public _602_ShadowofLight() {
        super(false);
        addStartNpc(31683);
        addKillId(21299, 21304);
        addQuestItem(7189);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.set("shadow_of_light", String.valueOf(11), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "eye_of_argos_q0602_0104.htm";
        } else if ("reply_3".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(7189) >= 100L) {
                final int i1 = Rnd.get(1000);
                st.takeItems(7189, 100L);
                if (i1 < 200) {
                    st.giveItems(6699, 3L);
                    st.giveItems(57, 40000L);
                    st.addExpAndSp(120000L, 20000L);
                } else if (i1 < 400) {
                    st.giveItems(6698, 3L);
                    st.giveItems(57, 60000L);
                    st.addExpAndSp(110000L, 15000L);
                } else if (i1 < 500) {
                    st.giveItems(6700, 3L);
                    st.giveItems(57, 40000L);
                    st.addExpAndSp(150000L, 10000L);
                } else if (i1 < 1000) {
                    st.giveItems(57, 100000L);
                    st.addExpAndSp(140000L, 11250L);
                }
                st.unset("shadow_of_light");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "eye_of_argos_q0602_0201.htm";
            } else {
                htmltext = "eye_of_argos_q0602_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("shadow_of_light");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31683) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 68) {
                    htmltext = "eye_of_argos_q0602_0101.htm";
                    break;
                }
                htmltext = "eye_of_argos_q0602_0103.htm";
                st.exitCurrentQuest(true);
                break;
            }
            case 2: {
                if (npcId != 31683 || GetMemoState < 11 || GetMemoState > 12) {
                    break;
                }
                if (GetMemoState == 12 && st.getQuestItemsCount(7189) >= 100L) {
                    htmltext = "eye_of_argos_q0602_0105.htm";
                    break;
                }
                htmltext = "eye_of_argos_q0602_0106.htm";
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("shadow_of_light");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 11) {
            if (npcId == 21299) {
                final int i4 = Rnd.get(1000);
                if (i4 < 560) {
                    if (st.getQuestItemsCount(7189) + 1L >= 100L) {
                        st.setCond(2);
                        st.set("shadow_of_light", String.valueOf(12), true);
                        st.giveItems(7189, 100L - st.getQuestItemsCount(7189));
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.giveItems(7189, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            } else if (npcId == 21304) {
                final int i4 = Rnd.get(1000);
                if (i4 < 800) {
                    if (st.getQuestItemsCount(7189) + 1L >= 100L) {
                        st.setCond(2);
                        st.set("shadow_of_light", String.valueOf(12), true);
                        st.giveItems(7189, 100L - st.getQuestItemsCount(7189));
                        st.playSound("ItemSound.quest_middle");
                    } else {
                        st.giveItems(7189, 1L);
                        st.playSound("ItemSound.quest_itemget");
                    }
                }
            }
        }
        return null;
    }
}
