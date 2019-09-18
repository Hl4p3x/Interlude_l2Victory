package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.AdminFunctions;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Util;

public class AdminNochannel implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanBanChat) {
            return false;
        }
        int banChatCount = 0;
        int penaltyCount = 0;
        final int banChatCountPerDay = activeChar.getPlayerAccess().BanChatCountPerDay;
        if (banChatCountPerDay > 0) {
            final String count = activeChar.getVar("banChatCount");
            if (count != null) {
                banChatCount = Integer.parseInt(count);
            }
            final String penalty = activeChar.getVar("penaltyChatCount");
            if (penalty != null) {
                penaltyCount = Integer.parseInt(penalty);
            }
            long LastBanChatDayTime = 0L;
            final String time = activeChar.getVar("LastBanChatDayTime");
            if (time != null) {
                LastBanChatDayTime = Long.parseLong(time);
            }
            if (LastBanChatDayTime != 0L) {
                if (System.currentTimeMillis() - LastBanChatDayTime < 86400000L) {
                    if (banChatCount >= banChatCountPerDay) {
                        activeChar.sendMessage("\u0412 \u0441\u0443\u0442\u043a\u0438, \u0432\u044b \u043c\u043e\u0436\u0435\u0442\u0435 \u0432\u044b\u0434\u0430\u0442\u044c \u043d\u0435 \u0431\u043e\u043b\u0435\u0435 " + banChatCount + " \u0431\u0430\u043d\u043e\u0432 \u0447\u0430\u0442\u0430.");
                        return false;
                    }
                } else {
                    int bonus_mod = banChatCount / 10;
                    bonus_mod = 1;
                    if (activeChar.getPlayerAccess().BanChatBonusId > 0 && activeChar.getPlayerAccess().BanChatBonusCount > 0) {
                        int add_count = activeChar.getPlayerAccess().BanChatBonusCount * bonus_mod;
                        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(activeChar.getPlayerAccess().BanChatBonusId);
                        if(item != null) {
                            activeChar.sendMessage("\u0411\u043e\u043d\u0443\u0441 \u0437\u0430 \u043c\u043e\u0434\u0435\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u0438\u0435: " + add_count + " " + item.getName());
                            if (penaltyCount > 0) {
                                activeChar.sendMessage("\u0428\u0442\u0440\u0430\u0444 \u0437\u0430 \u043d\u0430\u0440\u0443\u0448\u0435\u043d\u0438\u044f: " + penaltyCount + " " + item.getName());
                                activeChar.setVar("penaltyChatCount", "" + Math.max(0, penaltyCount - add_count), -1L);
                                add_count -= penaltyCount;
                            }
                            if (add_count > 0) {
                                ItemFunctions.addItem(activeChar, activeChar.getPlayerAccess().BanChatBonusId, add_count, true);
                            }
                        }
                    }
                    activeChar.setVar("LastBanChatDayTime", "" + System.currentTimeMillis(), -1L);
                    activeChar.setVar("banChatCount", "0", -1L);
                    banChatCount = 0;
                }
            } else {
                activeChar.setVar("LastBanChatDayTime", "" + System.currentTimeMillis(), -1L);
            }
        }
        switch (command) {
            case admin_nochannel:
            case admin_nc: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //nochannel charName [period] [reason]");
                    return false;
                }
                int timeval = 30;
                if (wordList.length > 2) {
                    try {
                        timeval = Integer.parseInt(wordList[2]);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                final String msg = AdminFunctions.banChat(activeChar, null, wordList[1], timeval, (wordList.length > 3) ? Util.joinStrings(" ", wordList, 3) : null);
                activeChar.sendMessage(msg);
                if (banChatCountPerDay > -1 && msg.startsWith("\u0412\u044b \u0437\u0430\u0431\u0430\u043d\u0438\u043b\u0438 \u0447\u0430\u0442")) {
                    ++banChatCount;
                    activeChar.setVar("banChatCount", "" + banChatCount, -1L);
                    activeChar.sendMessage("\u0423 \u0432\u0430\u0441 \u043e\u0441\u0442\u0430\u043b\u043e\u0441\u044c " + (banChatCountPerDay - banChatCount) + " \u0431\u0430\u043d\u043e\u0432 \u0447\u0430\u0442\u0430.");
                    break;
                }
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
        admin_nochannel,
        admin_nc
    }
}
