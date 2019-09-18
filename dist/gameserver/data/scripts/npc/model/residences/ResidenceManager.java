package npc.model.residences;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.instances.MerchantInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.ReflectionUtils;
import ru.j2dev.gameserver.utils.TimeUtils;
import ru.j2dev.gameserver.utils.WarehouseFunctions;

import java.util.List;
import java.util.StringTokenizer;

public abstract class ResidenceManager extends MerchantInstance {
    protected static final int COND_FAIL = 0;
    protected static final int COND_SIEGE = 1;
    protected static final int COND_OWNER = 2;
    protected String _siegeDialog;
    protected String _mainDialog;
    protected String _failDialog;
    protected int[] _doors;

    public ResidenceManager(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        setDialogs();
        _doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
    }

    protected void setDialogs() {
        _siegeDialog = getTemplate().getAIParams().getString("siege_dialog", "npcdefault.htm");
        _mainDialog = getTemplate().getAIParams().getString("main_dialog", "npcdefault.htm");
        _failDialog = getTemplate().getAIParams().getString("fail_dialog", "npcdefault.htm");
    }

    protected abstract Residence getResidence();

    protected abstract L2GameServerPacket decoPacket();

    protected abstract int getPrivUseFunctions();

    protected abstract int getPrivSetFunctions();

    protected abstract int getPrivDismiss();

    protected abstract int getPrivDoors();

    public void broadcastDecoInfo() {
        final L2GameServerPacket decoPacket = decoPacket();
        if (decoPacket == null) {
            return;
        }
        for (final Player player : World.getAroundPlayers(this)) {
            player.sendPacket(decoPacket);
        }
    }

