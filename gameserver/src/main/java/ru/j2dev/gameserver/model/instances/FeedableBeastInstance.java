package ru.j2dev.gameserver.model.instances;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FeedableBeastInstance extends MonsterInstance {
    public static final TIntObjectHashMap<growthInfo> growthCapableMobs = new TIntObjectHashMap<>();
    public static final TIntArrayList tamedBeasts = new TIntArrayList();
    public static final TIntArrayList feedableBeasts = new TIntArrayList();
    public static final Map<Integer, Integer> feedInfo = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcInstance.class);
    private static final int SKILL_GOLDEN_SPICE = 2188;
    private static final int SKILL_CRYSTAL_SPICE = 2189;
    private static int GOLDEN_SPICE;
    private static int CRYSTAL_SPICE = 1;

    static {
        growthCapableMobs.put(21451, new growthInfo(0, new int[][]{{21452, 21453, 21454, 21455}, {21456, 21457, 21458, 21459}}, 100));
        growthCapableMobs.put(21452, new growthInfo(1, new int[][]{{21460, 21462}, new int[0]}, 40));
        growthCapableMobs.put(21453, new growthInfo(1, new int[][]{{21461, 21463}, new int[0]}, 40));
        growthCapableMobs.put(21454, new growthInfo(1, new int[][]{{21460, 21462}, new int[0]}, 40));
        growthCapableMobs.put(21455, new growthInfo(1, new int[][]{{21461, 21463}, new int[0]}, 40));
        growthCapableMobs.put(21456, new growthInfo(1, new int[][]{new int[0], {21464, 21466}}, 40));
        growthCapableMobs.put(21457, new growthInfo(1, new int[][]{new int[0], {21465, 21467}}, 40));
        growthCapableMobs.put(21458, new growthInfo(1, new int[][]{new int[0], {21464, 21466}}, 40));
        growthCapableMobs.put(21459, new growthInfo(1, new int[][]{new int[0], {21465, 21467}}, 40));
        growthCapableMobs.put(21460, new growthInfo(2, new int[][]{{21468, 16017}, new int[0]}, 25));
        growthCapableMobs.put(21461, new growthInfo(2, new int[][]{{21469, 16018}, new int[0]}, 25));
        growthCapableMobs.put(21462, new growthInfo(2, new int[][]{{21468, 16017}, new int[0]}, 25));
        growthCapableMobs.put(21463, new growthInfo(2, new int[][]{{21469, 16018}, new int[0]}, 25));
        growthCapableMobs.put(21464, new growthInfo(2, new int[][]{new int[0], {21468, 16017}}, 25));
        growthCapableMobs.put(21465, new growthInfo(2, new int[][]{new int[0], {21469, 16018}}, 25));
        growthCapableMobs.put(21466, new growthInfo(2, new int[][]{new int[0], {21468, 16017}}, 25));
        growthCapableMobs.put(21467, new growthInfo(2, new int[][]{new int[0], {21469, 16018}}, 25));
        growthCapableMobs.put(21470, new growthInfo(0, new int[][]{{21472, 21474, 21471, 21473}, {21475, 21476, 21477, 21478}}, 100));
        growthCapableMobs.put(21471, new growthInfo(1, new int[][]{{21479, 21481}, new int[0]}, 40));
        growthCapableMobs.put(21472, new growthInfo(1, new int[][]{{21480, 21482}, new int[0]}, 40));
        growthCapableMobs.put(21473, new growthInfo(1, new int[][]{{21479, 21481}, new int[0]}, 40));
        growthCapableMobs.put(21474, new growthInfo(1, new int[][]{{21480, 21482}, new int[0]}, 40));
        growthCapableMobs.put(21475, new growthInfo(1, new int[][]{new int[0], {21483, 21485}}, 40));
        growthCapableMobs.put(21476, new growthInfo(1, new int[][]{new int[0], {21484, 21486}}, 40));
        growthCapableMobs.put(21477, new growthInfo(1, new int[][]{new int[0], {21483, 21485}}, 40));
        growthCapableMobs.put(21478, new growthInfo(1, new int[][]{new int[0], {21484, 21486}}, 40));
        growthCapableMobs.put(21479, new growthInfo(2, new int[][]{{21487, 16014}, new int[0]}, 25));
        growthCapableMobs.put(21480, new growthInfo(2, new int[][]{{21488, 16013}, new int[0]}, 25));
        growthCapableMobs.put(21481, new growthInfo(2, new int[][]{{21487, 16014}, new int[0]}, 25));
        growthCapableMobs.put(21482, new growthInfo(2, new int[][]{{21488, 16013}, new int[0]}, 25));
        growthCapableMobs.put(21483, new growthInfo(2, new int[][]{new int[0], {21487, 16014}}, 25));
        growthCapableMobs.put(21484, new growthInfo(2, new int[][]{new int[0], {21488, 16013}}, 25));
        growthCapableMobs.put(21485, new growthInfo(2, new int[][]{new int[0], {21487, 16014}}, 25));
        growthCapableMobs.put(21486, new growthInfo(2, new int[][]{new int[0], {21488, 16013}}, 25));
        growthCapableMobs.put(21489, new growthInfo(0, new int[][]{{21491, 21493, 21490, 21492}, {21495, 21497, 21494, 21496}}, 100));
        growthCapableMobs.put(21490, new growthInfo(1, new int[][]{{21498, 21500}, new int[0]}, 40));
        growthCapableMobs.put(21491, new growthInfo(1, new int[][]{{21499, 21501}, new int[0]}, 40));
        growthCapableMobs.put(21492, new growthInfo(1, new int[][]{{21498, 21500}, new int[0]}, 40));
        growthCapableMobs.put(21493, new growthInfo(1, new int[][]{{21499, 21501}, new int[0]}, 40));
        growthCapableMobs.put(21494, new growthInfo(1, new int[][]{new int[0], {21502, 21504}}, 40));
        growthCapableMobs.put(21495, new growthInfo(1, new int[][]{new int[0], {21503, 21505}}, 40));
        growthCapableMobs.put(21496, new growthInfo(1, new int[][]{new int[0], {21502, 21504}}, 40));
        growthCapableMobs.put(21497, new growthInfo(1, new int[][]{new int[0], {21503, 21505}}, 40));
        growthCapableMobs.put(21498, new growthInfo(2, new int[][]{{21506, 16015}, new int[0]}, 25));
        growthCapableMobs.put(21499, new growthInfo(2, new int[][]{{21507, 16016}, new int[0]}, 25));
        growthCapableMobs.put(21500, new growthInfo(2, new int[][]{{21506, 16015}, new int[0]}, 25));
        growthCapableMobs.put(21501, new growthInfo(2, new int[][]{{21507, 16015}, new int[0]}, 25));
        growthCapableMobs.put(21502, new growthInfo(2, new int[][]{new int[0], {21506, 16015}}, 25));
        growthCapableMobs.put(21503, new growthInfo(2, new int[][]{new int[0], {21507, 16016}}, 25));
        growthCapableMobs.put(21504, new growthInfo(2, new int[][]{new int[0], {21506, 16015}}, 25));
        growthCapableMobs.put(21505, new growthInfo(2, new int[][]{new int[0], {21507, 16016}}, 25));
        for (int i = 16013; i <= 16018; ++i) {
            tamedBeasts.add(i);
        }
        for (int i = 16013; i <= 16019; ++i) {
            feedableBeasts.add(i);
        }
        for (int i = 21451; i <= 21507; ++i) {
            feedableBeasts.add(i);
        }
        for (int i = 21824; i <= 21829; ++i) {
            feedableBeasts.add(i);
        }
    }

    public FeedableBeastInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    private boolean isGoldenSpice(final int skillId) {
        return skillId == 2188;
    }

    private boolean isCrystalSpice(final int skillId) {
        return skillId == 2189;
    }

    private int getFoodSpice(final int skillId) {
        if (isGoldenSpice(skillId)) {
            return 6643;
        }
        return 6644;
    }

    public int getItemIdBySkillId(final int skillId) {
        int itemId;
        switch (skillId) {
            case 2188: {
                itemId = 6643;
                break;
            }
            case 2189: {
                itemId = 6644;
                break;
            }
            default: {
                itemId = 0;
                break;
            }
        }
        return itemId;
    }

    private void spawnNext(final Player player, final int growthLevel, final int food) {
        final int npcId = getNpcId();
        int nextNpcId;
        nextNpcId = growthCapableMobs.get(npcId).spice[food][Rnd.get(growthCapableMobs.get(npcId).spice[food].length)];
        feedInfo.remove(getObjectId());
        if (growthCapableMobs.get(npcId).growth_level == 0) {
            onDecay();
        } else {
            deleteMe();
        }
        if (tamedBeasts.contains(nextNpcId)) {
            if (player.getTrainedBeast() != null) {
                player.getTrainedBeast().doDespawn();
            }
            final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(nextNpcId);
            final TamedBeastInstance nextNpc = new TamedBeastInstance(IdFactory.getInstance().getNextId(), template);
            final Location loc = player.getLoc();
            loc.x += Rnd.get(-50, 50);
            loc.y += Rnd.get(-50, 50);
            nextNpc.spawnMe(loc);
            nextNpc.setTameType(player);
            nextNpc.setFoodType(getFoodSpice((food == GOLDEN_SPICE) ? SKILL_GOLDEN_SPICE : SKILL_CRYSTAL_SPICE));
            nextNpc.setRunning();
            nextNpc.setOwner(player);
            QuestState st = player.getQuestState("_020_BringUpWithLove");
            if (st != null && !st.isCompleted() && Rnd.chance(5) && st.getQuestItemsCount(7185) == 0L) {
                st.giveItems(7185, 1L);
                st.setCond(2);
            }
            st = player.getQuestState("_655_AGrandPlanForTamingWildBeasts");
            if (st != null && !st.isCompleted() && st.getCond() == 1 && st.getQuestItemsCount(8084) < 10L) {
                st.giveItems(8084, 1L);
            }
        } else {
            final MonsterInstance nextNpc2 = spawn(nextNpcId, getX(), getY(), getZ());
            feedInfo.put(nextNpc2.getObjectId(), player.getObjectId());
            player.setObjectTarget(nextNpc2);
            ThreadPoolManager.getInstance().schedule(new AggrPlayer(nextNpc2, player), 3000L);
        }
    }

    @Override
    protected void onDeath(final Creature killer) {
        feedInfo.remove(getObjectId());
        super.onDeath(killer);
    }

    public MonsterInstance spawn(final int npcId, final int x, final int y, final int z) {
        try {
            final MonsterInstance monster = (MonsterInstance) NpcTemplateHolder.getInstance().getTemplate(npcId).getInstanceConstructor().newInstance(IdFactory.getInstance().getNextId(), NpcTemplateHolder.getInstance().getTemplate(npcId));
            monster.setSpawnedLoc(new Location(x, y, z));
            monster.spawnMe(monster.getSpawnedLoc());
            return monster;
        } catch (Exception e) {
            LOGGER.error("Could not spawn Npc " + npcId, e);
            return null;
        }
    }

    public void onSkillUse(final Player player, final int skillId) {
        final int npcId = getNpcId();
        if (!feedableBeasts.contains(npcId)) {
            return;
        }
        if (isGoldenSpice(skillId) && isCrystalSpice(skillId)) {
            return;
        }
        final int food = isGoldenSpice(skillId) ? 0 : 1;
        final int objectId = getObjectId();
        broadcastPacket(new SocialAction(objectId, 2));
        if (growthCapableMobs.containsKey(npcId)) {
            if (growthCapableMobs.get(npcId).spice[food].length == 0) {
                return;
            }
            final int growthLevel = growthCapableMobs.get(npcId).growth_level;
            if (growthLevel > 0 && feedInfo.get(objectId) != null && feedInfo.get(objectId) != player.getObjectId()) {
                return;
            }
            if (Rnd.chance(growthCapableMobs.get(npcId).growth_chance)) {
                spawnNext(player, growthLevel, food);
            }
        } else if (Rnd.chance(60)) {
            dropItem(player, getItemIdBySkillId(skillId), 1L);
        }
    }

    private static class growthInfo {
        public final int growth_level;
        public final int growth_chance;
        public final int[][] spice;

        public growthInfo(final int level, final int[][] sp, final int chance) {
            growth_level = level;
            spice = sp;
            growth_chance = chance;
        }
    }

    public static class AggrPlayer extends RunnableImpl {
        private final NpcInstance _actor;
        private final Player _killer;

        public AggrPlayer(final NpcInstance actor, final Player killer) {
            _actor = actor;
            _killer = killer;
        }

        @Override
        public void runImpl() {
            _actor.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, _killer, 1000);
        }
    }
}
