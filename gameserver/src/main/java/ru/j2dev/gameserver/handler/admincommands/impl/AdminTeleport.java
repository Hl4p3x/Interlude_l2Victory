package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class AdminTeleport implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanTeleport) {
            return false;
        }
        switch (command) {
            case admin_show_moves: {
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/teleports.htm"));
                break;
            }
            case admin_show_moves_other: {
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/tele/other.htm"));
                break;
            }
            case admin_show_teleport: {
                showTeleportCharWindow(activeChar);
                break;
            }
            case admin_teleport_to_character: {
                teleportToCharacter(activeChar, activeChar.getTarget());
                break;
            }
            case admin_teleport_to:
            case admin_teleportto: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //teleportto charName");
                    return false;
                }
                final String chaName = Util.joinStrings(" ", wordList, 1);
                final Player cha = GameObjectsStorage.getPlayer(chaName);
                if (cha == null) {
                    activeChar.sendMessage("Player '" + chaName + "' not found in world");
                    return false;
                }
                teleportToCharacter(activeChar, cha);
                break;
            }
            case admin_move_to:
            case admin_moveto:
            case admin_teleport: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //teleport x y z [ref]");
                    return false;
                }
                teleportTo(activeChar, activeChar, Util.joinStrings(" ", wordList, 1, 3), (ArrayUtils.valid(wordList, 4) != null && !ArrayUtils.valid(wordList, 4).isEmpty()) ? Integer.parseInt(wordList[4]) : 0);
                break;
            }
            case admin_walk: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //walk x y z");
                    return false;
                }
                try {
                    activeChar.moveToLocation(Location.parseLoc(Util.joinStrings(" ", wordList, 1)), 0, true);
                    break;
                } catch (IllegalArgumentException e) {
                    activeChar.sendMessage("USAGE: //walk x y z");
                    return false;
                }
            }
            case admin_gonorth:
            case admin_gosouth:
            case admin_goeast:
            case admin_gowest:
            case admin_goup:
            case admin_godown: {
                final int val = (wordList.length < 2) ? 150 : Integer.parseInt(wordList[1]);
                int x = activeChar.getX();
                int y = activeChar.getY();
                int z = activeChar.getZ();
                switch (command) {
                    case admin_goup:
                        z += val;
                        break;
                    case admin_godown:
                        z -= val;
                        break;
                    case admin_goeast:
                        x += val;
                        break;
                    case admin_gowest:
                        x -= val;
                        break;
                    case admin_gosouth:
                        y += val;
                        break;
                    case admin_gonorth:
                        y -= val;
                        break;
                }
                activeChar.teleToLocation(x, y, z);
                showTeleportWindow(activeChar);
                break;
            }
            case admin_tele: {
                showTeleportWindow(activeChar);
                break;
            }
            case admin_teleto:
            case admin_tele_to:
            case admin_instant_move: {
                if (wordList.length > 1 && "r".equalsIgnoreCase(wordList[1])) {
                    activeChar.setTeleMode(2);
                    break;
                }
                if (wordList.length > 1 && "end".equalsIgnoreCase(wordList[1])) {
                    activeChar.setTeleMode(0);
                    break;
                }
                activeChar.setTeleMode(1);
                break;
            }
            case admin_tonpc:
            case admin_to_npc: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //tonpc npcId|npcName");
                    return false;
                }
                final String npcName = Util.joinStrings(" ", wordList, 1);
                try {
                    final NpcInstance npc;
                    if ((npc = GameObjectsStorage.getByNpcId(Integer.parseInt(npcName))) != null) {
                        teleportToCharacter(activeChar, npc);
                        return true;
                    }
                } catch (Exception ignored) {
                }
                NpcInstance npc;
                if ((npc = GameObjectsStorage.getNpcByName(npcName)) != null) {
                    teleportToCharacter(activeChar, npc);
                    return true;
                }
                activeChar.sendMessage("Npc " + npcName + " not found");
                break;
            }
            case admin_toobject: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //toobject objectId");
                    return false;
                }
                final Integer target = Integer.parseInt(wordList[1]);
                final GameObject obj;
                if ((obj = GameObjectsStorage.findObject(target)) != null) {
                    teleportToCharacter(activeChar, obj);
                    return true;
                }
                activeChar.sendMessage("Object " + target + " not found");
                break;
            }
        }
        if (!activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        switch (command) {
            case admin_teleport_character: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //teleport_character x y z");
                    return false;
                }
                teleportCharacter(activeChar, Util.joinStrings(" ", wordList, 1));
                showTeleportCharWindow(activeChar);
                break;
            }
            case admin_recall: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //recall charName");
                    return false;
                }
                final String targetName = Util.joinStrings(" ", wordList, 1);
                final Player recall_player = GameObjectsStorage.getPlayer(targetName);
                if (recall_player != null) {
                    teleportTo(activeChar, recall_player, activeChar.getLoc(), activeChar.getReflectionId());
                    return true;
                }
                final int obj_id = CharacterDAO.getInstance().getObjectIdByName(targetName);
                if (obj_id > 0) {
                    teleportCharacter_offline(obj_id, activeChar.getLoc());
                    activeChar.sendMessage(targetName + " is offline. Offline teleport used...");
                    break;
                }
                activeChar.sendMessage("->" + targetName + "<- is incorrect.");
                break;
            }
            case admin_sendhome: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //sendhome");
                    return false;
                }
                sendHome(activeChar, Util.joinStrings(" ", wordList, 1));
                break;
            }
            case admin_setref: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //setref <reflection>");
                    return false;
                }
                final int ref_id = Integer.parseInt(wordList[1]);
                if (ref_id != 0 && ReflectionManager.getInstance().get(ref_id) == null) {
                    activeChar.sendMessage("Reflection <" + ref_id + "> not found.");
                    return false;
                }
                GameObject target2 = activeChar;
                final GameObject obj2 = activeChar.getTarget();
                if (obj2 != null) {
                    target2 = obj2;
                }
                target2.setReflection(ref_id);
                target2.decayMe();
                target2.spawnMe();
                break;
            }
            case admin_getref: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //getref <char_name>");
                    return false;
                }
                final Player cha2 = GameObjectsStorage.getPlayer(wordList[1]);
                if (cha2 == null) {
                    activeChar.sendMessage("Player '" + wordList[1] + "' not found in world");
                    return false;
                }
                activeChar.sendMessage("Player '" + wordList[1] + "' in reflection: " + activeChar.getReflectionId() + ", name: " + activeChar.getReflection().getName());
                break;
            }
            case admin_recall_party: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Usage: //recall_party <party_leader_name>");
                    return false;
                }
                final Player leader = GameObjectsStorage.getPlayer(wordList[1]);
                if (leader == null) {
                    activeChar.sendMessage("Player '" + wordList[1] + "' not found in world");
                    return false;
                }
                final Party party = leader.getParty();
                if (party == null) {
                    activeChar.sendMessage("Player '" + wordList[1] + "' have no party.");
                    return false;
                }
                for (final Player member : party.getPartyMembers()) {
                    final Location ptl = Location.findPointToStay(activeChar, 128);
                    member.teleToLocation(ptl, activeChar.getReflection());
                    member.sendMessage("Your party have been recalled by Admin.");
                }
                activeChar.sendMessage("Party recalled.");
                break;
            }
        }
        if (!activeChar.getPlayerAccess().CanEditNPC) {
            return false;
        }
        switch (command) {
            case admin_recall_npc: {
                recallNPC(activeChar);
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void showTeleportWindow(final Player activeChar) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        String replyMSG = "<html><title>Teleport Menu</title>" + "<body>" +
                "<br>" +
                "<center><table>" +
                "<tr><td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "<td><button value=\"North\" action=\"bypass -h admin_gonorth\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "<td><button value=\"Up\" action=\"bypass -h admin_goup\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>" +
                "<tr><td><button value=\"West\" action=\"bypass -h admin_gowest\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "<td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "<td><button value=\"East\" action=\"bypass -h admin_goeast\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>" +
                "<tr><td><button value=\"  \" action=\"bypass -h admin_tele\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "<td><button value=\"South\" action=\"bypass -h admin_gosouth\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "<td><button value=\"Down\" action=\"bypass -h admin_godown\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>" +
                "</table></center>" +
                "</body></html>";
        adminReply.setHtml(replyMSG);
        activeChar.sendPacket(adminReply);
    }

    private void showTeleportCharWindow(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        Player player;
        if (target.isPlayer()) {
            player = (Player) target;
            final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            String replyMSG = "<html><title>Teleport Character</title>" + "<body>" +
                    "The character you will teleport is " + player.getName() + "." +
                    "<br>" +
                    "Co-ordinate x" +
                    "<edit var=\"char_cord_x\" width=110>" +
                    "Co-ordinate y" +
                    "<edit var=\"char_cord_y\" width=110>" +
                    "Co-ordinate z" +
                    "<edit var=\"char_cord_z\" width=110>" +
                    "<button value=\"Teleport\" action=\"bypass -h admin_teleport_character $char_cord_x $char_cord_y $char_cord_z\" width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">" +
                    "<button value=\"Teleport near you\" action=\"bypass -h admin_teleport_character " + activeChar.getX() + " " + activeChar.getY() + " " + activeChar.getZ() + "\" width=115 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">" +
                    "<center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></center>" +
                    "</body></html>";
            adminReply.setHtml(replyMSG);
            activeChar.sendPacket(adminReply);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void teleportTo(final Player activeChar, final Player target, final String Cords, final int ref) {
        try {
            teleportTo(activeChar, target, Location.parseLoc(Cords), ref);
        } catch (IllegalArgumentException e) {
            activeChar.sendMessage("You must define 3 coordinates required to teleport");
        }
    }

    private void sendHome(final Player activeChar, final String playerName) {
        final Player target = GameObjectsStorage.getPlayer(playerName);
        if (target != null) {
            target.teleToClosestTown();
            target.sendMessage("The GM has sent you to the nearest town");
            activeChar.sendMessage("You have sent " + playerName + " to the nearest town");
        } else {
            activeChar.sendPacket(Msg.INVALID_TARGET);
        }
    }

    private void teleportTo(final Player activeChar, final Player target, final Location loc, final int ref) {
        if (!target.equals(activeChar)) {
            target.sendMessage("Admin is teleporting you.");
        }
        target.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        target.teleToLocation(loc, ref);
        if (target.equals(activeChar)) {
            activeChar.sendMessage("You have been teleported to " + loc + ", reflection id: " + ref);
        }
    }

    private void teleportCharacter(final Player activeChar, final String Cords) {
        final GameObject target = activeChar.getTarget();
        if (target == null || !target.isPlayer()) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return;
        }
        if (target.getObjectId() == activeChar.getObjectId()) {
            activeChar.sendMessage("You cannot teleport yourself.");
            return;
        }
        teleportTo(activeChar, (Player) target, Cords, activeChar.getReflectionId());
    }

    private void teleportCharacter_offline(final int obj_id, final Location loc) {
        if (obj_id == 0) {
            return;
        }
        Connection con = null;
        PreparedStatement st = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            st = con.prepareStatement("UPDATE characters SET x=?,y=?,z=? WHERE obj_Id=? LIMIT 1");
            st.setInt(1, loc.x);
            st.setInt(2, loc.y);
            st.setInt(3, loc.z);
            st.setInt(4, obj_id);
            st.executeUpdate();
        } catch (Exception ignored) {
        } finally {
            DbUtils.closeQuietly(con, st);
        }
    }

    private void teleportToCharacter(final Player activeChar, final GameObject target) {
        if (target == null) {
            return;
        }
        activeChar.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        activeChar.teleToLocation(target.getLoc(), target.getReflectionId());
        activeChar.sendMessage("You have teleported to " + target);
    }

    private void recallNPC(final Player activeChar) {
        final GameObject obj = activeChar.getTarget();
        if (obj != null && obj.isNpc()) {
            obj.setLoc(activeChar.getLoc());
            ((NpcInstance) obj).broadcastCharInfo();
            activeChar.sendMessage("You teleported npc " + obj.getName() + " to " + activeChar.getLoc() + ".");
        } else {
            activeChar.sendMessage("Target is't npc.");
        }
    }

    private enum Commands {
        admin_show_moves,
        admin_show_moves_other,
        admin_show_teleport,
        admin_teleport_to_character,
        admin_teleportto,
        admin_teleport_to,
        admin_move_to,
        admin_moveto,
        admin_teleport,
        admin_teleport_character,
        admin_recall,
        admin_walk,
        admin_recall_npc,
        admin_gonorth,
        admin_gosouth,
        admin_goeast,
        admin_gowest,
        admin_goup,
        admin_godown,
        admin_tele,
        admin_teleto,
        admin_tele_to,
        admin_instant_move,
        admin_tonpc,
        admin_to_npc,
        admin_toobject,
        admin_setref,
        admin_getref,
        admin_recall_party,
        admin_sendhome
    }
}
