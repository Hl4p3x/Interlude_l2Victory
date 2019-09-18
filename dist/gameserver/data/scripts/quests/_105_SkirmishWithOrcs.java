package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;

public class _105_SkirmishWithOrcs extends Quest {
    private static final int Kendell = 30218;
    private static final int Kendells1stOrder = 1836;
    private static final int Kendells2stOrder = 1837;
    private static final int Kendells3stOrder = 1838;
    private static final int Kendells4stOrder = 1839;
    private static final int Kendells5stOrder = 1840;
    private static final int Kendells6stOrder = 1841;
    private static final int Kendells7stOrder = 1842;
    private static final int Kendells8stOrder = 1843;
    private static final int KabooChiefs1stTorque = 1844;
    private static final int KabooChiefs2stTorque = 1845;
    private static final int RED_SUNSET_SWORD = 981;
    private static final int RED_SUNSET_STAFF = 754;
    private static final int KabooChiefUoph = 27059;
    private static final int KabooChiefKracha = 27060;
    private static final int KabooChiefBatoh = 27061;
    private static final int KabooChiefTanukia = 27062;
    private static final int KabooChiefTurel = 27064;
    private static final int KabooChiefRoko = 27065;
    private static final int KabooChiefKamut = 27067;
    private static final int KabooChiefMurtika = 27068;

    public _105_SkirmishWithOrcs() {
        super(false);
        addStartNpc(30218);
        addKillId(27059);
        addKillId(27060);
        addKillId(27061);
        addKillId(27062);
        addKillId(27064);
        addKillId(27065);
        addKillId(27067);
        addKillId(27068);
        addQuestItem(1836, 1837, 1838, 1839, 1840, 1841, 1842, 1843, 1844, 1845);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("sentinel_kendnell_q0105_03.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            if (st.getQuestItemsCount(1836) + st.getQuestItemsCount(1837) + st.getQuestItemsCount(1838) + st.getQuestItemsCount(1839) == 0L) {
                final int n = Rnd.get(4);
                switch (n) {
                    case 0:
                        st.giveItems(1836, 1L, false);
                        break;
                    case 1:
                        st.giveItems(1837, 1L, false);
                        break;
                    case 2:
                        st.giveItems(1838, 1L, false);
                        break;
                    default:
                        st.giveItems(1839, 1L, false);
                        break;
                }
            }
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int cond = st.getCond();
        if (cond == 0) {
            if (st.getPlayer().getRace() != Race.elf) {
                htmltext = "sentinel_kendnell_q0105_00.htm";
                st.exitCurrentQuest(true);
            } else if (st.getPlayer().getLevel() < 10) {
                htmltext = "sentinel_kendnell_q0105_10.htm";
                st.exitCurrentQuest(true);
            } else {
                htmltext = "sentinel_kendnell_q0105_02.htm";
            }
        } else if (cond == 1 && st.getQuestItemsCount(1836) + st.getQuestItemsCount(1837) + st.getQuestItemsCount(1838) + st.getQuestItemsCount(1839) != 0L) {
            htmltext = "sentinel_kendnell_q0105_05.htm";
        } else if (cond == 2 && st.getQuestItemsCount(1844) != 0L) {
            htmltext = "sentinel_kendnell_q0105_06.htm";
            st.takeItems(1836, -1L);
            st.takeItems(1837, -1L);
            st.takeItems(1838, -1L);
            st.takeItems(1839, -1L);
            st.takeItems(1844, 1L);
            final int n = Rnd.get(4);
            switch (n) {
                case 0:
                    st.giveItems(1840, 1L, false);
                    break;
                case 1:
                    st.giveItems(1841, 1L, false);
                    break;
                case 2:
                    st.giveItems(1842, 1L, false);
                    break;
                default:
                    st.giveItems(1843, 1L, false);
                    break;
            }
            st.setCond(3);
            st.setState(2);
        } else if (cond == 3 && st.getQuestItemsCount(1840) + st.getQuestItemsCount(1841) + st.getQuestItemsCount(1842) + st.getQuestItemsCount(1843) == 1L) {
            htmltext = "sentinel_kendnell_q0105_07.htm";
        } else if (cond == 4 && st.getQuestItemsCount(1845) > 0L) {
            htmltext = "sentinel_kendnell_q0105_08.htm";
            st.takeItems(1840, -1L);
            st.takeItems(1841, -1L);
            st.takeItems(1842, -1L);
            st.takeItems(1843, -1L);
            st.takeItems(1845, -1L);
            if (st.getPlayer().getClassId().isMage()) {
                st.giveItems(754, 1L, false);
            } else {
                st.giveItems(981, 1L, false);
            }
            st.giveItems(57, 17599L, false);
            st.getPlayer().addExpAndSp(41478L, 3555L);
            if (st.getPlayer().getClassId().getLevel() == 1 && !st.getPlayer().getVarB("p1q3")) {
                st.getPlayer().setVar("p1q3", "1", -1L);
                st.getPlayer().sendPacket(new ExShowScreenMessage("Now go find the Newbie Guide.", 5000, ScreenMessageAlign.TOP_CENTER, true));
                st.giveItems(1060, 100L);
                for (int item = 4412; item <= 4417; ++item) {
                    st.giveItems(item, 10L);
                }
                if (st.getPlayer().getClassId().isMage()) {
                    st.playTutorialVoice("tutorial_voice_027");
                    st.giveItems(5790, 3000L);
                } else {
                    st.playTutorialVoice("tutorial_voice_026");
                    st.giveItems(5789, 6000L);
                }
            }
            st.exitCurrentQuest(false);
            st.playSound("ItemSound.quest_finish");
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (cond == 1 && st.getQuestItemsCount(1844) == 0L) {
            if (npcId == 27059 && st.getQuestItemsCount(1836) > 0L) {
                st.giveItems(1844, 1L, false);
            } else if (npcId == 27060 && st.getQuestItemsCount(1837) > 0L) {
                st.giveItems(1844, 1L, false);
            } else if (npcId == 27061 && st.getQuestItemsCount(1838) > 0L) {
                st.giveItems(1844, 1L, false);
            } else if (npcId == 27062 && st.getQuestItemsCount(1839) > 0L) {
                st.giveItems(1844, 1L, false);
            }
            if (st.getQuestItemsCount(1844) > 0L) {
                st.setCond(2);
                st.setState(2);
                st.playSound("ItemSound.quest_middle");
            }
        } else if (cond == 3 && st.getQuestItemsCount(1845) == 0L) {
            if (npcId == 27064 && st.getQuestItemsCount(1840) > 0L) {
                st.giveItems(1845, 1L, false);
            } else if (npcId == 27065 && st.getQuestItemsCount(1841) > 0L) {
                st.giveItems(1845, 1L, false);
            } else if (npcId == 27067 && st.getQuestItemsCount(1842) > 0L) {
                st.giveItems(1845, 1L, false);
            } else if (npcId == 27068 && st.getQuestItemsCount(1843) > 0L) {
                st.giveItems(1845, 1L, false);
            }
            if (st.getQuestItemsCount(1845) > 0L) {
                st.setCond(4);
                st.setState(2);
                st.playSound("ItemSound.quest_middle");
            }
        }
        return null;
    }
}