    protected int getCond(final Player player) {
        final Residence residence = getResidence();
        final Clan residenceOwner = residence.getOwner();
        if (residenceOwner == null || player.getClan() != residenceOwner) {
            return 0;
        }
        if (residence.getSiegeEvent().isInProgress()) {
            return 1;
        }
        return 2;
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        String filename = null;
        final int cond = getCond(player);
        switch (cond) {
            case 2: {
                filename = _mainDialog;
                break;
            }
            case 1: {
                filename = _siegeDialog;
                break;
            }
            case 0: {
                filename = _failDialog;
                break;
            }
        }
        player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final StringTokenizer st = new StringTokenizer(command, " ");
        final String actualCommand = st.nextToken();
        String val = "";
        if (st.countTokens() >= 1) {
            val = st.nextToken();
        }
        final int cond = getCond(player);
        switch (cond) {
            case 1: {
                showChatWindow(player, _siegeDialog);
            }
            case 0: {
                showChatWindow(player, _failDialog);
            }
            default: {
                if ("banish".equalsIgnoreCase(actualCommand)) {
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("residence/Banish.htm");
                    sendHtmlMessage(player, html);
                } else if ("banish_foreigner".equalsIgnoreCase(actualCommand)) {
                    if (!isHaveRigths(player, getPrivDismiss())) {
                        player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                        return;
                    }
                    getResidence().banishForeigner();
                    return;
                } else if ("Buy".equalsIgnoreCase(actualCommand)) {
                    if ("".equals(val)) {
                        return;
                    }
                    showShopWindow(player, Integer.valueOf(val), true);
                } else {
                    if ("manage_vault".equalsIgnoreCase(actualCommand)) {
                        if ("deposit".equalsIgnoreCase(val)) {
                            WarehouseFunctions.showDepositWindowClan(player);
                        } else if ("withdraw".equalsIgnoreCase(val)) {
                            final int value = Integer.valueOf(st.nextToken());
                            if (value == 99) {
                                final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
                                html2.setFile("residence/clan.htm");
                                html2.replace("%npcname%", getName());
                                player.sendPacket(html2);
                            } else {
                                WarehouseFunctions.showWithdrawWindowClan(player, value);
                            }
                        } else {
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            html.setFile("residence/vault.htm");
                            sendHtmlMessage(player, html);
                        }
                        return;
                    }
                    if ("door".equalsIgnoreCase(actualCommand)) {
                        showChatWindow(player, "residence/door.htm");
                    } else if ("openDoors".equalsIgnoreCase(actualCommand)) {
                        if (isHaveRigths(player, getPrivDoors())) {
                            for (final int i : _doors) {
                                ReflectionUtils.getDoor(i).openMe();
                            }
                            showChatWindow(player, "residence/door.htm");
                        } else {
                            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                        }
                    } else if ("closeDoors".equalsIgnoreCase(actualCommand)) {
                        if (isHaveRigths(player, getPrivDoors())) {
                            for (final int i : _doors) {
                                ReflectionUtils.getDoor(i).closeMe();
                            }
                            showChatWindow(player, "residence/door.htm");
                        } else {
                            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                        }
                    } else if ("functions".equalsIgnoreCase(actualCommand)) {
                        if (!isHaveRigths(player, getPrivUseFunctions())) {
                            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                            return;
                        }
                        if ("tele".equalsIgnoreCase(val)) {
                            if (!getResidence().isFunctionActive(1)) {
                                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                                html.setFile("residence/teleportNotActive.htm");
                                sendHtmlMessage(player, html);
                                return;
                            }
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            html.setFile("residence/teleport.htm");
                            final TeleportLocation[] locs = getResidence().getFunction(1).getTeleports();
                            final StringBuilder teleport_list = new StringBuilder(100 * locs.length);
                            for (final TeleportLocation loc : locs) {
                                final String price = String.valueOf(loc.getPrice());
                                final String nameAddrVal = new CustomMessage(loc.getName(), player, new Object[0]).toString();
                                teleport_list.append("<a action=\"bypass -h scripts_Util:Gatekeeper ");
                                teleport_list.append(loc.getX());
                                teleport_list.append(" ");
                                teleport_list.append(loc.getY());
                                teleport_list.append(" ");
                                teleport_list.append(loc.getZ());
                                teleport_list.append(" ");
                                teleport_list.append(price);
                                teleport_list.append("\" msg=\"811;F;");
                                teleport_list.append(nameAddrVal);
                                teleport_list.append("\">");
                                teleport_list.append(nameAddrVal);
                                teleport_list.append(" - ");
                                teleport_list.append(price);
                                teleport_list.append(" ");
                                teleport_list.append("Adena");
                                teleport_list.append("</a><br1>");
                            }
                            html.replace("%teleList%", teleport_list.toString());
                            sendHtmlMessage(player, html);
                        } else if ("item_creation".equalsIgnoreCase(val)) {
                            if (!getResidence().isFunctionActive(2)) {
                                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                                html.setFile("residence/itemNotActive.htm");
                                sendHtmlMessage(player, html);
                                return;
                            }
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            html.setFile("residence/item.htm");
                            String template = "<button value=\"Buy Item\" action=\"bypass -h npc_%objectId%_Buy %id%\" width=67 height=19 back=\"L2UI.DefaultButton_click\" fore=\"L2UI.DefaultButton\">";
                            template = template.replaceAll("%id%", String.valueOf(getResidence().getFunction(2).getBuylist()[1])).replace("%objectId%", String.valueOf(getObjectId()));
                            html.replace("%itemList%", template);
                            sendHtmlMessage(player, html);
                        } else if ("support".equalsIgnoreCase(val)) {
                            if (!getResidence().isFunctionActive(6)) {
                                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                                html.setFile("residence/supportNotActive.htm");
                                sendHtmlMessage(player, html);
                                return;
                            }
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            html.setFile("residence/support.htm");
                            final Object[][] allBuffs = getResidence().getFunction(6).getBuffs();
                            final StringBuilder support_list = new StringBuilder(allBuffs.length * 50);
                            int i = 0;
                            for (final Object[] buff : allBuffs) {
                                final Skill s = (Skill) buff[0];
                                support_list.append("<a action=\"bypass -h npc_%objectId%_support ");
                                support_list.append(String.valueOf(s.getId()));
                                support_list.append(" ");
                                support_list.append(String.valueOf(s.getLevel()));
                                support_list.append("\">");
                                support_list.append(s.getName());
                                support_list.append(" Lv.");
                                support_list.append(String.valueOf(s.getDisplayLevel()));
                                support_list.append("</a><br1>");
                                if (++i % 5 == 0) {
                                    support_list.append("<br>");
                                }
                            }
                            html.replace("%magicList%", support_list.toString());
                            html.replace("%mp%", String.valueOf(Math.round(getCurrentMp())));
                            html.replace("%all%", Config.ALT_CH_ALL_BUFFS ? "<a action=\"bypass -h npc_%objectId%_support all\">Give all</a><br1><a action=\"bypass -h npc_%objectId%_support allW\">Give warrior</a><br1><a action=\"bypass -h npc_%objectId%_support allM\">Give mystic</a><br>" : "");
                            sendHtmlMessage(player, html);
                        } else if ("back".equalsIgnoreCase(val)) {
                            showChatWindow(player, 0);
                        } else {
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            html.setFile("residence/functions.htm");
                            if (getResidence().isFunctionActive(5)) {
                                html.replace("%xp_regen%", String.valueOf(getResidence().getFunction(5).getLevel()) + "%");
                            } else {
                                html.replace("%xp_regen%", "0%");
                            }
                            if (getResidence().isFunctionActive(3)) {
                                html.replace("%hp_regen%", String.valueOf(getResidence().getFunction(3).getLevel()) + "%");
                            } else {
                                html.replace("%hp_regen%", "0%");
                            }
                            if (getResidence().isFunctionActive(4)) {
                                html.replace("%mp_regen%", String.valueOf(getResidence().getFunction(4).getLevel()) + "%");
                            } else {
                                html.replace("%mp_regen%", "0%");
                            }
                            sendHtmlMessage(player, html);
                        }
                    } else if ("manage".equalsIgnoreCase(actualCommand)) {
                        if (!isHaveRigths(player, getPrivSetFunctions())) {
                            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                            return;
                        }
                        if ("recovery".equalsIgnoreCase(val)) {
                            if (st.countTokens() >= 1) {
                                val = st.nextToken();
                                boolean success = true;
                                if ("hp".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(3, Integer.valueOf(st.nextToken()));
                                } else if ("mp".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(4, Integer.valueOf(st.nextToken()));
                                } else if ("exp".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(5, Integer.valueOf(st.nextToken()));
                                }
                                if (!success) {
                                    player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
                                } else {
                                    broadcastDecoInfo();
                                }
                            }
                            showManageRecovery(player);
                        } else if ("other".equalsIgnoreCase(val)) {
                            if (st.countTokens() >= 1) {
                                val = st.nextToken();
                                boolean success = true;
                                if ("item".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(2, Integer.valueOf(st.nextToken()));
                                } else if ("tele".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(1, Integer.valueOf(st.nextToken()));
                                } else if ("support".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(6, Integer.valueOf(st.nextToken()));
                                }
                                if (!success) {
                                    player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
                                } else {
                                    broadcastDecoInfo();
                                }
                            }
                            showManageOther(player);
                        } else if ("deco".equalsIgnoreCase(val)) {
                            if (st.countTokens() >= 1) {
                                val = st.nextToken();
                                boolean success = true;
                                if ("platform".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(8, Integer.valueOf(st.nextToken()));
                                } else if ("curtain".equalsIgnoreCase(val)) {
                                    success = getResidence().updateFunctions(7, Integer.valueOf(st.nextToken()));
                                }
                                if (!success) {
                                    player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
                                } else {
                                    broadcastDecoInfo();
                                }
                            }
                            showManageDeco(player);
                        } else if ("back".equalsIgnoreCase(val)) {
                            showChatWindow(player, 0);
                        } else {
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            html.setFile("residence/manage.htm");
                            sendHtmlMessage(player, html);
                        }
                        return;
                    } else if ("support".equalsIgnoreCase(actualCommand)) {
                        if (!isHaveRigths(player, getPrivUseFunctions())) {
                            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                            return;
                        }
                        setTarget(player);
                        if ("".equals(val)) {
                            return;
                        }
                        if (!getResidence().isFunctionActive(6)) {
                            return;
                        }
                        if (val.startsWith("all")) {
                            for (final Object[] buff2 : getResidence().getFunction(6).getBuffs()) {
                                if (!"allM".equals(val) || buff2[1] != "W") {
                                    if (!"allW".equals(val) || buff2[1] != "M") {
                                        final Skill s2 = (Skill) buff2[0];
                                        if (!useSkill(s2.getId(), s2.getLevel(), player)) {
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            final int skill_id = Integer.parseInt(val);
                            int skill_lvl = 0;
                            if (st.countTokens() >= 1) {
                                skill_lvl = Integer.parseInt(st.nextToken());
                            }
                            useSkill(skill_id, skill_lvl, player);
                        }
                        onBypassFeedback(player, "functions support");
                        return;
                    }
                }
                super.onBypassFeedback(player, command);
            }
        }
    }

    private boolean useSkill(final int id, final int level, final Player player) {
        final Skill skill = SkillTable.getInstance().getInfo(id, level);
        if (skill == null) {
            player.sendMessage("Invalid skill " + id);
            return true;
        }
        if (skill.getMpConsume() > getCurrentMp()) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("residence/NeedCoolTime.htm");
            html.replace("%mp%", String.valueOf(Math.round(getCurrentMp())));
            sendHtmlMessage(player, html);
            return false;
        }
        altUseSkill(skill, player);
        return true;
    }

    private void sendHtmlMessage(final Player player, final NpcHtmlMessage html) {
        html.replace("%npcname%", HtmlUtils.htmlNpcName(getNpcId()));
        player.sendPacket(html);
    }

    private void replace(final NpcHtmlMessage html, final int type, final String replace1, final String replace2) {
        final boolean proc = type == 3 || type == 4 || type == 5;
        if (getResidence().isFunctionActive(type)) {
            html.replace("%" + replace1 + "%", String.valueOf(getResidence().getFunction(type).getLevel()) + (proc ? "%" : ""));
            html.replace("%" + replace1 + "Price%", String.valueOf(getResidence().getFunction(type).getLease()));
            html.replace("%" + replace1 + "Date%", TimeUtils.toSimpleFormat(getResidence().getFunction(type).getEndTimeInMillis()));
        } else {
            html.replace("%" + replace1 + "%", "0");
            html.replace("%" + replace1 + "Price%", "0");
            html.replace("%" + replace1 + "Date%", "0");
        }
        if (getResidence().getFunction(type) != null && getResidence().getFunction(type).getLevels().size() > 0) {
            StringBuilder out = new StringBuilder("[<a action=\"bypass -h npc_%objectId%_manage " + replace2 + " " + replace1 + " 0\">Stop</a>]");
            for (final int level : getResidence().getFunction(type).getLevels()) {
                out.append("[<a action=\"bypass -h npc_%objectId%_manage ").append(replace2).append(" ").append(replace1).append(" ").append(level).append("\">").append(level).append(proc ? "%" : "").append("</a>]");
            }
            html.replace("%" + replace1 + "Manage%", out.toString());
        } else {
            html.replace("%" + replace1 + "Manage%", "Not Available");
        }
    }

    private void showManageRecovery(final Player player) {
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("residence/edit_recovery.htm");
        replace(html, 5, "exp", "recovery");
        replace(html, 3, "hp", "recovery");
        replace(html, 4, "mp", "recovery");
        sendHtmlMessage(player, html);
    }

    private void showManageOther(final Player player) {
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("residence/edit_other.htm");
        replace(html, 1, "tele", "other");
        replace(html, 6, "support", "other");
        replace(html, 2, "item", "other");
        sendHtmlMessage(player, html);
    }

    private void showManageDeco(final Player player) {
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("residence/edit_deco.htm");
        replace(html, 7, "curtain", "deco");
        replace(html, 8, "platform", "deco");
        sendHtmlMessage(player, html);
    }

    protected boolean isHaveRigths(final Player player, final int rigthsToCheck) {
        return player.getClan() != null && (player.getClanPrivileges() & rigthsToCheck) == rigthsToCheck;
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        final List<L2GameServerPacket> list = super.addPacketList(forPlayer, dropper);
        final L2GameServerPacket p = decoPacket();
        if (p != null) {
            list.add(p);
        }
        return list;
    }
}
