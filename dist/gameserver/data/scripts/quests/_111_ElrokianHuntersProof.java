package quests;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;

public class _111_ElrokianHuntersProof extends Quest {
    private static final int Marquez = 32113;
    private static final int Asamah = 32115;
    private static final int Kirikachin = 32116;
    private static final int[] Velociraptor = {22196, 22197, 22198, 22218, 22223};
    private static final int[] Ornithomimus = {22200, 22201, 22202, 22219, 22224};
    private static final int[] Deinonychus = {22203, 22204, 22205, 22220, 22225};
    private static final int[] Pachycephalosaurus = {22208, 22209, 22210, 22221, 22226};
    private static final int DiaryFragment = 8768;
    private static final int OrnithomimusClaw = 8770;
    private static final int DeinonychusBone = 8771;
    private static final int PachycephalosaurusSkin = 8772;
    private static final int ElrokianTrap = 8763;
    private static final int TrapStone = 8764;

    public _111_ElrokianHuntersProof() {
        super(true);
        addStartNpc(32113);
        addTalkId(32115);
        addTalkId(32116);
        addKillId(_111_ElrokianHuntersProof.Velociraptor);
        addKillId(_111_ElrokianHuntersProof.Ornithomimus);
        addKillId(_111_ElrokianHuntersProof.Deinonychus);
        addKillId(_111_ElrokianHuntersProof.Pachycephalosaurus);
        addQuestItem(8768, 8770, 8771, 8772);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        final int cond = st.getCond();
        final Player player = st.getPlayer();
        if ("marquez_q111_2.htm".equalsIgnoreCase(event) && cond == 0) {
            st.setCond(2);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        } else if ("asamah_q111_2.htm".equalsIgnoreCase(event)) {
            st.setCond(3);
            st.playSound("ItemSound.quest_middle");
        } else if ("marquez_q111_4.htm".equalsIgnoreCase(event)) {
            st.setCond(4);
            st.playSound("ItemSound.quest_middle");
        } else if ("marquez_q111_6.htm".equalsIgnoreCase(event)) {
            st.setCond(6);
            st.takeItems(8768, -1L);
            st.playSound("ItemSound.quest_middle");
        } else if ("kirikachin_q111_2.htm".equalsIgnoreCase(event)) {
            st.setCond(7);
            player.sendPacket(new PlaySound("EtcSound.elcroki_song_full"));
        } else if ("kirikachin_q111_3.htm".equalsIgnoreCase(event)) {
            st.setCond(8);
            st.playSound("ItemSound.quest_middle");
        } else if ("asamah_q111_4.htm".equalsIgnoreCase(event)) {
            st.setCond(9);
            st.playSound("ItemSound.quest_middle");
        } else if ("asamah_q111_5.htm".equalsIgnoreCase(event)) {
            st.setCond(10);
            st.playSound("ItemSound.quest_middle");
        } else if ("asamah_q111_7.htm".equalsIgnoreCase(event)) {
            st.takeItems(8770, -1L);
            st.takeItems(8771, -1L);
            st.takeItems(8772, -1L);
            st.setCond(12);
            st.playSound("ItemSound.quest_middle");
        } else if ("asamah_q111_8.htm".equalsIgnoreCase(event)) {
            st.giveItems(57, 1022636L);
            st.giveItems(8763, 1L);
            st.giveItems(8764, 100L);
            st.setState(3);
            st.exitCurrentQuest(false);
            st.playSound("ItemSound.quest_finish");
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == 32113) {
            if (st.getPlayer().getLevel() >= 75 && cond == 0) {
                htmltext = "marquez_q111_1.htm";
            } else if (st.getPlayer().getLevel() < 75 && cond == 0) {
                htmltext = "marquez_q111_0.htm";
            } else if (cond == 3) {
                htmltext = "marquez_q111_3.htm";
            } else if (cond == 5) {
                htmltext = "marquez_q111_5.htm";
            }
        } else if (npcId == 32115) {
            switch (cond) {
                case 2:
                    htmltext = "asamah_q111_1.htm";
                    break;
                case 8:
                    htmltext = "asamah_q111_3.htm";
                    break;
                case 11:
                    htmltext = "asamah_q111_6.htm";
                    break;
            }
        } else if (npcId == 32116 && cond == 6) {
            htmltext = "kirikachin_q111_1.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int id = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 4) {
            for (final int i : _111_ElrokianHuntersProof.Velociraptor) {
                if (id == i && st.getQuestItemsCount(8768) < 50L) {
                    st.giveItems(8768, 1L, false);
                    if (st.getQuestItemsCount(8768) == 50L) {
                        st.playSound("ItemSound.quest_middle");
                        st.setCond(5);
                        return null;
                    }
                    st.playSound("ItemSound.quest_itemget");
                }
            }
        } else if (cond == 10) {
            for (final int i : _111_ElrokianHuntersProof.Ornithomimus) {
                if (id == i && st.getQuestItemsCount(8770) < 10L) {
                    st.giveItems(8770, 1L, false);
                    return null;
                }
            }
            for (final int i : _111_ElrokianHuntersProof.Deinonychus) {
                if (id == i && st.getQuestItemsCount(8771) < 10L) {
                    st.giveItems(8771, 1L, false);
                    return null;
                }
            }
            for (final int i : _111_ElrokianHuntersProof.Pachycephalosaurus) {
                if (id == i && st.getQuestItemsCount(8772) < 10L) {
                    st.giveItems(8772, 1L, false);
                    return null;
                }
            }
            if (st.getQuestItemsCount(8770) >= 10L && st.getQuestItemsCount(8771) >= 10L && st.getQuestItemsCount(8772) >= 10L) {
                st.setCond(11);
                st.playSound("ItemSound.quest_middle");
                return null;
            }
        }
        return null;
    }
}
