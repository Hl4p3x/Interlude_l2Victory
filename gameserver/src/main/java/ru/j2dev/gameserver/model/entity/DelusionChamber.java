package ru.j2dev.gameserver.model.entity;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.InstantZoneHolder;
import ru.j2dev.gameserver.manager.DimensionalRiftManager;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.InstantZone;
import ru.j2dev.gameserver.utils.Location;

import java.util.concurrent.Future;

public class DelusionChamber extends DimensionalRift {
    private Future<?> killRiftTask;

    public DelusionChamber(final Party party, final int type, final int room) {
        super(party, type, room);
    }

    @Override
    public synchronized void createNewKillRiftTimer() {
        if (killRiftTask != null) {
            killRiftTask.cancel(false);
            killRiftTask = null;
        }
        killRiftTask = ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                if (getParty() != null && !getParty().getPartyMembers().isEmpty()) {
                    getParty().getPartyMembers().stream().filter(p -> p.getReflection() == DelusionChamber.this).forEach(p -> {
                        final String var = p.getVar("backCoords");
                        if (var == null) {
                            return;
                        }
                        if ("".equals(var)) {
                            return;
                        }
                        p.teleToLocation(Location.parseLoc(var), ReflectionManager.DEFAULT);
                        p.unsetVar("backCoords");
                    });
                }
                collapse();
            }
        }, 100L);
    }

    @Override
    public void partyMemberExited(final Player player) {
        if (getPlayersInside(false) < 2 || getPlayersInside(true) == 0) {
            createNewKillRiftTimer();
        }
    }

    @Override
    public void manualExitRift(final Player player, final NpcInstance npc) {
        if (!player.isInParty() || player.getParty().getReflection() != this) {
            return;
        }
        if (!player.getParty().isLeader(player)) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "rift/NotPartyLeader.htm", npc);
            return;
        }
        createNewKillRiftTimer();
    }

    @Override
    public String getName() {
        final InstantZone iz = InstantZoneHolder.getInstance().getInstantZone(_roomType + 120);
        return iz.getName();
    }

    @Override
    protected int getManagerId() {
        return 32664;
    }
}
