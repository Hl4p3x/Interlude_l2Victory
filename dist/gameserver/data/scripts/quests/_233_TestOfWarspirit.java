package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.ArrayList;
import java.util.List;

public class _233_TestOfWarspirit extends Quest {
    private static final int Somak = 30510;
    private static final int Vivyan = 30030;
    private static final int Sarien = 30436;
    private static final int Racoy = 30507;
    private static final int Manakia = 30515;
    private static final int Orim = 30630;
    private static final int Ancestor_Martankus = 30649;
    private static final int Pekiron = 30682;
    private static final int Porta = 20213;
    private static final int Excuro = 20214;
    private static final int Mordeo = 20215;
    private static final int Noble_Ant = 20089;
    private static final int Noble_Ant_Leader = 20090;
    private static final int Leto_Lizardman_Shaman = 20581;
    private static final int Leto_Lizardman_Overlord = 20582;
    private static final int Medusa = 20158;
    private static final int Stenoa_Gorgon_Queen = 27108;
    private static final int Tamlin_Orc = 20601;
    private static final int Tamlin_Orc_Archer = 20602;
    private static final int Dimensional_Diamond = 7562;
    private static final int MARK_OF_WARSPIRIT = 2879;
    private static final int VENDETTA_TOTEM = 2880;
    private static final int TAMLIN_ORC_HEAD = 2881;
    private static final int WARSPIRIT_TOTEM = 2882;
    private static final int ORIMS_CONTRACT = 2883;
    private static final int PORTAS_EYE = 2884;
    private static final int EXCUROS_SCALE = 2885;
    private static final int MORDEOS_TALON = 2886;
    private static final int BRAKIS_REMAINS1 = 2887;
    private static final int PEKIRONS_TOTEM = 2888;
    private static final int TONARS_REMAINS1 = 2894;
    private static final int MANAKIAS_TOTEM = 2895;
    private static final int HERMODTS_SKULL = 2896;
    private static final int HERMODTS_REMAINS1 = 2901;
    private static final int RACOYS_TOTEM = 2902;
    private static final int VIVIANTES_LETTER = 2903;
    private static final int INSECT_DIAGRAM_BOOK = 2904;
    private static final int KIRUNAS_REMAINS1 = 2910;
    private static final int BRAKIS_REMAINS2 = 2911;
    private static final int TONARS_REMAINS2 = 2912;
    private static final int HERMODTS_REMAINS2 = 2913;
    private static final int KIRUNAS_REMAINS2 = 2914;
    private static int TONARS_SKULL = 2889;
    private static int TONARS_RIB_BONE = 2890;
    private static int TONARS_SPINE = 2891;
    private static int TONARS_ARM_BONE = 2892;
    private static int TONARS_THIGH_BONE = 2893;
    private static int HERMODTS_RIB_BONE = 2897;
    private static int HERMODTS_SPINE = 2898;
    private static int HERMODTS_ARM_BONE = 2899;
    private static int HERMODTS_THIGH_BONE = 2900;
    private static int KIRUNAS_SKULL = 2905;
    private static int KIRUNAS_RIB_BONE = 2906;
    private static int KIRUNAS_SPINE = 2907;
    private static int KIRUNAS_ARM_BONE = 2908;
    private static int KIRUNAS_THIGH_BONE = 2909;
    private static int[] Noble_Ant_Drops = {KIRUNAS_THIGH_BONE, KIRUNAS_ARM_BONE, KIRUNAS_SPINE, KIRUNAS_RIB_BONE, KIRUNAS_SKULL};
    private static int[] Leto_Lizardman_Drops = {TONARS_SKULL, TONARS_RIB_BONE, TONARS_SPINE, TONARS_ARM_BONE, TONARS_THIGH_BONE};
    private static int[] Medusa_Drops = {HERMODTS_RIB_BONE, HERMODTS_SPINE, HERMODTS_THIGH_BONE, HERMODTS_ARM_BONE};

