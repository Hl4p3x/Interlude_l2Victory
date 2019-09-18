package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import gnu.trove.list.array.TIntArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.EventEquipHolder;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.listener.actor.OnAttackListener;
import ru.j2dev.gameserver.listener.actor.OnMagicUseListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.InvisibleType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.LockType;
import ru.j2dev.gameserver.model.items.PcInventory;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExEventMatchMessage.MessageType;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

@HideAccess
@StringEncryption
public abstract class PvpEvent implements OnInitScriptListener {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
    protected final TeamType BLUE_TEAM = TeamType.BLUE;
    protected final TeamType RED_TEAM = TeamType.RED;
    protected ScheduledFuture<?> _startTask;
    protected ScheduledFuture<?> _endTask;
    protected boolean _isRegistrationActive;
    protected int _status;
    protected int _time_to_start;
    protected int _minLevel;
    protected int _maxLevel;
    protected int _autoContinue;
    protected Reflection _reflection;
    protected Player player;
    protected NpcInstance lastNpc;
    protected String[] args;
    private final PvpEventListenerList listeners = new PvpEventListenerList();
    private final Map<Integer, ArrayList<Integer>> _storedItems = new ConcurrentHashMap<>();
    private final Map<Integer, int[]> _lockedItems = new ConcurrentHashMap<>();

    protected int getMinLevelForCategory(final int category) {
        switch (category) {
            case -1: {
                return 1;
            }
            case 1: {
                return 20;
            }
            case 2: {
                return 30;
            }
            case 3: {
                return 40;
            }
            case 4: {
                return 52;
            }
            case 5: {
                return 62;
            }
            case 6: {
                return 76;
            }
        }
        return 0;
    }

    protected int getMaxLevelForCategory(final int category) {
        switch (category) {
            case -1: {
                return 80;
            }
            case 1: {
                return 29;
            }
            case 2: {
                return 39;
            }
            case 3: {
                return 51;
            }
            case 4: {
                return 61;
            }
            case 5: {
                return 75;
            }
            case 6: {
                return 80;
            }
        }
        return 0;
    }

    protected int getCategory(final int level) {
        if (level >= 20 && level <= 29) {
            return 1;
        }
        if (level >= 30 && level <= 39) {
            return 2;
        }
        if (level >= 40 && level <= 51) {
            return 3;
        }
        if (level >= 52 && level <= 61) {
            return 4;
        }
        if (level >= 62 && level <= 75) {
            return 5;
        }
        if (level >= 76) {
            return 6;
        }
        return 0;
    }

    protected IStaticPacket getFinalStatistics(final PvpEventTeam team, final String event) {
        final NpcHtmlMessage msg = new NpcHtmlMessage(1);
        try {
            final StringBuilder builder = new StringBuilder();
            final PvpEventPlayerInfo[] inf = PvpEventUtils.sortAndTrimPlayerInfos(team.getPlayerInfos().toArray(new PvpEventPlayerInfo[0]), 25);
            builder.append("<html><title>%replace%</title><body><center><br><table width=265 border=0 bgcolor=\"000000\"><tr><td width=40><font color=\"LEVEL\">Rank</font></td><td><font color=\"LEVEL\">Name</font></td><td width=40><font color=\"LEVEL\">Kills</font></td></tr>");
            int poz = 1;
            for (final PvpEventPlayerInfo anInf : inf) {
                if (anInf == null) {
                    continue;
                }
                builder.append("<tr><td>").append(poz++).append(".</td><td>").append(anInf.getPlayer() == null ? "D/C" : anInf.getPlayer().getName()).append("</td><td>").append(anInf.getKillsCount()).append("</td></tr>");
            }
            builder.append("</table></center></body></head>");
            msg.setHtml(builder.toString().replace("%replace%", event));
        } catch (final Exception e) {
            LOGGER.error("", e);
        }
        return msg;
    }

