package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _263_OrcSubjugation extends Quest {
    public final int KAYLEEN = 30346;
    public final int BALOR_ORC_ARCHER = 20385;
    public final int BALOR_ORC_FIGHTER = 20386;
    public final int BALOR_ORC_FIGHTER_LEADER = 20387;
    public final int BALOR_ORC_LIEUTENANT = 20388;
    public final int ORC_AMULET = 1116;
    public final int ORC_NECKLACE = 1117;

    public _263_OrcSubjugation() {
        super(false);
        addStartNpc(30346);
        addKillId(20385, 20386, 20387, 20388);
        addQuestItem(1116, 1117);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("sentry_kayleen_q0263_03.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("sentry_kayleen_q0263_06.htm".equals(event)) {
            st.setCond(0);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 8 && st.getPlayer().getRace() == Race.darkelf) {
                htmltext = "sentry_kayleen_q0263_02.htm";
                return htmltext;
            }
            if (st.getPlayer().getRace() != Race.darkelf) {
                htmltext = "sentry_kayleen_q0263_00.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() < 8) {
                htmltext = "sentry_kayleen_q0263_01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1) {
            if (st.getQuestItemsCount(1116) == 0L && st.getQuestItemsCount(1117) == 0L) {
                htmltext = "sentry_kayleen_q0263_04.htm";
            } else if (st.getQuestItemsCount(1116) + st.getQuestItemsCount(1117) >= 10L) {
                htmltext = "sentry_kayleen_q0263_05.htm";
                st.giveItems(57, st.getQuestItemsCount(1116) * 20L + st.getQuestItemsCount(1117) * 30L + 1100L);
                st.takeItems(1116, -1L);
                st.takeItems(1117, -1L);
            } else {
                htmltext = "sentry_kayleen_q0263_05.htm";
                st.giveItems(57, st.getQuestItemsCount(1116) * 20L + st.getQuestItemsCount(1117) * 30L);
                st.takeItems(1116, -1L);
                st.takeItems(1117, -1L);
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (st.getCond() == 1 && Rnd.chance(60)) {
            if (npcId == 20385) {
                st.giveItems(1116, 1L);
            } else if (npcId == 20386 || npcId == 20387 || npcId == 20388) {
                st.giveItems(1117, 1L);
            }
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
