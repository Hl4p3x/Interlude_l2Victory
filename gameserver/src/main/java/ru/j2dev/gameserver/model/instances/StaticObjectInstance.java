package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.reference.L2Reference;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Events;
import ru.j2dev.gameserver.templates.StaticObjectTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.Collections;
import java.util.List;

public class StaticObjectInstance extends GameObject {
    private final HardReference<StaticObjectInstance> reference;
    private final StaticObjectTemplate _template;
    private int _meshIndex;

    public StaticObjectInstance(final int objectId, final StaticObjectTemplate template) {
        super(objectId);
        _template = template;
        reference = new L2Reference<>(this);
    }

    @Override
    public HardReference<StaticObjectInstance> getRef() {
        return reference;
    }

    public int getUId() {
        return _template.getUId();
    }

    public int getType() {
        return _template.getType();
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (Events.onAction(player, this, shift)) {
            return;
        }
        if (player.getTarget() != this) {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), 0));
            return;
        }
        final MyTargetSelected my = new MyTargetSelected(getObjectId(), 0);
        player.sendPacket(my);
        if (!isInRange(player, getActingRange())) {
            if (!player.getAI().isIntendingInteract(this)) {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
            }
            return;
        }
        if (_template.getType() == 0) {
            player.sendPacket(new NpcHtmlMessage(player, getUId(), "newspaper/arena.htm", 0));
        } else if (_template.getType() == 2) {
            player.sendPacket(new ShowTownMap(_template.getFilePath(), _template.getMapX(), _template.getMapY()));
            player.sendActionFailed();
        }
    }

    @Override
    public int getActingRange() {
        switch (_template.getType()) {
            case 1: {
                return 150;
            }
            default: {
                return 300;
            }
        }
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        return Collections.singletonList(new StaticObject(this));
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        return false;
    }

    public void broadcastInfo(final boolean force) {
        final StaticObject p = new StaticObject(this);
        for (final Player player : World.getAroundPlayers(this)) {
            player.sendPacket(p);
        }
    }

    @Override
    public int getGeoZ(final Location loc) {
        return loc.z;
    }

    public int getMeshIndex() {
        return _meshIndex;
    }

    public void setMeshIndex(final int meshIndex) {
        _meshIndex = meshIndex;
    }
}