    protected boolean isInTeam(final Creature cha, final PvpEventTeam... eventTeams) {
        for (PvpEventTeam team : eventTeams) {
            if (team.isInTeam(cha.getPlayer())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isInLiveTeam(final Creature cha, final PvpEventTeam... eventTeams) {
        for (PvpEventTeam team : eventTeams) {
            if (team.isLive(cha.getPlayer())) {
                return true;
            }
        }
        return false;
    }

    public void checkKillsAndAnnounce(PvpEventPlayerInfo info, PvpEventTeam team, PvpEventTeam team1) {
        String text;
        switch (info.getKillsCount()) {
            case 0:
            case 1:
            case 2:
                return;
            case 3:
                text = info.getPlayer().getName() + ": Killing Spree!";
                break;
            case 4:
                text = info.getPlayer().getName() + ": Rampage!";
                break;
            case 5:
                text = info.getPlayer().getName() + ": Unstoppable!";
                break;
            case 6:
                text = info.getPlayer().getName() + ": DOMINATING!";
                break;
            case 7:
                text = info.getPlayer().getName() + ": GODLIKE!";
                break;
            case 8:
                text = info.getPlayer().getName() + ": HOLY SHIT!";
                break;
            case 9:
                text = info.getPlayer().getName() + ": ARENA MASTER!";
                break;
            case 15:
                text = info.getPlayer().getName() + ": BEST PLAYER!";
                break;
            default:
                return;
        }

        final ExShowScreenMessage screenMessage = new ExShowScreenMessage(text, 6000, ExShowScreenMessage.ScreenMessageAlign.TOP_CENTER, false);

        team.getAllPlayers().forEach(team1Player -> team1Player.sendPacket(screenMessage));

        team1.getAllPlayers().forEach(team2Player -> team2Player.sendPacket(screenMessage));
    }

    private void buffPlayable(Player player, List<Integer> list, int time, Summon pet) {
        list.forEach(skillId -> {
            final int lvl = SkillTable.getInstance().getBaseLevel(skillId);
            final Skill skill = SkillTable.getInstance().getInfo(skillId, lvl);
            applySkillEffect(skill, time * 60000, player, false);
            if (pet != null) {
                applySkillEffect(skill, time * 60000, pet, false);
            }
        });
    }

    private void applySkillEffect(final Skill skill, final long time, final Playable actor, final boolean animation) {
        if (actor == null) {
            return;
        }
        if (animation) {
            actor.broadcastPacket(new MagicSkillUse(actor, actor, skill.getId(), skill.getLevel(), skill.getHitTime(), skill.getReuseDelay()));
        }
        if (skill != null) {
            skill.getEffects(actor, actor, false, false, time, 1, false);
        }
    }

    protected void lockItems(Player player, int[] itemsToLock) {
        player.getInventory().lockItems(LockType.INCLUDE, itemsToLock);
    }

    protected void unlockItems(Player player, int[] itemsToLock) {
        player.getInventory().unlockItems(itemsToLock);
    }

    protected boolean setStatus(final boolean status) {
        if (status == isActive()) {
            return false;
        }
        if (status) {
            ServerVariables.set(getEventType().toString(), true);
        } else {
            ServerVariables.set(getEventType().toString(), false);
        }
        return true;
    }

    public boolean isActive() {
        return ServerVariables.getBool(getEventType().toString(), false);
    }

    protected void onEscape(final Player player) {
    }

    protected void start(Player player, NpcInstance lastNpc, String[] args) {}

    protected void onStart(PvpEvent pvpEvent) {
        listeners.onStart(pvpEvent);
    }

    protected void onStop(PvpEvent pvpEvent) {
        listeners.onStop(pvpEvent);
    }

    @HideAccess
    private void equipPlayer(Player player, ArrayList<Integer> items) {
        if (player == null || items == null || items.isEmpty()) {
            return;
        }
        final PcInventory inventory = player.getInventory();
        int[] itemsToLock = inventory.getItems().stream().filter(ItemInstance::isEquipable).mapToInt(ItemInstance::getItemId).toArray();
        storeAndLockItems(player, itemsToLock);
        inventory.getItems().stream().filter(ItemInstance::isEquipped).forEach(item -> storeAndUnEquipItem(player.getObjectId(), item.getObjectId()));

        ItemInstance item;
        for (int itemId : items) {
            item = inventory.addItem(itemId, 1);
            if (item == null) {
                continue;
            }
            item.setEnchantLevel(6); //todo параметр достойный конфига
            item.setCustomFlags(getEventType().hashCode());
            inventory.equipItem(item);
        }
        player.sendItemList(false);
        player.broadcastUserInfo(false);
    }

    @HideAccess
    private void storeAndLockItems(final Player player, int[] itemsToLock) {
        _lockedItems.put(player.getObjectId(), itemsToLock);
        lockItems(player, itemsToLock);
    }

    @HideAccess
    private void storeAndUnEquipItem(int charId, int itemObjId) {
        putStoredItem(charId, itemObjId, _storedItems.get(charId));
    }

    @HideAccess
    private void putStoredItem(int charId, int itemObjId, ArrayList<Integer> items) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(itemObjId);
        _storedItems.put(charId, items);
    }

    @HideAccess
    private void restoreItems(Player player) {
        equipStoredItems(player, _storedItems.get(player.getObjectId()));
    }

    @HideAccess
    private void equipStoredItems(final Player player, ArrayList<Integer> items) {
        if(player == null || items == null || items.isEmpty()) {
            return;
        }
        final PcInventory inventory = player.getInventory();
        inventory.getItems().forEach(item -> {
            if (item.isEquipped()) {
                inventory.unEquipItem(item);
            }
            if (item.getCustomFlags() == getEventType().hashCode()) {
                inventory.destroyItem(item);
            }
        });

        if (items.isEmpty()) {
            return;
        }

        ItemInstance item;
        for (int itemId : items) {
            item = inventory.getItemByObjectId(itemId);
            if (item == null) {
                continue;
            }

            if (item.getOwnerId() != player.getObjectId()) {
                continue;
            }

            if (item.getLocation() != ItemInstance.ItemLocation.INVENTORY) {
                continue;
            }

            if (ItemFunctions.checkIfCanEquip(player, item) != null) {
                continue;
            }

            inventory.equipItem(item);
            inventory.unlock();
        }
        player.sendItemList(false);
        player.broadcastUserInfo(false);
    }

    public abstract PvpEventType getEventType();

    protected final void preparePlayer(final Player player) {
        final Summon pet = player.getPet();
        if (!isAllowSummons() && pet != null) {
            pet.unSummon();
        }
        if (!isAllowBuffs()) {
            removeBuff(player);
        }
        if (!isAllowClanSkill() && player.getClan() != null) {
            player.getClan().getSkills().forEach(skill -> player.removeSkill(skill, false));
        }
        if (!isAllowHeroSkill() && player.isHero()) {
            HeroManager.removeSkills(player);
        }
        if (player.isMounted()) {
            player.setMount(0, 0, 0);
        }
        if (player.isInvul()) {
            player.setIsInvul(false);
        }
        if (player.isInvisible()) {
            player.setInvisibleType(InvisibleType.NONE);
        }
        if (isDispelTransformation() && player.getTransformation() > 0) {
            player.setTransformation(0);
        }
        checkParty(player);
        if(isEquipedByCustomArmorWeapon()) {
            equipPlayer(player, EventEquipHolder.getInstance().getEquipForEventTypeAndClassId(getEventType(), player.getActiveClassId()));
        }
    }

    private void checkParty(final Player player) {
        final Party party = player.getParty();
        if (isPartyDisable() && party != null) {
            player.leaveParty();
        }
        if (isPartyDisableMassEvent() && party != null) {
            party.getPartyMembers().stream().filter(partyMember -> partyMember.getTeam() != player.getTeam()).forEach(Player::leaveParty);
        }
    }

    public void sayToAll(final String address, final String... arg) {
        Announcements.getInstance().announceByCustomMessage(address, arg, ChatType.CRITICAL_ANNOUNCE);
    }

    public void sendEventMessage(final PvpEventTeam team, final MessageType type, final String message) {
        team.getAllPlayers().forEach(player -> player.sendPacket(new ExEventMatchMessage(type, message)));
    }

    public void revivePlayer(final Player player) {
        if (player.isDead()) {
            healPlayer(player);
            player.broadcastPacket(new Revive(player));
        }
    }

    protected void healPlayer(final Player player) {
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), true);
}

