package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.utils.Location;

public class NpcInfo extends L2GameServerPacket {
    private boolean can_writeImpl;
    private int _npcObjId;
    private int _npcId;
    private int _running;
    private int _incombat;
    private int _dead;
    private int _showSpawnAnimation;
    private int _runSpd;
    private int _walkSpd;
    private int _mAtkSpd;
    private int _pAtkSpd;
    private int _rhand;
    private int _lhand;
    private int _enchantEffect;
    private int _karma;
    private int _pvp_flag;
    private int _abnormalEffect;
    private int _abnormalEffect2;
    private int _clan_id;
    private int _clan_crest_id;
    private int _ally_id;
    private int _ally_crest_id;
    private int _formId;
    private int _blueTitle;
    private double _colHeight;
    private double _colRadius;
    private double _currentColHeight;
    private double _currentColRadius;
    private double _moveAnimMod;
    private double _atkAnimMod;
    private boolean _isAttackable;
    private boolean _isNameAbove;
    private boolean _isFlying;
    private Location _loc;
    private String _name;
    private String _title;
    private boolean _showName;
    private int _state;
    private TeamType _team;

    public NpcInfo(final NpcInstance cha, final Creature attacker) {
        can_writeImpl = false;
        _name = "";
        _title = "";
        _npcId = ((cha.getDisplayId() != 0) ? cha.getDisplayId() : cha.getTemplate().npcId);
        _isAttackable = (attacker != null && cha.isAutoAttackable(attacker));
        _rhand = cha.getRightHandItem();
        _lhand = cha.getLeftHandItem();
        _enchantEffect = 0;
        if (Config.SERVER_SIDE_NPC_NAME || cha.getTemplate().displayId != 0 || !cha.getName().equals(cha.getTemplate().name)) {
            _name = cha.getName();
        }
        if (Config.SERVER_SIDE_NPC_TITLE || cha.getTemplate().displayId != 0 || !cha.getTitle().equals(cha.getTemplate().title)) {
            _title = cha.getTitle();
        }
        _showSpawnAnimation = cha.getSpawnAnimation();
        _showName = cha.isShowName();
        _state = cha.getNpcState();
        common(cha);
    }

    public NpcInfo(final Summon cha, final Creature attacker) {
        can_writeImpl = false;
        _name = "";
        _title = "";
        if (cha.getPlayer() != null && cha.getPlayer().isInvisible()) {
            return;
        }
        _npcId = cha.getTemplate().npcId;
        _isAttackable = (attacker != null && cha.isAutoAttackable(attacker));
        _rhand = 0;
        _lhand = 0;
        _enchantEffect = 0;
        _showName = true;
        _name = cha.getName();
        _title = cha.getTitle();
        _showSpawnAnimation = cha.getSpawnAnimation();
        common(cha);
    }

    private void common(final Creature cha) {
        _colHeight = cha.getTemplate().getCollisionHeight();
        _colRadius = cha.getTemplate().getCollisionRadius();
        _currentColHeight = cha.getColHeight();
        _currentColRadius = cha.getColRadius();
        _npcObjId = cha.getObjectId();
        _loc = cha.getLoc();
        _mAtkSpd = cha.getMAtkSpd();
        if (Config.ALT_NPC_CLAN == 0) {
            final Clan clan = cha.getClan();
            final Alliance alliance = (clan == null) ? null : clan.getAlliance();
            _clan_id = ((clan == null) ? 0 : clan.getClanId());
            _clan_crest_id = ((clan == null) ? 0 : clan.getCrestId());
            _ally_id = ((alliance == null) ? 0 : alliance.getAllyId());
            _ally_crest_id = ((alliance == null) ? 0 : alliance.getAllyCrestId());
        } else if (cha.isNpc() && Config.ALT_NPC_CLAN > 0 && ((NpcInstance) cha).getCastle() != null) {
            final Clan clan = ClanTable.getInstance().getClan(Config.ALT_NPC_CLAN);
            final Alliance alliance = (clan == null) ? null : clan.getAlliance();
            _clan_id = ((clan == null) ? 0 : clan.getClanId());
            _clan_crest_id = ((clan == null) ? 0 : clan.getCrestId());
            _ally_id = ((alliance == null) ? 0 : alliance.getAllyId());
            _ally_crest_id = ((alliance == null) ? 0 : alliance.getAllyCrestId());
        } else {
            _clan_id = 0;
            _clan_crest_id = 0;
            _ally_id = 0;
            _ally_crest_id = 0;
        }
        _moveAnimMod = cha.getMovementSpeedMultiplier();
        _atkAnimMod = cha.getAttackSpeedMultiplier();
        _runSpd = (int) (cha.getRunSpeed() / _moveAnimMod);
        _walkSpd = (int) (cha.getWalkSpeed() / _moveAnimMod);
        _karma = cha.getKarma();
        _pvp_flag = cha.getPvpFlag();
        _pAtkSpd = cha.getPAtkSpd();
        _running = (cha.isRunning() ? 1 : 0);
        _incombat = (cha.isInCombat() ? 1 : 0);
        _dead = (cha.isAlikeDead() ? 1 : 0);
        _abnormalEffect = cha.getAbnormalEffect();
        _abnormalEffect2 = cha.getAbnormalEffect2();
        _isFlying = cha.isFlying();
        _team = cha.getTeam();
        _formId = cha.getFormId();
        _isNameAbove = cha.isNameAbove();
        _blueTitle = ((cha.isSummon() || cha.isPet()) ? 1 : 0);
        can_writeImpl = true;
    }

    public NpcInfo update() {
        _showSpawnAnimation = 1;
        return this;
    }

    @Override
    protected final void writeImpl() {
        if (!can_writeImpl) {
            return;
        }
        writeC(0x16);
        writeD(_npcObjId);
        writeD(_npcId + 1000000);
        writeD(_isAttackable ? 1 : 0);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z + Config.CLIENT_Z_SHIFT);
        writeD(_loc.h);
        writeD(0);
        writeD(_mAtkSpd);
        writeD(_pAtkSpd);
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_runSpd);
        writeD(_walkSpd);
        writeF(_moveAnimMod);
        writeF(_atkAnimMod);
        writeF(_colRadius);
        writeF(_colHeight);
        writeD(_rhand);
        writeD(0);
        writeD(_lhand);
        writeC(_isNameAbove ? 1 : 0);
        writeC(_running);
        writeC(_incombat);
        writeC(_dead);
        writeC(_showSpawnAnimation);
        writeS(_name);
        writeS(_title);
        writeD(_blueTitle);
        writeD(_pvp_flag);
        writeD(_karma);
        writeD(_abnormalEffect);
        writeD(_clan_id);
        writeD(_clan_crest_id);
        writeD(_ally_id);
        writeD(_ally_crest_id);
        writeC(_isFlying ? 2 : 0);
        writeC(_team.ordinal());
        writeF(_currentColRadius);
        writeF(_currentColHeight);
        writeD(_enchantEffect);
        writeD(_isFlying ? 1 : 0);
    }
}
