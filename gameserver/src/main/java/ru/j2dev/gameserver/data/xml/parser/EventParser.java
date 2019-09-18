package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.data.xml.AbstractDirParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.EventHolder;
import ru.j2dev.gameserver.data.xml.holder.FStringHolder;
import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.EventType;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.events.actions.*;
import ru.j2dev.gameserver.model.entity.events.objects.*;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;
import ru.j2dev.gameserver.network.lineage2.components.SysString;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.*;

public final class EventParser extends AbstractDirParser<EventHolder> {

    protected EventParser() {
        super(EventHolder.getInstance());
    }

    public static EventParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLDir() {
        return new File(Config.DATAPACK_ROOT, "data/xml/events/");
    }

    @Override
    public boolean isIgnored(final File f) {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void readData(final EventHolder holder, final Element rootElement) throws Exception {
        for (Element eventElement : rootElement.getChildren("event")) {
            final int id = Integer.parseInt(eventElement.getAttributeValue("id"));
            final String name = eventElement.getAttributeValue("name");
            final String impl = eventElement.getAttributeValue("impl");
            final EventType type = EventType.valueOf(eventElement.getAttributeValue("type"));
            Class<GlobalEvent> eventClass;
            try {
                eventClass = (Class<GlobalEvent>) Class.forName("ru.j2dev.gameserver.model.entity.events.impl." + impl + "Event");
            } catch (ClassNotFoundException e) {
                info("Not found impl class: " + impl + "; File: " + getCurrentFileName());
                continue;
            }
            final Constructor<GlobalEvent> constructor = eventClass.getConstructor(MultiValueSet.class);
            final MultiValueSet<String> set = new MultiValueSet<>();
            set.set("id", id);
            set.set("name", name);
            eventElement.getChildren("parameter").forEach(parameterElement -> set.set(parameterElement.getAttributeValue("name"), parameterElement.getAttributeValue("value")));
            final GlobalEvent event = constructor.newInstance(set);
            event.addOnStartActions(parseActions(eventElement.getChild("on_start"), Integer.MAX_VALUE));
            event.addOnStopActions(parseActions(eventElement.getChild("on_stop"), Integer.MAX_VALUE));
            event.addOnInitActions(parseActions(eventElement.getChild("on_init"), Integer.MAX_VALUE));
            final Element onTime = eventElement.getChild("on_time");
            if (onTime != null) {
                onTime.getChildren("on").forEach(on -> {
                    final int time = Integer.parseInt(on.getAttributeValue("time"));
                    final List<EventAction> actions = parseActions(on, time);
                    event.addOnTimeActions(time, actions);
                });
            }
            eventElement.getChildren("objects").forEach(objectElement -> {
                final String objectsName = objectElement.getAttributeValue("name");
                final List<Serializable> objects = parseObjects(objectElement);
                event.addObjects(objectsName, objects);
            });
            holder.addEvent(type, event);
        }
    }

    private List<Serializable> parseObjects(final Element element) {
        if (element == null) {
            return Collections.emptyList();
        }
        final List<Serializable> objects = new ArrayList<>(2);
        for (final Element objectsElement : element.getChildren()) {
            final String nodeName = objectsElement.getName();
            if ("boat_point".equalsIgnoreCase(nodeName)) {
                objects.add(BoatPoint.parse(objectsElement));
            } else if ("point".equalsIgnoreCase(nodeName)) {
                objects.add(Location.parse(objectsElement));
            } else if ("spawn_ex".equalsIgnoreCase(nodeName)) {
                objects.add(new SpawnExObject(objectsElement.getAttributeValue("name")));
            } else if ("door".equalsIgnoreCase(nodeName)) {
                objects.add(new DoorObject(Integer.parseInt(objectsElement.getAttributeValue("id"))));
            } else if ("static_object".equalsIgnoreCase(nodeName)) {
                objects.add(new StaticObjectObject(Integer.parseInt(objectsElement.getAttributeValue("id"))));
            } else if ("siege_toggle_npc".equalsIgnoreCase(nodeName)) {
                final int id = Integer.parseInt(objectsElement.getAttributeValue("id"));
                final int fakeId = Integer.parseInt(objectsElement.getAttributeValue("fake_id"));
                final int x = Integer.parseInt(objectsElement.getAttributeValue("x"));
                final int y = Integer.parseInt(objectsElement.getAttributeValue("y"));
                final int z = Integer.parseInt(objectsElement.getAttributeValue("z"));
                final int hp = Integer.parseInt(objectsElement.getAttributeValue("hp"));
                Set<String> set = Collections.emptySet();
                for (final Element sub : objectsElement.getChildren()) {
                    if (set.isEmpty()) {
                        set = new HashSet<>();
                    }
                    set.add(sub.getAttributeValue("name"));
                }
                objects.add(new SiegeToggleNpcObject(id, fakeId, new Location(x, y, z), hp, set));
            } else if ("castle_zone".equalsIgnoreCase(nodeName)) {
                final long price = Long.parseLong(objectsElement.getAttributeValue("price"));
                objects.add(new CastleDamageZoneObject(objectsElement.getAttributeValue("name"), price));
            } else if ("zone".equalsIgnoreCase(nodeName)) {
                objects.add(new ZoneObject(objectsElement.getAttributeValue("name")));
            } else {
                if (!"ctb_team".equalsIgnoreCase(nodeName)) {
                    continue;
                }
                final int mobId = Integer.parseInt(objectsElement.getAttributeValue("mob_id"));
                final int flagId = Integer.parseInt(objectsElement.getAttributeValue("id"));
                final Location loc = Location.parse(objectsElement);
                objects.add(new CTBTeamObject(mobId, flagId, loc));
            }
        }
        return objects;
    }

    private List<EventAction> parseActions(final Element element, final int time) {
        if (element == null) {
            return Collections.emptyList();
        }
        IfElseAction lastIf = null;
        final List<EventAction> actions = new ArrayList<>(0);
        for (final Element actionElement : element.getChildren()) {
            if ("start".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final StartStopAction startStopAction = new StartStopAction(name, true);
                actions.add(startStopAction);
            } else if ("stop".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final StartStopAction startStopAction = new StartStopAction(name, false);
                actions.add(startStopAction);
            } else if ("spawn".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, true);
                actions.add(spawnDespawnAction);
            } else if ("despawn".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final SpawnDespawnAction spawnDespawnAction = new SpawnDespawnAction(name, false);
                actions.add(spawnDespawnAction);
            } else if ("open".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final OpenCloseAction a = new OpenCloseAction(true, name);
                actions.add(a);
            } else if ("close".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final OpenCloseAction a = new OpenCloseAction(false, name);
                actions.add(a);
            } else if ("active".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final ActiveDeactiveAction a2 = new ActiveDeactiveAction(true, name);
                actions.add(a2);
            } else if ("deactive".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final ActiveDeactiveAction a2 = new ActiveDeactiveAction(false, name);
                actions.add(a2);
            } else if ("refresh".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final RefreshAction a3 = new RefreshAction(name);
                actions.add(a3);
            } else if ("init".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final InitAction a4 = new InitAction(name);
                actions.add(a4);
            } else if ("npc_say".equalsIgnoreCase(actionElement.getName())) {
                final int npc = Integer.parseInt(actionElement.getAttributeValue("npc"));
                final ChatType chat = ChatType.valueOf(actionElement.getAttributeValue("chat"));
                final int range = Integer.parseInt(actionElement.getAttributeValue("range"));
                final int fsstring = NpcString.valueOf(actionElement.getAttributeValue("text")).getId();
                final NpcSayAction action = new NpcSayAction(npc, range, chat, fsstring);
                actions.add(action);
            } else if ("play_sound".equalsIgnoreCase(actionElement.getName())) {
                final int range2 = Integer.parseInt(actionElement.getAttributeValue("range"));
                final String sound = actionElement.getAttributeValue("sound");
                final Type type = Type.valueOf(actionElement.getAttributeValue("type"));
                final PlaySoundAction action2 = new PlaySoundAction(range2, sound, type);
                actions.add(action2);
            } else if ("give_item".equalsIgnoreCase(actionElement.getName())) {
                final int itemId = Integer.parseInt(actionElement.getAttributeValue("id"));
                final long count = Integer.parseInt(actionElement.getAttributeValue("count"));
                final GiveItemAction action3 = new GiveItemAction(itemId, count);
                actions.add(action3);
            } else if ("announce".equalsIgnoreCase(actionElement.getName())) {
                final String val = actionElement.getAttributeValue("val");
                if (val == null && time == Integer.MAX_VALUE) {
                    info("Can't get announce time." + getCurrentFileName());
                } else {
                    final int val2 = (val == null) ? time : Integer.parseInt(val);
                    final EventAction action4 = new AnnounceAction(val2);
                    actions.add(action4);
                }
            } else if ("if".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final IfElseAction action5 = new IfElseAction(name, false);
                action5.setIfList(parseActions(actionElement, time));
                actions.add(action5);
                lastIf = action5;
            } else if ("ifnot".equalsIgnoreCase(actionElement.getName())) {
                final String name = actionElement.getAttributeValue("name");
                final IfElseAction action5 = new IfElseAction(name, true);
                action5.setIfList(parseActions(actionElement, time));
                actions.add(action5);
                lastIf = action5;
            } else if ("else".equalsIgnoreCase(actionElement.getName())) {
                if (lastIf == null) {
                    info("Not find <if> for <else> tag");
                } else {
                    lastIf.setElseList(parseActions(actionElement, time));
                }
            } else if ("say".equalsIgnoreCase(actionElement.getName())) {
                final ChatType chat2 = ChatType.valueOf(actionElement.getAttributeValue("chat"));
                final int range3 = Integer.parseInt(actionElement.getAttributeValue("range"));
                final String how = actionElement.getAttributeValue("how");
                final String text = actionElement.getAttributeValue("text");
                final SysString sysString = SysString.valueOf2(how);
                SayAction sayAction;
                if (sysString != null) {
                    sayAction = new SayAction(range3, chat2, sysString, SystemMsg.valueOf(text));
                } else {
                    sayAction = new SayAction(range3, chat2, how, FStringHolder.getInstance().getTemplate(NpcString.valueOf(text).getId()).getEn());
                }
                actions.add(sayAction);
            } else {
                if (!"teleport_players".equalsIgnoreCase(actionElement.getName())) {
                    continue;
                }
                final String name = actionElement.getAttributeValue("id");
                final TeleportPlayersAction a5 = new TeleportPlayersAction(name);
                actions.add(a5);
            }
        }
        return actions.isEmpty() ? Collections.emptyList() : actions;
    }

    private static class LazyHolder {
        protected static final EventParser INSTANCE = new EventParser();
    }
}
