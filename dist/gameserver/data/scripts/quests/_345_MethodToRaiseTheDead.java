package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class _345_MethodToRaiseTheDead extends Quest {
    int VICTIMS_ARM_BONE;
    int VICTIMS_THIGH_BONE;
    int VICTIMS_SKULL;
    int VICTIMS_RIB_BONE;
    int VICTIMS_SPINE;
    int USELESS_BONE_PIECES;
    int POWDER_TO_SUMMON_DEAD_SOULS;
    int BILL_OF_IASON_HEINE;
    int CHANCE;
    int CHANCE2;

    public _345_MethodToRaiseTheDead() {
        super(false);
        VICTIMS_ARM_BONE = 4274;
        VICTIMS_THIGH_BONE = 4275;
        VICTIMS_SKULL = 4276;
        VICTIMS_RIB_BONE = 4277;
        VICTIMS_SPINE = 4278;
        USELESS_BONE_PIECES = 4280;
        POWDER_TO_SUMMON_DEAD_SOULS = 4281;
        BILL_OF_IASON_HEINE = 4310;
        CHANCE = 15;
        CHANCE2 = 50;
        addStartNpc(30970);
        addTalkId(30970);
        addTalkId(30970);
        addTalkId(30912);
        addTalkId(30973);
        addQuestItem(VICTIMS_ARM_BONE, VICTIMS_THIGH_BONE, VICTIMS_SKULL, VICTIMS_RIB_BONE, VICTIMS_SPINE, POWDER_TO_SUMMON_DEAD_SOULS);
        addKillId(20789);
        addKillId(20791);
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        switch (event) {
            case "1":
                st.setCond(1);
                st.setState(2);
                htmltext = "dorothy_the_locksmith_q0345_03.htm";
                st.playSound("ItemSound.quest_accept");
                break;
            case "2":
                st.setCond(2);
                htmltext = "dorothy_the_locksmith_q0345_07.htm";
                break;
            case "3":
                if (st.getQuestItemsCount(57) >= 1000L) {
                    st.takeItems(57, 1000L);
                    st.giveItems(POWDER_TO_SUMMON_DEAD_SOULS, 1L);
                    st.setCond(3);
                    htmltext = "magister_xenovia_q0345_03.htm";
                    st.playSound("ItemSound.quest_itemget");
                } else {
                    htmltext = "<html><head><body>You dont have enough adena!</body></html>";
                }
                break;
            case "4":
                htmltext = "medium_jar_q0345_07.htm";
                st.takeItems(POWDER_TO_SUMMON_DEAD_SOULS, -1L);
                st.takeItems(VICTIMS_ARM_BONE, -1L);
                st.takeItems(VICTIMS_THIGH_BONE, -1L);
                st.takeItems(VICTIMS_SKULL, -1L);
                st.takeItems(VICTIMS_RIB_BONE, -1L);
                st.takeItems(VICTIMS_SPINE, -1L);
                st.setCond(6);
                break;
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final int npcId = npc.getNpcId();
        String htmltext = "noquest";
        final int id = st.getState();
        final int level = st.getPlayer().getLevel();
        final int cond = st.getCond();
        final long amount = st.getQuestItemsCount(USELESS_BONE_PIECES);
        if (npcId == 30970) {
            if (id == 1) {
                if (level >= 35) {
                    htmltext = "dorothy_the_locksmith_q0345_02.htm";
                } else {
                    htmltext = "dorothy_the_locksmith_q0345_01.htm";
                    st.exitCurrentQuest(true);
                }
            } else if (cond == 1 && st.getQuestItemsCount(VICTIMS_ARM_BONE) > 0L && st.getQuestItemsCount(VICTIMS_THIGH_BONE) > 0L && st.getQuestItemsCount(VICTIMS_SKULL) > 0L && st.getQuestItemsCount(VICTIMS_RIB_BONE) > 0L && st.getQuestItemsCount(VICTIMS_SPINE) > 0L) {
                htmltext = "dorothy_the_locksmith_q0345_06.htm";
            } else if (cond == 1 && st.getQuestItemsCount(VICTIMS_ARM_BONE) + st.getQuestItemsCount(VICTIMS_THIGH_BONE) + st.getQuestItemsCount(VICTIMS_SKULL) + st.getQuestItemsCount(VICTIMS_RIB_BONE) + st.getQuestItemsCount(VICTIMS_SPINE) < 5L) {
                htmltext = "dorothy_the_locksmith_q0345_05.htm";
            } else if (cond == 7) {
                htmltext = "dorothy_the_locksmith_q0345_14.htm";
                st.setCond(1);
                st.giveItems(57, amount * 238L);
                st.giveItems(BILL_OF_IASON_HEINE, (long) (Rnd.get(7) + 1));
                st.takeItems(USELESS_BONE_PIECES, -1L);
            }
        }
        if (npcId == 30912) {
            switch (cond) {
                case 2:
                    htmltext = "magister_xenovia_q0345_01.htm";
                    st.playSound("ItemSound.quest_middle");
                    break;
                case 3:
                    htmltext = "<html><head><body>What did the urn say?</body></html>";
                    break;
                case 6:
                    htmltext = "magister_xenovia_q0345_07.htm";
                    st.setCond(7);
                    break;
            }
        }
        if (npcId == 30973 && cond == 3) {
            htmltext = "medium_jar_q0345_01.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState st) {
        final int random = Rnd.get(100);
        if (random <= CHANCE) {
            if (st.getQuestItemsCount(VICTIMS_ARM_BONE) == 0L) {
                st.giveItems(VICTIMS_ARM_BONE, 1L);
            } else if (st.getQuestItemsCount(VICTIMS_THIGH_BONE) == 0L) {
                st.giveItems(VICTIMS_THIGH_BONE, 1L);
            } else if (st.getQuestItemsCount(VICTIMS_SKULL) == 0L) {
                st.giveItems(VICTIMS_SKULL, 1L);
            } else if (st.getQuestItemsCount(VICTIMS_RIB_BONE) == 0L) {
                st.giveItems(VICTIMS_RIB_BONE, 1L);
            } else if (st.getQuestItemsCount(VICTIMS_SPINE) == 0L) {
                st.giveItems(VICTIMS_SPINE, 1L);
            }
        }
        if (random <= CHANCE2) {
            st.giveItems(USELESS_BONE_PIECES, (long) (Rnd.get(8) + 1));
        }
        return null;
    }
}
