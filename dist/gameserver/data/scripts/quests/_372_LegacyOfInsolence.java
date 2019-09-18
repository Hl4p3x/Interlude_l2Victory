package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _372_LegacyOfInsolence extends Quest {
    private static final int HOLLY = 30839;
    private static final int WALDERAL = 30844;
    private static final int DESMOND = 30855;
    private static final int PATRIN = 30929;
    private static final int CLAUDIA = 31001;
    private static final int CORRUPT_SAGE = 20817;
    private static final int ERIN_EDIUNCE = 20821;
    private static final int HALLATE_INSP = 20825;
    private static final int PLATINUM_OVL = 20829;
    private static final int PLATINUM_PRE = 21069;
    private static final int MESSENGER_A1 = 21062;
    private static final int MESSENGER_A2 = 21063;
    private static final int Ancient_Red_Papyrus = 5966;
    private static final int Ancient_Blue_Papyrus = 5967;
    private static final int Ancient_Black_Papyrus = 5968;
    private static final int Ancient_White_Papyrus = 5969;
    private static final int[] Revelation_of_the_Seals_Range = {5972, 5978};
    private static final int[] Ancient_Epic_Chapter_Range = {5979, 5983};
    private static final int[] Imperial_Genealogy_Range = {5984, 5988};
    private static final int[] Blueprint_Tower_of_Insolence_Range = {5989, 6001};
    private static final int[] Reward_Dark_Crystal = {5368, 5392, 5426};
    private static final int[] Reward_Tallum = {5370, 5394, 5428};
    private static final int[] Reward_Nightmare = {5380, 5404, 5430};
    private static final int[] Reward_Majestic = {5382, 5406, 5432};
    private static final int Three_Recipes_Reward_Chance = 1;
    private static final int Two_Recipes_Reward_Chance = 2;
    private static final int Adena4k_Reward_Chance = 2;

    private final Map<Integer, int[]> DROPLIST;

    public _372_LegacyOfInsolence() {
        super(true);
        DROPLIST = new HashMap<>();
        addStartNpc(WALDERAL);
        addTalkId(HOLLY);
        addTalkId(DESMOND);
        addTalkId(PATRIN);
        addTalkId(CLAUDIA);
        addKillId(CORRUPT_SAGE);
        addKillId(ERIN_EDIUNCE);
        addKillId(HALLATE_INSP);
        addKillId(PLATINUM_OVL);
        addKillId(PLATINUM_PRE);
        addKillId(MESSENGER_A1);
        addKillId(MESSENGER_A2);
        DROPLIST.put(CORRUPT_SAGE, new int[]{Ancient_Red_Papyrus, 35});
        DROPLIST.put(ERIN_EDIUNCE, new int[]{Ancient_Red_Papyrus, 40});
        DROPLIST.put(HALLATE_INSP, new int[]{Ancient_Red_Papyrus, 45});
        DROPLIST.put(PLATINUM_OVL, new int[]{Ancient_Blue_Papyrus, 40});
        DROPLIST.put(PLATINUM_PRE, new int[]{Ancient_Black_Papyrus, 25});
        DROPLIST.put(MESSENGER_A1, new int[]{Ancient_White_Papyrus, 25});
        DROPLIST.put(MESSENGER_A2, new int[]{Ancient_White_Papyrus, 25});
    }

    private static void giveRecipe(final QuestState st, final int recipe_id) {
        st.giveItems(recipe_id, 1L);
    }

    private static boolean check_and_reward(final QuestState st, final int[] items_range, final int[] reward) {
        for (int item_id = items_range[0]; item_id <= items_range[1]; ++item_id) {
            if (st.getQuestItemsCount(item_id) < 1L) {
                return false;
            }
        }
        for (int item_id = items_range[0]; item_id <= items_range[1]; ++item_id) {
            st.takeItems(item_id, 1L);
        }
        if (Rnd.chance(Three_Recipes_Reward_Chance)) {
            for (final int reward_item_id : reward) {
                giveRecipe(st, reward_item_id);
            }
            st.playSound("ItemSound.quest_jackpot");
        } else if (Rnd.chance(Two_Recipes_Reward_Chance)) {
            final int ignore_reward_id = reward[Rnd.get(reward.length)];
            for (final int reward_item_id2 : reward) {
                if (reward_item_id2 != ignore_reward_id) {
                    giveRecipe(st, reward_item_id2);
                }
            }
            st.playSound("ItemSound.quest_jackpot");
        } else if (Rnd.chance(Adena4k_Reward_Chance)) {
            st.giveItems(57, 4000L, false);
        } else {
            giveRecipe(st, reward[Rnd.get(reward.length)]);
        }
        return true;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int _state = st.getState();
        if (_state == 1) {
            if ("30844-6.htm".equalsIgnoreCase(event)) {
                st.setState(2);
                st.setCond(1);
                st.playSound("ItemSound.quest_accept");
            } else if ("30844-9.htm".equalsIgnoreCase(event)) {
                st.setCond(2);
            } else if ("30844-7.htm".equalsIgnoreCase(event)) {
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
            }
        } else if (_state == 2) {
            if ("30839-exchange".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Imperial_Genealogy_Range, Reward_Dark_Crystal) ? "30839-2.htm" : "30839-3.htm");
            } else if ("30855-exchange".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Revelation_of_the_Seals_Range, Reward_Majestic) ? "30855-2.htm" : "30855-3.htm");
            } else if ("30929-exchange".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Ancient_Epic_Chapter_Range, Reward_Tallum) ? "30839-2.htm" : "30839-3.htm");
            } else if ("31001-exchange".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Revelation_of_the_Seals_Range, Reward_Nightmare) ? "30839-2.htm" : "30839-3.htm");
            } else if ("30844-DarkCrystal".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Blueprint_Tower_of_Insolence_Range, Reward_Dark_Crystal) ? "30844-11.htm" : "30844-12.htm");
            } else if ("30844-Tallum".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Blueprint_Tower_of_Insolence_Range, Reward_Tallum) ? "30844-11.htm" : "30844-12.htm");
            } else if ("30844-Nightmare".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Blueprint_Tower_of_Insolence_Range, Reward_Nightmare) ? "30844-11.htm" : "30844-12.htm");
            } else if ("30844-Majestic".equalsIgnoreCase(event)) {
                htmltext = (check_and_reward(st, Blueprint_Tower_of_Insolence_Range, Reward_Majestic) ? "30844-11.htm" : "30844-12.htm");
            }
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != WALDERAL) {
                return htmltext;
            }
            if (st.getPlayer().getLevel() >= 59) {
                htmltext = "30844-4.htm";
            } else {
                htmltext = "30844-5.htm";
                st.exitCurrentQuest(true);
            }
        } else if (_state == 2) {
            htmltext = String.valueOf(npcId) + "-1.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        final int[] drop = DROPLIST.get(npc.getNpcId());
        if (drop == null) {
            return null;
        }
        qs.rollAndGive(drop[0], 1, (double) drop[1]);
        return null;
    }

    
}
