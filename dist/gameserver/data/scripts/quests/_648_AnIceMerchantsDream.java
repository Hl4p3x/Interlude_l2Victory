package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class _648_AnIceMerchantsDream extends Quest {
    private static final int Rafforty = 32020;
    private static final int Ice_Shelf = 32023;
    private static final int Silver_Hemocyte = 8057;
    private static final int Silver_Ice_Crystal = 8077;
    private static final int Black_Ice_Crystal = 8078;
    private static final int Silver_Hemocyte_Chance = 10;
    private static final int Silver2Black_Chance = 30;
    private static final List<Integer> silver2black = new ArrayList<>();

    public _648_AnIceMerchantsDream() {
        super(true);
        addStartNpc(Rafforty);
        addStartNpc(Ice_Shelf);
        IntStream.rangeClosed(22080, 22098).filter(i -> i != 22095).forEach(this::addKillId);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("repre_q0648_04.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("repre_q0648_22.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        if (_state != 2) {
            return event;
        }
        final long Silver_Ice_Crystal_Count = st.getQuestItemsCount(Silver_Ice_Crystal);
        final long Black_Ice_Crystal_Count = st.getQuestItemsCount(Black_Ice_Crystal);
        if ("repre_q0648_14.htm".equalsIgnoreCase(event)) {
            final long reward = Silver_Ice_Crystal_Count * 300L + Black_Ice_Crystal_Count * 1200L;
            if (reward <= 0L) {
                return "repre_q0648_15.htm";
            }
            st.takeItems(Silver_Ice_Crystal, -1L);
            st.takeItems(Black_Ice_Crystal, -1L);
            st.giveItems(57, reward);
        } else if ("ice_lathe_q0648_06.htm".equalsIgnoreCase(event)) {
            final int char_obj_id = st.getPlayer().getObjectId();
            synchronized (silver2black) {
                if (silver2black.contains(char_obj_id)) {
                    return event;
                }
                if (Silver_Ice_Crystal_Count <= 0L) {
                    return "cheat.htm";
                }
                silver2black.add(char_obj_id);
            }
            st.takeItems(Silver_Ice_Crystal, 1L);
            st.playSound("ItemSound2.broken_key");
        } else if ("ice_lathe_q0648_08.htm".equalsIgnoreCase(event)) {
            final Integer char_obj_id2 = st.getPlayer().getObjectId();
            synchronized (silver2black) {
                if (!silver2black.contains(char_obj_id2)) {
                    return "cheat.htm";
                }
                while (silver2black.contains(char_obj_id2)) {
                    silver2black.remove(char_obj_id2);
                }
            }
            if (!Rnd.chance(Silver2Black_Chance)) {
                st.playSound("ItemSound3.sys_enchant_failed");
                return "ice_lathe_q0648_09.htm";
            }
            st.giveItems(Black_Ice_Crystal, 1L);
            st.playSound("ItemSound3.sys_enchant_sucess");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        int cond = st.getCond();
        if (_state == 1) {
            if (npcId == Rafforty) {
                if (st.getPlayer().getLevel() >= 53) {
                    st.setCond(0);
                    return "repre_q0648_03.htm";
                }
                st.exitCurrentQuest(true);
                return "repre_q0648_01.htm";
            } else if (npcId == Ice_Shelf) {
                return "ice_lathe_q0648_01.htm";
            }
        }
        if (_state != 2) {
            return "noquest";
        }
        final long Silver_Ice_Crystal_Count = st.getQuestItemsCount(Silver_Ice_Crystal);
        if (npcId == Ice_Shelf) {
            return (Silver_Ice_Crystal_Count > 0L) ? "ice_lathe_q0648_03.htm" : "ice_lathe_q0648_02.htm";
        }
        final long Black_Ice_Crystal_Count = st.getQuestItemsCount(Black_Ice_Crystal);
        if (npcId == Rafforty) {
            final QuestState st_115 = st.getPlayer().getQuestState(_115_TheOtherSideOfTruth.class);
            if (st_115 != null && st_115.isCompleted()) {
                cond = 2;
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
            }
            if (cond == 1) {
                if (Silver_Ice_Crystal_Count > 0L || Black_Ice_Crystal_Count > 0L) {
                    return "repre_q0648_10.htm";
                }
                return "repre_q0648_08.htm";
            } else if (cond == 2) {
                return (Silver_Ice_Crystal_Count > 0L || Black_Ice_Crystal_Count > 0L) ? "repre_q0648_11.htm" : "repre_q0648_09.htm";
            }
        }
        return "noquest";
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        final int cond = qs.getCond();
        if (cond > 0) {
            qs.rollAndGive(Silver_Ice_Crystal, 1, (double) (npc.getNpcId() - 22050));
            if (cond == 2) {
                qs.rollAndGive(Silver_Hemocyte, 1, (double) Silver_Hemocyte_Chance);
            }
        }
        return null;
    }

    
}
