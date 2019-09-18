package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SubClass;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.ClassType;
import ru.j2dev.gameserver.model.base.PlayerClass;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayersManager;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.pledge.Alliance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.SiegeUtils;
import ru.j2dev.gameserver.utils.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public final class VillageMasterInstance extends NpcInstance {
    public VillageMasterInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    public static void setLeader(final Player player, final Clan clan, final SubUnit unit, final UnitMember newLeader) {
        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ClanLeaderWillBeChangedFromS1ToS2", player).addString(clan.getLeaderName()).addString(newLeader.getName()));
        if (Config.CLAN_LEADER_CHANGE_METHOD) {
            if (clan.getLevel() >= 4) {
                if (clan.getLeader() != null) {
                    final Player oldLeaderPlayer = clan.getLeader().getPlayer();
                    if (oldLeaderPlayer != null) {
                        SiegeUtils.removeSiegeSkills(oldLeaderPlayer);
                    }
                }
                final Player newLeaderPlayer = newLeader.getPlayer();
                if (newLeaderPlayer != null) {
                    SiegeUtils.addSiegeSkills(newLeaderPlayer);
                }
            }
            synchronized (clan) {
                unit.setLeader(newLeader, true);
            }
            clan.broadcastClanStatus(true, true, false);
        } else {
            unit.updateDbLeader(newLeader);
            clan.broadcastClanStatus(true, true, false);
        }
    }

    private static boolean checkPlayerForClanLeader(final NpcInstance npc, final Player player) {
        if (player.getClan() == null) {
            npc.showChatWindow(player, "villagemaster/pl_no_pledgeman.htm");
            return false;
        }
        if (!player.isClanLeader()) {
            npc.showChatWindow(player, "villagemaster/pl_err_master.htm");
            return false;
        }
        return true;
    }

    private static void dissolveClan(final NpcInstance npc, final Player player) {
        if (player == null || player.getClan() == null) {
            return;
        }
        final Clan clan = player.getClan();
        if (!player.isClanLeader()) {
            player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return;
        }
        if (clan.isPlacedForDisband()) {
            player.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return;
        }
        if (!clan.canDisband()) {
            player.sendPacket(Msg.YOU_CANNOT_APPLY_FOR_DISSOLUTION_AGAIN_WITHIN_SEVEN_DAYS_AFTER_A_PREVIOUS_APPLICATION_FOR_DISSOLUTION);
            return;
        }
        if (clan.getAllyId() != 0) {
            player.sendPacket(Msg.YOU_CANNOT_DISPERSE_THE_CLANS_IN_YOUR_ALLIANCE);
            return;
        }
        if (clan.isAtWar() > 0) {
            player.sendPacket(Msg.YOU_CANNOT_DISSOLVE_A_CLAN_WHILE_ENGAGED_IN_A_WAR);
            return;
        }
        if (clan.getCastle() != 0 || clan.getHasHideout() != 0) {
            player.sendPacket(Msg.UNABLE_TO_DISPERSE_YOUR_CLAN_OWNS_ONE_OR_MORE_CASTLES_OR_HIDEOUTS);
            return;
        }
        for (final Residence r : ResidenceHolder.getInstance().getResidences()) {
            if (r.getSiegeEvent().getSiegeClan("attackers", clan) != null || r.getSiegeEvent().getSiegeClan("defenders", clan) != null || r.getSiegeEvent().getSiegeClan("defenders_waiting", clan) != null) {
                player.sendPacket(SystemMsg.UNABLE_TO_DISSOLVE_YOUR_CLAN_HAS_REQUESTED_TO_PARTICIPATE_IN_A_CASTLE_SIEGE);
                return;
            }
        }
        clan.placeForDisband();
        clan.broadcastClanStatus(true, true, false);
        npc.showChatWindow(player, "villagemaster/pl009.htm");
    }

    private static void restoreClan(final VillageMasterInstance npc, final Player player) {
        if (!checkPlayerForClanLeader(npc, player)) {
            return;
        }
        final Clan clan = player.getClan();
        if (!clan.isPlacedForDisband()) {
            player.sendPacket(SystemMsg.THERE_ARE_NO_REQUESTS_TO_DISPERSE);
            return;
        }
        clan.unPlaceDisband();
        clan.broadcastClanStatus(true, true, false);
        npc.showChatWindow(player, "villagemaster/pl012.htm");
    }

    @Override
    public void onBypassFeedback(final Player player, String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (command.equals("create_clan_check")) {
            if (player.getLevel() < Config.CHARACTER_MIN_LEVEL_FOR_CLAN_CREATE) {
                showChatWindow(player, "villagemaster/pl002.htm");
            } else if (player.isClanLeader()) {
                showChatWindow(player, "villagemaster/pl003.htm");
            } else if (player.getClan() != null) {
                showChatWindow(player, "villagemaster/pl004.htm");
            } else {
                showChatWindow(player, "villagemaster/pl005.htm");
            }
        } else if (command.equals("disband_clan_check")) {
            if (checkPlayerForClanLeader(this, player)) {
                showChatWindow(player, "villagemaster/pl007.htm");
            }
        } else if (command.equals("restore_clan_check")) {
            if (checkPlayerForClanLeader(this, player)) {
                showChatWindow(player, "villagemaster/pl010.htm");
            }
        } else if (command.startsWith("create_clan") && command.length() > 12) {
            final String val = command.substring(12);
            createClan(this, player, val);
        } else if (command.startsWith("create_academy") && command.length() > 15) {
            final String sub = command.substring(15, command.length());
            createSubPledge(player, sub, -1, 5, "");
        } else if (command.startsWith("create_royal") && command.length() > 15) {
            final String[] sub2 = command.substring(13, command.length()).split(" ", 2);
            if (sub2.length == 2) {
                createSubPledge(player, sub2[1], 100, 6, sub2[0]);
            }
        } else if (command.startsWith("create_knight") && command.length() > 16) {
            final String[] sub2 = command.substring(14, command.length()).split(" ", 2);
            if (sub2.length == 2) {
                createSubPledge(player, sub2[1], 1001, 7, sub2[0]);
            }
        } else if (command.startsWith("assign_subpl_leader") && command.length() > 22) {
            final String[] sub2 = command.substring(20, command.length()).split(" ", 2);
            if (sub2.length == 2) {
                assignSubPledgeLeader(player, sub2[1], sub2[0]);
            }
        } else if (command.startsWith("assign_new_clan_leader") && command.length() > 23) {
            final String val = command.substring(23);
            setLeader(player, val);
        } else if (command.startsWith("cancel_new_clan_leader")) {
            cancelNewLeader(player);
        } else if (command.startsWith("create_ally") && command.length() > 12) {
            final String val = command.substring(12);
            createAlly(player, val);
        } else if (command.startsWith("dissolve_ally")) {
            dissolveAlly(player);
        } else if (command.startsWith("dissolve_clan")) {
            dissolveClan(this, player);
        } else if (command.startsWith("restore_clan")) {
            restoreClan(this, player);
        } else if (command.startsWith("increase_clan_level")) {
            levelUpClan(player);
        } else if (command.startsWith("learn_clan_skills")) {
            NpcInstance.showClanSkillList(player);
        } else if (command.startsWith("ShowCouponExchange")) {
            if (Functions.getItemCount(player, 8869) > 0L || Functions.getItemCount(player, 8870) > 0L) {
                command = "Multisell 800";
            } else {
                command = "Link villagemaster/reflect_weapon_master_noticket.htm";
            }
            super.onBypassFeedback(player, command);
        } else if (command.startsWith("Subclass")) {
            if (player.getPet() != null) {
                player.sendPacket(SystemMsg.A_SUBCLASS_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SERVITOR_OR_PET_IS_SUMMONED);
                return;
            }
            if (player.isActionsDisabled() || player.getTransformation() != 0) {
                player.sendPacket(SystemMsg.SUBCLASSES_MAY_NOT_BE_CREATED_OR_CHANGED_WHILE_A_SKILL_IS_IN_USE);
                return;
            }
            if (player.getWeightPenalty() >= 3) {
                player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_WHILE_YOU_ARE_OVER_YOUR_WEIGHT_LIMIT);
                return;
            }
            if (player.getInventoryLimit() * 0.8 < player.getInventory().getSize()) {
                player.sendPacket(SystemMsg.A_SUBCLASS_CANNOT_BE_CREATED_OR_CHANGED_BECAUSE_YOU_HAVE_EXCEEDED_YOUR_INVENTORY_LIMIT);
                return;
            }
            final StringBuilder content = new StringBuilder("<html><body>");
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            final Map<Integer, SubClass> playerClassList = player.getSubClasses();
            if (player.getLevel() < 40) {
                content.append("You must be level 40 or more to operate with your sub-classes.");
                content.append("</body></html>");
                html.setHtml(content.toString());
                player.sendPacket(html);
                return;
            }
            int classId = 0;
            int newClassId = 0;
            int intVal = 0;
            try {
                for (final String id : command.substring(9, command.length()).split(" ")) {
                    if (intVal == 0) {
                        intVal = Integer.parseInt(id);
                    } else if (classId > 0) {
                        newClassId = Integer.parseInt(id);
                    } else {
                        classId = Integer.parseInt(id);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            switch (intVal) {
                case 1: {
                    final Set<PlayerClass> subsAvailable = getAvailableSubClasses(player, true);
                    if (subsAvailable != null && !subsAvailable.isEmpty()) {
                        content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");
                        if (Config.ALT_ALLOW_SUBCLASS_FOR_CUSTOM_ITEM) {
                            content.append(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubClassPriceForCustomItem", player).addItemName(Config.ALT_SUBCLASS_FOR_CUSTOM_ITEM_ID).addNumber(Config.ALT_SUBCLASS_FOR_CUSTOM_ITEM_COUNT));
                            content.append("<br>");
                        }
                        for (final PlayerClass subClass : subsAvailable) {
                            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 4 ").append(subClass.ordinal()).append("\">").append(HtmlUtils.makeClassNameFString(player, subClass.ordinal())).append("</a><br>");
                        }
                        break;
                    }
                    player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", player));
                    return;
                }
                case 2: {
                    content.append("Change Subclass:<br>");
                    final int baseClassId = player.getBaseClassId();
                    if (playerClassList.size() < 2) {
                        content.append("You can't change subclasses when you don't have a subclass to begin with.<br><a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 1\">Add subclass.</a>");
                        break;
                    }
                    content.append("Which class would you like to switch to?<br>");
                    if (baseClassId == player.getActiveClassId()) {
                        content.append(HtmlUtils.makeClassNameFString(player, baseClassId)).append(" <font color=\"LEVEL\">(Base Class)</font><br><br>");
                    } else {
                        content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(baseClassId).append("\">").append(HtmlUtils.makeClassNameFString(player, baseClassId)).append("</a> <font color=\"LEVEL\">(Base Class)</font><br><br>");
                    }
                    for (final SubClass subClass2 : playerClassList.values()) {
                        if (subClass2.isBase()) {
                            continue;
                        }
                        final int subClassId = subClass2.getClassId();
                        if (subClassId == player.getActiveClassId()) {
                            content.append(HtmlUtils.makeClassNameFString(player, subClassId)).append("<br>");
                        } else {
                            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 5 ").append(subClassId).append("\">").append(HtmlUtils.makeClassNameFString(player, subClassId)).append("</a><br>");
                        }
                    }
                    break;
                }
                case 3: {
                    content.append("Change Subclass:<br>Which of the following sub-classes would you like to change?<br>");
                    for (final SubClass sub3 : playerClassList.values()) {
                        content.append("<br>");
                        if (!sub3.isBase()) {
                            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 6 ").append(sub3.getClassId()).append("\">").append(HtmlUtils.makeClassNameFString(player, sub3.getClassId())).append("</a><br>");
                        }
                    }
                    content.append("<br>If you change a sub-class, you'll start at level 40 after the 2nd class transfer.");
                    break;
                }
                case 4: {
                    boolean allowAddition = true;
                    if (player.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", player).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
                        allowAddition = false;
                    }
                    if (!playerClassList.isEmpty()) {
                        for (final SubClass subClass3 : playerClassList.values()) {
                            if (subClass3.getLevel() < Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS) {
                                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubBeforeLevel", player).addNumber(Config.ALT_GAME_LEVEL_TO_GET_SUBCLASS));
                                allowAddition = false;
                                break;
                            }
                        }
                    }
                    if (player.isInDuel()) {
                        allowAddition = false;
                    }
                    if (Config.OLY_ENABLED && (OlympiadPlayersManager.getInstance().isRegistred(player) || player.isOlyParticipant())) {
                        player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
                        return;
                    }
                    if (!Config.ALT_GAME_SUBCLASS_WITHOUT_QUESTS && !playerClassList.isEmpty() && playerClassList.size() < 2) {
                        if (ArrayUtils.contains(Config.ALT_INITIAL_QUESTS, 705)) {
                            allowAddition = player.isQuestCompleted("_705_PathToSubclass");
                            if (!allowAddition) {
                                player.sendMessage(new CustomMessage("_705_PathToSubclass.NotDone", player));
                            }
                        } else if (player.isQuestCompleted("_234_FatesWhisper")) {
                            allowAddition = player.isQuestCompleted("_235_MimirsElixir");
                            if (!allowAddition) {
                                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestMimirsElixir", player));
                            }
                        } else {
                            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.QuestFatesWhisper", player));
                            allowAddition = false;
                        }
                    }
                    if (Config.ALT_ALLOW_SUBCLASS_FOR_CUSTOM_ITEM && allowAddition) {
                        if (ItemFunctions.getItemCount(player, Config.ALT_SUBCLASS_FOR_CUSTOM_ITEM_ID) < Config.ALT_SUBCLASS_FOR_CUSTOM_ITEM_COUNT) {
                            if (Config.ALT_SUBCLASS_FOR_CUSTOM_ITEM_ID == 57) {
                                player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                            } else {
                                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                            }
                            return;
                        }
                        ItemFunctions.removeItem(player, Config.ALT_SUBCLASS_FOR_CUSTOM_ITEM_ID, Config.ALT_SUBCLASS_FOR_CUSTOM_ITEM_COUNT, true);
                    }
                    if (!allowAddition) {
                        html.setFile("villagemaster/SubClass_Fail.htm");
                        break;
                    }
                    if (!player.addSubClass(classId, true)) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player));
                        return;
                    }
                    content.append("Add Subclass:<br>The subclass of <font color=\"LEVEL\">").append(HtmlUtils.makeClassNameFString(player, classId)).append("</font> has been added.");
                    player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
                    break;
                }
                case 5: {
                    if (Config.OLY_ENABLED && (OlympiadPlayersManager.getInstance().isRegistred(player) || player.isOlyParticipant())) {
                        player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
                        return;
                    }
                    if (player.isInDuel()) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player));
                        return;
                    }
                    player.setActiveSubClass(classId, true);
                    content.append("Change Subclass:<br>Your active subclass is now a <font color=\"LEVEL\">").append(HtmlUtils.makeClassNameFString(player, player.getActiveClassId())).append("</font>.");
                    player.sendPacket(SystemMsg.YOU_HAVE_SUCCESSFULLY_SWITCHED_TO_YOUR_SUBCLASS);
                    break;
                }
                case 6: {
                    content.append("Please choose a subclass to change to. If the one you are looking for is not here, please seek out the appropriate master for that class.<br><font color=\"LEVEL\">Warning!</font> All classes and skills for this class will be removed.<br><br>");
                    final Set<PlayerClass> subsAvailable = getAvailableSubClasses(player, false);
                    if (!subsAvailable.isEmpty()) {
                        for (final PlayerClass subClass4 : subsAvailable) {
                            content.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Subclass 7 ").append(classId).append(" ").append(subClass4.ordinal()).append("\">").append(HtmlUtils.makeClassNameFString(player, subClass4.ordinal())).append("</a><br>");
                        }
                        break;
                    }
                    player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", player));
                    return;
                }
                case 7: {
                    if (Config.OLY_ENABLED && (OlympiadPlayersManager.getInstance().isRegistred(player) || player.isOlyParticipant())) {
                        player.sendPacket(Msg.YOU_CANT_JOIN_THE_OLYMPIAD_WITH_A_SUB_JOB_CHARACTER);
                        return;
                    }
                    if (player.modifySubClass(classId, newClassId)) {
                        content.append("Change Subclass:<br>Your subclass has been changed to <font color=\"LEVEL\">").append(HtmlUtils.makeClassNameFString(player, newClassId)).append("</font>.");
                        player.sendPacket(SystemMsg.THE_NEW_SUBCLASS_HAS_BEEN_ADDED);
                        break;
                    }
                    player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", player));
                    return;
                }
            }
            content.append("</body></html>");
            if (content.length() > 26) {
                html.setHtml(content.toString());
            }
            player.sendPacket(html);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        if(getTemplate().getHtmRoot() != null) {
            return getTemplate().getHtmRoot()+ pom +".htm";
        }
        return "villagemaster/" + pom + ".htm";
    }

    private void createClan(final NpcInstance npc, final Player player, final String clanName) {
        if (player.getLevel() < Config.CHARACTER_MIN_LEVEL_FOR_CLAN_CREATE) {
            player.sendPacket(Msg.YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN);
            return;
        }
        if (player.getClanId() != 0) {
            player.sendPacket(Msg.YOU_HAVE_FAILED_TO_CREATE_A_CLAN);
            return;
        }
        if (!player.canCreateClan()) {
            player.sendPacket(Msg.YOU_MUST_WAIT_10_DAYS_BEFORE_CREATING_A_NEW_CLAN);
            return;
        }
        if (clanName.length() > 16) {
            player.sendPacket(Msg.CLAN_NAMES_LENGTH_IS_INCORRECT);
            return;
        }
        if (!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE)) {
            player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
            return;
        }
        final Clan clan = ClanTable.getInstance().createClan(player, clanName);
        if (clan == null) {
            player.sendPacket(Msg.THIS_NAME_ALREADY_EXISTS);
            return;
        }
        player.sendPacket(clan.listAll());
        player.sendPacket(new PledgeShowInfoUpdate(clan), Msg.CLAN_HAS_BEEN_CREATED);
        player.updatePledgeClass();
        player.broadcastCharInfo();
        npc.showChatWindow(player, "villagemaster/pl006.htm");
    }

    private void cancelNewLeader(final Player leader) {
        if (!leader.isClanLeader()) {
            showChatWindow(leader, "villagemaster/pl_err_master.htm");
            return;
        }
        if (leader.getEvent(SiegeEvent.class) != null) {
            leader.sendMessage(new CustomMessage("scripts.services.Rename.SiegeNow", leader));
            return;
        }
        final Clan clan = leader.getClan();
        final SubUnit mainUnit = clan.getSubUnit(0);
        final UnitMember unitMember = mainUnit.getLeader();
        if (unitMember.getObjectId() != leader.getObjectId() || mainUnit.getNextLeaderObjectId() == 0 || mainUnit.getNextLeaderObjectId() == leader.getObjectId()) {
            showChatWindow(leader, "villagemaster/pl_not_transfer.htm");
            return;
        }
        setLeader(leader, clan, mainUnit, unitMember);
        showChatWindow(leader, "villagemaster/pl_cancel_success.htm");
    }

    private void setLeader(final Player leader, final String newLeader) {
        if (!leader.isClanLeader()) {
            showChatWindow(leader, "villagemaster/pl_err_master.htm");
            return;
        }
        if (leader.getClan().isPlacedForDisband()) {
            leader.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return;
        }
        if (leader.getEvent(SiegeEvent.class) != null) {
            leader.sendMessage(new CustomMessage("scripts.services.Rename.SiegeNow", leader));
            return;
        }
        final Clan clan = leader.getClan();
        final SubUnit mainUnit = clan.getSubUnit(0);
        final UnitMember member = mainUnit.getUnitMember(newLeader);
        if (member == null) {
            showChatWindow(leader, "villagemaster/clan-20.htm");
            return;
        }
        if (member.getLeaderOf() != -128) {
            leader.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.CannotAssignUnitLeader", leader));
            return;
        }
        if (mainUnit.getNextLeaderObjectId() != 0 && mainUnit.getNextLeaderObjectId() != leader.getObjectId()) {
            showChatWindow(leader, "villagemaster/pl_transfer_already.htm");
            return;
        }
        setLeader(leader, clan, mainUnit, member);
        showChatWindow(leader, "villagemaster/pl_transfer_success.htm");
    }

    private void createSubPledge(final Player player, final String clanName, int pledgeType, final int minClanLvl, final String leaderName) {
        UnitMember subLeader = null;
        final Clan clan = player.getClan();
        if (clan == null || !player.isClanLeader()) {
            player.sendPacket(Msg.YOU_HAVE_FAILED_TO_CREATE_A_CLAN);
            return;
        }
        if (!Util.isMatchingRegexp(clanName, Config.CLAN_NAME_TEMPLATE)) {
            player.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
            return;
        }
        final Collection<SubUnit> subPledge = clan.getAllSubUnits();
        for (final SubUnit element : subPledge) {
            if (element.getName().equals(clanName)) {
                player.sendPacket(Msg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
                return;
            }
        }
        if (ClanTable.getInstance().getClanByName(clanName) != null) {
            player.sendPacket(Msg.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME_PLEASE_ENTER_A_DIFFERENT_NAME);
            return;
        }
        if (clan.getLevel() < minClanLvl) {
            player.sendPacket(Msg.THE_CONDITIONS_NECESSARY_TO_CREATE_A_MILITARY_UNIT_HAVE_NOT_BEEN_MET);
            return;
        }
        final SubUnit unit = clan.getSubUnit(0);
        if (pledgeType != -1) {
            subLeader = unit.getUnitMember(leaderName);
            if (subLeader == null) {
                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader", player));
                return;
            }
            if (subLeader.getLeaderOf() != -128) {
                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ItCantBeSubUnitLeader", player));
                return;
            }
        }
        pledgeType = clan.createSubPledge(player, pledgeType, subLeader, clanName);
        if (pledgeType == -128) {
            return;
        }
        clan.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(clan.getSubUnit(pledgeType)));
        SystemMessage sm;
        if (pledgeType == -1) {
            sm = new SystemMessage(1741);
            sm.addString(player.getClan().getName());
        } else if (pledgeType >= 1001) {
            sm = new SystemMessage(1794);
            sm.addString(player.getClan().getName());
        } else if (pledgeType >= 100) {
            sm = new SystemMessage(1795);
            sm.addString(player.getClan().getName());
        } else {
            sm = Msg.CLAN_HAS_BEEN_CREATED;
        }
        player.sendPacket(sm);
        if (subLeader != null) {
            clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subLeader));
            if (subLeader.isOnline()) {
                subLeader.getPlayer().updatePledgeClass();
                subLeader.getPlayer().broadcastCharInfo();
            }
        }
    }

    private void assignSubPledgeLeader(final Player player, final String clanName, final String leaderName) {
        final Clan clan = player.getClan();
        if (clan == null) {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ClanDoesntExist", player));
            return;
        }
        if (!player.isClanLeader()) {
            player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return;
        }
        SubUnit targetUnit = null;
        for (final SubUnit unit : clan.getAllSubUnits()) {
            if (unit.getType() != 0) {
                if (unit.getType() == -1) {
                    continue;
                }
                if (!unit.getName().equalsIgnoreCase(clanName)) {
                    continue;
                }
                targetUnit = unit;
            }
        }
        if (targetUnit == null) {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubUnitNotFound", player));
            return;
        }
        final SubUnit mainUnit = clan.getSubUnit(0);
        final UnitMember subLeader = mainUnit.getUnitMember(leaderName);
        if (subLeader == null) {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader", player));
            return;
        }
        if (subLeader.getObjectId() == mainUnit.getNextLeaderObjectId()) {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.PlayerCantBeAssignedAsSubUnitLeader", player));
            return;
        }
        if (subLeader.getLeaderOf() != -128) {
            player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.ItCantBeSubUnitLeader", player));
            return;
        }
        targetUnit.setLeader(subLeader, true);
        clan.broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(targetUnit));
        clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(subLeader));
        if (subLeader.isOnline()) {
            subLeader.getPlayer().updatePledgeClass();
            subLeader.getPlayer().broadcastCharInfo();
        }
        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NewSubUnitLeaderHasBeenAssigned", player));
    }

    private void levelUpClan(final Player player) {
        final Clan clan = player.getClan();
        if (clan == null) {
            return;
        }
        if (!player.isClanLeader()) {
            player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return;
        }
        if (player.getClan().isPlacedForDisband()) {
            player.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return;
        }
        boolean increaseClanLevel = false;
        switch (clan.getLevel()) {
            case 0: {
                if (player.getSp() >= Config.CLAN_FIRST_LEVEL_SP && player.getAdena() >= Config.CLAN_FIRST_LEVEL_ADENA) {
                    player.setSp(player.getSp() - Config.CLAN_FIRST_LEVEL_SP);
                    player.reduceAdena(Config.CLAN_FIRST_LEVEL_ADENA, true);
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
            case 1: {
                if (player.getSp() >= Config.CLAN_SECOND_LEVEL_SP && player.getAdena() >= Config.CLAN_SECOND_LEVEL_ADENA) {
                    player.setSp(player.getSp() - Config.CLAN_SECOND_LEVEL_SP);
                    player.reduceAdena(Config.CLAN_SECOND_LEVEL_ADENA, true);
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
            case 2: {
                if (player.getSp() >= Config.CLAN_THIRD_LEVEL_SP && player.getInventory().destroyItemByItemId(1419, 1L)) {
                    player.setSp(player.getSp() - Config.CLAN_THIRD_LEVEL_SP);
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
            case 3: {
                if (player.getSp() >= Config.CLAN_FOUR_LEVEL_SP && player.getInventory().destroyItemByItemId(3874, 1L)) {
                    player.setSp(player.getSp() - Config.CLAN_FOUR_LEVEL_SP);
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
            case 4: {
                if (player.getSp() >= Config.CLAN_FIVE_LEVEL_SP && player.getInventory().destroyItemByItemId(3870, 1L)) {
                    player.setSp(player.getSp() - Config.CLAN_FIVE_LEVEL_SP);
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
            case 5: {
                if (clan.getReputationScore() >= Config.CLAN_SIX_LEVEL_CLAN_REPUTATION && clan.getAllSize() >= Config.CLAN_SIX_LEVEL_CLAN_MEMBER_COUNT) {
                    clan.incReputation(-Config.CLAN_SIX_LEVEL_CLAN_REPUTATION, false, "LvlUpClan");
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
            case 6: {
                if (clan.getReputationScore() >= Config.CLAN_SEVEN_LEVEL_CLAN_REPUTATION && clan.getAllSize() >= Config.CLAN_SEVEN_LEVEL_CLAN_MEMBER_COUNT) {
                    clan.incReputation(-Config.CLAN_SEVEN_LEVEL_CLAN_REPUTATION, false, "LvlUpClan");
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
            case 7: {
                if (clan.getReputationScore() >= Config.CLAN_EIGHT_LEVEL_CLAN_REPUTATION && clan.getAllSize() >= Config.CLAN_EIGHT_LEVEL_CLAN_MEMBER_COUNT) {
                    clan.incReputation(-Config.CLAN_EIGHT_LEVEL_CLAN_REPUTATION, false, "LvlUpClan");
                    increaseClanLevel = true;
                    break;
                }
                break;
            }
        }
        if (increaseClanLevel) {
            clan.setLevel(clan.getLevel() + 1);
            clan.updateClanInDB();
            player.broadcastCharInfo();
            doCast(SkillTable.getInstance().getInfo(5103, 1), player, true);
            if (clan.getLevel() >= 4) {
                SiegeUtils.addSiegeSkills(player);
            }
            if (clan.getLevel() == 5) {
                player.sendPacket(Msg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
            }
            final PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
            final PledgeStatusChanged ps = new PledgeStatusChanged(clan);
            for (final UnitMember mbr : clan) {
                if (mbr.isOnline()) {
                    mbr.getPlayer().updatePledgeClass();
                    mbr.getPlayer().sendPacket(Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps);
                    mbr.getPlayer().broadcastCharInfo();
                }
            }
        } else {
            player.sendPacket(Msg.CLAN_HAS_FAILED_TO_INCREASE_SKILL_LEVEL);
        }
    }

    private void createAlly(final Player player, final String allyName) {
        if (!player.isClanLeader()) {
            player.sendPacket(Msg.ONLY_CLAN_LEADERS_MAY_CREATE_ALLIANCES);
            return;
        }
        if (player.getClan().getAllyId() != 0) {
            player.sendPacket(Msg.YOU_ALREADY_BELONG_TO_ANOTHER_ALLIANCE);
            return;
        }
        if (player.getClan().isPlacedForDisband()) {
            player.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return;
        }
        if (allyName.length() > 16) {
            player.sendPacket(Msg.INCORRECT_LENGTH_FOR_AN_ALLIANCE_NAME);
            return;
        }
        if (!Util.isMatchingRegexp(allyName, Config.ALLY_NAME_TEMPLATE)) {
            player.sendPacket(Msg.INCORRECT_ALLIANCE_NAME);
            return;
        }
        if (player.getClan().getLevel() < 5) {
            player.sendPacket(Msg.TO_CREATE_AN_ALLIANCE_YOUR_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
            return;
        }
        if (ClanTable.getInstance().getAllyByName(allyName) != null) {
            player.sendPacket(Msg.THIS_ALLIANCE_NAME_ALREADY_EXISTS);
            return;
        }
        if (!player.getClan().canCreateAlly()) {
            player.sendPacket(Msg.YOU_CANNOT_CREATE_A_NEW_ALLIANCE_WITHIN_1_DAY_AFTER_DISSOLUTION);
            return;
        }
        final Alliance alliance = ClanTable.getInstance().createAlliance(player, allyName);
        if (alliance == null) {
            return;
        }
        player.broadcastCharInfo();
        player.sendMessage("Alliance " + allyName + " has been created.");
    }

    private void dissolveAlly(final Player player) {
        if (player == null || player.getAlliance() == null) {
            return;
        }
        if (!player.isAllyLeader()) {
            player.sendPacket(Msg.FEATURE_AVAILABLE_TO_ALLIANCE_LEADERS_ONLY);
            return;
        }
        if (player.getAlliance().getMembersCount() > 1) {
            player.sendPacket(Msg.YOU_HAVE_FAILED_TO_DISSOLVE_THE_ALLIANCE);
            return;
        }
        ClanTable.getInstance().dissolveAlly(player);
    }

    private Set<PlayerClass> getAvailableSubClasses(final Player player, final boolean isNew) {
        final int charClassId = player.getBaseClassId();
        final Race npcRace = getVillageMasterRace();
        final ClassType npcTeachType = getVillageMasterTeachType();
        final PlayerClass currClass = PlayerClass.values()[charClassId];
        final Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
        if (availSubs == null) {
            return Collections.emptySet();
        }
        availSubs.remove(currClass);
        availSubs.forEach(availSub -> {
            for (final SubClass subClass : player.getSubClasses().values()) {
                if (availSub.ordinal() == subClass.getClassId()) {
                    availSubs.remove(availSub);
                } else {
                    final ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
                    if (parent != null && parent.getId() == subClass.getClassId()) {
                        availSubs.remove(availSub);
                    } else {
                        final ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
                        if (subParent == null || subParent.getId() != availSub.ordinal()) {
                            return;
                        }
                        availSubs.remove(availSub);
                    }
                }
            }
            if (!Config.ALTSUBCLASS_LIST_ALL) {
                if (!availSub.isOfRace(Race.human) && !availSub.isOfRace(Race.elf)) {
                    if (availSub.isOfRace(npcRace)) {
                        return;
                    }
                    availSubs.remove(availSub);
                } else {
                    if (availSub.isOfType(npcTeachType) && npcRace == Race.human) {
                        return;
                    }
                    availSubs.remove(availSub);
                }
            }
        });
        return availSubs;
    }

    private Race getVillageMasterRace() {
        switch (getTemplate().getRace()) {
            case 14: {
                return Race.human;
            }
            case 15: {
                return Race.elf;
            }
            case 16: {
                return Race.darkelf;
            }
            case 17: {
                return Race.orc;
            }
            case 18: {
                return Race.dwarf;
            }
            default: {
                return null;
            }
        }
    }

    private ClassType getVillageMasterTeachType() {
        switch (getNpcId()) {
            case 30022:
            case 30030:
            case 30031:
            case 30032:
            case 30036:
            case 30037:
            case 30067:
            case 30070:
            case 30116:
            case 30117:
            case 30118:
            case 30120:
            case 30129:
            case 30130:
            case 30131:
            case 30132:
            case 30133:
            case 30141:
            case 30188:
            case 30191:
            case 30289:
            case 30305:
            case 30358:
            case 30359:
            case 30404:
            case 30419:
            case 30421:
            case 30422:
            case 30424:
            case 30502:
            case 30507:
            case 30510:
            case 30515:
            case 30537:
            case 30538:
            case 30571:
            case 30572:
            case 30575:
            case 30598:
            case 30614:
            case 30657:
            case 30665:
            case 30682:
            case 30706:
            case 30857:
            case 30858:
            case 30859:
            case 30905:
            case 30906:
            case 30927:
            case 30981:
            case 31279:
            case 31290:
            case 31291:
            case 31328:
            case 31335:
            case 31336:
            case 31348:
            case 31349:
            case 31350:
            case 31424:
            case 31428:
            case 31429:
            case 31452:
            case 31454:
            case 31524:
            case 31581:
            case 31591:
            case 31602:
            case 31613:
            case 31644:
            case 31856:
            case 31968:
            case 31973:
            case 31979:
            case 31980:
            case 32008:
            case 32010:
            case 32019:
            case 32095: {
                return ClassType.Priest;
            }
            case 30017:
            case 30019:
            case 30033:
            case 30034:
            case 30035:
            case 30068:
            case 30069:
            case 30110:
            case 30111:
            case 30112:
            case 30114:
            case 30115:
            case 30144:
            case 30145:
            case 30154:
            case 30158:
            case 30171:
            case 30174:
            case 30175:
            case 30176:
            case 30189:
            case 30190:
            case 30194:
            case 30293:
            case 30330:
            case 30344:
            case 30375:
            case 30377:
            case 30461:
            case 30464:
            case 30473:
            case 30476:
            case 30609:
            case 30610:
            case 30612:
            case 30634:
            case 30635:
            case 30637:
            case 30638:
            case 30639:
            case 30640:
            case 30666:
            case 30680:
            case 30694:
            case 30695:
            case 30696:
            case 30701:
            case 30715:
            case 30717:
            case 30720:
            case 30721:
            case 30854:
            case 30855:
            case 30861:
            case 30864:
            case 30907:
            case 30908:
            case 30912:
            case 30915:
            case 30988:
            case 31001:
            case 31046:
            case 31047:
            case 31048:
            case 31049:
            case 31050:
            case 31051:
            case 31052:
            case 31053:
            case 31281:
            case 31282:
            case 31283:
            case 31285:
            case 31326:
            case 31330:
            case 31331:
            case 31332:
            case 31333:
            case 31337:
            case 31339:
            case 31359:
            case 31415:
            case 31425:
            case 31426:
            case 31427:
            case 31430:
            case 31431:
            case 31605:
            case 31608:
            case 31614:
            case 31620:
            case 31643:
            case 31740:
            case 31755:
            case 31953:
            case 31969:
            case 31970:
            case 31971:
            case 31972:
            case 31976:
            case 31977:
            case 31996:
            case 32056:
            case 32074:
            case 32082:
            case 32083:
            case 32084:
            case 32085:
            case 32086:
            case 32087:
            case 32088:
            case 32089:
            case 32098: {
                return ClassType.Mystic;
            }
            default: {
                return ClassType.Fighter;
            }
        }
    }
}
