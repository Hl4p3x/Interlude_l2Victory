package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;

public class _643_RiseAndFallOfTheElrokiTribe extends Quest {
    private static final int DROP_CHANCE = 75;
    private static final int BONES_OF_A_PLAINS_DINOSAUR = 8776;
    private static final int[] PLAIN_DINOSAURS = {22208, 22209, 22210, 22211, 22212, 22213, 22221, 22222, 22226, 22227};
    private static final int[] REWARDS = {8712, 8713, 8714, 8715, 8716, 8717, 8718, 8719, 8720, 8721, 8722};

    public _643_RiseAndFallOfTheElrokiTribe() {
        super(true);
        addStartNpc(32106);
        addTalkId(32117);
        Arrays.stream(PLAIN_DINOSAURS).forEach(this::addKillId);
        addQuestItem(BONES_OF_A_PLAINS_DINOSAUR);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
        if ("singsing_q0643_05.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("shaman_caracawe_q0643_06.htm".equalsIgnoreCase(event)) {
            if (count >= 300L) {
                st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, 300L);
                st.giveItems(REWARDS[Rnd.get(REWARDS.length)], 5L, false);
            } else {
                htmltext = "shaman_caracawe_q0643_05.htm";
            }
        } else if ("None".equalsIgnoreCase(event)) {
            htmltext = null;
        } else if ("Quit".equalsIgnoreCase(event)) {
            htmltext = null;
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        if (st.getCond() == 0) {
            if (st.getPlayer().getLevel() >= 75) {
                htmltext = "singsing_q0643_01.htm";
            } else {
                htmltext = "singsing_q0643_04.htm";
                st.exitCurrentQuest(true);
            }
        } else if (st.getState() == 2) {
            if (npcId == 32106) {
                final long count = st.getQuestItemsCount(BONES_OF_A_PLAINS_DINOSAUR);
                if (count == 0L) {
                    htmltext = "singsing_q0643_08.htm";
                } else {
                    htmltext = "singsing_q0643_08.htm";
                    st.takeItems(BONES_OF_A_PLAINS_DINOSAUR, -1L);
                    st.giveItems(57, count * 1374L, false);
                }
            } else if (npcId == 32117) {
                htmltext = "shaman_caracawe_q0643_02.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (st.getCond() == 1) {
            st.rollAndGive(BONES_OF_A_PLAINS_DINOSAUR, 1, (double) DROP_CHANCE);
        }
        return null;
    }
}
