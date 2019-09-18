package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _291_RevengeOfTheRedbonnet extends Quest {
    int MaryseRedbonnet;
    int BlackWolfPelt;
    int ScrollOfEscape;
    int GrandmasPearl;
    int GrandmasMirror;
    int GrandmasNecklace;
    int GrandmasHairpin;
    int BlackWolf;

    public _291_RevengeOfTheRedbonnet() {
        super(false);
        MaryseRedbonnet = 30553;
        BlackWolfPelt = 1482;
        ScrollOfEscape = 736;
        GrandmasPearl = 1502;
        GrandmasMirror = 1503;
        GrandmasNecklace = 1504;
        GrandmasHairpin = 1505;
        BlackWolf = 20317;
        addStartNpc(MaryseRedbonnet);
        addTalkId(MaryseRedbonnet);
        addKillId(BlackWolf);
        addQuestItem(BlackWolfPelt);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("marife_redbonnet_q0291_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() < 4) {
                htmltext = "marife_redbonnet_q0291_01.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "marife_redbonnet_q0291_02.htm";
            }
        } else if (cond == 1) {
            htmltext = "marife_redbonnet_q0291_04.htm";
        } else if (cond == 2 && st.getQuestItemsCount(BlackWolfPelt) < 40L) {
            htmltext = "marife_redbonnet_q0291_04.htm";
            st.setCond(1);
        } else if (cond == 2 && st.getQuestItemsCount(BlackWolfPelt) >= 40L) {
            final int random = Rnd.get(100);
            st.takeItems(BlackWolfPelt, -1L);
            if (random < 3) {
                st.giveItems(GrandmasPearl, 1L);
            } else if (random < 21) {
                st.giveItems(GrandmasMirror, 1L);
            } else if (random < 46) {
                st.giveItems(GrandmasNecklace, 1L);
            } else {
                st.giveItems(ScrollOfEscape, 1L);
                st.giveItems(GrandmasHairpin, 1L);
            }
            htmltext = "marife_redbonnet_q0291_05.htm";
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1 && st.getQuestItemsCount(BlackWolfPelt) < 40L) {
            st.giveItems(BlackWolfPelt, 1L);
            if (st.getQuestItemsCount(BlackWolfPelt) < 40L) {
                st.playSound("ItemSound.quest_itemget");
            } else {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
                st.setState(2);
            }
        }
        return null;
    }
}
