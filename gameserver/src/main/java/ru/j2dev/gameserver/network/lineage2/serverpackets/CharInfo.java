package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.skills.effects.EffectCubic;
import ru.j2dev.gameserver.utils.Location;

import java.util.Arrays;

public class CharInfo extends L2GameServerPacket {
    public static final int[] PAPERDOLL_ORDER = {0, 6, 7, 8, 9, 10, 11, 12, 13, 7, 15, 16};
    private static final Logger LOGGER = LoggerFactory.getLogger(CharInfo.class);

    private int[][] _inv;
    private int _mAtkSpd;
    private int _pAtkSpd;
    private int _runSpd;
    private int _walkSpd;
    private int _swimSpd;
    private int _flRunSpd;
    private int _flWalkSpd;
    private int _flyRunSpd;
    private int _flyWalkSpd;
    private Location _loc;
    private Location _fishLoc;
    private String _name;
    private String _title;
    private int _objId;
    private int _race;
    private int _sex;
    private int base_class;
    private int pvp_flag;
    private int karma;
    private int rec_have;
    private double moveAnimMod;
    private double atkAnimMod;
    private double col_radius;
    private double col_height;
    private int hair_style;
    private int hair_color;
    private int face;
    private int _abnormalEffect;
    private int _abnormalEffect2;
    private int clan_id;
    private int clan_crest_id;
    private int large_clan_crest_id;
    private int ally_id;
    private int ally_crest_id;
    private int class_id;
    private int _sit;
    private int _run;
    private int _combat;
    private int _dead;
    private int private_store;
    private int _enchant;
    private int _noble;
    private int _hero;
    private int _fishing;
    private int mount_type;
    private int plg_class;
    private int pledge_type;
    private int clan_rep_score;
    private int cw_level;
    private int mount_id;
    private int _nameColor;
    private int _title_color;
    private int _transform;
    private int _agathion;
    private int _clanBoatObjectId;
    private EffectCubic[] cubics;
    private boolean _isPartyRoomLeader;
    private boolean _isFlying;
    private TeamType _team;

    public CharInfo(final Player cha) {
        this((Creature) cha);
    }

    public CharInfo(final Creature cha) {
        if (cha == null) {
            System.out.println("CharInfo: cha is null!");
            Thread.dumpStack();
            return;
        }
        if (cha.isInvisible()) {
            return;
        }
        if (cha.isDeleted()) {
            return;
        }
        final Player player = cha.getPlayer();
        if (player == null) {
            return;
        }
        if (_loc == null) {
            _loc = cha.getLoc();
        }
        _objId = cha.getObjectId();
        if (player.getTransformationName() != null || (player.getReflection() == ReflectionManager.GIRAN_HARBOR && player.getPrivateStoreType() != 0)) {
            _name = ((player.getTransformationName() != null) ? player.getTransformationName() : player.getName());
            _title = ((player.getTransformationTitle() != null) ? player.getTransformationTitle() : "");
            clan_id = 0;
            clan_crest_id = 0;
            ally_id = 0;
            ally_crest_id = 0;
            large_clan_crest_id = 0;
            if (player.isCursedWeaponEquipped()) {
                cw_level = CursedWeaponsManager.getInstance().getLevel(player.getCursedWeaponEquippedId());
            }
        } else {
            _name = player.getName();
            if (player.getPrivateStoreType() != 0) {
                _title = "";
            } else if (!player.isConnected()) {
                _title = player.getDisconnectedTitle();
                _title_color = player.getDisconnectedTitleColor();
            } else {
                _title = player.getTitle();
                _title_color = player.getTitleColor();
            }
            final Clan clan = player.getClan();
            final Alliance alliance = (clan == null) ? null : clan.getAlliance();
            clan_id = ((clan == null) ? 0 : clan.getClanId());
            clan_crest_id = ((clan == null) ? 0 : clan.getCrestId());
            large_clan_crest_id = ((clan == null) ? 0 : clan.getCrestLargeId());
            ally_id = ((alliance == null) ? 0 : alliance.getAllyId());
            ally_crest_id = ((alliance == null) ? 0 : alliance.getAllyCrestId());
            cw_level = 0;
        }
        if (player.isMounted()) {
            _enchant = 0;
            mount_id = player.getMountNpcId() + 1000000;
            mount_type = player.getMountType();
        } else {
            _enchant = player.getEnchantEffect();
            mount_id = 0;
            mount_type = 0;
        }
        _inv = new int[17][2];
        Arrays.stream(PAPERDOLL_ORDER).forEach(PAPERDOLL_ID -> {
            _inv[PAPERDOLL_ID][0] = player.getInventory().getPaperdollItemId(PAPERDOLL_ID);
            _inv[PAPERDOLL_ID][1] = player.getInventory().getPaperdollAugmentationId(PAPERDOLL_ID);
        });
        _mAtkSpd = player.getMAtkSpd();
        _pAtkSpd = player.getPAtkSpd();
        moveAnimMod = player.getMovementSpeedMultiplier();
        _runSpd = (int) (player.getRunSpeed() / moveAnimMod);
        _walkSpd = (int) (player.getWalkSpeed() / moveAnimMod);
        _flRunSpd = 0;
        _flWalkSpd = 0;
        if (player.isFlying()) {
            _flyRunSpd = _runSpd;
            _flyWalkSpd = _walkSpd;
        } else {
            _flyRunSpd = 0;
            _flyWalkSpd = 0;
        }
        _swimSpd = player.getSwimSpeed();
        _race = player.getBaseTemplate().race.ordinal();
        _sex = player.getSex();
        base_class = player.getBaseClassId();
        pvp_flag = player.getPvpFlag();
        karma = player.getKarma();
        atkAnimMod = player.getAttackSpeedMultiplier();
        col_radius = player.getColRadius();
        col_height = player.getColHeight();
        hair_style = player.getHairStyle();
        hair_color = player.getHairColor();
        face = player.getFace();
        if (clan_id > 0 && player.getClan() != null) {
            clan_rep_score = player.getClan().getReputationScore();
        } else {
            clan_rep_score = 0;
        }
        _sit = (player.isSitting() ? 0 : 1);
        _run = (player.isRunning() ? 1 : 0);
        _combat = (player.isInCombat() ? 1 : 0);
        _dead = (player.isAlikeDead() ? 1 : 0);
        private_store = (player.isInObserverMode() ? 7 : player.getPrivateStoreType());
        cubics = player.getCubics().toArray(new EffectCubic[0]);
        _abnormalEffect = player.getAbnormalEffect();
        _abnormalEffect2 = player.getAbnormalEffect2();
        rec_have = player.getReceivedRec();
        class_id = player.getClassId().getId();
        _team = player.getTeam();
        _noble = (player.isNoble() ? 1 : 0);
        _hero = ((player.isHero() || (player.isGM() && Config.GM_HERO_AURA)) ? 1 : 0);
        _fishing = (player.isFishing() ? 1 : 0);
        _fishLoc = player.getFishLoc();
        _nameColor = player.getNameColor();
        plg_class = player.getPledgeClass();
        pledge_type = player.getPledgeType();
        _transform = player.getTransformation();
        _agathion = player.getAgathionId();
        _isPartyRoomLeader = (player.getMatchingRoom() != null && player.getMatchingRoom().getType() == MatchingRoom.PARTY_MATCHING && player.getMatchingRoom().getLeader() == player);
        _isFlying = player.isInFlyingTransform();
    }