    public _233_TestOfWarspirit() {
        super(false);
        addStartNpc(Somak);
        addTalkId(Vivyan);
        addTalkId(Sarien);
        addTalkId(Racoy);
        addTalkId(Manakia);
        addTalkId(Orim);
        addTalkId(Ancestor_Martankus);
        addTalkId(Pekiron);
        addKillId(Porta);
        addKillId(Excuro);
        addKillId(Mordeo);
        addKillId(Noble_Ant);
        addKillId(Noble_Ant_Leader);
        addKillId(Leto_Lizardman_Shaman);
        addKillId(Leto_Lizardman_Overlord);
        addKillId(Medusa);
        addKillId(Stenoa_Gorgon_Queen);
        addKillId(Tamlin_Orc);
        addKillId(Tamlin_Orc_Archer);
        addQuestItem(VENDETTA_TOTEM);
        addQuestItem(TAMLIN_ORC_HEAD);
        addQuestItem(WARSPIRIT_TOTEM);
        addQuestItem(ORIMS_CONTRACT);
        addQuestItem(PORTAS_EYE);
        addQuestItem(EXCUROS_SCALE);
        addQuestItem(MORDEOS_TALON);
        addQuestItem(BRAKIS_REMAINS1);
        addQuestItem(PEKIRONS_TOTEM);
        addQuestItem(TONARS_SKULL);
        addQuestItem(TONARS_RIB_BONE);
        addQuestItem(TONARS_SPINE);
        addQuestItem(TONARS_ARM_BONE);
        addQuestItem(TONARS_THIGH_BONE);
        addQuestItem(TONARS_REMAINS1);
        addQuestItem(MANAKIAS_TOTEM);
        addQuestItem(HERMODTS_SKULL);
        addQuestItem(HERMODTS_RIB_BONE);
        addQuestItem(HERMODTS_SPINE);
        addQuestItem(HERMODTS_ARM_BONE);
        addQuestItem(HERMODTS_THIGH_BONE);
        addQuestItem(HERMODTS_REMAINS1);
        addQuestItem(RACOYS_TOTEM);
        addQuestItem(VIVIANTES_LETTER);
        addQuestItem(INSECT_DIAGRAM_BOOK);
        addQuestItem(KIRUNAS_SKULL);
        addQuestItem(KIRUNAS_RIB_BONE);
        addQuestItem(KIRUNAS_SPINE);
        addQuestItem(KIRUNAS_ARM_BONE);
        addQuestItem(KIRUNAS_THIGH_BONE);
        addQuestItem(KIRUNAS_REMAINS1);
        addQuestItem(BRAKIS_REMAINS2);
        addQuestItem(TONARS_REMAINS2);
        addQuestItem(HERMODTS_REMAINS2);
        addQuestItem(KIRUNAS_REMAINS2);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("30510-05.htm".equalsIgnoreCase(event) && _state == 1) {
            if (!st.getPlayer().getVarB("dd3")) {
                st.giveItems(Dimensional_Diamond, 92L);
                st.getPlayer().setVar("dd3", "1", -1L);
            }
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("30630-04.htm".equalsIgnoreCase(event) && _state == 2) {
            st.giveItems(ORIMS_CONTRACT, 1L);
        } else if ("30682-02.htm".equalsIgnoreCase(event) && _state == 2) {
            st.giveItems(PEKIRONS_TOTEM, 1L);
        } else if ("30515-02.htm".equalsIgnoreCase(event) && _state == 2) {
            st.giveItems(MANAKIAS_TOTEM, 1L);
        } else if ("30507-02.htm".equalsIgnoreCase(event) && _state == 2) {
            st.giveItems(RACOYS_TOTEM, 1L);
        } else if ("30030-04.htm".equalsIgnoreCase(event) && _state == 2) {
            st.giveItems(VIVIANTES_LETTER, 1L);
        } else if ("30649-03.htm".equalsIgnoreCase(event) && _state == 2 && st.getQuestItemsCount(WARSPIRIT_TOTEM) > 0L) {
            st.takeItems(WARSPIRIT_TOTEM, -1L);
            st.takeItems(BRAKIS_REMAINS2, -1L);
            st.takeItems(HERMODTS_REMAINS2, -1L);
            st.takeItems(KIRUNAS_REMAINS2, -1L);
            st.takeItems(TAMLIN_ORC_HEAD, -1L);
            st.takeItems(TONARS_REMAINS2, -1L);
            st.giveItems(MARK_OF_WARSPIRIT, 1L);
            if (!st.getPlayer().getVarB("prof2.3")) {
                st.addExpAndSp(63483L, 17500L);
                st.getPlayer().setVar("prof2.3", "1", -1L);
            }
            st.playSound("ItemSound.quest_finish");
            st.unset("cond");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (st.getQuestItemsCount(MARK_OF_WARSPIRIT) > 0L) {
            st.exitCurrentQuest(true);
            return "completed";
        }
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != Somak) {
                return "noquest";
            }
            if (st.getPlayer().getRace() != Race.orc) {
                st.exitCurrentQuest(true);
                return "30510-01.htm";
            }
            if (st.getPlayer().getClassId().getId() != 50) {
                st.exitCurrentQuest(true);
                return "30510-02.htm";
            }
            if (st.getPlayer().getLevel() < 39) {
                st.exitCurrentQuest(true);
                return "30510-03.htm";
            }
            st.setCond(0);
            return "30510-04.htm";
        } else {
            if (_state != 2 || st.getCond() != 1) {
                return "noquest";
            }
            switch (npcId) {
                case Somak:
                    if (st.getQuestItemsCount(VENDETTA_TOTEM) > 0L) {
                        if (st.getQuestItemsCount(TAMLIN_ORC_HEAD) < 13L) {
                            return "30510-08.htm";
                        }
                        st.takeItems(VENDETTA_TOTEM, -1L);
                        st.giveItems(WARSPIRIT_TOTEM, 1L);
                        st.giveItems(BRAKIS_REMAINS2, 1L);
                        st.giveItems(HERMODTS_REMAINS2, 1L);
                        st.giveItems(KIRUNAS_REMAINS2, 1L);
                        st.giveItems(TONARS_REMAINS2, 1L);
                        st.playSound("ItemSound.quest_middle");
                        return "30510-09.htm";
                    } else {
                        if (st.getQuestItemsCount(WARSPIRIT_TOTEM) > 0L) {
                            return "30510-10.htm";
                        }
                        if (st.getQuestItemsCount(BRAKIS_REMAINS1) == 0L || st.getQuestItemsCount(HERMODTS_REMAINS1) == 0L || st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0L || st.getQuestItemsCount(TONARS_REMAINS1) == 0L) {
                            return "30510-06.htm";
                        }
                        st.takeItems(BRAKIS_REMAINS1, -1L);
                        st.takeItems(HERMODTS_REMAINS1, -1L);
                        st.takeItems(KIRUNAS_REMAINS1, -1L);
                        st.takeItems(TONARS_REMAINS1, -1L);
                        st.giveItems(VENDETTA_TOTEM, 1L);
                        st.playSound("ItemSound.quest_middle");
                        return "30510-07.htm";
                    }
                case Orim:
                    if (st.getQuestItemsCount(ORIMS_CONTRACT) > 0L) {
                        if (st.getQuestItemsCount(PORTAS_EYE) < 10L || st.getQuestItemsCount(EXCUROS_SCALE) < 10L || st.getQuestItemsCount(MORDEOS_TALON) < 10L) {
                            return "30630-05.htm";
                        }
                        st.takeItems(ORIMS_CONTRACT, -1L);
                        st.takeItems(PORTAS_EYE, -1L);
                        st.takeItems(EXCUROS_SCALE, -1L);
                        st.takeItems(MORDEOS_TALON, -1L);
                        st.giveItems(BRAKIS_REMAINS1, 1L);
                        st.playSound("ItemSound.quest_middle");
                        return "30630-06.htm";
                    } else {
                        if (st.getQuestItemsCount(BRAKIS_REMAINS1) == 0L && st.getQuestItemsCount(BRAKIS_REMAINS2) == 0L && st.getQuestItemsCount(VENDETTA_TOTEM) == 0L) {
                            return "30630-01.htm";
                        }
                        return "30630-07.htm";
                    }
                case Pekiron:
                    if (st.getQuestItemsCount(PEKIRONS_TOTEM) > 0L) {
                        for (final int drop_id : Leto_Lizardman_Drops) {
                            if (st.getQuestItemsCount(drop_id) == 0L) {
                                return "30682-03.htm";
                            }
                        }
                        st.takeItems(PEKIRONS_TOTEM, -1L);
                        for (final int drop_id : Leto_Lizardman_Drops) {
                            if (st.getQuestItemsCount(drop_id) == 0L) {
                                st.takeItems(drop_id, -1L);
                            }
                        }
                        st.giveItems(TONARS_REMAINS1, 1L);
                        st.playSound("ItemSound.quest_middle");
                        return "30682-04.htm";
                    }
                    if (st.getQuestItemsCount(TONARS_REMAINS1) == 0L && st.getQuestItemsCount(TONARS_REMAINS2) == 0L && st.getQuestItemsCount(VENDETTA_TOTEM) == 0L) {
                        return "30682-01.htm";
                    }
                    return "30682-05.htm";
                default:
                    if (npcId == Manakia) {
                        if (st.getQuestItemsCount(MANAKIAS_TOTEM) > 0L) {
                            if (st.getQuestItemsCount(HERMODTS_SKULL) == 0L) {
                                return "30515-03.htm";
                            }
                            for (final int drop_id : Medusa_Drops) {
                                if (st.getQuestItemsCount(drop_id) == 0L) {
                                    return "30515-03.htm";
                                }
                            }
                            st.takeItems(MANAKIAS_TOTEM, -1L);
                            st.takeItems(HERMODTS_SKULL, -1L);
                            for (final int drop_id : Medusa_Drops) {
                                if (st.getQuestItemsCount(drop_id) == 0L) {
                                    st.takeItems(drop_id, -1L);
                                }
                            }
                            st.giveItems(HERMODTS_REMAINS1, 1L);
                            st.playSound("ItemSound.quest_middle");
                            return "30515-04.htm";
                        } else {
                            if (st.getQuestItemsCount(HERMODTS_REMAINS1) == 0L && st.getQuestItemsCount(HERMODTS_REMAINS2) == 0L && st.getQuestItemsCount(VENDETTA_TOTEM) == 0L) {
                                return "30515-01.htm";
                            }
                            if (st.getQuestItemsCount(RACOYS_TOTEM) == 0L && (st.getQuestItemsCount(KIRUNAS_REMAINS2) > 0L || st.getQuestItemsCount(WARSPIRIT_TOTEM) > 0L || st.getQuestItemsCount(BRAKIS_REMAINS2) > 0L || st.getQuestItemsCount(HERMODTS_REMAINS2) > 0L || st.getQuestItemsCount(TAMLIN_ORC_HEAD) > 0L || st.getQuestItemsCount(TONARS_REMAINS2) > 0L)) {
                                return "30515-05.htm";
                            }
                        }
                    }
                    if (npcId == Racoy) {
                        if (st.getQuestItemsCount(RACOYS_TOTEM) > 0L) {
                            if (st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) == 0L) {
                                return (st.getQuestItemsCount(VIVIANTES_LETTER) == 0L) ? "30507-03.htm" : "30507-04.htm";
                            }
                            if (st.getQuestItemsCount(VIVIANTES_LETTER) == 0L) {
                                for (final int drop_id : Noble_Ant_Drops) {
                                    if (st.getQuestItemsCount(drop_id) == 0L) {
                                        return "30507-05.htm";
                                    }
                                }
                                st.takeItems(RACOYS_TOTEM, -1L);
                                st.takeItems(INSECT_DIAGRAM_BOOK, -1L);
                                for (final int drop_id : Noble_Ant_Drops) {
                                    if (st.getQuestItemsCount(drop_id) == 0L) {
                                        st.takeItems(drop_id, -1L);
                                    }
                                }
                                st.giveItems(KIRUNAS_REMAINS1, 1L);
                                st.playSound("ItemSound.quest_middle");
                                return "30507-06.htm";
                            }
                        } else {
                            if (st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0L && st.getQuestItemsCount(KIRUNAS_REMAINS2) == 0L && st.getQuestItemsCount(VENDETTA_TOTEM) == 0L) {
                                return "30507-01.htm";
                            }
                            return "30507-07.htm";
                        }
                    }
                    if (npcId == Vivyan) {
                        if (st.getQuestItemsCount(RACOYS_TOTEM) > 0L) {
                            if (st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) == 0L) {
                                return (st.getQuestItemsCount(VIVIANTES_LETTER) == 0L) ? "30030-01.htm" : "30030-05.htm";
                            }
                            if (st.getQuestItemsCount(VIVIANTES_LETTER) == 0L) {
                                return "30030-06.htm";
                            }
                        } else if (st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0L && st.getQuestItemsCount(KIRUNAS_REMAINS2) == 0L && st.getQuestItemsCount(VENDETTA_TOTEM) == 0L) {
                            return "30030-07.htm";
                        }
                    }
                    if (npcId == Sarien) {
                        if (st.getQuestItemsCount(RACOYS_TOTEM) > 0L) {
                            if (st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) == 0L && st.getQuestItemsCount(VIVIANTES_LETTER) > 0L) {
                                st.takeItems(VIVIANTES_LETTER, -1L);
                                st.giveItems(INSECT_DIAGRAM_BOOK, 1L);
                                st.playSound("ItemSound.quest_middle");
                                return "30436-01.htm";
                            }
                            if (st.getQuestItemsCount(VIVIANTES_LETTER) == 0L && st.getQuestItemsCount(INSECT_DIAGRAM_BOOK) > 0L) {
                                return "30436-02.htm";
                            }
                        } else if (st.getQuestItemsCount(KIRUNAS_REMAINS1) == 0L && st.getQuestItemsCount(KIRUNAS_REMAINS2) == 0L && st.getQuestItemsCount(VENDETTA_TOTEM) == 0L) {
                            return "30436-03.htm";
                        }
                    }
                    if (npcId == Ancestor_Martankus && st.getQuestItemsCount(WARSPIRIT_TOTEM) > 0L) {
                        return "30649-01.htm";
                    }
                    return "noquest";
            }
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2 || qs.getCond() < 1) {
            return null;
        }
        final int npcId = npc.getNpcId();
        if (npcId == Porta && qs.getQuestItemsCount(ORIMS_CONTRACT) > 0L && qs.getQuestItemsCount(PORTAS_EYE) < 10L) {
            qs.giveItems(PORTAS_EYE, 1L);
            qs.playSound((qs.getQuestItemsCount(PORTAS_EYE) == 10L) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        } else if (npcId == Excuro && qs.getQuestItemsCount(ORIMS_CONTRACT) > 0L && qs.getQuestItemsCount(EXCUROS_SCALE) < 10L) {
            qs.giveItems(EXCUROS_SCALE, 1L);
            qs.playSound((qs.getQuestItemsCount(EXCUROS_SCALE) == 10L) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        } else if (npcId == Mordeo && qs.getQuestItemsCount(ORIMS_CONTRACT) > 0L && qs.getQuestItemsCount(MORDEOS_TALON) < 10L) {
            qs.giveItems(MORDEOS_TALON, 1L);
            qs.playSound((qs.getQuestItemsCount(MORDEOS_TALON) == 10L) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        } else if ((npcId == Noble_Ant || npcId == Noble_Ant_Leader) && qs.getQuestItemsCount(RACOYS_TOTEM) > 0L) {
            List<Integer> drops = new ArrayList<>();
            for (final int drop_id : Noble_Ant_Drops) {
                if (qs.getQuestItemsCount(drop_id) == 0L) {
                    drops.add(drop_id);
                }
            }
            if (drops.size() > 0 && Rnd.chance(30)) {
                final int drop_id2 = drops.get(Rnd.get(drops.size()));
                qs.giveItems(drop_id2, 1L);
                qs.playSound((drops.size() == 1) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
            drops.clear();
        } else if ((npcId == Leto_Lizardman_Shaman || npcId == Leto_Lizardman_Overlord) && qs.getQuestItemsCount(PEKIRONS_TOTEM) > 0L) {
            List<Integer> drops = new ArrayList<>();
            for (final int drop_id : Leto_Lizardman_Drops) {
                if (qs.getQuestItemsCount(drop_id) == 0L) {
                    drops.add(drop_id);
                }
            }
            if (drops.size() > 0 && Rnd.chance(25)) {
                final int drop_id2 = drops.get(Rnd.get(drops.size()));
                qs.giveItems(drop_id2, 1L);
                qs.playSound((drops.size() == 1) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
            drops.clear();
        } else if (npcId == Medusa && qs.getQuestItemsCount(MANAKIAS_TOTEM) > 0L) {
            List<Integer> drops = new ArrayList<>();
            for (final int drop_id : Medusa_Drops) {
                if (qs.getQuestItemsCount(drop_id) == 0L) {
                    drops.add(drop_id);
                }
            }
            if (drops.size() > 0 && Rnd.chance(30)) {
                final int drop_id2 = drops.get(Rnd.get(drops.size()));
                qs.giveItems(drop_id2, 1L);
                qs.playSound((drops.size() == 1 && qs.getQuestItemsCount(HERMODTS_SKULL) > 0L) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
            }
            drops.clear();
        } else if (npcId == Stenoa_Gorgon_Queen && qs.getQuestItemsCount(MANAKIAS_TOTEM) > 0L && qs.getQuestItemsCount(HERMODTS_SKULL) == 0L && Rnd.chance(30)) {
            qs.giveItems(HERMODTS_SKULL, 1L);
            boolean _allset = true;
            for (final int drop_id : Medusa_Drops) {
                if (qs.getQuestItemsCount(drop_id) == 0L) {
                    _allset = false;
                    break;
                }
            }
            qs.playSound(_allset ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        } else if ((npcId == Tamlin_Orc || npcId == Tamlin_Orc_Archer) && qs.getQuestItemsCount(VENDETTA_TOTEM) > 0L && qs.getQuestItemsCount(TAMLIN_ORC_HEAD) < 13L && Rnd.chance((npcId == Tamlin_Orc) ? 30 : 50)) {
            qs.giveItems(TAMLIN_ORC_HEAD, 1L);
            qs.playSound((qs.getQuestItemsCount(TAMLIN_ORC_HEAD) == 13L) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        }
        return null;
    }

    
}
