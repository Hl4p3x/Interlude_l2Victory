package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _613_ProveYourCourage extends Quest {
    private static final int elder_ashas_barka_durai = 31377;
    private static final int ketra_hero_hekaton = 25299;
    private static final int q_barka_friendship_3 = 7223;
    private static final int q_hekaton_head = 7240;
    private static final int q_feather_of_valor = 7229;

    public _613_ProveYourCourage() {
        super(true);
        addStartNpc(elder_ashas_barka_durai);
        addKillId(ketra_hero_hekaton);
        addQuestItem(q_hekaton_head);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if (event.equals("quest_accept")) {
            st.setCond(1);
            st.set("prove_your_courage_varka", String.valueOf(11), true);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "elder_ashas_barka_durai_q0613_0104.htm";
        } else if (event.equals("reply_3")) {
            if (st.getQuestItemsCount(7240) >= 1L) {
                st.takeItems(q_hekaton_head, -1L);
                st.giveItems(q_feather_of_valor, 1L);
                st.addExpAndSp(10000L, 0L);
                st.unset("prove_your_courage_varka");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                htmltext = "elder_ashas_barka_durai_q0613_0201.htm";
            } else {
                htmltext = "elder_ashas_barka_durai_q0613_0202.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int GetMemoState = st.getInt("prove_your_courage_varka");
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        switch (id) {
            case 1: {
                if (npcId != 31377) {
                    break;
                }
                if (st.getPlayer().getLevel() < 75) {
                    st.exitCurrentQuest(true);
                    htmltext = "elder_ashas_barka_durai_q0613_0103.htm";
                    break;
                }
                if (st.getQuestItemsCount(q_barka_friendship_3) >= 1L) {
                    htmltext = "elder_ashas_barka_durai_q0613_0101.htm";
                    break;
                }
                st.exitCurrentQuest(true);
                htmltext = "elder_ashas_barka_durai_q0613_0102.htm";
                break;
            }
            case 2: {
                if (npcId != 31377 || GetMemoState < 11 || GetMemoState > 12) {
                    break;
                }
                if (GetMemoState == 12 && st.getQuestItemsCount(7240) >= 1L) {
                    htmltext = "elder_ashas_barka_durai_q0613_0105.htm";
                    break;
                }
                htmltext = "elder_ashas_barka_durai_q0613_0106.htm";
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int GetMemoState = st.getInt("prove_your_courage_varka");
        final int npcId = npc.getNpcId();
        if (GetMemoState == 11 && npcId == 25299) {
            final int i4 = Rnd.get(1000);
            if (i4 < 1000) {
                if (st.getQuestItemsCount(7240) + 1L >= 1L) {
                    if (st.getQuestItemsCount(7240) < 1L) {
                        st.setCond(2);
                        st.set("prove_your_courage_varka", String.valueOf(12), true);
                        st.giveItems(7240, 1L);
                        st.playSound("ItemSound.quest_middle");
                    }
                } else {
                    st.giveItems(7240, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
