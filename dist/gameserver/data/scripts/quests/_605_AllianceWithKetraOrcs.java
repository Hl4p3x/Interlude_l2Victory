package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _605_AllianceWithKetraOrcs extends Quest {
    private static final int[] KETRA_NPC_LIST = new int[19];
    private static final int MARK_OF_KETRA_ALLIANCE1 = 7211;
    private static final int MARK_OF_KETRA_ALLIANCE2 = 7212;
    private static final int MARK_OF_KETRA_ALLIANCE3 = 7213;
    private static final int MARK_OF_KETRA_ALLIANCE4 = 7214;
    private static final int MARK_OF_KETRA_ALLIANCE5 = 7215;
    private static final int VB_SOLDIER = 7216;
    private static final int VB_CAPTAIN = 7217;
    private static final int VB_GENERAL = 7218;
    private static final int TOTEM_OF_VALOR = 7219;
    private static final int TOTEM_OF_WISDOM = 7220;
    private static final int RECRUIT = 21350;
    private static final int FOOTMAN = 21351;
    private static final int SCOUT = 21353;
    private static final int HUNTER = 21354;
    private static final int SHAMAN = 21355;
    private static final int PRIEST = 21357;
    private static final int WARRIOR = 21358;
    private static final int MEDIUM = 21360;
    private static final int MAGUS = 21361;
    private static final int OFFICIER = 21362;
    private static final int COMMANDER = 21369;
    private static final int ELITE_GUARD = 21370;
    private static final int GREAT_MAGUS = 21365;
    private static final int GENERAL = 21366;
    private static final int GREAT_SEER = 21368;
    private static final int VARKA_PROPHET = 21373;
    private static final int DISCIPLE_OF_PROPHET = 21375;
    private static final int PROPHET_GUARDS = 21374;
    private static final int HEAD_MAGUS = 21371;
    private static final int HEAD_GUARDS = 21372;
    private static final int Wahkan = 31371;

    public _605_AllianceWithKetraOrcs() {
        super(true);
        addStartNpc(31371);
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[0] = 21324;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[1] = 21325;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[2] = 21327;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[3] = 21328;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[4] = 21329;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[5] = 21331;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[6] = 21332;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[7] = 21334;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[8] = 21335;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[9] = 21336;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[10] = 21338;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[11] = 21339;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[12] = 21340;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[13] = 21342;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[14] = 21343;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[15] = 21344;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[16] = 21345;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[17] = 21346;
        _605_AllianceWithKetraOrcs.KETRA_NPC_LIST[18] = 21347;
        addKillId(_605_AllianceWithKetraOrcs.KETRA_NPC_LIST);
        addKillId(21350);
        addKillId(21351);
        addKillId(21353);
        addKillId(21354);
        addKillId(21355);
        addKillId(21357);
        addKillId(21358);
        addKillId(21360);
        addKillId(21361);
        addKillId(21362);
        addKillId(21369);
        addKillId(21370);
        addKillId(21365);
        addKillId(21366);
        addKillId(21368);
        addKillId(21373);
        addKillId(21375);
        addKillId(21374);
        addKillId(21371);
        addKillId(21372);
        addQuestItem(7216);
        addQuestItem(7217);
        addQuestItem(7218);
    }

    private static void takeAllMarks(final QuestState st) {
        st.takeItems(7211, -1L);
        st.takeItems(7212, -1L);
        st.takeItems(7213, -1L);
        st.takeItems(7214, -1L);
        st.takeItems(7215, -1L);
    }

    private static boolean CheckNextLevel(final QuestState st, final int soilder_count, final int capitan_count, final int general_count, final int other_item, final boolean take) {
        if (soilder_count > 0 && st.getQuestItemsCount(7216) < soilder_count) {
            return false;
        }
        if (capitan_count > 0 && st.getQuestItemsCount(7217) < capitan_count) {
            return false;
        }
        if (general_count > 0 && st.getQuestItemsCount(7218) < general_count) {
            return false;
        }
        if (other_item > 0 && st.getQuestItemsCount(other_item) < 1L) {
            return false;
        }
        if (take) {
            if (soilder_count > 0) {
                st.takeItems(7216, (long) soilder_count);
            }
            if (capitan_count > 0) {
                st.takeItems(7217, (long) capitan_count);
            }
            if (general_count > 0) {
                st.takeItems(7218, (long) general_count);
            }
            if (other_item > 0) {
                st.takeItems(other_item, 1L);
            }
            takeAllMarks(st);
        }
        return true;
    }

    public boolean isKetraNpc(final int npc) {
        for (final int i : _605_AllianceWithKetraOrcs.KETRA_NPC_LIST) {
            if (npc == i) {
                return true;
            }
        }
        return false;
    }

    

    @Override
    public void onAbort(final QuestState st) {
        takeAllMarks(st);
        st.setCond(0);
        st.getPlayer().updateKetraVarka();
        st.playSound("ItemSound.quest_middle");
    }

    private void checkMarks(final QuestState st) {
        if (st.getCond() == 0) {
            return;
        }
        if (st.getQuestItemsCount(7215) > 0L) {
            st.setCond(6);
        } else if (st.getQuestItemsCount(7214) > 0L) {
            st.setCond(5);
        } else if (st.getQuestItemsCount(7213) > 0L) {
            st.setCond(4);
        } else if (st.getQuestItemsCount(7212) > 0L) {
            st.setCond(3);
        } else if (st.getQuestItemsCount(7211) > 0L) {
            st.setCond(2);
        } else {
            st.setCond(1);
        }
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("first-2.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            return event;
        }
        checkMarks(st);
        final int cond = st.getCond();
        if ("first-have-2.htm".equalsIgnoreCase(event) && cond == 1 && CheckNextLevel(st, 100, 0, 0, 0, true)) {
            st.giveItems(7211, 1L);
            st.setCond(2);
            st.getPlayer().updateKetraVarka();
            st.playSound("ItemSound.quest_middle");
        } else if ("second-have-2.htm".equalsIgnoreCase(event) && cond == 2 && CheckNextLevel(st, 200, 100, 0, 0, true)) {
            st.giveItems(7212, 1L);
            st.setCond(3);
            st.getPlayer().updateKetraVarka();
            st.playSound("ItemSound.quest_middle");
        } else if ("third-have-2.htm".equalsIgnoreCase(event) && cond == 3 && CheckNextLevel(st, 300, 200, 100, 0, true)) {
            st.giveItems(7213, 1L);
            st.setCond(4);
            st.getPlayer().updateKetraVarka();
            st.playSound("ItemSound.quest_middle");
        } else if ("fourth-have-2.htm".equalsIgnoreCase(event) && cond == 4 && CheckNextLevel(st, 300, 300, 200, 7219, true)) {
            st.giveItems(7214, 1L);
            st.setCond(5);
            st.getPlayer().updateKetraVarka();
            st.playSound("ItemSound.quest_middle");
        } else if ("fifth-have-2.htm".equalsIgnoreCase(event) && cond == 5 && CheckNextLevel(st, 400, 400, 200, 7220, true)) {
            st.giveItems(7215, 1L);
            st.setCond(6);
            st.getPlayer().updateKetraVarka();
            st.playSound("ItemSound.quest_middle");
        } else if ("quit-2.htm".equalsIgnoreCase(event)) {
            takeAllMarks(st);
            st.setCond(0);
            st.getPlayer().updateKetraVarka();
            st.playSound("ItemSound.quest_middle");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getPlayer().getVarka() > 0) {
            st.exitCurrentQuest(true);
            return "isvarka.htm";
        }
        checkMarks(st);
        if (st.getState() == 1) {
            st.setCond(0);
        }
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 31371) {
            if (cond == 0) {
                if (st.getPlayer().getLevel() < 74) {
                    st.exitCurrentQuest(true);
                    return "no-level.htm";
                }
                return "first.htm";
            } else {
                if (cond == 1) {
                    return CheckNextLevel(st, 100, 0, 0, 0, false) ? "first-have.htm" : "first-havenot.htm";
                }
                if (cond == 2) {
                    return CheckNextLevel(st, 200, 100, 0, 0, false) ? "second-have.htm" : "second.htm";
                }
                if (cond == 3) {
                    return CheckNextLevel(st, 300, 200, 100, 0, false) ? "third-have.htm" : "third.htm";
                }
                if (cond == 4) {
                    return CheckNextLevel(st, 300, 300, 200, 7219, false) ? "fourth-have.htm" : "fourth.htm";
                }
                if (cond == 5) {
                    return CheckNextLevel(st, 400, 400, 200, 7220, false) ? "fifth-have.htm" : "fifth.htm";
                }
                if (cond == 6) {
                    return "high.htm";
                }
            }
        }
        return "noquest";
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if (isKetraNpc(npcId)) {
            if (st.getQuestItemsCount(7215) > 0L) {
                takeAllMarks(st);
                st.giveItems(7214, 1L);
                st.getPlayer().updateKetraVarka();
            } else if (st.getQuestItemsCount(7214) > 0L) {
                takeAllMarks(st);
                st.giveItems(7213, 1L);
                st.getPlayer().updateKetraVarka();
            } else if (st.getQuestItemsCount(7213) > 0L) {
                takeAllMarks(st);
                st.giveItems(7212, 1L);
                st.getPlayer().updateKetraVarka();
            } else if (st.getQuestItemsCount(7212) > 0L) {
                takeAllMarks(st);
                st.giveItems(7211, 1L);
                st.getPlayer().updateKetraVarka();
            } else if (st.getQuestItemsCount(7211) > 0L) {
                takeAllMarks(st);
                st.getPlayer().updateKetraVarka();
            } else if (st.getPlayer().getKetra() > 0) {
                st.getPlayer().updateKetraVarka();
                st.exitCurrentQuest(true);
                return "quit-2.htm";
            }
        }
        if (st.getQuestItemsCount(7215) > 0L) {
            return null;
        }
        final int cond = st.getCond();
        if (npcId == 21350 || npcId == 21351 || npcId == 21353 || npcId == 21354 || npcId == 21355) {
            if (cond > 0) {
                st.rollAndGive(7216, 1, 60.0);
            }
        } else if (npcId == 21357 || npcId == 21358 || npcId == 21360 || npcId == 21361 || npcId == 21362 || npcId == 21369 || npcId == 21370) {
            if (cond > 1) {
                st.rollAndGive(7217, 1, 70.0);
            }
        } else if ((npcId == 21365 || npcId == 21366 || npcId == 21368 || npcId == 21373 || npcId == 21375 || npcId == 21374 || npcId == 21371 || npcId == 21372) && cond > 2) {
            st.rollAndGive(7218, 1, 80.0);
        }
        return null;
    }
}
