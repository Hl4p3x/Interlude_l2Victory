package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.MonsterRace;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class RaceManagerInstance extends NpcInstance {
    public static final int LANES = 8;
    public static final int WINDOW_START = 0;
    protected static final int[][] codes = {{-1, 0}, {0, 15322}, {13765, -1}};
    protected static final int[] cost = {100, 500, 1000, 5000, 10000, 20000, 50000, 100000};
    private static final long SECOND = 1000L;
    private static final long MINUTE = 60000L;
    private static final int ACCEPTING_BETS = 0;
    private static final int WAITING = 1;
    private static final int STARTING_RACE = 2;
    private static final int RACE_END = 3;
    protected static MonRaceInfo packet;
    private static List<Race> history;
    private static Set<RaceManagerInstance> managers;
    private static int _raceNumber;
    private static int minutes = 5;
    private static int state = 3;
    private static boolean notInitialized;

    static {
        _raceNumber = 1;
        notInitialized = true;
    }

    public RaceManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        if (notInitialized) {
            notInitialized = false;
            _raceNumber = ServerVariables.getInt("monster_race", 1);
            history = new ArrayList<>();
            managers = new CopyOnWriteArraySet<>();
            final ThreadPoolManager s = ThreadPoolManager.getInstance();
            s.scheduleAtFixedRate(new Announcement(816), 0L, 600000L);
            s.scheduleAtFixedRate(new Announcement(817), 30000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(816), 60000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(817), 90000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(818), 120000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(818), 180000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(818), 240000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(818), 300000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(819), 360000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(819), 420000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(820), 420000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(820), 480000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(821), 510000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(822), 530000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(823), 535000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(823), 536000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(823), 537000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(823), 538000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(823), 539000L, 600000L);
            s.scheduleAtFixedRate(new Announcement(824), 540000L, 600000L);
        }
        managers.add(this);
    }

    public void removeKnownPlayer(final Player player) {
        for (int i = 0; i < 8; ++i) {
            player.sendPacket(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
        }
    }

    public void makeAnnouncement(final int type) {
        final SystemMessage sm = new SystemMessage(type);
        switch (type) {
            case 816:
            case 817: {
                if (state != 0) {
                    state = 0;
                    startRace();
                }
                sm.addNumber(_raceNumber);
                break;
            }
            case 818:
            case 820:
            case 823: {
                sm.addNumber(minutes);
                sm.addNumber(_raceNumber);
                --minutes;
                break;
            }
            case 819: {
                sm.addNumber(_raceNumber);
                state = 1;
                minutes = 2;
                break;
            }
            case 822:
            case 825: {
                sm.addNumber(_raceNumber);
                minutes = 5;
                break;
            }
            case 826: {
                state = 3;
                sm.addNumber(MonsterRace.getInstance().getFirstPlace() + 1);
                sm.addNumber(MonsterRace.getInstance().getSecondPlace() + 1);
                break;
            }
        }
        broadcast(sm);
        if (type == 824) {
            state = 2;
            startRace();
            minutes = 5;
        }
    }

    protected void broadcast(final L2GameServerPacket pkt) {
        for (final RaceManagerInstance manager : managers) {
            if (!manager.isDead()) {
                manager.broadcastPacketToOthers(pkt);
            }
        }
    }

    public void sendMonsterInfo() {
        broadcast(packet);
    }

    private void startRace() {
        final MonsterRace race = MonsterRace.getInstance();
        if (state == 2) {
            final PlaySound SRace = new PlaySound("S_Race");
            broadcast(SRace);
            final PlaySound SRace2 = new PlaySound(Type.SOUND, "ItemSound2.race_start", 1, 121209259, new Location(12125, 182487, -3559));
            broadcast(SRace2);
            packet = new MonRaceInfo(codes[1][0], codes[1][1], race.getMonsters(), race.getSpeeds());
            sendMonsterInfo();
            ThreadPoolManager.getInstance().schedule(new RunRace(), 5000L);
        } else {
            race.newRace();
            race.newSpeeds();
            packet = new MonRaceInfo(codes[0][0], codes[0][1], race.getMonsters(), race.getSpeeds());
            sendMonsterInfo();
        }
    }

    @Override
    public void onBypassFeedback(final Player player, String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("BuyTicket") && state != 0) {
            player.sendPacket(Msg.MONSTER_RACE_TICKETS_ARE_NO_LONGER_AVAILABLE);
            command = "Chat 0";
        }
        if (command.startsWith("ShowOdds") && state == 0) {
            player.sendPacket(Msg.MONSTER_RACE_PAYOUT_INFORMATION_IS_NOT_AVAILABLE_WHILE_TICKETS_ARE_BEING_SOLD);
            command = "Chat 0";
        }
        if (command.startsWith("BuyTicket")) {
            int val = Integer.parseInt(command.substring(10));
            if (val == 0) {
                player.setRace(0, 0);
                player.setRace(1, 0);
            }
            if ((val == 10 && player.getRace(0) == 0) || (val == 20 && player.getRace(0) == 0 && player.getRace(1) == 0)) {
                val = 0;
            }
            showBuyTicket(player, val);
        } else if ("ShowOdds".equals(command)) {
            showOdds(player);
        } else if ("ShowInfo".equals(command)) {
            showMonsterInfo(player);
        } else if (!"calculateWin".equals(command)) {
            if (!"viewHistory".equals(command)) {
                super.onBypassFeedback(player, command);
            }
        }
    }

    public void showOdds(final Player player) {
        if (state == 0) {
            return;
        }
        final int npcId = getTemplate().npcId;
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        final String filename = getHtmlPath(npcId, 5, player);
        html.setFile(filename);
        for (int i = 0; i < 8; ++i) {
            final int n = i + 1;
            final String search = "Mob" + n;
            html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
        }
        html.replace("1race", String.valueOf(_raceNumber));
        player.sendPacket(html);
        player.sendActionFailed();
    }

    public void showMonsterInfo(final Player player) {
        final int npcId = getTemplate().npcId;
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        final String filename = getHtmlPath(npcId, 6, player);
        html.setFile(filename);
        for (int i = 0; i < 8; ++i) {
            final int n = i + 1;
            final String search = "Mob" + n;
            html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
        }
        player.sendPacket(html);
        player.sendActionFailed();
    }

    public void showBuyTicket(final Player player, final int val) {
        if (state != 0) {
            return;
        }
        final int npcId = getTemplate().npcId;
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        if (val < 10) {
            final String filename = getHtmlPath(npcId, 2, player);
            html.setFile(filename);
            for (int i = 0; i < 8; ++i) {
                final int n = i + 1;
                final String search = "Mob" + n;
                html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
            }
            final String search = "No1";
            if (val == 0) {
                html.replace(search, "");
            } else {
                html.replace(search, "" + val);
                player.setRace(0, val);
            }
        } else if (val < 20) {
            if (player.getRace(0) == 0) {
                return;
            }
            final String filename = getHtmlPath(npcId, 3, player);
            html.setFile(filename);
            html.replace("0place", "" + player.getRace(0));
            String search = "Mob1";
            final String replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
            html.replace(search, replace);
            search = "0adena";
            if (val == 10) {
                html.replace(search, "");
            } else {
                html.replace(search, "" + cost[val - 11]);
                player.setRace(1, val - 10);
            }
        } else if (val == 20) {
            if (player.getRace(0) == 0 || player.getRace(1) == 0) {
                return;
            }
            final String filename = getHtmlPath(npcId, 4, player);
            html.setFile(filename);
            html.replace("0place", "" + player.getRace(0));
            String search = "Mob1";
            final String replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
            html.replace(search, replace);
            search = "0adena";
            final int price = cost[player.getRace(1) - 1];
            html.replace(search, "" + price);
            search = "0tax";
            final int tax = 0;
            html.replace(search, "" + tax);
            search = "0total";
            final int total = price + tax;
            html.replace(search, "" + total);
        } else {
            if (player.getRace(0) == 0 || player.getRace(1) == 0) {
                return;
            }
            if (player.getAdena() < cost[player.getRace(1) - 1]) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                return;
            }
            final int ticket = player.getRace(0);
            final int priceId = player.getRace(1);
            player.setRace(0, 0);
            player.setRace(1, 0);
            player.reduceAdena(cost[priceId - 1], true);
            final SystemMessage sm = new SystemMessage(371);
            sm.addNumber(_raceNumber);
            sm.addItemName(4443);
            player.sendPacket(sm);
            final ItemInstance item = ItemFunctions.createItem(4443);
            item.setEnchantLevel(_raceNumber);
            item.setBlessed(ticket);
            item.setDamaged(cost[priceId - 1] / 100);
            player.getInventory().addItem(item);
            return;
        }
        html.replace("1race", String.valueOf(_raceNumber));
        player.sendPacket(html);
        player.sendActionFailed();
    }

    public MonRaceInfo getPacket() {
        return packet;
    }

    class Announcement extends RunnableImpl {
        private final int type;

        public Announcement(final int type) {
            this.type = type;
        }

        @Override
        public void runImpl() {
            makeAnnouncement(type);
        }
    }

    public class Race {
        private final Info[] info;

        public Race(final Info[] info) {
            this.info = info;
        }

        public Info getLaneInfo(final int lane) {
            return info[lane];
        }

        public class Info {
            private final int id;
            private final int place;
            private final int odds;
            private final int payout;

            public Info(final int id, final int place, final int odds, final int payout) {
                this.id = id;
                this.place = place;
                this.odds = odds;
                this.payout = payout;
            }

            public int getId() {
                return id;
            }

            public int getOdds() {
                return odds;
            }

            public int getPayout() {
                return payout;
            }

            public int getPlace() {
                return place;
            }
        }
    }

    class RunRace extends RunnableImpl {
        @Override
        public void runImpl() {
            packet = new MonRaceInfo(codes[2][0], codes[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
            sendMonsterInfo();
            ThreadPoolManager.getInstance().schedule(new RunEnd(), 30000L);
        }
    }

    class RunEnd extends RunnableImpl {
        @Override
        public void runImpl() {
            makeAnnouncement(826);
            makeAnnouncement(825);
            _raceNumber++;
            ServerVariables.set("monster_race", _raceNumber);
            for (int i = 0; i < 8; ++i) {
                broadcast(new DeleteObject(MonsterRace.getInstance().getMonsters()[i]));
            }
        }
    }
}
