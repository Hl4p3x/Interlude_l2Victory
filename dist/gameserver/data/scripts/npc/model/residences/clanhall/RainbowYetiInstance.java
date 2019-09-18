package npc.model.residences.clanhall;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import ru.j2dev.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public class RainbowYetiInstance extends NpcInstance {
    private static final int ItemA = 8035;
    private static final int ItemB = 8036;
    private static final int ItemC = 8037;
    private static final int ItemD = 8038;
    private static final int ItemE = 8039;
    private static final int ItemF = 8040;
    private static final int ItemG = 8041;
    private static final int ItemH = 8042;
    private static final int ItemI = 8043;
    private static final int ItemK = 8045;
    private static final int ItemL = 8046;
    private static final int ItemN = 8047;
    private static final int ItemO = 8048;
    private static final int ItemP = 8049;
    private static final int ItemR = 8050;
    private static final int ItemS = 8051;
    private static final int ItemT = 8052;
    private static final int ItemU = 8053;
    private static final int ItemW = 8054;
    private static final int ItemY = 8055;
    private static final Word[] WORLD_LIST;

    static {
        (WORLD_LIST = new Word[8])[0] = new Word("BABYDUCK", new int[][]{{8036, 2}, {8035, 1}, {8055, 1}, {8038, 1}, {8053, 1}, {8037, 1}, {8045, 1}});
        RainbowYetiInstance.WORLD_LIST[1] = new Word("ALBATROS", new int[][]{{8035, 2}, {8046, 1}, {8036, 1}, {8052, 1}, {8050, 1}, {8048, 1}, {8051, 1}});
        RainbowYetiInstance.WORLD_LIST[2] = new Word("PELICAN", new int[][]{{8049, 1}, {8039, 1}, {8046, 1}, {8043, 1}, {8037, 1}, {8035, 1}, {8047, 1}});
        RainbowYetiInstance.WORLD_LIST[3] = new Word("KINGFISHER", new int[][]{{8045, 1}, {8043, 1}, {8047, 1}, {8041, 1}, {8040, 1}, {8043, 1}, {8051, 1}, {8042, 1}, {8039, 1}, {8050, 1}});
        RainbowYetiInstance.WORLD_LIST[4] = new Word("CYGNUS", new int[][]{{8037, 1}, {8055, 1}, {8041, 1}, {8047, 1}, {8053, 1}, {8051, 1}});
        RainbowYetiInstance.WORLD_LIST[5] = new Word("TRITON", new int[][]{{8052, 2}, {8050, 1}, {8043, 1}, {8047, 1}});
        RainbowYetiInstance.WORLD_LIST[6] = new Word("RAINBOW", new int[][]{{8050, 1}, {8035, 1}, {8043, 1}, {8047, 1}, {8036, 1}, {8048, 1}, {8054, 1}});
        RainbowYetiInstance.WORLD_LIST[7] = new Word("SPRING", new int[][]{{8051, 1}, {8049, 1}, {8050, 1}, {8043, 1}, {8047, 1}, {8041, 1}});
    }

    private final List<GameObject> _mobs;
    private int _generated;
    private Future<?> _task;

    public RainbowYetiInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _mobs = new ArrayList<>();
        _generated = -1;
        _task = null;
        _hasRandomWalk = false;
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        final ClanHallMiniGameEvent event = getEvent(ClanHallMiniGameEvent.class);
        if (event == null) {
            return;
        }
        final List<Player> around = World.getAroundPlayers(this, 750, 100);
        for (final Player player : around) {
            final CMGSiegeClanObject siegeClanObject = event.getSiegeClan("attackers", player.getClan());
            if (siegeClanObject == null || !siegeClanObject.getPlayers().contains(player.getObjectId())) {
                player.teleToLocation(event.getResidence().getOtherRestartPoint());
            }
        }
        _task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new GenerateTask(), 10000L, 300000L);
    }

    @Override
    public void onDelete() {
        super.onDelete();
        if (_task != null) {
            _task.cancel(false);
            _task = null;
        }
        for (final GameObject object : _mobs) {
            object.deleteMe();
        }
        _mobs.clear();
    }

    public void teleportFromArena() {
        final ClanHallMiniGameEvent event = getEvent(ClanHallMiniGameEvent.class);
        if (event == null) {
            return;
        }
        final List<Player> around = World.getAroundPlayers(this, 750, 100);
        for (final Player player : around) {
            player.teleToLocation(event.getResidence().getOtherRestartPoint());
        }
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("get".equalsIgnoreCase(command)) {
            final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
            boolean has = true;
            if (_generated == -1) {
                has = false;
            } else {
                final Word word = RainbowYetiInstance.WORLD_LIST[_generated];
                for (final int[] itemInfo : word.getItems()) {
                    if (player.getInventory().getCountOf(itemInfo[0]) < itemInfo[1]) {
                        has = false;
                    }
                }
                if (has) {
                    for (final int[] itemInfo : word.getItems()) {
                        if (!player.consumeItem(itemInfo[0], (long) itemInfo[1])) {
                            return;
                        }
                    }
                    final int rnd = Rnd.get(100);
                    if (_generated >= 0 && _generated <= 5) {
                        if (rnd < 70) {
                            addItem(player, 8030);
                        } else if (rnd < 80) {
                            addItem(player, 8031);
                        } else if (rnd < 90) {
                            addItem(player, 8032);
                        } else {
                            addItem(player, 8033);
                        }
                    } else if (rnd < 10) {
                        addItem(player, 8030);
                    } else if (rnd < 40) {
                        addItem(player, 8031);
                    } else if (rnd < 70) {
                        addItem(player, 8032);
                    } else {
                        addItem(player, 8033);
                    }
                }
            }
            if (!has) {
                msg.setFile("residence2/clanhall/watering_manager002.htm");
            } else {
                msg.setFile("residence2/clanhall/watering_manager004.htm");
            }
            player.sendPacket(msg);
        } else if ("see".equalsIgnoreCase(command)) {
            final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
            msg.setFile("residence2/clanhall/watering_manager005.htm");
            if (_generated == -1) {
                msg.replaceNpcString("%word%", NpcString.UNDECIDED);
            } else {
                msg.replace("%word%", RainbowYetiInstance.WORLD_LIST[_generated].getName());
            }
            player.sendPacket(msg);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private void addItem(final Player player, final int itemId) {
        final ClanHallMiniGameEvent event = getEvent(ClanHallMiniGameEvent.class);
        if (event == null) {
            return;
        }
        final ItemInstance item = ItemFunctions.createItem(itemId);
        item.addEvent(event);
        player.getInventory().addItem(item);
        player.sendPacket(SystemMessage2.obtainItems(item));
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        showChatWindow(player, "residence2/clanhall/watering_manager001.htm");
    }

    public void addMob(final GameObject object) {
        _mobs.add(object);
    }

    private static class Word {
        private final String _name;
        private final int[][] _items;

        public Word(final String name, final int[]... items) {
            _name = name;
            _items = items;
        }

        public String getName() {
            return _name;
        }

        public int[][] getItems() {
            return _items;
        }
    }

    private class GenerateTask extends RunnableImpl {
        @Override
        public void runImpl() {
            _generated = Rnd.get(RainbowYetiInstance.WORLD_LIST.length);
            final Word word = RainbowYetiInstance.WORLD_LIST[_generated];
            final List<Player> around = World.getAroundPlayers(RainbowYetiInstance.this, 750, 100);
            final ExShowScreenMessage msg = new ExShowScreenMessage(word.getName(), 5000, ScreenMessageAlign.TOP_CENTER, true);
            for (final Player player : around) {
                player.sendPacket(msg);
            }
        }
    }
}
