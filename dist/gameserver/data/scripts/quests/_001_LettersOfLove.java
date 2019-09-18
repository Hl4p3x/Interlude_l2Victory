package quests;

import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _001_LettersOfLove extends Quest {
    private final int DARIN = 30048;
    private final int ROXXY = 30006;
    private final int BAULRO = 30033;
    private final int DARINGS_LETTER = 687;
    private final int ROXXY_KERCHIEF = 688;
    private final int DARINGS_RECEIPT = 1079;
    private final int BAULS_POTION = 1080;
    private final int SCROLL_OF_GIRAN = 7559;
    private final int MARK_OF_TRAVELER_ID = 7570;

    public _001_LettersOfLove() {
        super(false);
        addStartNpc(DARIN);
        addTalkId(ROXXY);
        addTalkId(BAULRO);
        addQuestItem(DARINGS_LETTER);
        addQuestItem(ROXXY_KERCHIEF);
        addQuestItem(DARINGS_RECEIPT);
        addQuestItem(BAULS_POTION);
    }

    

    @Override
    public String onEvent(final String event, final QuestState qs, final NpcInstance npc) {
        String htmltext = event;
        if ("quest_accept".equalsIgnoreCase(event)) {
            htmltext = "daring_q0001_06.htm";
            qs.setCond(1);
            qs.setState(2);
            qs.giveItems(DARINGS_LETTER, 1L, false);
            qs.playSound("ItemSound.quest_accept");
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        switch (npcId) {
            case DARIN: {
                if (cond == 0) {
                    if (st.getPlayer().getLevel() >= 2) {
                        htmltext = "daring_q0001_02.htm";
                        break;
                    }
                    htmltext = "daring_q0001_01.htm";
                    st.exitCurrentQuest(true);
                    break;
                } else {
                    if (cond == 1) {
                        htmltext = "daring_q0001_07.htm";
                        break;
                    }
                    if (cond == 2 && st.getQuestItemsCount(ROXXY_KERCHIEF) == 1L) {
                        htmltext = "daring_q0001_08.htm";
                        st.takeItems(ROXXY_KERCHIEF, -1L);
                        st.giveItems(DARINGS_RECEIPT, 1L, false);
                        st.setCond(3);
                        st.playSound("ItemSound.quest_middle");
                        break;
                    }
                    if (cond == 3) {
                        htmltext = "daring_q0001_09.htm";
                        break;
                    }
                    if (cond == 4 && st.getQuestItemsCount(BAULS_POTION) == 1L) {
                        htmltext = "daring_q0001_10.htm";
                        st.takeItems(BAULS_POTION, -1L);
                        st.giveItems(SCROLL_OF_GIRAN, 1L, false);
                        st.giveItems(MARK_OF_TRAVELER_ID, 1L, false);
                        if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("ng1")) {
                            st.getPlayer().sendPacket(new ExShowScreenMessage("  Delivery duty complete.\nGo find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                        }
                        st.playSound("ItemSound.quest_finish");
                        st.exitCurrentQuest(false);
                        break;
                    }
                    break;
                }
            }
            case ROXXY: {
                if (cond == 1 && st.getQuestItemsCount(ROXXY_KERCHIEF) == 0L && st.getQuestItemsCount(DARINGS_LETTER) > 0L) {
                    htmltext = "rapunzel_q0001_01.htm";
                    st.takeItems(DARINGS_LETTER, -1L);
                    st.giveItems(ROXXY_KERCHIEF, 1L, false);
                    st.setCond(2);
                    st.playSound("ItemSound.quest_middle");
                    break;
                }
                if (cond == 2 && st.getQuestItemsCount(ROXXY_KERCHIEF) > 0L) {
                    htmltext = "rapunzel_q0001_02.htm";
                    break;
                }
                if (cond > 2 && (st.getQuestItemsCount(BAULS_POTION) > 0L || st.getQuestItemsCount(DARINGS_RECEIPT) > 0L)) {
                    htmltext = "rapunzel_q0001_03.htm";
                    break;
                }
                break;
            }
            case BAULRO: {
                if (cond == 3 && st.getQuestItemsCount(DARINGS_RECEIPT) == 1L) {
                    htmltext = "baul_q0001_01.htm";
                    st.takeItems(DARINGS_RECEIPT, -1L);
                    st.giveItems(BAULS_POTION, 1L, false);
                    st.setCond(4);
                    st.playSound("ItemSound.quest_middle");
                    break;
                }
                if (cond == 4) {
                    htmltext = "baul_q0001_02.htm";
                    break;
                }
                break;
            }
        }
        return htmltext;
    }
}