    protected void playerBuff(final Player player, final List<Integer> list) {
        buffPlayable(player, list, getBuffTime(), player.getPet());
    }

    protected void savePlayerBackCoord(final Player player, final String backVar) {
        player.setVar(backVar, player.getX() + " " + player.getY() + ' ' + player.getZ() + ' ' + player.getReflectionId(), -1);
    }

    protected void teleportPlayerToBack(final Player player, final boolean checkSkills, final boolean buffs, final String backVar) {
        try {
            if (checkSkills) {
                if (player.getClan() != null) {
                    player.getClan().getSkills().
                            stream().
                            filter(skill -> skill.getMinPledgeClass() <= player.getPledgeClass() || Config.CLAN_SKILLS_FOR_ALL).
                            forEach(skill -> player.addSkill(skill, true));
                }
                if (player.isHero()) {
                    HeroManager.addSkills(player);
                }
                player.sendSkillList();
            }
            String var = player.getVar(backVar);
            if (var == null || var.isEmpty()) {
                return;
            }
            String[] coords = var.split(" ");
            if (coords.length != 4) {
                return;
            }
            if (buffs) {
                removeBuff(player);
            }
            if(isEquipedByCustomArmorWeapon()) {
                if(_lockedItems.get(player.getObjectId()) != null) {
                    unlockItems(player, _lockedItems.get(player.getObjectId()));
                }
                restoreItems(player);
            }
            player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
            player.getVars().remove(backVar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeBuff(final Player player) {
        if (player != null) {

            if (player.isCastingNow()) {
                player.abortCast(true, true);
            }
            if (player.getPet() != null) {
                final Summon servitor = player.getPet();
                servitor.getEffectList().stopAllEffects();
            }
            player.getEffectList().stopAllEffects();
        }
    }

    protected boolean checkRegisterCondition(Player player, PvpEventTeam... teams) {
        if (!_isRegistrationActive) {
            sendNoActiveRegister(player);
            return true;
        }
        if(!isInTeam(player, teams)) {
            sendNotRegistred(player);
            return true;
        }
        return false;
    }

    protected void sendNoActiveRegister(Player player) {
        player.sendMessage(player.isLangRus() ? "Регистрация не активна!" : "Registration is not active!");
    }

    protected void sendAlreadyRegistred(Player player) {
        player.sendMessage(player.isLangRus() ? "Вы не зарегистрированы в эвенте!" : "You are not registred on event!");
    }

    private void sendNotRegistred(Player player) {
        player.sendMessage(player.isLangRus() ? "Вы не зарегистрированы в эвенте!" : "You are not registred on event!");
    }

    protected void sendCancelRegister(Player player) {
        player.sendMessage(player.isLangRus() ? "Вы отменили регистрацию в эвенте!" : "You cancelled register to event!");
    }

    public boolean isAllowClanSkill() {
        return false;
    }

    public boolean isAllowHeroSkill() {
        return false;
    }

    public boolean isAllowBuffs() {
        return false;
    }

    public boolean isDispelTransformation() {
        return false;
    }

    public boolean isEquipedByCustomArmorWeapon() {
        return false;
    }

    public boolean isAllowSummons() {
        return false;
    }

    public boolean isPartyDisable() {
        return false;
    }

    public boolean isPartyDisableMassEvent() {
        return false;
    }

    public boolean isQuestionAllowed() {
        return false;
    }

    public int getBuffTime() {
        return 300000;
    }

    protected ScheduledFuture<?> scheduleRunnable(final RunnableImpl runnable, final long delay) {
        return ThreadPoolManager.getInstance().schedule(runnable, delay);
    }

    protected void logEventStart(final int category, final int autoContinue) {
        LOGGER.info(getEventType()+": start event for levels [{}" + '-' + "{}" + ']', getMinLevelForCategory(category), getMaxLevelForCategory(category));
    }

    public class AttackMagicListenerImpl implements OnAttackListener, OnMagicUseListener {
        TIntArrayList restrictSkills = new TIntArrayList();
        boolean _checkTeam;

        public AttackMagicListenerImpl(int... skills) {
            for(int skill : skills) {
                if(skill != -1) {
                    restrictSkills.add(skill);
                }
            }
        }

        public AttackMagicListenerImpl(int[] skills, boolean checkTeam) {
            for(int skill : skills) {
                if(skill != -1) {
                    restrictSkills.add(skill);
                }
            }
            _checkTeam = checkTeam;
        }

        @Override
        public void onMagicUse(Creature actor, Skill skill, Creature target, boolean alt) {
            if (restrictSkills.contains(skill.getId()) || skill.getSkillType() == Skill.SkillType.RESURRECT) {
                actor.abortCast(true, false);
            }
            if (_checkTeam && actor.getTeam() == target.getTeam() && !skill.isAllowedForTeamate()) {
                actor.abortCast(true, false);
            }
        }

        @Override
        public void onAttack(Creature attacker, Creature target) {
            if (_checkTeam && attacker.getTeam() == target.getTeam()) {
                attacker.abortAttack(true, false);
            }
        }
    }

    public class StartTask extends RunnableImpl {
        final String[] args;

        public StartTask(final String... params) {
            args = params;
        }

        @Override
        public void runImpl() {
            if (!isActive()) {
                LOGGER.info(getEventType()+" not started: event inactive.");
                return;
            }

            for (Residence c : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
                if (c.getSiegeEvent() != null && c.getSiegeEvent().isInProgress()) {
                    LOGGER.info(getEventType()+" not started: CastleSiege in progress");
                    return;
                }
            }
            start(player, lastNpc, args);
        }
    }

    protected class ZoneListener implements OnZoneEnterLeaveListener {

        PvpEventTeam[] teams;

        public ZoneListener(PvpEventTeam... teams) {
            this.teams = teams;
        }

        @Override
        public void onZoneEnter(Zone zone, Creature cha) {
            if (cha == null) {
                return;
            }
            Player player = cha.getPlayer();
            if (_status > 0 && player != null && !isInTeam(player, teams)) {
                player.teleToClosestTown();
            }
        }

        @Override
        public void onZoneLeave(Zone zone, Creature cha) {
            if (cha == null) {
                return;
            }
            Player player = cha.getPlayer();
            if (_status > 1 && player != null && player.getTeam() != TeamType.NONE && isInTeam(player, teams)) {
                double angle = PositionUtils.convertHeadingToDegree(cha.getHeading()); // угол в градусах
                double radian = Math.toRadians(angle - 90); // угол в радианах
                int x = (int) (cha.getX() + 50 * Math.sin(radian));
                int y = (int) (cha.getY() - 50 * Math.cos(radian));
                int z = cha.getZ();
                player.teleToLocation(new Location(x, y, z));
            }
        }
    }

}

