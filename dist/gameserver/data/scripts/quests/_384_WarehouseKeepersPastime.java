package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class _384_WarehouseKeepersPastime extends Quest {
    private static final int Cliff = 30182;
    private static final int Baxt = 30685;
    private static final int Warehouse_Keepers_Medal = 5964;
    private static final Map<Integer, Integer> Medal_Chances = new HashMap<>();
    private static final Map<Integer, Bingo> bingos = new HashMap<>();
    private static final int[][] Rewards_Win = {{16, 1888, 1}, {32, 1887, 1}, {50, 1894, 1}, {80, 952, 1}, {89, 1890, 1}, {98, 1893, 1}, {100, 951, 1}};
    private static final int[][] Rewards_Win_Big = {{50, 883, 1}, {80, 951, 1}, {98, 852, 1}, {100, 401, 1}};
    private static final int[][] Rewards_Lose = {{50, 4041, 1}, {80, 952, 1}, {98, 1892, 1}, {100, 917, 1}};
    private static final int[][] Rewards_Lose_Big = {{50, 951, 1}, {80, 500, 1}, {98, 2437, 2}, {100, 135, 1}};

    public _384_WarehouseKeepersPastime() {
        super(false);
        addStartNpc(30182);
        addTalkId(30685);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20948, 18);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20945, 12);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20946, 15);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20947, 16);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20635, 15);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20773, 61);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20774, 60);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20760, 24);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20758, 24);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20759, 23);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20242, 22);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20281, 22);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20556, 14);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20668, 21);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20241, 22);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20286, 22);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20950, 20);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20949, 19);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20942, 9);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20943, 12);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20944, 11);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20559, 14);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20243, 21);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20282, 21);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20677, 34);
        _384_WarehouseKeepersPastime.Medal_Chances.put(20605, 15);
        for (final int id : _384_WarehouseKeepersPastime.Medal_Chances.keySet()) {
            addKillId(id);
        }
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        final long medals = st.getQuestItemsCount(5964);
        if ("30182-05.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if (("30182-08.htm".equalsIgnoreCase(event) || "30685-08.htm".equalsIgnoreCase(event)) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if (event.contains("-game") && _state == 2) {
            final boolean big_game = event.contains("-big");
            final int need_medals = big_game ? 100 : 10;
            if (medals < need_medals) {
                return event.replaceFirst("-big", "").replaceFirst("game", "09.htm");
            }
            st.takeItems(5964, (long) need_medals);
            final int char_obj_id = st.getPlayer().getObjectId();
            if (_384_WarehouseKeepersPastime.bingos.containsKey(char_obj_id)) {
                _384_WarehouseKeepersPastime.bingos.remove(char_obj_id);
            }
            final Bingo bingo = new Bingo(big_game, st);
            _384_WarehouseKeepersPastime.bingos.put(char_obj_id, bingo);
            return bingo.getDialog("");
        } else if (event.contains("choice-") && _state == 2) {
            final int char_obj_id2 = st.getPlayer().getObjectId();
            if (!_384_WarehouseKeepersPastime.bingos.containsKey(char_obj_id2)) {
                return null;
            }
            final Bingo bingo2 = _384_WarehouseKeepersPastime.bingos.get(char_obj_id2);
            return bingo2.Select(event.replaceFirst("choice-", ""));
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != 30182) {
                return "noquest";
            }
            if (st.getPlayer().getLevel() < 40) {
                st.exitCurrentQuest(true);
                return "30182-04.htm";
            }
            st.setCond(0);
            return "30182-01.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            final long medals = st.getQuestItemsCount(5964);
            if (medals >= 100L) {
                return String.valueOf(npcId) + "-06.htm";
            }
            if (medals >= 10L) {
                return String.valueOf(npcId) + "-06a.htm";
            }
            return String.valueOf(npcId) + "-06b.htm";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        final Integer chance = _384_WarehouseKeepersPastime.Medal_Chances.get(npc.getNpcId());
        if (chance != null && Rnd.chance(chance * Config.RATE_QUESTS_REWARD)) {
            qs.giveItems(5964, 1L);
            qs.playSound((qs.getQuestItemsCount(5964) == 10L) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        }
        return null;
    }

    

    public static class Bingo extends quests.Bingo {
        protected static final String msg_begin = "I've arranged 9 numbers on the panel. Don't peek! Ha ha ha!<br>Now give me your 10 medals. Some players run away when they realize that they don't stand a good chance of winning. Therefore, I prefer to hold the medals before the game starts. If you quit during game play, you'll forfeit your bet. Is that satisfactory?<br>Now, select your %choicenum% number.";
        protected static final String msg_0lines = "You are spectacularly unlucky! The red-colored numbers on the panel below are the ones you chose. As you can see, they didn't create even a single line. Did you know that it is harder not to create a single line than creating all 3 lines?<br>Usually, I don't give a reward when you don't create a single line, but since I'm feeling sorry for you, I'll be generous this time. Wait here.<br>.<br>.<br>.<br><br><br>Here, take this. I hope it will bring you better luck in the future.";
        protected static final String msg_3lines = "You've created 3 lines! The red colored numbers on the bingo panel below are the numbers you chose. Congratulations! As I promised, I'll give you an unclaimed item from my warehouse. Wait here.<br>.<br>.<br>.<br><br><br>Puff puff... it's very dusty. Here it is. Do you like it?";
        private static final String template_choice = "<a action=\"bypass -h Quest _384_WarehouseKeepersPastime choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ";
        private final boolean _BigGame;
        private final QuestState _qs;

        public Bingo(final boolean BigGame, final QuestState qs) {
            super("<a action=\"bypass -h Quest _384_WarehouseKeepersPastime choice-%n%\">%n%</a>&nbsp;&nbsp;&nbsp;&nbsp;  ");
            _BigGame = BigGame;
            _qs = qs;
        }

        @Override
        protected String getFinal() {
            final String result = super.getFinal();
            if (lines == 3) {
                reward(_BigGame ? _384_WarehouseKeepersPastime.Rewards_Win_Big : _384_WarehouseKeepersPastime.Rewards_Win);
            } else if (lines == 0) {
                reward(_BigGame ? _384_WarehouseKeepersPastime.Rewards_Lose_Big : _384_WarehouseKeepersPastime.Rewards_Lose);
            }
            _384_WarehouseKeepersPastime.bingos.remove(_qs.getPlayer().getObjectId());
            return result;
        }

        private void reward(final int[][] rew) {
            final int r = Rnd.get(100);
            for (final int[] l : rew) {
                if (r < l[0]) {
                    _qs.giveItems(l[1], (long) l[2], true);
                    return;
                }
            }
        }
    }
}
