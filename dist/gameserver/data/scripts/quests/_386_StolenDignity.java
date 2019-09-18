package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _386_StolenDignity extends Quest {
    private static final int Romp = 30843;
    private static final int Stolen_Infernium_Ore = 6363;
    private static final int Required_Stolen_Infernium_Ore = 100;
    private static final Map<Integer, Integer> dropchances = new HashMap<>();
    private static final Map<Integer, Bingo> bingos = new HashMap<>();
    private static final int[][] Rewards_Win = {{5529, 10}, {5532, 10}, {5533, 10}, {5534, 10}, {5535, 10}, {5536, 10}, {5537, 10}, {5538, 10}, {5539, 10}, {5541, 10}, {5542, 10}, {5543, 10}, {5544, 10}, {5545, 10}, {5546, 10}, {5547, 10}, {5548, 10}, {8331, 10}, {8341, 10}, {8342, 10}, {8346, 10}, {8349, 10}, {8712, 10}, {8713, 10}, {8714, 10}, {8715, 10}, {8716, 10}, {8717, 10}, {8718, 10}, {8719, 10}, {8720, 10}, {8721, 10}, {8722, 10}};
    private static final int[][] Rewards_Lose = {{5529, 4}, {5532, 4}, {5533, 4}, {5534, 4}, {5535, 4}, {5536, 4}, {5537, 4}, {5538, 4}, {5539, 4}, {5541, 4}, {5542, 4}, {5543, 4}, {5544, 4}, {5545, 4}, {5546, 4}, {5547, 4}, {5548, 4}, {8331, 4}, {8341, 4}, {8342, 4}, {8346, 4}, {8349, 4}, {8712, 4}, {8713, 4}, {8714, 4}, {8715, 4}, {8716, 4}, {8717, 4}, {8718, 4}, {8719, 4}, {8720, 4}, {8721, 4}, {8722, 4}};

    public _386_StolenDignity() {
        super(true);
        addStartNpc(30843);
        _386_StolenDignity.dropchances.put(20670, 14);
        _386_StolenDignity.dropchances.put(20671, 14);
        _386_StolenDignity.dropchances.put(20954, 11);
        _386_StolenDignity.dropchances.put(20956, 13);
        _386_StolenDignity.dropchances.put(20958, 13);
        _386_StolenDignity.dropchances.put(20959, 13);
        _386_StolenDignity.dropchances.put(20960, 11);
        _386_StolenDignity.dropchances.put(20964, 13);
        _386_StolenDignity.dropchances.put(20969, 19);
        _386_StolenDignity.dropchances.put(20967, 18);
        _386_StolenDignity.dropchances.put(20970, 18);
        _386_StolenDignity.dropchances.put(20971, 18);
        _386_StolenDignity.dropchances.put(20974, 28);
        _386_StolenDignity.dropchances.put(20975, 28);
        _386_StolenDignity.dropchances.put(21001, 14);
        _386_StolenDignity.dropchances.put(21003, 18);
        _386_StolenDignity.dropchances.put(21005, 14);
        _386_StolenDignity.dropchances.put(21020, 16);
        _386_StolenDignity.dropchances.put(21021, 15);
        _386_StolenDignity.dropchances.put(21259, 15);
        _386_StolenDignity.dropchances.put(21089, 13);
        _386_StolenDignity.dropchances.put(21108, 19);
        _386_StolenDignity.dropchances.put(21110, 18);
        _386_StolenDignity.dropchances.put(21113, 25);
        _386_StolenDignity.dropchances.put(21114, 23);
        _386_StolenDignity.dropchances.put(21116, 25);
        for (final int kill_id : _386_StolenDignity.dropchances.keySet()) {
            addKillId(kill_id);
        }
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("warehouse_keeper_romp_q0386_05.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("warehouse_keeper_romp_q0386_08.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else if ("game".equalsIgnoreCase(event)) {
            if (st.getQuestItemsCount(6363) < 100L) {
                return "warehouse_keeper_romp_q0386_11.htm";
            }
            st.takeItems(6363, 100L);
            final int char_obj_id = st.getPlayer().getObjectId();
            if (_386_StolenDignity.bingos.containsKey(char_obj_id)) {
                _386_StolenDignity.bingos.remove(char_obj_id);
            }
            final Bingo bingo = new Bingo(st);
            _386_StolenDignity.bingos.put(char_obj_id, bingo);
            return bingo.getDialog("");
        } else if (event.contains("choice-")) {
            final int char_obj_id = st.getPlayer().getObjectId();
            if (!_386_StolenDignity.bingos.containsKey(char_obj_id)) {
                return null;
            }
            final Bingo bingo = _386_StolenDignity.bingos.get(char_obj_id);
            return bingo.Select(event.replaceFirst("choice-", ""));
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getState() != 1) {
            return (st.getQuestItemsCount(6363) < 100L) ? "warehouse_keeper_romp_q0386_06.htm" : "warehouse_keeper_romp_q0386_07.htm";
        }
        if (st.getPlayer().getLevel() < 58) {
            st.exitCurrentQuest(true);
            return "warehouse_keeper_romp_q0386_04.htm";
        }
        return "warehouse_keeper_romp_q0386_01.htm";
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        final Integer _chance = _386_StolenDignity.dropchances.get(npc.getNpcId());
        if (_chance != null) {
            qs.rollAndGive(6363, 1, (double) _chance);
        }
        return null;
    }

    

    public static class Bingo extends quests.Bingo {
        protected static final String msg_begin = "I've arranged the numbers 1 through 9 on the grid. Don't peek!<br>Let me have the 100 Infernium Ores. Too many players try to run away without paying when it becomes obvious that they're losing...<br>OK, select six numbers between 1 and 9. Choose the %choicenum% number.";
        protected static final String msg_again = "You've already chosen that number. Make your %choicenum% choice again.";
        protected static final String msg_0lines = "Wow! How unlucky can you get? Your choices are highlighted in red below. As you can see, your choices didn't make a single line! Losing this badly is actually quite rare!<br>You look so sad, I feel bad for you... Wait here... <br>.<br>.<br>.<br>Take this... I hope it will bring you better luck in the future.";
        protected static final String msg_3lines = "Excellent! As you can see, you've formed three lines! Congratulations! As promised, I'll give you some unclaimed merchandise from the warehouse. Wait here...<br>.<br>.<br>.<br>Whew, it's dusty! OK, here you go. Do you like it?";
        protected static final String msg_lose = "Oh, too bad. Your choices didn't form three lines. You should try again... Your choices are highlighted in red.";
        private static final String template_choice = "<a action=\"bypass -h Quest _386_StolenDignity choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ";
        private final QuestState _qs;

        public Bingo(final QuestState qs) {
            super("<a action=\"bypass -h Quest _386_StolenDignity choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ");
            _qs = qs;
        }

        @Override
        protected String getFinal() {
            final String result = super.getFinal();
            if (lines == 3) {
                reward(_386_StolenDignity.Rewards_Win);
            } else if (lines == 0) {
                reward(_386_StolenDignity.Rewards_Lose);
            }
            _386_StolenDignity.bingos.remove(_qs.getPlayer().getObjectId());
            return result;
        }

        private void reward(final int[][] rew) {
            final int[] r = rew[Rnd.get(rew.length)];
            _qs.giveItems(r[0], (long) r[1], false);
        }
    }
}
