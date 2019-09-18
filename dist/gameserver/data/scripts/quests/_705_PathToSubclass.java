package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

/**
 * Created by JunkyFunky
 * on 29.01.2018 21:02
 * group j2dev
 */
public class _705_PathToSubclass extends Quest {
    private static final int CABRIOCOFFER = 45027;
    private static final int CHEST_KERNON = 45028;
    private static final int CHEST_GOLKONDA = 45029;
    private static final int CHEST_HALLATE = 45030;
    private static final int SHILLEN_MESSAGER = 25035;
    private static final int DEATH_LORD = 25220;
    private static final int KERNON = 25054;
    private static final int LONGHORN = 25126;
    private static final int REIRIAS_SOUL_ORB = 4666;
    private static final int KERNONS_INFERNIUM_SCEPTER = 4667;
    private static final int GOLCONDAS_INFERNIUM_SCEPTER = 4668;
    private static final int HALLATES_INFERNIUM_SCEPTER = 4669;

    public _705_PathToSubclass() {
        super(false);
        addTalkId(CABRIOCOFFER, CHEST_KERNON, CHEST_GOLKONDA, CHEST_HALLATE);
        addKillId(SHILLEN_MESSAGER, DEATH_LORD, KERNON, LONGHORN);
        addQuestItem(REIRIAS_SOUL_ORB, KERNONS_INFERNIUM_SCEPTER, GOLCONDAS_INFERNIUM_SCEPTER, HALLATES_INFERNIUM_SCEPTER);
    }

    @Override
    public void onCreate(QuestState qs) {
        super.onCreate(qs);
        qs.setCond(1);
        qs.setState(STARTED);
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int cond = st.getState();
        if (npcId == CABRIOCOFFER && cond == 1) {
            htmltext = "cabrio_coffer.htm";
        } else if (npcId == CHEST_HALLATE && cond == 1) {
            htmltext = "hallate_coffer.htm";
        } else if (npcId == CHEST_KERNON && cond == 1) {
            htmltext = "kernon_coffer.htm";
        } else if (npcId == CHEST_GOLKONDA && cond == 1) {
            htmltext = "golkonda_coffer.htm";
        }
        return htmltext;
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {

        if (event.equalsIgnoreCase("cabrio")) {
            st.giveItems(4666, 1L, false);
        } else if (event.equalsIgnoreCase("deathlord")) {
            st.giveItems(4669, 1L, false);
        } else if (event.equalsIgnoreCase("kernon")) {
            st.giveItems(4667, 1L, false);
        } else if (event.equalsIgnoreCase("longhorn")) {
            st.giveItems(4668, 1L, false);
        }

        if (getItemDestroy(st, 4666, 1L) && getItemDestroy(st, 4667, 1L) && getItemDestroy(st, 4668, 1L) && getItemDestroy(st, 4669, 1L)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(false);
        }

        return event;
    }

    /*public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        if ((npcId == 20517 || npcId == 20518 || npcId == 20455) && st.getCond() == 3) {
            if (st.getQuestItemsCount(970) == 1L && st.getQuestItemsCount(1107) < 10L && Rnd.chance(33)) {
                st.giveItems(1107, 1L);
                if (st.getQuestItemsCount(1107) == 10L) {
                    st.playSound("ItemSound.quest_middle");
                    st.setCond(4);
                } else {
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        }
        return null;
    } */

    private boolean getItemDestroy(final QuestState st, final int itemId, final long count) {
        return st.getPlayer().getInventory().destroyItemByItemId(itemId, count);
    }
}
