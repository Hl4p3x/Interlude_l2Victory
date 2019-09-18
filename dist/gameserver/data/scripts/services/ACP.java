package services;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.listener.Listener;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.listener.actor.OnCurrentHpDamageListener;
import ru.j2dev.gameserver.listener.actor.OnCurrentMpReduceListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerEnterListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerExitListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.actor.listener.PlayerListenerList;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExUseSharedGroupItem;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.skills.TimeStamp;
import ru.j2dev.gameserver.stats.Stats;

import java.util.concurrent.atomic.AtomicReference;

public class ACP implements OnInitScriptListener, IVoicedCommandHandler {
    private static final Logger LOG = LoggerFactory.getLogger(ACP.class);
    private static final long ACP_ACT_DELAY = 100L;

    private final String[] _commandList;

    public ACP() {
        _commandList = new String[]{"acp"};
    }

    private static void showACPInteface(final Player player) {
        final NpcHtmlMessage dialog = new NpcHtmlMessage(5);
        dialog.setFile("command/acp.htm");
        if (ACPType.HPACP.isEnabled(player)) {
            dialog.replace("%hpap%", String.valueOf(ACPType.HPACP.getActPercent(player)) + "%");
        } else {
            dialog.replace("%hpap%", "Off");
        }
        if (ACPType.CPACP.isEnabled(player)) {
            dialog.replace("%cpap%", String.valueOf(ACPType.CPACP.getActPercent(player)) + "%");
        } else {
            dialog.replace("%cpap%", "Off");
        }
        if (ACPType.MPACP.isEnabled(player)) {
            dialog.replace("%mpap%", String.valueOf(ACPType.MPACP.getActPercent(player)) + "%");
        } else {
            dialog.replace("%mpap%", "Off");
        }
        player.sendPacket(dialog);
    }

