package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _162_CurseOfUndergroundFortress extends Quest {
    int BONE_FRAGMENT3;
    int ELF_SKULL;
    int BONE_SHIELD;

    public _162_CurseOfUndergroundFortress() {
        super(false);
        BONE_FRAGMENT3 = 1158;
        ELF_SKULL = 1159;
        BONE_SHIELD = 625;
        addStartNpc(30147);
        addTalkId(30147);
        addKillId(20033);
        addKillId(20345);
        addKillId(20371);
        addKillId(20463);
        addKillId(20464);
        addKillId(20504);
        addQuestItem(ELF_SKULL, BONE_FRAGMENT3);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        if ("30147-04.htm".equals(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            htmltext = "30147-04.htm";
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getRace() == Race.darkelf) {
                htmltext = "30147-00.htm";
            } else if (st.getPlayer().getLevel() >= 12) {
                htmltext = "30147-02.htm";
            } else {
                htmltext = "30147-01.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1 && st.getQuestItemsCount(ELF_SKULL) + st.getQuestItemsCount(BONE_FRAGMENT3) < 13L) {
            htmltext = "30147-05.htm";
        } else if (cond == 2 && st.getQuestItemsCount(ELF_SKULL) + st.getQuestItemsCount(BONE_FRAGMENT3) >= 13L) {
            htmltext = "30147-06.htm";
            st.giveItems(BONE_SHIELD, 1L);
            st.giveItems(57, 24000L);
            st.takeItems(ELF_SKULL, -1L);
            st.takeItems(BONE_FRAGMENT3, -1L);
            st.setCond(0);
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if ((npcId == 20463 || npcId == 20464 || npcId == 20504) && cond == 1 && Rnd.chance(25) && st.getQuestItemsCount(BONE_FRAGMENT3) < 10L) {
            st.giveItems(BONE_FRAGMENT3, 1L);
            if (st.getQuestItemsCount(BONE_FRAGMENT3) == 10L) {
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if ((npcId == 20033 || npcId == 20345 || npcId == 20371) && cond == 1 && Rnd.chance(25) && st.getQuestItemsCount(ELF_SKULL) < 3L) {
            st.giveItems(ELF_SKULL, 1L);
            if (st.getQuestItemsCount(ELF_SKULL) == 3L) {
                st.playSound("ItemSound.quest_middle");
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        if (st.getQuestItemsCount(BONE_FRAGMENT3) == 10L && st.getQuestItemsCount(ELF_SKULL) == 3L) {
            st.setCond(2);
        }
        return null;
    }
}
