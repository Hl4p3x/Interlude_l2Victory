package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;
import java.util.List;

public class _606_WarwithVarkaSilenos extends Quest {
    private static final int KADUN_ZU_KETRA = 31370;
    private static final int VARKAS_MANE = 7233;
    private static final int VARKAS_MANE_DROP_CHANCE = 80;
    private static final int HORN_OF_BUFFALO = 7186;
    private static final List<Integer> VARKA_NPC_LIST = Arrays.asList(21350, 21351, 21353, 21354, 21355, 21357, 21358, 21360, 21361, 21362, 21364, 21365, 21366, 21368, 21369, 21370, 21371, 21372, 21373, 21374);

    public _606_WarwithVarkaSilenos() {
        super(true);
        addStartNpc(KADUN_ZU_KETRA);
        addKillId(VARKA_NPC_LIST);
        addQuestItem(VARKAS_MANE);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "quest_accept":
                htmltext = "elder_kadun_zu_ketra_q0606_0104.htm";
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "606_3":
                final long ec = st.getQuestItemsCount(VARKAS_MANE) / 5L;
                if (ec > 0L) {
                    htmltext = "elder_kadun_zu_ketra_q0606_0202.htm";
                    st.takeItems(VARKAS_MANE, ec * 5L);
                    st.giveItems(HORN_OF_BUFFALO, ec);
                } else {
                    htmltext = "elder_kadun_zu_ketra_q0606_0203.htm";
                }
                break;
            case "606_4":
                htmltext = "elder_kadun_zu_ketra_q0606_0204.htm";
                st.takeItems(VARKAS_MANE, -1L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getLevel() >= 74) {
                htmltext = "elder_kadun_zu_ketra_q0606_0101.htm";
            } else {
                htmltext = "elder_kadun_zu_ketra_q0606_0103.htm";
                st.exitCurrentQuest(true);
            }
        } else if (cond == 1 && st.getQuestItemsCount(VARKAS_MANE) == 0L) {
            htmltext = "elder_kadun_zu_ketra_q0606_0106.htm";
        } else if (cond == 1 && st.getQuestItemsCount(VARKAS_MANE) > 0L) {
            htmltext = "elder_kadun_zu_ketra_q0606_0105.htm";
        }
        return htmltext;
    }

    private boolean isVarkaNpc(final int npc) {
        return VARKA_NPC_LIST.contains(npc);
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        if (isVarkaNpc(npc.getNpcId()) && st.getCond() == 1 && st.getQuestItemsCount(VARKAS_MANE) < 100) {
            //st.rollAndGive(7233, 1, VARKAS_MANE_DROP_CHANCE);
            if (Rnd.chance(VARKAS_MANE_DROP_CHANCE)) {
                st.giveItems(VARKAS_MANE, 1);
                if(st.getQuestItemsCount(VARKAS_MANE) == 100) {
                    st.playSound(SOUND_MIDDLE);
                } else {
                    st.playSound(SOUND_ITEMGET);
                }
            }
        }
        return null;
    }
}
