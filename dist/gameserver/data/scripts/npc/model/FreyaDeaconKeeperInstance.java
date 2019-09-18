package npc.model;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.lang.ref.SoftReference;
import java.util.concurrent.Future;

public final class FreyaDeaconKeeperInstance extends NpcInstance {
    private static final int SILVER_HEMOCYTE_ITEM_ID = 8057;
    private static final long SILVER_HEMOCYTE_ITEM_COUNT = 10L;
    private static final int PETRIFICATION_SCROLL_ITEM_ID = 8379;
    private static final long PETRIFICATION_SCROLL_ITEM_COUNT = 3L;
    private static final String fnHi = "freya_deacon001.htm";
    private static final String fnHi2 = "freya_deacon002.htm";
    private static final String ICE_FAIRY_SIRR_MAKER = "[schuttgart13_mb2314_05m1]";
    private static final String[] FREYA_RELATIVE_MAKERS = {"[schuttgart13_mb2314_01m1]", "[schuttgart13_mb2314_02m1]", "[schuttgart13_mb2314_03m1]", "[schuttgart13_mb2314_04m1]"};
    private static final Location ICE_CASTLE_LOC = new Location(113533, -126159, -3488);
    private static final Location BACK_PORT_LOC = new Location(115792, -125760, -3373);
    private static final int BUFF_ID = 4479;
    private static final int BUFF_LVL = 1;
    private static final int ICE_BARRIER_001 = 23140001;
    private static final int ICE_BARRIER_002 = 23140002;
    private static final int SCE_NPC_ALL_DIE = 10025;
    private static final int SCE_OPEN_DOOR = 10026;
    private static final int SCE_CLOSE_DOOR = 10027;
    private static final int SCE_GIVE_BUFF = 11038;
    private static final int SCE_PRIVATE_DESPAWN = 11039;
    private static final int SCE_SAY_PARTY = 11040;
    private static final String ICE_CASTLE_ZONE_NAME = "[ice_fairy_sirr_epic]";

    private final Listner _listner;
    private Future<?> _nextTask;
    private SoftReference<Party> _activePartyRef;
    private int _activePartyMemberCount;

    public FreyaDeaconKeeperInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _listner = new Listner();
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("request_entrance_freya_castle".equalsIgnoreCase(command)) {
            tryEnterIceCastle(player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private synchronized void tryEnterIceCastle(final Player player) {
        if (getActiveParty() == null) {
            final Party party = player.getParty();
            if (party == null) {
                showChatWindow(player, 6);
                return;
            }
            if (party.getPartyLeader() != player) {
                showChatWindow(player, 2);
                return;
            }
            for (final Player partyMember : party.getPartyMembers()) {
                if (partyMember.getInventory().getCountOf(8057) < 10L) {
                    showChatWindow(player, 3, "<?name?>", partyMember.getName());
                    return;
                }
            }
            if (!player.isQuestContinuationPossible(true)) {
                showChatWindow(player, 4);
                return;
            }
            for (final Player partyMember : party.getPartyMembers()) {
                Functions.removeItem(partyMember, 8057, 10L);
            }
            enterIceCastle(party);
            showChatWindow(player, 5);
            Functions.npcShoutCustomMessage(this, "scripts.ice_castle.1121005");
        } else {
            showChatWindow(player, 1);
        }
    }

    private void clear() {
        openTheDoors();
        if (_activePartyRef != null) {
            _activePartyRef.clear();
            _activePartyRef = null;
        }
        _activePartyMemberCount = 0;
        if (_nextTask != null) {
            _nextTask.cancel(false);
            _nextTask = null;
        }
    }

    private void clearZone() {
        final Zone iceCastleZone = ReflectionUtils.getZone(FreyaDeaconKeeperInstance.ICE_CASTLE_ZONE_NAME);
        if (iceCastleZone != null) {
            for (final Player player : iceCastleZone.getInsidePlayers()) {
                player.teleToLocation(FreyaDeaconKeeperInstance.BACK_PORT_LOC);
            }
        }
    }

    private void closeTheDoors() {
        for (final DoorInstance door : ReflectionUtils.getDoors(23140001, 23140002)) {
            door.closeMe();
        }
    }

    private void openTheDoors() {
        for (final DoorInstance door : ReflectionUtils.getDoors(23140001, 23140002)) {
            door.openMe();
        }
    }

    private Party getActiveParty() {
        if (_activePartyRef == null) {
            return null;
        }
        return _activePartyRef.get();
    }

    private void spawnSirr() {
        for (final Spawner spawner : SpawnManager.getInstance().getSpawners("[schuttgart13_mb2314_05m1]")) {
            final NpcInstance sirr = spawner.doSpawn(true);
            spawner.stopRespawn();
            if (sirr.isMonster()) {
                sirr.addListener((Listener) _listner);
            }
        }
    }

    private void despawnSirr() {
        for (final Spawner spawner : SpawnManager.getInstance().getSpawners("[schuttgart13_mb2314_05m1]")) {
            for (final NpcInstance sirr : spawner.getAllSpawned()) {
                if (sirr.isMonster()) {
                    sirr.removeListener((Listener) _listner);
                }
            }
            spawner.deleteAll();
        }
    }

    private void spawnFreyaMobs() {
        for (final String makers : FreyaDeaconKeeperInstance.FREYA_RELATIVE_MAKERS) {
            for (final Spawner spawner : SpawnManager.getInstance().getSpawners(makers)) {
                spawner.init();
                spawner.stopRespawn();
            }
        }
    }

    private void despawnFreyaMobs() {
        for (final String makers : FreyaDeaconKeeperInstance.FREYA_RELATIVE_MAKERS) {
            for (final Spawner spawner : SpawnManager.getInstance().getSpawners(makers)) {
                spawner.deleteAll();
            }
        }
    }

    private void showOnScreenCustomMsgStr(final int time, final String address, final Object... args) {
        final Party party = getActiveParty();
        if (party != null && party.getMemberCount() > 0) {
            for (final Player player : getActiveParty()) {
                final CustomMessage customMessage = new CustomMessage(address, player, args);
                final ExShowScreenMessage msg = new ExShowScreenMessage(customMessage.toString(), time, 0, ScreenMessageAlign.TOP_CENTER, true, 1, -1, false);
                player.sendPacket(msg);
            }
        }
    }

    private void enterIceCastle(final Party party) {
        _activePartyRef = new SoftReference<>(party);
        _activePartyMemberCount = party.getMemberCount();
        _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1000), 120000L);
        showOnScreenCustomMsgStr(100000, "scripts.ice_castle.1121000");
    }

