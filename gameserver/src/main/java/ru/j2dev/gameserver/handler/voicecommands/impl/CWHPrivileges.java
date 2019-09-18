package ru.j2dev.gameserver.handler.voicecommands.impl;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.scripts.Functions;

import java.util.List;

public class CWHPrivileges implements IVoicedCommandHandler {
    private final String[] _commandList;

    public CWHPrivileges() {
        _commandList = new String[]{"clan"};
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String args) {
        if (activeChar.getClan() == null) {
            return false;
        }
        if (!"clan".equals(command) || !Config.ALT_ALLOW_CLAN_COMMAND_ALLOW_WH) {
            return false;
        }
        if (Config.ALT_ALLOW_CLAN_COMMAND_ONLY_FOR_CLAN_LEADER && !activeChar.isClanLeader()) {
            return false;
        }
        if ((activeChar.getClanPrivileges() & 0x10) != 0x10) {
            return false;
        }
        if (args != null) {
            final String[] param = args.split(" ");
            if (param.length > 0) {
                if ("allowwh".equalsIgnoreCase(param[0]) && param.length > 1) {
                    final UnitMember cm = activeChar.getClan().getAnyMember(param[1]);
                    if (cm != null && cm.getPlayer() != null) {
                        if (cm.getPlayer().getVarB("canWhWithdraw")) {
                            cm.getPlayer().unsetVar("canWhWithdraw");
                            activeChar.sendMessage("Privilege removed successfully");
                        } else {
                            cm.getPlayer().setVar("canWhWithdraw", "1", -1L);
                            activeChar.sendMessage("Privilege given successfully");
                        }
                    } else if (cm != null) {
                        final int state = mysql.simple_get_int("value", "character_variables", "obj_id=" + cm.getObjectId() + " AND name LIKE 'canWhWithdraw'");
                        if (state > 0) {
                            mysql.set("DELETE FROM `character_variables` WHERE obj_id=" + cm.getObjectId() + " AND name LIKE 'canWhWithdraw' LIMIT 1");
                            activeChar.sendMessage("Privilege removed successfully");
                        } else {
                            mysql.set("INSERT INTO character_variables  (obj_id, type, name, value, expire_time) VALUES (" + cm.getObjectId() + ",'user-var','canWhWithdraw','1',-1)");
                            activeChar.sendMessage("Privilege given successfully");
                        }
                    } else {
                        activeChar.sendMessage("Player not found.");
                    }
                } else if ("list".equalsIgnoreCase(param[0])) {
                    StringBuilder sb = new StringBuilder("SELECT `obj_id` FROM `character_variables` WHERE `obj_id` IN (");
                    final List<UnitMember> members = activeChar.getClan().getAllMembers();
                    for (int i = 0; i < members.size(); ++i) {
                        sb.append(members.get(i).getObjectId());
                        if (i < members.size() - 1) {
                            sb.append(",");
                        }
                    }
                    sb.append(") AND `name`='canWhWithdraw'");
                    final List<Object> list = mysql.get_array(sb.toString());
                    sb = new StringBuilder("<html><body>Clan member Warehouse privilege<br><br><table>");
                    for (final Object o_id : list) {
                        for (final UnitMember m : members) {
                            if (m.getObjectId() == Integer.parseInt(o_id.toString())) {
                                sb.append("<tr><td width=10></td><td width=60>").append(m.getName()).append("</td><td width=20><button width=50 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h user_clan allowwh ").append(m.getName()).append("\" value=\"Remove\">").append("<br></td></tr>");
                            }
                        }
                    }
                    sb.append("<tr><td width=10></td><td width=20><button width=60 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h user_clan\" value=\"Back\"></td></tr></table></body></html>");
                    Functions.show(sb.toString(), activeChar, null);
                    return true;
                }
            }
        }
        final String dialog = HtmCache.getInstance().getNotNull("scripts/services/clan.htm", activeChar);
        Functions.show(dialog, activeChar, null);
        return true;
    }
}
