package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.manager.RaidBossSpawnManager;
import ru.j2dev.gameserver.model.AggroList.HateInfo;
import ru.j2dev.gameserver.model.CommandChannel;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.DeleteTask;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

public class RaidBossInstance extends MonsterInstance {
    private static final long serialVersionUID = 1L;
    private static final int MINION_UNSPAWN_INTERVAL = 5000;
    private ScheduledFuture<?> minionMaintainTask;

    public RaidBossInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public boolean isRaid() {
        return true;
    }

    protected int getMinionUnspawnInterval() {
        return MINION_UNSPAWN_INTERVAL;
    }

    protected int getKilledInterval(final MinionInstance minion) {
        return Config.MINIONS_RESPAWN_INTERVAL;
    }

    @Override
    public void notifyMinionDied(final MinionInstance minion) {
        minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MaintainKilledMinion(minion), getKilledInterval(minion));
        super.notifyMinionDied(minion);
    }

    @Override
    protected void onDeath(final Creature killer) {
        if (minionMaintainTask != null) {
            minionMaintainTask.cancel(false);
            minionMaintainTask = null;
        }
        final int points = getTemplate().rewardRp;
        if (points > 0) {
            calcRaidPointsReward(points);
        }
        if (this instanceof ReflectionBossInstance) {
            super.onDeath(killer);
            return;
        }
        if (killer.isPlayable()) {
            final Player player = killer.getPlayer();
            if (player.isInParty()) {
                for (final Player member : player.getParty().getPartyMembers()) {
                    if (member.isNoble()) {
                        HeroManager.getInstance().addHeroDiary(member.getObjectId(), 1, getNpcId());
                    }
                }
                player.getParty().broadCast(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
            } else {
                if (player.isNoble()) {
                    HeroManager.getInstance().addHeroDiary(player.getObjectId(), 1, getNpcId());
                }
                player.sendPacket(Msg.CONGRATULATIONS_YOUR_RAID_WAS_SUCCESSFUL);
            }
            final Quest q = QuestManager.getQuest(508);
            if (q != null) {
                final String qn = q.getName();
                if (player.getClan() != null && player.getClan().getLeader().isOnline() && player.getClan().getLeader().getPlayer().getQuestState(qn) != null) {
                    final QuestState st = player.getClan().getLeader().getPlayer().getQuestState(qn);
                    st.getQuest().onKill(this, st);
                }
            }
        }
        if (getMinionList().hasAliveMinions()) {
            ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                @Override
                public void runImpl() {
                    if (isDead()) {
                        getMinionList().unspawnMinions();
                    }
                }
            }, getMinionUnspawnInterval());
        }
        int boxId = 0;
        switch (getNpcId()) {
            case 25035: {
                boxId = 31027;
                break;
            }
            case 25054: {
                boxId = 31028;
                break;
            }
            case 25126: {
                boxId = 31029;
                break;
            }
            case 25220: {
                boxId = 31030;
                break;
            }
        }
        if (boxId != 0) {
            final NpcTemplate boxTemplate = NpcTemplateHolder.getInstance().getTemplate(boxId);
            if (boxTemplate != null) {
                final NpcInstance box = new NpcInstance(IdFactory.getInstance().getNextId(), boxTemplate);
                box.spawnMe(getLoc());
                box.setSpawnedLoc(getLoc());
                ThreadPoolManager.getInstance().schedule(new DeleteTask(box), 60000L);
            }
        }
        super.onDeath(killer);
    }

    @SuppressWarnings("unchecked")
    private void calcRaidPointsReward(final int totalPoints) {
        final Map<Object, Object[]> participants = new HashMap<>();
        final double totalHp = getMaxHp();
        for (final HateInfo ai : getAggroList().getPlayableMap().values()) {
            final Player player = ai.attacker.getPlayer();
            final Object key = (player.getParty() != null) ? ((player.getParty().getCommandChannel() != null) ? player.getParty().getCommandChannel() : player.getParty()) : player.getPlayer();
            Object[] info = participants.computeIfAbsent(key, k -> new Object[]{new HashSet(), 0L});
            if (key instanceof CommandChannel) {
                for (final Player p : (CommandChannel) key) {
                    if (p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
                        ((Set<Player>) info[0]).add(p);
                    }
                }
            } else if (key instanceof Party) {
                for (final Player p : ((Party) key).getPartyMembers()) {
                    if (p.isInRangeZ(this, Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
                        ((Set<Player>) info[0]).add(p);
                    }
                }
            } else {
                ((Set<Player>) info[0]).add(player);
            }
            info[1] = (long) info[1] + ai.damage;
        }
        for (final Object[] groupInfo : participants.values()) {
            final Set<Player> players = (HashSet<Player>) groupInfo[0];
            final int perPlayer = (int) Math.round(totalPoints * (long) groupInfo[1] / (totalHp * players.size()));
            for (final Player player2 : players) {
                int playerReward = perPlayer;
                playerReward = (int) Math.round(playerReward * Experience.penaltyModifier(calculateLevelDiffForDrop(player2.getLevel()), 9.0));
                if (playerReward == 0) {
                    continue;
                }
                player2.sendPacket(new SystemMessage(1725).addNumber(playerReward));
                RaidBossSpawnManager.getInstance().addPoints(player2.getObjectId(), getNpcId(), playerReward);
            }
        }
        RaidBossSpawnManager.getInstance().updatePointsDb();
        RaidBossSpawnManager.getInstance().calculateRanking();
    }

    @Override
    protected void onDecay() {
        super.onDecay();
        RaidBossSpawnManager.getInstance().onBossDespawned(this);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        broadcastPacketToOthers(new PlaySound(PlaySound.Type.MUSIC, getParameters().getString("RaidSpawnMusic", "Rm01_A"), 0, getObjectId(), getLoc()));
        addSkill(SkillTable.getInstance().getInfo(4045, 1));
        RaidBossSpawnManager.getInstance().onBossSpawned(this);
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }

    @Override
    public boolean isLethalImmune() {
        return true;
    }

    @Override
    public boolean hasRandomWalk() {
        return false;
    }

    @Override
    public boolean canChampion() {
        return false;
    }

    private class MaintainKilledMinion extends RunnableImpl {
        private final MinionInstance minion;

        MaintainKilledMinion(final MinionInstance minion) {
            this.minion = minion;
        }

        @Override
        public void runImpl() {
            if (!isDead()) {
                minion.refreshID();
                spawnMinion(minion);
            }
        }
    }
}
