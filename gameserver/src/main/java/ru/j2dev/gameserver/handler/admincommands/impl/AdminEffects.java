package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.InvisibleType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Earthquake;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.skills.AbnormalEffect;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Util;

import java.util.List;

public class AdminEffects implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().GodMode) {
            return false;
        }
        AbnormalEffect ae = AbnormalEffect.NULL;
        GameObject target = activeChar.getTarget();
        switch (command) {
            case admin_invis:
            case admin_vis: {
                if (activeChar.isInvisible()) {
                    activeChar.setInvisibleType(InvisibleType.NONE);
                    activeChar.broadcastCharInfo();
                    if (activeChar.getPet() != null) {
                        activeChar.getPet().broadcastCharInfo();
                    }
                    activeChar.setVar("gm_vis", "true", -1L);
                    break;
                }
                activeChar.setInvisibleType(InvisibleType.NORMAL);
                activeChar.sendUserInfo(true);
                World.removeObjectFromPlayers(activeChar);
                activeChar.unsetVar("gm_vis");
                break;
            }
            case admin_gmspeed: {
                int val;
                if (wordList.length < 2) {
                    val = 0;
                } else {
                    try {
                        val = Integer.parseInt(wordList[1]);
                    } catch (Exception e) {
                        activeChar.sendMessage("USAGE: //gmspeed value=[0~4]");
                        return false;
                    }
                }
                final List<Effect> superhaste = activeChar.getEffectList().getEffectsBySkillId(7029);
                final int sh_level = (superhaste == null) ? 0 : (superhaste.isEmpty() ? 0 : superhaste.get(0).getSkill().getLevel());
                if (val == 0) {
                    if (sh_level != 0) {
                        activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true);
                    }
                    activeChar.unsetVar("gm_gmspeed");
                    break;
                }
                if (val < 1 || val > 4) {
                    activeChar.sendMessage("USAGE: //gmspeed value=[0..4]");
                    break;
                }
                if (Config.SAVE_GM_EFFECTS) {
                    activeChar.setVar("gm_gmspeed", String.valueOf(val), -1L);
                }
                if (val != sh_level) {
                    if (sh_level != 0) {
                        activeChar.doCast(SkillTable.getInstance().getInfo(7029, sh_level), activeChar, true);
                    }
                    activeChar.doCast(SkillTable.getInstance().getInfo(7029, val), activeChar, true);
                    break;
                }
                break;
            }
            case admin_invul: {
                handleInvul(activeChar, activeChar);
                if (!activeChar.isInvul()) {
                    activeChar.unsetVar("gm_invul");
                    break;
                }
                if (Config.SAVE_GM_EFFECTS) {
                    activeChar.setVar("gm_invul", "true", -1L);
                    break;
                }
                break;
            }
        }
        if (!activeChar.isGM()) {
            return false;
        }
        switch (command) {
            case admin_offline_vis: {
                for (final Player player : GameObjectsStorage.getAllPlayers()) {
                    if (player != null && player.isInOfflineMode()) {
                        player.setInvisibleType(InvisibleType.NONE);
                        player.decayMe();
                        player.spawnMe();
                    }
                }
                break;
            }
            case admin_offline_invis: {
                for (final Player player : GameObjectsStorage.getAllPlayers()) {
                    if (player != null && player.isInOfflineMode()) {
                        player.setInvisibleType(InvisibleType.NORMAL);
                        player.decayMe();
                    }
                }
                break;
            }
            case admin_earthquake: {
                try {
                    final int intensity = Integer.parseInt(wordList[1]);
                    final int duration = Integer.parseInt(wordList[2]);
                    activeChar.broadcastPacket(new Earthquake(activeChar.getLoc(), intensity, duration));
                    break;
                } catch (Exception e) {
                    activeChar.sendMessage("USAGE: //earthquake intensity duration");
                    return false;
                }
            }
            case admin_block: {
                if (target == null || !target.isCreature()) {
                    activeChar.sendPacket(Msg.INVALID_TARGET);
                    return false;
                }
                if (((Creature) target).isBlocked()) {
                    return false;
                }
                ((Creature) target).abortAttack(true, false);
                ((Creature) target).abortCast(true, false);
                ((Creature) target).block();
                activeChar.sendMessage("Target blocked.");
                ((Creature) target).sendMessage("You have been paralyzed by a GM " + activeChar.getName());
                break;
            }
            case admin_unblock: {
                if (target == null || !target.isCreature()) {
                    activeChar.sendPacket(Msg.INVALID_TARGET);
                    return false;
                }
                if (!((Creature) target).isBlocked()) {
                    return false;
                }
                ((Creature) target).unblock();
                activeChar.sendMessage("Target unblocked.");
                ((Creature) target).sendMessage("You have been unblocked by a GM " + activeChar.getName());
                break;
            }
            case admin_changename: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //changename newName");
                    return false;
                }
                if (target == null) {
                    target = activeChar;
                }
                if (!target.isCreature()) {
                    activeChar.sendPacket(Msg.INVALID_TARGET);
                    return false;
                }
                final String oldName = target.getName();
                final String newName = Util.joinStrings(" ", wordList, 1);
                ((Creature) target).setName(newName);
                ((Creature) target).broadcastCharInfo();
                activeChar.sendMessage("Changed name from " + oldName + " to " + newName + ".");
                break;
            }
            case admin_setinvul: {
                if (target == null || !target.isPlayer()) {
                    activeChar.sendPacket(Msg.INVALID_TARGET);
                    return false;
                }
                handleInvul(activeChar, (Player) target);
                break;
            }
            case admin_getinvul: {
                if (target != null && target.isCreature()) {
                    activeChar.sendMessage("Target " + target.getName() + "(object ID: " + target.getObjectId() + ") is " + (((Creature) target).isInvul() ? "" : "NOT ") + "invul");
                    break;
                }
                break;
            }
            case admin_social: {
                int val;
                if (wordList.length < 2) {
                    val = Rnd.get(1, 7);
                } else {
                    try {
                        val = Integer.parseInt(wordList[1]);
                    } catch (NumberFormatException nfe) {
                        activeChar.sendMessage("USAGE: //social value");
                        return false;
                    }
                }
                if (target == null || target == activeChar) {
                    activeChar.broadcastPacket(new SocialAction(activeChar.getObjectId(), val));
                    break;
                }
                if (target.isCreature()) {
                    ((Creature) target).broadcastPacket(new SocialAction(target.getObjectId(), val));
                    break;
                }
                break;
            }
            case admin_abnormal: {
                try {
                    if (wordList.length > 1) {
                        ae = AbnormalEffect.getByName(wordList[1]);
                    }
                } catch (Exception e2) {
                    activeChar.sendMessage("USAGE: //abnormal name");
                    activeChar.sendMessage("//abnormal - Clears all abnormal effects");
                    return false;
                }
                final Creature effectTarget = (target == null) ? activeChar : ((Creature) target);
                if (ae == AbnormalEffect.NULL) {
                    effectTarget.startAbnormalEffect(AbnormalEffect.NULL);
                    effectTarget.sendMessage("Abnormal effects clearned by admin.");
                    if (effectTarget != activeChar) {
                        effectTarget.sendMessage("Abnormal effects clearned.");
                        break;
                    }
                    break;
                } else {
                    effectTarget.startAbnormalEffect(ae);
                    effectTarget.sendMessage("Admin added abnormal effect: " + ae.getName());
                    if (effectTarget != activeChar) {
                        effectTarget.sendMessage("Added abnormal effect: " + ae.getName());
                        break;
                    }
                    break;
                }
            }
            case admin_transform: {
                int val;
                try {
                    val = Integer.parseInt(wordList[1]);
                } catch (Exception e3) {
                    activeChar.sendMessage("USAGE: //transform transform_id");
                    return false;
                }
                activeChar.setTransformation(val);
                break;
            }
            case admin_showmovie: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //showmovie id");
                    return false;
                }
                int id;
                try {
                    id = Integer.parseInt(wordList[1]);
                } catch (NumberFormatException e4) {
                    activeChar.sendMessage("You must specify id");
                    return false;
                }
                activeChar.showQuestMovie(id);
                break;
            }
        }
        return true;
    }

    private void handleInvul(final Player activeChar, final Player target) {
        if (target.isInvul()) {
            target.setIsInvul(false);
            target.stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
            if (target.getPet() != null) {
                target.getPet().setIsInvul(false);
                target.getPet().stopAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
            }
            activeChar.sendMessage(target.getName() + " is now mortal.");
        } else {
            target.setIsInvul(true);
            target.startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
            if (target.getPet() != null) {
                target.getPet().setIsInvul(true);
                target.getPet().startAbnormalEffect(AbnormalEffect.S_INVULNERABLE);
            }
            activeChar.sendMessage(target.getName() + " is now immortal.");
        }
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_invis,
        admin_vis,
        admin_offline_vis,
        admin_offline_invis,
        admin_earthquake,
        admin_block,
        admin_unblock,
        admin_changename,
        admin_gmspeed,
        admin_invul,
        admin_setinvul,
        admin_getinvul,
        admin_social,
        admin_abnormal,
        admin_transform,
        admin_showmovie
    }
}
