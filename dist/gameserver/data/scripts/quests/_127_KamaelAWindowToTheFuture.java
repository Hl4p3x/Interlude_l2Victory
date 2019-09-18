package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPlayScene;

public class _127_KamaelAWindowToTheFuture extends Quest {
    private final int AKLAN;
    private final int ALDER;
    private final int DOMINIC;
    private final int JURIS;
    private final int KLAUS;
    private final int OLTLIN;
    private final int RODEMAI;
    private final int MARK_DOMINIC;
    private final int MARK_HUMAN;
    private final int MARK_DWARF;
    private final int MARK_ELF;
    private final int MARK_DELF;
    private final int MARK_ORC;

    public _127_KamaelAWindowToTheFuture() {
        super(false);
        AKLAN = 31288;
        ALDER = 32092;
        DOMINIC = 31350;
        JURIS = 30113;
        KLAUS = 30187;
        OLTLIN = 30862;
        RODEMAI = 30756;
        MARK_DOMINIC = 8939;
        MARK_HUMAN = 8940;
        MARK_DWARF = 8941;
        MARK_ELF = 8942;
        MARK_DELF = 8943;
        MARK_ORC = 8944;
        addStartNpc(DOMINIC);
        addTalkId(AKLAN);
        addTalkId(ALDER);
        addTalkId(JURIS);
        addTalkId(KLAUS);
        addTalkId(OLTLIN);
        addTalkId(RODEMAI);
        addQuestItem(MARK_DOMINIC, MARK_HUMAN, MARK_DWARF, MARK_ELF, MARK_DELF, MARK_ORC);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("dominic4.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(MARK_DOMINIC, 1L);
        } else if ("dominic6.htm".equalsIgnoreCase(event)) {
            st.takeItems(MARK_HUMAN, -1L);
            st.takeItems(MARK_DWARF, -1L);
            st.takeItems(MARK_ELF, -1L);
            st.takeItems(MARK_DELF, -1L);
            st.takeItems(MARK_ORC, -1L);
            st.takeItems(MARK_DOMINIC, -1L);
            st.giveItems(57, 15910L, false);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if ("klaus6.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
            st.playSound("ItemSound.quest_middle");
        } else if ("klaus8.htm".equalsIgnoreCase(event)) {
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        } else if ("alder5.htm".equalsIgnoreCase(event)) {
            st.setCond(4);
            st.giveItems(MARK_DWARF, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("aklan4.htm".equalsIgnoreCase(event)) {
            st.setCond(5);
            st.giveItems(MARK_ORC, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("oltlin4.htm".equalsIgnoreCase(event)) {
            st.setCond(6);
            st.giveItems(MARK_DELF, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("juris4.htm".equalsIgnoreCase(event)) {
            st.setCond(7);
            st.giveItems(MARK_ELF, 1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("kamaelstory".equalsIgnoreCase(event)) {
            htmltext = "rodemai6.htm";
            st.setCond(8);
            st.playSound("ItemSound.quest_middle");
            st.getPlayer().sendPacket(ExPlayScene.STATIC);
        } else if ("rodemai5.htm".equalsIgnoreCase(event)) {
            st.setCond(9);
            st.playSound("ItemSound.quest_middle");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == DOMINIC) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 1) {
                    htmltext = "dominic1.htm";
                } else {
                    htmltext = "dominic0.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 9) {
                htmltext = "dominic5.htm";
            }
        } else if (npcId == KLAUS) {
            if (cond == 1) {
                htmltext = "klaus1.htm";
            } else if (cond == 2) {
                htmltext = "klaus6.htm";
            }
        } else if (npcId == ALDER) {
            if (cond == 3) {
                htmltext = "alder1.htm";
            }
        } else if (npcId == AKLAN) {
            if (cond == 4) {
                htmltext = "aklan1.htm";
            }
        } else if (npcId == OLTLIN) {
            if (cond == 5) {
                htmltext = "oltlin1.htm";
            }
        } else if (npcId == JURIS) {
            if (cond == 6) {
                htmltext = "juris1.htm";
            }
        } else if (npcId == RODEMAI) {
            if (cond == 7) {
                htmltext = "rodemai1.htm";
            } else if (cond == 8) {
                htmltext = "rodemai4.htm";
            }
        }
        return htmltext;
    }

    
}
