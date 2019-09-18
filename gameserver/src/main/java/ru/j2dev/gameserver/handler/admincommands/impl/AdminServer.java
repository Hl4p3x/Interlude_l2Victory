package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.lang.reflect.Field;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminServer implements IAdminCommandHandler {
    public static void showHelpPage(final Player targetChar, final String filename) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        adminReply.setFile("admin/" + filename);
        targetChar.sendPacket(adminReply);
    }

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Menu) {
            return false;
        }
        switch (command) {
            case admin_server: {
                try {
                    final String val = fullString.substring(13);
                    showHelpPage(activeChar, val);
                } catch (StringIndexOutOfBoundsException ignored) {
                }
                break;
            }
            case admin_check_actor: {
                final GameObject obj = activeChar.getTarget();
                if (obj == null) {
                    activeChar.sendMessage("target == null");
                    return false;
                }
                if (!obj.isCreature()) {
                    activeChar.sendMessage("target is not a character");
                    return false;
                }
                final Creature target = (Creature) obj;
                final CharacterAI ai = target.getAI();
                if (ai == null) {
                    activeChar.sendMessage("ai == null");
                    return false;
                }
                final Creature actor = ai.getActor();
                if (actor == null) {
                    activeChar.sendMessage("actor == null");
                    return false;
                }
                activeChar.sendMessage("actor: " + actor);
                break;
            }
            case admin_setvar: {
                if (wordList.length != 3) {
                    activeChar.sendMessage("Incorrect argument count!!!");
                    return false;
                }
                ServerVariables.set(wordList[1], wordList[2]);
                activeChar.sendMessage("Value changed.");
                break;
            }
            case admin_set_ai_interval: {
                if (wordList.length != 2) {
                    activeChar.sendMessage("Incorrect argument count!!!");
                    return false;
                }
                final int interval = Integer.parseInt(wordList[1]);
                int count = 0;
                int count2 = 0;
                for (final NpcInstance npc : GameObjectsStorage.getNpcs()) {
                    if (npc != null) {
                        if (npc instanceof RaidBossInstance) {
                            continue;
                        }
                        final CharacterAI char_ai = npc.getAI();
                        if (!(char_ai instanceof DefaultAI)) {
                            continue;
                        }
                        try {
                            final Field field = DefaultAI.class.getDeclaredField("AI_TASK_DELAY");
                            field.setAccessible(true);
                            field.set(char_ai, interval);
                            if (!char_ai.isActive()) {
                                continue;
                            }
                            char_ai.stopAITask();
                            ++count;
                            final WorldRegion region = npc.getCurrentRegion();
                            if (region == null || !region.isActive()) {
                                continue;
                            }
                            char_ai.startAITask();
                            count2++;
                        } catch (Exception ignored) {
                        }
                    }
                }
                activeChar.sendMessage(count + " AI stopped, " + count2 + " AI started");
                break;
            }
            case admin_spawn2: {
                final StringTokenizer st = new StringTokenizer(fullString, " ");
                try {
                    st.nextToken();
                    final String id = st.nextToken();
                    int respawnTime = 30;
                    int mobCount = 1;
                    if (st.hasMoreTokens()) {
                        mobCount = Integer.parseInt(st.nextToken());
                    }
                    if (st.hasMoreTokens()) {
                        respawnTime = Integer.parseInt(st.nextToken());
                    }
                    spawnMonster(activeChar, id, respawnTime, mobCount);
                } catch (Exception ignored) {
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

    private void spawnMonster(final Player activeChar, String monsterId, final int respawnTime, final int mobCount) {
        GameObject target = activeChar.getTarget();
        if (target == null) {
            target = activeChar;
        }
        final Pattern pattern = Pattern.compile("[0-9]*");
        final Matcher regexp = pattern.matcher(monsterId);
        NpcTemplate template;
        if (regexp.matches()) {
            final int monsterTemplate = Integer.parseInt(monsterId);
            template = NpcTemplateHolder.getInstance().getTemplate(monsterTemplate);
        } else {
            monsterId = monsterId.replace('_', ' ');
            template = NpcTemplateHolder.getInstance().getTemplateByName(monsterId);
        }
        if (template == null) {
            activeChar.sendMessage("Incorrect monster template.");
            return;
        }
        try {
            final SimpleSpawner spawn = new SimpleSpawner(template);
            spawn.setLoc(target.getLoc());
            spawn.setAmount(mobCount);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            spawn.setReflection(activeChar.getReflection());
            spawn.init();
            if (respawnTime == 0) {
                spawn.stopRespawn();
            }
            activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
        } catch (Exception e) {
            activeChar.sendMessage("Target is not ingame.");
        }
    }

    private enum Commands {
        admin_server,
        admin_check_actor,
        admin_setvar,
        admin_set_ai_interval,
        admin_spawn2
    }
}