    private class Listner implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            if (_nextTask != null) {
                _nextTask.cancel(false);
                _nextTask = null;
            }
            ThreadPoolManager.getInstance().execute(new IceCastleRunner(1007));
        }
    }

    private class IceCastleRunner extends RunnableImpl {
        private final int _eventId;

        private IceCastleRunner(final int eventId) {
            _eventId = eventId;
        }

        @Override
        public void runImpl() {
            switch (_eventId) {
                case 1000: {
                    final Party party = getActiveParty();
                    boolean canContinue = true;
                    if (party == null || party.getMemberCount() != _activePartyMemberCount) {
                        canContinue = false;
                    }
                    if (canContinue && party != null) {
                        for (final Player player : party.getPartyMembers()) {
                            if (getDistance(player) > 512.0) {
                                canContinue = false;
                            }
                        }
                    }
                    if (!canContinue) {
                        clear();
                        Functions.npcShoutCustomMessage(FreyaDeaconKeeperInstance.this, "scripts.ice_castle.1121007");
                        return;
                    }
                    closeTheDoors();
                    clearZone();
                    _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1001), 5000L);
                    Functions.teleportParty(party, FreyaDeaconKeeperInstance.ICE_CASTLE_LOC, 64);
                    break;
                }
                case 1001: {
                    final Party party = getActiveParty();
                    if (party == null || party.getMemberCount() != _activePartyMemberCount) {
                        clear();
                        return;
                    }
                    showOnScreenCustomMsgStr(10000, "scripts.ice_castle.1121001");
                    spawnSirr();
                    spawnFreyaMobs();
                    _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1002), 30000L);
                    break;
                }
                case 1002: {
                    final Party party = getActiveParty();
                    if (party != null) {
                        final Skill buff = SkillTable.getInstance().getInfo(4479, 1);
                        for (final Player partyMember : party.getPartyMembers()) {
                            buff.getEffects(partyMember, partyMember, false, false);
                        }
                    }
                    _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1003), 300000L);
                    break;
                }
                case 1003: {
                    showOnScreenCustomMsgStr(10000, "scripts.ice_castle.1121008");
                    _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1004), 600000L);
                    break;
                }
                case 1004: {
                    showOnScreenCustomMsgStr(10000, "scripts.ice_castle.1121009");
                    _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1005), 600000L);
                    break;
                }
                case 1005: {
                    showOnScreenCustomMsgStr(10000, "scripts.ice_castle.1121002");
                    _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1006), 540000L);
                    break;
                }
                case 1006: {
                    despawnFreyaMobs();
                    despawnSirr();
                    openTheDoors();
                    showOnScreenCustomMsgStr(10000, "scripts.ice_castle.1121003");
                    clear();
                    break;
                }
                case 1007: {
                    despawnFreyaMobs();
                    openTheDoors();
                    showOnScreenCustomMsgStr(10000, "scripts.ice_castle.1121004");
                    _nextTask = ThreadPoolManager.getInstance().schedule(new IceCastleRunner(1008), 30000L);
                    break;
                }
                case 1008: {
                    despawnSirr();
                    clear();
                    break;
                }
            }
        }
    }
}
