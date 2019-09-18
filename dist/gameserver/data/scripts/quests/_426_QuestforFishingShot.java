package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;
import java.util.stream.IntStream;

public class _426_QuestforFishingShot extends Quest {
    private static final int SWEET_FLUID = 7586;
    private static final int[] MOBS = {20005, 20013, 20016, 20017, 20024, 20025, 20043, 20044, 20046, 20047, 20050, 20058, 20063, 20066, 20070, 20074, 20077, 20078, 20079, 20080, 20081, 20082, 20083, 20084, 20085, 20088, 20089, 20100, 20106, 20115, 20120, 20131, 20132, 20135, 20157, 20162, 20176, 20225, 20227, 20230, 20232, 20234, 20241, 20267, 20268, 20269, 20270, 20271, 20308, 20312, 20317, 20324, 20333, 20341, 20345, 20346, 20349, 20350, 20356, 20357, 20363, 20368, 20386, 20389, 20403, 20404, 20433, 20448, 20456, 20463, 20470, 20471, 20475, 20476, 20511, 20525, 20528, 20536, 20537, 20538, 20539, 20544, 20547, 20550, 20551, 20552, 20553, 20554, 20555, 20557, 20559, 20560, 20562, 20573, 20575, 20576, 20630, 20632, 20634, 20636, 20641, 20643, 20644, 20646, 20648, 20650, 20651, 20659, 20661, 20652, 20656, 20655, 20657, 20658, 20663, 20665, 20667, 20781, 20772, 20783, 20784, 20786, 20788, 20790, 20791, 20792, 20794, 20796, 20798, 20800, 20802, 20804, 20808, 20809, 20810, 20811, 20812, 20814, 20815, 20816, 20819, 20822, 20824, 20825, 20828, 20829, 20830, 20833, 20834, 20836, 20837, 20839, 20841, 20843, 20845, 20847, 20849, 20936, 20938, 20939, 20944, 20943, 20940, 20941, 20978, 20979, 20983, 20985, 20991, 20994, 21023, 21024, 21025, 21026, 21058, 21060, 21061, 21066, 21067, 21070, 21072, 21075, 21078, 21081, 21100, 21101, 21102, 21103, 21104, 21105, 21106, 21107, 21117, 21125, 21261, 21269, 21271, 21272, 21273, 21314, 21316, 21318, 21320, 21322, 21508, 21510, 21511, 21514, 21516, 21518, 21520, 21523, 21526, 21529, 21530, 21531, 21536, 21532, 21542, 21543, 21544};
    private static final int[] HMOBS = {20651, 20652, 20655, 20656, 20657, 20658, 20772, 20809, 20810, 20811, 20812, 20814, 20815, 20816, 20819, 20822, 20824, 20825, 20828, 20829, 20830, 20978, 20979, 20983, 20985, 21058, 21061, 21066, 21067, 21070, 21072, 21075, 21078, 21081, 21314, 21316, 21318, 21320, 21322, 21376, 21378, 21380, 21382, 21384, 21387, 21390, 21393, 21395, 21508, 21510, 21511, 21514, 21516, 21518, 21520, 21523, 21526, 21529, 21530, 21531, 21532, 21536, 21542, 21543, 21544};

    public _426_QuestforFishingShot() {
        super(true);
        IntStream.rangeClosed(31562, 31579).forEach(this::addStartNpc);
        addStartNpc(31696);
        addStartNpc(31697);
        addStartNpc(31989);
        addStartNpc(32007);
        addKillId(MOBS);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("4.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.setCond(1);
        } else if ("3.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int condition = st.getCond();
        final int id = st.getState();
        if (id == 1) {
            htmltext = "1.htm";
        } else if (condition == 1) {
            htmltext = "2.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (Rnd.chance(30)) {
            if (Arrays.binarySearch(HMOBS, npcId) >= 0) {
                st.giveItems(SWEET_FLUID, (long) (Rnd.get(5) + 1));
            } else {
                st.giveItems(SWEET_FLUID, (long) (Rnd.get(3) + 1));
            }
            st.playSound("ItemSound.quest_itemget");
        }
        return null;
    }
}
