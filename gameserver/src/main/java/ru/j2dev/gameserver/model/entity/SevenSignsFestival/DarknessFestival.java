package ru.j2dev.gameserver.model.entity.SevenSignsFestival;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.instances.FestivalMonsterInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.Arrays;
import java.util.concurrent.Future;

public class DarknessFestival extends Reflection {
    public static final int FESTIVAL_LENGTH = 1080000;
    public static final int FESTIVAL_FIRST_SPAWN = 60000;
    public static final int FESTIVAL_SECOND_SPAWN = 540000;
    public static final int FESTIVAL_CHEST_SPAWN = 900000;
    private static final Logger LOGGER = LoggerFactory.getLogger(DarknessFestival.class);

    private final int _levelRange;
    private final int _cabal;
    private final FestivalSpawn _witchSpawn;
    private final FestivalSpawn _startLocation;
    private int currentState;
    private boolean _challengeIncreased;
    private Future<?> _spawnTimerTask;

    public DarknessFestival(final Party party, final int cabal, final int level) {
        currentState = 0;
        _challengeIncreased = false;
        onCreate();
        setName("Darkness Festival");
        setParty(party);
        _levelRange = level;
        _cabal = cabal;
        startCollapseTimer(1140000L);
        if (cabal == 2) {
            _witchSpawn = new FestivalSpawn(FestivalSpawn.FESTIVAL_DAWN_WITCH_SPAWNS[_levelRange]);
            _startLocation = new FestivalSpawn(FestivalSpawn.FESTIVAL_DAWN_PLAYER_SPAWNS[_levelRange]);
        } else {
            _witchSpawn = new FestivalSpawn(FestivalSpawn.FESTIVAL_DUSK_WITCH_SPAWNS[_levelRange]);
            _startLocation = new FestivalSpawn(FestivalSpawn.FESTIVAL_DUSK_PLAYER_SPAWNS[_levelRange]);
        }
        party.setReflection(this);
        setReturnLoc(party.getPartyLeader().getLoc());
        party.getPartyMembers().forEach(p -> {
            p.setVar("backCoords", p.getLoc().toXYZString(), -1L);
            p.getEffectList().stopAllEffects();
            p.teleToLocation(Location.findPointToStay(_startLocation.loc, 20, 100, getGeoIndex()), this);
        });
        scheduleNext();
        final NpcTemplate witchTemplate = NpcTemplateHolder.getInstance().getTemplate(_witchSpawn.npcId);
        try {
            final SimpleSpawner npcSpawn = new SimpleSpawner(witchTemplate);
            npcSpawn.setLoc(_witchSpawn.loc);
            npcSpawn.setReflection(this);
            addSpawn(npcSpawn);
            npcSpawn.doSpawn(true);
        } catch (Exception e) {
            LOGGER.error("", e);
        }
        sendMessageToParticipants("The festival will begin in 1 minute.");
    }

    private void scheduleNext() {
        switch (currentState) {
            case 0: {
                currentState = 60000;
                _spawnTimerTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        spawnFestivalMonsters(60, 0);
                        sendMessageToParticipants("Go!");
                        scheduleNext();
                    }
                }, 60000L);
                break;
            }
            case 60000: {
                currentState = 540000;
                _spawnTimerTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        spawnFestivalMonsters(60, 2);
                        sendMessageToParticipants("Next wave arrived!");
                        scheduleNext();
                    }
                }, 480000L);
                break;
            }
            case 540000: {
                currentState = 900000;
                _spawnTimerTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        spawnFestivalMonsters(60, 3);
                        sendMessageToParticipants("The chests have spawned! Be quick, the festival will end soon.");
                    }
                }, 360000L);
                break;
            }
        }
    }

    public void spawnFestivalMonsters(final int respawnDelay, final int spawnType) {
        int[][] spawns = null;
        switch (spawnType) {
            case 0:
            case 1: {
                spawns = ((_cabal == 2) ? FestivalSpawn.FESTIVAL_DAWN_PRIMARY_SPAWNS[_levelRange] : FestivalSpawn.FESTIVAL_DUSK_PRIMARY_SPAWNS[_levelRange]);
                break;
            }
            case 2: {
                spawns = ((_cabal == 2) ? FestivalSpawn.FESTIVAL_DAWN_SECONDARY_SPAWNS[_levelRange] : FestivalSpawn.FESTIVAL_DUSK_SECONDARY_SPAWNS[_levelRange]);
                break;
            }
            case 3: {
                spawns = ((_cabal == 2) ? FestivalSpawn.FESTIVAL_DAWN_CHEST_SPAWNS[_levelRange] : FestivalSpawn.FESTIVAL_DUSK_CHEST_SPAWNS[_levelRange]);
                break;
            }
        }
        if (spawns != null) {
            Arrays.stream(spawns).map(FestivalSpawn::new).forEach(currSpawn -> {
                final NpcTemplate npcTemplate = NpcTemplateHolder.getInstance().getTemplate(currSpawn.npcId);
                final SimpleSpawner npcSpawn = new SimpleSpawner(npcTemplate);
                npcSpawn.setReflection(this);
                npcSpawn.setLoc(currSpawn.loc);
                npcSpawn.setHeading(Rnd.get(65536));
                npcSpawn.setAmount(1);
                npcSpawn.setRespawnDelay(respawnDelay);
                npcSpawn.startRespawn();
                final FestivalMonsterInstance festivalMob = (FestivalMonsterInstance) npcSpawn.doSpawn(true);
                if (spawnType == 1) {
                    festivalMob.setOfferingBonus(2);
                } else if (spawnType == 3) {
                    festivalMob.setOfferingBonus(5);
                }
                addSpawn(npcSpawn);
            });
        }
    }

    public boolean increaseChallenge() {
        if (_challengeIncreased) {
            return false;
        }
        _challengeIncreased = true;
        spawnFestivalMonsters(60, 1);
        return true;
    }

    @Override
    public void collapse() {
        if (isCollapseStarted()) {
            return;
        }
        if (_spawnTimerTask != null) {
            _spawnTimerTask.cancel(false);
            _spawnTimerTask = null;
        }
        if (SevenSigns.getInstance().getCurrentPeriod() == 1 && getParty() != null) {
            final Player player = getParty().getPartyLeader();
            final ItemInstance bloodOfferings = player.getInventory().getItemByItemId(5901);
            final long offeringCount = (bloodOfferings == null) ? 0L : bloodOfferings.getCount();
            if (player.getInventory().destroyItem(bloodOfferings)) {
                final boolean isHighestScore = SevenSignsFestival.getInstance().setFinalScore(getParty(), _cabal, _levelRange, offeringCount);
                player.sendPacket(new SystemMessage(1267).addNumber(offeringCount));
                sendCustomMessageToParticipants("l2p.gameserver.model.entity.SevenSignsFestival.Ended");
                if (isHighestScore) {
                    sendMessageToParticipants("Your score is highest!");
                }
            } else {
                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.BloodOfferings", player));
            }
        }
        super.collapse();
    }

    private void sendMessageToParticipants(final String s) {
        for (final Player p : getPlayers()) {
            p.sendMessage(s);
        }
    }

    private void sendCustomMessageToParticipants(final String s) {
        for (final Player p : getPlayers()) {
            p.sendMessage(new CustomMessage(s, p));
        }
    }

    public void partyMemberExited() {
        if (getParty() == null || getParty().getMemberCount() <= 1) {
            collapse();
        }
    }

    @Override
    public boolean canChampions() {
        return true;
    }

    @Override
    public boolean isAutolootForced() {
        return true;
    }
}
