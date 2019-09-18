package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.tables.SkillTable;

import java.util.stream.IntStream;

public class _367_ElectrifyingRecharge extends Quest {
    private static final int LORAIN = 30673;
    private static final int CATHEROK = 21035;
    private static final int Titan_Lamp_First = 5875;
    private static final int Titan_Lamp_Last = 5879;
    private static final int Broken_Titan_Lamp = 5880;
    private static final int broke_chance = 3;
    private static final int uplight_chance = 7;

    public _367_ElectrifyingRecharge() {
        super(false);
        addStartNpc(LORAIN);
        addKillId(CATHEROK);
        IntStream.rangeClosed(Titan_Lamp_First, Titan_Lamp_Last).forEach(this::addQuestItem);
        addQuestItem(Broken_Titan_Lamp);
    }

    private static boolean takeAllLamps(final QuestState st) {
        boolean result = false;
        for (int Titan_Lamp_id = Titan_Lamp_First; Titan_Lamp_id <= Titan_Lamp_Last; ++Titan_Lamp_id) {
            if (st.getQuestItemsCount(Titan_Lamp_id) > 0L) {
                result = true;
                st.takeItems(Titan_Lamp_id, -1L);
            }
        }
        if (st.getQuestItemsCount(Broken_Titan_Lamp) > 0L) {
            result = true;
            st.takeItems(Broken_Titan_Lamp, -1L);
        }
        return result;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("30673-03.htm".equalsIgnoreCase(event) && _state == 1) {
            takeAllLamps(st);
            st.giveItems(Titan_Lamp_First, 1L);
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("30673-07.htm".equalsIgnoreCase(event) && _state == 2) {
            takeAllLamps(st);
            st.giveItems(Titan_Lamp_First, 1L);
        } else if ("30673-08.htm".equalsIgnoreCase(event) && _state == 2) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != LORAIN) {
            return htmltext;
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getLevel() < 37) {
                htmltext = "30673-02.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30673-01.htm";
                st.setCond(0);
            }
        } else if (_state == 2) {
            if (st.getQuestItemsCount(Titan_Lamp_Last) > 0L) {
                htmltext = "30673-06.htm";
                takeAllLamps(st);
                st.giveItems(4553 + Rnd.get(12), 1L);
                st.playSound("ItemSound.quest_middle");
            } else if (st.getQuestItemsCount(Broken_Titan_Lamp) > 0L) {
                htmltext = "30673-05.htm";
                takeAllLamps(st);
                st.giveItems(Titan_Lamp_First, 1L);
            } else {
                htmltext = "30673-04.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onAttack(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        if (qs.getQuestItemsCount(Broken_Titan_Lamp) > 0L) {
            return null;
        }
        if (Rnd.chance(uplight_chance)) {
            for (int Titan_Lamp_id = Titan_Lamp_First; Titan_Lamp_id < Titan_Lamp_Last; ++Titan_Lamp_id) {
                if (qs.getQuestItemsCount(Titan_Lamp_id) > 0L) {
                    final int Titan_Lamp_Next = Titan_Lamp_id + 1;
                    takeAllLamps(qs);
                    qs.giveItems(Titan_Lamp_Next, 1L);
                    if (Titan_Lamp_Next == Titan_Lamp_Last) {
                        qs.setCond(2);
                        qs.playSound("ItemSound.quest_middle");
                    } else {
                        qs.playSound("ItemSound.quest_itemget");
                    }
                    npc.doCast(SkillTable.getInstance().getInfo(4072, 4), qs.getPlayer(), true);
                    return null;
                }
                if (Rnd.chance(broke_chance) && takeAllLamps(qs)) {
                    qs.giveItems(Broken_Titan_Lamp, 1L);
                }
            }
        }
        return null;
    }

    
}
