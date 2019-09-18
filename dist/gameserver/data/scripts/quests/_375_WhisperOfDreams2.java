package quests;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.Arrays;
import java.util.List;

public class _375_WhisperOfDreams2 extends Quest {
    private final int MANAKIA = 30515;
    private final int MSTONE = 5887;
    private final int K_HORN = 5888;
    private final int CH_SKULL = 5889;
    private final int CAVE_HOWLER = 20624;
    private final int KARIK = 20629;
    private final int[] REWARDS = {5348, 5352, 5350};
    private final List<Pair<Integer, Pair<Integer, Integer>>> DROPLIST = Arrays.asList(Pair.of(CAVE_HOWLER, Pair.of(CH_SKULL, 100)), Pair.of(KARIK, Pair.of(K_HORN, 100)));
    private final String _default = "noquest";

    public _375_WhisperOfDreams2() {
        super(1);
        addStartNpc(MANAKIA);
        DROPLIST.forEach(e -> {
            addKillId(e.getLeft());
            addQuestItem((int) ((Pair) e.getRight()).getKey());
        });
    }

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30515-6.htm".equalsIgnoreCase(event)) {
            st.takeItems(MSTONE, 1L);
            st.setState(2);
            st.setCond(1);
            st.playSound("ItemSound.quest_accept");
        } else if ("30515-7.htm".equalsIgnoreCase(event)) {
            st.playSound("ItemSound.quest_finish");
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = _default;
        final int id = st.getState();
        if (id == 1) {
            st.setCond(0);
            htmltext = "30515-1.htm";
            if (st.getPlayer().getLevel() < 60) {
                htmltext = "30515-2.htm";
                st.exitCurrentQuest(true);
            } else if (st.getQuestItemsCount(MSTONE) < 1L) {
                htmltext = "30515-3.htm";
                st.exitCurrentQuest(true);
            }
        } else if (id == 2) {
            boolean enoughItems = true;
            for (final Pair<Integer, Pair<Integer, Integer>> e : DROPLIST) {
                if (st.getQuestItemsCount((int) ((Pair) e.getRight()).getKey()) < (int) ((Pair) e.getRight()).getValue()) {
                    enoughItems = false;
                    break;
                }
            }
            if (enoughItems) {
                st.takeItems(CH_SKULL, -1L);
                st.takeItems(K_HORN, -1L);
                final int item = REWARDS[Rnd.get(REWARDS.length)];
                st.giveItems(item, 1L);
                htmltext = "30515-4.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "30515-5.htm";
            }
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcid = npc.getNpcId();
        int itemTypeId = 0;
        int requiredItemCount = 0;
        for (final Pair<Integer, Pair<Integer, Integer>> e : DROPLIST) {
            if (npcid == e.getLeft()) {
                itemTypeId = (int) ((Pair) e.getRight()).getKey();
                requiredItemCount = (int) ((Pair) e.getRight()).getValue();
            }
        }
        final Player partyMember = Rnd.get(st.getPartyMembers(2, Config.ALT_PARTY_DISTRIBUTION_RANGE, npc));
        final QuestState partyMemberQuestState = partyMember.getQuestState(this);
        if (partyMemberQuestState == null) {
            return null;
        }
        final long actualItemCount = partyMemberQuestState.getQuestItemsCount(itemTypeId);
        if (actualItemCount < requiredItemCount) {
            int rewardItemCount = 1;
            if (actualItemCount + 1L < requiredItemCount && Rnd.chance(20)) {
                rewardItemCount = 2;
            }
            partyMemberQuestState.giveItems(itemTypeId, (long) rewardItemCount, true);
            partyMemberQuestState.playSound("ItemSound.quest_itemget");
        } else if (actualItemCount >= requiredItemCount) {
            partyMemberQuestState.playSound("ItemSound.quest_middle");
        }
        return null;
    }

    
}
