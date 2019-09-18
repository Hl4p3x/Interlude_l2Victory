package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _028_ChestCaughtWithABaitOfIcyAir extends Quest {
    int OFulle;
    int Kiki;
    int BigYellowTreasureChest;
    int KikisLetter;
    int ElvenRing;

    public _028_ChestCaughtWithABaitOfIcyAir() {
        super(false);
        OFulle = 31572;
        Kiki = 31442;
        BigYellowTreasureChest = 6503;
        KikisLetter = 7626;
        ElvenRing = 881;
        addStartNpc(OFulle);
        addTalkId(Kiki);
        addQuestItem(KikisLetter);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "fisher_ofulle_q0028_0104.htm":
                st.setState(2);
                st.setCond(1);
                st.playSound("ItemSound.quest_accept");
                break;
            case "fisher_ofulle_q0028_0201.htm":
                if (st.getQuestItemsCount(BigYellowTreasureChest) > 0L) {
                    st.setCond(2);
                    st.takeItems(BigYellowTreasureChest, 1L);
                    st.giveItems(KikisLetter, 1L);
                    st.playSound("ItemSound.quest_middle");
                } else {
                    htmltext = "fisher_ofulle_q0028_0202.htm";
                }
                break;
            case "mineral_trader_kiki_q0028_0301.htm":
                if (st.getQuestItemsCount(KikisLetter) == 1L) {
                    htmltext = "mineral_trader_kiki_q0028_0301.htm";
                    st.takeItems(KikisLetter, -1L);
                    st.giveItems(ElvenRing, 1L);
                    st.playSound("ItemSound.quest_finish");
                    st.exitCurrentQuest(false);
                } else {
                    htmltext = "mineral_trader_kiki_q0028_0302.htm";
                    st.exitCurrentQuest(true);
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        final int cond = st.getCond();
        if (npcId == OFulle) {
            if (id == 1) {
                if (st.getPlayer().getLevel() < 36) {
                    htmltext = "fisher_ofulle_q0028_0101.htm";
                    st.exitCurrentQuest(true);
                } else {
                    final QuestState OFullesSpecialBait = st.getPlayer().getQuestState(_051_OFullesSpecialBait.class);
                    if (OFullesSpecialBait != null) {
                        if (OFullesSpecialBait.isCompleted()) {
                            htmltext = "fisher_ofulle_q0028_0101.htm";
                        } else {
                            htmltext = "fisher_ofulle_q0028_0102.htm";
                            st.exitCurrentQuest(true);
                        }
                    } else {
                        htmltext = "fisher_ofulle_q0028_0103.htm";
                        st.exitCurrentQuest(true);
                    }
                }
            } else if (cond == 1) {
                htmltext = "fisher_ofulle_q0028_0105.htm";
                if (st.getQuestItemsCount(BigYellowTreasureChest) == 0L) {
                    htmltext = "fisher_ofulle_q0028_0106.htm";
                }
            } else if (cond == 2) {
                htmltext = "fisher_ofulle_q0028_0203.htm";
            }
        } else if (npcId == Kiki) {
            if (cond == 2) {
                htmltext = "mineral_trader_kiki_q0028_0201.htm";
            } else {
                htmltext = "mineral_trader_kiki_q0028_0302.htm";
            }
        }
        return htmltext;
    }
}
