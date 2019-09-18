package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _274_SkirmishWithTheWerewolves extends Quest {
    private static final int MARAKU_WEREWOLF_HEAD = 1477;
    private static final int NECKLACE_OF_VALOR = 1507;
    private static final int NECKLACE_OF_COURAGE = 1506;
    private static final int MARAKU_WOLFMEN_TOTEM = 1501;

    public _274_SkirmishWithTheWerewolves() {
        super(false);
        addStartNpc(30569);
        addKillId(20363);
        addKillId(20364);
        addQuestItem(1477);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("prefect_brukurse_q0274_03.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int id = st.getState();
        final int cond = st.getCond();
        if (id == 1) {
            if (st.getPlayer().getRace() != Race.orc) {
                htmltext = "prefect_brukurse_q0274_00.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() < 9) {
                htmltext = "prefect_brukurse_q0274_01.htm";
                st.exitCurrentQuest(true);
            } else {
                if (st.getQuestItemsCount(1507) > 0L || st.getQuestItemsCount(1506) > 0L) {
                    htmltext = "prefect_brukurse_q0274_02.htm";
                    return htmltext;
                }
                htmltext = "prefect_brukurse_q0274_07.htm";
            }
        } else if (cond == 1) {
            htmltext = "prefect_brukurse_q0274_04.htm";
        } else if (cond == 2) {
            if (st.getQuestItemsCount(1477) < 40L) {
                htmltext = "prefect_brukurse_q0274_04.htm";
            } else {
                st.takeItems(1477, -1L);
                st.giveItems(57, 3500L, true);
                if (st.getQuestItemsCount(1501) >= 1L) {
                    st.giveItems(57, st.getQuestItemsCount(1501) * 600L, true);
                    st.takeItems(1501, -1L);
                }
                htmltext = "prefect_brukurse_q0274_05.htm";
                st.exitCurrentQuest(true);
                st.playSound("ItemSound.quest_finish");
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && st.getQuestItemsCount(1477) < 40L) {
            if (st.getQuestItemsCount(1477) < 39L) {
                st.playSound("ItemSound.quest_itemget");
            } else {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            }
            st.giveItems(1477, 1L);
        }
        if (Rnd.chance(5)) {
            st.giveItems(1501, 1L);
        }
        return null;
    }
}
