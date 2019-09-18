package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.MonsterRace;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.DeleteObject;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MonRaceInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.utils.Location;

public class AdminMonsterRace implements IAdminCommandHandler {
    protected static int state = -1;

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if ("admin_mons".equalsIgnoreCase(fullString)) {
            if (!activeChar.getPlayerAccess().MonsterRace) {
                return false;
            }
            handleSendPacket(activeChar);
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void handleSendPacket(final Player activeChar) {
        final int[][] codes = {{-1, 0}, {0, 15322}, {13765, -1}, {-1, 0}};
        final MonsterRace race = MonsterRace.getInstance();
        if (state == -1) {
            ++state;
            race.newRace();
            race.newSpeeds();
            activeChar.broadcastPacket(new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds()));
        } else if (state == 0) {
            ++state;
            activeChar.sendPacket(Msg.THEYRE_OFF);
            activeChar.broadcastPacket(new PlaySound("S_Race"));
            activeChar.broadcastPacket(new PlaySound(Type.SOUND, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559)));
            activeChar.broadcastPacket(new MonRaceInfo(codes[state][0], codes[state][1], race.getMonsters(), race.getSpeeds()));
            ThreadPoolManager.getInstance().schedule(new RunRace(codes, activeChar), 5000L);
        }
    }

    private enum Commands {
        admin_mons
    }

    class RunRace extends RunnableImpl {
        private final int[][] codes;
        private final Player activeChar;

        public RunRace(final int[][] codes, final Player activeChar) {
            this.codes = codes;
            this.activeChar = activeChar;
        }

        @Override
        public void runImpl() {
            activeChar.broadcastPacket(new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds()));
            ThreadPoolManager.getInstance().schedule(new RunEnd(activeChar), 30000L);
        }
    }

    class RunEnd extends RunnableImpl {
        private final Player activeChar;

        public RunEnd(final Player activeChar) {
            this.activeChar = activeChar;
        }

        @Override
        public void runImpl() {
            for (int i = 0; i < 8; ++i) {
                final NpcInstance obj = MonsterRace.getInstance().getMonsters()[i];
                activeChar.broadcastPacket(new DeleteObject(obj));
            }
            state = -1;
        }
    }
}
