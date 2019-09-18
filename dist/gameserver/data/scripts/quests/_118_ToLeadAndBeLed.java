package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _118_ToLeadAndBeLed extends Quest {
    private static final int PINTER = 30298;
    private static final int MAILLE_LIZARDMAN = 20919;
    private static final int BLOOD_OF_MAILLE_LIZARDMAN = 8062;
    private static final int KING_OF_THE_ARANEID = 20927;
    private static final int KING_OF_THE_ARANEID_LEG = 8063;
    private static final int D_CRY = 1458;
    private static final int D_CRY_COUNT_HEAVY = 721;
    private static final int D_CRY_COUNT_LIGHT_MAGIC = 604;
    private static final int CLAN_OATH_HELM = 7850;
    private static final int CLAN_OATH_ARMOR = 7851;
    private static final int CLAN_OATH_GAUNTLETS = 7852;
    private static final int CLAN_OATH_SABATON = 7853;
    private static final int CLAN_OATH_BRIGANDINE = 7854;
    private static final int CLAN_OATH_LEATHER_GLOVES = 7855;
    private static final int CLAN_OATH_BOOTS = 7856;
    private static final int CLAN_OATH_AKETON = 7857;
    private static final int CLAN_OATH_PADDED_GLOVES = 7858;
    private static final int CLAN_OATH_SANDALS = 7859;

    public _118_ToLeadAndBeLed() {
        super(false);
        addStartNpc(_118_ToLeadAndBeLed.PINTER);
        addKillId(_118_ToLeadAndBeLed.MAILLE_LIZARDMAN);
        addKillId(_118_ToLeadAndBeLed.KING_OF_THE_ARANEID);
        addQuestItem(_118_ToLeadAndBeLed.BLOOD_OF_MAILLE_LIZARDMAN, _118_ToLeadAndBeLed.KING_OF_THE_ARANEID_LEG);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        switch (event) {
            case "30298-02.htm":
                st.setCond(1);
                st.setState(2);
                st.playSound("ItemSound.quest_accept");
                break;
            case "30298-05a.htm":
                st.set("choose", "1");
                st.setCond(3);
                break;
            case "30298-05b.htm":
                st.set("choose", "2");
                st.setCond(4);
                break;
            case "30298-05c.htm":
                st.set("choose", "3");
                st.setCond(5);
                break;
            case "30298-08.htm":
                final int choose = st.getInt("choose");
                final int need_dcry = (choose == 1) ? _118_ToLeadAndBeLed.D_CRY_COUNT_HEAVY : _118_ToLeadAndBeLed.D_CRY_COUNT_LIGHT_MAGIC;
                if (st.getQuestItemsCount(_118_ToLeadAndBeLed.D_CRY) < need_dcry) {
                    return "30298-07.htm";
                }
                st.setCond(7);
                st.takeItems(_118_ToLeadAndBeLed.D_CRY, (long) need_dcry);
                st.playSound("ItemSound.quest_middle");
                break;
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        if (npc.getNpcId() != _118_ToLeadAndBeLed.PINTER) {
            return "noquest";
        }
        final int _state = st.getState();
        if (_state == 1) {
            if (st.getPlayer().getLevel() < 19) {
                st.exitCurrentQuest(true);
                return "30298-00.htm";
            }
            if (st.getPlayer().getClanId() == 0) {
                st.exitCurrentQuest(true);
                return "30298-00a.htm";
            }
            if (st.getPlayer().getSponsor() == 0) {
                st.exitCurrentQuest(true);
                return "30298-00b.htm";
            }
            st.setCond(0);
            return "30298-01.htm";
        } else {
            final int cond = st.getCond();
            if (cond == 1 && _state == 2) {
                return "30298-02a.htm";
            }
            if (cond == 2 && _state == 2) {
                if (st.getQuestItemsCount(_118_ToLeadAndBeLed.BLOOD_OF_MAILLE_LIZARDMAN) < 10L) {
                    st.setCond(1);
                    return "30298-02a.htm";
                }
                st.takeItems(_118_ToLeadAndBeLed.BLOOD_OF_MAILLE_LIZARDMAN, -1L);
                return "30298-04.htm";
            } else {
                if (cond == 3 && _state == 2) {
                    return "30298-05a.htm";
                }
                if (cond == 4 && _state == 2) {
                    return "30298-05b.htm";
                }
                if (cond == 5 && _state == 2) {
                    return "30298-05c.htm";
                }
                if (cond == 7 && _state == 2) {
                    return "30298-08a.htm";
                }
                if (cond != 8 || _state != 2) {
                    return "noquest";
                }
                if (st.getQuestItemsCount(_118_ToLeadAndBeLed.KING_OF_THE_ARANEID_LEG) < 8L) {
                    st.setCond(7);
                    return "30298-08a.htm";
                }
                st.takeItems(_118_ToLeadAndBeLed.KING_OF_THE_ARANEID_LEG, -1L);
                st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_HELM, 1L);
                final int choose = st.getInt("choose");
                switch (choose) {
                    case 1:
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_ARMOR, 1L);
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_GAUNTLETS, 1L);
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_SABATON, 1L);
                        break;
                    case 2:
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_BRIGANDINE, 1L);
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_LEATHER_GLOVES, 1L);
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_BOOTS, 1L);
                        break;
                    default:
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_AKETON, 1L);
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_PADDED_GLOVES, 1L);
                        st.giveItems(_118_ToLeadAndBeLed.CLAN_OATH_SANDALS, 1L);
                        break;
                }
                st.unset("cond");
                st.playSound("ItemSound.quest_finish");
                st.exitCurrentQuest(false);
                return "30298-09.htm";
            }
        }
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        final int cond = st.getCond();
        if (npcId == _118_ToLeadAndBeLed.MAILLE_LIZARDMAN && st.getQuestItemsCount(_118_ToLeadAndBeLed.BLOOD_OF_MAILLE_LIZARDMAN) < 10L && cond == 1 && Rnd.chance(50)) {
            st.giveItems(_118_ToLeadAndBeLed.BLOOD_OF_MAILLE_LIZARDMAN, 1L);
            if (st.getQuestItemsCount(_118_ToLeadAndBeLed.BLOOD_OF_MAILLE_LIZARDMAN) == 10L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(2);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        } else if (npcId == _118_ToLeadAndBeLed.KING_OF_THE_ARANEID && st.getQuestItemsCount(_118_ToLeadAndBeLed.KING_OF_THE_ARANEID_LEG) < 8L && cond == 7 && Rnd.chance(50)) {
            st.giveItems(_118_ToLeadAndBeLed.KING_OF_THE_ARANEID_LEG, 1L);
            if (st.getQuestItemsCount(_118_ToLeadAndBeLed.KING_OF_THE_ARANEID_LEG) == 8L) {
                st.playSound("ItemSound.quest_middle");
                st.setCond(8);
            } else {
                st.playSound("ItemSound.quest_itemget");
            }
        }
        return null;
    }
}
