package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _156_MillenniumLove extends Quest {
    int LILITHS_LETTER;
    int THEONS_DIARY;
    int GR_COMP_PACKAGE_SS;
    int GR_COMP_PACKAGE_SPS;

    public _156_MillenniumLove() {
        super(false);
        LILITHS_LETTER = 1022;
        THEONS_DIARY = 1023;
        GR_COMP_PACKAGE_SS = 5250;
        GR_COMP_PACKAGE_SPS = 5256;
        addStartNpc(30368);
        addTalkId(30368);
        addTalkId(30368);
        addTalkId(30368);
        addTalkId(30369);
        addQuestItem(LILITHS_LETTER, THEONS_DIARY);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "30368-06.htm":
                st.giveItems(LILITHS_LETTER, 1L);
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "156_1":
                st.takeItems(LILITHS_LETTER, -1L);
                if (st.getQuestItemsCount(THEONS_DIARY) == 0L) {
                    st.giveItems(THEONS_DIARY, 1L);
                    st.setCond(2);
                }
                htmltext = "30369-03.htm";
                break;
            case "156_2":
                st.takeItems(LILITHS_LETTER, -1L);
                st.playSound("ItemSound.quest_finish");
                htmltext = "30369-04.htm";
                st.exitCurrentQuest(false);
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (npcId == 30368) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() >= 15) {
                    htmltext = "30368-02.htm";
                } else {
                    htmltext = "30368-05.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 && st.getQuestItemsCount(LILITHS_LETTER) == 1L) {
                htmltext = "30368-07.htm";
            } else if (cond == 2 && st.getQuestItemsCount(THEONS_DIARY) == 1L) {
                st.takeItems(THEONS_DIARY, -1L);
                if (st.getPlayer().getClassId().isMage()) {
                    st.giveItems(GR_COMP_PACKAGE_SPS, 1L);
                } else {
                    st.giveItems(GR_COMP_PACKAGE_SS, 1L);
                }
                st.playSound("ItemSound.quest_finish");
                htmltext = "30368-08.htm";
                st.exitCurrentQuest(false);
            }
        } else if (npcId == 30369) {
            if (cond == 1 && st.getQuestItemsCount(LILITHS_LETTER) == 1L) {
                htmltext = "30369-02.htm";
            } else if (cond == 2 && st.getQuestItemsCount(THEONS_DIARY) == 1L) {
                htmltext = "30369-05.htm";
            }
        }
        return htmltext;
    }
}
