package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.commons.text.PrintfFormat;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;

import java.util.Map;

public class AdminQuests implements IAdminCommandHandler {
    private static final PrintfFormat fmtHEAD = new PrintfFormat("<center><font color=\"LEVEL\">%s [id=%d]</font><br><edit var=\"new_val\" width=100 height=12></center><br>");
    private static final PrintfFormat fmtRow = new PrintfFormat("<tr><td>%s</td><td>%s</td><td width=30>%s</td></tr>");
    private static final PrintfFormat fmtSetButton = new PrintfFormat("<button value=\"Set\" action=\"bypass -h admin_quest %d %s %s %s %s\" width=30 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
    private static final PrintfFormat fmtFOOT = new PrintfFormat("<br><br><br><center><button value=\"Clear Quest\" action=\"bypass -h admin_quest %d CLEAR %s\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"> <button value=\"Quests List\" action=\"bypass -h admin_quests %s\" width=100 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></center>");
    private static final PrintfFormat fmtListRow = new PrintfFormat("<tr><td><a action=\"bypass -h admin_quest %d %s\">%s</a></td><td>%s</td></tr>");
    private static final PrintfFormat fmtListNew = new PrintfFormat("<tr><td><edit var=\"new_quest\" width=100 height=12></td><td><button value=\"Add\" action=\"bypass -h admin_quest $new_quest STATE 2 %s\" width=40 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");

    private static boolean ShowQuestState(final QuestState qs, final Player activeChar) {
        final Map<String, String> vars = qs.getVars();
        final int id = qs.getQuest().getQuestIntId();
        final String char_name = qs.getPlayer().getName();
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        final StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append(fmtHEAD.sprintf(qs.getQuest().getClass().getSimpleName(), id));
        replyMSG.append("<table width=260>");
        replyMSG.append(fmtRow.sprintf("PLAYER: ", char_name, ""));
        replyMSG.append(fmtRow.sprintf("STATE: ", qs.getStateName(), fmtSetButton.sprintf(id, "STATE", "$new_val", char_name, "")));
        vars.entrySet().stream().filter(key -> !"<state>".equalsIgnoreCase(key.getKey())).map(key -> fmtRow.sprintf(key + ": ", key.getValue(), fmtSetButton.sprintf(id, "VAR", key.getKey(), "$new_val", char_name))).forEach(replyMSG::append);
        replyMSG.append(fmtRow.sprintf("<edit var=\"new_name\" width=50 height=12>", "~new var~", fmtSetButton.sprintf(id, "VAR", "$new_name", "$new_val", char_name)));
        replyMSG.append("</table>");
        replyMSG.append(fmtFOOT.sprintf(id, char_name, char_name));
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
        vars.clear();
        return true;
    }

    private static boolean ShowQuestList(final Player targetChar, final Player activeChar) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        final StringBuilder replyMSG = new StringBuilder("<html><body><table width=260>");
        for (final QuestState qs : targetChar.getAllQuestsStates()) {
            if (qs != null && qs.getQuest().getQuestIntId() != 255) {
                replyMSG.append(fmtListRow.sprintf(qs.getQuest().getQuestIntId(), targetChar.getName(), qs.getQuest().getName(), qs.getStateName()));
            }
        }
        replyMSG.append(fmtListNew.sprintf(new Object[]{targetChar.getName()}));
        replyMSG.append("</table></body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
        return true;
    }

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditCharAll) {
            return false;
        }
        switch (command) {
            case admin_quests: {
                return ShowQuestList(getTargetChar(wordList, 1, activeChar), activeChar);
            }
            case admin_quest: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //quest id|name [SHOW|STATE|VAR|CLEAR] ...");
                    return true;
                }
                final Quest _quest = QuestManager.getQuest2(wordList[1]);
                if (_quest == null) {
                    activeChar.sendMessage("Quest " + wordList[1] + " undefined");
                    return true;
                }
                if (wordList.length < 3 || "SHOW".equalsIgnoreCase(wordList[2])) {
                    return cmd_Show(_quest, wordList, activeChar);
                }
                if ("STATE".equalsIgnoreCase(wordList[2])) {
                    return cmd_State(_quest, wordList, activeChar);
                }
                if ("VAR".equalsIgnoreCase(wordList[2])) {
                    return cmd_Var(_quest, wordList, activeChar);
                }
                if ("CLEAR".equalsIgnoreCase(wordList[2])) {
                    return cmd_Clear(_quest, wordList, activeChar);
                }
                return cmd_Show(_quest, wordList, activeChar);
            }
            default: {
                return true;
            }
        }
    }

    private boolean cmd_Clear(final Quest _quest, final String[] wordList, final Player activeChar) {
        final Player targetChar = getTargetChar(wordList, 3, activeChar);
        final QuestState qs = targetChar.getQuestState(_quest.getName());
        if (qs == null) {
            activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "]");
            return false;
        }
        qs.exitCurrentQuest(true);
        return ShowQuestList(targetChar, activeChar);
    }

    private boolean cmd_Show(final Quest _quest, final String[] wordList, final Player activeChar) {
        final Player targetChar = getTargetChar(wordList, 3, activeChar);
        final QuestState qs = targetChar.getQuestState(_quest.getName());
        if (qs == null) {
            activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "]");
            return false;
        }
        return ShowQuestState(qs, activeChar);
    }

    private boolean cmd_Var(final Quest _quest, final String[] wordList, final Player activeChar) {
        if (wordList.length < 5) {
            activeChar.sendMessage("USAGE: //quest id|name VAR varname newvalue [target]");
            return false;
        }
        final Player targetChar = getTargetChar(wordList, 5, activeChar);
        final QuestState qs = targetChar.getQuestState(_quest.getName());
        if (qs == null) {
            activeChar.sendMessage("Player " + targetChar.getName() + " havn't Quest [" + _quest.getName() + "], init quest by command:");
            activeChar.sendMessage("//quest id|name STATE 1|2|3 [target]");
            return false;
        }
        if ("~".equalsIgnoreCase(wordList[4]) || "#".equalsIgnoreCase(wordList[4])) {
            qs.unset(wordList[3]);
        } else {
            qs.set(wordList[3], wordList[4]);
        }
        return ShowQuestState(qs, activeChar);
    }

    private boolean cmd_State(final Quest _quest, final String[] wordList, final Player activeChar) {
        if (wordList.length < 4) {
            activeChar.sendMessage("USAGE: //quest id|name STATE 1|2|3 [target]");
            return false;
        }
        int state;
        try {
            state = Integer.parseInt(wordList[3]);
        } catch (Exception e) {
            activeChar.sendMessage("Wrong State ID: " + wordList[3]);
            return false;
        }
        final Player targetChar = getTargetChar(wordList, 4, activeChar);
        QuestState qs = targetChar.getQuestState(_quest.getName());
        if (qs == null) {
            activeChar.sendMessage("Init Quest [" + _quest.getName() + "] for " + targetChar.getName());
            qs = _quest.newQuestState(targetChar, state);
            qs.set("cond", "1");
        } else {
            qs.setState(state);
        }
        return ShowQuestState(qs, activeChar);
    }

    private Player getTargetChar(final String[] wordList, final int wordListIndex, final Player activeChar) {
        if (wordListIndex >= 0 && wordList.length > wordListIndex) {
            final Player player = World.getPlayer(wordList[wordListIndex]);
            if (player == null) {
                activeChar.sendMessage("Can't find player: " + wordList[wordListIndex]);
            }
            return player;
        }
        final GameObject my_target = activeChar.getTarget();
        if (my_target != null && my_target.isPlayer()) {
            return (Player) my_target;
        }
        return activeChar;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_quests,
        admin_quest
    }
}
