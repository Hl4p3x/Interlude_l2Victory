package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _275_BlackWingedSpies extends Quest {
    private static final int Tantus = 30567;
    private static final int Darkwing_Bat = 20316;
    private static final int Varangkas_Tracker = 27043;
    private static final int Darkwing_Bat_Fang = 1478;
    private static final int Varangkas_Parasite = 1479;
    private static final int Varangkas_Parasite_Chance = 10;

    public _275_BlackWingedSpies() {
        super(false);
        addStartNpc(Tantus);
        addKillId(Darkwing_Bat);
        addKillId(Varangkas_Tracker);
        addQuestItem(Darkwing_Bat_Fang);
        addQuestItem(Varangkas_Parasite);
    }

    private static void spawn_Varangkas_Tracker(final QuestState st) {
        if (st.getQuestItemsCount(Varangkas_Parasite) > 0L) {
            st.takeItems(Varangkas_Parasite, -1L);
        }
        st.giveItems(Varangkas_Parasite, 1L);
        st.addSpawn(Varangkas_Tracker);
    }

    public static void give_Darkwing_Bat_Fang(final QuestState st, long _count) {
        final long max_inc = 70L - st.getQuestItemsCount(Darkwing_Bat_Fang);
        if (max_inc < 1L) {
            return;
        }
        if (_count > max_inc) {
            _count = max_inc;
        }
        st.giveItems(Darkwing_Bat_Fang, _count);
        st.playSound((_count == max_inc) ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        if (_count == max_inc) {
            st.setCond(2);
        }
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("neruga_chief_tantus_q0275_03.htm".equalsIgnoreCase(event) && st.getState() == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (npc.getNpcId() != Tantus) {
            return "noquest";
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getRace() != Race.orc) {
                st.exitCurrentQuest(true);
                return "neruga_chief_tantus_q0275_00.htm";
            }
            if (st.getPlayer().getLevel() < 11) {
                st.exitCurrentQuest(true);
                return "neruga_chief_tantus_q0275_01.htm";
            }
            st.setCond(0);
            return "neruga_chief_tantus_q0275_02.htm";
        } else {
            if (_state != 2) {
                return "noquest";
            }
            final int cond = st.getCond();
            if (st.getQuestItemsCount(Darkwing_Bat_Fang) < 70L) {
                if (cond != 1) {
                    st.setCond(1);
                }
                return "neruga_chief_tantus_q0275_04.htm";
            }
            if (cond == 2) {
                st.giveItems(57, 4550L);
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(true);
                return "neruga_chief_tantus_q0275_05.htm";
            }
            return "noquest";
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        final int npcId = npc.getNpcId();
        final long Darkwing_Bat_Fang_count = qs.getQuestItemsCount(Darkwing_Bat_Fang);
        if (npcId == Darkwing_Bat && Darkwing_Bat_Fang_count < 70L) {
            if (Darkwing_Bat_Fang_count > 10L && Darkwing_Bat_Fang_count < 65L && Rnd.chance(Varangkas_Parasite_Chance)) {
                spawn_Varangkas_Tracker(qs);
                return null;
            }
            give_Darkwing_Bat_Fang(qs, 1L);
        } else if (npcId == Varangkas_Tracker && Darkwing_Bat_Fang_count < 70L && qs.getQuestItemsCount(Varangkas_Parasite) > 0L) {
            qs.takeItems(Varangkas_Parasite, -1L);
            give_Darkwing_Bat_Fang(qs, 5L);
        }
        return null;
    }

    
}
