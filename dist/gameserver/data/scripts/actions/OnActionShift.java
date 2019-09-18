package actions;

import org.apache.commons.lang3.StringUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.handler.admincommands.impl.AdminEditChar;
import ru.j2dev.gameserver.model.AggroList.HateComparator;
import ru.j2dev.gameserver.model.AggroList.HateInfo;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.PetInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.spawn.SpawnTemplate;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.PositionUtils;
import ru.j2dev.gameserver.utils.Util;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class OnActionShift extends Functions {
    public boolean OnActionShift_NpcInstance(final Player player, final GameObject object) {
        if (player == null || object == null) {
            return false;
        }
        if (!Config.ALLOW_NPC_SHIFTCLICK && !player.isGM()) {
            if (Config.ALT_GAME_SHOW_DROPLIST && object.isNpc()) {
                final NpcInstance npc = (NpcInstance) object;
                if (npc.isDead()) {
                    return false;
                }
                droplist(player, npc);
            }
            return false;
        }
        if (object.isNpc()) {
            final NpcInstance npc = (NpcInstance) object;
            if (npc.isDead()) {
                return false;
            }
            String dialog;
            if (Config.ALT_FULL_NPC_STATS_PAGE) {
                dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.onActionShift.full.htm", player);
                if (npc.getSpawn() != null && npc.getSpawn() instanceof HardSpawner) {
                    final HardSpawner hardSpawner = (HardSpawner) npc.getSpawn();
                    final SpawnTemplate spawnTemplate = hardSpawner.getTemplate();
                    dialog = dialog.replaceFirst("%maker%", (spawnTemplate != null && spawnTemplate.getMakerName() != null) ? spawnTemplate.getMakerName() : "");
                    final String b = npc.getEvents().stream().map(e -> e + ";").collect(Collectors.joining("", hardSpawner.getTemplate().getEventName() + "|", ""));
                    dialog = dialog.replaceFirst("%event%", b);
                } else {
                    dialog = dialog.replaceFirst("%maker%", "");
                    final String b2 = npc.getEvents().stream().map(e2 -> e2 + ";").collect(Collectors.joining("", "", ""));
                    dialog = dialog.replaceFirst("%event%", b2);
                }
                dialog = dialog.replaceFirst("%class%", String.valueOf(npc.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
                dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
                dialog = dialog.replaceFirst("%respawn%", (npc.getSpawn() != null) ? Util.formatTime((int) ((npc.getSpawn().getRespawnCron() != null) ? ((npc.getSpawn().getRespawnCron().next(System.currentTimeMillis()) - System.currentTimeMillis()) / 1000L) : npc.getSpawn().getRespawnDelay())) : "0");
                dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
                dialog = dialog.replaceFirst("%evs%", String.valueOf(npc.getEvasionRate(null)));
                dialog = dialog.replaceFirst("%acc%", String.valueOf(npc.getAccuracy()));
                dialog = dialog.replaceFirst("%crt%", String.valueOf(npc.getCriticalHit(null, null)));
                dialog = dialog.replaceFirst("%aspd%", String.valueOf(npc.getPAtkSpd()));
                dialog = dialog.replaceFirst("%cspd%", String.valueOf(npc.getMAtkSpd()));
                dialog = dialog.replaceFirst("%currentMP%", String.valueOf(npc.getCurrentMp()));
                dialog = dialog.replaceFirst("%currentHP%", String.valueOf(npc.getCurrentHp()));
                dialog = dialog.replaceFirst("%loc%", "");
                dialog = dialog.replaceFirst("%dist%", String.valueOf((int) npc.getDistance3D(player)));
                dialog = dialog.replaceFirst("%killed%", String.valueOf(0));
                dialog = dialog.replaceFirst("%spReward%", String.valueOf(npc.getSpReward()));
                dialog = dialog.replaceFirst("%xyz%", npc.getLoc().x + " " + npc.getLoc().y + " " + npc.getLoc().z);
                dialog = dialog.replaceFirst("%ai_type%", npc.getAI().getClass().getSimpleName());
                dialog = dialog.replaceFirst("%direction%", PositionUtils.getDirectionTo(npc, player).toString().toLowerCase());
                dialog = dialog.replaceFirst("%spawner%", "");
            } else {
                dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.onActionShift.htm", player);
            }
            dialog = dialog.replaceFirst("%name%", nameNpc(npc));
            dialog = dialog.replaceFirst("%id%", String.valueOf(npc.getNpcId()));
            dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
            dialog = dialog.replaceFirst("%factionId%", String.valueOf(npc.getFaction()));
            dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
            dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
            dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
            dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
            dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
            dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
            dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
            dialog = dialog.replaceFirst("%expReward%", String.valueOf(npc.getExpReward()));
            dialog = dialog.replaceFirst("%spReward%", String.valueOf(npc.getSpReward()));
            dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));
            if (player.isGM()) {
                dialog = dialog.replaceFirst("%AI%", String.valueOf(npc.getAI()) + ",<br1>active: " + npc.getAI().isActive() + ",<br1>intention: " + npc.getAI().getIntention());
            } else {
                dialog = dialog.replaceFirst("%AI%", "");
            }
            show(dialog, player, npc);
        }
        return true;
    }

    public String getNpcRaceById(final int raceId) {
        switch (raceId) {
            case 1: {
                return "Undead";
            }
            case 2: {
                return "Magic Creatures";
            }
            case 3: {
                return "Beasts";
            }
            case 4: {
                return "Animals";
            }
            case 5: {
                return "Plants";
            }
            case 6: {
                return "Humanoids";
            }
            case 7: {
                return "Spirits";
            }
            case 8: {
                return "Angels";
            }
            case 9: {
                return "Demons";
            }
            case 10: {
                return "Dragons";
            }
            case 11: {
                return "Giants";
            }
            case 12: {
                return "Bugs";
            }
            case 13: {
                return "Fairies";
            }
            case 14: {
                return "Humans";
            }
            case 15: {
                return "Elves";
            }
            case 16: {
                return "Dark Elves";
            }
            case 17: {
                return "Orcs";
            }
            case 18: {
                return "Dwarves";
            }
            case 19: {
                return "Others";
            }
            case 20: {
                return "Non-living Beings";
            }
            case 21: {
                return "Siege Weapons";
            }
            case 22: {
                return "Defending Army";
            }
            case 23: {
                return "Mercenaries";
            }
            case 24: {
                return "Unknown Creature";
            }
            case 25: {
                return "Kamael";
            }
            default: {
                return "Not defined";
            }
        }
    }

    public void droplist() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        droplist(player, npc);
    }

    public void droplist(final Player player, final NpcInstance npc) {
        if (player == null || npc == null) {
            return;
        }
        if (Config.ALT_GAME_SHOW_DROPLIST) {
            RewardListInfo.showRewardHtml(player, npc);
        }
    }

    public void quests() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        final StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
        dialog.append(nameNpc(npc)).append("<br></font></center><br>");
        final Map<QuestEventType, Quest[]> list = npc.getTemplate().getQuestEvents();
        for (final Entry<QuestEventType, Quest[]> entry : list.entrySet()) {
            for (final Quest q : entry.getValue()) {
                dialog.append(entry.getKey()).append(" ").append(q.getClass().getSimpleName()).append("<br1>");
            }
        }
        dialog.append("</body></html>");
        show(dialog.toString(), player, npc);
    }

    public void skills() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        final StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
        dialog.append(nameNpc(npc)).append("<br></font></center>");
        final Collection<Skill> list = npc.getAllSkills();
        if (list != null && !list.isEmpty()) {
            dialog.append("<br>Active:<br>");
            for (final Skill s : list) {
                if (s.isActive()) {
                    dialog.append(s.getName()).append("<br1>");
                }
            }
            dialog.append("<br>Passive:<br>");
            for (final Skill s : list) {
                if (!s.isActive()) {
                    dialog.append(s.getName()).append("<br1>");
                }
            }
        }
        dialog.append("</body></html>");
        show(dialog.toString(), player, npc);
    }

    public void effects() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        final StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
        dialog.append(nameNpc(npc)).append("<br></font></center><br>");
        final List<Effect> list = npc.getEffectList().getAllEffects();
        if (list != null && !list.isEmpty()) {
            for (final Effect e : list) {
                dialog.append(e.getSkill().getName()).append("<br1>");
            }
        }
        dialog.append("<br><center><button value=\"");
        dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
        dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:effects\" width=100 height=15 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" /></center></body></html>");
        show(dialog.toString(), player, npc);
    }

    public void stats() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        String dialog = HtmCache.getInstance().getNotNull("scripts/actions/player.L2NpcInstance.stats.htm", player);
        dialog = dialog.replaceFirst("%name%", nameNpc(npc));
        dialog = dialog.replaceFirst("%level%", String.valueOf(npc.getLevel()));
        dialog = dialog.replaceFirst("%factionId%", String.valueOf(npc.getFaction()));
        dialog = dialog.replaceFirst("%aggro%", String.valueOf(npc.getAggroRange()));
        dialog = dialog.replaceFirst("%race%", getNpcRaceById(npc.getTemplate().getRace()));
        dialog = dialog.replaceFirst("%maxHp%", String.valueOf(npc.getMaxHp()));
        dialog = dialog.replaceFirst("%maxMp%", String.valueOf(npc.getMaxMp()));
        dialog = dialog.replaceFirst("%pDef%", String.valueOf(npc.getPDef(null)));
        dialog = dialog.replaceFirst("%mDef%", String.valueOf(npc.getMDef(null, null)));
        dialog = dialog.replaceFirst("%pAtk%", String.valueOf(npc.getPAtk(null)));
        dialog = dialog.replaceFirst("%mAtk%", String.valueOf(npc.getMAtk(null, null)));
        dialog = dialog.replaceFirst("%accuracy%", String.valueOf(npc.getAccuracy()));
        dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(npc.getEvasionRate(null)));
        dialog = dialog.replaceFirst("%criticalHit%", String.valueOf(npc.getCriticalHit(null, null)));
        dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(npc.getRunSpeed()));
        dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(npc.getWalkSpeed()));
        dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(npc.getPAtkSpd()));
        dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(npc.getMAtkSpd()));
        show(dialog, player, npc);
    }

    public void resists() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        final StringBuilder dialog = new StringBuilder("<html><body><center><font color=\"LEVEL\">");
        dialog.append(nameNpc(npc)).append("<br></font></center><table width=\"80%\">");
        boolean hasResist = addResist(dialog, "Fire", npc.calcStat(Stats.DEFENCE_FIRE, 0.0, null, null));
        hasResist |= addResist(dialog, "Wind", npc.calcStat(Stats.DEFENCE_WIND, 0.0, null, null));
        hasResist |= addResist(dialog, "Water", npc.calcStat(Stats.DEFENCE_WATER, 0.0, null, null));
        hasResist |= addResist(dialog, "Earth", npc.calcStat(Stats.DEFENCE_EARTH, 0.0, null, null));
        hasResist |= addResist(dialog, "Light", npc.calcStat(Stats.DEFENCE_HOLY, 0.0, null, null));
        hasResist |= addResist(dialog, "Darkness", npc.calcStat(Stats.DEFENCE_UNHOLY, 0.0, null, null));
        hasResist |= addResist(dialog, "Bleed", npc.calcStat(Stats.BLEED_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Poison", npc.calcStat(Stats.POISON_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Stun", npc.calcStat(Stats.STUN_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Root", npc.calcStat(Stats.ROOT_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Sleep", npc.calcStat(Stats.SLEEP_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Paralyze", npc.calcStat(Stats.PARALYZE_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Mental", npc.calcStat(Stats.MENTAL_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Debuff", npc.calcStat(Stats.DEBUFF_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Cancel", npc.calcStat(Stats.CANCEL_RESIST, 0.0, null, null));
        hasResist |= addResist(dialog, "Sword", 100.0 - npc.calcStat(Stats.SWORD_WPN_VULNERABILITY, null, null));
        hasResist |= addResist(dialog, "Dual Sword", 100.0 - npc.calcStat(Stats.DUAL_WPN_VULNERABILITY, null, null));
        hasResist |= addResist(dialog, "Blunt", 100.0 - npc.calcStat(Stats.BLUNT_WPN_VULNERABILITY, null, null));
        hasResist |= addResist(dialog, "Dagger", 100.0 - npc.calcStat(Stats.DAGGER_WPN_VULNERABILITY, null, null));
        hasResist |= addResist(dialog, "Bow", 100.0 - npc.calcStat(Stats.BOW_WPN_VULNERABILITY, null, null));
        hasResist |= addResist(dialog, "Polearm", 100.0 - npc.calcStat(Stats.POLE_WPN_VULNERABILITY, null, null));
        hasResist |= addResist(dialog, "Fist", 100.0 - npc.calcStat(Stats.FIST_WPN_VULNERABILITY, null, null));
        if (!hasResist) {
            dialog.append("</table>No resists</body></html>");
        } else {
            dialog.append("</table></body></html>");
        }
        show(dialog.toString(), player, npc);
    }

    private boolean addResist(final StringBuilder dialog, final String name, final double val) {
        if (val == 0.0) {
            return false;
        }
        dialog.append("<tr><td>").append(name).append("</td><td>");
        if (val == Double.POSITIVE_INFINITY) {
            dialog.append("MAX");
        } else {
            if (val != Double.NEGATIVE_INFINITY) {
                dialog.append((int) val);
                dialog.append("</td></tr>");
                return true;
            }
            dialog.append("MIN");
        }
        dialog.append("</td></tr>");
        return true;
    }

    public void aggro() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        final StringBuilder dialog = new StringBuilder("<html><body><table width=\"80%\"><tr><td>Attacker</td><td>Damage</td><td>Hate</td></tr>");
        final Set<HateInfo> set = new TreeSet<>(HateComparator.getInstance());
        set.addAll(npc.getAggroList().getCharMap().values());
        for (final HateInfo aggroInfo : set) {
            dialog.append("<tr><td>").append(aggroInfo.attacker.getName()).append("</td><td>").append(aggroInfo.damage).append("</td><td>").append(aggroInfo.hate).append("</td></tr>");
        }
        dialog.append("</table><br><center><button value=\"");
        dialog.append(player.isLangRus() ? "Обновить" : "Refresh");
        dialog.append("\" action=\"bypass -h scripts_actions.OnActionShift:aggro\" width=100 height=15 back=\"L2UI_CH3.bigbutton2_down\" fore=\"L2UI_CH3.bigbutton2\" /></center></body></html>");
        show(dialog.toString(), player, npc);
    }

    public boolean OnActionShift_DoorInstance(final Player player, final GameObject object) {
        if (player == null || object == null || !player.getPlayerAccess().Door || !object.isDoor()) {
            return false;
        }
        final DoorInstance door = (DoorInstance) object;
        String dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2DoorInstance.onActionShift.htm", player);
        dialog = dialog.replaceFirst("%CurrentHp%", String.valueOf((int) door.getCurrentHp()));
        dialog = dialog.replaceFirst("%MaxHp%", String.valueOf(door.getMaxHp()));
        dialog = dialog.replaceAll("%ObjectId%", String.valueOf(door.getObjectId()));
        dialog = dialog.replaceFirst("%doorId%", String.valueOf(door.getDoorId()));
        dialog = dialog.replaceFirst("%pdef%", String.valueOf(door.getPDef(null)));
        dialog = dialog.replaceFirst("%mdef%", String.valueOf(door.getMDef(null, null)));
        dialog = dialog.replaceFirst("%type%", door.getDoorType().name());
        dialog = dialog.replaceFirst("%upgradeHP%", String.valueOf(door.getUpgradeHp()));
        final StringBuilder b = new StringBuilder("");
        for (final GlobalEvent e : door.getEvents()) {
            b.append(e).append(";");
        }
        dialog = dialog.replaceFirst("%event%", b.toString());
        show(dialog, player);
        player.sendActionFailed();
        return true;
    }

    public boolean OnActionShift_Player(final Player player, final GameObject object) {
        if (player == null || object == null || !player.getPlayerAccess().CanViewChar) {
            return false;
        }
        if (object.isPlayer()) {
            AdminEditChar.showCharacterList(player, (Player) object);
        }
        return true;
    }

    public boolean OnActionShift_PetInstance(final Player player, final GameObject object) {
        if (player == null || object == null || !player.getPlayerAccess().CanViewChar) {
            return false;
        }
        if (object.isPet()) {
            final PetInstance pet = (PetInstance) object;
            String dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2PetInstance.onActionShift.htm", player);
            dialog = dialog.replaceFirst("%name%", HtmlUtils.htmlNpcName(pet.getNpcId()));
            dialog = dialog.replaceFirst("%title%", StringUtils.isEmpty(pet.getTitle()) ? "Empty" : pet.getTitle());
            dialog = dialog.replaceFirst("%level%", String.valueOf(pet.getLevel()));
            dialog = dialog.replaceFirst("%class%", String.valueOf(pet.getClass().getSimpleName().replaceFirst("L2", "").replaceFirst("Instance", "")));
            dialog = dialog.replaceFirst("%xyz%", pet.getLoc().x + " " + pet.getLoc().y + " " + pet.getLoc().z);
            dialog = dialog.replaceFirst("%heading%", String.valueOf(pet.getLoc().h));
            dialog = dialog.replaceFirst("%owner%", String.valueOf(pet.getPlayer().getName()));
            dialog = dialog.replaceFirst("%ownerId%", String.valueOf(pet.getPlayer().getObjectId()));
            dialog = dialog.replaceFirst("%npcId%", String.valueOf(pet.getNpcId()));
            dialog = dialog.replaceFirst("%controlItemId%", String.valueOf(pet.getControlItem().getItemId()));
            dialog = dialog.replaceFirst("%exp%", String.valueOf(pet.getExp()));
            dialog = dialog.replaceFirst("%sp%", String.valueOf(pet.getSp()));
            dialog = dialog.replaceFirst("%maxHp%", String.valueOf(pet.getMaxHp()));
            dialog = dialog.replaceFirst("%maxMp%", String.valueOf(pet.getMaxMp()));
            dialog = dialog.replaceFirst("%currHp%", String.valueOf((int) pet.getCurrentHp()));
            dialog = dialog.replaceFirst("%currMp%", String.valueOf((int) pet.getCurrentMp()));
            dialog = dialog.replaceFirst("%pDef%", String.valueOf(pet.getPDef(null)));
            dialog = dialog.replaceFirst("%mDef%", String.valueOf(pet.getMDef(null, null)));
            dialog = dialog.replaceFirst("%pAtk%", String.valueOf(pet.getPAtk(null)));
            dialog = dialog.replaceFirst("%mAtk%", String.valueOf(pet.getMAtk(null, null)));
            dialog = dialog.replaceFirst("%accuracy%", String.valueOf(pet.getAccuracy()));
            dialog = dialog.replaceFirst("%evasionRate%", String.valueOf(pet.getEvasionRate(null)));
            dialog = dialog.replaceFirst("%crt%", String.valueOf(pet.getCriticalHit(null, null)));
            dialog = dialog.replaceFirst("%runSpeed%", String.valueOf(pet.getRunSpeed()));
            dialog = dialog.replaceFirst("%walkSpeed%", String.valueOf(pet.getWalkSpeed()));
            dialog = dialog.replaceFirst("%pAtkSpd%", String.valueOf(pet.getPAtkSpd()));
            dialog = dialog.replaceFirst("%mAtkSpd%", String.valueOf(pet.getMAtkSpd()));
            dialog = dialog.replaceFirst("%dist%", String.valueOf((int) pet.getRealDistance(player)));
            dialog = dialog.replaceFirst("%STR%", String.valueOf(pet.getSTR()));
            dialog = dialog.replaceFirst("%DEX%", String.valueOf(pet.getDEX()));
            dialog = dialog.replaceFirst("%CON%", String.valueOf(pet.getCON()));
            dialog = dialog.replaceFirst("%INT%", String.valueOf(pet.getINT()));
            dialog = dialog.replaceFirst("%WIT%", String.valueOf(pet.getWIT()));
            dialog = dialog.replaceFirst("%MEN%", String.valueOf(pet.getMEN()));
            show(dialog, player);
        }
        return true;
    }

    public boolean OnActionShift_ItemInstance(final Player player, final GameObject object) {
        if (player == null || object == null || !player.getPlayerAccess().CanViewChar) {
            return false;
        }
        if (object.isItem()) {
            final ItemInstance item = (ItemInstance) object;
            String dialog = HtmCache.getInstance().getNotNull("scripts/actions/admin.L2ItemInstance.onActionShift.htm", player);
            dialog = dialog.replaceFirst("%name%", String.valueOf(item.getTemplate().getName()));
            dialog = dialog.replaceFirst("%objId%", String.valueOf(item.getObjectId()));
            dialog = dialog.replaceFirst("%itemId%", String.valueOf(item.getItemId()));
            dialog = dialog.replaceFirst("%grade%", String.valueOf(item.getCrystalType()));
            dialog = dialog.replaceFirst("%count%", String.valueOf(item.getCount()));
            final Player owner = GameObjectsStorage.getPlayer(item.getOwnerId());
            dialog = dialog.replaceFirst("%owner%", (owner == null) ? "none" : owner.getName());
            dialog = dialog.replaceFirst("%ownerId%", String.valueOf(item.getOwnerId()));
            for (final Element e : Element.VALUES) {
                dialog = dialog.replaceFirst("%" + e.name().toLowerCase() + "Val%", String.valueOf(item.getAttributeElementValue(e, true)));
            }
            dialog = dialog.replaceFirst("%attrElement%", String.valueOf(item.getAttributeElement()));
            dialog = dialog.replaceFirst("%attrValue%", String.valueOf(item.getAttributeElementValue()));
            dialog = dialog.replaceFirst("%enchLevel%", String.valueOf(item.getEnchantLevel()));
            dialog = dialog.replaceFirst("%type%", String.valueOf(item.getItemType()));
            dialog = dialog.replaceFirst("%dropTime%", String.valueOf(item.getDropTimeOwner()));
            show(dialog, player);
            player.sendActionFailed();
        }
        return true;
    }

    private String nameNpc(final NpcInstance npc) {
        return HtmlUtils.htmlNpcName(npc.getNpcId());
    }
}
