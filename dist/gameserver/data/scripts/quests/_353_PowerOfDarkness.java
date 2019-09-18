package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _353_PowerOfDarkness extends Quest {
    private static final int GALMAN = 31044;
    private static final int Malruk_Succubus = 20283;
    private static final int Malruk_Succubus_Turen = 20284;
    private static final int Malruk_Succubus2 = 20244;
    private static final int Malruk_Succubus_Turen2 = 20245;
    private static final int STONE = 5862;
    private static final int ADENA = 57;
    private static final int STONE_CHANCE = 50;

    public _353_PowerOfDarkness() {
        super(false);
        addStartNpc(GALMAN);
        addKillId(Malruk_Succubus);
        addKillId(Malruk_Succubus_Turen);
        addKillId(Malruk_Succubus2);
        addKillId(Malruk_Succubus_Turen2);
        addQuestItem(STONE);
    }

    @Override
    public String onEvent(final String event, final QuestState qs, final NpcInstance npc) {
        final int id = qs.getState();
        if ("31044-04.htm".equalsIgnoreCase(event) && id == 1) {
            qs.setState(2);
            qs.setCond(1);
            qs.playSound("ItemSound.quest_accept");
        } else if ("31044-08.htm".equalsIgnoreCase(event) && id == 2) {
            qs.playSound("ItemSound.quest_finish");
            qs.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState qs) {
        String htmltext = "noquest";
        if (npc.getNpcId() != GALMAN) {
            return htmltext;
        }
        if (qs.getState() == 1) {
            if (qs.getPlayer().getLevel() >= 55) {
                htmltext = "31044-02.htm";
                qs.setCond(0);
            } else {
                htmltext = "31044-01.htm";
                qs.exitCurrentQuest(true);
            }
        } else {
            final long stone_count = qs.getQuestItemsCount(STONE);
            if (stone_count > 0L) {
                htmltext = "31044-06.htm";
                qs.takeItems(STONE, -1L);
                qs.giveItems(ADENA, 2500L + 230L * stone_count);
                qs.playSound("ItemSound.quest_middle");
            } else {
                htmltext = "31044-05.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        if (qs.getState() != 2) {
            return null;
        }
        if (Rnd.chance(STONE_CHANCE)) {
            qs.giveItems(STONE, 1L);
            qs.playSound("ItemSound.quest_itemget");
        }
        return null;
    }

    
}
