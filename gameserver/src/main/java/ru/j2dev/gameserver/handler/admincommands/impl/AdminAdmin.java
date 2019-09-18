package ru.j2dev.gameserver.handler.admincommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.stats.Stats;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class AdminAdmin implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (activeChar.getPlayerAccess().Menu) {
            switch (command) {
                case admin_admin: {
                    activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
                    break;
                }
                case admin_play_sounds: {
                    if (wordList.length == 1) {
                        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs.htm"));
                        break;
                    }
                    try {
                        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/songs/songs" + wordList[1] + ".htm"));
                    } catch (StringIndexOutOfBoundsException ignored) {
                    }
                    break;
                }
                case admin_play_sound: {
                    try {
                        playAdminSound(activeChar, wordList[1]);
                    } catch (StringIndexOutOfBoundsException ignored) {
                    }
                    break;
                }
                case admin_silence: {
                    if (activeChar.getMessageRefusal()) {
                        activeChar.unsetVar("gm_silence");
                        activeChar.setMessageRefusal(false);
                        activeChar.sendPacket(Msg.MESSAGE_ACCEPTANCE_MODE);
                        activeChar.sendEtcStatusUpdate();
                        break;
                    }
                    if (Config.SAVE_GM_EFFECTS) {
                        activeChar.setVar("gm_silence", "true", -1L);
                    }
                    activeChar.setMessageRefusal(true);
                    activeChar.sendPacket(Msg.MESSAGE_REFUSAL_MODE);
                    activeChar.sendEtcStatusUpdate();
                    break;
                }
                case admin_tradeoff: {
                    try {
                        if ("on".equalsIgnoreCase(wordList[1])) {
                            activeChar.setTradeRefusal(true);
                            Functions.sendDebugMessage(activeChar, "tradeoff enabled");
                        } else if ("off".equalsIgnoreCase(wordList[1])) {
                            activeChar.setTradeRefusal(false);
                            Functions.sendDebugMessage(activeChar, "tradeoff disabled");
                        }
                    } catch (Exception ex2) {
                        if (activeChar.getTradeRefusal()) {
                            Functions.sendDebugMessage(activeChar, "tradeoff currently enabled");
                        } else {
                            Functions.sendDebugMessage(activeChar, "tradeoff currently disabled");
                        }
                    }
                    break;
                }
                case admin_show_html: {
                    final String html = wordList[1];
                    try {
                        if (html != null) {
                            activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + html));
                        } else {
                            Functions.sendDebugMessage(activeChar, "Html page not found");
                        }
                    } catch (Exception npe) {
                        Functions.sendDebugMessage(activeChar, "Html page not found");
                    }
                    break;
                }
                case admin_setnpcstate: {
                    if (wordList.length < 2) {
                        Functions.sendDebugMessage(activeChar, "USAGE: //setnpcstate state");
                        return false;
                    }
                    final GameObject target = activeChar.getTarget();
                    int state;
                    try {
                        state = Integer.parseInt(wordList[1]);
                    } catch (NumberFormatException e) {
                        Functions.sendDebugMessage(activeChar, "You must specify state");
                        return false;
                    }
                    if (!target.isNpc()) {
                        Functions.sendDebugMessage(activeChar, "You must target an NPC");
                        return false;
                    }
                    final NpcInstance npc = (NpcInstance) target;
                    npc.setNpcState(state);
                    break;
                }
                case admin_setareanpcstate: {
                    try {
                        final String val = fullString.substring(15).trim();
                        final String[] vals = val.split(" ");
                        final int range = NumberUtils.toInt(vals[0], 0);
                        final int astate = (vals.length > 1) ? NumberUtils.toInt(vals[1], 0) : 0;
                        for (final NpcInstance n : activeChar.getAroundNpc(range, 200)) {
                            n.setNpcState(astate);
                        }
                    } catch (Exception e2) {
                        Functions.sendDebugMessage(activeChar, "Usage: //setareanpcstate [range] [state]");
                    }
                    break;
                }
                case admin_showmovie: {
                    if (wordList.length < 2) {
                        Functions.sendDebugMessage(activeChar, "USAGE: //showmovie id");
                        return false;
                    }
                    int id;
                    try {
                        id = Integer.parseInt(wordList[1]);
                    } catch (NumberFormatException e3) {
                        Functions.sendDebugMessage(activeChar, "You must specify id");
                        return false;
                    }
                    activeChar.showQuestMovie(id);
                    break;
                }
                case admin_setzoneinfo: {
                    if (wordList.length < 2) {
                        Functions.sendDebugMessage(activeChar, "USAGE: //setzoneinfo id");
                        return false;
                    }
                    int stateid;
                    try {
                        stateid = Integer.parseInt(wordList[1]);
                    } catch (NumberFormatException e4) {
                        Functions.sendDebugMessage(activeChar, "You must specify id");
                        return false;
                    }
                    activeChar.broadcastPacket(new ExChangeClientEffectInfo(stateid));
                    break;
                }
                case admin_eventtrigger: {
                    if (wordList.length < 2) {
                        Functions.sendDebugMessage(activeChar, "USAGE: //eventtrigger id");
                        return false;
                    }
                    int triggerid;
                    try {
                        triggerid = Integer.parseInt(wordList[1]);
                    } catch (NumberFormatException e5) {
                        Functions.sendDebugMessage(activeChar, "You must specify id");
                        return false;
                    }
                    activeChar.broadcastPacket(new EventTrigger(triggerid, true));
                    break;
                }
                case admin_debug: {
                    final GameObject ob = activeChar.getTarget();
                    if (!ob.isPlayer()) {
                        Functions.sendDebugMessage(activeChar, "Only player target is allowed");
                        return false;
                    }
                    final Player pl = ob.getPlayer();
                    final List<String> _s = new ArrayList<>();
                    _s.add("==========TARGET STATS:");
                    _s.add("==Magic Resist: " + pl.calcStat(Stats.MAGIC_RESIST, null, null));
                    _s.add("==Magic Power: " + pl.calcStat(Stats.MAGIC_POWER, 1.0, null, null));
                    _s.add("==Skill Power: " + pl.calcStat(Stats.SKILL_POWER, 1.0, null, null));
                    _s.add("==Cast Break Rate: " + pl.calcStat(Stats.CAST_INTERRUPT, 1.0, null, null));
                    _s.add("==========Powers:");
                    _s.add("==Bleed: " + pl.calcStat(Stats.BLEED_POWER, 1.0, null, null));
                    _s.add("==Poison: " + pl.calcStat(Stats.POISON_POWER, 1.0, null, null));
                    _s.add("==Stun: " + pl.calcStat(Stats.STUN_POWER, 1.0, null, null));
                    _s.add("==Root: " + pl.calcStat(Stats.ROOT_POWER, 1.0, null, null));
                    _s.add("==Mental: " + pl.calcStat(Stats.MENTAL_POWER, 1.0, null, null));
                    _s.add("==Sleep: " + pl.calcStat(Stats.SLEEP_POWER, 1.0, null, null));
                    _s.add("==Paralyze: " + pl.calcStat(Stats.PARALYZE_POWER, 1.0, null, null));
                    _s.add("==Cancel: " + pl.calcStat(Stats.CANCEL_POWER, 1.0, null, null));
                    _s.add("==Debuff: " + pl.calcStat(Stats.DEBUFF_POWER, 1.0, null, null));
                    _s.add("==========PvP Stats:");
                    _s.add("==Phys Attack Dmg: " + pl.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0, null, null));
                    _s.add("==Phys Skill Dmg: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0, null, null));
                    _s.add("==Magic Skill Dmg: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0, null, null));
                    _s.add("==Phys Attack Def: " + pl.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0, null, null));
                    _s.add("==Phys Skill Def: " + pl.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0, null, null));
                    _s.add("==Magic Skill Def: " + pl.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0, null, null));
                    _s.add("==========Reflects:");
                    _s.add("==Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_DAMAGE_CHANCE, null, null));
                    _s.add("==Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_PSKILL_DAMAGE_CHANCE, null, null));
                    _s.add("==Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_AND_BLOCK_MSKILL_DAMAGE_CHANCE, null, null));
                    _s.add("==Counterattack: Phys Dmg Chance: " + pl.calcStat(Stats.REFLECT_DAMAGE_PERCENT, null, null));
                    _s.add("==Counterattack: Phys Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, null, null));
                    _s.add("==Counterattack: Magic Skill Dmg Chance: " + pl.calcStat(Stats.REFLECT_MSKILL_DAMAGE_PERCENT, null, null));
                    _s.add("==========MP Consume Rate:");
                    _s.add("==Magic Skills: " + pl.calcStat(Stats.MP_MAGIC_SKILL_CONSUME, 1.0, null, null));
                    _s.add("==Phys Skills: " + pl.calcStat(Stats.MP_PHYSICAL_SKILL_CONSUME, 1.0, null, null));
                    _s.add("==Music: " + pl.calcStat(Stats.MP_DANCE_SKILL_CONSUME, 1.0, null, null));
                    _s.add("==========Shield:");
                    _s.add("==Shield Defence: " + pl.calcStat(Stats.SHIELD_DEFENCE, null, null));
                    _s.add("==Shield Defence Rate: " + pl.calcStat(Stats.SHIELD_RATE, null, null));
                    _s.add("==Shield Defence Angle: " + pl.calcStat(Stats.SHIELD_ANGLE, null, null));
                    _s.add("==========Etc:");
                    _s.add("==Fatal Blow Rate: " + pl.calcStat(Stats.FATALBLOW_RATE, null, null));
                    _s.add("==Phys Skill Evasion Rate: " + pl.calcStat(Stats.PSKILL_EVASION, null, null));
                    _s.add("==Counterattack Rate: " + pl.calcStat(Stats.COUNTER_ATTACK, null, null));
                    _s.add("==Pole Attack Angle: " + pl.calcStat(Stats.POLE_ATTACK_ANGLE, null, null));
                    _s.add("==Pole Target Count: " + pl.calcStat(Stats.POLE_TARGET_COUNT, 1.0, null, null));
                    _s.add("==========DONE.");
                    for (final String s : _s) {
                        Functions.sendDebugMessage(activeChar, s);
                    }
                    break;
                }
                case admin_uievent: {
                    if (wordList.length < 5) {
                        Functions.sendDebugMessage(activeChar, "USAGE: //uievent isHide doIncrease startTime endTime Text");
                        return false;
                    }
                    boolean hide;
                    boolean increase;
                    int startTime;
                    int endTime;
                    String text;
                    try {
                        hide = Boolean.parseBoolean(wordList[1]);
                        increase = Boolean.parseBoolean(wordList[2]);
                        startTime = Integer.parseInt(wordList[3]);
                        endTime = Integer.parseInt(wordList[4]);
                        text = wordList[5];
                    } catch (NumberFormatException e6) {
                        Functions.sendDebugMessage(activeChar, "Invalid format");
                        return false;
                    }
                    activeChar.broadcastPacket(new ExSendUIEvent(activeChar, hide, increase, startTime, endTime, text));
                    break;
                }
                case admin_forcenpcinfo: {
                    final GameObject obj2 = activeChar.getTarget();
                    if (!obj2.isNpc()) {
                        Functions.sendDebugMessage(activeChar, "Only NPC target is allowed");
                        return false;
                    }
                    ((NpcInstance) obj2).broadcastCharInfo();
                    break;
                }
                case admin_loc: {
                    Functions.sendDebugMessage(activeChar, "Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
                    System.out.println("Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
                    break;
                }
                case admin_spawn_loc: {
                    if (wordList.length > 2) {
                        try {
                            final int sideHLen = Math.max(16, Integer.parseInt(wordList[1])) / 2;
                            final int x0 = activeChar.getLoc().getX() - sideHLen;
                            final int y0 = activeChar.getLoc().getY() - sideHLen;
                            final int x2 = activeChar.getLoc().getX() + sideHLen;
                            final int y2 = activeChar.getLoc().getY() + sideHLen;
                            final int zmin = activeChar.getLoc().getZ();
                            final int zmax = activeChar.getLoc().getZ() + 128;
                            Functions.sendDebugMessage(activeChar, "Spawn location Dumped. Check Server Log");
                            System.out.printf("<spawn name=\"[custom_spawn]\">\n\t<mesh>\n\t\t<vertex x=\"%d\" y=\"%d\" minz=\"%d\" maxz=\"%d\" />\n\t\t<vertex x=\"%d\" y=\"%d\" minz=\"%d\" maxz=\"%d\" />\n\t\t<vertex x=\"%d\" y=\"%d\" minz=\"%d\" maxz=\"%d\" />\n\t\t<vertex x=\"%d\" y=\"%d\" minz=\"%d\" maxz=\"%s\" />\n\t</mesh>\n\t<npc id=\"%s\" count=\"1\" respawn=\"60\" />\n</spawn>\n", x0, y0, zmin, zmax, x2, y0, zmin, zmax, x2, y2, zmin, zmax, x0, y2, zmin, zmax, wordList[2]);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        break;
                    }
                    activeChar.sendMessage("usage: //spawn_loc <side_len> <npc_id>");
                    break;
                }
                case admin_spawn_pos: {
                    if (wordList.length > 1) {
                        Functions.sendDebugMessage(activeChar, "Spawn position Dumped. Check Server Log");
                        System.out.printf("<spawn name=\"[custom_spawn]\">\n\t<npc id=\"%s\" count=\"1\" respawn=\"60\" pos=\"%s\" />\n</spawn>\n", wordList[1], activeChar.getLoc().toXYZHString());
                        break;
                    }
                    activeChar.sendMessage("usage: //spawn_pos <npc_id>");
                    break;
                }
                case admin_locdump: {
                    final StringTokenizer st = new StringTokenizer(fullString, " ");
                    try {
                        st.nextToken();
                        try {
                            new File("dumps").mkdir();
                            final File f = new File("dumps/locdump.txt");
                            if (!f.exists()) {
                                f.createNewFile();
                            }
                            Functions.sendDebugMessage(activeChar, "Coords: X:" + activeChar.getLoc().x + " Y:" + activeChar.getLoc().y + " Z:" + activeChar.getLoc().z + " H:" + activeChar.getLoc().h);
                            final FileWriter writer = new FileWriter(f, true);
                            writer.write("Loc: " + activeChar.getLoc().x + ", " + activeChar.getLoc().y + ", " + activeChar.getLoc().z + "\n");
                            writer.close();
                        } catch (Exception ignored) {
                        }
                    } catch (Exception ignored) {
                    }
                    break;
                }
                case admin_undying: {
                    if (activeChar.isUndying()) {
                        activeChar.setUndying(false);
                        Functions.sendDebugMessage(activeChar, "Undying state has been disabled.");
                        break;
                    }
                    activeChar.setUndying(true);
                    Functions.sendDebugMessage(activeChar, "Undying state has been enabled.");
                    break;
                }
            }
            return true;
        }
        if (activeChar.getPlayerAccess().CanTeleport) {
            switch (command) {
                case admin_show_html: {
                    final String html = wordList[1];
                    try {
                        if (html != null) {
                            if (html.startsWith("tele")) {
                                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/" + html));
                            } else {
                                activeChar.sendMessage("Access denied");
                            }
                        } else {
                            activeChar.sendMessage("Html page not found");
                        }
                    } catch (Exception npe) {
                        activeChar.sendMessage("Html page not found");
                    }
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    public void playAdminSound(final Player activeChar, final String sound) {
        activeChar.broadcastPacket(new PlaySound(sound));
        activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/admin.htm"));
        activeChar.sendMessage("Playing " + sound + ".");
    }

    private enum Commands {
        admin_admin,
        admin_play_sounds,
        admin_play_sound,
        admin_silence,
        admin_tradeoff,
        admin_cfg,
        admin_config,
        admin_show_html,
        admin_setnpcstate,
        admin_setareanpcstate,
        admin_showmovie,
        admin_setzoneinfo,
        admin_eventtrigger,
        admin_debug,
        admin_uievent,
        admin_forcenpcinfo,
        admin_loc,
        admin_spawn_loc,
        admin_spawn_pos,
        admin_locdump,
        admin_undying
    }
}
