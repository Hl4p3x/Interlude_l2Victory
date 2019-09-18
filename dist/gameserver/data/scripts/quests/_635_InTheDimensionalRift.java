package quests;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.utils.Location;

public class _635_InTheDimensionalRift extends Quest {
    private static final int DIMENSION_FRAGMENT = 7079;
    private static final int[][] COORD = {new int[0], {-41572, 209731, -5087}, {42950, 143934, -5381}, {45256, 123906, -5411}, {46192, 170290, -4981}, {111273, 174015, -5437}, {-20221, -250795, -8160}, {-21726, 77385, -5171}, {140405, 79679, -5427}, {-52366, 79097, -4741}, {118311, 132797, -4829}, {172185, -17602, -4901}, {83000, 209213, -5439}, {-19500, 13508, -4901}, {113865, 84543, -6541}};

    public _635_InTheDimensionalRift() {
        super(false);
        for (int npcId = 31494; npcId < 31508; ++npcId) {
            addStartNpc(npcId);
        }
        for (int npcId = 31095; npcId <= 31126; ++npcId) {
            if (npcId != 31111 && npcId != 31112 && npcId != 31113) {
                addStartNpc(npcId);
            }
        }
        for (int npcId = 31488; npcId < 31494; ++npcId) {
            addTalkId(npcId);
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        String htmltext = event;
        final int id = st.getInt("id");
        final String loc = st.get("loc");
        if ("5.htm".equals(event)) {
            if (id > 0 || loc != null) {
                if (isZiggurat(st.getPlayer().getLastNpc().getNpcId()) && !takeAdena(st)) {
                    htmltext = "Sorry...";
                    st.exitCurrentQuest(true);
                    return htmltext;
                }
                st.setState(2);
                st.setCond(1);
                st.getPlayer().teleToLocation(-114790, -180576, -6781);
            } else {
                htmltext = "What are you trying to do?";
                st.exitCurrentQuest(true);
            }
        } else if ("6.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        String htmltext;
        final int npcId = npc.getNpcId();
        final int id = st.getInt("id");
        final String loc = st.get("loc");
        if (isZiggurat(npcId) || isKeeper(npcId)) {
            if (st.getPlayer().getLevel() < 20) {
                st.exitCurrentQuest(true);
                htmltext = "1.htm";
            } else if (st.getQuestItemsCount(7079) == 0L) {
                if (isKeeper(npcId)) {
                    htmltext = "3.htm";
                } else {
                    htmltext = "3-ziggurat.htm";
                }
            } else {
                st.set("loc", st.getPlayer().getLoc().toString());
                if (isKeeper(npcId)) {
                    htmltext = "4.htm";
                } else {
                    htmltext = "4-ziggurat.htm";
                }
            }
        } else if (id > 0) {
            final int[] coord = _635_InTheDimensionalRift.COORD[id];
            st.getPlayer().teleToLocation(coord[0], coord[1], coord[2]);
            htmltext = "7.htm";
            st.exitCurrentQuest(true);
        } else if (loc != null) {
            st.getPlayer().teleToLocation(Location.parseLoc(loc));
            htmltext = "7.htm";
            st.exitCurrentQuest(true);
        } else {
            htmltext = "Where are you from?";
            st.exitCurrentQuest(true);
        }
        return htmltext;
    }

    private boolean takeAdena(final QuestState st) {
        final int level = st.getPlayer().getLevel();
        int fee;
        if (level < 30) {
            fee = 2000;
        } else if (level < 40) {
            fee = 4500;
        } else if (level < 50) {
            fee = 8000;
        } else if (level < 60) {
            fee = 12500;
        } else if (level < 70) {
            fee = 18000;
        } else {
            fee = 24500;
        }
        if (!st.getPlayer().reduceAdena((long) fee, true)) {
            st.getPlayer().sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            return false;
        }
        return true;
    }

    private boolean isZiggurat(final int id) {
        return id >= 31095 && id <= 31126;
    }

    private boolean isKeeper(final int id) {
        return id >= 31494 && id <= 31508;
    }
}
