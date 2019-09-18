package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _619_RelicsOfTheOldEmpire extends Quest {
    public static final int[] Recipes;
    private static final int Entrance_Pass_to_the_Sepulcher = 7075;
    private static final int Broken_Relic_Part = 7254;
    private static final int GHOST = 31538;
    private static final Map<Integer, Integer> drops;

    static {
        (drops = new HashMap<>()).put(18120, 138);
        _619_RelicsOfTheOldEmpire.drops.put(18121, 131);
        _619_RelicsOfTheOldEmpire.drops.put(18122, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18123, 138);
        _619_RelicsOfTheOldEmpire.drops.put(18124, 131);
        _619_RelicsOfTheOldEmpire.drops.put(18125, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18126, 138);
        _619_RelicsOfTheOldEmpire.drops.put(18127, 131);
        _619_RelicsOfTheOldEmpire.drops.put(18128, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18129, 138);
        _619_RelicsOfTheOldEmpire.drops.put(18130, 131);
        _619_RelicsOfTheOldEmpire.drops.put(18131, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18132, 141);
        _619_RelicsOfTheOldEmpire.drops.put(18133, 130);
        _619_RelicsOfTheOldEmpire.drops.put(18134, 98);
        _619_RelicsOfTheOldEmpire.drops.put(18135, 130);
        _619_RelicsOfTheOldEmpire.drops.put(18136, 130);
        _619_RelicsOfTheOldEmpire.drops.put(18137, 96);
        _619_RelicsOfTheOldEmpire.drops.put(18138, 129);
        _619_RelicsOfTheOldEmpire.drops.put(18139, 127);
        _619_RelicsOfTheOldEmpire.drops.put(18140, 128);
        _619_RelicsOfTheOldEmpire.drops.put(18141, 64);
        _619_RelicsOfTheOldEmpire.drops.put(18142, 64);
        _619_RelicsOfTheOldEmpire.drops.put(18143, 64);
        _619_RelicsOfTheOldEmpire.drops.put(18144, 64);
        _619_RelicsOfTheOldEmpire.drops.put(18145, 53);
        _619_RelicsOfTheOldEmpire.drops.put(18146, 56);
        _619_RelicsOfTheOldEmpire.drops.put(18147, 51);
        _619_RelicsOfTheOldEmpire.drops.put(18148, 60);
        _619_RelicsOfTheOldEmpire.drops.put(18149, 53);
        _619_RelicsOfTheOldEmpire.drops.put(18166, 99);
        _619_RelicsOfTheOldEmpire.drops.put(18167, 98);
        _619_RelicsOfTheOldEmpire.drops.put(18168, 101);
        _619_RelicsOfTheOldEmpire.drops.put(18169, 97);
        _619_RelicsOfTheOldEmpire.drops.put(18170, 97);
        _619_RelicsOfTheOldEmpire.drops.put(18171, 101);
        _619_RelicsOfTheOldEmpire.drops.put(18172, 96);
        _619_RelicsOfTheOldEmpire.drops.put(18173, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18174, 132);
        _619_RelicsOfTheOldEmpire.drops.put(18175, 101);
        _619_RelicsOfTheOldEmpire.drops.put(18176, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18177, 132);
        _619_RelicsOfTheOldEmpire.drops.put(18178, 101);
        _619_RelicsOfTheOldEmpire.drops.put(18179, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18180, 132);
        _619_RelicsOfTheOldEmpire.drops.put(18181, 101);
        _619_RelicsOfTheOldEmpire.drops.put(18182, 100);
        _619_RelicsOfTheOldEmpire.drops.put(18183, 132);
        _619_RelicsOfTheOldEmpire.drops.put(18184, 101);
        _619_RelicsOfTheOldEmpire.drops.put(18185, 133);
        _619_RelicsOfTheOldEmpire.drops.put(18186, 134);
        _619_RelicsOfTheOldEmpire.drops.put(18187, 130);
        _619_RelicsOfTheOldEmpire.drops.put(18188, 98);
        _619_RelicsOfTheOldEmpire.drops.put(18189, 130);
        _619_RelicsOfTheOldEmpire.drops.put(18190, 130);
        _619_RelicsOfTheOldEmpire.drops.put(18191, 96);
        _619_RelicsOfTheOldEmpire.drops.put(18192, 129);
        _619_RelicsOfTheOldEmpire.drops.put(18193, 127);
        _619_RelicsOfTheOldEmpire.drops.put(18194, 128);
        _619_RelicsOfTheOldEmpire.drops.put(18195, 98);
        _619_RelicsOfTheOldEmpire.drops.put(18212, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18213, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18214, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18215, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18216, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18217, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18218, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18219, 510);
        _619_RelicsOfTheOldEmpire.drops.put(18220, 134);
        _619_RelicsOfTheOldEmpire.drops.put(18221, 138);
        _619_RelicsOfTheOldEmpire.drops.put(18222, 131);
        _619_RelicsOfTheOldEmpire.drops.put(18223, 98);
        _619_RelicsOfTheOldEmpire.drops.put(18224, 132);
        _619_RelicsOfTheOldEmpire.drops.put(18225, 131);
        _619_RelicsOfTheOldEmpire.drops.put(18226, 96);
        _619_RelicsOfTheOldEmpire.drops.put(18227, 166);
        _619_RelicsOfTheOldEmpire.drops.put(18228, 125);
        _619_RelicsOfTheOldEmpire.drops.put(18229, 128);
        _619_RelicsOfTheOldEmpire.drops.put(18230, 53);
        _619_RelicsOfTheOldEmpire.drops.put(21396, 36);
        _619_RelicsOfTheOldEmpire.drops.put(21397, 39);
        _619_RelicsOfTheOldEmpire.drops.put(21398, 48);
        _619_RelicsOfTheOldEmpire.drops.put(21399, 62);
        _619_RelicsOfTheOldEmpire.drops.put(21400, 42);
        _619_RelicsOfTheOldEmpire.drops.put(21401, 48);
        _619_RelicsOfTheOldEmpire.drops.put(21402, 47);
        _619_RelicsOfTheOldEmpire.drops.put(21403, 49);
        _619_RelicsOfTheOldEmpire.drops.put(21404, 34);
        _619_RelicsOfTheOldEmpire.drops.put(21405, 36);
        _619_RelicsOfTheOldEmpire.drops.put(21406, 61);
        _619_RelicsOfTheOldEmpire.drops.put(21407, 60);
        _619_RelicsOfTheOldEmpire.drops.put(21408, 70);
        _619_RelicsOfTheOldEmpire.drops.put(21409, 70);
        _619_RelicsOfTheOldEmpire.drops.put(21410, 45);
        _619_RelicsOfTheOldEmpire.drops.put(21411, 46);
        _619_RelicsOfTheOldEmpire.drops.put(21412, 52);
        _619_RelicsOfTheOldEmpire.drops.put(21413, 52);
        _619_RelicsOfTheOldEmpire.drops.put(21414, 51);
        _619_RelicsOfTheOldEmpire.drops.put(21415, 51);
        _619_RelicsOfTheOldEmpire.drops.put(21416, 83);
        _619_RelicsOfTheOldEmpire.drops.put(21417, 83);
        _619_RelicsOfTheOldEmpire.drops.put(21418, 43);
        _619_RelicsOfTheOldEmpire.drops.put(21419, 36);
        _619_RelicsOfTheOldEmpire.drops.put(21420, 63);
        _619_RelicsOfTheOldEmpire.drops.put(21421, 53);
        _619_RelicsOfTheOldEmpire.drops.put(21422, 68);
        _619_RelicsOfTheOldEmpire.drops.put(21423, 69);
        _619_RelicsOfTheOldEmpire.drops.put(21424, 89);
        _619_RelicsOfTheOldEmpire.drops.put(21425, 69);
        _619_RelicsOfTheOldEmpire.drops.put(21426, 38);
        _619_RelicsOfTheOldEmpire.drops.put(21427, 49);
        _619_RelicsOfTheOldEmpire.drops.put(21428, 55);
        _619_RelicsOfTheOldEmpire.drops.put(21429, 65);
        _619_RelicsOfTheOldEmpire.drops.put(21430, 70);
        _619_RelicsOfTheOldEmpire.drops.put(21431, 91);
        _619_RelicsOfTheOldEmpire.drops.put(21432, 156);
        _619_RelicsOfTheOldEmpire.drops.put(21433, 66);
        _619_RelicsOfTheOldEmpire.drops.put(21434, 135);
        _619_RelicsOfTheOldEmpire.drops.put(21435, 67);
        _619_RelicsOfTheOldEmpire.drops.put(21436, 67);
        _619_RelicsOfTheOldEmpire.drops.put(21437, 17);
        _619_RelicsOfTheOldEmpire.drops.put(21798, 36);
        _619_RelicsOfTheOldEmpire.drops.put(21799, 52);
        _619_RelicsOfTheOldEmpire.drops.put(21800, 31);
        Recipes = new int[]{6881, 6883, 6885, 6887, 7580, 6891, 6893, 6895, 6897, 6899};
    }

    public _619_RelicsOfTheOldEmpire() {
        super(true);
        addStartNpc(31538);
        addKillId(_619_RelicsOfTheOldEmpire.drops.keySet());
        addQuestItem(7254);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        switch (event) {
            case "explorer_ghost_a_q0619_03.htm":
                if (st.getPlayer().getLevel() < 74) {
                    st.exitCurrentQuest(true);
                    return "explorer_ghost_a_q0619_02.htm";
                }
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "explorer_ghost_a_q0619_09.htm":
                if (st.getQuestItemsCount(7254) < 1000L) {
                    return (st.getQuestItemsCount(7075) > 0L) ? "explorer_ghost_a_q0619_06.htm" : "explorer_ghost_a_q0619_07.htm";
                }
                st.takeItems(7254, 1000L);
                st.giveItems(_619_RelicsOfTheOldEmpire.Recipes[Rnd.get(_619_RelicsOfTheOldEmpire.Recipes.length)], 1L);
                return "explorer_ghost_a_q0619_09.htm";
            case "explorer_ghost_a_q0619_10.htm":
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                break;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getState() == 1) {
            if (st.getPlayer().getLevel() < 74) {
                st.exitCurrentQuest(true);
                return "explorer_ghost_a_q0619_02.htm";
            }
            st.setCond(0);
            return "explorer_ghost_a_q0619_01.htm";
        } else {
            if (st.getQuestItemsCount(7254) >= 1000L) {
                return "explorer_ghost_a_q0619_04.htm";
            }
            return (st.getQuestItemsCount(7075) > 0L) ? "explorer_ghost_a_q0619_06.htm" : "explorer_ghost_a_q0619_07.htm";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final Integer Broken_Relic_Part_chance = _619_RelicsOfTheOldEmpire.drops.get(npcId);
        if (Broken_Relic_Part_chance == null) {
            return null;
        }
        st.rollAndGive(7254, 1, (double) Broken_Relic_Part_chance);
        if (npcId > 20000) {
            st.rollAndGive(7075, 1, 3.0);
        }
        return null;
    }

    
}
