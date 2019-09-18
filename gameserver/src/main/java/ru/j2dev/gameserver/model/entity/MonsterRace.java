package ru.j2dev.gameserver.model.entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.lang.reflect.Constructor;

public class MonsterRace {
    private static final Logger LOGGER = LoggerFactory.getLogger(MonsterRace.class);
    private static MonsterRace _instance;

    private final NpcInstance[] monsters = new NpcInstance[8];
    private final int[] first = new int[2];
    private final int[] second = new int[2];
    private int[][] speeds = new int[8][20];

    private MonsterRace() {
    }

    public static MonsterRace getInstance() {
        if (_instance == null) {
            _instance = new MonsterRace();
        }
        return _instance;
    }

    public void newRace() {
        int random;
        for (int i = 0; i < 8; ++i) {
            final int id = 31003;
            random = Rnd.get(24);
            for (int j = i - 1; j >= 0; --j) {
                if (monsters[j].getTemplate().npcId == id + random) {
                    random = Rnd.get(24);
                }
            }
            try {
                final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(id + random);
                Constructor<?> _constructor = template.getInstanceConstructor();
                final int objectId = IdFactory.getInstance().getNextId();
                monsters[i] = (NpcInstance) _constructor.newInstance(objectId, template);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        newSpeeds();
    }

    public void newSpeeds() {
        speeds = new int[8][20];
        int total;
        first[1] = 0;
        second[1] = 0;
        for (int i = 0; i < 8; ++i) {
            total = 0;
            for (int j = 0; j < 20; ++j) {
                if (j == 19) {
                    speeds[i][j] = 100;
                } else {
                    speeds[i][j] = Rnd.get(65, 124);
                }
                total += speeds[i][j];
            }
            if (total >= first[1]) {
                second[0] = first[0];
                second[1] = first[1];
                first[0] = 8 - i;
                first[1] = total;
            } else if (total >= second[1]) {
                second[0] = 8 - i;
                second[1] = total;
            }
        }
    }

    public NpcInstance[] getMonsters() {
        return monsters;
    }

    public int[][] getSpeeds() {
        return speeds;
    }

    public int getFirstPlace() {
        return 8 - first[0];
    }

    public int getSecondPlace() {
        return 8 - second[0];
    }
}
