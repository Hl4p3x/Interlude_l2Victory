package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.data.xml.holder.BuyListHolder;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.data.xml.holder.SpawnHolder;
import ru.j2dev.gameserver.data.xml.parser.*;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.tables.FishTable;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Strings;

public class AdminReload implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanReload) {
            return false;
        }
        switch (command) {
            case admin_reload_config: {
                try {
                    Config.load();
                } catch (Exception e) {
                    activeChar.sendMessage("Error: " + e.getMessage() + "!");
                    return false;
                }
                activeChar.sendMessage("Config reloaded!");
                break;
            }
            case admin_reload_multisell: {
                try {
                    MultiSellHolder.getInstance().reload();
                } catch (Exception e) {
                    return false;
                }
                activeChar.sendMessage("Multisell list reloaded!");
                break;
            }
            case admin_reload_gmaccess: {
                try {
                    Config.loadGMAccess();
                    GameObjectsStorage.getPlayers().forEach(player -> {
                        if (!Config.EVERYBODY_HAS_ADMIN_RIGHTS) {
                            player.setPlayerAccess(Config.gmlist.get(player.getObjectId()));
                        } else {
                            player.setPlayerAccess(Config.gmlist.get(0));
                        }
                    });
                } catch (Exception e) {
                    return false;
                }
                activeChar.sendMessage("GMAccess reloaded!");
                break;
            }
            case admin_reload_htm: {
                HtmCache.getInstance().clear();
                activeChar.sendMessage("HTML cache clearned.");
                break;
            }
            case admin_reload_qr: {
                Config.loadQuestRateSettings();
                activeChar.sendMessage("Quest rates reloaded.");
                break;
            }
            case admin_reload_qs: {
                if (fullString.endsWith("all")) {
                    GameObjectsStorage.getPlayers().forEach(this::reloadQuestStates);
                    break;
                }
                final GameObject t = activeChar.getTarget();
                if (t != null && t.isPlayer()) {
                    final Player p = (Player) t;
                    reloadQuestStates(p);
                } else {
                    reloadQuestStates(activeChar);
                }
                break;
            }
            case admin_reload_qs_help: {
                activeChar.sendMessage("");
                activeChar.sendMessage("Quest Help:");
                activeChar.sendMessage("reload_qs_help - This Message.");
                activeChar.sendMessage("reload_qs <selected target> - reload all quest states for target.");
                activeChar.sendMessage("reload_qs <no target or target is not player> - reload quests for self.");
                activeChar.sendMessage("reload_qs all - reload quests for all players in world.");
                activeChar.sendMessage("");
                break;
            }
            case admin_reload_skills: {
                SkillTable.getInstance().reload();
                break;
            }
            case admin_reload_items: {
                ItemTemplateParser.getInstance().reload();
                break;
            }
            case admin_reload_npc: {
                NpcTemplateParser.getInstance().reload();
                break;
            }
            case admin_reload_spawn: {
                ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        SpawnHolder.getInstance().clear();
                        SpawnParser.getInstance().load();
                        SpawnManager.getInstance().reloadAll();
                    }
                });
                break;
            }
            case admin_reload_fish: {
                FishTable.getInstance().reload();
                break;
            }
            case admin_reload_translit: {
                Strings.reload();
                break;
            }
            case admin_reload_shops: {
                BuyListHolder.reload();
            }
            case admin_reload_chatfilters: {
                ChatFilterParser.getInstance().reload();
                break;
            }
            case admin_reload_pets: {
                PetDataTable.getInstance().reload();
                break;
            }
            case admin_reload_locale: {
                StringParser.getInstance().reload();
                break;
            }
            case admin_reload_nobles: {
                NoblessManager.getInstance().LoadNobleses();
                break;
            }
        }
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/reload.htm"));
        return true;
    }

    private void reloadQuestStates(final Player p) {
        for (final QuestState qs : p.getAllQuestsStates()) {
            p.removeQuestState(qs.getQuest().getName());
        }
        Quest.restoreQuestStates(p);
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_reload,
        admin_reload_config,
        admin_reload_multisell,
        admin_reload_gmaccess,
        admin_reload_htm,
        admin_reload_qr,
        admin_reload_qs,
        admin_reload_qs_help,
        admin_reload_skills,
        admin_reload_items,
        admin_reload_npc,
        admin_reload_spawn,
        admin_reload_fish,
        admin_reload_chatfilters,
        admin_reload_translit,
        admin_reload_shops,
        admin_reload_static,
        admin_reload_pets,
        admin_reload_locale,
        admin_reload_nobles
    }
}
