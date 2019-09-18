package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _316_DestroyPlaguebringers extends Quest {
    private static final int Ellenia = 30155;
    private static final int Sukar_Wererat = 20040;
    private static final int Sukar_Wererat_Leader = 20047;
    private static final int Varool_Foulclaw = 27020;
    private static final int Wererats_Fang = 1042;
    private static final int Varool_Foulclaws_Fang = 1043;
    private static final int Wererats_Fang_Chance = 50;
    private static final int Varool_Foulclaws_Fang_Chance = 30;

    public _316_DestroyPlaguebringers() {
        super(false);
        addStartNpc(Ellenia);
        addKillId(Sukar_Wererat);
        addKillId(Sukar_Wererat_Leader);
        addKillId(Varool_Foulclaw);
        addQuestItem(Wererats_Fang);
        addQuestItem(Varool_Foulclaws_Fang);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("elliasin_q0316_04.htm".equalsIgnoreCase(event) && _state == 1 && st.getPlayer().getRace() == Race.elf && st.getPlayer().getLevel() >= 18) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("elliasin_q0316_08.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != Ellenia) {
            return htmltext;
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getRace() != Race.elf) {
                htmltext = "elliasin_q0316_00.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() < 18) {
                htmltext = "elliasin_q0316_02.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "elliasin_q0316_03.htm";
                st.setCond(0);
            }
        } else if (_state == 2) {
            final long Reward = st.getQuestItemsCount(Wererats_Fang) * 60L + st.getQuestItemsCount(Varool_Foulclaws_Fang) * 10000L;
            if (Reward > 0L) {
                htmltext = "elliasin_q0316_07.htm";
                st.takeItems(Wererats_Fang, -1L);
                st.takeItems(Varool_Foulclaws_Fang, -1L);
                st.giveItems(57, Reward);
                st.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "elliasin_q0316_05.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        if (npc.getNpcId() == Varool_Foulclaw && qs.getQuestItemsCount(Varool_Foulclaws_Fang) == 0L && Rnd.chance(Varool_Foulclaws_Fang_Chance)) {
            qs.giveItems(Varool_Foulclaws_Fang, 1L);
            qs.playSound("ItemSound.quest_itemget");
        } else if (Rnd.chance(Wererats_Fang_Chance)) {
            qs.giveItems(Wererats_Fang, 1L);
            qs.playSound("ItemSound.quest_itemget");
        }
        return null;
    }

    
}
