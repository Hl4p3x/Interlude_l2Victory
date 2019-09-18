package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _224_TestOfSagittarius extends Quest {
    private static final int BERNARDS_INTRODUCTION_ID = 3294;
    private static final int LETTER_OF_HAMIL3_ID = 3297;
    private static final int HUNTERS_RUNE2_ID = 3299;
    private static final int MARK_OF_SAGITTARIUS_ID = 3293;
    private static final int CRESCENT_MOON_BOW_ID = 3028;
    private static final int TALISMAN_OF_KADESH_ID = 3300;
    private static final int BLOOD_OF_LIZARDMAN_ID = 3306;
    private static final int LETTER_OF_HAMIL1_ID = 3295;
    private static final int LETTER_OF_HAMIL2_ID = 3296;
    private static final int HUNTERS_RUNE1_ID = 3298;
    private static final int TALISMAN_OF_SNAKE_ID = 3301;
    private static final int MITHRIL_CLIP_ID = 3302;
    private static final int STAKATO_CHITIN_ID = 3303;
    private static final int ST_BOWSTRING_ID = 3304;
    private static final int MANASHENS_HORN_ID = 3305;
    private static final int WOODEN_ARROW_ID = 17;
    private static final int RewardExp = 54726;
    private static final int RewardSP = 20250;

    public _224_TestOfSagittarius() {
        super(false);
        addStartNpc(30702);
        addTalkId(30514);
        addTalkId(30626);
        addTalkId(30653);
        addTalkId(30702);
        addTalkId(30717);
        addKillId(20230);
        addKillId(20232);
        addKillId(20233);
        addKillId(20234);
        addKillId(20269);
        addKillId(20270);
        addKillId(27090);
        addKillId(20551);
        addKillId(20563);
        addKillId(20577);
        addKillId(20578);
        addKillId(20579);
        addKillId(20580);
        addKillId(20581);
        addKillId(20582);
        addKillId(20079);
        addKillId(20080);
        addKillId(20081);
        addKillId(20082);
        addKillId(20084);
        addKillId(20086);
        addKillId(20089);
        addKillId(20090);
        addQuestItem(3299, 3028, 3300, 3306, 3294, 3298, 3295, 3301, 3296, 3297, 3302, 3303, 3304, 3305);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "1":
                htmltext = "30702-04.htm";
                st.setCond(1);
                st.setState(2);
                if (!st.getPlayer().getVarB("dd3")) {
                    st.giveItems(7562, 96L);
                    st.getPlayer().setVar("dd3", "1", -1L);
                }
                st.playSound("ItemSound.quest_accept");
                st.giveItems(3294, 1L);
                break;
            case "30626_1":
                htmltext = "30626-02.htm";
                break;
            case "30626_2":
                htmltext = "30626-03.htm";
                st.takeItems(3294, st.getQuestItemsCount(3294));
                st.giveItems(3295, 1L);
                st.setCond(2);
                break;
            case "30626_3":
                htmltext = "30626-06.htm";
                break;
            case "30626_4":
                htmltext = "30626-07.htm";
                st.takeItems(3298, st.getQuestItemsCount(3298));
                st.giveItems(3296, 1L);
                st.setCond(5);
                break;
            case "30653_1":
                htmltext = "30653-02.htm";
                st.takeItems(3295, st.getQuestItemsCount(3295));
                st.setCond(3);
                break;
            case "30514_1":
                htmltext = "30514-02.htm";
                st.takeItems(3296, st.getQuestItemsCount(3296));
                st.setCond(6);
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(3293) > 0L) {
            st.exitCurrentQuest(true);
            return "completed";
        }
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        if (id == 1) {
            st.setState(2);
            st.setCond(0);
            st.set("id", "0");
        }
        if (npcId == 30702 && st.getCond() == 0) {
            if (st.getPlayer().getClassId().getId() == 7 || st.getPlayer().getClassId().getId() == 22 || st.getPlayer().getClassId().getId() == 35) {
                if (st.getPlayer().getLevel() >= 39) {
                    htmltext = "30702-03.htm";
                } else {
                    htmltext = "30702-01.htm";
                    st.exitCurrentQuest(true);
                }
            } else {
                htmltext = "30702-02.htm";
                st.exitCurrentQuest(true);
            }
        } else if (npcId == 30702 && st.getCond() == 1 && st.getQuestItemsCount(3294) > 0L) {
            htmltext = "30702-05.htm";
        } else if (npcId == 30626 && st.getCond() == 1 && st.getQuestItemsCount(3294) > 0L) {
            htmltext = "30626-01.htm";
        } else if (npcId == 30626 && st.getCond() == 2 && st.getQuestItemsCount(3295) > 0L) {
            htmltext = "30626-04.htm";
        } else if (npcId == 30626 && st.getCond() == 4 && st.getQuestItemsCount(3298) == 10L) {
            htmltext = "30626-05.htm";
        } else if (npcId == 30626 && st.getCond() == 5 && st.getQuestItemsCount(3296) > 0L) {
            htmltext = "30626-08.htm";
        } else if (npcId == 30626 && st.getCond() == 8) {
            htmltext = "30626-09.htm";
            st.giveItems(3297, 1L);
            st.setCond(9);
        } else if (npcId == 30626 && st.getCond() == 9 && st.getQuestItemsCount(3297) > 0L) {
            htmltext = "30626-10.htm";
        } else if (npcId == 30626 && st.getCond() == 12 && st.getQuestItemsCount(3028) > 0L) {
            htmltext = "30626-11.htm";
            st.setCond(13);
        } else if (npcId == 30626 && st.getCond() == 13) {
            htmltext = "30626-12.htm";
        } else if (npcId == 30626 && st.getCond() == 14 && st.getQuestItemsCount(3300) > 0L) {
            htmltext = "30626-13.htm";
            st.takeItems(3028, -1L);
            st.takeItems(3300, -1L);
            st.takeItems(3306, -1L);
            st.giveItems(3293, 1L);
            if (!st.getPlayer().getVarB("prof2.3")) {
                st.addExpAndSp(54726L, 20250L);
                st.getPlayer().setVar("prof2.3", "1", -1L);
            }
            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitCurrentQuest(false);
        } else if (npcId == 30653 && st.getCond() == 2 && st.getQuestItemsCount(3295) > 0L) {
            htmltext = "30653-01.htm";
        } else if (npcId == 30653 && st.getCond() == 3) {
            htmltext = "30653-03.htm";
        } else if (npcId == 30514 && st.getCond() == 5 && st.getQuestItemsCount(3296) > 0L) {
            htmltext = "30514-01.htm";
        } else if (npcId == 30514 && st.getCond() == 6) {
            htmltext = "30514-03.htm";
        } else if (npcId == 30514 && st.getCond() == 7 && st.getQuestItemsCount(3301) > 0L) {
            htmltext = "30514-04.htm";
            st.takeItems(3301, st.getQuestItemsCount(3301));
            st.setCond(8);
        } else if (npcId == 30514 && st.getCond() == 8) {
            htmltext = "30514-05.htm";
        } else if (npcId == 30717 && st.getCond() == 9 && st.getQuestItemsCount(3297) > 0L) {
            htmltext = "30717-01.htm";
            st.takeItems(3297, st.getQuestItemsCount(3297));
            st.setCond(10);
        } else if (npcId == 30717 && st.getCond() == 10) {
            htmltext = "30717-03.htm";
        } else if (npcId == 30717 && st.getCond() == 12) {
            htmltext = "30717-04.htm";
        } else if (npcId == 30717 && st.getCond() == 11 && st.getQuestItemsCount(3303) > 0L && st.getQuestItemsCount(3302) > 0L && st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3305) > 0L) {
            htmltext = "30717-02.htm";
            st.takeItems(3302, st.getQuestItemsCount(3302));
            st.takeItems(3303, st.getQuestItemsCount(3303));
            st.takeItems(3304, st.getQuestItemsCount(3304));
            st.takeItems(3305, st.getQuestItemsCount(3305));
            st.giveItems(3028, 1L);
            st.giveItems(17, 10L);
            st.setCond(12);
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (npcId == 20079 || npcId == 20080 || npcId == 20081 || npcId == 20084 || npcId == 20086 || npcId == 20089 || npcId == 20090) {
            if (st.getCond() == 3 && st.getQuestItemsCount(3298) < 10L && Rnd.chance(50)) {
                st.giveItems(3298, 1L);
                if (st.getQuestItemsCount(3298) == 10L) {
                    st.setCond(4);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20269 || npcId == 20270) {
            if (st.getCond() == 6 && st.getQuestItemsCount(3299) < 10L && Rnd.chance(50)) {
                st.giveItems(3299, 1L);
                if (st.getQuestItemsCount(3299) == 10L) {
                    st.takeItems(3299, 10L);
                    st.giveItems(3301, 1L);
                    st.setCond(7);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20230 || npcId == 20232 || npcId == 20234) {
            if (st.getCond() == 10 && st.getQuestItemsCount(3303) == 0L && Rnd.chance(10)) {
                st.giveItems(3303, 1L);
                if (st.getQuestItemsCount(3302) > 0L && st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3305) > 0L) {
                    st.setCond(11);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20563) {
            if (st.getCond() == 10 && st.getQuestItemsCount(3305) == 0L && Rnd.chance(10)) {
                st.giveItems(3305, 1L);
                if (st.getQuestItemsCount(3302) > 0L && st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3303) > 0L) {
                    st.setCond(11);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20233) {
            if (st.getCond() == 10 && st.getQuestItemsCount(3304) == 0L && Rnd.chance(10)) {
                st.giveItems(3304, 1L);
                if (st.getQuestItemsCount(3302) > 0L && st.getQuestItemsCount(3305) > 0L && st.getQuestItemsCount(3303) > 0L) {
                    st.setCond(11);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20551) {
            if (st.getCond() == 10 && st.getQuestItemsCount(3302) == 0L && Rnd.chance(10)) {
                st.giveItems(3302, 1L);
                if (st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3305) > 0L && st.getQuestItemsCount(3303) > 0L) {
                    st.setCond(11);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20551) {
            if (st.getCond() == 10 && st.getQuestItemsCount(3302) == 0L && Rnd.chance(10)) {
                if (st.getQuestItemsCount(3304) > 0L && st.getQuestItemsCount(3305) > 0L && st.getQuestItemsCount(3303) > 0L) {
                    st.giveItems(3302, 1L);
                    st.setCond(11);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    st.giveItems(3302, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 20577 || npcId == 20578 || npcId == 20579 || npcId == 20580 || npcId == 20581 || npcId == 20582) {
            if (st.getCond() == 13) {
                if (Rnd.chance((double) ((st.getQuestItemsCount(3306) - 120L) * 5L))) {
                    st.addSpawn(27090);
                    st.takeItems(3306, st.getQuestItemsCount(3306));
                    st.playSound("Itemsound.quest_before_battle");
                } else {
                    st.giveItems(3306, 1L);
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (npcId == 27090 && st.getCond() == 13 && st.getQuestItemsCount(3300) == 0L) {
            if (st.getItemEquipped(7) == 3028) {
                st.giveItems(3300, 1L);
                st.setCond(14);
                st.playSound("ItemSound.quest_middle");
            } else {
                st.addSpawn(27090);
            }
        }
        return null;
    }
}
