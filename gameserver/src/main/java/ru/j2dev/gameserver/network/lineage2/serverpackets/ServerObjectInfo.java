package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class ServerObjectInfo extends L2GameServerPacket {
    private final NpcInstance _npc;
    private final int _npcObjId, _npcId;
    private final boolean _isAttackable;
    private final int _x, _y, _z;
    private final int _heading;
    private final double colHeight;
    private final double colRadius;
    private double moveAnimMod;
    private double atkAnimMod;
    private String _name = "";

    public ServerObjectInfo(final NpcInstance cha, final Creature actor) {
        _npc = cha;
        _npcObjId = cha.getObjectId();
        _npcId = _npc.getDisplayId();
        if (Config.SERVER_SIDE_NPC_NAME || cha.getTemplate().displayId != 0 || !cha.getName().equalsIgnoreCase(cha.getTemplate().getName())) {
            _name = cha.getName();
        }
        _isAttackable = cha.isAutoAttackable(actor);
        _x = _npc.getX();
        _y = _npc.getY();
        _z = _npc.getZ();
        _heading = cha.getHeading();
        colHeight = cha.getTemplate().getCollisionHeight();
        colRadius = cha.getTemplate().getCollisionRadius();
        moveAnimMod = cha.getMovementSpeedMultiplier();
        atkAnimMod = cha.getAttackSpeedMultiplier();
    }

    @Override
    protected void writeImpl() {
        writeC(0x8C);
        writeD(_npcObjId);
        writeD(_npcId + 1000000);
        writeS(_name);
        writeD(_isAttackable ? 1 : 0);
        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeD(_heading);
        writeF(moveAnimMod); // взято из клиента
        writeF(atkAnimMod); // attack speed multiplier
        writeF(colRadius);
        writeF(colHeight);
        writeD((int) (_isAttackable ? _npc.getCurrentHp() : 0));
        writeD(_isAttackable ? _npc.getMaxHp() : 0);
        writeD(0x01); // object type
        writeD(0x00); // special effects
    }
}
