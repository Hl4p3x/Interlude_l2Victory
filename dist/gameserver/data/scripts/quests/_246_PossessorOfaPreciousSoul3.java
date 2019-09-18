package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.List;

public class _246_PossessorOfaPreciousSoul3 extends Quest {
    private final int CARADINES_LETTER_2_PART = 7678;
    private final int RING_OF_GODDESS_WATERBINDER = 7591;
    private final int NECKLACE_OF_GODDESS_EVERGREEN = 7592;
    private final int STAFF_OF_GODDESS_RAIN_SONG = 7593;
    private final int CARADINES_LETTER = 7679;
    private final int RELIC_BOX = 7594;

    public _246_PossessorOfaPreciousSoul3() {
        super(true);
        addStartNpc(31740);
        addTalkId(31740);
        addTalkId(31741);
        addTalkId(30721);
        addKillId(21541);
        addKillId(21544);
        addKillId(25325);
        addQuestItem(RING_OF_GODDESS_WATERBINDER, NECKLACE_OF_GODDESS_EVERGREEN, STAFF_OF_GODDESS_RAIN_SONG);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        switch (event) {
            case "31740-2.htm":
                st.setCond(1);
                st.takeItems(CARADINES_LETTER_2_PART, 1L);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "31741-2.htm":
                st.setCond(2);
                st.playSound("ItemSound.quest_middle");
                break;
            case "31741-4.htm":
                st.setCond(4);
                st.playSound("ItemSound.quest_middle");
                break;
            case "31741-6.htm":
                st.takeItems(RING_OF_GODDESS_WATERBINDER, 1L);
                st.takeItems(NECKLACE_OF_GODDESS_EVERGREEN, 1L);
                st.takeItems(STAFF_OF_GODDESS_RAIN_SONG, 1L);
                st.setCond(6);
                st.giveItems(RELIC_BOX, 1L);
                st.playSound("ItemSound.quest_middle");
                break;
            case "30721-2.htm":
                st.takeItems(RELIC_BOX, 1L);
                st.giveItems(CARADINES_LETTER, 1L);
                st.unset("cond");
                st.exitCurrentQuest(false);
                break;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31740) {
            if (cond == 0) {
                if (st.getQuestItemsCount(CARADINES_LETTER_2_PART) >= 1L && st.getPlayer().isSubClassActive() && st.getPlayer().getLevel() >= 65) {
                    htmltext = "31740-1.htm";
                } else {
                    htmltext = "31740-0.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 && st.getPlayer().isSubClassActive()) {
                htmltext = "31740-2r.htm";
            }
        } else if (npcId == 31741 && st.getPlayer().isSubClassActive()) {
            if (cond == 1) {
                htmltext = "31741-1.htm";
            } else if ((cond == 2 || cond == 3) && (st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) < 1L || st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) < 1L)) {
                htmltext = "8743-2r.htm";
            } else if (cond == 3 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1L && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1L) {
                htmltext = "31741-3.htm";
            } else if (cond == 4) {
                htmltext = "31741-4.htm";
            } else if ((cond == 4 || cond == 5) && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) < 1L) {
                htmltext = "31741-4r.htm";
            } else if (cond == 5 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1L && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1L && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) == 1L) {
                htmltext = "31741-5.htm";
            } else if (cond == 6) {
                htmltext = "31741-6r.htm";
            }
        } else if (npcId == 30721 && st.getPlayer().isSubClassActive() && cond == 6 && st.getQuestItemsCount(RELIC_BOX) == 1L) {
            htmltext = "30721-1.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 2 && st.getPlayer().isSubClassActive()) {
            if (Rnd.chance(15)) {
                if (npcId == 21541 && st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 0L) {
                    st.giveItems(RING_OF_GODDESS_WATERBINDER, 1L);
                    if (st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1L && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1L) {
                        st.setCond(3);
                    }
                    st.playSound("ItemSound.quest_itemget");
                } else if (npcId == 21544 && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 0L) {
                    st.giveItems(NECKLACE_OF_GODDESS_EVERGREEN, 1L);
                    if (st.getQuestItemsCount(RING_OF_GODDESS_WATERBINDER) == 1L && st.getQuestItemsCount(NECKLACE_OF_GODDESS_EVERGREEN) == 1L) {
                        st.setCond(3);
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (cond == 4 && st.getPlayer().isSubClassActive() && npcId == 25325 && st.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) == 0L) {
            final Player player = st.getPlayer();
            final List<Player> partyMembers = st.getPartyMembers(2, Config.ALT_PARTY_DISTRIBUTION_RANGE, player);
            for (final Player partyMember : partyMembers) {
                final QuestState pqs = partyMember.getQuestState(this);
                if (pqs != null && partyMember.isSubClassActive() && pqs.getQuestItemsCount(STAFF_OF_GODDESS_RAIN_SONG) == 0L) {
                    if (pqs.getCond() != 4) {
                        continue;
                    }
                    pqs.giveItems(STAFF_OF_GODDESS_RAIN_SONG, 1L);
                    pqs.setCond(5);
                    pqs.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    }
}