    private static boolean isACPEnabled() {
        return Config.SERVICES_HPACP_ENABLE || Config.SERVICES_CPACP_ENABLE || Config.SERVICES_MPACP_ENABLE;
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player player, final String args) {
        if (isACPEnabled() && "acp".equalsIgnoreCase(command)) {
            final String[] param = args.split("\\s+");
            if (param.length > 1) {
                final String type = param[0];
                final String val = param[1];
                for (final ACPType acpType : ACPType.VALUES) {
                    if (!acpType.isEnabled()) {
                        acpType.remove(player);
                    } else if (acpType.getCfgName().equalsIgnoreCase(type)) {
                        if ("true".equalsIgnoreCase(val) || "on".equalsIgnoreCase(val) || "enable".equalsIgnoreCase(val) || "1".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val)) {
                            acpType.apply(player);
                            acpType.setEnabled(player, true);
                            player.sendMessage(new CustomMessage("services.ACP.Enabled", player, acpType.getCfgName()));
                        } else if ("false".equalsIgnoreCase(val) || "of".equalsIgnoreCase(val) || "off".equalsIgnoreCase(val) || "disable".equalsIgnoreCase(val) || "0".equalsIgnoreCase(val) || "no".equalsIgnoreCase(val)) {
                            acpType.remove(player);
                            acpType.setEnabled(player, false);
                            player.sendMessage(new CustomMessage("services.ACP.Disabled", player, acpType.getCfgName()));
                        } else if (acpType.isEnabled(player)) {
                            final int actPercent = acpType.setActPercent(player, NumberUtils.toInt(val, acpType.getActDefPercent()));
                            acpType.apply(player);
                            player.sendMessage(new CustomMessage("services.ACP.ActPercentSet", player, acpType.getCfgName(), actPercent));
                        }
                    }
                }
            }
            showACPInteface(player);
            return true;
        }
        return false;
    }

    private class OnPlayerEnterExitListenerImpl implements OnPlayerEnterListener, OnPlayerExitListener{

        @Override
        public void onPlayerEnter(final Player player) {
            if (isACPEnabled()) {
                for (final ACPType acpType : ACPType.VALUES) {
                    if (acpType.isEnabled() && acpType.isEnabled(player)) {
                        acpType.apply(player);
                    }
                }
            }
        }

        @Override
        public void onPlayerExit(final Player player) {
            if (isACPEnabled()) {
                for (final ACPType acpType : ACPType.VALUES) {
                    acpType.remove(player);
                }
            }
        }
    }

    @Override
    public void onInit() {
        if (isACPEnabled()) {
            PlayerListenerList.addGlobal(new OnPlayerEnterExitListenerImpl());
            VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
            ACP.LOG.info("ACPService: Loaded");
        }
    }

    enum ACPType {
        HPACP("HP") {
            @Override
            public boolean isEnabled() {
                return Config.SERVICES_HPACP_ENABLE;
            }

            @Override
            public void apply(final Player player) {
                for (final Listener<Creature> listener : player.getListeners().getListeners()) {
                    if (listener instanceof HPACPHelper) {
                        return;
                    }
                }
                player.addListener(new HPACPHelper(player));
            }

            @Override
            public void remove(final Player player) {
                for (final Listener<Creature> listener : player.getListeners().getListeners()) {
                    if (listener instanceof HPACPHelper) {
                        player.getListeners().remove(listener);
                    }
                }
            }

            @Override
            public int[] getPotionsItemIds() {
                return Config.SERVICES_HPACP_POTIONS_ITEM_IDS;
            }

            @Override
            protected int getActMinPercent() {
                return Config.SERVICES_HPACP_MIN_PERCENT;
            }

            @Override
            protected int getActMaxPercent() {
                return Config.SERVICES_HPACP_MAX_PERCENT;
            }

            @Override
            protected int getActDefPercent() {
                return Config.SERVICES_HPACP_DEF_PERCENT;
            }
        },
        CPACP("CP") {
            @Override
            public boolean isEnabled() {
                return Config.SERVICES_CPACP_ENABLE;
            }

            @Override
            public void apply(final Player player) {
                for (final Listener<Creature> listener : player.getListeners().getListeners()) {
                    if (listener instanceof CPACPHelper) {
                        return;
                    }
                }
                player.addListener(new CPACPHelper(player));
            }

            @Override
            public void remove(final Player player) {
                for (final Listener<Creature> listener : player.getListeners().getListeners()) {
                    if (listener instanceof CPACPHelper) {
                        player.getListeners().remove(listener);
                    }
                }
            }

            @Override
            public int[] getPotionsItemIds() {
                return Config.SERVICES_CPACP_POTIONS_ITEM_IDS;
            }

            @Override
            protected int getActMinPercent() {
                return Config.SERVICES_CPACP_MIN_PERCENT;
            }

            @Override
            protected int getActMaxPercent() {
                return Config.SERVICES_CPACP_MAX_PERCENT;
            }

            @Override
            protected int getActDefPercent() {
                return Config.SERVICES_CPACP_DEF_PERCENT;
            }
        },
        MPACP("MP") {
            @Override
            public boolean isEnabled() {
                return Config.SERVICES_MPACP_ENABLE;
            }

            @Override
            public void apply(final Player player) {
                for (final Listener<Creature> listener : player.getListeners().getListeners()) {
                    if (listener instanceof MPACPHelper) {
                        return;
                    }
                }
                player.addListener(new MPACPHelper(player));
            }

            @Override
            public void remove(final Player player) {
                for (final Listener<Creature> listener : player.getListeners().getListeners()) {
                    if (listener instanceof MPACPHelper) {
                        player.getListeners().remove(listener);
                    }
                }
            }

            @Override
            public int[] getPotionsItemIds() {
                return Config.SERVICES_MPACP_POTIONS_ITEM_IDS;
            }

            @Override
            protected int getActMinPercent() {
                return Config.SERVICES_MPACP_MIN_PERCENT;
            }

            @Override
            protected int getActMaxPercent() {
                return Config.SERVICES_MPACP_MAX_PERCENT;
            }

            @Override
            protected int getActDefPercent() {
                return Config.SERVICES_MPACP_DEF_PERCENT;
            }
        };

        public static final ACPType[] VALUES = values();
        private static final String ENABLE_VAR_NAME_SUFIX = "Enabled";
        private static final String ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX = "ActPercent";

        private final String _cfgName;

        ACPType(final String cfgName) {
            _cfgName = cfgName;
        }

        public String getCfgName() {
            return _cfgName;
        }

        public abstract boolean isEnabled();

        public boolean isEnabled(final Player player) {
            return isEnabled() && player.getVarB(name() + ACPType.ENABLE_VAR_NAME_SUFIX, false);
        }

        public int getActPercent(final Player player) {
            final int percent = player.getVarInt(name() + ACPType.ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX, getActDefPercent());
            return Math.min(Math.max(getActMinPercent(), percent), getActMaxPercent());
        }

        public int setActPercent(final Player player, int percent) {
            percent = Math.min(Math.max(getActMinPercent(), percent), getActMaxPercent());
            player.setVar(name() + ACPType.ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX, percent, -1L);
            return percent;
        }

        public void setEnabled(final Player player, final boolean enabled) {
            if (enabled) {
                player.setVar(name() + ACPType.ENABLE_VAR_NAME_SUFIX, "true", -1L);
                return;
            }
            player.unsetVar(name() + ACPType.ENABLE_VAR_NAME_SUFIX);
            player.unsetVar(name() + ACPType.ACT_PERCENT_VAR_NAME_VAR_NAME_SUFIX);
        }

        public abstract void apply(final Player p0);

        public abstract void remove(final Player p0);

        public abstract int[] getPotionsItemIds();

        protected abstract int getActMinPercent();

        protected abstract int getActMaxPercent();

        protected abstract int getActDefPercent();
    }

    private abstract static class ACPHelper implements Runnable, PlayerListener {
        private final HardReference<Player> _pleyerRef;
        private final AtomicReference<ACPHelperState> _state;
        private final ACPType _type;

        protected ACPHelper(final Player player, final ACPType type) {
            _type = type;
            _pleyerRef = player.getRef();
            _state = new AtomicReference<>(ACPHelperState.IDLE);
        }

        protected Player getPlayer() {
            return _pleyerRef.get();
        }

        public ACPType getType() {
            return _type;
        }

        protected void act(final Player player) {
            if (getPlayer() != player) {
                if (player != null) {
                    player.removeListener(this);
                }
                if (getPlayer() != null) {
                    getPlayer().removeListener(this);
                }
                return;
            }
            if (_state.compareAndSet(ACPHelperState.IDLE, ACPHelperState.APPLY)) {
                schedule(ACP_ACT_DELAY);
            }
        }

        @Override
        public void run() {
            final Player player = getPlayer();
            if (player == null) {
                return;
            }
            try {
                if (_state.compareAndSet(ACPHelperState.APPLY, ACPHelperState.USE)) {
                    use(player);
                }
            } catch (Exception ex) {
                ACP.LOG.error("Exception in ACP helper task", ex);
                _state.set(ACPHelperState.IDLE);
            }
        }

        private void schedule(final long delay) {
            ThreadPoolManager.getInstance().schedule(this, delay);
        }

        protected boolean canUse(final Player player) {
            return player != null && !player.isDead() && !player.isOutOfControl() && !player.isInStoreMode() && !player.isFishing() && !player.isHealBlocked() && !player.isOlyParticipant() && (Config.SERVICES_HPACP_WORK_IN_PEACE_ZONE || !player.isInZonePeace());
        }

        private void use(final Player player) {
            if (!canUse(player)) {
                _state.compareAndSet(ACPHelperState.USE, ACPHelperState.IDLE);
                return;
            }
            final ItemInstance potionItem = findUsableItem(player);
            if (potionItem == null) {
                _state.compareAndSet(ACPHelperState.USE, ACPHelperState.IDLE);
                return;
            }
            final long remaining = getReuseRemaining(potionItem, player);
            if (remaining <= 0L) {
                useItem(player, potionItem);
                if (canUse(player) && _state.compareAndSet(ACPHelperState.USE, ACPHelperState.APPLY)) {
                    schedule(ACP_ACT_DELAY);
                } else {
                    _state.compareAndSet(ACPHelperState.USE, ACPHelperState.IDLE);
                }
            } else if (_state.compareAndSet(ACPHelperState.USE, ACPHelperState.APPLY)) {
                schedule(ACP_ACT_DELAY + remaining);
            }
        }

        private long getReuseRemaining(final ItemInstance item, final Player player) {
            if (item.getTemplate().getReuseDelay() == 0L) {
                return 0L;
            }
            final TimeStamp timeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup());
            if (timeStamp == null || !timeStamp.hasNotPassed()) {
                return 0L;
            }
            return Math.max(0L, timeStamp.getEndTime() - System.currentTimeMillis());
        }

        private ItemInstance findUsableItem(final Player player) {
            if (player.isInStoreMode() || player.isOutOfControl()) {
                return null;
            }
            final int[] itemIds = _type.getPotionsItemIds();
            ItemInstance candidateItem = null;
            for (final int itemId : itemIds) {
                final ItemInstance item = player.getInventory().getItemByItemId(itemId);
                if (item != null) {
                    if (item.getTemplate().testCondition(player, item, false)) {
                        if (!player.getInventory().itemIsLocked(item)) {
                            if (candidateItem == null) {
                                candidateItem = item;
                            } else {
                                final TimeStamp itemTimeStamp = player.getSharedGroupReuse(item.getTemplate().getReuseGroup());
                                if (itemTimeStamp == null) {
                                    candidateItem = item;
                                } else {
                                    final TimeStamp candidateTimeStamp = player.getSharedGroupReuse(candidateItem.getTemplate().getReuseGroup());
                                    if (candidateTimeStamp != null && candidateTimeStamp.getEndTime() > itemTimeStamp.getEndTime()) {
                                        candidateItem = item;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return candidateItem;
        }

        private boolean useItem(final Player player, final ItemInstance item) {
            if (item.getTemplate().getHandler().useItem(player, item, false)) {
                final long nextTimeUse = item.getTemplate().getReuseType().next(item);
                if (nextTimeUse > System.currentTimeMillis()) {
                    final TimeStamp timeStamp = new TimeStamp(item.getItemId(), nextTimeUse, (long) item.getTemplate().getReuseDelay());
                    player.addSharedGroupReuse(item.getTemplate().getReuseGroup(), timeStamp);
                    if (item.getTemplate().getReuseDelay() > 0) {
                        player.sendPacket(new ExUseSharedGroupItem(item.getTemplate().getDisplayReuseGroup(), timeStamp));
                    }
                }
                return true;
            }
            return false;
        }

        enum ACPHelperState {
            IDLE,
            APPLY,
            USE
        }
    }

    private static final class HPACPHelper extends ACPHelper implements OnCurrentHpDamageListener {
        protected HPACPHelper(final Player player) {
            super(player, ACPType.HPACP);
        }

        @Override
        protected boolean canUse(final Player player) {
            if (super.canUse(player)) {
                final double useLim = player.calcStat(Stats.HP_LIMIT, null, null) * player.getMaxHp() / 100.0 * (getType().getActPercent(player) / 100.0);
                return player.getCurrentHp() < useLim;
            }
            return false;
        }

        @Override
        public void onCurrentHpDamage(final Creature actor, final double damage, final Creature attacker, final Skill skill) {
            final Player player = actor.getPlayer();
            if (player == null) {
                actor.removeListener(this);
                return;
            }
            if (!getType().isEnabled(player)) {
                actor.removeListener(this);
            }
            act(player);
        }
    }

    private static final class CPACPHelper extends ACPHelper implements OnCurrentHpDamageListener {
        protected CPACPHelper(final Player player) {
            super(player, ACPType.CPACP);
        }

        @Override
        protected boolean canUse(final Player player) {
            if (super.canUse(player)) {
                final double useLim = player.calcStat(Stats.CP_LIMIT, null, null) * player.getMaxCp() / 100.0 * (getType().getActPercent(player) / 100.0);
                return player.getCurrentCp() < useLim;
            }
            return false;
        }

        @Override
        public void onCurrentHpDamage(final Creature actor, final double damage, final Creature attacker, final Skill skill) {
            final Player player = actor.getPlayer();
            if (player == null) {
                actor.removeListener(this);
                return;
            }
            if (!getType().isEnabled(player)) {
                actor.removeListener(this);
            }
            act(player);
        }
    }

    private static final class MPACPHelper extends ACPHelper implements OnCurrentMpReduceListener {
        protected MPACPHelper(final Player player) {
            super(player, ACPType.MPACP);
        }

        @Override
        protected boolean canUse(final Player player) {
            if (super.canUse(player)) {
                final double useLim = player.calcStat(Stats.MP_LIMIT, null, null) * player.getMaxMp() / 100.0 * (getType().getActPercent(player) / 100.0);
                return player.getCurrentMp() < useLim;
            }
            return false;
        }

        @Override
        public void onCurrentMpReduce(final Creature actor, final double reduce, final Creature attacker) {
            final Player player = actor.getPlayer();
            if (player == null) {
                actor.removeListener(this);
                return;
            }
            if (!getType().isEnabled(player)) {
                actor.removeListener(this);
            }
            act(player);
        }
    }
}
