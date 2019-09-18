package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject.SiegeClanComparatorImpl;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SysMsgContainer;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.tables.ClanTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClanHallMiniGameEvent extends SiegeEvent<ClanHall, CMGSiegeClanObject> {
    public static final String REFUND = "refund";
    public static final String NEXT_STEP = "next_step";
    private boolean _arenaClosed;

    public ClanHallMiniGameEvent(final MultiValueSet<String> set) {
        super(set);
        _arenaClosed = true;
    }

    @Override
    public void startEvent() {
        _oldOwner = getResidence().getOwner();
        final List<CMGSiegeClanObject> siegeClans = getObjects("attackers");
        if (siegeClans.size() < 2) {
            CMGSiegeClanObject siegeClan = null;
            try {
                siegeClan = siegeClans.get(0);
            } catch (Exception ignored) {
            }
            if (siegeClan != null) {
                final CMGSiegeClanObject oldSiegeClan = getSiegeClan(REFUND, siegeClan.getObjectId());
                if (oldSiegeClan != null) {
                    SiegeClanDAO.getInstance().delete(getResidence(), siegeClan);
                    oldSiegeClan.setParam(oldSiegeClan.getParam() + siegeClan.getParam());
                    SiegeClanDAO.getInstance().update(getResidence(), oldSiegeClan);
                } else {
                    siegeClan.setType(REFUND);
                    siegeClans.remove(siegeClan);
                    addObject(REFUND, siegeClan);
                    SiegeClanDAO.getInstance().update(getResidence(), siegeClan);
                }
            }
            siegeClans.clear();
            broadcastTo(SystemMsg.THIS_CLAN_HALL_WAR_HAS_BEEN_CANCELLED, "attackers");
            broadcastInZone2((new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW)).addResidenceName(getResidence()));
            reCalcNextTime(false);
            return;
        }
        final CMGSiegeClanObject[] clans = siegeClans.toArray(new CMGSiegeClanObject[0]);
        Arrays.sort(clans, SiegeClanComparatorImpl.getInstance());
        final List<CMGSiegeClanObject> temp = new ArrayList<>(4);
        for (final CMGSiegeClanObject siegeClan2 : clans) {
            SiegeClanDAO.getInstance().delete(getResidence(), siegeClan2);
            if (temp.size() == 4) {
                siegeClans.remove(siegeClan2);
                siegeClan2.broadcast(SystemMsg.YOU_HAVE_FAILED_IN_YOUR_ATTEMPT_TO_REGISTER_FOR_THE_CLAN_HALL_WAR);
            } else {
                temp.add(siegeClan2);
                siegeClan2.broadcast(SystemMsg.YOU_HAVE_BEEN_REGISTERED_FOR_A_CLAN_HALL_WAR);
            }
        }
        _arenaClosed = false;
        super.startEvent();
    }

    @Override
    public void stopEvent(final boolean step) {
        removeBanishItems();
        final Clan newOwner = getResidence().getOwner();
        if (newOwner != null) {
            if (_oldOwner != newOwner) {
                newOwner.broadcastToOnlineMembers(PlaySound.SIEGE_VICTORY);
                newOwner.incReputation(1700, false, toString());
            }
            broadcastTo(((SysMsgContainer) new SystemMessage2(SystemMsg.S1_CLAN_HAS_DEFEATED_S2).addString(newOwner.getName())).addResidenceName(getResidence()), "attackers", "defenders");
            broadcastTo(((SysMsgContainer) new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED)).addResidenceName(getResidence()), "attackers", "defenders");
        } else {
            broadcastTo(((SysMsgContainer) new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW)).addResidenceName(getResidence()), "attackers");
        }
        updateParticles(false, "attackers");
        removeObjects("attackers");
        super.stopEvent(step);
        _oldOwner = null;
    }

    public void nextStep() {
        final List<CMGSiegeClanObject> siegeClans = getObjects("attackers");
        for (int i = 0; i < siegeClans.size(); ++i) {
            spawnAction("arena_" + i, true);
        }
        updateParticles(_arenaClosed = true, "attackers");
        broadcastTo((new SystemMessage2(SystemMsg.THE_SIEGE_TO_CONQUER_S1_HAS_BEGUN)).addResidenceName(getResidence()), "attackers");
    }

    @Override
    public void setRegistrationOver(final boolean b) {
        if (b) {
            broadcastTo(SystemMsg.THE_REGISTRATION_PERIOD_FOR_A_CLAN_HALL_WAR_HAS_ENDED, "attackers");
        }
        super.setRegistrationOver(b);
    }

    @Override
    public CMGSiegeClanObject newSiegeClan(final String type, final int clanId, final long param, final long date) {
        final Clan clan = ClanTable.getInstance().getClan(clanId);
        return (clan == null) ? null : new CMGSiegeClanObject(type, clan, param, date);
    }

    @Override
    public void announce(final int val) {
        final int seconds = val % 60;
        final int min = val / 60;
        if (min > 0) {
            final SystemMsg msg = (min > 10) ? SystemMsg.IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_MUST_HURRY_AND_MOVE_TO_THE_LEFT_SIDE_OF_THE_CLAN_HALLS_ARENA : SystemMsg.IN_S1_MINUTES_THE_GAME_WILL_BEGIN_ALL_PLAYERS_PLEASE_ENTER_THE_ARENA_NOW;
            broadcastTo((new SystemMessage2(msg)).addInteger(min), "attackers");
        } else {
            broadcastTo(new SystemMessage2(SystemMsg.IN_S1_SECONDS_THE_GAME_WILL_BEGIN).addInteger(seconds), "attackers");
        }
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
        addObjects(REFUND, SiegeClanDAO.getInstance().load(getResidence(), REFUND));
    }

    @Override
    public void action(final String name, final boolean start) {
        if (NEXT_STEP.equalsIgnoreCase(name)) {
            nextStep();
        } else {
            super.action(name, start);
        }
    }

    @Override
    public int getUserRelation(final Player thisPlayer, final int result) {
        return result;
    }

    @Override
    public int getRelation(final Player thisPlayer, final Player targetPlayer, final int result) {
        return result;
    }

    public boolean isArenaClosed() {
        return _arenaClosed;
    }

    @Override
    public void onAddEvent(final GameObject object) {
        if (object.isItem()) {
            addBanishItem((ItemInstance) object);
        }
    }
}
