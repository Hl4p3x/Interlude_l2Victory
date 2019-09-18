package handler.admincommands;

import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.handler.admincommands.AdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.utils.HtmlUtils;

import java.util.Calendar;
import java.util.Objects;

public class AdminResidence extends ScriptAdminCommand {
    @Override
    public boolean useAdminCommand(final Enum comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditNPC) {
            return false;
        }
        switch (command) {
            case admin_residence_list: {
                final NpcHtmlMessage msg = new NpcHtmlMessage(5);
                msg.setFile("admin/residence/residence_list.htm");
                final StringBuilder replyMSG = new StringBuilder(200);
                ResidenceHolder.getInstance().getResidences().stream().filter(Objects::nonNull).forEach(residence -> {
                    replyMSG.append("<tr><td>");
                    replyMSG.append("<a action=\"bypass -h admin_residence ").append(residence.getId()).append("\">").append(HtmlUtils.htmlResidenceName(residence.getId())).append("</a>");
                    replyMSG.append("</td><td>");
                    final Clan owner = residence.getOwner();
                    if (owner == null) {
                        replyMSG.append("NPC");
                    } else {
                        replyMSG.append(owner.getName());
                    }
                    replyMSG.append("</td></tr>");
                });
                msg.replace("%residence_list%", replyMSG.toString());
                activeChar.sendPacket(msg);
                break;
            }
            case admin_residence: {
                if (wordList.length != 2) {
                    return false;
                }
                final Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
                if (r == null) {
                    return false;
                }
                final SiegeEvent<?, ?> event = (SiegeEvent<?, ?>) r.getSiegeEvent();
                final NpcHtmlMessage msg = new NpcHtmlMessage(5);
                msg.setFile("admin/residence/siege_info.htm");
                msg.replace("%residence%", HtmlUtils.htmlResidenceName(r.getId()));
                msg.replace("%id%", String.valueOf(r.getId()));
                msg.replace("%owner%", (r.getOwner() == null) ? "NPC" : r.getOwner().getName());
                msg.replace("%cycle%", String.valueOf(r.getCycle()));
                msg.replace("%paid_cycle%", String.valueOf(r.getPaidCycle()));
                msg.replace("%reward_count%", String.valueOf(r.getRewardCount()));
                msg.replace("%left_time%", String.valueOf(r.getCycleDelay()));
                final StringBuilder clans = new StringBuilder(100);
                event.getObjects().forEach((key, value) -> {
                    value.stream().filter(o -> o instanceof SiegeClanObject).map(o -> (SiegeClanObject) o).forEach(siegeClanObject -> clans.append("<tr>").append("<td>").append(siegeClanObject.getClan().getName()).append("</td>").append("<td>").append(siegeClanObject.getClan().getLeaderName()).append("</td>").append("<td>").append(siegeClanObject.getType()).append("</td>").append("</tr>"));
                });
                msg.replace("%clans%", clans.toString());
                msg.replace("%hour%", String.valueOf(r.getSiegeDate().get(Calendar.HOUR_OF_DAY)));
                msg.replace("%minute%", String.valueOf(r.getSiegeDate().get(Calendar.MINUTE)));
                msg.replace("%day%", String.valueOf(r.getSiegeDate().get(Calendar.DATE)));
                msg.replace("%month%", String.valueOf(r.getSiegeDate().get(Calendar.MONTH) + 1));
                msg.replace("%year%", String.valueOf(r.getSiegeDate().get(Calendar.YEAR)));
                activeChar.sendPacket(msg);
                break;
            }
            case admin_set_owner: {
                if (wordList.length != 3) {
                    return false;
                }
                final Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
                if (r == null) {
                    return false;
                }
                Clan clan = null;
                final String clanName = wordList[2];
                if (!"npc".equalsIgnoreCase(clanName)) {
                    clan = ClanTable.getInstance().getClanByName(clanName);
                    if (clan == null) {
                        activeChar.sendPacket(SystemMsg.INCORRECT_NAME);
                        AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
                        return false;
                    }
                }
                final SiegeEvent<?, ?> event = (SiegeEvent<?, ?>) r.getSiegeEvent();
                event.clearActions();
                r.getLastSiegeDate().setTimeInMillis((clan == null) ? 0L : System.currentTimeMillis());
                r.getOwnDate().setTimeInMillis((clan == null) ? 0L : System.currentTimeMillis());
                r.changeOwner(clan);
                event.reCalcNextTime(false);
                break;
            }
            case admin_set_siege_time: {
                final Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
                if (r == null) {
                    return false;
                }
                final Calendar calendar = (Calendar) r.getSiegeDate().clone();
                for (int i = 2; i < wordList.length; ++i) {
                    int val = Integer.parseInt(wordList[i]);
                    int type;
                    switch (i) {
                        case 2: {
                            type = 11;
                            break;
                        }
                        case 3: {
                            type = 12;
                            break;
                        }
                        case 4: {
                            type = 5;
                            break;
                        }
                        case 5: {
                            type = 2;
                            --val;
                            break;
                        }
                        case 6: {
                            type = 1;
                            break;
                        }
                        default: {
                            continue;
                        }
                    }
                    calendar.set(type, val);
                }
                final SiegeEvent<?, ?> event = (SiegeEvent<?, ?>) r.getSiegeEvent();
                event.clearActions();
                r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
                event.registerActions();
                r.setJdbcState(JdbcEntityState.UPDATED);
                r.update();
                AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
                break;
            }
            case admin_quick_siege_start: {
                final Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
                if (r == null) {
                    return false;
                }
                final Calendar calendar = Calendar.getInstance();
                if (wordList.length >= 3) {
                    calendar.set(Calendar.SECOND, -Integer.parseInt(wordList[2]));
                }
                final SiegeEvent<?, ?> event = (SiegeEvent<?, ?>) r.getSiegeEvent();
                event.clearActions();
                r.getSiegeDate().setTimeInMillis(calendar.getTimeInMillis());
                event.registerActions();
                r.setJdbcState(JdbcEntityState.UPDATED);
                r.update();
                AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
                break;
            }
            case admin_quick_siege_stop: {
                final Residence r = ResidenceHolder.getInstance().getResidence(Integer.parseInt(wordList[1]));
                if (r == null) {
                    return false;
                }
                final SiegeEvent<?, ?> event = (SiegeEvent<?, ?>) r.getSiegeEvent();
                event.clearActions();
                ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        event.stopEvent();
                    }
                });
                AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_residence " + r.getId());
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
        admin_residence_list,
        admin_residence,
        admin_set_owner,
        admin_set_siege_time,
        admin_quick_siege_start,
        admin_quick_siege_stop
    }
}
