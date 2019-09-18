package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.SkillAcquireHolder;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.AcquireType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExEnchantSkillList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillCoolTime;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillList;
import ru.j2dev.gameserver.stats.Calculator;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AdminSkill implements IAdminCommandHandler {
    private static Skill[] adminSkills;

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditChar) {
            return false;
        }
        switch (command) {
            case admin_show_skills: {
                showSkillsPage(activeChar);
                break;
            }
            case admin_show_effects: {
                showEffects(activeChar);
                break;
            }
            case admin_stop_effect: {
                stopEffect(activeChar, wordList);
                break;
            }
            case admin_remove_skills: {
                removeSkillsPage(activeChar);
                break;
            }
            case admin_skill_list: {
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/skills.htm"));
                break;
            }
            case admin_skill_index: {
                if (wordList.length > 1) {
                    activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/skills/" + wordList[1] + ".htm"));
                    break;
                }
                break;
            }
            case admin_add_skill: {
                adminAddSkill(activeChar, wordList);
                break;
            }
            case admin_remove_skill: {
                adminRemoveSkill(activeChar, wordList);
                break;
            }
            case admin_get_skills: {
                adminGetSkills(activeChar);
                break;
            }
            case admin_reset_skills: {
                adminResetSkills(activeChar);
                break;
            }
            case admin_give_all_skills: {
                adminGiveAllSkills(activeChar);
                break;
            }
            case admin_debug_stats: {
                debug_stats(activeChar);
                break;
            }
            case admin_remove_cooldown: {
                final Player target = (activeChar.getTarget() != null) ? activeChar.getTarget().getPlayer() : ((wordList.length > 1) ? GameObjectsStorage.getPlayer(wordList[1]) : null);
                if (target != null) {
                    target.resetReuse();
                    target.sendPacket(new SkillCoolTime(target));
                    activeChar.sendMessage("Skills reuse delay reseted.");
                    break;
                }
                activeChar.sendMessage("Usage: //remove_cooldown [<target>|player_name]");
                break;
            }
            case admin_buff: {
                for (int i = 7041; i <= 7064; ++i) {
                    activeChar.addSkill(SkillTable.getInstance().getInfo(i, 1));
                }
                activeChar.sendPacket(new SkillList(activeChar));
                break;
            }
            case admin_skill_ench:
            case admin_skill_enchant: {
                activeChar.sendPacket(ExEnchantSkillList.packetFor(activeChar));
                break;
            }
        }
        return true;
    }

    private void debug_stats(final Player activeChar) {
        final GameObject target_obj = activeChar.getTarget();
        if (!target_obj.isCreature()) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return;
        }
        final Creature target = (Creature) target_obj;
        final Calculator[] calculators = target.getCalculators();
        StringBuilder log_str = new StringBuilder("--- Debug for " + target.getName() + " ---\r\n");
        for (final Calculator calculator : calculators) {
            if (calculator != null) {
                final Env env = new Env(target, activeChar, null);
                env.value = calculator.getBase();
                log_str.append("Stat: ").append(calculator._stat.getValue()).append(", prevValue: ").append(calculator.getLast()).append("\r\n");
                final Func[] funcs = calculator.getFunctions();
                for (int i = 0; i < funcs.length; ++i) {
                    String order = Integer.toHexString(funcs[i].getOrder()).toUpperCase();
                    if (order.length() == 1) {
                        order = "0" + order;
                    }
                    log_str.append("\tFunc #").append(i).append("@ [0x").append(order).append("]").append(funcs[i].getClass().getSimpleName()).append("\t").append(env.value);
                    if (funcs[i].getCondition() == null || funcs[i].getCondition().test(env)) {
                        funcs[i].calc(env);
                    }
                    log_str.append(" -> ").append(env.value).append((funcs[i].getOwner() != null) ? ("; owner: " + funcs[i].getOwner()) : "; no owner").append("\r\n");
                }
            }
        }
        Log.add(log_str.toString(), "debug_stats");
    }

    private void adminGiveAllSkills(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        Player player;
        if (target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            player = (Player) target;
            int unLearnable = 0;
            int skillCounter = 0;
            for (Collection<SkillLearn> skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL); skills.size() > unLearnable; skills = SkillAcquireHolder.getInstance().getAvailableSkills(player, AcquireType.NORMAL)) {
                unLearnable = 0;
                for (final SkillLearn s : skills) {
                    final Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
                    if (sk == null || !sk.getCanLearn(player.getClassId())) {
                        unLearnable++;
                    } else {
                        if (player.getSkillLevel(sk.getId()) == -1) {
                            skillCounter++;
                        }
                        player.addSkill(sk, true);
                    }
                }
            }
            player.sendMessage("Admin gave you " + skillCounter + " skills.");
            player.sendSkillList();
            activeChar.sendMessage("You gave " + skillCounter + " skills to " + player.getName());
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void removeSkillsPage(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        if (target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Player player = (Player) target;
            List<Skill> skills = new ArrayList<>(player.getAllSkills());
            skills = skills.subList(0, Math.min(skills.size(), 50));
            final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            final StringBuilder replyMSG = new StringBuilder("<html><body>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
            replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_skills\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br><br>");
            replyMSG.append("<center>Editing character: ").append(player.getName()).append("</center>");
            replyMSG.append("<br><table width=270><tr><td>Lv: ").append(player.getLevel()).append(" ").append(player.getTemplate().className).append("</td></tr></table>");
            replyMSG.append("<br><center>Click on the skill you wish to remove:</center>");
            replyMSG.append("<br><table width=270>");
            replyMSG.append("<tr><td width=80>Name:</td><td width=60>Level:</td><td width=40>Id:</td></tr>");
            skills.forEach(element -> replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_remove_skill ").append(element.getId()).append("\">").append(element.getName()).append("</a></td><td width=60>").append(element.getLevel()).append("</td><td width=40>").append(element.getId()).append("</td></tr>"));
            replyMSG.append("</table>");
            replyMSG.append("<br><center><table>");
            replyMSG.append("Remove custom skill:");
            replyMSG.append("<tr><td>Id: </td>");
            replyMSG.append("<td><edit var=\"id_to_remove\" width=110></td></tr>");
            replyMSG.append("</table></center>");
            replyMSG.append("<center><button value=\"Remove skill\" action=\"bypass -h admin_remove_skill $id_to_remove\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></center>");
            replyMSG.append("<br><center><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15></center>");
            replyMSG.append("</body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void showSkillsPage(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        if (target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Player player = (Player) target;
            final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            String replyMSG = "<html><body>" + "<table width=260><tr>" +
                    "<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "<td width=180><center>Character Selection Menu</center></td>" +
                    "<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "</tr></table>" +
                    "<br><br>" +
                    "<center>Editing character: " + player.getName() + "</center>" +
                    "<br><table width=270><tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr></table>" +
                    "<br><center><table>" +
                    "<tr><td><button value=\"Add skills\" action=\"bypass -h admin_skill_list\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "<td><button value=\"Get skills\" action=\"bypass -h admin_get_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>" +
                    "<tr><td><button value=\"Delete skills\" action=\"bypass -h admin_remove_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "<td><button value=\"Reset skills\" action=\"bypass -h admin_reset_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>" +
                    "<tr><td><button value=\"Give All Skills\" action=\"bypass -h admin_give_all_skills\" width=70 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>" +
                    "</table></center>" +
                    "</body></html>";
            adminReply.setHtml(replyMSG);
            activeChar.sendPacket(adminReply);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void showEffects(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        if (target != null && target.isPlayable() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Playable playable = (Playable) target;
            final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            final StringBuilder replyMSG = new StringBuilder("<html><body>");
            replyMSG.append("<table width=260><tr>");
            replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
            replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
            replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
            replyMSG.append("</tr></table>");
            replyMSG.append("<br><br>");
            replyMSG.append("<center>Editing character: ").append(playable.getName()).append("</center>");
            replyMSG.append("<br><center><button value=\"Refresh\" action=\"bypass -h admin_show_effects\" width=100 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\" /></center>");
            replyMSG.append("<br>");
            final List<Effect> list = playable.getEffectList().getAllEffects();
            if (list != null && !list.isEmpty()) {
                for (final Effect e : list) {
                    replyMSG.append("&nbsp;<a action=\"bypass -h admin_stop_effect ").append(e.getSkill().getId()).append("\">");
                    replyMSG.append(e.getSkill().getName()).append(" ").append(e.getSkill().getLevel());
                    replyMSG.append("</a> - ").append(e.getSkill().isToggle() ? "Infinity" : (e.getTimeLeft() + " seconds")).append("<br1>");
                }
            }
            replyMSG.append("<br></body></html>");
            adminReply.setHtml(replyMSG.toString());
            activeChar.sendPacket(adminReply);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void stopEffect(final Player activeChar, final String[] wordList) {
        final GameObject target = activeChar.getTarget();
        if (target != null && target.isPlayable() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Playable playable = (Playable) target;
            if (wordList.length == 2) {
                final int id = Integer.parseInt(wordList[1]);
                final List<Effect> effects = playable.getEffectList().getEffectsBySkillId(id);
                if (effects != null && !effects.isEmpty()) {
                    for (final Effect eff : effects) {
                        eff.exit();
                        playable.getPlayer().sendMessage("Admin removed effect of " + eff.getSkill().getName() + ".");
                        playable.sendChanges();
                        playable.updateStats();
                        playable.updateEffectIcons();
                        playable.broadcastStatusUpdate();
                        activeChar.sendMessage("You removed effect of " + eff.getSkill().getName() + " from " + playable.getName() + ".");
                    }
                } else {
                    activeChar.sendMessage("Error: there is no such skill.");
                }
            }
            showEffects(activeChar);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void adminGetSkills(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        if (target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Player player = (Player) target;
            if (player.getName().equals(activeChar.getName())) {
                player.sendMessage("There is no point in doing it on your character.");
            } else {
                final Collection<Skill> skills = player.getAllSkills();
                adminSkills = activeChar.getAllSkillsArray();
                for (final Skill element : adminSkills) {
                    activeChar.removeSkill(element, true);
                }
                for (final Skill element2 : skills) {
                    activeChar.addSkill(element2, true);
                }
                activeChar.sendMessage("You now have all the skills of  " + player.getName() + ".");
            }
            showSkillsPage(activeChar);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void adminResetSkills(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        Player player;
        if (target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            player = (Player) target;
            final Skill[] skills = player.getAllSkillsArray();
            int counter = 0;
            for (final Skill skill : skills) {
                if (!skill.isClanSkill() && !skill.isCommon() && !SkillAcquireHolder.getInstance().isSkillPossible(player, skill)) {
                    player.removeSkill(skill, true);
                    player.removeSkillFromShortCut(skill.getId());
                    ++counter;
                }
            }
            player.checkSkills();
            player.sendSkillList();
            player.sendMessage("[GM]" + activeChar.getName() + " has updated your skills.");
            activeChar.sendMessage(counter + " skills removed.");
            showSkillsPage(activeChar);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void adminAddSkill(final Player activeChar, final String[] wordList) {
        final GameObject target = activeChar.getTarget();
        if (target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Player player = (Player) target;
            if (wordList.length == 3) {
                final int id = Integer.parseInt(wordList[1]);
                final int level = Integer.parseInt(wordList[2]);
                final Skill skill = SkillTable.getInstance().getInfo(id, level);
                if (skill != null) {
                    player.sendMessage("Admin gave you the skill " + skill.getName() + ".");
                    player.addSkill(skill, true);
                    player.sendSkillList();
                    activeChar.sendMessage("You gave the skill " + skill.getName() + " to " + player.getName() + ".");
                } else {
                    activeChar.sendMessage("Error: there is no such skill.");
                }
            }
            showSkillsPage(activeChar);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void adminRemoveSkill(final Player activeChar, final String[] wordList) {
        final GameObject target = activeChar.getTarget();
        Player player;
        if (target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            player = (Player) target;
            if (wordList.length == 2) {
                final int id = Integer.parseInt(wordList[1]);
                final int level = player.getSkillLevel(id);
                final Skill skill = SkillTable.getInstance().getInfo(id, level);
                if (skill != null) {
                    player.sendMessage("Admin removed the skill " + skill.getName() + ".");
                    player.removeSkill(skill, true);
                    player.sendSkillList();
                    activeChar.sendMessage("You removed the skill " + skill.getName() + " from " + player.getName() + ".");
                } else {
                    activeChar.sendMessage("Error: there is no such skill.");
                }
            }
            removeSkillsPage(activeChar);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private enum Commands {
        admin_show_skills,
        admin_remove_skills,
        admin_skill_list,
        admin_skill_index,
        admin_add_skill,
        admin_remove_skill,
        admin_get_skills,
        admin_reset_skills,
        admin_give_all_skills,
        admin_show_effects,
        admin_stop_effect,
        admin_debug_stats,
        admin_remove_cooldown,
        admin_buff,
        admin_skill_ench,
        admin_skill_enchant
    }
}
