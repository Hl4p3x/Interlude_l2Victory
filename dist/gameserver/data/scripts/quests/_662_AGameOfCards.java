package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class _662_AGameOfCards extends Quest {
    private static final int KLUMP = 30845;
    private static final int[] mobs = {20677, 21109, 21112, 21116, 21114, 21004, 21002, 21006, 21008, 21010, 18001, 20672, 20673, 20674, 20955, 20962, 20961, 20959, 20958, 20966, 20965, 20968, 20973, 20972, 21278, 21279, 21280, 21286, 21287, 21288, 21520, 21526, 21530, 21535, 21508, 21510, 21513, 21515};
    private static final int RED_GEM = 8765;
    private static final int Enchant_Weapon_S = 959;
    private static final int Enchant_Weapon_A = 729;
    private static final int Enchant_Weapon_B = 947;
    private static final int Enchant_Weapon_C = 951;
    private static final int Enchant_Weapon_D = 955;
    private static final int Enchant_Armor_D = 956;
    private static final int ZIGGOS_GEMSTONE = 8868;
    private static final int drop_chance = 35;
    private static final Map<Integer, CardGame> Games = new ConcurrentHashMap<>();

    public _662_AGameOfCards() {
        super(true);
        addStartNpc(30845);
        addKillId(_662_AGameOfCards.mobs);
        addQuestItem(8765);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("30845_02.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("30845_07.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        } else {
            if ("30845_03.htm".equalsIgnoreCase(event) && _state == 2 && st.getQuestItemsCount(8765) >= 50L) {
                return "30845_04.htm";
            }
            if ("30845_10.htm".equalsIgnoreCase(event) && _state == 2) {
                if (st.getQuestItemsCount(8765) < 50L) {
                    return "30845_10a.htm";
                }
                st.takeItems(8765, 50L);
                final int player_id = st.getPlayer().getObjectId();
                if (_662_AGameOfCards.Games.containsKey(player_id)) {
                    _662_AGameOfCards.Games.remove(player_id);
                }
                _662_AGameOfCards.Games.put(player_id, new CardGame(player_id));
            } else if ("play".equalsIgnoreCase(event) && _state == 2) {
                final int player_id = st.getPlayer().getObjectId();
                if (!_662_AGameOfCards.Games.containsKey(player_id)) {
                    return null;
                }
                return _662_AGameOfCards.Games.get(player_id).playField();
            } else if (event.startsWith("card") && _state == 2) {
                final int player_id = st.getPlayer().getObjectId();
                if (!_662_AGameOfCards.Games.containsKey(player_id)) {
                    return null;
                }
                try {
                    final int cardn = Integer.valueOf(event.replaceAll("card", ""));
                    return _662_AGameOfCards.Games.get(player_id).next(cardn, st);
                } catch (Exception E) {
                    return null;
                }
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (npc.getNpcId() != 30845) {
            return "noquest";
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getLevel() < 61) {
                st.exitCurrentQuest(true);
                return "30845_00.htm";
            }
            st.setCond(0);
            return "30845_01.htm";
        } else {
            if (_state == 2) {
                return (st.getQuestItemsCount(8765) < 50L) ? "30845_03.htm" : "30845_04.htm";
            }
            return "noquest";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() == 2) {
            qs.rollAndGive(8765, 1, 35.0);
        }
        return null;
    }

    

    private static class CardGame {
        private static final String[] card_chars = {"A", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        private static final String html_header = "<html><body>";
        private static final String html_footer = "</body></html>";
        private static final String table_header = "<table border=\"1\" cellpadding=\"3\"><tr>";
        private static final String table_footer = "</tr></table><br><br>";
        private static final String td_begin = "<center><td width=\"50\" align=\"center\"><br><br><br> ";
        private static final String td_end = " <br><br><br><br></td></center>";

        private final String[] cards;
        private final int player_id;

        public CardGame(final int _player_id) {
            cards = new String[5];
            player_id = _player_id;
            for (int i = 0; i < cards.length; ++i) {
                cards[i] = "<a action=\"bypass -h Quest _662_AGameOfCards card" + i + "\">?</a>";
            }
        }

        public String next(final int cardn, final QuestState st) {
            if (cardn >= cards.length || !cards[cardn].startsWith("<a")) {
                return null;
            }
            cards[cardn] = CardGame.card_chars[Rnd.get(CardGame.card_chars.length)];
            for (final String card : cards) {
                if (card.startsWith("<a")) {
                    return playField();
                }
            }
            return finish(st);
        }

        private String finish(final QuestState st) {
            StringBuilder result = new StringBuilder("<html><body><table border=\"1\" cellpadding=\"3\"><tr>");
            final Map<String, Integer> matches = new HashMap<>();
            for (final String card : cards) {
                int count = matches.containsKey(card) ? matches.remove(card) : 0;
                ++count;
                matches.put(card, count);
            }
            for (final String card : cards) {
                if (matches.get(card) < 2) {
                    matches.remove(card);
                }
            }
            final String[] smatches = matches.keySet().toArray(new String[matches.size()]);
            final Integer[] cmatches = matches.values().toArray(new Integer[matches.size()]);
            String txt = "Hmmm...? This is... No pair? Tough luck, my friend! Want to try again? Perhaps your luck will take a turn for the better...";
            if (cmatches.length == 1) {
                switch (cmatches[0]) {
                    case 5:
                        txt = "Hmmm...? This is... Five of a kind!!!! What luck! The goddess of victory must be with you! Here is your prize! Well earned, well played!";
                        st.giveItems(8868, 43L);
                        st.giveItems(959, 3L);
                        st.giveItems(729, 1L);
                        break;
                    case 4:
                        txt = "Hmmm...? This is... Four of a kind! Well done, my young friend! That sort of hand doesn't come up very often, that's for sure. Here's your prize.";
                        st.giveItems(959, 2L);
                        st.giveItems(951, 2L);
                        break;
                    case 3:
                        txt = "Hmmm...? This is... Three of a kind? Very good, you are very lucky. Here's your prize.";
                        st.giveItems(951, 2L);
                        break;
                    case 2:
                        txt = "Hmmm...? This is... One pair? You got lucky this time, but I wonder if it'll last. Here's your prize.";
                        st.giveItems(956, 2L);
                        break;
                }
            } else if (cmatches.length == 2) {
                if (cmatches[0] == 3 || cmatches[1] == 3) {
                    txt = "Hmmm...? This is... A full house? Excellent! you're better than I thought. Here's your prize.";
                    st.giveItems(729, 1L);
                    st.giveItems(947, 2L);
                    st.giveItems(955, 1L);
                } else {
                    txt = "Hmmm...? This is... Two pairs? You got lucky this time, but I wonder if it'll last. Here's your prize.";
                    st.giveItems(951, 1L);
                }
            }
            for (final String card2 : cards) {
                if (smatches.length > 0 && smatches[0].equalsIgnoreCase(card2)) {
                    result.append("<center><td width=\"50\" align=\"center\"><br><br><br> <font color=\"55FD44\">").append(card2).append("</font>").append(" <br><br><br><br></td></center>");
                } else if (smatches.length == 2 && smatches[1].equalsIgnoreCase(card2)) {
                    result.append("<center><td width=\"50\" align=\"center\"><br><br><br> <font color=\"FE6666\">").append(card2).append("</font>").append(" <br><br><br><br></td></center>");
                } else {
                    result.append("<center><td width=\"50\" align=\"center\"><br><br><br> ").append(card2).append(" <br><br><br><br></td></center>");
                }
            }
            result.append("</tr></table><br><br>").append(txt);
            if (st.getQuestItemsCount(8765) >= 50L) {
                result.append("<br><br><a action=\"bypass -h Quest _662_AGameOfCards 30845_10.htm\">Play Again!</a>");
            }
            result.append("</body></html>");
            _662_AGameOfCards.Games.remove(player_id);
            return result.toString();
        }

        public String playField() {
            StringBuilder result = new StringBuilder("<html><body><table border=\"1\" cellpadding=\"3\"><tr>");
            for (final String card : cards) {
                result.append("<center><td width=\"50\" align=\"center\"><br><br><br> ").append(card).append(" <br><br><br><br></td></center>");
            }
            result.append("</tr></table><br><br>Check your next card.</body></html>");
            return result.toString();
        }
    }
}
