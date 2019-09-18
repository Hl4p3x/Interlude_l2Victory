package ru.j2dev.gameserver.model.actor.recorder;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.matching.MatchingRoom;

public final class PlayerStatsChangeRecorder extends CharStatsChangeRecorder<Player> {
    public static final int BROADCAST_KARMA = 8;
    public static final int SEND_STORAGE_INFO = 16;
    public static final int SEND_MAX_LOAD = 32;
    public static final int SEND_CUR_LOAD = 64;
    public static final int BROADCAST_CHAR_INFO2 = 128;
    private final int[] _attackElement;
    private final int[] _defenceElement;
    private int _maxCp;
    private int _maxLoad;
    private int _curLoad;
    private long _exp;
    private int _sp;
    private int _karma;
    private int _pk;
    private int _pvp;
    private int _fame;
    private int _inventory;
    private int _warehouse;
    private int _clan;
    private int _trade;
    private int _recipeDwarven;
    private int _recipeCommon;
    private int _partyRoom;
    private String _title;
    private int _cubicsHash;

    public PlayerStatsChangeRecorder(final Player activeChar) {
        super(activeChar);
        _attackElement = new int[6];
        _defenceElement = new int[6];
        _title = "";
    }

    @Override
    protected void refreshStats() {
        _maxCp = set(4, _maxCp, _activeChar.getMaxCp());
        super.refreshStats();
        _maxLoad = set(34, _maxLoad, _activeChar.getMaxLoad());
        _curLoad = set(64, _curLoad, _activeChar.getCurrentLoad());
        for (final Element e : Element.VALUES) {
            _attackElement[e.getId()] = set(2, _attackElement[e.getId()], _activeChar.getAttack(e));
            _defenceElement[e.getId()] = set(2, _defenceElement[e.getId()], _activeChar.getDefence(e));
        }
        _exp = set(2, _exp, _activeChar.getExp());
        _sp = set(2, _sp, _activeChar.getIntSp());
        _pk = set(2, _pk, _activeChar.getPkKills());
        _pvp = set(2, _pvp, _activeChar.getPvpKills());
        _karma = set(8, _karma, _activeChar.getKarma());
        _inventory = set(16, _inventory, _activeChar.getInventoryLimit());
        _warehouse = set(16, _warehouse, _activeChar.getWarehouseLimit());
        _clan = set(16, _clan, Config.WAREHOUSE_SLOTS_CLAN);
        _trade = set(16, _trade, _activeChar.getTradeLimit());
        _recipeDwarven = set(16, _recipeDwarven, _activeChar.getDwarvenRecipeLimit());
        _recipeCommon = set(16, _recipeCommon, _activeChar.getCommonRecipeLimit());
        _cubicsHash = set(1, _cubicsHash, _activeChar.getCubics().hashCode());
        _partyRoom = set(1, _partyRoom, (_activeChar.getMatchingRoom() != null && _activeChar.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && _activeChar.getMatchingRoom().getLeader() == _activeChar) ? _activeChar.getMatchingRoom().getId() : 0);
        _team = set(128, _team, _activeChar.getTeam());
        _title = set(1, _title, _activeChar.getTitle());
    }

    @Override
    protected void onSendChanges() {
        super.onSendChanges();
        if ((_changes & 0x80) == 0x80) {
            _activeChar.broadcastCharInfo();
            if (_activeChar.getPet() != null) {
                _activeChar.getPet().broadcastCharInfo();
            }
        }
        if ((_changes & 0x1) == 0x1) {
            _activeChar.broadcastCharInfo();
        } else if ((_changes & 0x2) == 0x2) {
            _activeChar.sendUserInfo();
        }
        if ((_changes & 0x40) == 0x40) {
            _activeChar.sendStatusUpdate(false, false, 14);
        }
        if ((_changes & 0x20) == 0x20) {
            _activeChar.sendStatusUpdate(false, false, 15);
        }
        if ((_changes & 0x8) == 0x8) {
            _activeChar.sendStatusUpdate(true, false, 27);
        }
    }
}
