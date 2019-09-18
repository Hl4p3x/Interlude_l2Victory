package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _336_CoinOfMagic extends Quest {
    private static final int COIN_DIAGRAM = 3811;
    private static final int KALDIS_COIN = 3812;
    private static final int MEMBERSHIP_1 = 3813;
    private static final int MEMBERSHIP_2 = 3814;
    private static final int MEMBERSHIP_3 = 3815;
    private static final int BLOOD_MEDUSA = 3472;
    private static final int BLOOD_WEREWOLF = 3473;
    private static final int BLOOD_BASILISK = 3474;
    private static final int BLOOD_DREVANUL = 3475;
    private static final int BLOOD_SUCCUBUS = 3476;
    private static final int BLOOD_DRAGON = 3477;
    private static final int BELETHS_BLOOD = 3478;
    private static final int MANAKS_BLOOD_WEREWOLF = 3479;
    private static final int NIAS_BLOOD_MEDUSA = 3480;
    private static final int GOLD_DRAGON = 3481;
    private static final int GOLD_WYVERN = 3482;
    private static final int GOLD_KNIGHT = 3483;
    private static final int GOLD_GIANT = 3484;
    private static final int GOLD_DRAKE = 3485;
    private static final int GOLD_WYRM = 3486;
    private static final int BELETHS_GOLD = 3487;
    private static final int MANAKS_GOLD_GIANT = 3488;
    private static final int NIAS_GOLD_WYVERN = 3489;
    private static final int SILVER_UNICORN = 3490;
    private static final int SILVER_FAIRY = 3491;
    private static final int SILVER_DRYAD = 3492;
    private static final int SILVER_DRAGON = 3493;
    private static final int SILVER_GOLEM = 3494;
    private static final int SILVER_UNDINE = 3495;
    private static final int BELETHS_SILVER = 3496;
    private static final int MANAKS_SILVER_DRYAD = 3497;
    private static final int NIAS_SILVER_FAIRY = 3498;
    private static final int[] BASIC_COINS = {3472, 3482, 3490};
    private static final int SORINT = 30232;
    private static final int BERNARD = 30702;
    private static final int PAGE = 30696;
    private static final int HAGGER = 30183;
    private static final int STAN = 30200;
    private static final int RALFORD = 30165;
    private static final int FERRIS = 30847;
    private static final int COLLOB = 30092;
    private static final int PANO = 30078;
    private static final int DUNING = 30688;
    private static final int LORAIN = 30673;
    private static final int TimakOrcArcher = 20584;
    private static final int TimakOrcSoldier = 20585;
    private static final int TimakOrcShaman = 20587;
    private static final int Lakin = 20604;
    private static final int TorturedUndead = 20678;
    private static final int HatarHanishee = 20663;
    private static final int Shackle = 20235;
    private static final int TimakOrc = 20583;
    private static final int HeadlessKnight = 20146;
    private static final int RoyalCaveServant = 20240;
    private static final int MalrukSuccubusTuren = 20245;
    private static final int Formor = 20568;
    private static final int FormorElder = 20569;
    private static final int VanorSilenosShaman = 20685;
    private static final int TarlkBugbearHighWarrior = 20572;
    private static final int OelMahum = 20161;
    private static final int OelMahumWarrior = 20575;
    private static final int HaritLizardmanMatriarch = 20645;
    private static final int HaritLizardmanShaman = 20644;
    private static final int Shackle2 = 20279;
    private static final int HeadlessKnight2 = 20280;
    private static final int MalrukSuccubusTuren2 = 20284;
    private static final int RoyalCaveServant2 = 20276;
    private static final int GraveLich = 21003;
    private static final int DoomServant = 21006;
    private static final int DoomArcher = 21008;
    private static final int DoomKnight = 20674;
    private static final int Kookaburra2 = 21276;
    private static final int Kookaburra3 = 21275;
    private static final int Kookaburra4 = 21274;
    private static final int Antelope2 = 21278;
    private static final int Antelope3 = 21279;
    private static final int Antelope4 = 21280;
    private static final int Bandersnatch2 = 21282;
    private static final int Bandersnatch3 = 21284;
    private static final int Bandersnatch4 = 21283;
    private static final int Buffalo2 = 21287;
    private static final int Buffalo3 = 21288;
    private static final int Buffalo4 = 21286;
    private static final int ClawsofSplendor = 21521;
    private static final int WisdomofSplendor = 21526;
    private static final int PunishmentofSplendor = 21531;
    private static final int WailingofSplendor = 21539;
    private static final int HungeredCorpse = 20954;
    private static final int BloodyGhost = 20960;
    private static final int NihilInvader = 20957;
    private static final int DarkGuard = 20959;
    private static final int[][] PROMOTE = {new int[0], new int[0], {3492, 3474, 3476, 3495, 3484, 3486}, {3473, 3485, 3491, 3475, 3483, 3494}};
    private static final int[][] EXCHANGE_LEVEL = {{30696, 3}, {30673, 3}, {30183, 3}, {30165, 2}, {30200, 2}, {30688, 2}, {30847, 1}, {30092, 1}, {30078, 1}};
    private static final int[][] DROPLIST = {{20584, 3472}, {20585, 3472}, {20587, 3472}, {20604, 3472}, {20678, 3472}, {20663, 3472}, {20583, 3482}, {20235, 3482}, {20146, 3482}, {20240, 3482}, {20245, 3482}, {20568, 3490}, {20569, 3490}, {20685, 3490}, {20572, 3490}, {20161, 3490}, {20575, 3490}};
    private static final int[] UNKNOWN = {21003, 21006, 21008, 20674, 21276, 21275, 21274, 21278, 21279, 21280, 21282, 21284, 21283, 21287, 21288, 21286, 21521, 21526, 21531, 21539, 20954, 20960, 20957, 20959};

    public _336_CoinOfMagic() {
        super(true);
        addStartNpc(30232);
        addTalkId(30232, 30702, 30696, 30183, 30200, 30165, 30847, 30092, 30078, 30688, 30673);
        for (final int[] mob : _336_CoinOfMagic.DROPLIST) {
            addKillId(mob[0]);
        }
        addKillId(_336_CoinOfMagic.UNKNOWN);
        addKillId(20645);
        addKillId(20644);
        addQuestItem(3811, 3812, 3813, 3814, 3815);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int cond = st.getCond();
        if ("30702-06.htm".equalsIgnoreCase(event)) {
            if (cond < 7) {
                st.setCond(7);
                st.playSound("ItemSound.quest_accept");
            }
        } else if ("30232-22.htm".equalsIgnoreCase(event)) {
            if (cond < 6) {
                st.setCond(6);
            }
        } else if ("30232-23.htm".equalsIgnoreCase(event)) {
            if (cond < 5) {
                st.setCond(5);
            }
        } else if ("30702-02.htm".equalsIgnoreCase(event)) {
            st.setCond(2);
        } else if ("30232-05.htm".equalsIgnoreCase(event)) {
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
            st.giveItems(3811, 1L);
            st.setCond(1);
        } else if ("30232-04.htm".equalsIgnoreCase(event) || "30232-18a.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
            st.playSound("ItemSound.quest_giveup");
        } else if ("raise".equalsIgnoreCase(event)) {
            htmltext = promote(st);
        }
        return htmltext;
    }

    private String promote(final QuestState st) {
        final int grade = st.getInt("grade");
        String html;
        if (grade == 1) {
            html = "30232-15.htm";
        } else {
            int h = 0;
            for (final int i : _336_CoinOfMagic.PROMOTE[grade]) {
                if (st.getQuestItemsCount(i) > 0L) {
                    ++h;
                }
            }
            if (h == 6) {
                for (final int i : _336_CoinOfMagic.PROMOTE[grade]) {
                    st.takeItems(i, 1L);
                }
                html = "30232-" + str((long) (19 - grade)) + ".htm";
                st.takeItems(3812 + grade, -1L);
                st.giveItems(3811 + grade, 1L);
                st.set("grade", str((long) (grade - 1)));
                if (grade == 3) {
                    st.setCond(9);
                } else if (grade == 2) {
                    st.setCond(11);
                }
                st.playSound("ItemSound.quest_fanfare_middle");
            } else {
                html = "30232-" + str((long) (16 - grade)) + ".htm";
                if (grade == 3) {
                    st.setCond(8);
                } else if (grade == 2) {
                    st.setCond(9);
                }
            }
        }
        return html;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext = "noquest";
        final int npcId = npc.getNpcId();
        final int id = st.getState();
        final int grade = st.getInt("grade");
        switch (npcId) {
            case 30232:
                if (id == 1) {
                    if (st.getPlayer().getLevel() < 40) {
                        htmltext = "30232-01.htm";
                        st.exitCurrentQuest(true);
                    } else {
                        htmltext = "30232-02.htm";
                    }
                } else if (st.getQuestItemsCount(3811) > 0L) {
                    if (st.getQuestItemsCount(3812) > 0L) {
                        st.takeItems(3812, -1L);
                        st.takeItems(3811, -1L);
                        st.giveItems(3815, 1L);
                        st.set("grade", "3");
                        st.setCond(4);
                        st.playSound("ItemSound.quest_fanfare_middle");
                        htmltext = "30232-07.htm";
                    } else {
                        htmltext = "30232-06.htm";
                    }
                } else if (grade == 3) {
                    htmltext = "30232-12.htm";
                } else if (grade == 2) {
                    htmltext = "30232-11.htm";
                } else if (grade == 1) {
                    htmltext = "30232-10.htm";
                }
                break;
            case 30702:
                if (st.getQuestItemsCount(3811) > 0L && grade == 0) {
                    htmltext = "30702-01.htm";
                } else if (grade == 3) {
                    htmltext = "30702-05.htm";
                }
                break;
            default:
                for (final int[] e : _336_CoinOfMagic.EXCHANGE_LEVEL) {
                    if (npcId == e[0] && grade <= e[1]) {
                        htmltext = npcId + "-01.htm";
                    }
                }
                break;
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int cond = st.getCond();
        final int grade = st.getInt("grade");
        final int chance = npc.getLevel() + grade * 3 - 20;
        final int npcId = npc.getNpcId();
        if (npcId == 20645 || npcId == 20644) {
            if (cond == 2 && st.rollAndGive(3812, 1, 1, 1, 10.0 * npc.getTemplate().rateHp)) {
                st.setCond(3);
            }
            return null;
        }
        for (final int[] e : _336_CoinOfMagic.DROPLIST) {
            if (e[0] == npcId) {
                st.rollAndGive(e[1], 1, (double) chance);
                return null;
            }
        }
        for (final int u : _336_CoinOfMagic.UNKNOWN) {
            if (u == npcId) {
                st.rollAndGive(_336_CoinOfMagic.BASIC_COINS[Rnd.get(_336_CoinOfMagic.BASIC_COINS.length)], 1, chance * npc.getTemplate().rateHp);
                return null;
            }
        }
        return null;
    }
}
