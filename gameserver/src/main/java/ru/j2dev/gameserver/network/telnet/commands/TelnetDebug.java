package ru.j2dev.gameserver.network.telnet.commands;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.io.FileUtils;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.telnet.TelnetCommand;
import ru.j2dev.gameserver.network.telnet.TelnetCommandHolder;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TelnetDebug implements TelnetCommandHolder {
    private final Set<TelnetCommand> _commands;

    public TelnetDebug() {
        (_commands = new LinkedHashSet<>()).add(new TelnetCommand("dumpnpc", "dnpc") {
            @Override
            public String getUsage() {
                return "dumpnpc";
            }

            @Override
            public String handle(final String[] args) {
                final StringBuilder sb = new StringBuilder();
                int total = 0;
                int maxId = 0;
                int maxCount = 0;
                final TIntObjectHashMap<List<NpcInstance>> npcStats = new TIntObjectHashMap<>();
                for (final GameObject obj : GameObjectsStorage.getAllObjects()) {
                    if (obj.isCreature() && obj.isNpc()) {
                        final NpcInstance npc = (NpcInstance) obj;
                        final int id = npc.getNpcId();
                        List<NpcInstance> list;
                        if ((list = npcStats.get(id)) == null) {
                            npcStats.put(id, list = new ArrayList<>());
                        }
                        list.add(npc);
                        if (list.size() > maxCount) {
                            maxId = id;
                            maxCount = list.size();
                        }
                        ++total;
                    }
                }
                sb.append("Total NPCs: ").append(total).append("\n");
                sb.append("Maximum NPC ID: ").append(maxId).append(" count : ").append(maxCount).append("\n");
                final TIntObjectIterator<List<NpcInstance>> itr = npcStats.iterator();
                while (itr.hasNext()) {
                    itr.advance();
                    final int id2 = itr.key();
                    final List<NpcInstance> list = itr.value();
                    sb.append("=== ID: ").append(id2).append(" ").append(" Count: ").append(list.size()).append(" ===").append("\n");
                    for (final NpcInstance npc2 : list) {
                        try {
                            sb.append("AI: ");
                            if (npc2.hasAI()) {
                                sb.append(npc2.getAI().getClass().getName());
                            } else {
                                sb.append("none");
                            }
                            sb.append(", ");
                            if (npc2.getReflectionId() > 0) {
                                sb.append("ref: ").append(npc2.getReflectionId());
                                sb.append(" - ").append(npc2.getReflection().getName());
                            }
                            sb.append("loc: ").append(npc2.getLoc());
                            sb.append(", ");
                            sb.append("spawned: ");
                            sb.append(npc2.isVisible());
                            sb.append("\n");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    new File("stats").mkdir();
                    FileUtils.writeStringToFile(new File("stats/NpcStats-" + new SimpleDateFormat("MMddHHmmss").format(System.currentTimeMillis()) + ".txt"), sb.toString());
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
                return "NPC stats saved.\n";
            }
        });
        _commands.add(new TelnetCommand("asrestart") {
            @Override
            public String getUsage() {
                return "asrestart";
            }

            @Override
            public String handle(final String[] args) {
                AuthServerCommunication.getInstance().restart();
                return "Restarted.\n";
            }
        });
    }

    @Override
    public Set<TelnetCommand> getCommands() {
        return _commands;
    }
}
