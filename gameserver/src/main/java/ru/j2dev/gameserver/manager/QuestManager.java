package ru.j2dev.gameserver.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.model.quest.Quest;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestManager.class);
    public static final int TUTORIAL_QUEST = 255;
    private static Map<String, Quest> _questsByName = new ConcurrentHashMap<>();
    private static Map<Integer, Quest> _questsById = new ConcurrentHashMap<>();

    public static Quest getQuest(final String name) {
        return _questsByName.get(name);
    }

    public static Quest getQuest(final Class<?> quest) {
        return getQuest(quest.getSimpleName());
    }

    public static Quest getQuest(final int questId) {
        return _questsById.get(questId);
    }

    public static Quest getQuest2(final String nameOrId) {
        if (_questsByName.containsKey(nameOrId)) {
            return _questsByName.get(nameOrId);
        }
        try {
            final int questId = Integer.parseInt(nameOrId);
            return _questsById.get(questId);
        } catch (Exception e) {
            return null;
        }
    }

    public static void addQuest(final Quest newQuest) {
        _questsByName.put(newQuest.getName(), newQuest);
        _questsById.put(newQuest.getQuestIntId(), newQuest);
    }

    public static Collection<Quest> getQuests() {
        return _questsByName.values();
    }
}
