package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _293_HiddenVein extends Quest {
    private static final int Filaur = 30535;
    private static final int Chichirin = 30539;
    private static final int Utuku_Orc = 20446;
    private static final int Utuku_Orc_Archer = 20447;
    private static final int Utuku_Orc_Grunt = 20448;
    private static final int Chrysolite_Ore = 1488;
    private static final int Torn_Map_Fragment = 1489;
    private static final int Hidden_Ore_Map = 1490;
    private static final int Torn_Map_Fragment_Chance = 5;
    private static final int Chrysolite_Ore_Chance = 45;

    public _293_HiddenVein() {
        super(false);
        addStartNpc(Filaur);
        addTalkId(Chichirin);
        addKillId(Utuku_Orc);
        addKillId(Utuku_Orc_Archer);
        addKillId(Utuku_Orc_Grunt);
        addQuestItem(Chrysolite_Ore);
        addQuestItem(Torn_Map_Fragment);
        addQuestItem(Hidden_Ore_Map);
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("elder_filaur_q0293_03.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("elder_filaur_q0293_06.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        } else if ("chichirin_q0293_03.htm".equalsIgnoreCase(event) && _state == 2) {
            if (st.getQuestItemsCount(Torn_Map_Fragment) < 4L) {
                return "chichirin_q0293_02.htm";
            }
            st.takeItems(Torn_Map_Fragment, 4L);
            st.giveItems(Hidden_Ore_Map, 1L);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int _state = st.getState();
        final int npcId = npc.getNpcId();
        if (_state == 1) {
            if (npcId != Filaur) {
                return "noquest";
            }
            if (st.getPlayer().getRace() != Race.dwarf) {
                st.exitCurrentQuest(true);
                return "elder_filaur_q0293_00.htm";
            }
            if (st.getPlayer().getLevel() < 6) {
                st.exitCurrentQuest(true);
                return "elder_filaur_q0293_01.htm";
            }
            st.setCond(0);
            return "elder_filaur_q0293_02.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            if (npcId == Filaur) {
                final long Chrysolite_Ore_count = st.getQuestItemsCount(Chrysolite_Ore);
                final long Hidden_Ore_Map_count = st.getQuestItemsCount(Hidden_Ore_Map);
                final long reward = st.getQuestItemsCount(Chrysolite_Ore) * 10L + st.getQuestItemsCount(Hidden_Ore_Map) * 1000L;
                if (reward == 0L) {
                    return "elder_filaur_q0293_04.htm";
                }
                if (Chrysolite_Ore_count > 0L) {
                    st.takeItems(Chrysolite_Ore, -1L);
                }
                if (Hidden_Ore_Map_count > 0L) {
                    st.takeItems(Hidden_Ore_Map, -1L);
                }
                st.giveItems(57, reward);
                if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q2")) {
                    st.getPlayer().setVar("p1q2", "1", -1L);
                    st.getPlayer().sendPacket(new ExShowScreenMessage("Acquisition of Soulshot for beginners complete.\n                  Go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                    final QuestState qs = st.getPlayer().getQuestState(_255_Tutorial.class);
                    if (qs != null && qs.getInt("Ex") != 10) {
                        st.showQuestionMark(26);
                        qs.set("Ex", "10");
                        if (st.getPlayer().getClassId().isMage()) {
                            st.playTutorialVoice("tutorial_voice_027");
                            st.giveItems(5790, 3000L);
                        } else {
                            st.playTutorialVoice("tutorial_voice_026");
                            st.giveItems(5789, 6000L);
                        }
                    }
                }
                return (Chrysolite_Ore_count > 0L && Hidden_Ore_Map_count > 0L) ? "elder_filaur_q0293_09.htm" : ((Hidden_Ore_Map_count > 0L) ? "elder_filaur_q0293_08.htm" : "elder_filaur_q0293_05.htm");
            } else {
                if (npcId == Chichirin) {
                    return "chichirin_q0293_01.htm";
                }
                return "noquest";
            }
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        if (Rnd.chance(Torn_Map_Fragment_Chance)) {
            qs.giveItems(Torn_Map_Fragment, 1L);
            qs.playSound("ItemSound.quest_itemget");
        } else if (Rnd.chance(Chrysolite_Ore_Chance)) {
            qs.giveItems(Chrysolite_Ore, 1L);
            qs.playSound("ItemSound.quest_itemget");
        }
        return null;
    }

    
}