    @Override
    protected final void writeImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_objId == 0) {
            return;
        }
        if (activeChar.getObjectId() == _objId) {
            LOGGER.error("You cant send CharInfo about his character to active user!!!");
            return;
        }
        writeC(3);
        writeD(_loc.x);
        writeD(_loc.y);
        writeD(_loc.z + Config.CLIENT_Z_SHIFT);
        writeD(_loc.h);
        writeD(_objId);
        writeS(_name);
        writeD(_race);
        writeD(_sex);
        writeD(base_class);
        Arrays.stream(PAPERDOLL_ORDER).map(PAPERDOLL_ID -> _inv[PAPERDOLL_ID][0]).forEach(this::writeD);
        Arrays.stream(PAPERDOLL_ORDER).map(PAPERDOLL_ID -> _inv[PAPERDOLL_ID][1]).forEach(this::writeD);
        writeD(pvp_flag);
        writeD(karma);
        writeD(_mAtkSpd);
        writeD(_pAtkSpd);
        writeD(pvp_flag);
        writeD(karma);
        writeD(_runSpd);
        writeD(_walkSpd);
        writeD(_swimSpd);
        writeD(_swimSpd);
        writeD(_flRunSpd);
        writeD(_flWalkSpd);
        writeD(_flyRunSpd);
        writeD(_flyWalkSpd);
        writeF(moveAnimMod);
        writeF(atkAnimMod);
        writeF(col_radius);
        writeF(col_height);
        writeD(hair_style);
        writeD(hair_color);
        writeD(face);
        writeS(_title);
        writeD(clan_id);
        writeD(clan_crest_id);
        writeD(ally_id);
        writeD(ally_crest_id);
        writeD(0);
        writeC(_sit);
        writeC(_run);
        writeC(_combat);
        writeC(_dead);
        writeC(0);
        writeC(mount_type);
        writeC(private_store);
        writeH(cubics.length);
        Arrays.stream(cubics).mapToInt(cubic -> (cubic == null) ? 0 : cubic.getId()).forEach(this::writeH);
        writeC(_isPartyRoomLeader ? 1 : 0);
        writeD(_abnormalEffect);
        writeC(_isFlying ? 2 : 0);
        writeH(rec_have);
        writeD(mount_id);
        writeD(class_id);
        writeD(0);
        writeC(_enchant);
        writeC(_team.ordinal());
        writeD(large_clan_crest_id);
        writeC(_noble);
        writeC(_hero);
        writeC(_fishing);
        writeD(_fishLoc.x);
        writeD(_fishLoc.y);
        writeD(_fishLoc.z);
        writeD(_nameColor);
        writeD(_loc.h);
        writeD(plg_class);
        writeD(pledge_type);
        writeD(_title_color);
        writeD(cw_level);
        writeD(clan_rep_score);
        writeD(_transform);
        writeD(_agathion);
        writeD(1);
        writeD(_abnormalEffect2);
    }
}
