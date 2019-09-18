package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _257_GuardIsBusy extends Quest {
    int GLUDIO_LORDS_MARK;
    int ORC_AMULET;
    int ORC_NECKLACE;
    int WEREWOLF_FANG;
    int ADENA;

    public _257_GuardIsBusy() {
        super(false);
        GLUDIO_LORDS_MARK = 1084;
        ORC_AMULET = 752;
        ORC_NECKLACE = 1085;
        WEREWOLF_FANG = 1086;
        ADENA = 57;
        addStartNpc(30039);
        addKillId(20130, 20131, 20132, 20342, 20343, 20006, 20093, 20096, 20098);
        addQuestItem(ORC_AMULET, ORC_NECKLACE, WEREWOLF_FANG, GLUDIO_LORDS_MARK);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("gilbert_q0257_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.takeItems(GLUDIO_LORDS_MARK, -1L);
            st.giveItems(GLUDIO_LORDS_MARK, 1L);
        } else if ("257_2".equalsIgnoreCase(event)) {
            htmltext = "gilbert_q0257_05.htm";
            st.takeItems(GLUDIO_LORDS_MARK, -1L);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("257_3".equalsIgnoreCase(event)) {
            htmltext = "gilbert_q0257_06.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 6) {
                htmltext = "gilbert_q0257_02.htm";
                return htmltext;
            }
            htmltext = "gilbert_q0257_01.htm";
            st.exitCurrentQuest(true);
        } else if (cond == 1 && st.getQuestItemsCount(ORC_AMULET) < 1L && st.getQuestItemsCount(ORC_NECKLACE) < 1L && st.getQuestItemsCount(WEREWOLF_FANG) < 1L) {
            htmltext = "gilbert_q0257_04.htm";
        } else if (cond == 1 && (st.getQuestItemsCount(ORC_AMULET) > 0L || st.getQuestItemsCount(ORC_NECKLACE) > 0L || st.getQuestItemsCount(WEREWOLF_FANG) > 0L)) {
            st.giveItems(ADENA, 12L * st.getQuestItemsCount(ORC_AMULET) + 20L * st.getQuestItemsCount(ORC_NECKLACE) + 25L * st.getQuestItemsCount(WEREWOLF_FANG), false);
            if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q2")) {
                st.getPlayer().setVar("p1q2", "1", -1L);
                st.getPlayer().sendPacket(new ExShowScreenMessage("Acquisition of Soulshot for beginners complete.\n                  Go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                final QuestState qs = st.getPlayer().getQuestState(_255_Tutorial.class);
                if (qs != null && qs.getInt("Ex") != 10) {
                    st.showQuestionMark(26);
                    qs.set("Ex", "10");
                    if (st.getPlayer().getClassId().isMage()) {
                        st.playTutorialVoice("tutorial_voice_027");
                        st.giveItems(5790, 3000L);
                    } else {
                        st.playTutorialVoice("tutorial_voice_026");
                        st.giveItems(5789, 6000L);
                    }
                }
            }
            st.takeItems(ORC_AMULET, -1L);
            st.takeItems(ORC_NECKLACE, -1L);
            st.takeItems(WEREWOLF_FANG, -1L);
            htmltext = "gilbert_q0257_07.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (st.getQuestItemsCount(GLUDIO_LORDS_MARK) > 0L && st.getCond() > 0) {
            switch (npcId) {
                case 20130:
                case 20131:
                case 20006:
                    st.rollAndGive(ORC_AMULET, 1, 50.0);
                    break;
                case 20093:
                case 20096:
                case 20098:
                    st.rollAndGive(ORC_NECKLACE, 1, 50.0);
                    break;
                case 20132:
                    st.rollAndGive(WEREWOLF_FANG, 1, 33.0);
                    break;
                case 20343:
                    st.rollAndGive(WEREWOLF_FANG, 1, 50.0);
                    break;
                case 20342:
                    st.rollAndGive(WEREWOLF_FANG, 1, 75.0);
                    break;
            }
        }
        return null;
    }
}
