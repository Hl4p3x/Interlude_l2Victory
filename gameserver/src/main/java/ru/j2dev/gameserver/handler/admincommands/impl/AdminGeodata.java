package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.DoorInstance;

public class AdminGeodata implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanReload) {
            return false;
        }
        switch (command) {
            case admin_geo_z: {
                if (wordList.length > 1) {
                    activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoEngine.getHeight(activeChar.getX(), activeChar.getY(), Integer.parseInt(wordList[1]), activeChar.getReflectionId()) + " Loc_Z = " + activeChar.getZ());
                    break;
                }
                activeChar.sendMessage("GeoEngine: Geo_Z = " + GeoEngine.getHeight(activeChar.getLoc(), activeChar.getReflectionId()) + " Loc_Z = " + activeChar.getZ());
                break;
            }
            case admin_geo_type: {
                final int type = GeoEngine.getType(activeChar.getX(), activeChar.getY(), activeChar.getReflectionId());
                activeChar.sendMessage("GeoEngine: Geo_Type = " + type);
                break;
            }
            case admin_geo_nswe: {
                String result = "";
                final byte nswe = GeoEngine.getNSWE(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getReflectionId());
                if ((nswe & 0x8) == 0x0) {
                    result += " N";
                }
                if ((nswe & 0x4) == 0x0) {
                    result += " S";
                }
                if ((nswe & 0x2) == 0x0) {
                    result += " W";
                }
                if ((nswe & 0x1) == 0x0) {
                    result += " E";
                }
                activeChar.sendMessage("GeoEngine: Geo_NSWE -> " + nswe + "->" + result);
                break;
            }
            case admin_geo_los: {
                if (activeChar.getTarget() == null) {
                    activeChar.sendMessage("None Target!");
                    break;
                }
                if (GeoEngine.canSeeTarget(activeChar, activeChar.getTarget(), false)) {
                    activeChar.sendMessage("GeoEngine: Can See Target");
                    break;
                }
                activeChar.sendMessage("GeoEngine: Can't See Target");
                break;
            }
            case admin_geo_move: {
                if (activeChar.getTarget() == null) {
                    activeChar.sendMessage("None target!");
                    break;
                }
                if (GeoEngine.canMoveToCoord(activeChar.getX(), activeChar.getY(), activeChar.getZ(), activeChar.getTarget().getX(), activeChar.getTarget().getY(), activeChar.getTarget().getZ(), activeChar.getGeoIndex())) {
                    activeChar.sendMessage("GeoEngine: Can move to target.");
                    break;
                }
                activeChar.sendMessage("GeoEngine: Can't move to target.");
                break;
            }
            case admin_geo_trace: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //geo_trace on|off");
                    break;
                }
                if ("on".equalsIgnoreCase(wordList[1])) {
                    activeChar.setVar("trace", "1", -1L);
                    break;
                }
                if ("off".equalsIgnoreCase(wordList[1])) {
                    activeChar.unsetVar("trace");
                    break;
                }
                activeChar.sendMessage("Usage: //geo_trace on|off");
                break;
            }
            case admin_geo_dump: {
                GameObjectsStorage.getAllDoors().forEach(DoorInstance::openMe);
                GeoEngine.DumpGeodata("dump_geo");
                activeChar.sendMessage("Geodata dumped to directory /gameserver/dump_geo");
                break;
            }
            case admin_geo_squaredump: {
                if (wordList.length > 2) {
                    GeoEngine.DumpGeodataFileMap(Byte.parseByte(wordList[1]), Byte.parseByte(wordList[2]));
                    activeChar.sendMessage("Geo square saved " + wordList[1] + '_' + wordList[2]);
                    break;
                }
                GeoEngine.DumpGeodataFile(activeChar.getX(), activeChar.getY());
                activeChar.sendMessage("Actual geo square saved.");
                break;
            }
            case admin_geo_map: {
                final int x = (activeChar.getX() - World.MAP_MIN_X >> 15) + Config.GEO_X_FIRST;
                final int y = (activeChar.getY() - World.MAP_MIN_Y >> 15) + Config.GEO_Y_FIRST;
                activeChar.sendMessage("GeoMap: " + x + "_" + y);
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_geo_z,
        admin_geo_type,
        admin_geo_nswe,
        admin_geo_los,
        admin_geo_move,
        admin_geo_trace,
        admin_geo_map,
        admin_geo_dump,
        admin_geo_squaredump
    }
}
