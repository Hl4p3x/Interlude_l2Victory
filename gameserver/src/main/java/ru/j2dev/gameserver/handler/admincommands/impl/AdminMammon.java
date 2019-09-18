package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class AdminMammon implements IAdminCommandHandler {
    final List<Integer> npcIds;

    public AdminMammon() {
        npcIds = new ArrayList<>();
    }

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        npcIds.clear();
        if (!activeChar.getPlayerAccess().Menu) {
            return false;
        }
        if (fullString.startsWith("admin_find_mammon")) {
            npcIds.add(31113);
            npcIds.add(31126);
            npcIds.add(31092);
            int teleportIndex = -1;
            try {
                if (fullString.length() > 16) {
                    teleportIndex = Integer.parseInt(fullString.substring(18));
                }
            } catch (Exception ignored) {
            }
            findAdminNPCs(activeChar, npcIds, teleportIndex, -1);
        } else if ("admin_show_mammon".equals(fullString)) {
            npcIds.add(31113);
            npcIds.add(31126);
            findAdminNPCs(activeChar, npcIds, -1, 1);
        } else if ("admin_hide_mammon".equals(fullString)) {
            npcIds.add(31113);
            npcIds.add(31126);
            findAdminNPCs(activeChar, npcIds, -1, 0);
        } else if (fullString.startsWith("admin_list_spawns")) {
            int npcId = 0;
            try {
                npcId = Integer.parseInt(fullString.substring(18).trim());
            } catch (Exception NumberFormatException) {
                activeChar.sendMessage("Command format is //list_spawns <NPC_ID>");
            }
            npcIds.add(npcId);
            findAdminNPCs(activeChar, npcIds, -1, -1);
        } else if (fullString.startsWith("admin_msg")) {
            activeChar.sendPacket(new SystemMessage(Integer.parseInt(fullString.substring(10).trim())));
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    public void findAdminNPCs(final Player activeChar, final List<Integer> npcIdList, final int teleportIndex, final int makeVisible) {
        int index = 0;
        for (final NpcInstance npcInst : GameObjectsStorage.getNpcs()) {
            final int npcId = npcInst.getNpcId();
            if (npcIdList.contains(npcId)) {
                if (makeVisible == 1) {
                    npcInst.spawnMe();
                } else if (makeVisible == 0) {
                    npcInst.decayMe();
                }
                if (!npcInst.isVisible()) {
                    continue;
                }
                ++index;
                if (teleportIndex > -1) {
                    if (teleportIndex != index) {
                        continue;
                    }
                    activeChar.teleToLocation(npcInst.getLoc());
                } else {
                    activeChar.sendMessage(index + " - " + npcInst.getName() + " (" + npcInst.getObjectId() + "): " + npcInst.getX() + " " + npcInst.getY() + " " + npcInst.getZ());
                }
            }
        }
    }

    private enum Commands {
        admin_find_mammon,
        admin_show_mammon,
        admin_hide_mammon,
        admin_list_spawns
    }
}
