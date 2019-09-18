package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _169_OffspringOfNightmares extends Quest {
    private static final int Vlasty = 30145;
    private static final int CrackedSkull = 1030;
    private static final int PerfectSkull = 1031;
    private static final int BoneGaiters = 31;
    private static final int DarkHorror = 20105;
    private static final int LesserDarkHorror = 20025;

    public _169_OffspringOfNightmares() {
        super(false);
        addStartNpc(30145);
        addTalkId(30145);
        addKillId(20105);
        addKillId(20025);
        addQuestItem(1030, 1031);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30145-04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30145-08.htm".equalsIgnoreCase(event)) {
            st.takeItems(1030, -1L);
            st.takeItems(1031, -1L);
            st.giveItems(31, 1L);
            st.giveItems(57, 17050L, true);
            st.getPlayer().addExpAndSp(17475L, 818L);
            if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q4")) {
                st.getPlayer().setVar("p1q4", "1", -1L);
                st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
            }
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30145) {
            switch (cond) {
                case 0:
                    if (st.getPlayer().getRace() != Race.darkelf) {
                        htmltext = "30145-00.htm";
                        st.exitCurrentQuest(true);
                    } else if (st.getPlayer().getLevel() >= 15) {
                        htmltext = "30145-03.htm";
                    } else {
                        htmltext = "30145-02.htm";
                        st.exitCurrentQuest(true);
                    }
                    break;
                case 1:
                    if (st.getQuestItemsCount(1030) == 0L) {
                        htmltext = "30145-05.htm";
                    } else {
                        htmltext = "30145-06.htm";
                    }
                    break;
                case 2:
                    htmltext = "30145-07.htm";
                    break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        if (cond == 1) {
            if (Rnd.chance(20) && st.getQuestItemsCount(1031) == 0L) {
                st.giveItems(1031, 1L);
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
                st.setState(2);
            }
            if (Rnd.chance(70)) {
                st.giveItems(1030, 1L);
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
