package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;

public class ClanHallSiegeEvent extends SiegeEvent<ClanHall, SiegeClanObject> {
    public static final String BOSS = "boss";

    public ClanHallSiegeEvent(final MultiValueSet<String> set) {
        super(set);
    }

    @Override
    public void startEvent() {
        _oldOwner = getResidence().getOwner();
        if (_oldOwner != null) {
            getResidence().changeOwner(null);
            addObject(ATTACKERS, new SiegeClanObject(ATTACKERS, _oldOwner, 0L));
        }
        if (getObjects(ATTACKERS).size() == 0) {
            broadcastInZone2((new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST)).addResidenceName(getResidence()));
            reCalcNextTime(false);
            return;
        }
        SiegeClanDAO.getInstance().delete(getResidence());
        updateParticles(true, ATTACKERS);
        broadcastTo((new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN)).addResidenceName(getResidence()), ATTACKERS);
        super.startEvent();
    }

    @Override
    public void stopEvent(final boolean step) {
        final Clan newOwner = getResidence().getOwner();
        if (newOwner != null) {
            newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);
            newOwner.incReputation(1700, false, toString());
            broadcastTo((new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName())).addResidenceName(getResidence()), ATTACKERS);
            broadcastTo((new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED)).addResidenceName(getResidence()), ATTACKERS);
        } else {
            broadcastTo((new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW)).addResidenceName(getResidence()), ATTACKERS);
        }
        updateParticles(false, ATTACKERS);
        removeObjects(ATTACKERS);
        super.stopEvent(step);
        _oldOwner = null;
    }

    @Override
    public void setRegistrationOver(final boolean b) {
        if (b) {
            broadcastTo((new SystemMessage2(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED)).addResidenceName(getResidence()), ATTACKERS);
        }
        super.setRegistrationOver(b);
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
        addObjects(ATTACKERS, SiegeClanDAO.getInstance().load(getResidence(), ATTACKERS));
    }

    @Override
    public int getUserRelation(final Player thisPlayer, final int result) {
        return result;
    }

    @Override
    public int getRelation(final Player thisPlayer, final Player targetPlayer, final int result) {
        return result;
    }

    @Override
    public boolean canResurrect(final Player resurrectPlayer, final Creature target, final boolean force) {
        final boolean playerInZone = resurrectPlayer.isInZone(ZoneType.SIEGE);
        final boolean targetInZone = target.isInZone(ZoneType.SIEGE);
        if (!playerInZone && !targetInZone) {
            return true;
        }
        if (!targetInZone) {
            return false;
        }
        final Player targetPlayer = target.getPlayer();
        final ClanHallSiegeEvent siegeEvent = target.getEvent(ClanHallSiegeEvent.class);
        if (siegeEvent != this) {
            if (force) {
                targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
            }
            resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
            return false;
        }
        final SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan(ATTACKERS, targetPlayer.getClan());
        if (targetSiegeClan.getFlag() == null) {
            if (force) {
                targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
            }
            resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
            return false;
        }
        if (force) {
            return true;
        }
        resurrectPlayer.sendPacket(SystemMsg.INVALID_TARGET);
        return false;
    }
}
