package npc.model;

import bosses.FourSepulchersManager;
import bosses.FourSepulchersSpawn;
import bosses.FourSepulchersSpawn.GateKeeper;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class SepulcherNpcInstance extends NpcInstance {
    private static final String HTML_FILE_PATH = "SepulcherNpc/";
    private static final int HALLS_KEY = 7260;
    protected static Map<Integer, Integer> _hallGateKeepers = new HashMap<>();

    protected Future<?> _closeTask;
    protected Future<?> _spawnMonsterTask;

    public SepulcherNpcInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected void onDelete() {
        if (_closeTask != null) {
            _closeTask.cancel(false);
            _closeTask = null;
        }
        if (_spawnMonsterTask != null) {
            _spawnMonsterTask.cancel(false);
            _spawnMonsterTask = null;
        }
        super.onDelete();
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        if (isDead()) {
            player.sendActionFailed();
            return;
        }
        switch (getNpcId()) {
            case 31468:
            case 31469:
            case 31470:
            case 31471:
            case 31472:
            case 31473:
            case 31474:
            case 31475:
            case 31476:
            case 31477:
            case 31478:
            case 31479:
            case 31480:
            case 31481:
            case 31482:
            case 31483:
            case 31484:
            case 31485:
            case 31486:
            case 31487: {
                doDie(player);
                if (_spawnMonsterTask != null) {
                    _spawnMonsterTask.cancel(false);
                }
                _spawnMonsterTask = ThreadPoolManager.getInstance().schedule(new SpawnMonster(getNpcId()), 3500L);
            }
            case 31455:
            case 31456:
            case 31457:
            case 31458:
            case 31459:
            case 31460:
            case 31461:
            case 31462:
            case 31463:
            case 31464:
            case 31465:
            case 31466:
            case 31467: {
                if (player.isInParty() && !hasPartyAKey(player) && player.getParty().getPartyLeader() == player) {
                    Functions.addItem(player.getParty().getPartyLeader(), HALLS_KEY, 1L);
                    doDie(player);
                }
            }
            default: {
                super.showChatWindow(player, val);
            }
        }
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = String.valueOf(npcId);
        } else {
            pom = npcId + "-" + val;
        }
        return HTML_FILE_PATH + pom + ".htm";
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (command.startsWith("open_gate")) {
            ItemInstance hallsKey = player.getInventory().getItemByItemId(HALLS_KEY);
            if (hallsKey == null) {
                showHtmlFile(player, "Gatekeeper-no.htm");
            } else if (FourSepulchersManager.isAttackTime()) {
                switch (getNpcId()) {
                    case 31929:
                    case 31934:
                    case 31939:
                    case 31944: {
                        if (!FourSepulchersSpawn.isShadowAlive(getNpcId())) {
                            FourSepulchersSpawn.spawnShadow(getNpcId());
                            break;
                        }
                        break;
                    }
                }
                openNextDoor(getNpcId());
                if (player.getParty() != null) {
                    for (final Player mem : player.getParty().getPartyMembers()) {
                        hallsKey = mem.getInventory().getItemByItemId(HALLS_KEY);
                        if (hallsKey != null) {
                            Functions.removeItem(mem, HALLS_KEY, hallsKey.getCount());
                        }
                    }
                } else {
                    Functions.removeItem(player, HALLS_KEY, hallsKey.getCount());
                }
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void openNextDoor(final int npcId) {
        final GateKeeper gk = FourSepulchersManager.getHallGateKeeper(npcId);
        gk.door.openMe();
        if (_closeTask != null) {
            _closeTask.cancel(false);
        }
        _closeTask = ThreadPoolManager.getInstance().schedule(new CloseNextDoor(gk), 10000L);
    }

    public void sayInShout(final String msg) {
        if (msg == null || msg.isEmpty()) {
            return;
        }
        final Say2 sm = new Say2(0, ChatType.SHOUT, getName(), msg);
        for (final Player player : GameObjectsStorage.getAllPlayers()) {
            if (player == null) {
                continue;
            }
            if (!PositionUtils.checkIfInRange(15000, player, this, true)) {
                continue;
            }
            player.sendPacket(sm);
        }
    }

    public void showHtmlFile(final Player player, final String file) {
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile(HTML_FILE_PATH + file);
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }

    private boolean hasPartyAKey(final Player player) {
        return player.getParty().getPartyMembers().stream().anyMatch(m -> ItemFunctions.getItemCount(m, HALLS_KEY) > 0L);
    }

    private class CloseNextDoor extends RunnableImpl {
        private final GateKeeper _gk;
        private int state;

        public CloseNextDoor(final GateKeeper gk) {
            state = 0;
            _gk = gk;
        }

        @Override
        public void runImpl() {
            if (state == 0) {
                try {
                    _gk.door.closeMe();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                state++;
                _closeTask = ThreadPoolManager.getInstance().schedule(this, 10000L);
            } else if (state == 1) {
                FourSepulchersSpawn.spawnMysteriousBox(_gk.template.npcId);
                _closeTask = null;
            }
        }
    }

    private class SpawnMonster extends RunnableImpl {
        private final int _NpcId;

        public SpawnMonster(final int npcId) {
            _NpcId = npcId;
        }

        @Override
        public void runImpl() {
            FourSepulchersSpawn.spawnMonster(_NpcId);
        }
    }
}
