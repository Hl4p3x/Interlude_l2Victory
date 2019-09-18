package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SysMsgContainer;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class ClanHallNpcSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject> {
    public ClanHallNpcSiegeEvent(final MultiValueSet<String> set) {
        super(set);
    }

    @Override
    public void startEvent() {
        _oldOwner = getResidence().getOwner();
        broadcastInZone(((SysMsgContainer) new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN)).addResidenceName(getResidence()));
        super.startEvent();
    }

    @Override
    public void stopEvent(final boolean step) {
        final Clan newOwner = getResidence().getOwner();
        if (newOwner != null) {
            if (_oldOwner != newOwner) {
                newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);
                newOwner.incReputation(1700, false, toString());
                if (_oldOwner != null) {
                    _oldOwner.incReputation(-1700, false, toString());
                }
            }
            broadcastInZone(((SysMsgContainer) new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName())).addResidenceName(getResidence()));
            broadcastInZone(((SysMsgContainer) new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED)).addResidenceName(getResidence()));
        } else {
            broadcastInZone(((SysMsgContainer) new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW)).addResidenceName(getResidence()));
        }
        super.stopEvent(step);
        _oldOwner = null;
    }

    @Override
    public void processStep(final Clan clan) {
        if (clan != null) {
            getResidence().changeOwner(clan);
        }
        stopEvent(true);
    }

    @Override
    public void loadSiegeClans() {
    }
}
