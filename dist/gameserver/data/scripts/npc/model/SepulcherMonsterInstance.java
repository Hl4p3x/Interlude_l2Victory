package npc.model;

import bosses.FourSepulchersSpawn;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcSay;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.concurrent.Future;

public class SepulcherMonsterInstance extends MonsterInstance {
    private static final int HALLS_KEY = 7260;
    public int mysteriousBoxId;
    private Future<?> _victimShout;
    private Future<?> _victimSpawnKeyBoxTask;
    private Future<?> _changeImmortalTask;
    private Future<?> _onDeadEventTask;

    public SepulcherMonsterInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected void onSpawn() {
        switch (getNpcId()) {
            case 18150:
            case 18151:
            case 18152:
            case 18153:
            case 18154:
            case 18155:
            case 18156:
            case 18157: {
                if (_victimSpawnKeyBoxTask != null) {
                    _victimSpawnKeyBoxTask.cancel(false);
                }
                _victimSpawnKeyBoxTask = ThreadPoolManager.getInstance().schedule(new VictimSpawnKeyBox(this), 300000L);
                if (_victimShout != null) {
                    _victimShout.cancel(false);
                }
                _victimShout = ThreadPoolManager.getInstance().schedule(new VictimShout(this), 5000L);
            }
            case 18231:
            case 18232:
            case 18233:
            case 18234:
            case 18235:
            case 18236:
            case 18237:
            case 18238:
            case 18239:
            case 18240:
            case 18241:
            case 18242:
            case 18243: {
                if (_changeImmortalTask != null) {
                    _changeImmortalTask.cancel(false);
                }
                _changeImmortalTask = ThreadPoolManager.getInstance().schedule(new ChangeImmortal(this), 1600L);
                break;
            }
        }
        super.onSpawn();
    }

