package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _374_WhisperOfDreams1 extends Quest {
    private static final int seer_manakia = 30515;
    private static final int torai = 30557;
    private static final int cave_beast = 20620;
    private static final int death_wave = 20621;
    private static final int cave_beast_tooth = 5884;
    private static final int death_wave_light = 5885;
    private static final int sealed_mysterious_stone = 5886;
    private static final int mysterious_stone = 5887;

    public _374_WhisperOfDreams1() {
        super(true);
        addStartNpc(30515);
        addTalkId(30515, 30557);
        addKillId(20620, 20621);
        addQuestItem(5884, 5885, 5886, 5887);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int npcId = npc.getNpcId();
        if (npcId == 30515) {
            if ("quest_accept".equalsIgnoreCase(event)) {
                htmltext = "seer_manakia_q0374_03.htm";
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
            } else if ("reply_1".equalsIgnoreCase(event)) {
                htmltext = "seer_manakia_q0374_06.htm";
            } else if ("reply_2".equalsIgnoreCase(event)) {
                htmltext = "seer_manakia_q0374_07.htm";
                st.exitCurrentQuest(true);
                st.playSound("ItemSound.quest_finish");
            } else if ("reply_3".equalsIgnoreCase(event) && st.getQuestItemsCount(5884) >= 65L && st.getQuestItemsCount(5885) >= 65L) {
                st.giveItems(5486, 3L);
                st.giveItems(57, 15886L, true);
                st.takeItems(5884, -1L);
                st.takeItems(5885, -1L);
                htmltext = "seer_manakia_q0374_10.htm";
            } else if ("reply_4".equalsIgnoreCase(event) && st.getQuestItemsCount(5884) >= 65L && st.getQuestItemsCount(5885) >= 65L) {
                st.giveItems(5487, 2L);
                st.giveItems(57, 28458L, true);
                st.takeItems(5884, -1L);
                st.takeItems(5885, -1L);
                htmltext = "seer_manakia_q0374_10.htm";
            } else if ("reply_5".equalsIgnoreCase(event) && st.getQuestItemsCount(5884) >= 65L && st.getQuestItemsCount(5885) >= 65L) {
                st.giveItems(5488, 2L);
                st.giveItems(57, 28458L, true);
                st.takeItems(5884, -1L);
                st.takeItems(5885, -1L);
                htmltext = "seer_manakia_q0374_10.htm";
            } else if ("reply_6".equalsIgnoreCase(event) && st.getQuestItemsCount(5884) >= 65L && st.getQuestItemsCount(5885) >= 65L) {
                st.giveItems(5485, 4L);
                st.giveItems(57, 28458L, true);
                st.takeItems(5884, -1L);
                st.takeItems(5885, -1L);
                htmltext = "seer_manakia_q0374_10.htm";
            } else if ("reply_7".equalsIgnoreCase(event) && st.getQuestItemsCount(5884) >= 65L && st.getQuestItemsCount(5885) >= 65L) {
                st.giveItems(5489, 6L);
                st.giveItems(57, 28458L, true);
                st.takeItems(5884, -1L);
                st.takeItems(5885, -1L);
                htmltext = "seer_manakia_q0374_10.htm";
            }
        }
        if (npcId == 30557 && "reply_1".equalsIgnoreCase(event) && st.getQuestItemsCount(5886) > 0L) {
            htmltext = "torai_q0374_02.htm";
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
            st.takeItems(5886, -1L);
            st.giveItems(5887, 1L);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "no-quest";
        final int npcId = npc.getNpcId();
        final int state = st.getState();
        final int cond = st.getCond();
        switch (state) {
            case 1: {
                if (npcId != 30515) {
                    break;
                }
                if (st.getPlayer().getLevel() >= 56) {
                    htmltext = "seer_manakia_q0374_01.htm";
                    break;
                }
                st.exitCurrentQuest(true);
                htmltext = "seer_manakia_q0374_02.htm";
                break;
            }
            case 2: {
                if (npcId == 30515) {
                    if (((st.getQuestItemsCount(5884) <= 65L && st.getQuestItemsCount(5885) < 65L) || (st.getQuestItemsCount(5884) < 65L && st.getQuestItemsCount(5885) <= 65L)) && st.getQuestItemsCount(5886) == 0L) {
                        htmltext = "seer_manakia_q0374_04.htm";
                    } else if (st.getQuestItemsCount(5884) >= 65L && st.getQuestItemsCount(5885) >= 65L && st.getQuestItemsCount(5886) == 0L) {
                        htmltext = "seer_manakia_q0374_05.htm";
                    } else if (cond == 1 && st.getQuestItemsCount(5886) > 0L) {
                        htmltext = "seer_manakia_q0374_08.htm";
                        st.setCond(2);
                        st.playSound("ItemSound.quest_middle");
                    } else if (cond == 2 && st.getQuestItemsCount(5886) > 0L) {
                        htmltext = "seer_manakia_q0374_09.htm";
                        st.playSound("ItemSound.quest_middle");
                    }
                }
                if (npcId != 30557) {
                    break;
                }
                if (cond == 2 && st.getQuestItemsCount(5886) > 0L) {
                    htmltext = "torai_q0374_01.htm";
                }
                if (cond == 3 && st.getQuestItemsCount(5886) > 0L) {
                    htmltext = "torai_q0374_03.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (Rnd.chance(1) && st.getState() == 2 && st.getCond() != 3 && st.getQuestItemsCount(5886) < 1L) {
            st.rollAndGive(5886, 1, 100.0);
            st.playSound("ItemSound.quest_middle");
        } else if (npcId == 20620 && st.getState() == 2 && Rnd.chance(60) && st.getQuestItemsCount(5884) < 65L) {
            st.rollAndGive(5884, 1, 100.0);
            st.playSound("ItemSound.quest_middle");
        } else if (npcId == 20621 && st.getState() == 2 && Rnd.chance(60) && st.getQuestItemsCount(5885) < 65L) {
            st.rollAndGive(5885, 1, 100.0);
            st.playSound("ItemSound.quest_middle");
        }
        return null;
    }

    
}
