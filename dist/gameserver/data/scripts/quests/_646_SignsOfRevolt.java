package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.stream.IntStream;

public class _646_SignsOfRevolt extends Quest {
    private static final int TORRANT = 32016;
    private static final int Ragna_Orc = 22029;
    private static final int Ragna_Orc_Sorcerer = 22044;
    private static final int Guardian_of_the_Ghost_Town = 22047;
    private static final int Varangkas_Succubus = 22049;
    private static final int Steel = 1880;
    private static final int Coarse_Bone_Powder = 1881;
    private static final int Leather = 1882;
    private static final int CURSED_DOLL = 8087;
    private static final int CURSED_DOLL_Chance = 75;

    public _646_SignsOfRevolt() {
        super(false);
        addStartNpc(TORRANT);
        IntStream.rangeClosed(Ragna_Orc, Ragna_Orc_Sorcerer).forEach(this::addKillId);
        addKillId(Guardian_of_the_Ghost_Town);
        addKillId(Varangkas_Succubus);
        addQuestItem(CURSED_DOLL);
    }

    private static String doReward(final QuestState st, final int reward_id, final int _count) {
        if (st.getQuestItemsCount(CURSED_DOLL) < 180L) {
            return null;
        }
        st.takeItems(CURSED_DOLL, -1L);
        st.giveItems(reward_id, (long) _count, true);
        st.playSound("ItemSound.quest_finish");
        st.exitCurrentQuest(true);
        return "torant_q0646_0202.htm";
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int _state = st.getState();
        if ("torant_q0646_0103.htm".equalsIgnoreCase(event) && _state == 1) {
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else {
            if ("reward_adena".equalsIgnoreCase(event) && _state == 2) {
                return doReward(st, 57, 21600);
            }
            if ("reward_cbp".equalsIgnoreCase(event) && _state == 2) {
                return doReward(st, Coarse_Bone_Powder, 12);
            }
            if ("reward_steel".equalsIgnoreCase(event) && _state == 2) {
                return doReward(st, Steel, 9);
            }
            if ("reward_leather".equalsIgnoreCase(event) && _state == 2) {
                return doReward(st, Leather, 20);
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        if (npc.getNpcId() != TORRANT) {
            return htmltext;
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getLevel() < 40) {
                htmltext = "torant_q0646_0102.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "torant_q0646_0101.htm";
                st.setCond(0);
            }
        } else if (_state == 2) {
            htmltext = ((st.getQuestItemsCount(CURSED_DOLL) >= 180L) ? "torant_q0646_0105.htm" : "torant_q0646_0106.htm");
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        final Player player = qs.getRandomPartyMember(2, Config.ALT_PARTY_DISTRIBUTION_RANGE);
        if (player == null) {
            return null;
        }
        final QuestState st = player.getQuestState(qs.getQuest().getName());
        final long CURSED_DOLL_COUNT = st.getQuestItemsCount(CURSED_DOLL);
        if (CURSED_DOLL_COUNT < 180L && Rnd.chance(CURSED_DOLL_Chance)) {
            st.giveItems(CURSED_DOLL, 1L);
            if (CURSED_DOLL_COUNT == 179L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }

    
}
