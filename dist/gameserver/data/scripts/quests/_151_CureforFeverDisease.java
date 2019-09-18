package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _151_CureforFeverDisease extends Quest {
    int POISON_SAC;
    int FEVER_MEDICINE;
    int ROUND_SHIELD;

    public _151_CureforFeverDisease() {
        super(false);
        POISON_SAC = 703;
        FEVER_MEDICINE = 704;
        ROUND_SHIELD = 102;
        addStartNpc(30050);
        addTalkId(30032);
        addKillId(20103, 20106, 20108);
        addQuestItem(FEVER_MEDICINE, POISON_SAC);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30050-03.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        int cond = 0;
        if (id != 1) {
            cond = st.getCond();
        }
        if (npcId == 30050) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 15) {
                    htmltext = "30050-02.htm";
                } else {
                    htmltext = "30050-01.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 && st.getQuestItemsCount(POISON_SAC) == 0L && st.getQuestItemsCount(FEVER_MEDICINE) == 0L) {
                htmltext = "30050-04.htm";
            } else if (cond == 1 && st.getQuestItemsCount(POISON_SAC) == 1L) {
                htmltext = "30050-05.htm";
            } else if (cond == 3 && st.getQuestItemsCount(FEVER_MEDICINE) == 1L) {
                st.takeItems(FEVER_MEDICINE, -1L);
                st.giveItems(ROUND_SHIELD, 1L);
                st.getPlayer().addExpAndSp(13106L, 613L);
                if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4")) {
                    st.getPlayer().setVar("p1q4", "1", -1L);
                    st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                }
                htmltext = "30050-06.htm";
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
            }
        } else if (npcId == 30032) {
            if (cond == 2 && st.getQuestItemsCount(POISON_SAC) > 0L) {
                st.giveItems(FEVER_MEDICINE, 1L);
                st.takeItems(POISON_SAC, -1L);
                st.setCond(3);
                htmltext = "30032-01.htm";
            } else if (cond == 3 && st.getQuestItemsCount(FEVER_MEDICINE) > 0L) {
                htmltext = "30032-02.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if ((npcId == 20103 || npcId == 20106 || npcId == 20108) && st.getQuestItemsCount(POISON_SAC) == 0L && st.getCond() == 1 && Rnd.chance(50)) {
            st.setCond(2);
            st.giveItems(POISON_SAC, 1L);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }
}
