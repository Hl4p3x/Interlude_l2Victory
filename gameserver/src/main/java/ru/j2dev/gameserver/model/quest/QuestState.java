package ru.j2dev.gameserver.model.quest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.OnKillListener;
import ru.j2dev.gameserver.manager.SpawnManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.spawn.PeriodOfDay;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class QuestState {
    public static final int RESTART_HOUR = 6;
    public static final int RESTART_MINUTES = 30;
    public static final String VAR_COND = "cond";
    public static final QuestState[] EMPTY_ARRAY = new QuestState[0];
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestState.class);

    private final Player _player;
    private final Quest _quest;
    private final Map<String, String> _vars;
    private final Map<String, QuestTimer> _timers;
    private int _state;
    private Integer _cond;
    private OnKillListener _onKillListener;

    public QuestState(final Quest quest, final Player player, final int state) {
        _cond = null;
        _vars = new ConcurrentHashMap<>();
        _timers = new ConcurrentHashMap<>();
        _onKillListener = null;
        _quest = quest;
        (_player = player).setQuestState(this);
        _state = state;
        quest.notifyCreate(this);
    }

    public void addExpAndSp(long exp, long sp) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        exp *= (long) getRateQuestsRewardExp();
        sp *= (long) getRateQuestsRewardSp();
        if (exp > 0L && sp > 0L) {
            player.addExpAndSp(exp, sp);
        } else {
            if (exp > 0L) {
                player.addExpAndSp(exp, 0L);
            }
            if (sp > 0L) {
                player.addExpAndSp(0L, sp);
            }
        }
    }

    public void addNotifyOfDeath(final Player player, final boolean withPet) {
        final OnDeathListenerImpl listener = new OnDeathListenerImpl();
        player.addListener(listener);
        if (withPet) {
            final Summon summon = player.getPet();
            if (summon != null) {
                summon.addListener(listener);
            }
        }
    }

    public void addPlayerOnKillListener() {
        if (_onKillListener != null) {
            throw new IllegalArgumentException("Cant add twice kill listener to player");
        }
        _onKillListener = new PlayerOnKillListenerImpl();
        _player.addListener(_onKillListener);
    }

    public void removePlayerOnKillListener() {
        if (_onKillListener != null) {
            _player.removeListener(_onKillListener);
        }
    }

    public void addRadar(final int x, final int y, final int z) {
        final Player player = getPlayer();
        if (player != null) {
            player.addRadar(x, y, z);
        }
    }

    public void addRadarWithMap(final int x, final int y, final int z) {
        final Player player = getPlayer();
        if (player != null) {
            player.addRadarWithMap(x, y, z);
        }
    }

    public void exitCurrentQuest(final Quest quest) {
        final Player player = getPlayer();
        exitCurrentQuest(true);
        quest.newQuestState(player, 4);
        final QuestState qs = player.getQuestState(quest.getClass());
        qs.setRestartTime();
    }

    public QuestState exitCurrentQuest(final boolean repeatable) {
        final Player player = getPlayer();
        if (player == null) {
            return this;
        }
        removePlayerOnKillListener();
        Arrays.stream(_quest.getItems()).forEach(itemId -> {
            final ItemInstance item = player.getInventory().getItemByItemId(itemId);
            if (item != null) {
                if (itemId != 57) {
                    final long count = item.getCount();
                    player.getInventory().destroyItemByItemId(itemId, count);
                    player.getWarehouse().destroyItemByItemId(itemId, count);
                }
            }
        });
        if (repeatable) {
            player.removeQuestState(_quest.getName());
            Quest.deleteQuestInDb(this);
            _vars.clear();
        } else {
            _vars.keySet().stream().filter(Objects::nonNull).forEach(this::unset);
            setState(3);
            Quest.updateQuestInDb(this);
        }
        player.sendPacket(new QuestList(player));
        return this;
    }

    public void abortQuest() {
        _quest.onAbort(this);
        exitCurrentQuest(true);
    }

    public String get(final String var) {
        return _vars.get(var);
    }

    public Map<String, String> getVars() {
        return _vars;
    }

    public int getInt(final String var) {
        int varint = 0;
        try {
            final String val = get(var);
            if (val == null) {
                return 0;
            }
            varint = Integer.parseInt(val);
        } catch (Exception e) {
            LOGGER.error(getPlayer().getName() + ": variable " + var + " isn't an integer: " + varint, e);
        }
        return varint;
    }

    public int getItemEquipped(final int loc) {
        return getPlayer().getInventory().getPaperdollItemId(loc);
    }

    public Player getPlayer() {
        return _player;
    }

    public Quest getQuest() {
        return _quest;
    }

    public boolean checkQuestItemsCount(final int... itemIds) {
        final Player player = getPlayer();
        if (player == null) {
            return false;
        }
        return Arrays.stream(itemIds).noneMatch(itemId -> player.getInventory().getCountOf(itemId) <= 0L);
    }

    public long getSumQuestItemsCount(final int... itemIds) {
        final Player player = getPlayer();
        if (player == null) {
            return 0L;
        }
        return Arrays.stream(itemIds).mapToLong(itemId -> player.getInventory().getCountOf(itemId)).sum();
    }

    public long getQuestItemsCount(final int itemId) {
        final Player player = getPlayer();
        return (player == null) ? 0L : player.getInventory().getCountOf(itemId);
    }

    public long getQuestItemsCount(final int... itemsIds) {
        return Arrays.stream(itemsIds).mapToLong(this::getQuestItemsCount).sum();
    }

    public boolean haveQuestItem(final int itemId, final int count) {
        return getQuestItemsCount(itemId) >= count;
    }

    public boolean haveQuestItem(final int itemId) {
        return haveQuestItem(itemId, 1);
    }

    public int getState() {
        return (_state == 4) ? 1 : _state;
    }

    public String getStateName() {
        return Quest.getStateName(_state);
    }

    public void giveItems(final int itemId, final long count) {
        if (itemId == 57) {
            giveItems(itemId, count, true);
        } else {
            giveItems(itemId, count, false);
        }
    }

    public void giveItems(final int itemId, long count, final boolean rate) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        if (count <= 0L) {
            count = 1L;
        }
        if (rate) {
            count *= (long) getRateQuestsReward();
        }
        ItemFunctions.addItem(player, itemId, count, true);
        player.sendChanges();
    }

    public void giveItems(final int itemId, long count, final Element element, final int power) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        if (count <= 0L) {
            count = 1L;
        }
        final ItemTemplate template = ItemTemplateHolder.getInstance().getTemplate(itemId);
        if (template == null) {
            return;
        }
        for (int i = 0; i < count; ++i) {
            final ItemInstance item = ItemFunctions.createItem(itemId);
            if (element != Element.NONE) {
                item.setAttributeElement(element, power);
            }
            player.getInventory().addItem(item);
        }
        player.sendPacket(SystemMessage2.obtainItems(template.getItemId(), count, 0));
        player.sendChanges();
    }

    public void dropItem(final NpcInstance npc, final int itemId, final long count) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        final ItemInstance item = ItemFunctions.createItem(itemId);
        item.setCount(count);
        item.dropToTheGround(player, npc);
    }

    public int rollDrop(final int count, final double calcChance) {
        if (calcChance <= 0.0 || count <= 0) {
            return 0;
        }
        return rollDrop(count, count, calcChance);
    }

    public int rollDrop(final int min, final int max, double calcChance) {
        if (calcChance <= 0.0 || min <= 0 || max <= 0) {
            return 0;
        }
        int dropmult = 1;
        calcChance *= getRateQuestsDrop();
        if (getQuest().getParty() > 0) {
            final Player player = getPlayer();
            if (player.getParty() != null) {
                calcChance *= Config.ALT_PARTY_BONUS[player.getParty().getMemberCountInRange(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) - 1];
            }
        }
        if (calcChance > 100.0) {
            if ((int) Math.ceil(calcChance / 100.0) <= calcChance / 100.0) {
                calcChance = Math.nextUp(calcChance);
            }
            dropmult = (int) Math.ceil(calcChance / 100.0);
            calcChance /= dropmult;
        }
        return Rnd.chance(calcChance) ? Rnd.get(min * dropmult, max * dropmult) : 0;
    }

    public double getRateQuestsDrop() {
        final Player player = getPlayer();
        final double Bonus = (player == null) ? 1.0 : player.getBonus().getQuestDropRate();
        return Config.RATE_QUESTS_DROP * Bonus * getQuest().getRates().getDropRate();
    }

    public double getRateQuestsReward() {
        final Player player = getPlayer();
        final double Bonus = (player == null) ? 1.0 : player.getBonus().getQuestRewardRate();
        return Config.RATE_QUESTS_REWARD * Bonus * getQuest().getRates().getRewardRate();
    }

    public double getRateQuestsRewardExp() {
        final Player player = getPlayer();
        final double Bonus = (player == null) ? 1.0 : player.getBonus().getQuestRewardRate();
        return Config.RATE_QUESTS_REWARD_EXP_SP * Bonus * getQuest().getRates().getExpRate();
    }

    public double getRateQuestsRewardSp() {
        final Player player = getPlayer();
        final double Bonus = (player == null) ? 1.0 : player.getBonus().getQuestRewardRate();
        return Config.RATE_QUESTS_REWARD_EXP_SP * Bonus * getQuest().getRates().getSpRate();
    }

    public boolean rollAndGive(final int itemId, final int min, final int max, final int limit, final double calcChance) {
        if (calcChance <= 0.0 || min <= 0 || max <= 0 || limit <= 0 || itemId <= 0) {
            return false;
        }
        long count = rollDrop(min, max, calcChance);
        if (count > 0L) {
            final long alreadyCount = getQuestItemsCount(itemId);
            if (alreadyCount + count > limit) {
                count = limit - alreadyCount;
            }
            if (count > 0L) {
                giveItems(itemId, count, false);
                if (count + alreadyCount >= limit) {
                    playSound("ItemSound.quest_middle");
                    return true;
                }
                playSound("ItemSound.quest_itemget");
            }
        }
        return false;
    }

    public void rollAndGive(final int itemId, final int min, final int max, final double calcChance) {
        if (calcChance <= 0.0 || min <= 0 || max <= 0 || itemId <= 0) {
            return;
        }
        final int count = rollDrop(min, max, calcChance);
        if (count > 0) {
            giveItems(itemId, count, false);
            playSound("ItemSound.quest_itemget");
        }
    }

    public boolean rollAndGive(final int itemId, final int count, final double calcChance) {
        if (calcChance <= 0.0 || count <= 0 || itemId <= 0) {
            return false;
        }
        final int countToDrop = rollDrop(count, calcChance);
        if (countToDrop > 0) {
            giveItems(itemId, countToDrop, false);
            playSound("ItemSound.quest_itemget");
            return true;
        }
        return false;
    }

    public boolean isCompleted() {
        return getState() == 3;
    }

    public boolean isStarted() {
        return getState() == 2;
    }

    public boolean isCreated() {
        return getState() == 1;
    }

    public void killNpcByObjectId(final int _objId) {
        final NpcInstance npc = GameObjectsStorage.getNpc(_objId);
        if (npc != null) {
            npc.doDie(null);
        } else {
            LOGGER.warn("Attemp to kill object that is not npc in quest " + getQuest().getQuestIntId());
        }
    }

    public String set(final String var, final String val) {
        return set(var, val, true);
    }

    public String set(final String var, final int intval) {
        return set(var, String.valueOf(intval), true);
    }

    public String set(final String var, String val, final boolean store) {
        if (val == null) {
            val = "";
        }
        _vars.put(var, val);
        if (store) {
            Quest.updateQuestVarInDb(this, var, val);
        }
        return val;
    }

    public Object setState(final int state) {
        final Player player = getPlayer();
        if (player == null) {
            return null;
        }
        _state = state;
        if (getQuest().isVisible() && isStarted()) {
            player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
        }
        Quest.updateQuestInDb(this);
        player.sendPacket(new QuestList(player));
        player.getListeners().onQuestStateChange(this);
        return state;
    }

    public Object setStateAndNotSave(final int state) {
        final Player player = getPlayer();
        if (player == null) {
            return null;
        }
        _state = state;
        if (getQuest().isVisible() && isStarted()) {
            player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
        }
        player.sendPacket(new QuestList(player));
        return state;
    }

    public void playSound(final String sound) {
        final Player player = getPlayer();
        if (player != null) {
            player.sendPacket(new PlaySound(sound));
        }
    }

    public void playTutorialVoice(final String voice) {
        final Player player = getPlayer();
        if (player != null) {
            player.sendPacket(new PlaySound(Type.VOICE, voice, 0, 0, player.getLoc()));
        }
    }

    public void onTutorialClientEvent(final int number) {
        final Player player = getPlayer();
        if (player != null) {
            player.sendPacket(new TutorialEnableClientEvent(number));
        }
    }

    public void showQuestionMark(final int number) {
        final Player player = getPlayer();
        if (player != null) {
            player.sendPacket(new TutorialShowQuestionMark(number));
        }
    }

    public void showTutorialHTML(final String html) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        final String text = HtmCache.getInstance().getNotNull("quests/_255_Tutorial/" + html, player);
        player.sendPacket(new TutorialShowHtml(text));
    }

    public void showAltStartPlayer(final String html) {
        final Player player = getPlayer();
        if (player == null) {
            return;
        }
        final String text = HtmCache.getInstance().getNotNull("quests/_777_AltStartPlayer/" + html, player);
        player.sendPacket(new TutorialShowHtml(text));
    }

    public void startQuestTimer(final String name, final long time) {
        startQuestTimer(name, time, null);
    }

    public void startQuestTimer(final String name, final long time, final NpcInstance npc) {
        final QuestTimer timer = new QuestTimer(name, time, npc);
        timer.setQuestState(this);
        final QuestTimer oldTimer = getTimers().put(name, timer);
        if (oldTimer != null) {
            oldTimer.stop();
        }
        timer.start();
    }

    public boolean isRunningQuestTimer(final String name) {
        return getTimers().get(name) != null;
    }

    public boolean cancelQuestTimer(final String name) {
        final QuestTimer timer = removeQuestTimer(name);
        if (timer != null) {
            timer.stop();
        }
        return timer != null;
    }

    QuestTimer removeQuestTimer(final String name) {
        final QuestTimer timer = getTimers().remove(name);
        if (timer != null) {
            timer.setQuestState(null);
        }
        return timer;
    }

    public void pauseQuestTimers() {
        getQuest().pauseQuestTimers(this);
    }

    public void stopQuestTimers() {
        getTimers().values().forEach(timer -> {
            timer.setQuestState(null);
            timer.stop();
        });
        _timers.clear();
    }

    public void resumeQuestTimers() {
        getQuest().resumeQuestTimers(this);
    }

    Map<String, QuestTimer> getTimers() {
        return _timers;
    }

    public long takeItems(final int itemId, long count) {
        final Player player = getPlayer();
        if (player == null) {
            return 0L;
        }
        final ItemInstance item = player.getInventory().getItemByItemId(itemId);
        if (item == null) {
            return 0L;
        }
        if (count < 0L || count > item.getCount()) {
            count = item.getCount();
        }
        player.getInventory().destroyItemByItemId(itemId, count);
        player.sendPacket(SystemMessage2.removeItems(itemId, count));
        return count;
    }

    public long takeAllItems(final int itemId) {
        return takeItems(itemId, -1L);
    }

    public long takeAllItems(final int... itemsIds) {
        return Arrays.stream(itemsIds).mapToLong(this::takeAllItems).sum();
    }

    public long takeAllItems(final Collection<Integer> itemsIds) {
        return itemsIds.stream().mapToInt(id -> id).mapToLong(this::takeAllItems).sum();
    }

    public String unset(final String var) {
        if (var == null) {
            return null;
        }
        final String old = _vars.remove(var);
        if (old != null) {
            Quest.deleteQuestVarInDb(this, var);
        }
        return old;
    }

    private boolean checkPartyMember(final Player member, final int state, final int maxrange, final GameObject rangefrom) {
        if (member == null) {
            return false;
        }
        if (rangefrom != null && maxrange > 0 && !member.isInRange(rangefrom, maxrange)) {
            return false;
        }
        final QuestState qs = member.getQuestState(getQuest().getName());
        return qs != null && qs.getState() == state;
    }

    public List<Player> getPartyMembers(final int state, final int maxrange, final GameObject rangefrom) {
        final List<Player> result = new ArrayList<>();
        final Party party = getPlayer().getParty();
        if (party == null) {
            if (checkPartyMember(getPlayer(), state, maxrange, rangefrom)) {
                result.add(getPlayer());
            }
            return result;
        }
        party.getPartyMembers().stream().filter(member -> checkPartyMember(member, state, maxrange, rangefrom)).forEach(result::add);
        return result;
    }

    public Player getRandomPartyMember(final int state, final int maxrangefromplayer) {
        return getRandomPartyMember(state, maxrangefromplayer, getPlayer());
    }

    public Player getRandomPartyMember(final int state, final int maxrange, final GameObject rangefrom) {
        final List<Player> list = getPartyMembers(state, maxrange, rangefrom);
        if (list.size() == 0) {
            return null;
        }
        return list.get(Rnd.get(list.size()));
    }

    public NpcInstance addSpawn(final int npcId) {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, 0);
    }

    public NpcInstance addSpawn(final int npcId, final int despawnDelay) {
        return addSpawn(npcId, getPlayer().getX(), getPlayer().getY(), getPlayer().getZ(), 0, 0, despawnDelay);
    }

    public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z) {
        return addSpawn(npcId, x, y, z, 0, 0, 0);
    }

    public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int despawnDelay) {
        return addSpawn(npcId, x, y, z, 0, 0, despawnDelay);
    }

    public NpcInstance addSpawn(final int npcId, final int x, final int y, final int z, final int heading, final int randomOffset, final int despawnDelay) {
        return getQuest().addSpawn(npcId, x, y, z, heading, randomOffset, despawnDelay);
    }

    public NpcInstance findTemplate(final int npcId) {
        return SpawnManager.getInstance().getSpawners(PeriodOfDay.ALL.name()).stream().filter(spawn -> spawn != null && spawn.getCurrentNpcId() == npcId).findFirst().map(Spawner::getLastSpawn).orElse(null);
    }

    public int calculateLevelDiffForDrop(final int mobLevel, final int player) {
        if (!Config.DEEPBLUE_DROP_RULES) {
            return 0;
        }
        return Math.max(player - mobLevel - Config.DEEPBLUE_DROP_MAXDIFF, 0);
    }

    public int getCond() {
        if (_cond == null) {
            int val = getInt("cond");
            if ((val & Integer.MIN_VALUE) != 0x0) {
                val &= Integer.MAX_VALUE;
                for (int i = 1; i < 32; ++i) {
                    val >>= 1;
                    if (val == 0) {
                        val = i;
                        break;
                    }
                }
            }
            _cond = val;
        }
        return _cond;
    }

    public String setCond(final int newCond) {
        return setCond(newCond, true);
    }

    public String setCond(int newCond, final boolean store) {
        if (newCond == getCond()) {
            return String.valueOf(newCond);
        }
        int oldCond = getInt("cond");
        _cond = newCond;
        if ((oldCond & Integer.MIN_VALUE) != 0x0) {
            if (newCond > 2) {
                oldCond &= (0x80000001 | (1 << newCond) - 1);
                newCond = (oldCond | 1 << newCond - 1);
            }
        } else if (newCond > 2) {
            newCond = (0x80000001 | 1 << newCond - 1 | (1 << oldCond) - 1);
        }
        final String sVal = String.valueOf(newCond);
        final String result = set("cond", sVal, false);
        if (store) {
            Quest.updateQuestVarInDb(this, "cond", sVal);
        }
        final Player player = getPlayer();
        if (player != null) {
            player.sendPacket(new QuestList(player));
            if (newCond != 0 && getQuest().isVisible() && isStarted()) {
                player.sendPacket(new ExShowQuestMark(getQuest().getQuestIntId()));
            }
        }
        return result;
    }

    public void setRestartTime() {
        final Calendar reDo = Calendar.getInstance();
        if (reDo.get(Calendar.HOUR_OF_DAY) >= 6) {
            reDo.add(Calendar.DATE, 1);
        }
        reDo.set(Calendar.HOUR_OF_DAY, 6);
        reDo.set(Calendar.MINUTE, 30);
        set("restartTime", String.valueOf(reDo.getTimeInMillis()));
    }

    public boolean isNowAvailable() {
        final String val = get("restartTime");
        if (val == null) {
            return true;
        }
        final long restartTime = Long.parseLong(val);
        return restartTime <= System.currentTimeMillis();
    }

    public void addBuff(final Creature creature, final int skillId, final int skillLvl) {
        //SkillUtils.applySkillEffect(skillId, skillLvl, creature);
    }

    public class OnDeathListenerImpl implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            final Player player = actor.getPlayer();
            if (player == null) {
                return;
            }
            player.removeListener(this);
            _quest.notifyDeath(killer, actor, QuestState.this);
        }
    }

    public class PlayerOnKillListenerImpl implements OnKillListener {
        @Override
        public void onKill(final Creature actor, final Creature victim) {
            if (!victim.isPlayer()) {
                return;
            }
            final Player actorPlayer = (Player) actor;
            List<Player> players;
            switch (_quest.getParty()) {
                case 0: {
                    players = Collections.singletonList(actorPlayer);
                    break;
                }
                case 2: {
                    if (actorPlayer.getParty() == null) {
                        players = Collections.singletonList(actorPlayer);
                        break;
                    }
                    players = new ArrayList<>(actorPlayer.getParty().getMemberCount());
                    actorPlayer.getParty().getPartyMembers().stream().filter($member -> $member.isInActingRange(actorPlayer)).forEach(players::add);
                    break;
                }
                default: {
                    players = Collections.emptyList();
                    break;
                }
            }
            players.stream().map(player -> player.getQuestState(_quest.getClass())).filter(questState -> questState != null && !questState.isCompleted()).forEach(questState -> _quest.notifyKill((Player) victim, questState));
        }

        @Override
        public boolean ignorePetOrSummon() {
            return true;
        }
    }
}