    @Override
    protected void onDeath(final Creature killer) {
        super.onDeath(killer);
        switch (getNpcId()) {
            case 18120:
            case 18121:
            case 18122:
            case 18123:
            case 18124:
            case 18125:
            case 18126:
            case 18127:
            case 18128:
            case 18129:
            case 18130:
            case 18131:
            case 18149:
            case 18158:
            case 18159:
            case 18160:
            case 18161:
            case 18162:
            case 18163:
            case 18164:
            case 18165:
            case 18183:
            case 18184:
            case 18212:
            case 18213:
            case 18214:
            case 18215:
            case 18216:
            case 18217:
            case 18218:
            case 18219: {
                if (_onDeadEventTask != null) {
                    _onDeadEventTask.cancel(false);
                }
                _onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500L);
                break;
            }
            case 18150:
            case 18151:
            case 18152:
            case 18153:
            case 18154:
            case 18155:
            case 18156:
            case 18157: {
                if (_victimSpawnKeyBoxTask != null) {
                    _victimSpawnKeyBoxTask.cancel(false);
                    _victimSpawnKeyBoxTask = null;
                }
                if (_victimShout != null) {
                    _victimShout.cancel(false);
                    _victimShout = null;
                }
                if (_onDeadEventTask != null) {
                    _onDeadEventTask.cancel(false);
                }
                _onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500L);
                break;
            }
            case 18141:
            case 18142:
            case 18143:
            case 18144:
            case 18145:
            case 18146:
            case 18147:
            case 18148: {
                if (FourSepulchersSpawn.isViscountMobsAnnihilated(mysteriousBoxId) && !FourSepulchersSpawn.isKeyBoxMobSpawned() && !hasPartyAKey(killer.getPlayer())) {
                    if (_onDeadEventTask != null) {
                        _onDeadEventTask.cancel(false);
                    }
                    _onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500L);
                    break;
                }
                break;
            }
            case 18220:
            case 18221:
            case 18222:
            case 18223:
            case 18224:
            case 18225:
            case 18226:
            case 18227:
            case 18228:
            case 18229:
            case 18230:
            case 18231:
            case 18232:
            case 18233:
            case 18234:
            case 18235:
            case 18236:
            case 18237:
            case 18238:
            case 18239:
            case 18240: {
                if (FourSepulchersSpawn.isDukeMobsAnnihilated(mysteriousBoxId)) {
                    if (_onDeadEventTask != null) {
                        _onDeadEventTask.cancel(false);
                    }
                    _onDeadEventTask = ThreadPoolManager.getInstance().schedule(new OnDeadEvent(this), 3500L);
                    break;
                }
                break;
            }
        }
    }

    @Override
    protected void onDelete() {
        if (_victimSpawnKeyBoxTask != null) {
            _victimSpawnKeyBoxTask.cancel(false);
            _victimSpawnKeyBoxTask = null;
        }
        if (_onDeadEventTask != null) {
            _onDeadEventTask.cancel(false);
            _onDeadEventTask = null;
        }
        super.onDelete();
    }

    private boolean hasPartyAKey(final Player player) {
        if (player.getParty() == null) {
            return false;
        }
        for (final Player m : player.getParty().getPartyMembers()) {
            if (ItemFunctions.getItemCount(m, HALLS_KEY) > 0L) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canChampion() {
        return false;
    }

    private class VictimShout extends RunnableImpl {
        private final SepulcherMonsterInstance _activeChar;

        public VictimShout(final SepulcherMonsterInstance activeChar) {
            _activeChar = activeChar;
        }

        @Override
        public void runImpl() {
            if (_activeChar.isDead()) {
                return;
            }
            if (!_activeChar.isVisible()) {
                return;
            }
            broadcastPacket(new NpcSay(SepulcherMonsterInstance.this, ChatType.NPC_NORMAL, "forgive me!!"));
        }
    }

    private class VictimSpawnKeyBox extends RunnableImpl {
        private final SepulcherMonsterInstance _activeChar;

        public VictimSpawnKeyBox(final SepulcherMonsterInstance activeChar) {
            _activeChar = activeChar;
        }

        @Override
        public void runImpl() {
            if (_activeChar.isDead()) {
                return;
            }
            if (!_activeChar.isVisible()) {
                return;
            }
            FourSepulchersSpawn.spawnKeyBox(_activeChar);
            broadcastPacket(new NpcSay(SepulcherMonsterInstance.this, ChatType.NPC_NORMAL, "Many thanks for rescue me."));
            if (_victimShout != null) {
                _victimShout.cancel(false);
                _victimShout = null;
            }
        }
    }

    private class OnDeadEvent extends RunnableImpl {
        SepulcherMonsterInstance _activeChar;

        public OnDeadEvent(final SepulcherMonsterInstance activeChar) {
            _activeChar = activeChar;
        }

        @Override
        public void runImpl() {
            switch (_activeChar.getNpcId()) {
                case 18120:
                case 18121:
                case 18122:
                case 18123:
                case 18124:
                case 18125:
                case 18126:
                case 18127:
                case 18128:
                case 18129:
                case 18130:
                case 18131:
                case 18149:
                case 18158:
                case 18159:
                case 18160:
                case 18161:
                case 18162:
                case 18163:
                case 18164:
                case 18165:
                case 18183:
                case 18184:
                case 18212:
                case 18213:
                case 18214:
                case 18215:
                case 18216:
                case 18217:
                case 18218:
                case 18219: {
                    FourSepulchersSpawn.spawnKeyBox(_activeChar);
                    break;
                }
                case 18150:
                case 18151:
                case 18152:
                case 18153:
                case 18154:
                case 18155:
                case 18156:
                case 18157: {
                    FourSepulchersSpawn.spawnExecutionerOfHalisha(_activeChar);
                    break;
                }
                case 18141:
                case 18142:
                case 18143:
                case 18144:
                case 18145:
                case 18146:
                case 18147:
                case 18148: {
                    FourSepulchersSpawn.spawnMonster(_activeChar.mysteriousBoxId);
                    break;
                }
                case 18220:
                case 18221:
                case 18222:
                case 18223:
                case 18224:
                case 18225:
                case 18226:
                case 18227:
                case 18228:
                case 18229:
                case 18230:
                case 18231:
                case 18232:
                case 18233:
                case 18234:
                case 18235:
                case 18236:
                case 18237:
                case 18238:
                case 18239:
                case 18240: {
                    FourSepulchersSpawn.spawnArchonOfHalisha(_activeChar.mysteriousBoxId);
                    break;
                }
            }
        }
    }

    private class ChangeImmortal extends RunnableImpl {
        private final SepulcherMonsterInstance activeChar;

        public ChangeImmortal(final SepulcherMonsterInstance mob) {
            activeChar = mob;
        }

        @Override
        public void runImpl() {
            final Skill fp = SkillTable.getInstance().getInfo(4616, 1);
            fp.getEffects(activeChar, activeChar, false, false);
        }
    }
}
